package com.sk.bds.datainsight.controller;

import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.service.SettingService;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SettingController {

    private static final Logger log = LoggerFactory.getLogger(SettingController.class);

    @Autowired
    SettingService service;

    @RequestMapping(value = "/setting/key", method = RequestMethod.GET)
    public Object getAccessKey(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        HashMap<String, Object> param = new HashMap<>();
        for (String key : request.getParameterMap().keySet()) {
            param.put(key, request.getParameter(key));
        }
        return res.getRes(service.getAccessKey(userId, param));
    }

    @RequestMapping(value = "/setting/key", method = RequestMethod.POST)
    public Object createAccessKey(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"description"};
        Util.checkParameter(verifyKeys, null, body);
        service.createAccessKey(userId, body);
        ResponseBase res = new ResponseBase();
        return res.getRes();
    }

    @RequestMapping(value = "/setting/key/{id}/desc", method = RequestMethod.PUT)
    public Object updateAccessKeyDesc(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        body.put("id", id);
        String[] verifyKeys = {"description"};
        Util.checkParameter(verifyKeys, null, body);
        service.updateAccessKeyDesc(body);
        ResponseBase res = new ResponseBase();
        return res.getRes();
    }

    @RequestMapping(value = "/setting/key/status", method = RequestMethod.PUT)
    public Object updateAccessKeyStatus(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        String[] verifyKeys = {"status", "id"};
        Util.checkParameter(verifyKeys, null, body);
        service.updateAccessKeyStatus(body);
        ResponseBase res = new ResponseBase();
        return res.getRes();
    }

    @RequestMapping(value = "/setting/key/{id}", method = RequestMethod.DELETE)
    public Object deleteAccessKey(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        service.deleteAccessKey(id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/setting/key/{id}", method = RequestMethod.GET)
    public Object getUseAccessKey(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getUseAccessKey(userId, id));
    }
    @RequestMapping(value = "/setting/group", method = RequestMethod.PUT)
    public Object createAnalysisGroup(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"name", "description"};
        Util.checkParameter(verifyKeys, null, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(service.createGroup(userId, body));
    }

    @RequestMapping(value = "/setting/group/{id}", method = RequestMethod.POST)
    public Object updateAnalysisGroup(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String[] verifyKeys = {"name", "description"};
        Util.checkParameter(verifyKeys, null, body);
        service.updateGroup(userId, Integer.parseInt(id), body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/setting/group", method = RequestMethod.GET)
    public Object getAnalysisGroup(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        int userId = (int)request.getAttribute("userId");
        String parameter = request.getParameter("param");

        if (parameter == null) {
            throw new BadException("param is null");
        }
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getGroup(userId, new JSONObject(parameter)));
    }

    @RequestMapping(value = "/setting/group/{id}", method = RequestMethod.DELETE)
    public Object getAnalysisGroup(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.deleteGroup(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }
}
