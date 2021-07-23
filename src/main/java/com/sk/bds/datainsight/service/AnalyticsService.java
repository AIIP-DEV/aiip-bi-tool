package com.sk.bds.datainsight.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.sk.bds.datainsight.database.dao.AnalysisDao;
import com.sk.bds.datainsight.database.dao.SettingDao;
import com.sk.bds.datainsight.database.dao.UserDao;
import com.sk.bds.datainsight.database.model.*;
import com.sk.bds.datainsight.echart.OptionHelper;
import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.exception.InternalException;
import com.sk.bds.datainsight.util.Constants;
import com.sk.bds.datainsight.util.DataConverter;
import com.sk.bds.datainsight.util.DataSourceManager;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {
    private static final Logger log = LoggerFactory.getLogger(DataService.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    public final static int MARIADB_MAX_COLUMN_COUNT = 1000;

    @Autowired
    UserDao userDao;

    @Autowired
    SettingDao settingDao;

    @Value("${schedule.url}")
    String scheduleUrl;

    @Autowired
    ChartService chartService;

    public AnalyticsService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Map<String, Object> getAnalyticsDetail(int userId, String uuId, boolean isData) throws Exception {
        AnalysisDao dao = getDao(userId);
        Map<String, Object> result = new HashMap<>();
        String analysisId = Integer.toString(dao.getAnalysisIdByUuId(uuId));
        try {
            Analysis analysis = dao.getAnalysisId(analysisId);
            checkAnalysis(analysis, analysisId);
            DataService ds = new DataService(userDao);
            result.put("id", analysis.getId());
            result.put("groupId", analysis.getGroupId());
            result.put("name", analysis.getName());
            result.put("subName", analysis.getSubName());
            result.put("dataSetId", analysis.getDataSetId());
            result.put("createUser", analysis.getCreateUser());
            result.put("updateDate", analysis.getUpdateDate());
            result.put("createDate", analysis.getCreateDate());
            result.put("queryId", analysis.getQueryId());
            result.put("variableInfo", analysis.getVariableInfo());
            List<Filter> analysisFilterList = dao.getAnalysisFilter(analysisId);
            List<AnalysisFilterGroup> filterGroup = dao.getAnalysisFilterGroup(analysisId);
            for (AnalysisFilterGroup fg : filterGroup) {
                fg.addFilter(analysisFilterList);
            }
            result.put("filterGroup", filterGroup);
            ArrayList<String[]> column = new ArrayList<>();
            if (analysis.getDataTable() != null) {
                List<Map<String, Object>> schemaData = dao.getColumnInfo(analysis.getDataTable());
                for (Map<String, Object> data : schemaData) {
                    column.add(new String[]{data.get("Field").toString(), ds.getType(data.get("Type"))});
                }
            } else {
                String queryText = dao.getQueryTextFromAnalysisId(analysisId);
                List<Map<String, Object>> tableList = dao.selectDataSetTables(Util.getDataSetList(queryText));
                List<Map<String, Object>> variableList = null;
                List<Object> variableInfo = analysis.getVariableInfo();
                if (variableInfo != null) {
                    variableList = new ArrayList<>();
                    for (Object obj : variableInfo) {
                        variableList.add((Map<String, Object>) obj);
                    }
                }
                queryText = Util.getQueryText(queryText, tableList, variableList, null);
                List<Map<String, Object>> list = dao.getDataSetTable(queryText);
                column.addAll(Util.getColumnInfo(list));
            }
            result.put("column", column);
            result.put("data", new ArrayList());
            result.put("charts", getAnalyticsChartDataList(userId, uuId));
        } finally {
            dao.close();
        }
        return result;
    }

    public Object getAnalyticsChartDataList(int userId, String uuId) throws Exception {
        AnalysisDao dao = getDao(userId);
        DataService ds = new DataService(userDao);
        String analysisId = Integer.toString(dao.getAnalysisIdByUuId(uuId));
        User user = userDao.getUserById(userId);
        try {
            return chartService.getAnalysisChartDataMap(ds, dao, analysisId, null, user.getSsoId());
        } finally {
            dao.close();
        }
    }

    public boolean countUuId(int userId, String uuId) throws Exception {
        AnalysisDao dao = getDao(userId);
        Integer count = dao.countAnalysisIdByUuId(uuId);
        if (count != 0) {
            return true;
        }
        return false;
    }

    public Object getAnalyticsChartDataListByAnalysisId(int userId, String analysisId) throws Exception {
        AnalysisDao dao = getDao(userId);
        DataService ds = new DataService(userDao);
        User user = userDao.getUserById(userId);
        try {
            return chartService.getAnalysisChartDataMap(ds, dao, analysisId, null, user.getSsoId());
        } finally {
            dao.close();
        }
    }

    public Object getAnalyticsList(int userId, JSONObject param) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            List<AnalysisWithChart> list = dao.getAnalysisList(param);
            int count = dao.getAnalysisListCount(param);
            Map<String, Object> result = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                List<AnalysisChart> chartList = (List<AnalysisChart>) getAnalyticsChartDataListByAnalysisId(userId, Integer.toString(list.get(i).getId()));
                if (chartList != null && chartList.size() > 0 && list.get(i).getId() == chartList.get(0).getAnalysisId()) {
                    list.get(i).setCharts(chartList);
                }
            }
            result.put("list", list);
            result.put("totalAnalysis", count);
            return result;
        } finally {
            dao.close();
        }
    }

    public void delAnalytics(int userId, String uuId) throws Exception {
        AnalysisDao dao = getDao(userId);
        String analysisId = Integer.toString(dao.getAnalysisIdByUuId(uuId));
        try {
            Analysis analysis = dao.getAnalysisId(analysisId);
            checkAnalysis(analysis, analysisId);
            dao.transactionStart();
            dao.delAnalysis(analysisId);
            dao.transactionEnd();
            dao.dropDataSetTable(analysis.getDataTable());
        } catch (BadException be) {
            throw be;
        } catch (Exception e) {
            dao.rollBack();
            log.error("delAnalysis error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("분석작업(ID:%s) 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", analysisId));
            throw ie;
        } finally {
            dao.close();
        }
    }

    private class CreateDataSetThread extends Thread {
        String tableName;
        String filename;
        FileInputStream stream;
        Boolean hasHeader;
        int analysisId;
        int analyticsId;
        int userId;
        String delimiter;
        Map<String, Object> param;
        String auth;
        boolean isFile;
        CancelCheckThread thread;
        boolean createTable = false;
        boolean isCancel = false;

        CreateDataSetThread(int userId, String tableName, String filename, FileInputStream stream, Boolean hasHeader, String delimiter, int analyticsId) {
            this.userId = userId;
            this.tableName = tableName;
            this.filename = filename;
            this.stream = stream;
            this.hasHeader = hasHeader;
            this.delimiter = delimiter;
            this.analyticsId = analyticsId;
            isFile = true;
        }

        public void run() {
            AnalysisDao dao = null;
            try {
                dao = getDao(userId);
                thread = new CancelCheckThread(this, dao, analysisId);
                uploadFile();
                if (isCancel) {
                    log.info("CreateDataSetThread Cancel {}", analysisId);
                    dao.transactionStart();
                    Analysis analysis = Analysis.builder().id(analysisId).status(3).build();
                    dao.updateAnalysis(analysis);
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
                    Analysis analysis = Analysis.builder().id(analysisId).status(4).build();
                    dao.updateAnalysis(analysis);
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


        private class CancelCheckThread extends Thread {
            CreateDataSetThread thread;
            AnalysisDao dao;
            int dataSetId;
            boolean isLive;

            CancelCheckThread(CreateDataSetThread thread, AnalysisDao dao, int dataSetId) {
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
                } catch (Exception e) {
                }
            }
        }

        private void uploadFile() throws Exception {
            AnalysisDao dao = getDao(userId);
            try {
                JSONArray useColumns = new JSONArray();
                JSONArray columnInfo = new JSONArray();
                String filename = this.filename;
                FileInputStream stream = this.stream;
                DataConverter dc = new DataConverter(filename, stream, delimiter, hasHeader);
                HashMap<String, String> header = dc.getHeader();
                HashMap<Integer, String> index = dc.getIndex();
                StringBuffer columns = new StringBuffer();
                StringBuffer insertColumn = new StringBuffer();
                StringBuffer selectColumn = new StringBuffer();
                if (index.size() > MARIADB_MAX_COLUMN_COUNT) {
                    log.warn("uploadFile error: {}", String.format("file's column count exceeds dbms limit count: %d > %d", index.size(), MARIADB_MAX_COLUMN_COUNT));
                    throw new BadException(String.format("파일이 포함할 수 있는 컬럼 수는 최대 %d개 입니다.<br/>현재 파일 내 포함된 컬럼 수: %d", MARIADB_MAX_COLUMN_COUNT, index.size()));
                }
                for (String key : index.values()) {
                    String type = getDBType(header.get(key));
                    columns.append(String.format("`%s` %s NULL,", key, type));
                    insertColumn.append(String.format("`%s`,", key));
                    selectColumn.append(String.format(":%s,", key.replaceAll("-", "dash")));
                    JSONArray info = new JSONArray();
                    columnInfo.put(info);
                    useColumns.put(key);
                    info.put(key);
                    info.put(type);
                }
                if (!createTable) {
                    dao.createAnalysisTable(tableName, columns.substring(0, columns.length() - 1));
                    createTable = true;
                }

                List<Map<String, Object>> list = dc.getData();
                List<Map<String, Object>> insertList = new ArrayList<>();
                int max = list.size();
                int cnt = 0;
                for (int i = 0; i < max; ++i) {
                    insertList.add(list.get(i));
                    cnt++;
                    if (cnt % 1000 == 0 || cnt == max) {
                        dao.transactionStart();
                        dao.insertAnalysisTable(tableName, insertColumn.substring(0, insertColumn.length() - 1),
                                selectColumn.substring(0, selectColumn.length() - 1), insertList);
                        dao.transactionEnd();
                        insertList.clear();
                    }
                    if (isCancel) {
                        break;
                    }
                }
                list.clear();
                if (!isCancel) {
                    String columnInfoStr = columnInfo.toString().replaceAll("\"", "");
                    dao.transactionStart();
                    Analysis analysis = Analysis.builder().id(analysisId).status(0).build();
                    dao.updateAnalytics(analyticsId, useColumns.toString().replaceAll("\"", ""), columnInfoStr, columnInfoStr);
                    dao.updateAnalysis(analysis);
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

    public Object uploadFile(int userId, int groupId, String path, Boolean hasHeader, String uuId, String delimiter) throws Exception {
        File file = new File(path);
        String filename = file.getName();
        FileInputStream stream = new FileInputStream(file);

        return uploadFile(userId, groupId, DataSourceManager.FILE, filename, stream, hasHeader, uuId, delimiter);
    }

    private Object uploadFile(int userId, int groupId, int type, String filename, FileInputStream stream, Boolean hasHeader, String uuId, String delimiter) throws Exception {
        AnalysisDao analysisDao = getDao(userId);

        HashMap<String, Object> result = new HashMap<>();
        JSONObject srcObj = new JSONObject();
        User user = analysisDao.getUser();
        String tableName = "ANALYSIS_TABLE_" + System.currentTimeMillis();
        srcObj.put("type", type);
        srcObj.put("host", user.getDbUrl());
        srcObj.put("port", user.getDbPort());
        srcObj.put("dbName", user.getDbName());
        srcObj.put("id", user.getDbId());
        srcObj.put("ssl", false);
        srcObj.put("tableName", tableName);
        DataSet dataSet = new DataSet(filename, DataSourceManager.SOURCE_NAME[type], srcObj.toString(),
                tableName, "", new ArrayList<List<String>>(), new ArrayList<List<String>>(), true);
        try {
            int id = 0;
            analysisDao.transactionStart();
            uuId = Integer.toString(groupId) + "_" + uuId + "_" + filename;
            Integer analysisId = analysisDao.countAnalysisIdByUuId(uuId);
            if (analysisId != 1) {
                id = analysisDao.insertAnalytics(filename, uuId, null, tableName);
            }
            Integer count = analysisDao.getGroupCount(Integer.toString(groupId));
            if (count == 0) {
                analysisDao.createGroup(Integer.toString(groupId));
            }
            Integer getGroupId = analysisDao.getGroupId(Integer.toString(groupId));
            analysisDao.insertAnalysisGroupMapping(id, getGroupId);
            result.put("id", id);
            if (dataSet != null) {
                insertAnalysisAddColumn(analysisDao, id, dataSet);
            }
            analysisDao.transactionEnd();
            new CreateDataSetThread(userId, tableName, filename, stream, hasHeader, delimiter, id).start();
        } finally {
            analysisDao.close();
        }
        return result;
    }

    public Map<String, Object> getCreateInfo(List<List<String>> changeInfo, List<List<String>> originalInfo, List<Map<String, Object>> addColumn) {
        StringBuffer columnBuffer = new StringBuffer();
        StringBuffer insertColumn = new StringBuffer();
        StringBuffer selectColumnQuery = new StringBuffer();
        StringBuffer selectColumn = new StringBuffer();

        HashMap<String, String> columnNameMap = new HashMap<>();
        for (int i = 0; i < originalInfo.size(); ++i) {
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

    public String getDBType(String type) {
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

    private String getChangeType(Object column, Object convertType, boolean isColumn) {
        String result = null;
        String type = convertType.toString();
        switch (type) {
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

    public Map<String, Object> createAnalysis(int userId, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        Map<String, Object> result = new HashMap<>();
        boolean createTable = false;
        String tableName = String.format("ANALYSIS_TABLE_%d", System.currentTimeMillis());
        Integer dataSetId = (Integer) param.get("dataSetId");
        Integer analysisId = (Integer) param.get("analysisId");
        Integer queryId = (Integer) param.get("queryId");
        String variableInfo = null;
        try {
            DataService ds = new DataService(userDao);
            DataSet dataSet = null;
            if (dataSetId != null) {
                dataSet = dao.getDataSet(dataSetId.toString());
                if (dataSet == null) {
                    String message = String.format("DataSet not found, %s", dataSetId);
                    log.error(message);
                    BadException badException = new BadException(
                            String.format("분석작업 생성에 실패 하였습니다.\n데이터셋(ID:%s)을 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dataSetId));
                    throw badException;
                }
                JSONArray array = new JSONArray(dataSet.getUseColumns());
                List<String> useColumns = new ArrayList<>();
                for (int i = 0; i < array.length(); ++i) {
                    useColumns.add(array.getString(i));
                }

                List<Map<String, Object>> schemaData = dao.getColumnInfo(dataSet.getDataTable());
                StringBuffer insertColumn = new StringBuffer();
                for (Map<String, Object> data : schemaData) {
                    String field = data.get("Field").toString();
                    if (useColumns.contains(field)) {
                        List<String> info = new ArrayList<>();
                        info.add(field);
                        info.add(ds.getType(data.get("Type")));
                        insertColumn.append(String.format("`%s`,", field));
                    }
                }
                String insertColumnStr = insertColumn.substring(0, insertColumn.length() - 1);
                List<Filter> filter = dao.getDataSetFilter(dataSetId.toString());
                List<DataSetFilterGroup> filterGroup = dao.getDataSetFilterGroup(dataSetId.toString());
                for (DataSetFilterGroup fg : filterGroup) {
                    fg.addFilter(filter);
                }
                String where = ds.getWhereStrFromDB(filterGroup);
                dao.createAnalysisDataSetTable(dataSet.getDataTable(), tableName, insertColumnStr, where);
                createTable = true;
            } else if (analysisId != null) {
                Analysis analysis = dao.getAnalysisId(analysisId.toString());
                checkAnalysis(analysis, analysisId.toString());
                dataSetId = analysis.getDataSetId();
                List<Filter> filter = dao.getAnalysisFilter(analysisId.toString());
                List<AnalysisFilterGroup> filterGroup = dao.getAnalysisFilterGroup(analysisId.toString());
                for (AnalysisFilterGroup fg : filterGroup) {
                    fg.addFilter(filter);
                }
                if (dataSetId != null) {
                    dataSet = dao.getDataSet(dataSetId.toString());
                    String where = ds.getWhereStrFromDB(filterGroup);
                    dao.createAnalysisDataSetTable(analysis.getDataTable(), tableName, "*", where);
                    createTable = true;
                } else {
                    queryId = analysis.getQueryId();
                    tableName = null;
                    if (analysis.getVariableInfo() != null) {
                        JSONArray array = new JSONArray();
                        Util.setJsonFromList(array, analysis.getVariableInfo());
                        variableInfo = array.toString();
                    }
                }
            } else if (queryId != null) {
                tableName = null;
                if (param.get("variable") != null) {
                    variableInfo = new JSONArray((List) param.get("variable")).toString();
                }
            } else {
                String message = String.format("Invalid parameter, %s", param.toString());
                log.error(message);
                BadException badException = new BadException("분석작업 생성에 실패 하였습니다.\n잘못된 값이 입력 되었습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
                throw badException;
            }

            dao.transactionStart();
            int id = dao.insertAnalysis(dataSetId, queryId, variableInfo, (String) param.get("name"), (String) param.get("createUser"), tableName, (String) param.get("subName"));
            dao.insertAnalysisGroupMapping(id, Integer.parseInt(String.valueOf(param.get("groupId"))));
            result.put("id", id);
            if (dataSet != null) {
                insertAnalysisAddColumn(dao, id, dataSet);
            }
            dao.transactionEnd();
        } catch (BadException be) {
            throw be;
        } catch (Exception e) {
            log.error("createAnalysis error: {}", e.getMessage());
            dao.rollBack();
            if (createTable) {
                dao.dropDataSetTable(tableName);
            }
            InternalException ie = new InternalException(e);
            ie.setMessage("분석작업 생성에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
        return result;
    }

    private AnalysisDao getDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new AnalysisDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()), user);
    }

    private void insertAnalysisAddColumn(AnalysisDao dao, int id, DataSet dataSet) throws Exception {
        List<Map<String, Object>> data = dao.selectAddColumn(dataSet.getId());
        if (data != null && data.size() > 0) {
            JSONArray jsonData = new JSONArray(data);
            dao.insertAnalysisColumnInfo(id, dataSet, jsonData.toString());
        } else {
            dao.insertAnalysisColumnInfo(id, dataSet, null);
        }
    }

    private void checkAnalysis(Analysis analysis, String analysisId) throws Exception {
        if (analysis == null) {
            String message = String.format("Analysis not found, %s", analysisId);
            log.error(message);
            BadException be = new BadException(String.format("분석작업(ID:%s)을 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", analysisId));
            throw be;
        }
    }

    public Object addAnalysisChart(int userId, int analysisId, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        ObjectMapper mapper = new ObjectMapper();
        param.put("analysisId", analysisId);
        Number key;

        OptionHelper optionHelper = OptionHelper.of((Map<String, Object>) param.get(Constants.ECHART_GET_OPTION), (String) param.get("type"));
        HashMap hashMap = mapper.readValue(optionHelper.toJson(), new TypeReference<Map<String, Object>>() {});

//        HashMap hashMap = mapper.readValue(param.get("option").toString(), new TypeReference<Map<String, Object>>() {});
        Object originOption = hashMap.clone();
        try {
            HashMap<String, Object> result = new HashMap<>();
            dao.transactionStart();
            key = dao.insertAnalysisChart(new AnalysisChart(param));
            dao.transactionEnd();
            result.put("key", key);
            result.put("origin_option", originOption);
            return result;
        } catch (Exception e) {
            dao.rollBack();
            log.error("addAnalysisChart error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("분석작업 차트 추가에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void updateAnalysisChart(int userId, int analysisId, int chartId, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        param.put("analysisId", analysisId);
        param.put("id", chartId);
        try {
            dao.transactionStart();
            dao.updateAnalysisChart(new AnalysisChart(param));
            dao.transactionEnd();
        } catch (Exception e) {
            dao.rollBack();
            log.error("updateAnalysisChart error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("분석작업 차트 업데이트에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public Object getAnalysisChartData(int userId, String id, Map<String, Object> param) throws Exception, CloneNotSupportedException {
        AnalysisDao dao = getDao(userId);
        DataService ds = new DataService(userDao);
        User user = userDao.getUserById(userId);
        param.put("analysisId", id);
        AnalysisChart chart = new AnalysisChart(param);
        HashMap hashMap = (HashMap) param.get("option");
        Object originOption = hashMap.clone();
        try {
            HashMap<String, Object> result = new HashMap<>();
            Map<String, Object> option = (Map<String, Object>) chartService.getAnalysisChartData(ds, dao, id, chart, null, user.getSsoId());
            result.put("option", option);
            result.put("origin_option", originOption);
            return result;
        } finally {
            dao.close();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, transactionManager = "transactionManager", rollbackFor = Exception.class)
    public String exportChart(int userId, Map<String, Object> param) throws Exception {
        String result;
        try {
            String data = (String) chartService.getExportData(new DataService(userDao), getDao(userId), Integer.parseInt(param.get("id").toString()), -1, userId, true, null, true, null);
            ClassPathResource htmlRes = new ClassPathResource("static-echart.html");
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(htmlRes.getInputStream()));
            StringBuilder htmlBuffer = new StringBuilder();
            while ((line = br.readLine()) != null) {
                htmlBuffer.append(line);
            }
            br.close();
            result = htmlBuffer.toString();
            result = result.replace("#data#", data);
        } catch (Exception e) {
            log.error("exportChart error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("분석작업 차트 내보내기가 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        }
        return result;
    }

    public void chartDownloadToCsv(HttpServletResponse response, int userId, int chartId, String uuId) throws Exception {
        // 테이블 헤더(컬럼) 가져오기
        AnalysisDao dao = getDao(userId);
        String analysisId = Integer.toString(dao.getAnalysisIdByUuId(uuId));
        List<Map<String, Object>> schemaData = null;
        Analysis analysis = dao.getAnalysisId(analysisId);
        schemaData = dao.getColumnInfo(analysis.getDataTable());

        ArrayList<String> columnData = new ArrayList<>();
        if (schemaData != null) {
            for (Map<String, Object> schema : schemaData) {
                columnData.add(schema.get("Field").toString());
            }
        }

        List<AnalysisChart> chartList = (List<AnalysisChart>) getAnalyticsChartDataList(userId, uuId);
        Map<String, Object> option = new HashMap<>();

        for (int i = 0; i < chartList.size(); i++) {
            if (chartId == chartList.get(i).getId()) {
                option = (Map<String, Object>) chartList.get(i).getOption();
            }
        }
        // series 데이터 형식 처리
        if (option.get("series") instanceof Map) {
            Map<String, Object> series = (Map<String, Object>) option.get("series");
            List<Map<String, Object>> seriesData = (List<Map<String, Object>>) series.get("data");

            PrintWriter writer = response.getWriter();
            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            ArrayList<Map<String, Object>> data = new ArrayList<>();
            for (int i = 0; i < seriesData.size(); i++) {
                data.add(seriesData.get(i));
            }

            ArrayList<String> CSV_DATA_NAME = new ArrayList<>();
            ArrayList<String> CSV_DATA_VALUE = new ArrayList<>();
            for (int i = 0; i < seriesData.size(); i++) {
                if (data.get(i).get("name") == null) {
                    CSV_DATA_NAME.add(null);
                    CSV_DATA_VALUE.add(null);
                } else {
                    CSV_DATA_NAME.add(data.get(i).get("name").toString());
                    CSV_DATA_VALUE.add(data.get(i).get("value").toString());
                }
            }

            String[] data_name = CSV_DATA_NAME.toArray(new String[CSV_DATA_NAME.size()]);
            String[] data_value = CSV_DATA_VALUE.toArray(new String[CSV_DATA_VALUE.size()]);
            csvWriter.writeNext(data_name);
            csvWriter.writeNext(data_value);

        } else {
            List<Map<String, Object>> series = (List<Map<String, Object>>) option.get("series");
            ArrayList<Object> seriesData = new ArrayList<>();
            series.forEach(a -> seriesData.add(a.get("data")));
            ArrayList<Object> data = (ArrayList<Object>) seriesData.get(0);
            ArrayList<String> CSV_DATA_NAME = new ArrayList<>();
            ArrayList<String> CSV_DATA_VALUE = new ArrayList<>();
            String[] split = Arrays.deepToString(new ArrayList[]{data}).replaceAll(" ", "")
                    .replaceAll("]", "")
                    .replaceAll("\\[", "")
                    .split(",");
            for (int i = 0; i < split.length; i++) {
                CSV_DATA_NAME.add(split[i]);
                try {
                    CSV_DATA_VALUE.add(split[++i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            PrintWriter writer = response.getWriter();
            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            String[] data_name = CSV_DATA_NAME.toArray(new String[CSV_DATA_NAME.size()]);
            String[] data_value = CSV_DATA_VALUE.toArray(new String[CSV_DATA_VALUE.size()]);
            csvWriter.writeNext(data_name);
            csvWriter.writeNext(data_value);
        }
    }

}
