package com.sk.bds.datainsight.controller;

import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.service.AnalysisService;
import com.sk.bds.datainsight.util.AnonymousCallable;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    @Autowired
    AnalysisService service;

    @RequestMapping(value = "/analysis", method = RequestMethod.GET)
    public Object getAnalysisList(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        String parameter = request.getParameter("param");
        if (parameter == null) {
            throw new BadException("param is null");
        }
        return res.getRes(service.getAnalysisList(userId, new JSONObject(URLDecoder.decode(parameter, "UTF-8"))));
    }

    @RequestMapping(value = "/analysis/{id}", method = RequestMethod.DELETE)
    public Object delAnalysis(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.delAnalysis(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/{id}", method = RequestMethod.GET)
    public Object getAnalysisDetail(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getAnalysisDetail(userId, id, false));
    }

    @RequestMapping(value = "/analysis/data/{id}", method = RequestMethod.GET)
    public Object getAnalysisData(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getAnalysisDetail(userId, id, true));
    }

    @RequestMapping(value = "/analysis", method = RequestMethod.PUT)
    public Object createAnalysis(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.createAnalysis(userId, body));
    }

    @RequestMapping(value = "/analysis/{id}", method = RequestMethod.PUT)
    public Object updateAnalysis(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        service.updateAnalysis(userId, id, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/filter", method = RequestMethod.PUT)
    public Object addAnalysisFilter(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"column", "condition", "enable", "groupId"};
        String[] intParserKeys = {"groupId"};
        Util.checkParameter(verifyKeys, intParserKeys, body);
        service.addAnalysisFilter(userId, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/filter/{id}", method = RequestMethod.PUT)
    public Object updateAnalysisFilter(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"column", "condition", "enable"};
        Util.checkParameter(verifyKeys, null, body);
        service.updateAnalysisFilter(userId, Integer.parseInt(id), body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/filter/{id}", method = RequestMethod.DELETE)
    public Object delAnalysisFilter(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.delAnalysisFilter(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/filter/group", method = RequestMethod.PUT)
    public Object addAnalysisFilterGroup(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"analysisId", "name", "enable"};
        Util.checkParameter(verifyKeys, null, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(service.addAnalysisFilterGroup(userId, body));
    }

    @RequestMapping(value = "/analysis/filter/group/{id}", method = RequestMethod.PUT)
    public Object updateAnalysisFilterGroup(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"name", "enable"};
        Util.checkParameter(verifyKeys, null, body);
        service.updateAnalysisFilterGroup(userId, Integer.parseInt(id), body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/filter/group/{id}", method = RequestMethod.DELETE)
    public Object delAnalysisFilterGroup(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.delAnalysisFilterGroup(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/{id}/chart", method = RequestMethod.PUT)
    public Object addAnalysisChart(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"chart", "valueInfo", "drawInfo"};
        Util.checkParameter(verifyKeys, null, body);
        service.addAnalysisChart(userId, Integer.parseInt(id), body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/{id}/chart/{chartId}", method = RequestMethod.PUT)
    public Object updateAnalysisChart(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id, @PathVariable final String chartId) throws Exception {
        log.info("body: {}", body);
        log.info("updateAnalysisChart : " + request.getAttribute("ssoId") + "  analysisId : " + id + " chartId :" + chartId);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"chart", "valueInfo", "drawInfo"};
        Util.checkParameter(verifyKeys, null, body);
        service.updateAnalysisChart(userId, Integer.parseInt(id), Integer.parseInt(chartId), body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/{id}/chart/{chartId}", method = RequestMethod.DELETE)
    public Object delAnalysisChart(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id, @PathVariable final String chartId) throws Exception {
        log.info("delAnalysisChart : " + request.getAttribute("ssoId") + "  analysisId : " + id + " chartId :" + chartId + " user IP addr : " + request.getRemoteAddr());
        int userId = (int)request.getAttribute("userId");
        service.delAnalysisChart(userId, chartId);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    // 분석 작업 메타 정보
    // response = 차트 종류 + 차트 종류에 따른 옵션들
    @RequestMapping(value = "/analysis/meta", method = RequestMethod.GET)
    public Object getAnalysisMeta(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        return res.getRes(service.getAnalysisMeta(userId));
    }

    // 분석 작업 메타 정보
    // response = 차트 종류 + 차트 종류에 따른 옵션들
    @RequestMapping(value = "/v2/analysis/meta", method = RequestMethod.GET)
    public Object getAnalysisChartMeta(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        return res.getRes(service.getAnalysisChartMeta(userId));
    }

    @RequestMapping(value = "/analysis/users", method = RequestMethod.GET)
    public Object getAnalysisUsers(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        return res.getRes(service.getUsers(userId));
    }

    @RequestMapping(value = "/analysis/share", method = RequestMethod.PUT)
    public Object shareAnalysis(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"analysisId", "targetId"};
        String[] intParserKeys = {"targetId"};
        Util.checkParameter(verifyKeys, intParserKeys, body);
        service.shareAnalysis(userId, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/analysis/export", method = RequestMethod.POST)
    public Object exportAnalysisChart(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("exportAnalysisChart : " + request.getAttribute("ssoId") + "  body : " + body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"chartId"};
        String[] intParserKeys = {"chartId"};
        Util.checkParameter(verifyKeys, intParserKeys, body);
        String result = service.exportChart(userId, body);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        OutputStream os = response.getOutputStream();
        os.write(result.getBytes());
        os.flush();
        os.close();
        return null;
    }

    @AnonymousCallable
    @RequestMapping(value = "/analysis/export", method = RequestMethod.GET)
    public Object getExportChartData(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        Map<String, String> param = new HashMap<>();
        for (String key : request.getParameterMap().keySet()) {
            if (!"exportToken".equals(key)) {
                param.put(key, request.getParameter(key));
            }
        }
        if (param.size() == 0) {
            param = null;
        }
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getExportChartData(request.getParameter("exportToken"), param));
    }

    @AnonymousCallable
    @RequestMapping(value = "/analysis/export/csv", method = RequestMethod.GET)
    public Object getExportChartDataOnly(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        Map<String, String> param = new HashMap<>();
        for (String key : request.getParameterMap().keySet()) {
            if (!"exportToken".equals(key)) {
                param.put(key, request.getParameter(key));
            }
        }
        if (param.size() == 0) {
            param = null;
        }
        ResponseBase res = new ResponseBase();
        return res.getRes(service.exportData(request.getParameter("exportToken"), param, request.getHeader("User-Agent"), response));
    }

    @RequestMapping(value = "/analysis/export/{chartId}", method = RequestMethod.GET)
    public Object exportData(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String chartId) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.exportData(userId, chartId, request.getHeader("User-Agent"), response);
        return null;
    }

    @RequestMapping(value = "/analysis/chart/list/{id}", method = RequestMethod.GET)
    public Object getAnalysisChartDataList(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getAnalysisChartDataList(userId, id));
    }

    // 차트 drawInfo를 내려준다
    @RequestMapping(value = "/analysis/chart/data/{id}", method = RequestMethod.POST)
    public Object getAnalysisChartData(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        /* req body
        {
            "name": "test2",
            "drawInfo": {
            "x": 0,
            "y": 2,
            "w": 3,
            "h": 2,
            "orderBy": "name",
            "reverse": false
        },
            "chart": 18,
            "valueInfo": {
                "axis": "petal_width",
                "value": null,
                "line": null,
                "group": null,
                "flag": null
            }
        }*/

        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getAnalysisChartData(userId, id, body));
    }

    @RequestMapping(value = "/analysis/sample/{id}", method = RequestMethod.GET)
    public Object getSampleData(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getSampleData(userId, id));
    }
}
