package com.sk.bds.datainsight.service;

import com.sk.bds.datainsight.database.dao.*;
import com.sk.bds.datainsight.database.dao.ReportDao;
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
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    UserDao userDao;
    @Autowired
    ChartService chartService;

    public Object getList(int userId, JSONObject param) throws Exception {
        ReportDao dao = getDao(userId);
        try {
            Object list = dao.getReportList(param);
            int count = dao.getReportListCount(param);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", count);
            return result;
        } finally {
            dao.close();
        }
    }

    public void delete(int userId, String reportId) throws Exception {
        ReportDao dao = getDao(userId);
        try {
            dao.transactionStart();
            dao.delReport(reportId);
            dao.transactionEnd();

        } catch (Exception e) {
            log.error("delReport error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("대시보드(ID:%s) 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", reportId));
            throw ie;
        } finally {
            dao.close();
        }
    }

//    public Object getChartDataList(int userId, String ReportId) throws Exception {
//        ReportDao dao = getDao(userId);
//        try {
//            DataService ds = new DataService(userDao);
//            return chartService.getReportChartDataMap(ds, dao, ReportId);
//        } finally {
//            dao.close();
//        }
//    }

    private ReportDao getDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new ReportDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()), user);
    }

    private AnalysisDao getAnalysisDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new AnalysisDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()));
    }

    public Map<String, Object> getDetail(int userId, String reportId, String analysisId) throws Exception {
        DataService ds = new DataService(userDao);
        User user = userDao.getUserById(userId);
        Map<String, Object> result = new HashMap<>();
        ReportDao dao = getDao(userId);
        AnalysisDao analysisDao = getAnalysisDao(userId);
        try {
            Report report = dao.getReport(reportId);
            if (report != null) {
                result.put("id", report.getId());
                result.put("name", report.getName());
                result.put("createUser", report.getCreateUser());
                List<ReportChart> reportChartList = dao.getReportChartList(reportId);
                result.put("dashboardObject", reportChartList);
                for (ReportChart reportChart : reportChartList) {
                    if (reportChart.getReportId() == Integer.parseInt(reportId)) {
                        List<Object> analysisCharts = new ArrayList<>();
                        List<AnalysisChart> chartList = chartService.getAnalysisChartDataMap(ds, analysisDao, analysisId, null, user.getSsoId());
                        // drawInfo 안에 차트 찾기
//                        DrawInfo drawInfo = reportChart.getDrawInfo();
//                        Map<String, Object> drawInfoBody = drawInfo.getBody();
//                        Map<String, Object> body = (Map<String, Object>) drawInfoBody.get("body");
//                        List<Map<String, Object>> layout = (List<Map<String, Object>>) body.get("layout");
//
//                        for(Map<String, Object> type : layout){
//                            if(type.get("type") == "CHART"){
//                                for (AnalysisChart analysisChart : chartList) {
//                                    if (analysisChart.getId() != null) {
//                                        analysisCharts.add(analysisChart);
//                                    }
//                                }
//                            }
//                        }
//                        result.put("charts", analysisCharts);
                    }
                }
            } else {
                log.error(String.format("Not found report %s", reportId));
                throw new BadException(String.format("리포트(ID:%s)를 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", reportId));
            }
        } finally {
            dao.close();
        }
        return result;
    }

    public void create(int userId, Map<String, Object> param) throws Exception {
        ReportDao dao = getDao(userId);
        param.put("thumbImg", "");
        param.put("createUser", "");
        try {
            int workflowId = Integer.parseInt(param.get("workflowId").toString());
            dao.transactionStart();
            int reportId = dao.insertReport(new Report(param));
            Integer count = dao.getGroupCount(Integer.toString(workflowId));
            if (count == 0) {
                dao.createGroup(Integer.toString(workflowId));
            }
            dao.transactionEnd();

            // TODO
            dao.transactionStart();
            int groupId = dao.getGroupId(String.valueOf(workflowId));
            if (reportId > 0) {
                createReportChart(userId, param, groupId, reportId);
            }
            dao.insertReportGroupMapping(reportId, groupId);
            dao.transactionEnd();
        } catch (Exception e) {
            log.error("createReport error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage("대시보드 생성에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }


    public void update(int userId, String reportId, Map<String, Object> param) throws Exception {
        ReportDao dao = getDao(userId);
        try {
            dao.transactionStart();
            dao.updateReportChart(new ReportChart(param));
//            dao.updateReport(reportId, param.get("name"), param.get("thumb_img"), param.get("create_user"));
            // TODO
//            dao.insertReportGroupMapping(Integer.parseInt(reportId), Integer.parseInt(String.valueOf("workflowId")));
            dao.transactionEnd();
        } catch (Exception e) {
            log.error("updateReport error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("대시보드(ID:%s) 업데이트에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", reportId));
            throw ie;
        } finally {
            dao.close();
        }
    }

    public void createReportChart(int userId, Map<String, Object> param, int groupId, int reportId) throws Exception {
        ReportDao dao = getDao(userId);

        param.put("reportId", reportId);
        param.put("thumbImg", "");
        param.put("createUser", "");
        List<Map<String, Object>> layout = (List<Map<String, Object>>) param.get("layout");
        for (Map<String, Object> map : layout) {
            if (map.get("type").equals("CHART")) {
                param.put("analysis_chart_id", map.get("analysis"));
                param.put("type", "CHART");
            }
        }
        try {

//            dao.transactionStart();
            dao.insertReportChart(new ReportChart(param));
//            dao.transactionEnd();
        } catch (Exception e) {
            log.error("addToReport error: {}", e.getMessage());
//            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage("대시보드 추가에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
//            dao.close();
        }
    }

    public void updateReportChart(int userId, String reportId, Map<String, Object> param) throws Exception {
        ReportDao dao = getDao(userId);
        param.put("reportId", reportId);
        try {
            // TODO : analysis 차트 아이디 찾는 로직 추가
            dao.transactionStart();
            // 없는 거면 지우기
            dao.updateReportChart(new ReportChart(param));
            dao.transactionEnd();
        } catch (Exception e) {
            log.error("updateReportChart error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage("대시보드 차트 업데이트에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
    }

    public Object getDetailById(int userId, String reportId) {
        DataService ds = new DataService(userDao);
        User user = userDao.getUserById(userId);
        Map<String, Object> result = new HashMap<>();
        ReportDao dao = null;
        ReportChart test = null;
        try {
            dao = getDao(userId);
            Report report = dao.getReport(reportId);
            if (report != null) {
                test = dao.getReportChartById(reportId);
            } else {
                log.error(String.format("Not found report %s", reportId));
                throw new BadException(String.format("리포트(ID:%s)를 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", reportId));
            }
        } catch (Exception e) {
            log.error("updateReportChart error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("리포트(ID:%s)를 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", reportId));
        } finally {
            if (dao != null) {
                dao.close();
            }
        }
        return test;
    }
}
