package com.sk.bds.datainsight.controller;

import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.service.ReportService;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Map;

@RestController
@RequestMapping(value = "/report", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ReportController {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    ReportService reportService;

    @GetMapping("")
    public Object getList(final HttpServletRequest request) throws Exception {
        int userId = (int)request.getAttribute("userId");
        String parameter = request.getParameter("param");
        if (parameter == null) {
            throw new BadException("param is null");
        }
        ResponseBase res = new ResponseBase();
        return res.getRes(reportService.getList(userId, new JSONObject(URLDecoder.decode(parameter, "UTF-8"))));
    }

    @DeleteMapping("/{id}")
    public Object delete(final HttpServletRequest request, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        reportService.delete(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

//    @GetMapping("/chart/list/{id}")
//    public Object getChartDataList(final HttpServletRequest request, @PathVariable final String id) throws Exception {
//        int userId = (int)request.getAttribute("userId");
//        ResponseBase res = new ResponseBase();
//        return res.getRes(reportService.getChartDataList(userId, id));
//    }


    @GetMapping("/{analysisId}/{id}")
    public Object getDetail(final HttpServletRequest request, @PathVariable final String id, @PathVariable final String analysisId) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(reportService.getDetail(userId, id, analysisId));
    }

    @GetMapping("/{id}")
    public Object getDetailById(final HttpServletRequest request, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(reportService.getDetailById(userId, id));
    }

    @PutMapping("")
    public Object create(final HttpServletRequest request, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
//        int userId = 41;
//        String[] verifyKeys = {"analysisId", "name", "thumbImg"};
//        Util.checkParameter(verifyKeys, null, body);
        reportService.create(userId, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    // TODO: 뭘 바꿀 수 있는지?
    @PutMapping("/{id}")
    public Object update(final HttpServletRequest request, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"name", "drawInfo"};
        Util.checkParameter(verifyKeys, null, body);
        int userId = 41;
//        int userId = (int)request.getAttribute("userId");
        reportService.update(userId, id, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    // id : dashboardId
    @PostMapping("")
    public Object createReportChart(final HttpServletRequest request, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
//        int userId = 41;
//        String[] verifyKeys = {"drawInfo"};
//        Util.checkParameter(verifyKeys, null, body);
        reportService.createReportChart(userId, body, 0, 0);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }
}
