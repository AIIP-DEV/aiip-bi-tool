package com.sk.bds.datainsight.service;

import com.sk.bds.datainsight.database.dao.AnalysisDao;
import com.sk.bds.datainsight.database.dao.SettingDao;
import com.sk.bds.datainsight.database.dao.UserDao;
import com.sk.bds.datainsight.database.model.AccessKey;
import com.sk.bds.datainsight.database.model.Group;
import com.sk.bds.datainsight.database.model.UseAccessKey;
import com.sk.bds.datainsight.database.model.User;
import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.exception.InternalException;
import com.sk.bds.datainsight.util.DataSourceManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SettingService {
    private static final Logger log = LoggerFactory.getLogger(SettingService.class);

    @Autowired
    UserDao userDao;

    @Autowired
    SettingDao settingDao;

    public HashMap<String, Object> getAccessKey(int userId, Map<String, Object> param) throws Exception {
        param.put("userId", userId);
        if (param.get("offset") == null) {
            param.put("offset", 0);
        } else {
            param.put("offset", Integer.parseInt((String)param.get("offset")));
        }
        if (param.get("limit") == null) {
            param.put("limit", 0);
        } else {
            param.put("limit", Integer.parseInt((String)param.get("limit")));
        }
        if (param.get("sort") != null) {
            String[] sort = param.get("sort").toString().split("[|]");
            switch (sort[0]) {
                case "id":
                    param.put("sort", String.format("A.ID %s", sort[1]));
                    break;
                case "description":
                    param.put("sort", String.format("A.DESCRIPTION %s", sort[1]));
                    break;
                case "createDate":
                    param.put("sort", String.format("A.CREATE_DATE %s", sort[1]));
                    break;
                case "useDate":
                    param.put("sort", String.format("C.LAST_USED_DATE %s", sort[1]));
                    break;
                case "status":
                    param.put("sort", String.format("A.STATUS %s", sort[1]));
                    break;
            }
        }
        List<AccessKey> list = settingDao.getAccessKey(param);
        int count = settingDao.getAccessKeyCount(param);
        HashMap<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", count);
        return result;
    }

    public void createAccessKey(int userId, Map<String, Object> param) throws Exception {
        param.put("userId", userId);
        try {
            AccessKey accessKey = new AccessKey(param);
            settingDao.insertAccessKey(accessKey);
        } catch (Exception e) {
            log.error("createAccessKey error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("액세스 키 생성에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        }
    }

    public void updateAccessKeyDesc(Map<String, Object> param) throws Exception {
        try {
            settingDao.updateAccessKeyDescription(param);
        } catch (Exception e) {
            log.error("updateAccessKeyDesc error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("액세스 키 수정에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        }
    }

    public void updateAccessKeyStatus(Map<String, Object> param) throws Exception {
        try {
            settingDao.updateAccessKeyStatus(param);
        } catch (Exception e) {
            log.error("updateAccessKeyStatus error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("액세스 키 상태 수정에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        }
    }

    public void deleteAccessKey(String id) throws Exception {
        try {
            String[] idArray = id.split(",");
            settingDao.deleteAccessKey(Arrays.asList(idArray));
        } catch (Exception e) {
            log.error("deleteAccessKey error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("액세스 키 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        }
    }

    public List<UseAccessKey> getUseAccessKey(int userId, String id) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            List<UseAccessKey> list = settingDao.getUseAccessKeyInfo(id);
            HashMap<Integer, List<UseAccessKey>> map = new HashMap<>();
            StringBuffer idBuffer = new StringBuffer();
            for (UseAccessKey useAccessKey : list) {
                int chartId = useAccessKey.getChartId();
                idBuffer.append(String.format("%d,", chartId));
                List<UseAccessKey> data = map.get(chartId);
                if (data == null) {
                    data = new ArrayList<>();
                    map.put(chartId, data);
                }
                data.add(useAccessKey);
            }
            List<Map<String, Object>> titleList = dao.selectChartTitle(idBuffer.substring(0, idBuffer.length() - 1));
            for (Map<String, Object> data : titleList) {
                List<UseAccessKey> useAccessKeyList = map.get(data.get("ID"));
                for (UseAccessKey useAccessKey : useAccessKeyList) {
                    useAccessKey.setTitle((String)data.get("TITLE"));
                }
            }
            return list;
        } finally {
            dao.close();
        }
    }

    public Object createGroup(int userId, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("id", dao.insertAnalysisGroup(new Group(param)));
            return result;
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BadException("동일한 이름이 존재 합니다.");
        } finally {
            dao.close();
        }
    }

    public void updateGroup(int userId, int id, Map<String, Object> param) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            dao.updateAnalysisGroup(id, String.valueOf(param.get("name")), String.valueOf(param.get("description")));
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BadException("동일한 이름이 존재 합니다.");
        } finally {
            dao.close();
        }
    }

    public Object getGroup(int userId, JSONObject param) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            Object list = dao.selectAnalysisGroupList(param);
            int count = dao.selectAnalysisGroupListCount(param);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", count);
            return result;
        } finally {
            dao.close();
        }
    }

    public void deleteGroup(int userId, String id) throws Exception {
        AnalysisDao dao = getDao(userId);
        try {
            dao.deleteAnalysisGroup(id);
        } finally {
            dao.close();
        }
    }

    private AnalysisDao getDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new AnalysisDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()));
    }
}
