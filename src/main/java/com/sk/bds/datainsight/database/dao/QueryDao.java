package com.sk.bds.datainsight.database.dao;

import com.sk.bds.datainsight.database.model.*;
import com.sk.bds.datainsight.util.SqlMap;
import com.sk.bds.datainsight.util.Util;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryDao {
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private Connection conn;

    public QueryDao(SingleConnectionDataSource ds) throws Exception {
        conn = ds.getConnection();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(ds);
        jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
    }

    public void transactionStart() throws SQLException {
        TransactionSynchronizationManager.initSynchronization();
        conn.setAutoCommit(false);
    }

    public void transactionEnd() throws SQLException {
        TransactionSynchronizationManager.clearSynchronization();
        conn.commit();
    }

    public void rollBack() {
        try {
            conn.rollback();
            TransactionSynchronizationManager.clearSynchronization();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> selectDataSetTables(List<String> list) throws SQLException {
        return jdbcTemplate.queryForList(String.format(SqlMap.Query.SELECT_DATA_SET_TABLES, String.join(",", list)));
    }

    public String selectDataSetTable(String id) throws SQLException {
        return jdbcTemplate.queryForObject(String.format(SqlMap.Query.SELECT_DATA_SET_TABLE, id), String.class);
    }

    public List<Map<String, Object>> runQuery(String query) throws SQLException {
        return jdbcTemplate.queryForList(query);
    }

    public int insertQuery(Map<String, Object> param) throws SQLException {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("NAME", param.get("name"));
        paramMap.put("DESCRIPTION", param.get("description"));
        paramMap.put("QUERY_TEXT", param.get("query"));
        paramMap.put("DATA_SET_INFO", param.get("dataSet"));
        paramMap.put("VARIABLE_INFO", param.get("variable"));
        Date date = new Date();
        paramMap.put("UPDATE_DATE", date);
        paramMap.put("CREATE_DATE", date);
        MapSqlParameterSource sqlParam = new MapSqlParameterSource(paramMap);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("QUERY").usingGeneratedKeyColumns("ID");
        Number key = simpleJdbcInsert.executeAndReturnKey(sqlParam);
        return key.intValue();
    }

    public void updateQuery(Map<String, Object> param) throws SQLException {
        jdbcTemplate.update(SqlMap.Query.UPDATE_QUERY,
                new Object[]{param.get("name"), param.get("description"), param.get("query"), param.get("dataSet"), param.get("variable"), param.get("id")});
    }

    public void deleteQuery(int id) throws SQLException {
        jdbcTemplate.update(SqlMap.Query.DELETE_QUERY, new Object[]{id});
        jdbcTemplate.execute(String.format(SqlMap.Query.DELETE_QUERY_GROUP_MAPPING, id));

    }

    public List<Query> getQueryList(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.QUERY_LIST);
        return jdbcTemplate.query(String.format(SqlMap.Query.SELECT_QUERY, listParam.get("where"), listParam.get("orderBy"), listParam.get("limit")), new Query());
    }

    public int getQueryListCount(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.QUERY_LIST);
        return jdbcTemplate.queryForObject(String.format(SqlMap.Query.SELECT_QUERY_COUNT, listParam.get("where")), Integer.class);
    }

    public void insertQueryGroupMapping(int id, int groupId) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.INSERT_ANALYSIS_GROUP_MAPPING, new Object[]{id, groupId, 3});
    }

    public Query getQueryFromId(String id) throws SQLException {
        return jdbcTemplate.queryForObject(String.format(SqlMap.Query.SELECT_QUERY_ID, id), new Query());
    }

}
