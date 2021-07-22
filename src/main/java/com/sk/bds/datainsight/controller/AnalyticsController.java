package com.sk.bds.datainsight.controller;

import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.service.AnalyticsService;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value = "/analytics", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Autowired
    AnalyticsService service;

    // 분석작업 생성
    @RequestMapping(value = "/file", method = RequestMethod.POST)
    public Object createAnalytics(final HttpServletRequest request, @RequestBody Map<String, Object> dataSetInfo) throws Exception {
        JSONObject json = new JSONObject(dataSetInfo);
        int userId = (int)request.getAttribute("userId");
        String path = json.getString("path");
        String delimiter = json.getString("delimiter");
        boolean hasHeader = json.getBoolean("hasHeader");
        int groupId = json.getInt("groupId");
        String uuId = json.getString("UUID");
        ResponseBase res = new ResponseBase();


        log.info("hasHeader = " + hasHeader);
        log.info("groupId = " + groupId);

        return res.getRes(service.uploadFile(userId, groupId, path, hasHeader, uuId, delimiter));
    }

    // 차트 생성
    @PutMapping(value = "/{id}/chart")
    public Object addAnalysisChart(final HttpServletRequest request, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        int userId = (int) request.getAttribute("userId");
//        String[] verifyKeys = {"valueInfo", "option"};
//        Util.checkParameter(verifyKeys, null, body);
        ResponseBase res = new ResponseBase();
        Object result = service.addAnalysisChart(userId, Integer.parseInt(id), body);
        return res.getRes(result);
    }

    @RequestMapping(value = "/{id}/chart/{chartId}", method = RequestMethod.PUT)
    public Object updateAnalyticsChart(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id, @PathVariable final String chartId) throws Exception {
//        log.info("body: {}", body);
        log.info("updateAnalysisChart : " + request.getAttribute("ssoId") + "  analysisId : " + id + " chartId :" + chartId);
        int userId = (int)request.getAttribute("userId");
//        String[] verifyKeys = {"chart", "valueInfo", "drawInfo"};
//        Util.checkParameter(verifyKeys, null, body);
        service.updateAnalysisChart(userId, Integer.parseInt(id), Integer.parseInt(chartId), body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    // 차트 생성을 위한 정보
    @PostMapping(value = "/chart/data/{id}")
    public Object getAnalysisChartData(final HttpServletRequest request, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        int userId = (int) request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getAnalysisChartData(userId, id, body));
    }

    // uuid로 분석작업 세부사항 찾기
    @RequestMapping(value = "/{uuId}", method = RequestMethod.GET)
    public Object getAnalyticsDetail(final HttpServletRequest request, @PathVariable final String uuId) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getAnalyticsDetail(userId, uuId, false));
    }

    // 분석 작업에 있는 차트 리스트
    @RequestMapping(value = "/chart/list/{uuId}", method = RequestMethod.GET)
    public Object getAnalyticsChartDataList(final HttpServletRequest request, @PathVariable final String uuId) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getAnalyticsChartDataList(userId, uuId));
    }

    // uuid로 analysis가 있는지
    @RequestMapping(value = "/count/{uuId}", method = RequestMethod.GET)
    public Object countUuId(final HttpServletRequest request, @PathVariable final String uuId) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.countUuId(userId, uuId));
    }

    // 분석 작업 리스트
    @RequestMapping(method = RequestMethod.GET)
    public Object getAnalysisList(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        String parameter = request.getParameter("param");
        if (parameter == null) {
            throw new BadException("param is null");
        }
        return res.getRes(service.getAnalyticsList(userId, new JSONObject(URLDecoder.decode(parameter, "UTF-8"))));
    }

    // 분석 작업 삭제
    @RequestMapping(value = "/{uuId}", method = RequestMethod.DELETE)
    public Object delAnalytics(final HttpServletRequest request, @PathVariable final String uuId) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.delAnalytics(userId, uuId);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    // 정적 HTML 내보내기
    @PostMapping(value = "/export")
    public Object exportAnalysisChart(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("exportAnalysisChart : " + request.getAttribute("ssoId") + "  body : " + body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"id"};
        String[] intParserKeys = {"id"};

        Util.checkParameter(verifyKeys, intParserKeys, body);
        String result = service.exportChart(userId, body);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        OutputStream os = response.getOutputStream();
        os.write(result.getBytes());
        os.flush();
        os.close();
        return null;
    }
    // csv download
    @RequestMapping(value = "/download/csv/{chartId}/{uuId}", method = RequestMethod.GET)
    public void downloadContainerCsv(final HttpServletRequest request, final HttpServletResponse response, @PathVariable int chartId, @PathVariable String uuId) throws Exception {
        int userId = (int)request.getAttribute("userId");
        response.setContentType("text/csv; charset=MS949");
        response.setHeader("Content-Disposition", "attachment; filename=container.csv");
        response.setContentType("application/csv");
        service.chartDownloadToCsv(response, userId, chartId, uuId);
    }
}
