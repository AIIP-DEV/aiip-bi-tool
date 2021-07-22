package com.sk.bds.datainsight.controller;

import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.service.AccountService;
import com.sk.bds.datainsight.service.DashboardService;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Map;

@RestController
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    DashboardService service;
    @Autowired
    AccountService accountService;

    @RequestMapping(value = "/dashboard/init", method = RequestMethod.PUT)
    public Object firstInit(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        accountService.firstInit((int)request.getAttribute("userId"), (String)request.getAttribute("ssoId"));
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public Object getDashboardList(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        int userId = (int)request.getAttribute("userId");
        String parameter = request.getParameter("param");
        if (parameter == null) {
            throw new BadException("param is null");
        }
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getDashboardList(userId, new JSONObject(URLDecoder.decode(parameter, "UTF-8"))));
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.PUT)
    public Object createDashboard(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"analysisId", "name", "thumbImg"};
        Util.checkParameter(verifyKeys, null, body);
        service.createDashboard(userId, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/dashboard/{id}", method = RequestMethod.GET)
    public Object getDashboardDetail(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getDashboardDetail(userId, id));
    }

    @RequestMapping(value = "/dashboard/chart/list/{id}", method = RequestMethod.GET)
    public Object getDashboardChartDataList(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getDashboardChartDataList(userId, id));
    }

    @RequestMapping(value = "/dashboard/{id}", method = RequestMethod.DELETE)
    public Object delDashboard(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.delDashboard(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/dashboard/{id}", method = RequestMethod.PUT)
    public Object updateDashboard(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"name"};
        Util.checkParameter(verifyKeys, null, body);
        int userId = (int)request.getAttribute("userId");
        service.updateDashboard(userId, id, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    // 대시보드에 차트 추가
   @RequestMapping(value = "/dashboard/{id}", method = RequestMethod.POST)
    public Object addToDashboard(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"analysisId", "chartList"};
        Util.checkParameter(verifyKeys, null, body);
        service.addToDashboard(userId, id, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }
    // 대시보드 차트 업데이트
    @RequestMapping(value = "/dashboard/{id}/chart/{chartId}", method = RequestMethod.PUT)
    public Object updateDashboardChart(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id, @PathVariable final String chartId) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"chart", "valueInfo", "drawInfo"};
        Util.checkParameter(verifyKeys, null, body);
        service.updateDashboardChart(userId, Integer.parseInt(id), Integer.parseInt(chartId), body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }
    // 대시보드 차트 삭제
    @RequestMapping(value = "/dashboard/{id}/chart/{chartId}", method = RequestMethod.DELETE)
    public Object delDashboardChart(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id, @PathVariable final String chartId) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.delDashboardChart(userId, chartId);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }
}
