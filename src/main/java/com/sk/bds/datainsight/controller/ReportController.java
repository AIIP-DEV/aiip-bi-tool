package com.sk.bds.datainsight.controller;

import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.service.ReportService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

@RestController
@RequestMapping(value = "/report", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ReportController {

    @Autowired
    ReportService reportService;

    @GetMapping("")
    public Object getReportList(final HttpServletRequest request) throws Exception {
        int userId = (int)request.getAttribute("userId");
        String parameter = request.getParameter("param");
        if (parameter == null) {
            throw new BadException("param is null");
        }
        ResponseBase res = new ResponseBase();
        return res.getRes(reportService.getList(userId, new JSONObject(URLDecoder.decode(parameter, "UTF-8"))));
    }

    @DeleteMapping("/{id}")
    public Object deleteReport(final HttpServletRequest request, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        reportService.deleteDashboard(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @GetMapping("/chart/list/{id}")
    public Object getDashboardChartDataList(final HttpServletRequest request, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(reportService.getChartDataList(userId, id));
    }

    @GetMapping("/{analysisId}/{id}")
    public Object getDashboardDetail(final HttpServletRequest request, @PathVariable final String analysisId, @PathVariable final String id) throws Exception {
//        int userId = 41;
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(reportService.getDetail(userId, id, analysisId));
    }

    /* To do List 07/21
    1. update report chart
    2. del report chart
    */

}
