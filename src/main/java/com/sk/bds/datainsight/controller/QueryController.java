package com.sk.bds.datainsight.controller;

import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.service.DataService;
import com.sk.bds.datainsight.service.QueryService;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@RestController
public class QueryController {

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    @Autowired
    QueryService service;

    @RequestMapping(value = "/query/run", method = RequestMethod.POST)
    public Object runQuery(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"query"};
        Util.checkParameter(verifyKeys, null, body);
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.runQuery(userId, body));
    }

    @RequestMapping(value = "/query/sample/{id}", method = RequestMethod.GET)
    public Object sampleQuery(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.sampleQuery(userId, id));
    }

    @RequestMapping(value = "/query", method = RequestMethod.PUT)
    public Object insertQuery(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"name", "query", "groupId"};
        Util.checkParameter(verifyKeys, null, body);
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.insertQuery(userId, body));
    }

    @RequestMapping(value = "/query/{id}", method = RequestMethod.POST)
    public Object updateQuery(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final int id) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"name", "query", "groupId"};
        Util.checkParameter(verifyKeys, null, body);
        int userId = (int)request.getAttribute("userId");
        service.updateQuery(userId, id, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/query/{id}", method = RequestMethod.DELETE)
    public Object deleteQuery(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final int id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.deleteQuery(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public Object getQuery(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        int userId = (int)request.getAttribute("userId");
        String parameter = request.getParameter("param");
        if (parameter == null) {
            throw new BadException("param is null");
        }
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getQuery(userId, new JSONObject(URLDecoder.decode(parameter, "UTF-8"))));
    }

    @RequestMapping(value = "/query/{id}", method = RequestMethod.GET)
    public Object getQueryFromId(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getQueryFromId(userId, id));
    }
}
