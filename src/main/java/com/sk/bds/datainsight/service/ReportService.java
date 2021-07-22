package com.sk.bds.datainsight.service;

import com.sk.bds.datainsight.database.dao.AnalysisDao;
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
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    UserDao userDao;
    @Autowired
    ChartService chartService;

    public Object getList(int userId, JSONObject param) throws Exception {
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

    public void deleteDashboard(int userId, String dashboardId) throws Exception {
        DashboardDao dao = getDao(userId);
        try {
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

    public Object getChartDataList(int userId, String dashboardId) throws Exception {
        DashboardDao dao = getDao(userId);
        try {
            DataService ds = new DataService(userDao);
            return chartService.getDashboardChartDataMap(ds, dao, dashboardId);
        } finally {
            dao.close();
        }
    }

    private DashboardDao getDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new DashboardDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()), user);
    }

    private AnalysisDao getAnalysisDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new AnalysisDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()));
    }

    public Map<String, Object> getDetail(int userId, String dashboardId, String analysisId) throws Exception {
        DataService ds = new DataService(userDao);
        User user = userDao.getUserById(userId);
        Map<String, Object> result = new HashMap<>();
        DashboardDao dao = getDao(userId);
        AnalysisDao analysisDao = getAnalysisDao(userId);
        try {
            Dashboard dashboard = dao.getDashboard(dashboardId);
            if (dashboard != null) {
                result.put("id", dashboard.getId());
                result.put("name", dashboard.getName());
                result.put("createUser", dashboard.getCreateUser());
                List<DashboardChart> dashboardChartList = dao.getDashboardChartList(dashboardId);
                result.put("dashboardObject", dashboardChartList);
                for (DashboardChart dashboardChart : dashboardChartList) {
                    if (dashboardChart.getDrawInfo().getOption().containsKey("chart")) {
                        List<Object> analysisCharts = new ArrayList<>();
                        List<AnalysisChart> chartList = chartService.getAnalysisChartDataMap(ds, analysisDao, analysisId, null, user.getSsoId());
                        for (AnalysisChart analysisChart : chartList) {
                            if (analysisChart.getId() == dashboardChart.getDrawInfo().getOption().get("chartId")) {
                                analysisCharts.add(analysisChart);
                            }
                        }
                        result.put("charts", analysisCharts);
                    }
                }

            } else {
                log.error(String.format("Not found report %s", dashboardId));
                throw new BadException(String.format("리포트(ID:%s)를 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", dashboardId));
            }
        } finally {
            dao.close();
        }
        return result;
    }
}
