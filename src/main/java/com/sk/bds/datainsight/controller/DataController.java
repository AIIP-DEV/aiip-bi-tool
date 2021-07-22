package com.sk.bds.datainsight.controller;

import com.google.gson.JsonArray;
import com.sk.bds.datainsight.database.dao.SettingDao;
import com.sk.bds.datainsight.database.model.DataSet;
import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.service.DataService;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DataController {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Autowired
    DataService service;

    @Autowired
    SettingDao settingDao;

    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public Object getDataSetList(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        String parameter = request.getParameter("param");
        if (parameter == null) {
            throw new BadException("param is null");
        }
        return res.getRes(service.getDataSetList(userId, new JSONObject(URLDecoder.decode(parameter, "UTF-8"))));
    }

    @RequestMapping(value = "/data/{id}", method = RequestMethod.GET)
    public Object getDataSetDetail(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        return res.getRes(service.getDataSetDetail(userId, id, true));
    }

    @RequestMapping(value = "/data/{id}", method = RequestMethod.POST)
    public Object getAddColumnData(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        return res.getRes(service.getAddColumnData(userId, id, body));
    }

    @RequestMapping(value = "/data/{id}", method = RequestMethod.DELETE)
    public Object delDataSet(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.delDataSet(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/data/meta/connect", method = RequestMethod.POST)
    public Object dbConnect(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"type", "host", "port", "dbName", "id", "pwd", "ssl"};
        String[] intParserKeys = {"type"};
        Util.checkParameter(verifyKeys, intParserKeys, body);
        ResponseBase res = new ResponseBase();
        res.put("result", service.connectTest(Integer.parseInt(body.get("type").toString()), body));
        return res.getRes();
    }

    @RequestMapping(value = "/data/meta/tables", method = RequestMethod.POST)
    public Object dbTables(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"type", "host", "port", "dbName", "id", "pwd", "ssl"};
        String[] intParserKeys = {"type"};
        Util.checkParameter(verifyKeys, intParserKeys, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getTableList(Integer.parseInt(body.get("type").toString()), body));
    }

    @RequestMapping(value = "/data/meta/connInfo/{id}", method = RequestMethod.GET)
    public Object dataConnInfo(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        return res.getRes(service.getDataSetConnInfo(userId,id));
    }

    @RequestMapping(value = "/data/meta/{tableName}/sample", method = RequestMethod.POST)
    public Object dbTableSample(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String tableName) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"type", "host", "port", "dbName", "id", "pwd", "ssl"};
        String[] intParserKeys = {"type"};
        Util.checkParameter(verifyKeys, intParserKeys, body);
        body.put("tableName", tableName);
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getSample(Integer.parseInt(body.get("type").toString()), body));
    }

    @RequestMapping(value = "/data", method = RequestMethod.PUT)
    public Object createDataSet(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"name", "srcConnection", "useColumns", "originalInfo", "changeInfo"};
        Util.checkParameter(verifyKeys, null, body);
        body.put("name", Util.getHangulText((String)body.get("name")));
        int userId = (int)request.getAttribute("userId");
        ResponseBase res = new ResponseBase();
        String auth = (String)request.getAttribute("Authorization");
        res.put("id", service.createDataSet(userId, body, auth));
        return res.getRes();
    }

    @RequestMapping(value = "/data/{id}", method = RequestMethod.PUT)
    public Object updateDataSet(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String id) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"name", "useColumns", "originalInfo", "changeInfo"};
        Util.checkParameter(verifyKeys, null, body);
        int userId = (int)request.getAttribute("userId");
        service.updateDataSet(userId, id, body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/data/file", method = RequestMethod.POST)
    public Object uploadFile(final HttpServletRequest request, @RequestParam("file") MultipartFile[] files, @RequestParam("dataSetInfo") JSONObject dataSetInfo) throws Exception {
        if(files == null || files.length <= 0) {
            throw new BadException("Not found file");
        } else if(!dataSetInfo.has("delimiter")) {
            throw new BadException("Delimiter parameter is empty");
        } else if(!dataSetInfo.has("hasHeader")) {
            throw new BadException("HasHeader parameter is empty");
        } else if (!dataSetInfo.has("groupId")) {
            throw new BadException("groupId parameter is empty");
        }
        log.info("files:{}, length:{}", files, files.length);
        int userId = (int)request.getAttribute("userId");
        String delimiter = dataSetInfo.getString("delimiter");
        boolean hasHeader = dataSetInfo.getBoolean("hasHeader");
        ResponseBase res = new ResponseBase();
        int groupId = dataSetInfo.getInt("groupId");

        log.info("delimiter = " + delimiter);
        log.info("hasHeader = " + hasHeader);
        log.info("groupId = " + groupId);

        return res.getRes(service.uploadFile(userId, groupId, files, delimiter, hasHeader));
    }

    @RequestMapping(value = "/data/icos", method = RequestMethod.GET)
    public Object getICOSList(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return service.getICOSList(request.getHeader("Authorization")).toString();
    }

    @RequestMapping(value = "/data/icos/{name}", method = RequestMethod.GET)
    public Object getICOSBucketList(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String name) throws Exception {
        return service.getICOSBucketList(request.getHeader("Authorization"), name).toString();
    }

    @RequestMapping(value = "/data/icos/{storage}/{bucket}", method = RequestMethod.GET)
    public Object getICOSObjectList(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String storage, @PathVariable final String bucket) throws Exception {
        String path = request.getParameter("path");
        if (path == null || "".equals(path.trim())) {
            throw new BadException("Invalid parameter (path)");
        }
        return service.getICOSObjectList(request.getHeader("Authorization"), storage, bucket, path).toString();
    }

    @RequestMapping(value = "/data/icos/{storage}/{bucket}", method = RequestMethod.POST)
    public Object uploadICOSObject(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String storage, @PathVariable final String bucket, @RequestBody final Map<String, Object> body) throws Exception {
        String path = request.getParameter("path");
        String folder = request.getParameter("folder");
        JSONObject params;
        String[] paths = null;
        try{
            params = new JSONObject(body);
        } catch(Exception e) {
            throw new BadException("Invalid parameter (path, hasHeader)");
        }

        if ((path == null || "".equals(path.trim())) && (folder == null || "".equals(folder.trim()))) {
            throw new BadException(String.format("Invalid parameter (path: %s, folder: %s)", path, folder));
        }
        if (path != null) {
            paths = path.split(",");
        }

        if(!params.has("delimiter") || !params.has("hasHeader") || !params.has("groupId")) {
            throw new BadException("Invalid parameter (path, hasHeader, groupId)");
        }

        String delimiter = params.getString("delimiter");
        boolean hasHeader = params.getBoolean("hasHeader");
        int userId = (int)request.getAttribute("userId");
        int groupId = params.getInt("groupId");

        log.info("delimiter = " + delimiter);
        log.info("hasHeader = " + hasHeader);
        log.info("groupId = " + groupId);

        Object result = service.uploadICOSObject(userId, groupId, request.getHeader("Authorization"), storage, bucket, paths, folder, delimiter, hasHeader);
        if (result instanceof HashMap) {
            return result;
        } else {
            return result.toString();
        }
    }

    @RequestMapping(value = "/data/schedule/{dataSetId}", method = RequestMethod.GET)
    public Object getDataSetScheduleLIst(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String dataSetId) throws Exception {
        ResponseBase res = new ResponseBase();
        int userId = (int)request.getAttribute("userId");
        return res.getRes(service.getDataSetScheduleLIst(userId,Integer.parseInt(dataSetId)));
    }

    @RequestMapping(value = "/data/schedule/{scheduleId}/detail/{type}", method = RequestMethod.GET)
    public Object getDataSetScheduleDetail(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String scheduleId, @PathVariable final String type) throws Exception {
        ResponseBase res = new ResponseBase();
        return res.getRes(service.getDataSetScheduleDetail(Integer.parseInt(scheduleId), type));
    }

    @RequestMapping(value = "/data/schedule/{dataSetId}", method = RequestMethod.POST)
    public Object createDataSetSchedule(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body, @PathVariable final String dataSetId) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String auth = (String)request.getAttribute("Authorization");
        String[] verifyKeys = {"startTime", "repeat"};
        Util.checkParameter(verifyKeys, null, body);
        body.put("userId", userId);
        body.put("dataSetId", Integer.parseInt(dataSetId));
        body.put("Authorization", auth);
        service.createDataSetSchedule(body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/data/schedule/{id}", method = RequestMethod.DELETE)
    public Object deleteDataSetSchedule(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        String auth = (String)request.getAttribute("Authorization");
        service.deleteDataSetSchedule(id, auth);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/data/schedule/onetime", method = RequestMethod.POST)
    public Object dataSetScheduleOnetime(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        int userId = (int)request.getAttribute("userId");
        String auth = (String)request.getAttribute("Authorization");
        String[] verifyKeys = {"dataSetId"};
        Util.checkParameter(verifyKeys, null, body);
        body.put("userId", userId);
        body.put("Authorization", auth);
        service.dataSetScheduleOnetime(body);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }
    @RequestMapping(value = "/data/schedule/onetime/{id}", method = RequestMethod.POST)
    public Object dataSetScheduleOnetimeFix(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final int id) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        param.put("isEnd", "Y");
        settingDao.updateScheduleOnetime(param);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/data/export/{id}", method = RequestMethod.GET)
    public Object exportData(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.exportData(userId, id, request.getHeader("User-Agent"), response);
        return null;
    }

    @RequestMapping(value = "/data/cancel/{id}", method = RequestMethod.DELETE)
    public Object cancelDataSet(final HttpServletRequest request, final HttpServletResponse response, @PathVariable final String id) throws Exception {
        int userId = (int)request.getAttribute("userId");
        service.cancelDataSet(userId, id);
        ResponseBase res = new ResponseBase();
        return res.getRes(null);
    }

    @RequestMapping(value = "/data/meta/database", method = RequestMethod.POST)
    public Object getDatabases(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final Map<String, Object> body) throws Exception {
        log.info("body: {}", body);
        String[] verifyKeys = {"type", "host", "port", "id", "pwd", "ssl"};
        String[] intParserKeys = {"type"};
        Util.checkParameter(verifyKeys, intParserKeys, body);
        ResponseBase res = new ResponseBase();
        res.put("list", service.getDatabases(Integer.parseInt(body.get("type").toString()), body));
        return res.getRes();
    }
}
