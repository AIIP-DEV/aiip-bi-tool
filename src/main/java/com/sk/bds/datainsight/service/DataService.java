package com.sk.bds.datainsight.service;

import au.com.bytecode.opencsv.CSVWriter;
import com.sk.bds.datainsight.database.dao.DataSetDao;
import com.sk.bds.datainsight.database.dao.SettingDao;
import com.sk.bds.datainsight.database.dao.UserDao;
import com.sk.bds.datainsight.database.model.*;
import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.exception.InternalException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.util.DataConverter;
import com.sk.bds.datainsight.util.DataSourceManager;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Service
public class DataService {
    private static final Logger log = LoggerFactory.getLogger(DataService.class);

    private final String[][] META_QUERY = {
            {"SHOW TABLES", "DESC %s", "SELECT %s FROM %s LIMIT 100"},
            {"SHOW TABLES", "DESC %s", "SELECT %s FROM %s LIMIT 100"},
            {"SHOW TABLES", "DESCRIBE %s", "SELECT %s FROM %s LIMIT 100"},
            {"SHOW TABLES", "DESC %s", "SELECT %s FROM %s LIMIT 100"},
            {"SHOW TABLES", "DESC %s", "SELECT %s FROM %s LIMIT 100"},
            {"SHOW TABLES", "DESC %s", "SELECT %s FROM %s LIMIT 100"}
    };

    public final static int META_TYPE_TABLE = 0;
    public final static int META_TYPE_SCHEMA = 1;
    public final static int META_TYPE_SAMPLE = 2;

    public final static int MARIADB_MAX_COLUMN_COUNT = 1000;

    @Autowired
    UserDao userDao;
    @Autowired
    SettingDao settingDao;

    @Value("${icos.api}")
    String icosAPI;
    @Value("${icos.service}")
    String icosService;
    @Value("${schedule.url}")
    String scheduleUrl;

    public DataService() {}

    public DataService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean connectTest(int type, Map<String, Object> param) {
        boolean result = true;
        try {
            DataSourceManager.getDataSource(type, param).getConnection().close();
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    //데이터셋 원격 접속 정보 가져오는 함수(모의해킹에 대한 소스 수정) 2019-03
    public DataSet getDataSetConnInfo(int userId, String dataSetID) throws Exception {
        DataSetDao dao = getDao(userId);
        try {
            DataSet ds = dao.getDataSet(dataSetID);
            return ds;
        }
        finally {
            dao.close();
        }
    }

    public Object getTableList(int type, Map<String, Object> param) throws Exception {
        String query = META_QUERY[type][META_TYPE_TABLE];
        SingleConnectionDataSource sds = DataSourceManager.getDataSource(type, param);
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(sds);
            Object result = jdbcTemplate.queryForList(query, String.class);
            return result;
        } catch (Exception e) {
            log.error("getTableList error: {}", e.getMessage());
            BadException exception = new BadException("테이블 조회에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            exception.setExceptionMessage(e.getMessage());
            throw exception;
        } finally {
            if (sds != null) {
                try {
                    sds.getConnection().close();
                } catch (Exception e) {}
            }
        }
    }

    public Object getSample(int type, Map<String, Object> param) throws Exception {
        String schemaQuery = String.format(META_QUERY[type][META_TYPE_SCHEMA], param.get("tableName"));
        String sampleQuery = META_QUERY[type][META_TYPE_SAMPLE];
        SingleConnectionDataSource sds = DataSourceManager.getDataSource(type, param);
        List<List<String>> change = (List<List<String>>)param.get("changeInfo");
        List<Map<String, Object>> addColumn = (List<Map<String, Object>>)param.get("addColumn");
        List<Map<String, Object>> filterGroup = (List<Map<String, Object>>)param.get("filterGroup");
        JdbcTemplate jdbcTemplate = null;
        try {
            HashMap<String, Object> result = new HashMap<>();
            ArrayList<String[]> original = new ArrayList<>();
            ArrayList<String[]> newColumns = new ArrayList<>();
            jdbcTemplate = new JdbcTemplate(sds);
            if (type != DataSourceManager.HIVE) {
                jdbcTemplate.setQueryTimeout(0);
            }
            List<Map<String, Object>> schemaData = jdbcTemplate.queryForList(schemaQuery);
            StringBuffer columns = new StringBuffer();
            int index = 0;
            int columnCount = 0;
            ArrayList<Object> userColumn = new ArrayList<>();
            for (Map<String, Object> data : schemaData) {
                columnCount++;
                String nameKey = null;
                String typeKey = null;
                switch(type) {
                    case DataSourceManager.MYSQL:
                    case DataSourceManager.MARIADB:
                    case DataSourceManager.RDS:
                    case DataSourceManager.FILE:
                    case DataSourceManager.ICOS:
                        nameKey = "Field";
                        typeKey = "Type";
                        break;
                    case DataSourceManager.HIVE:
                        nameKey = "col_name";
                        typeKey = "data_type";
                        break;
                }
                Object columnData = data.get(nameKey);
                if ("".equals(columnData.toString().trim()) || columnData.toString().contains(" ") || userColumn.contains(columnData) || columnData == null) {
                    continue;
                }
                userColumn.add(columnData);
                original.add(new String[]{data.get(nameKey).toString(), getType(data.get(typeKey))});
                if (change != null) {
                    columns.append(String.format("%s AS `%s`,", getChangeType(data.get(nameKey), change.get(index).get(1), true),
                            data.get(nameKey).toString()));
                    newColumns.add(new String[]{change.get(index).get(0), change.get(index).get(1)});
                    index++;
                } else {
                    columns.append(String.format("`%s`,", data.get(nameKey).toString()));
                    newColumns.add(new String[]{data.get(nameKey).toString(), getType(data.get(typeKey))});
                }
            }
            if (addColumn != null) {
                for (Map<String, Object> add : addColumn) {
                    columnCount++;
                    String newColumn = getChangeType(String.format(add.get("formula").toString(), add.get("originalColumn")),
                            getType(add.get("type")), false);
                    columns.append(String.format("%s AS `%s`,", newColumn, add.get("name")));
                }
            }

            String whereStr = null;
            if (filterGroup != null) {
                whereStr = getFilterGroupWhere(filterGroup);
            }
            if (whereStr != null) {
                sampleQuery = String.format(sampleQuery, columns.substring(0, columns.length() - 1),
                        String.format("%s WHERE %s", param.get("tableName"), whereStr));
            } else {
                sampleQuery = String.format(sampleQuery, columns.substring(0, columns.length() - 1), param.get("tableName"));
            }
            if (columnCount * 100 > 2000) {
                sampleQuery = sampleQuery.replace("LIMIT 100", "LIMIT 50");
            }
            log.info("sampleQuery: {}", sampleQuery);
            Object sampleResult = jdbcTemplate.queryForList(sampleQuery);
            result.put("originalColumn", original);
            result.put("column", newColumns);
            result.put("sample", sampleResult);
            return result;
        } catch (Exception e) {
            log.error("getSample error: {}", e.getMessage());
            BadException exception = new BadException("샘플 데이터 조회에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            exception.setExceptionMessage(e.getMessage());
            throw exception;
        } finally {
            if (sds != null) {
                try {
                    sds.getConnection().close();
                } catch (Exception e) {}
            }
            if (jdbcTemplate != null) {
                try {
                    jdbcTemplate.getDataSource().getConnection().close();
                } catch (Exception e) {}
            }
        }
    }

    public Integer createDataSet(int userId, Map<String, Object> param, String auth) throws Exception {
        Integer resultID = null;
        DataSetDao dao = getDao(userId);
        String tableName = String.format("DATA_SET_TABLE_%d", System.currentTimeMillis());
        List<List<String>> changeInfo = (List<List<String>>)param.get("changeInfo");
        List<List<String>> originalInfo = (List<List<String>>)param.get("originalInfo");
        List<Map<String, Object>> addColumn = (List<Map<String, Object>>)param.get("addColumn");
        List<Map<String, Object>> filterGroup = (List<Map<String, Object>>)param.get("filterGroup");

        Map<String, Object> createInfo = getCreateInfo(changeInfo, originalInfo, addColumn);
        Map<String, String> columnNameMap = (Map<String, String>)createInfo.get("columnNameMap");
        try {
            dao.transactionStart();
            Map<String, Object> srcCon = (Map<String, Object>)param.get("srcConnection");
            int srcType = Integer.parseInt(srcCon.get("type").toString());
            DataSet dataSet = new DataSet(param.get("name").toString(), DataSourceManager.SOURCE_NAME[srcType], new JSONObject(srcCon).toString(),
                    tableName, param.get("useColumns").toString(), originalInfo, changeInfo, true);
            int key = dao.insertDataSet(dataSet);
            if (param.get("groupId") != null) {
                dao.insertDataSetGroupMapping(key, Integer.parseInt(String.valueOf(param.get("groupId"))));
            }
            if (filterGroup != null && filterGroup.size() > 0) {
                for (Map<String, Object> fg : filterGroup) {
                    int groupId = dao.insertDataSetFilterGroup(key, fg.get("name").toString(), fg.get("enable").toString());
                    insertFilter(dao, (List<Map<String, Object>>)fg.get("filter"), groupId, columnNameMap);
                }
            }
            insertAddColumn(dao, addColumn, key, columnNameMap);
            dao.transactionEnd();
            new CreateDataSetThread(userId, key, tableName, param, auth).start();
            resultID = key;
        } catch (Exception e) {
            log.error("createDataSet error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage("데이서셋 생성에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
        return resultID;

    }

    //데이터셋이 있는 데이터베이스 접속 정보 제거(모의해킹에 의한 소스 수정) 2019-03
    public Object getDataSetList(int userId, JSONObject param) throws Exception {
        DataSetDao dao = getDao(userId);
        try {
            List<Integer> idList = new ArrayList<>();
            Map<Integer, DataSet> dataSetMap = new HashMap<>();
            List<DataSet> dataSetList = dao.getDataSetList(param);
            int count = dao.getDataSetListCount(param);
            int isUpdate = dao.getDataSetUpdateCount();

            for(DataSet ds : dataSetList){
                String conInfoString = "";
                String srcConnection = ds.getSrcConnection();

                JSONObject obj = new JSONObject(srcConnection);

                Iterator<String> stringIterator = obj.keys();

                while(stringIterator.hasNext()) {
                    String key = stringIterator.next();
                    if(key.equals("host") || key.equals("id") || key.equals("pwd")){
                        conInfoString = conInfoString + "\""+ key + "\"" + ":" + "\"\"";
                    }
                    else {
                        conInfoString = conInfoString + "\""+ key + "\"" + ":" + "\""+ obj.get(key) + "\"";
                    }
                    conInfoString = conInfoString + ",";
                }
                conInfoString = "{"+conInfoString.substring(0,conInfoString.length()-1)+"}";
                ds.setSrcConnection(conInfoString);
                idList.add(ds.getId());
                dataSetMap.put(ds.getId(), ds);
            }
            if (idList.size() > 0) {
                List<Map<String, Object>> countList = settingDao.getDataSetScheduleCount(idList);
                for (Map<String, Object> countData : countList) {
                    dataSetMap.get(countData.get("id")).setScheduleCount(Integer.parseInt(countData.get("cnt").toString()));
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("list", dataSetList);
            result.put("total", count);
            result.put("isUpdate", isUpdate != 0);
            return result;

        } finally {
            dao.close();
        }
    }

    public Map<String, Object> getDataSetDetail(int userId, String dataSetId, boolean isSample) throws Exception {
        Map<String, Object> result = getData(userId, dataSetId, isSample, null, null);
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("dataSetId", dataSetId);
        result.put("schedule", settingDao.getDataSetSchedule(param));
        return result;
    }

    private Map<String, Object> getData(int userId, String dataSetId, boolean isSample, List<DataSetAddColumn> addColumnList, List<Map<String, Object>> filterGroup) throws Exception {
        Map<String, Object> result = new HashMap<>();
        DataSetDao dao = getDao(userId);
        String limit = "";
        if (isSample) {
            limit = "LIMIT 100";
        }
        try {
            DataSet dataSet = dao.getDataSet(dataSetId);
            if (dataSet == null) {
                BadException e = new BadException(String.format("Not found DataSet: %s", dataSetId));
                throw e;
            }
            result.put("groupId", dataSet.getGroupId());
            result.put("name", dataSet.getName());
            result.put("srcType", dataSet.getSrcType());
            result.put("useColumns", new JSONArray(dataSet.getUseColumns()).join(",").replaceAll("\"", "").split(","));
            List<Map<String, Object>> schemaData = dao.getColumnInfo(dataSet.getDataTable());
            List<DataSetAddColumn> addColumn = dao.getDataSetAddColumn(dataSetId);
            if (addColumnList != null) {
                addColumn.addAll(addColumnList);
            }
            int columnCount = 0;
            List<String> addColumnNameList = new ArrayList<>();
            for (DataSetAddColumn column : addColumn) {
                addColumnNameList.add(column.getName());
                columnCount++;
            }
            ArrayList<String[]> originalInfo = new ArrayList<>();
            for (Map<String, Object> data : schemaData) {
                String field = data.get("Field").toString();
                if (!addColumnNameList.contains(field)) {
                    originalInfo.add(new String[]{data.get("Field").toString(), getType(data.get("Type"))});
                    columnCount++;
                }
            }
            result.put("originalInfo", originalInfo);
            result.put("addColumn", addColumn);
            List<Filter> filter = dao.getDataSetFilter(dataSetId);
            String whereStr = null;
            if (filterGroup != null) {
                whereStr = getFilterGroupWhere(filterGroup);
            } else {
                List<DataSetFilterGroup> dbFilterGroup = dao.getDataSetFilterGroup(dataSetId);
                for (DataSetFilterGroup fg : dbFilterGroup) {
                    fg.addFilter(filter);
                }
                result.put("filterGroup", dbFilterGroup);
                whereStr = getWhereStrFromDB(dbFilterGroup);
            }

            String query = null;
            String addColumnQuery = null;
            if (addColumnList != null) {
                for (DataSetAddColumn addColumnData : addColumnList) {
                    String formula = String.format(addColumnData.getFormula(), addColumnData.getOriginalColumn());
                    if (addColumnQuery == null) {
                        addColumnQuery = String.format("%s AS `%s`", getChangeType(formula, addColumnData.getType(), false), addColumnData.getName());
                    } else {
                        addColumnQuery = String.format("%s, %s AS `%s`", addColumnQuery, getChangeType(formula, addColumnData.getType(), false), addColumnData.getName());
                    }
                }
            }
            if (isSample && columnCount * 100 > 2000) {
                limit = "LIMIT 50";
            }
            if (whereStr != null) {
                if (addColumnQuery != null) {
                    query = String.format("SELECT *, %s FROM %s WHERE %s %s", addColumnQuery, dataSet.getDataTable(), whereStr, limit);
                } else {
                    query = String.format("SELECT * FROM %s WHERE %s %s", dataSet.getDataTable(), whereStr, limit);
                }
            } else {
                if (addColumnQuery != null) {
                    query = String.format("SELECT *, %s FROM %s %s", addColumnQuery, dataSet.getDataTable(), limit);
                } else {
                    query = String.format("SELECT * FROM %s %s", dataSet.getDataTable(), limit);
                }
            }
            log.info("query: {}", query);
            result.put("sample", dao.getDataSetTable(query));
            return result;
        } catch (BadException be) {
            log.warn("getDataSetDetail error: {}", String.format("Not found dataSet: %s", dataSetId));
            be.setMessage(String.format("데이터셋(ID:%s)을 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dataSetId));
            throw be;
        } finally {
            dao.close();
        }
    }

    public Object getAddColumnData(int userId, String dataSetId, Map<String, Object> param) throws Exception {
        List<DataSetAddColumn> addColumnList = null;
        if (param.get("addColumn") != null) {
            addColumnList = new ArrayList<>();
            List<Map<String, Object>> addColumnData = (List<Map<String, Object>>)param.get("addColumn");
            for (Map<String, Object> data : addColumnData) {
                DataSetAddColumn addColumn = new DataSetAddColumn();
                addColumn.setName(data.get("name").toString());
                addColumn.setType(data.get("type").toString());
                addColumn.setOriginalColumn(data.get("originalColumn").toString());
                addColumn.setFormula(data.get("formula").toString());
                addColumnList.add(addColumn);
            }
        }
        Map<String, Object> result = getData(userId, dataSetId, true, addColumnList, (List<Map<String, Object>>)param.get("filterGroup"));
        return result.get("sample");
    }

    public void delDataSet(int userId, String dataSetId) throws Exception {
        DataSetDao dao = getDao(userId);
        try {
            DataSet dataSet = dao.getDataSet(dataSetId);
            if (dataSet != null) {
                dao.transactionStart();
                dao.delDataSet(dataSetId);
                dao.transactionEnd();
                dao.dropDataSetTable(dataSet.getDataTable());
            } else {
                BadException e = new BadException(String.format("데이터셋(ID:%s) 삭제에 실패 하였습니다.\n데이터셋을 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dataSetId));
                throw e;
            }
        } catch (BadException be) {
            log.warn("delDataSet error: {}", String.format("Not found dataSet: %s", dataSetId));
            throw be;
        } catch (Exception e) {
            log.error("delDataSet error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("데이터셋(ID:%s) 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dataSetId));
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void updateDataSet(int userId, String dataSetId, Map<String, Object> param) throws Exception {
        DataSetDao dao = getDao(userId);
        boolean createTable = false;
        boolean dropTableError = false;
        String newTableName = String.format("DATA_SET_TABLE_%s", System.currentTimeMillis());
        List<List<String>> changeInfo = (List<List<String>>)param.get("changeInfo");
        List<List<String>> originalInfo = (List<List<String>>)param.get("originalInfo");
        List<Map<String, Object>> addColumn = (List<Map<String, Object>>)param.get("addColumn");
        List<Map<String, Object>> filterGroup = (List<Map<String, Object>>)param.get("filterGroup");

        Map<String, Object> createInfo = getCreateInfo(changeInfo, originalInfo, addColumn);

        String createColumnStr = createInfo.get("createColumnStr").toString();
        String selectColumnQueryStr = createInfo.get("selectColumnQueryStr").toString();
        Map<String, String> columnNameMap = (Map<String, String>)createInfo.get("columnNameMap");
        try {
            DataSet dataSet = dao.getDataSet(dataSetId);
            if (dataSet == null) {
                BadException e = new BadException(String.format("데이터셋(ID:%s) 업데이트에 실패 하였습니다.\n데이터셋을 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dataSetId));
                throw e;
            }
            String srcTableName = dataSet.getDataTable();
            dataSet.setDataTable(newTableName);
            dataSet.setName(param.get("name").toString());
            dataSet.setUseColumns(param.get("useColumns").toString());
            dataSet.setChangeInfo(changeInfo);
            dao.createDataSetTable(newTableName, createColumnStr);
            createTable = true;
            dao.transactionStart();
            dao.delDataSetFilterGroup(dataSetId);
            dao.delDataSetAddColumn(dataSetId);
            if (filterGroup != null && filterGroup.size() > 0) {
                for (Map<String, Object> fg : filterGroup) {
                    int groupId = dao.insertDataSetFilterGroup(dataSet.getId(), fg.get("name").toString(), fg.get("enable").toString());
                    insertFilter(dao, (List<Map<String, Object>>)fg.get("filter"), groupId, columnNameMap);
                }
            }
            insertAddColumn(dao, addColumn, dataSet.getId(), columnNameMap);
            dao.updateDataSetTable(srcTableName, newTableName, selectColumnQueryStr);
            dao.updateDataSet(dataSet);
            if (param.get("groupId") != null) {
                dao.insertDataSetGroupMapping(Integer.parseInt(dataSetId), Integer.parseInt(String.valueOf(param.get("groupId"))));
            }
            dao.transactionEnd();
            dropTableError = true;
            dao.dropDataSetTable(srcTableName);
        } catch (BadException be) {
            log.warn("updateDataSet error: {}", String.format("Not found dataSet: %s", dataSetId));
            throw be;
        } catch (Exception e) {
            log.error("updateDataSet error: {}, dropTableError: {}", e.getMessage(), dropTableError);
            if (!dropTableError) {
                dao.rollBack();
                if (createTable) {
                    dao.dropDataSetTable(newTableName);
                }
            }
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("데이터셋(ID:%s) 업데이트에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dataSetId));
            throw ie;
        } finally {
            dao.close();
        }
    }

    public Object uploadFile(int userId, int groupId, MultipartFile[] file, String delimiter, Boolean hasHeader) throws Exception {
        String[] filenames = new String[file.length];
        InputStream[] streams = new InputStream[file.length];
        for (int i = 0; i < file.length; ++i) {
            filenames[i] = file[i].getOriginalFilename();
            streams[i] = file[i].getInputStream();
        }
        return uploadFile(userId, groupId, DataSourceManager.FILE, filenames, streams, delimiter, hasHeader, null, null, null);
    }

    private Object uploadFile(int userId, int groupId, int type, String[] filenames, InputStream[] streams, String delimiter, Boolean hasHeader, String icosStorage, String icosBucket, String icosObject) throws Exception {
        DataSetDao dao = getDao(userId);
        HashMap<String, Object> result = new HashMap<>();
        JSONObject srcObj = new JSONObject();
        User user = dao.getUser();
        String tableName = "DATA_SET_TABLE_" + System.currentTimeMillis();
        srcObj.put("type", type);
        srcObj.put("host", user.getDbUrl());
        srcObj.put("port", user.getDbPort());
        srcObj.put("dbName", user.getDbName());
        srcObj.put("id", user.getDbId());
        srcObj.put("pwd", user.getDbPwd());
        srcObj.put("ssl", false);
        srcObj.put("tableName", tableName);
        srcObj.put("icosStorage", icosStorage);
        srcObj.put("icosBucket", icosBucket);
        srcObj.put("icosObject", icosObject);
        DataSet dataSet = new DataSet(filenames[0], DataSourceManager.SOURCE_NAME[type], srcObj.toString(),
                tableName, "", new ArrayList<List<String>>(), new ArrayList<List<String>>(), true);
        try {
            dao.transactionStart();
            int dataSetId = dao.insertDataSet(dataSet);
            dao.insertDataSetGroupMapping(dataSetId, groupId);
            dao.transactionEnd();
            result.put("id", dataSetId);
            new CreateDataSetThread(userId, tableName, filenames, streams, delimiter, hasHeader, dataSetId).start();
        } finally {
            dao.close();
        }
        return result;
    }

    public Object getICOSList(String authorization) throws Exception {
        String api = String.format("%s/objectStorage?serviceName=%s", icosAPI, icosService);
        String method = "GET";
        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", authorization);
        return Util.sendHttp(api, method, header, null, false);
    }

    public Object getICOSBucketList(String authorization, String name) throws Exception {
        name = URLEncoder.encode(name, "UTF-8");
        String api = String.format("%s/bucket?objectStorageName=%s&serviceName=%s", icosAPI, name, icosService);
        String method = "GET";
        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", authorization);
        return Util.sendHttp(api, method, header, null, false);
    }

    public Object getICOSObjectList(String authorization, String storage, String bucket, String path) throws Exception {
        storage = URLEncoder.encode(storage, "UTF-8");
        path = URLEncoder.encode(path, "UTF-8");
        bucket = URLEncoder.encode(bucket, "UTF-8");
        String api = String.format("%s/object?objectStorageName=%s&bucketName=%s&directoryPath=%s&serviceName=%s", icosAPI, storage, bucket, path, icosService);
        String method = "GET";
        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", authorization);
        return Util.sendHttp(api, method, header, null, false);
    }

    public Object uploadICOSObject(int userId, int groupId, String authorization, String storage, String bucket, String[] paths, String folder, String delimiter, boolean hasHeader) throws Exception {
        if (folder != null) {
            JSONObject folderResponse = (JSONObject)getICOSObjectList(authorization, storage, bucket, folder);
            JSONArray dataArray = folderResponse.optJSONArray("data");
            ArrayList<String> pathList = new ArrayList<>();
            if (dataArray != null) {
                for (int i = 0; i < dataArray.length(); ++i) {
                    JSONObject data = dataArray.optJSONObject(i);
                    if (data != null) {
                        String objectName = data.optString("objectName");
                        if (objectName.lastIndexOf("/") != objectName.length() - 1) {
                            pathList.add(String.format("/%s", objectName));
                        }
                    }
                }
            }
            if (pathList.size() == 0) {
                throw new BadException(String.format("%s 파일을 찾을 수 없습니다.", folder));
            }
            paths = new String[pathList.size()];
            pathList.toArray(paths);
            log.info("uploadICOSObject paths: {}, pathList:{}", paths, pathList);
        }

        String[] filenames = new String[paths.length];
        InputStream[] streams = new InputStream[paths.length];
        storage = URLEncoder.encode(storage, "UTF-8");
        bucket = URLEncoder.encode(bucket, "UTF-8");
        String method = "GET";
        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", authorization);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < paths.length; ++i) {
            String path = paths[i];
            String objectName = URLEncoder.encode(path.substring(1), "UTF-8");
            sb.append(objectName).append(",");
            String api = String.format("%s/bucket/object/url?objectStorageName=%s&bucketName=%s&objectName=%s&serviceName=%s", icosAPI, storage, bucket, objectName, icosService);
            JSONObject result = (JSONObject)Util.sendHttp(api, method, header, null, false);
            if (result.getInt("statusCode") != 200) {
                return result;
            } else {
                String url = result.getJSONObject("data").getString("url");
                log.info("download url: " + result);
                Object obj = Util.sendHttp(url, method, null, null, true);
                if (obj instanceof InputStream) {
                    String[] tmp = path.split("/");
                    filenames[i] = tmp[tmp.length - 1];
                    streams[i] = (InputStream)obj;
                } else {
                    return obj;
                }
            }
        }
        ResponseBase res = new ResponseBase();
        return res.getRes(uploadFile(userId, groupId, DataSourceManager.ICOS, filenames, streams, delimiter, hasHeader, storage, bucket, sb.substring(0, sb.length() - 1)));
    }

    public void cancelDataSet(int userId, String dataSetId) throws Exception {
        DataSetDao dao = getDao(userId);
        try {
            DataSet dataSet = dao.getDataSet(dataSetId);
            if (dataSet != null) {
                if (dataSet.getStatus() == 1) {
                    dao.transactionStart();
                    dao.updateDataSet(dataSet.getId(), null, null, null, 2);
                    dao.transactionEnd();
                } else {
                    BadException e = new BadException(String.format("데이터셋(ID:%s) 취소에 실패 하였습니다.\n취소를 할 수 없는 상태 입니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dataSetId));
                    throw e;
                }
            } else {
                BadException e = new BadException(String.format("데이터셋(ID:%s) 취소에 실패 하였습니다.\n데이터셋을 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dataSetId));
                throw e;
            }
        } catch (BadException be) {
            log.warn("cancelDataSet error: {}", String.format("Not found dataSet: %s", dataSetId));
            throw be;
        } catch (Exception e) {
            log.error("cancelDataSet error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("데이터셋(ID:%s) 취소에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dataSetId));
            throw ie;
        } finally {
            dao.close();
        }
    }

    private void insertFilter(DataSetDao dao, List<Map<String, Object>> filter, int groupId, Map<String, String> columnNameMap) throws SQLException {
        if (filter != null) {
            for (Map<String, Object> info : filter) {
                Filter dataSetFilter = new Filter(groupId, columnNameMap.get(info.get("column").toString()),
                        info.get("condition").toString(), info.get("value1"), info.get("value2"), "true".equals(info.get("enable").toString()));
                dao.insertDataSetFilter(dataSetFilter);
            }
        }
    }

    private String getInsertStr(int srcType, String selectColumnQueryStr, String srcTableName, int index, int limit) {
        switch( srcType ) {
            case DataSourceManager.MYSQL:
            case DataSourceManager.MARIADB:
            case DataSourceManager.RDS:
                return String.format("SELECT %s FROM %s LIMIT %d,%d", selectColumnQueryStr, srcTableName, index, limit);
            case DataSourceManager.HIVE:
                StringBuffer query = new StringBuffer();
                query.append("SELECT %s, rowid_ FROM (").append("\n");
                query.append("    SELECT %s, ROW_NUMBER() OVER() as rowid_ FROM %s").append("\n");
                query.append(") t1").append("\n");
                query.append("WHERE rowid_ > %d and rowid_ <= %d");
                return String.format(query.toString(), selectColumnQueryStr, selectColumnQueryStr, srcTableName, index, index + limit);
            default:
                log.error("insertDataSetTable.getInsertStr, invalid srcType: {}", srcType);
                return null;
        }
    }

    private void insertAddColumn(DataSetDao dao, List<Map<String, Object>> addColumn, int dataSetId, Map<String, String> columnNameMap) throws SQLException {
        if (addColumn != null) {
            for (Map<String, Object> info : addColumn) {
                DataSetAddColumn dataSetAddColumn = new DataSetAddColumn(dataSetId, info.get("name").toString(),
                        columnNameMap.get(info.get("originalColumn").toString()), info.get("formula").toString(),
                        info.get("type").toString());
                dao.insertDataSetAddColumn(dataSetAddColumn);
            }
        }
    }

    public Map<String, Object> getCreateInfo(List<List<String>> changeInfo, List<List<String>> originalInfo, List<Map<String, Object>> addColumn) {
        StringBuffer columnBuffer = new StringBuffer();
        StringBuffer insertColumn = new StringBuffer();
        StringBuffer selectColumnQuery = new StringBuffer();
        StringBuffer selectColumn = new StringBuffer();

        HashMap<String, String> columnNameMap = new HashMap<>();
        for (int i=0; i<originalInfo.size(); ++i) {
            List<String> original = originalInfo.get(i);
            List<String> change = changeInfo.get(i);
            columnNameMap.put(original.get(0), change.get(0));
            columnBuffer.append(String.format("`%s` %s,", change.get(0), getDBType(change.get(1))));
            insertColumn.append(String.format("`%s`,", change.get(0)));
            selectColumn.append(String.format(":%s,", original.get(0)));
            selectColumnQuery.append(String.format("`%s`,", original.get(0)));
        }

        if (addColumn != null) {
            for (Map<String, Object> column : addColumn) {
                columnBuffer.append(String.format("`%s` %s,", column.get("name"), getDBType(column.get("type").toString())));
                insertColumn.append(String.format("`%s`,", column.get("name")));
                selectColumn.append(String.format(":%s,", column.get("name")));
                String formula = String.format(column.get("formula").toString(), column.get("originalColumn"));
                selectColumnQuery.append(String.format("%s AS `%s`,", getChangeType(formula, column.get("type"), false), column.get("name")));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("createColumnStr", columnBuffer.substring(0, columnBuffer.length() - 1));
        result.put("insertColumnStr", insertColumn.substring(0, insertColumn.length() - 1));
        result.put("selectColumnStr", selectColumn.substring(0, selectColumn.length() - 1));
        result.put("selectColumnQueryStr", selectColumnQuery.substring(0, selectColumnQuery.length() - 1));
        result.put("columnNameMap", columnNameMap);
        return result;
    }

    public  String getDBType(String type) {
        String result = "TEXT";
        if (type != null) {
            switch (type) {
                case "STRING":
                    result = "TEXT";
                    break;
                case "INTEGER":
                    result = "BIGINT";
                    break;
                case "FLOAT":
                    result = "DOUBLE";
                    break;
                case "DATE":
                    result = "DATETIME";
                    break;
            }
        }
        return result;
    }

    public String getType(Object input) {
        String result = "STRING";
        String inputStr = input.toString();
        if (inputStr.contains("int")) {
            result = "INTEGER";
        } else if (inputStr.contains("float") || inputStr.contains("double")) {
            result = "FLOAT";
        } else if (inputStr.contains("varchar") || inputStr.contains("char") || inputStr.contains("text")) {
            result = "STRING";
        } else if (inputStr.contains("date") || inputStr.contains("timestamp")) {
            result = "DATE";
        }
        return result;
    }

    private String getChangeType(Object column, Object convertType, boolean isColumn) {
        String result = null;
        String type = convertType.toString();
        switch(type) {
            case "INTEGER":
                if (isColumn) {
                    result = String.format("CONVERT(`%s`, DECIMAL)", column);
                } else {
                    result = String.format("CONVERT(%s, DECIMAL)", column);
                }
                break;
            case "FLOAT":
                if (isColumn) {
                    result = String.format("CONVERT(`%s`, DOUBLE)", column);
                } else {
                    result = String.format("CONVERT(%s, DOUBLE)", column);
                }
                break;
            case "STRING":
                if (isColumn) {
                    result = String.format("CONVERT(`%s`, CHAR)", column);
                } else {
                    result = String.format("CONVERT(%s, CHAR)", column);
                }
                break;
            case "DATE":
                if (isColumn) {
                    result = String.format("CONVERT(`%s`, DATETIME)", column);
                } else {
                    result = String.format("CONVERT(%s, DATETIME)", column);
                }
                break;
        }
        return result;
    }

    private String getFilterGroupWhere(List<Map<String, Object>> filterGroup) {
        String whereStr = null;
        if (filterGroup.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (Map<String, Object> fg : filterGroup) {
                if ("true".equals(fg.get("enable").toString())) {
                    String where = getWhereStr((List<Map<String, Object>>)fg.get("filter"));
                    if (where != null) {
                        sb.append(where);
                        sb.append(" AND ");
                    }
                }
            }
            if (sb.length() > 0) {
                whereStr = sb.substring(0, sb.length() - 5);
            }
        }
        return whereStr;
    }

    public String getWhereStrFromDB(List filterGroup) {
        String whereStr = null;
        StringBuffer stringBuffer = new StringBuffer();
        if (filterGroup != null && filterGroup.size() > 0) {
            for (Object obj : filterGroup) {
                FilterGroup fg = (FilterGroup)obj;
                if (fg.getEnable()) {
                    List<Map<String, Object>> params = new ArrayList<>();
                    for (Filter filter : fg.getFilter()) {
                        HashMap<String, Object> param = new HashMap<>();
                        param.put("enable", filter.getEnable());
                        param.put("condition", filter.getCondition());
                        param.put("value1", filter.getValue1());
                        param.put("value2", filter.getValue2());
                        param.put("column", filter.getColumn());
                        params.add(param);
                    }
                    if (params.size() > 0) {
                        String where = getWhereStr(params);
                        if (where != null) {
                            stringBuffer.append(getWhereStr(params));
                            stringBuffer.append(" AND ");
                        }
                    }
                }
            }
            if (stringBuffer.length() > 0) {
                whereStr = stringBuffer.substring(0, stringBuffer.length() - 5);
            }
        }
        return whereStr;
    }

    private String getWhereStr(List<Map<String, Object>> filter) {
        String whereStr = null;
        if (filter != null) {
            StringBuffer where = new StringBuffer();
            for (Map<String, Object> filterInfo : filter) {
                if ((boolean)filterInfo.get("enable")) {
                    if (where.length() > 0) {
                        where.append(String.format(" OR %s", getWhere(filterInfo)));
                    } else {
                        where.append(String.format("%s", getWhere(filterInfo)));
                    }
                }
            }
            if (where.length() > 0) {
                whereStr = String.format("(%s)", where.toString());
            }
        }
        return whereStr;
    }

    private String getWhere(Map<String, Object> info) {
        String result = null;
        String condition = info.get("condition").toString();
        boolean numberType = false;
        try {
            String value = info.get("value1").toString().toLowerCase().replaceAll("f", "-").replaceAll("l", "-");
            Double.parseDouble(value);
            numberType = true;
        } catch (Exception e) {}
        switch(condition) {
            case "eq":
                if (numberType) {
                    result = String.format("`%s` = %s", info.get("column"), info.get("value1"));
                } else {
                    result = String.format("`%s` = '%s'", info.get("column"), info.get("value1"));
                }
                break;
            case "ne":
                if (numberType) {
                    result = String.format("`%s` <> %s", info.get("column"), info.get("value1"));
                } else {
                    result = String.format("`%s` <> '%s'", info.get("column"), info.get("value1"));
                }
                break;
            case "gt":
                if (numberType) {
                    result = String.format("`%s` > %s", info.get("column"), info.get("value1"));
                } else {
                    result = String.format("`%s` > '%s'", info.get("column"), info.get("value1"));
                }
                break;
            case "ge":
                if (numberType) {
                    result = String.format("`%s` >= %s", info.get("column"), info.get("value1"));
                } else {
                    result = String.format("`%s` >= '%s'", info.get("column"), info.get("value1"));
                }
                break;
            case "lt":
                if (numberType) {
                    result = String.format("`%s` < %s", info.get("column"), info.get("value1"));
                } else {
                    result = String.format("`%s` < '%s'", info.get("column"), info.get("value1"));
                }
                break;
            case "le":
                if (numberType) {
                    result = String.format("`%s` <= %s", info.get("column"), info.get("value1"));
                } else {
                    result = String.format("`%s` <= '%s'", info.get("column"), info.get("value1"));
                }
                break;
            case "between":
                if (numberType) {
                    result = String.format("`%s` BETWEEN %s AND %s", info.get("column"), info.get("value1"), info.get("value2"));
                } else {
                    result = String.format("`%s` BETWEEN '%s' AND '%s'", info.get("column"), info.get("value1"), info.get("value2"));
                }
                break;
            case "like":
                result = String.format("`%s` LIKE '%s'", info.get("column"), info.get("value1"));
                break;
            case "null":
                result = String.format("`%s` IS NULL", info.get("column"));
                break;
            case "notNull":
                result = String.format("`%s` IS NOT NULL", info.get("column"));
                break;
        }
        return result;
    }

    public void createDataSetSchedule(Map<String, Object> param) throws Exception {
        try {
            settingDao.insertDataSetSchedule(param);
            String url = String.format("%s/add/%d", scheduleUrl, param.get("id"));
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization", (String)param.get("Authorization"));
            JSONObject response = (JSONObject)Util.sendHttp(url, "POST", header, null, false);
            if (!"OK".equals(response.optString("status"))) {
                throw new InternalException(response.optString("message"));
            }
        } catch (Exception e) {
            log.error("createDataSetSchedule", e);
            settingDao.deleteDataSetSchedule(String.valueOf(param.get("id")));
            InternalException ie = new InternalException(e);
            ie.setMessage("스케줄 생성에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        }
    }

    public void dataSetScheduleOnetime(Map<String, Object> param) throws Exception {
        try {
            if (settingDao.selectDataSetScheduleOnetime(param) > 0) {
                throw new BadException("스케줄이 이미 동작 하고 있습니다.");
            }
            settingDao.insertDataSetScheduleOnetime(param);
            String url = String.format("%s/onetime/%d", scheduleUrl, param.get("id"));
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization", (String)param.get("Authorization"));
            JSONObject response = (JSONObject)Util.sendHttp(url, "POST", header, null, false);
            if (!"OK".equals(response.optString("status"))) {
                throw new InternalException(response.optString("message"));
            }
        } catch (Exception e) {
            log.error("dataSetScheduleOnetime", e);
            settingDao.deleteDataSetScheduleOnetime(String.valueOf(param.get("id")));
            InternalException ie = new InternalException(e);
            ie.setMessage("스케줄 실행에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        }
    }

    public Object getDataSetScheduleLIst(int userId, int dataSetId) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("dataSetId", dataSetId);
        return settingDao.getDataSetScheduleList(param);
    }

    public Object getDataSetScheduleDetail(int scheduleId, String type) throws Exception {
        List<ScheduleDetail> list = null;
        if ("onetime".equals(type)) {
            list = settingDao.getDataSetOnetimeScheduleDetail(scheduleId);
        } else {
            list = settingDao.getDataSetScheduleDetail(scheduleId);
        }
        Map<String, Object> result = new HashMap<>();
        if (list.size() > 0) {
            ScheduleDetail detail = list.get(0);
            result.put("startTime", detail.getStartTime());
            result.put("endTime", detail.getEndTime());
            result.put("list", list);
        }
        return result;
    }

    public void exportData(int userId, String dataSetId, String userAgent, HttpServletResponse response) throws Exception {
        DataSetDao dao = getDao(userId);
        try {
            DataSet dataSet = dao.getDataSet(dataSetId);
            if (dataSet == null) {
                BadException e = new BadException(String.format("Not found DataSet: %s", dataSetId));
                throw e;
            }
            String name = dataSet.getName();
            if (name == null || "".equals(name.trim())) {
                name = "dataSet";
            }
            name = Util.getEncodedFilename(name, Util.getBrowser(userAgent));

            List<Map<String, Object>> schemaData = dao.getColumnInfo(dataSet.getDataTable());
            String[] headers = new String[schemaData.size()];
            for (int i = 0; i < schemaData.size(); ++i) {
                headers[i] = schemaData.get(i).get("Field").toString();
            }
            String query = String.format("SELECT * FROM %s", dataSet.getDataTable());
            log.info("query: {}", query);
            List<Map<String, Object>> dataList = dao.getDataSetTable(query);
            String encoding = Util.getEncoding(dataList);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.csv\"", name));
            OutputStream os = response.getOutputStream();
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(os, encoding));
            csvWriter.writeNext(headers);
            for (Map<String, Object> data : dataList) {
                String[] dataArray = new String[headers.length];
                for (int i = 0; i < headers.length; ++i) {
                    dataArray[i] = String.valueOf(data.get(headers[i]));
                }
                csvWriter.writeNext(dataArray);
            }
            csvWriter.close();
        } finally {
            dao.close();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void deleteDataSetSchedule(String id, String auth) throws Exception {
        try {
            String url = String.format("%s/del/%s", scheduleUrl, id);
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization", auth);
            Util.sendHttp(url, "DELETE", header, null, false);
            settingDao.deleteDataSetSchedule(id);
        } catch (Exception e) {
            log.error("deleteDataSetSchedule", e);
            InternalException ie = new InternalException(e);
            ie.setMessage("스케줄 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        }
    }

    public Object getDatabases(int type, Map<String, Object> param) throws Exception {
        List<String> databaseList = null;
        SingleConnectionDataSource ds = null;
        try {
            if (type == DataSourceManager.FILE || type == DataSourceManager.ICOS) {
                throw new BadException("지원하지 않는 타입의 요청입니다.");
            }
            String sql = "SHOW DATABASES";
            ds = DataSourceManager.getDataSource(type, param.get("host").toString(), param.get("port").toString(),
                    param.get("id").toString(), param.get("pwd").toString(), param.get("ssl").toString());
            JdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(ds).getJdbcTemplate();
            databaseList = jdbcTemplate.queryForList(sql, String.class);
        } catch (BadException e) {
            throw e;
        } catch (Exception e) {
            log.error("getHiveDatabases error", e);
            InternalException ie = new InternalException(e);
            ie.setMessage("데이터베이스 조회에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            if (ds != null) {
                ds.getConnection().close();
            }
        }
        return databaseList;
    }

    private DataSetDao getDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new DataSetDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()), user);
    }

    private class CreateDataSetThread extends Thread {
        String tableName;
        String[] filenames;
        InputStream[] streams;
        String delimiter;
        Boolean hasHeader;
        int dataSetId;
        int userId;
        Map<String, Object> param;
        String auth;
        boolean isFile;
        CancelCheckThread thread;
        boolean createTable = false;
        boolean isCancel = false;

        CreateDataSetThread(int userId, String tableName, String[] filenames, InputStream[] streams, String delimiter, Boolean hasHeader, int dataSetId) {
            this.userId = userId;
            this.tableName = tableName;
            this.filenames = filenames;
            this.streams = streams;
            this.delimiter = delimiter;
            this.hasHeader = hasHeader;
            this.dataSetId = dataSetId;
            isFile = true;
        }

        CreateDataSetThread(int userId, int dataSetId, String tableName, Map<String, Object> param, String auth) {
            this.userId = userId;
            this.dataSetId = dataSetId;
            this.tableName = tableName;
            this.param = param;
            this.auth = auth;
        }

        public void run() {
            DataSetDao dao = null;
            try {
                dao = getDao(userId);
                thread = new CancelCheckThread(this, dao, dataSetId);
                if (isFile) {
                    uploadFile();
                } else {
                    createDataSet();
                }
                if (isCancel) {
                    log.info("CreateDataSetThread Cancel {}", dataSetId);
                    dao.transactionStart();
                    dao.updateDataSet(dataSetId, null, null, null, 3);
                    dao.transactionEnd();
                    if (createTable) {
                        try {
                            dao.dropDataSetTable(tableName);
                        } catch (Exception e) {
                            log.error("CreateDataSetThread dropDataSetTable error", e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("CreateDataSetThread error", e);
                try {
                    dao.transactionStart();
                    dao.updateDataSet(dataSetId, null, null, null, 4);
                    dao.transactionEnd();
                } catch (Exception ex) {
                    log.error("CreateDataSetThread updateDataSet error", e);
                }
                if (createTable) {
                    try {
                        dao.dropDataSetTable(tableName);
                    } catch (Exception ex) {
                        log.error("CreateDataSetThread dropDataSetTable error", e);
                    }
                }
            } finally {
                thread.isLive = false;
                thread.interrupt();
                if (dao != null) {
                    dao.close();
                }
            }
        }

        private void createDataSet() throws Exception {
            DataSetDao dao = getDao(userId);
            JdbcTemplate jdbcTemplate = null;
            try {
                List<List<String>> changeInfo = (List<List<String>>)param.get("changeInfo");
                List<List<String>> originalInfo = (List<List<String>>)param.get("originalInfo");
                List<Map<String, Object>> addColumn = (List<Map<String, Object>>)param.get("addColumn");

                Map<String, Object> createInfo = getCreateInfo(changeInfo, originalInfo, addColumn);

                String createColumnStr = createInfo.get("createColumnStr").toString();
                String insertColumnStr = createInfo.get("insertColumnStr").toString();
                String selectColumnStr = createInfo.get("selectColumnStr").toString();
                String selectColumnQueryStr = createInfo.get("selectColumnQueryStr").toString();
                dao.createDataSetTable(tableName, createColumnStr);
                createTable = true;
                dao.transactionStart();
                Map<String, Object> srcCon = (Map<String, Object>)param.get("srcConnection");
                int srcType = Integer.parseInt(srcCon.get("type").toString());
                switch(srcType) {
                    case DataSourceManager.MYSQL:
                    case DataSourceManager.MARIADB:
                    case DataSourceManager.HIVE:
                    case DataSourceManager.RDS:
                        jdbcTemplate = new JdbcTemplate(DataSourceManager.getDataSource(srcType, srcCon));
                        if (srcType != DataSourceManager.HIVE) {
                            jdbcTemplate.setQueryTimeout(0);
                        }
                        insertDataSetTable(jdbcTemplate, srcType, dao, selectColumnQueryStr, srcCon.get("tableName").toString(),
                                tableName, insertColumnStr, selectColumnStr);
                        break;
                    case DataSourceManager.FILE:
                    case DataSourceManager.ICOS:
                        dao.updateDataSetTable(srcCon.get("tableName").toString(), tableName, selectColumnQueryStr);
                        break;
                }
                dao.transactionEnd();
                List<Map<String, Object>> scheduleList = (List<Map<String, Object>>)param.get("schedule");
                if (scheduleList != null) {
                    for (Map<String, Object> schedule : scheduleList) {
                        schedule.put("userId", userId);
                        schedule.put("dataSetId", dataSetId);
                        schedule.put("Authorization", auth);
                        createDataSetSchedule(schedule);
                    }
                }
                DataSet dataSet = dao.getDataSet(String.valueOf(dataSetId));
                if (dataSet != null && dataSet.getStatus() != 2) {
                    dao.transactionStart();
                    dao.updateDataSet(dataSetId, null, null, null, 0);
                    dao.transactionEnd();
                } else {
                    isCancel = true;
                }
            } finally {
                dao.close();
                if (jdbcTemplate != null) {
                    try {
                        jdbcTemplate.getDataSource().getConnection().close();
                    } catch (Exception e) {}
                }
            }
        }

        private void insertDataSetTable(JdbcTemplate jdbcTemplate, int srcType, DataSetDao dao, String selectColumnQueryStr, String srcTableName,
                                        String desTableName, String insertColumnStr, String selectColumnStr) throws SQLException {
            int index = 0;
            int limit = 1000;
            long total = 0;
            boolean hasNext = true;
            do {
                String query = getInsertStr(srcType, selectColumnQueryStr, srcTableName, index, limit);
                List<Map<String, Object>> list = jdbcTemplate.queryForList(query);
                index += limit;
                if (list.size() > 0) {
                    dao.insertDataSetTable(desTableName, insertColumnStr, selectColumnStr, list);
                    if (list.size() < limit) {
                        hasNext = false;
                    }
                    total += list.size();
                    log.info("insertDataSetTable, size: {}, hasNext: {}, limit: {}, total: {}", list.size(), hasNext, limit, total);
                } else {
                    hasNext = false;
                }
            } while(hasNext && !isCancel);
        }

        private void uploadFile() throws Exception {
            HashMap<String, String> oldHeader = null;
            HashMap<Integer, String> oldIndex = null;
            DataSetDao dao = getDao(userId);
            try {
                JSONArray useColumns = new JSONArray();
                JSONArray columnInfo = new JSONArray();
                for (int i = 0; i < filenames.length; ++i) {
                    String filename = filenames[i];
                    InputStream stream = streams[i];
                    DataConverter dc = new DataConverter(filename, stream, delimiter, hasHeader);
                    HashMap<String, String> header = dc.getHeader();
                    HashMap<Integer, String> index = dc.getIndex();
                    if (oldHeader == null) {
                        oldHeader = new HashMap<>();
                        oldHeader.putAll(header);
                        oldIndex = new HashMap<>();
                        oldIndex.putAll(index);
                    } else {
                        for (String key : oldHeader.keySet()) {
                            if (!oldHeader.get(key).equals(header.get(key))) {
                                throw new BadException(String.format("%s 해더 타입이 일치하지 않습니다., [column:%s, old:%s, new:%s]", filename, key, oldHeader.get(key), header.get(key)));
                            }
                        }
                        for (Integer key : oldIndex.keySet()) {
                            if (!oldIndex.get(key).equals(index.get(key))) {
                                throw new BadException(String.format("%s 해더 인덱스가 일치하지 않습니다., [index:%d, old:%s, new:%s]", filename, key, oldIndex.get(key), index.get(key)));
                            }
                        }
                    }
                    StringBuffer columns = new StringBuffer();
                    StringBuffer insertColumn = new StringBuffer();
                    StringBuffer selectColumn = new StringBuffer();
                    if (index.size() > MARIADB_MAX_COLUMN_COUNT) {
                        log.warn("uploadFile error: {}", String.format("file's column count exceeds dbms limit count: %d > %d", index.size(), MARIADB_MAX_COLUMN_COUNT));
                        throw new BadException( String.format("파일이 포함할 수 있는 컬럼 수는 최대 %d개 입니다.<br/>현재 파일 내 포함된 컬럼 수: %d", MARIADB_MAX_COLUMN_COUNT, index.size()));
                    }
                    for (String key : index.values()) {
                        String type = getDBType(header.get(key));
                        columns.append(String.format("`%s` %s NULL,", key, type));
                        insertColumn.append(String.format("`%s`,", key));
                        selectColumn.append(String.format(":%s,", key.replaceAll("-", "dash")));
                        JSONArray info = new JSONArray();
                        info.put(key);
                        info.put(type);
                        columnInfo.put(info);
                        useColumns.put(key);
                    }
                    if (!createTable) {
                        dao.createDataSetTable(tableName, columns.substring(0, columns.length() - 1));
                        createTable = true;
                    }
                    List<Map<String, Object>> list = dc.getData();
                    List<Map<String, Object>> insertList = new ArrayList<>();
                    int max = list.size();
                    int cnt = 0;
                    for (i = 0; i < max; ++i) {
                        insertList.add(list.get(i));
                        cnt++;
                        if (cnt % 1000 == 0 || cnt == max) {
                            dao.transactionStart();
                            dao.insertDataSetTable(tableName, insertColumn.substring(0, insertColumn.length() - 1), selectColumn.substring(0, selectColumn.length() - 1), insertList);
                            dao.transactionEnd();
                            insertList.clear();
                        }
                        if (isCancel) {
                            break;
                        }
                    }
                    list.clear();
                }
                if (!isCancel) {
                    String columnInfoStr = columnInfo.toString().replaceAll("\"", "");
                    dao.transactionStart();
                    dao.updateDataSet(dataSetId, useColumns.toString().replaceAll("\"", ""), columnInfoStr, columnInfoStr, 0);
                    dao.transactionEnd();
                }
            } catch (BadException e) {
                throw e;
            } catch (BadSqlGrammarException e) {
                InternalException ie = new InternalException(e);
                ie.setMessage("파일 업로드에 실패하였습니다.<br />구분자(delimiter)를 확인해 주세요.");
                throw ie;
            } finally {
                dao.close();
            }
        }
    }

    private class CancelCheckThread extends Thread {
        CreateDataSetThread thread;
        DataSetDao dao;
        int dataSetId;
        boolean isLive;

        CancelCheckThread(CreateDataSetThread thread, DataSetDao dao, int dataSetId) {
            this.thread = thread;
            this.dao = dao;
            this.dataSetId = dataSetId;
            this.isLive = true;
            start();
        }

        public void run() {
            try {
                while (isLive) {
                    sleep(1000 * 10);
                    DataSet dataSet = dao.getDataSet(String.valueOf(dataSetId));
                    if (dataSet != null) {
                        if (dataSet.getStatus() == 2) {
                            log.info("CancelCheckThread interrupt, {}", dataSetId);
                            thread.isCancel = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {}
        }
    }
}
