package com.sk.bds.datainsight.service;

import com.sk.bds.datainsight.database.dao.DashboardDao;
import com.sk.bds.datainsight.database.dao.UserDao;
import com.sk.bds.datainsight.database.model.*;
import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.exception.InternalException;
import com.sk.bds.datainsight.util.DataSourceManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    UserDao userDao;
    @Autowired
    ChartService chartService;

    public Object getDashboardList(int userId, JSONObject param) throws Exception {
        DashboardDao dao = getDao(userId);
        try {
            Object list = dao.getDashboardList(param);
            int count = dao.getDashboardListCount(param);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", count);
            return result;
        } finally {
            dao.close();
        }
    }

    public void delDashboard(int userId, String dashboardId) throws Exception {
        DashboardDao dao = getDao(userId);
        try {
            Dashboard dashboard = dao.getDashboard(dashboardId);
            List<DashboardDataSet> dataSets = dao.getDashboardDataSetList(dashboardId);
            dao.transactionStart();
            dao.delDashboard(dashboardId);
            dao.transactionEnd();
            for (DashboardDataSet dataSet : dataSets) {
                dao.dropDataTable(dataSet.getDataTable());
            }
        } catch (Exception e) {
            log.error("delDashboard error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("대시보드(ID:%s) 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dashboardId));
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void updateDashboard(int userId, String dashboardId, Map<String, Object> param) throws Exception {
        DashboardDao dao = getDao(userId);
        try {
            dao.transactionStart();
            dao.updateDashboard(dashboardId, param.get("name"), param.get("subName"));
            if (param.get("groupId") != null) {
                dao.insertDashboardGroupMapping(Integer.parseInt(dashboardId), Integer.parseInt(String.valueOf(param.get("groupId"))));
            }
            dao.transactionEnd();
        } catch (Exception e) {
            log.error("updateDashboard error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("대시보드(ID:%s) 업데이트에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dashboardId));
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void createDashboard(int userId, Map<String, Object> param) throws Exception {
        Map<String, Object> paramDataSet = new HashMap<>();

        DashboardDao dao = getDao(userId);
        boolean createTable = false;
        String analysisId = param.get("analysisId").toString();
        List<Integer> chartList = (List<Integer>)param.get("chartList");
        String tableName = String.format("DASHBOARD_TABLE_%d", System.currentTimeMillis());
        paramDataSet.put("dataTable", tableName);
        try {
            Analysis analysis = dao.getAnalysisId(analysisId);
            List<AnalysisChart> analysisChartList = dao.getAnalysisChart(analysisId);

            Integer dataSetId = analysis.getDataSetId();
            Integer queryId = analysis.getQueryId();
            paramDataSet.put("dataSetId", dataSetId);
            paramDataSet.put("queryId", queryId);
            if (queryId != null) {
                paramDataSet.remove("dataTable");
            }
            paramDataSet.put("variableInfo", analysis.getVariableInfo());
            String srcTableName = analysis.getDataTable();
            DataService ds = new DataService(userDao);

            List<Filter> filter = dao.getAnalysisFilter(analysisId);
            List<AnalysisFilterGroup> filterGroup = dao.getAnalysisFilterGroup(analysisId);
            for (AnalysisFilterGroup fg : filterGroup) {
                fg.addFilter(filter);
            }
            if (srcTableName != null) {
                String where = ds.getWhereStrFromDB(filterGroup);
                dao.createDashboardDataSetTable(srcTableName, tableName, "*", where);
                createTable = true;
            }
            dao.transactionStart();
            int dashboardId = dao.insertDashboard(new Dashboard(param));
            dao.insertDashboardGroupMapping(dashboardId, Integer.parseInt(String.valueOf(param.get("groupId"))));
            paramDataSet.put("dashboardId", dashboardId);
            int dashboardDataSetId = dao.insertDashboardDataSet(new DashboardDataSet(paramDataSet));
            int offsetY = getNextChartPositionY(null);
            boolean isSingleChartToAdd = (chartList.size() <= 1);
            for (AnalysisChart chart : analysisChartList) {
                if (chartList.contains(chart.getId())) {
                    offsetY = setChartPosition(chart, offsetY, isSingleChartToAdd);
                    dao.insertDashboardChart(new DashboardChart(dashboardId, dashboardDataSetId, chart));
                }
            }
            insertFilterInfo(dao, filterGroup, dashboardDataSetId, dataSetId, queryId, analysis.getId());
            dao.transactionEnd();
        } catch (Exception e) {
            log.error("createDashboard error: {}", e.getMessage());
            dao.rollBack();
            if (createTable) {
                dao.dropDataTable(tableName);
            }
            InternalException ie = new InternalException(e);
            ie.setMessage("대시보드 생성에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    private void insertFilterInfo(DashboardDao dao, List<AnalysisFilterGroup> filterGroup, int dashboardDataSetId, Integer dataSetId, Integer queryId, int analysisId) throws Exception {
        List<Map<String, Object>> filterInfoParam = new ArrayList<>();
        for (AnalysisFilterGroup fg : filterGroup) {
            if (fg.getEnable()) {
                DashboardFilterGroup dashboardFilterGroup = new DashboardFilterGroup(dashboardDataSetId, dataSetId, queryId);
                int id = dao.insertDashboardFilterGroupInfo(dashboardFilterGroup);
                for (Filter filter : fg.getFilter()) {
                    if (filter.enable) {
                        Map<String, Object> param = new HashMap<>();
                        param.put("groupId", id);
                        param.put("column", filter.getColumn());
                        param.put("condition", filter.getCondition());
                        param.put("value1", filter.getValue1());
                        param.put("value2", filter.getValue2());
                        filterInfoParam.add(param);
                    }
                }
            }
        }
        dao.insertDashboardColumnInfo(dashboardDataSetId, analysisId);
        if (filterInfoParam.size() > 0) {
            dao.insertDashboardFilterInfo(filterInfoParam);
        }
    }

    public Object getDashboardChartDataList(int userId, String dashboardId) throws Exception {
        DashboardDao dao = getDao(userId);
        try {
            DataService ds = new DataService(userDao);
            return chartService.getDashboardChartDataMap(ds, dao, dashboardId);
        } finally {
            dao.close();
        }
    }

    public Map<String, Object> getDashboardDetail(int userId, String dashboardId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        DashboardDao dao = getDao(userId);
        try {
            Dashboard dashboard = dao.getDashboard(dashboardId);
            if (dashboard != null) {
                result.put("id", dashboard.getId());
                result.put("name", dashboard.getName());
                result.put("subName", dashboard.getSubName());
                result.put("createUser", dashboard.getCreateUser());
                result.put("charts", dao.getDashboardChartList(dashboardId));
                List<DashboardDataSet> dataSets = dao.getDashboardDataSetList(dashboardId);
                result.put("dataSets", dataSets);

                Map<Integer, ArrayList<String[]>> column = new HashMap<>();
                Map<Integer, List<Map<String, Object>>> data = new HashMap<>();
                result.put("column", column);
                result.put("data", data);

            } else {
                log.error(String.format("Not found dashboard %s", dashboardId));
                BadException be = new BadException(String.format("대시보드(ID:%s)를 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dashboardId));
                throw be;
            }
        } finally {
            dao.close();
        }
        return result;
    }

    public void addToDashboard(int userId, String dashboardId, Map<String, Object> param) throws Exception {
        Map<String, Object> paramDataSet = new HashMap<>();

        DashboardDao dao = getDao(userId);
        boolean createTable = false;
        String analysisId = param.get("analysisId").toString();
        List<Integer> chartList = (List<Integer>)param.get("chartList");
        String tableName = String.format("DASHBOARD_TABLE_%d", System.currentTimeMillis());
        paramDataSet.put("dataTable", tableName);
        try {
            int iDashboardId = Integer.parseInt(dashboardId);
            paramDataSet.put("dashboardId", iDashboardId);
            Analysis analysis = dao.getAnalysisId(analysisId);
            List<AnalysisChart> analysisChartList = dao.getAnalysisChart(analysisId);
            List<DashboardChart> dashboardChartList = dao.getDashboardChartList(dashboardId);

            Integer dataSetId = analysis.getDataSetId();
            Integer queryId = analysis.getQueryId();
            paramDataSet.put("dataSetId", dataSetId);
            paramDataSet.put("queryId", queryId);
            if (queryId != null) {
                paramDataSet.remove("dataTable");
            }
            paramDataSet.put("variableInfo", analysis.getVariableInfo());
            String srcTableName = analysis.getDataTable();
            DataService ds = new DataService(userDao);

            List<Filter> filter = dao.getAnalysisFilter(analysisId);
            List<AnalysisFilterGroup> filterGroup = dao.getAnalysisFilterGroup(analysisId);
            for (AnalysisFilterGroup fg : filterGroup) {
                fg.addFilter(filter);
            }
            if (srcTableName != null) {
                String where = ds.getWhereStrFromDB(filterGroup);
                dao.createDashboardDataSetTable(srcTableName, tableName, "*", where);
                createTable = true;
            }
            dao.transactionStart();
            int dashboardDataSetId = dao.insertDashboardDataSet(new DashboardDataSet(paramDataSet));
            int offsetY = getNextChartPositionY(dashboardChartList);
            boolean isSingleChartToAdd = (chartList.size() <= 1);
            for (AnalysisChart chart : analysisChartList) {
                if (chartList.contains(chart.getId())) {
                    offsetY = setChartPosition(chart, offsetY, isSingleChartToAdd);
                    dao.insertDashboardChart(new DashboardChart(iDashboardId, dashboardDataSetId, chart));
                }
            }
            insertFilterInfo(dao, filterGroup, dashboardDataSetId, dataSetId, queryId, analysis.getId());
            dao.transactionEnd();
        } catch (Exception e) {
            log.error("addToDashboard error: {}", e.getMessage());
            dao.rollBack();
            if (createTable) {
                dao.dropDataTable(tableName);
            }
            InternalException ie = new InternalException(e);
            ie.setMessage("대시보드 추가에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    private int getNextChartPositionY(List<DashboardChart> dashboardChartList) {
        if (dashboardChartList == null) { return 0; }

        int y = 0;
        for (DashboardChart dChart : dashboardChartList) {
//            int h = dChart.getDrawInfo().getY() + dChart.getDrawInfo().getH();
//            if (y < h) { y = h; }
        }
        return y;
    }
    private int setChartPosition(AnalysisChart chart, int offsetY, boolean isSingleChartToAdd) {
        if (isSingleChartToAdd) {
//            DrawInfo drawInfo = chart.getDrawInfo();
//            drawInfo.setX(0);
//            drawInfo.setY(offsetY);
//            chart.setDrawInfo(drawInfo);

//            offsetY += drawInfo.getH();
        } else {
//            DrawInfo drawInfo = chart.getDrawInfo();
//            drawInfo.setY(offsetY + drawInfo.getY());
//            chart.setDrawInfo(drawInfo);
        }
        return offsetY;
    }

    public void updateDashboardChart(int userId, int dashboardId, int chartId, Map<String, Object> param) throws Exception {
        DashboardDao dao = getDao(userId);
        param.put("dashboardId", dashboardId);
        param.put("id", chartId);
        try {
            dao.transactionStart();
            dao.updateDashboardChart(new DashboardChart(param));
            dao.transactionEnd();
        } catch (Exception e) {
            dao.rollBack();
            log.error("updateDashboardChart error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("대시보드 차트 업데이트에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void delDashboardChart(int userId, String chartId) throws Exception {
        DashboardDao dao = getDao(userId);
        try {
            String dataTableName = null;
            DashboardChart chart = dao.getDashboardChart(chartId);
            int dashboardDataId = chart.getDashboardDataId();

            dao.transactionStart();
            dao.delDashboardChart(chartId);
            // 삭제한 차트가 참조하던 대시보드 데이터셋을 다른 차트에서 참조 중인지 확인
            List<DashboardChart> chartList = dao.getDashboardChartListByDataId(Integer.toString(dashboardDataId));
            if (chartList.size() < 1) {
                // 더이상 참조하는 차트가 없는 대시보드 데이터셋일 경우 대시보드 데이터셋 삭제
                DashboardDataSet dataSet = dao.getDashboardDataSet(Integer.toString(dashboardDataId));
                if (dataSet != null) {
                    dao.delDashboardDataSet(Integer.toString(dashboardDataId));
                    dataTableName = dataSet.getDataTable();
                }
            }
            dao.transactionEnd();
            if (dataTableName != null) {
                dao.dropDataTable(dataTableName);
            }
        } catch (Exception e) {
            dao.rollBack();
            log.error("delDashboardChart error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("대시보드 차트 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    private DashboardDao getDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new DashboardDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()));
    }
}
