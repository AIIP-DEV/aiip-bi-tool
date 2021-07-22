package com.sk.bds.datainsight.service;

import com.sk.bds.datainsight.database.dao.QueryDao;
import com.sk.bds.datainsight.database.dao.UserDao;
import com.sk.bds.datainsight.database.model.User;
import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.exception.InternalException;
import com.sk.bds.datainsight.util.DataSourceManager;
import com.sk.bds.datainsight.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class QueryService {
    @Autowired
    UserDao userDao;

    public Object runQuery(int userId, Map<String, Object> param) throws Exception {
        QueryDao dao = getDao(userId);
        try {
            String queryText = param.get("query").toString();
            List<Map<String, Object>> tableList = dao.selectDataSetTables(Util.getDataSetList(queryText));
            List<Map<String, Object>> variableList = (List<Map<String, Object>>)param.get("variable");
            queryText = Util.getQueryText(queryText, tableList, variableList, null);
            log.info("runQuery: {}", queryText);
            List<Map<String, Object>> list = dao.runQuery(queryText);
            Map<String, Object> result = new HashMap<>();
            result.put("column", Util.getColumnInfo(list));
            result.put("list", list);
            return result;
        } finally {
            dao.close();
        }
    }

    public Object sampleQuery(int userId, String dataSetId) throws Exception {
        QueryDao dao = getDao(userId);
        try {
            String queryText = String.format("SELECT * FROM %s LIMIT 100", dao.selectDataSetTable(dataSetId));
            log.info("sampleQuery: {}", queryText);
            List<Map<String, Object>> list = dao.runQuery(queryText);
            Map<String, Object> result = new HashMap<>();
            result.put("column", Util.getColumnInfo(list));
            result.put("list", list);
            return result;
        } finally {
            dao.close();
        }
    }

    public Object insertQuery(int userId, Map<String, Object> param) throws Exception {
        QueryDao dao = getDao(userId);
        Map<String, Object> result = new HashMap<>();
        try {
            param = getQueryParam(param);
            int id = dao.insertQuery(param);
            result.put("id", id);
            dao.insertQueryGroupMapping(id, Integer.parseInt(String.valueOf(param.get("groupId"))));
        } finally {
            dao.close();
        }
        return result;
    }

    public void updateQuery(int userId, int id, Map<String, Object> param) throws Exception {
        QueryDao dao = getDao(userId);
        try {
            param.put("id", id);
            param = getQueryParam(param);
            dao.updateQuery(param);
            dao.insertQueryGroupMapping(id, Integer.parseInt(String.valueOf(param.get("groupId"))));
        } finally {
            dao.close();
        }
    }

    public void deleteQuery(int userId, int id) throws Exception {
        QueryDao dao = getDao(userId);
        try {
            if (dao != null) {
                dao.transactionStart();
                dao.deleteQuery(id);
                dao.transactionEnd();
            } else {
                BadException e = new BadException(String.format("쿼리(ID:%s) 삭제에 실패 하였습니다.\n쿼리를 찾을 수 없습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", id));
                throw e;
            }
        } catch (BadException be) {
            log.warn("deleteQuery error: {}", String.format("Not found query: %s", id));
            throw be;
        } catch (Exception e) {
            log.error("deleteQuery error: {}", e.getMessage());
            dao.rollBack();
            InternalException ie = new InternalException(e);
            ie.setMessage(String.format("쿼리(ID:%s) 삭제에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.", id));
            throw ie;
        } finally {
            dao.close();
        }
    }

    public Object getQuery(int userId, JSONObject param) throws Exception {
        QueryDao dao = getDao(userId);
        try {
            Object list = dao.getQueryList(param);
            int count = dao.getQueryListCount(param);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", count);
            return result;
        } finally {
            dao.close();
        }
    }

    public Object getQueryFromId(int userId, String id) throws Exception {
        QueryDao dao = getDao(userId);
        try {
            return dao.getQueryFromId(id);
        } finally {
            dao.close();
        }
    }

    private Map<String, Object> getQueryParam(Map<String, Object> param) {
        String queryText = param.get("query").toString();
        List<String> dataSetList = Util.getDataSetList(queryText);
        if (dataSetList.size() > 0) {
            param.put("dataSet", String.join(",", dataSetList));
        }
        if (param.get("variable") != null) {
            param.put("variable", new JSONArray((List)param.get("variable")).toString());
        }
        return param;
    }

    private QueryDao getDao(int userId) throws Exception {
        User user = userDao.getUserById(userId);
        return new QueryDao(DataSourceManager.getDataSource(user.getDbUrl(), user.getDbPort(), user.getDbName(),
                user.getDbId(), user.getDbPwd()));
    }
}
