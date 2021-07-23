package com.sk.bds.datainsight.service;

import au.com.bytecode.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.bds.datainsight.database.dao.AnalysisDao;
import com.sk.bds.datainsight.database.dao.SettingDao;
import com.sk.bds.datainsight.database.dao.UserDao;
import com.sk.bds.datainsight.database.model.*;
import com.sk.bds.datainsight.exception.AuthException;
import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.exception.InternalException;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@Service
public class AnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);
    private static final String secretKey = "datainsight:key:20190312";
    private static final String IV = "1234567890123456";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserDao userDao;

    @Autowired
    SettingDao settingDao;

    @Autowired
    ChartService chartService;

    @Value("${service.domain}")
    private String serviceDomain;
    @Value("${service.api}")
    private String serviceApi;

    public Map<String, Object> getAnalysisDetail(int userId, String analysisId, boolean isData) throws Exception {
        AnalysisDao dao = getDao(userId);
        Map<String, Object> result = new HashMap<>();
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
            result.put("charts", dao.getAnalysisChart(analysisId));
        } finally {
            dao.close();
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
                insertAddColumn(dao, id, dataSet);
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

    private void insertAddColumn(AnalysisDao dao, int id, DataSet dataSet) throws Exception {
        List<Map<String, Object>> data = dao.selectAddColumn(dataSet.getId());
        if (data != null && data.size() > 0) {
            JSONArray jsonData = new JSONArray(data);
            dao.insertAnalysisColumnInfo(id, dataSet, jsonData.toString());
        } else {
            dao.insertAnalysisColumnInfo(id, dataSet, null);
        }
    }

    public void updateAnalysis(int userId, String analysisId, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            Analysis analysis = dao.getAnalysisId(analysisId);
            checkAnalysis(analysis, analysisId);
            if (param.get("name") != null) {
                analysis.setName(param.get("name").toString());
            }
            if (param.get("subName") != null) {
                analysis.setSubName(param.get("subName").toString());
            }
            if (param.get("thumbImg") != null) {
                analysis.setImage(param.get("thumbImg").toString());
            }
            if (param.get("variable") != null) {
                analysis.setVariableInfo((List) param.get("variable"));
            }
            dao.transactionStart();
            dao.updateAnalysis(analysis);
            if (param.get("groupId") != null) {
                dao.insertAnalysisGroupMapping(Integer.parseInt(analysisId), Integer.parseInt(String.valueOf(param.get("groupId"))));
            }
            dao.transactionEnd();
        } catch (BadException be) {
            throw be;
        } catch (Exception e) {
            dao.rollBack();
            log.error("updateAnalysis error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("분석작업(ID:%s) 업데이트에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", analysisId));
            throw ie;
        } finally {
            dao.close();
        }
    }

    public Object getAnalysisList(int userId, JSONObject param) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            Object list = dao.getAnalysisList(param);
            int count = dao.getAnalysisListCount(param);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", count);
            return result;
        } finally {
            dao.close();
        }
    }

    public void delAnalysis(int userId, String analysisId) throws Exception {
        AnalysisDao dao = getDao(userId);
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

    public void addAnalysisFilter(int userId, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            dao.transactionStart();
            dao.insertAnalysisFilter(Integer.parseInt(param.get("groupId").toString()), param.get("column").toString(), param.get("condition").toString(),
                    param.get("value1"), param.get("value2"), param.get("enable").toString());
            dao.transactionEnd();
        } catch (Exception e) {
            dao.rollBack();
            log.error("addAnalysisFilter error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("필터 추가에 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void updateAnalysisFilter(int userId, int filterId, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            dao.transactionStart();
            dao.updateAnalysisFilter(param.get("column").toString(), param.get("condition").toString(),
                    param.get("value1"), param.get("value2"), param.get("enable").toString(), filterId);
            dao.transactionEnd();
        } catch (Exception e) {
            dao.rollBack();
            log.error("updateAnalysisFilter error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("필터 업데이트에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void delAnalysisFilter(int userId, String filterId) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            dao.transactionStart();
            dao.delAnalysisFilter(filterId);
            dao.transactionEnd();
        } catch (Exception e) {
            dao.rollBack();
            log.error("delAnalysisFilter error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("필터 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public int addAnalysisFilterGroup(int userId, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        int id = -1;
        try {
            dao.transactionStart();
            id = dao.insertAnalysisFilterGroup(Integer.parseInt(param.get("analysisId").toString()), param.get("name").toString(),
                    param.get("enable").toString());
            dao.transactionEnd();
        } catch (Exception e) {
            dao.rollBack();
            log.error("addAnalysisFilterGroup error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("필터 그룹 추가에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
        return id;
    }

    public void updateAnalysisFilterGroup(int userId, int id, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            dao.transactionStart();
            dao.updateAnalysisFilterGroup(param.get("name").toString(), param.get("enable").toString(), id);
            dao.transactionEnd();
        } catch (Exception e) {
            dao.rollBack();
            log.error("updateAnalysisFilterGroup error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("필터 그룹 업데이트에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void delAnalysisFilterGroup(int userId, String id) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            dao.transactionStart();
            dao.delAnalysisFilterGroup(id);
            dao.transactionEnd();
        } catch (Exception e) {
            dao.rollBack();
            log.error("delAnalysisFilterGroup error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("필터 그룹 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void addAnalysisChart(int userId, int analysisId, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        param.put("analysisId", analysisId);
        try {
            dao.transactionStart();
            Number key = dao.insertAnalysisChart(new AnalysisChart(param));
            dao.transactionEnd();
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

    public void delAnalysisChart(int userId, String chartId) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            dao.transactionStart();
            dao.delAnalysisChart(chartId);
            dao.transactionEnd();
        } catch (Exception e) {
            dao.rollBack();
            log.error("delAnalysisChart error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("분석작업 차트 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public HashMap<String, Object> getAnalysisMeta(int userId) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            HashMap<String, Object> result = new HashMap<>();
            result.put("chart", dao.getChartInfo());
            return result;
        } finally {
            dao.close();
        }
    }

    public List<Map<String, Object>> getUsers(int userId) throws Exception {
        return userDao.getUsers(userId);
    }

    public void shareAnalysis(int userId, Map<String, Object> param) throws Exception {
        Map<String, Object> paramDataSet = new HashMap<>();

        AnalysisDao dao = getDao(userId);
        int targetId = Integer.parseInt(param.get("targetId").toString());
        AnalysisDao targetDao = getDao(targetId);
        boolean createTable = false;
        String analysisId = param.get("analysisId").toString();
        String tableName = String.format("DASHBOARD_TABLE_%d", System.currentTimeMillis());
        String srcDbName = userDao.getUserDbName(userId);
        paramDataSet.put("dataTable", tableName);
        try {
            Analysis analysis = dao.getAnalysisId(analysisId);
            Integer queryId = analysis.getQueryId();
            if (queryId != null) {
                throw new BadException("쿼리로 생성한 분석 작업은 공유 할 수 없습니다.");
            }
            checkAnalysis(analysis, analysisId);
            paramDataSet.put("dataSetId", null);
            param.put("name", analysis.getName());
            param.put("thumbImg", analysis.getImage());
            String srcTableName = analysis.getDataTable();
            DataService ds = new DataService(userDao);

            List<Filter> filter = dao.getAnalysisFilter(analysisId);
            List<AnalysisFilterGroup> filterGroup = dao.getAnalysisFilterGroup(analysisId);
            for (AnalysisFilterGroup fg : filterGroup) {
                fg.addFilter(filter);
            }
            String where = ds.getWhereStrFromDB(filterGroup);

            targetDao.createShareAnalysisDataSetTable(srcDbName, srcTableName, tableName, "*", where);
            createTable = true;
            targetDao.transactionStart();
            int dashboardId = targetDao.insertDashboard(new Dashboard(param));
            int dashboardDataSetId = targetDao.insertDashboardDataSet(new DashboardDataSet(paramDataSet));
            List<AnalysisChart> list = dao.getAnalysisChart(analysisId);
            for (AnalysisChart chart : list) {
//                targetDao.insertDashboardChart(new DashboardChart(dashboardId, dashboardDataSetId, chart));
            }
            targetDao.transactionEnd();
        } catch (BadException be) {
            throw be;
        } catch (Exception e) {
            log.error("shareAnalysis error: {}", e.getMessage());
            targetDao.rollBack();
            if (createTable) {
                targetDao.dropDataSetTable(tableName);
            }
            InternalException ie = new InternalException(e);
            ie.setMessage("분석작업 공유가 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
            targetDao.close();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, transactionManager = "transactionManager", rollbackFor = Exception.class)
    public String exportChart(int userId, Map<String, Object> param) throws Exception {
        String result = "#token#";
        ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes());
        InputStreamResource resource = new InputStreamResource(stream);
        String accessKey = (String)param.get("accessKey");
        try {
            if (accessKey != null) {
                ClassPathResource htmlRes = new ClassPathResource("dynamic-chart.html");
                String line = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(htmlRes.getInputStream()));
                StringBuffer htmlBuffer = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    htmlBuffer.append(line);
                }
                br.close();
                result = htmlBuffer.toString().replaceAll("#service-domain#", serviceDomain).replace("#api#", serviceApi);
                param.put("userId", userId);
                settingDao.insertExportChart(param);
                long id = (long) param.get("id");
                String exportToken = encryptAES(String.format("%d\n%s\n%d\n%d", id, accessKey, param.get("chartId"), userId));
                log.info("exportToken : " + exportToken);
                result = result.replace("#token#", exportToken);
                result = result.replace("#variable#", objectMapper.writeValueAsString(param.get("variable")));
            } else {
                String data = (String) chartService.getExportData(new DataService(userDao), getDao(userId), Integer.parseInt(param.get("chartId").toString()), -1, userId, true, null, true, null);
                ClassPathResource htmlRes = new ClassPathResource("static-chart.html");
                String line = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(htmlRes.getInputStream()));
                StringBuffer htmlBuffer = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    htmlBuffer.append(line);
                }
                br.close();
                result = htmlBuffer.toString();
                result = result.replace("#data#", data);
            }
        } catch (Exception e) {
            log.error("exportChart error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("분석작업 차트 내보내기가 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        }
        return result;
    }

    public Object getExportChartData(String exportToken, Map<String, String> variableMap) throws Exception {
        String[] data = decryptAES(exportToken).split("\n");
        int exportId = Integer.parseInt(data[0]);
        String accessKey = data[1];
        int chartId = Integer.parseInt(data[2]);
        int userId = Integer.parseInt(data[3]);
        String status = settingDao.selectAccessKeyStatus(accessKey);
        if (status == null || "0".equals(status)) {
            throw new AuthException("AccessKey 가 없거나 비활성화 상태입니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
        }
        return chartService.getExportData(new DataService(userDao), getDao(userId), chartId, exportId, userId, false, variableMap, true, null);
    }

    public void exportData(int userId, String chartId, String userAgent, HttpServletResponse response) throws Exception {
        exportData(userId, Integer.parseInt(chartId), userAgent, -1, null, null, response);
    }

    public Object exportData(String exportToken, Map<String, String> variableMap, String userAgent, HttpServletResponse response) throws Exception {
        String[] data = decryptAES(exportToken).split("\n");
        int exportId = Integer.parseInt(data[0]);
        String accessKey = data[1];
        int chartId = Integer.parseInt(data[2]);
        int userId = Integer.parseInt(data[3]);
        String status = settingDao.selectAccessKeyStatus(accessKey);
        if (status == null || "0".equals(status)) {
            throw new AuthException("AccessKey 가 없거나 비활성화 상태입니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
        }
        return exportData(userId, chartId, userAgent, exportId, accessKey, variableMap, response);
    }

    private Object exportData(int userId, int chartId, String userAgent, int exportId, String accessKey, Map<String, String> variableMap, HttpServletResponse response) throws Exception {
        String name = String.format("chart_%d", chartId);
        name = Util.getEncodedFilename(name, Util.getBrowser(userAgent));
        List<String> headerList = new ArrayList<>();
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) chartService.getExportData(new DataService(userDao), getDao(userId), chartId, exportId, userId, false, variableMap, false, headerList);
/*        String[] headers = new String[headerList.size()];
        headerList.toArray(headers);

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
        csvWriter.close();*/
        return objectMapper.writeValueAsString(dataList);
    }

    public Object getAnalysisChartDataList(int userId, String id) throws Exception {
        AnalysisDao dao = getDao(userId);
        DataService ds = new DataService(userDao);
        User user = userDao.getUserById(userId);
        try {
            return chartService.getAnalysisChartDataMap(ds, dao, id, null, user.getSsoId());
        } finally {
            dao.close();
        }
    }

    public Object getAnalysisChartData(int userId, String id, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        DataService ds = new DataService(userDao);
        param.put("analysisId", id);
        AnalysisChart chart = new AnalysisChart(param);
        try {
            HashMap<String, Object> result = new HashMap<>();
            Map<String, Object> option = (Map<String, Object>) chartService.getAnalysisChartData(ds, dao, id, chart, null, null);

            result.put("option", option);
            Map<String, Object> drawOption = new HashMap<>();
            return result;
        } finally {
            dao.close();
        }
    }
    public Object getSampleData(int userId, String analysisId) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            String query = null;
            Analysis analysis = dao.getAnalysisId(analysisId);
            Map<String, Object> result = dao.getAnalysisSrcInfo(analysisId);
            if (analysis.getQueryId() != null) {
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
                query = String.format("SELECT * FROM (%s) A LIMIT 100", queryText);
            } else {
                query = String.format("SELECT * FROM %s LIMIT 100", analysis.getDataTable());
            }
            result.put("sample", dao.getDataSetTable(query));
            return result;
        } finally {
            dao.close();
        }
    }

    private String encryptAES(String str) throws Exception {
        byte[] keyData = secretKey.getBytes();
        SecretKey secretKey = new SecretKeySpec(keyData, "AES");
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV.getBytes()));
        byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decryptAES(String str) throws Exception {
        byte[] keyData = secretKey.getBytes();
        SecretKey secretKey = new SecretKeySpec(keyData, "AES");
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV.getBytes("UTF-8")));
        byte[] byteStr = Base64.getDecoder().decode(str);
        return new String(c.doFinal(byteStr), "UTF-8");
    }

    private void checkAnalysis(Analysis analysis, String analysisId) throws Exception {
        if (analysis == null) {
            String message = String.format("Analysis not found, %s", analysisId);
            log.error(message);
            BadException be = new BadException(String.format("분석작업(ID:%s)을 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", analysisId));
            throw be;
        }
    }

    private AnalysisDao getDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new AnalysisDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()));
    }


    // for v2
    public HashMap<String, Object> getAnalysisChartMeta(int userId) throws Exception {
        HashMap<String, Object> chartMeta = new HashMap<>();
        EchartOption optionTest = new EchartOption();
        EchartOption.ChartType[] chartTypes = EchartOption.ChartType.values();
        for (EchartOption.ChartType item :
                chartTypes) {
            chartMeta.put(item.getName(), optionTest.getChartOption(item.getType()));
        }

        return chartMeta;
    }
}
