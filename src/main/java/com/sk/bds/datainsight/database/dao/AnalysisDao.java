package com.sk.bds.datainsight.database.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sk.bds.datainsight.database.model.*;
import com.sk.bds.datainsight.util.SqlMap;
import com.sk.bds.datainsight.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AnalysisDao {
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private Connection conn;
    private User user;


    public AnalysisDao(SingleConnectionDataSource ds, User user) throws Exception {
        this.user = user;
        conn = ds.getConnection();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(ds);
        jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
    }

    public AnalysisDao(SingleConnectionDataSource ds) throws Exception {
        conn = ds.getConnection();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(ds);
        jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
    }

    public User getUser() {
        return user;
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

    public List<AnalysisWithChart> getAnalysisList(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.ANALYSIS_LIST);
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS, listParam.get("where"), listParam.get("orderBy"), listParam.get("limit")), new AnalysisWithChart());
    }

    public List<AnalysisChart> getAnalysisChartList(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.ANALYSIS_LIST);
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_CHART_BY_ID, listParam.get("where"), listParam.get("orderBy"), listParam.get("limit")), new AnalysisChart());
    }

    public int getAnalysisListCount(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.ANALYSIS_LIST);
        return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_ANALYSIS_COUNT, listParam.get("where")), Integer.class);
    }

    public Analysis getAnalysisId(String analysisId) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_ANALYSIS_ID, analysisId), new Analysis());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Integer getAnalysisIdByUuId(String uuId) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_ANALYSIS_ID_BY_UUID, uuId), Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public Integer countAnalysisIdByUuId(String uuId) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_COUNT_ANALYSIS_ID_BY_UUID, uuId), Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public Map<String, Object> getAnalysisSrcInfo(String analysisId) throws SQLException {
        return jdbcTemplate.queryForMap(String.format(SqlMap.Analysis.SELECT_ANALYSIS_SRC_INFO, analysisId));
    }

    public void updateAnalysis(Analysis analysis) throws SQLException {
        String variableInfo = null;
        if (analysis.getVariableInfo() != null) {
            JSONArray array = new JSONArray();
            try {
                Util.setJsonFromList(array, analysis.getVariableInfo());
                variableInfo = array.toString();
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }
        jdbcTemplate.update(SqlMap.Analysis.UPDATE_ANALYSIS,
                new Object[]{analysis.getName(), analysis.getSubName(), analysis.getImage(), variableInfo, analysis.getId()});
    }

    public void updateAnalytics(int analysisId, String useColumns, String originalColumn, String changeColumn) throws SQLException {
        HashMap<String, Object> param = new HashMap<>();
        param.put("ANALYSIS_ID", analysisId);
        param.put("USE_COLUMNS", useColumns);
        param.put("ORIGINAL_COLUMN", originalColumn);
        param.put("CHANGE_COLUMN", changeColumn);

        namedParameterJdbcTemplate.update(SqlMap.Analysis.UPDATE_ANALYSIS_COLUMN_INFO, new MapSqlParameterSource(param));
    }

    public Integer getGroupCount(String groupId) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_GROUP_BY_NAME, groupId), Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public Integer getGroupId(String groupId) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_GROUP_BY_ID, groupId), Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public void createGroup(String groupId) throws SQLException {
        HashMap<String, Object> param = new HashMap<>();
        param.put("NAME", groupId);
        param.put("DESCRIPTION", groupId);

        namedParameterJdbcTemplate.update(SqlMap.Analysis.INSERT_GROUP_TABLE, new MapSqlParameterSource(param));
    }

    public void delAnalysis(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Analysis.DELETE_ANALYSIS, id));
        jdbcTemplate.execute(String.format(SqlMap.Analysis.DELETE_ANALYSIS_GROUP_MAPPING, id));
    }

    public void delAnalysisFilter(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Analysis.DELETE_ANALYSIS_FILTER, id));
    }

    public void delAnalysisFilterGroup(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Analysis.DELETE_ANALYSIS_FILTER_GROUP, id));
    }

    public void delAnalysisChart(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Analysis.DELETE_ANALYSIS_CHART, id));
    }

    public List<ChartInfo> getChartInfo() throws SQLException {
        return jdbcTemplate.query(SqlMap.Analysis.SELECT_CHART_INFO, new ChartInfo());
    }

    public int insertAnalysis(Integer dataSetId, Integer queryId, String variableInfo, String name, String createUser, String dataTable, String subName) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        Map<String, Object> param = new HashMap<>();
        param.put("DATA_SET_ID", dataSetId);
        param.put("QUERY_ID", queryId);
        param.put("VARIABLE_INFO", variableInfo);
        if (name != null) {
            param.put("NAME", name);
            simpleJdbcInsert.withTableName("ANALYSIS")
                    .usingGeneratedKeyColumns("ID")
                    .usingColumns("DATA_SET_ID", "NAME", "DATA_TABLE", "CREATE_USER", "QUERY_ID", "VARIABLE_INFO", "SUB_NAME");
        } else {
            simpleJdbcInsert = simpleJdbcInsert.withTableName("ANALYSIS")
                    .usingGeneratedKeyColumns("ID")
                    .usingColumns("DATA_SET_ID", "DATA_TABLE", "CREATE_USER", "QUERY_ID", "VARIABLE_INFO", "SUB_NAME");
        }
        param.put("DATA_TABLE", dataTable);
        param.put("CREATE_USER", createUser);
        param.put("SUB_NAME", subName);
        Number key = simpleJdbcInsert.executeAndReturnKey(param);
        return key.intValue();
    }

    public int insertAnalytics(String name, String subName, String createUser, String dataTable) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        Map<String, Object> param = new HashMap<>();
        if (name != null) {
            param.put("NAME", name);
            simpleJdbcInsert.withTableName("ANALYSIS")
                    .usingGeneratedKeyColumns("ID")
                    .usingColumns("NAME", "DATA_TABLE", "CREATE_USER", "SUB_NAME", "STATUS");
        } else {
            simpleJdbcInsert = simpleJdbcInsert.withTableName("ANALYSIS")
                    .usingGeneratedKeyColumns("ID")
                    .usingColumns("DATA_TABLE", "CREATE_USER", "SUB_NAME", "STATUS");
        }
        param.put("DATA_TABLE", dataTable);
        param.put("CREATE_USER", createUser);
        param.put("SUB_NAME", subName);
        param.put("STATUS", 0);
        Number key = simpleJdbcInsert.executeAndReturnKey(param);
        return key.intValue();
    }

    public void createAnalysisTable(String tableName, String column) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Analysis.CREATE_ANALYSIS_TABLE, tableName, column));
    }

    public void insertAnalysisFilter(int groupId, String column, String condition, Object value1, Object value2, String enable) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.INSERT_ANALYSIS_FILTER, new Object[]{groupId, column, condition, value1, value2, enable});
    }

    public int insertAnalysisFilterGroup(int analysisId, String name, String enable) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert = simpleJdbcInsert.withTableName("ANALYSIS_FILTER_GROUP")
                .usingGeneratedKeyColumns("ID")
                .usingColumns("ANALYSIS_ID", "NAME", "ENABLE");
        Map<String, Object> param = new HashMap<>();
        param.put("ANALYSIS_ID", analysisId);
        param.put("NAME", name);
        param.put("ENABLE", enable);
        Number key = simpleJdbcInsert.executeAndReturnKey(param);
        return key.intValue();

    }

    public void insertAnalysisTable(String tableName, String insertColumns, String selectColumns, List<Map<String, Object>> values) throws SQLException {
        String query = String.format(SqlMap.Analysis.INSERT_ANALYSIS_TABLE, tableName, insertColumns, selectColumns);
        Map<String, Object>[] param = values.toArray(new Map[values.size()]);
        namedParameterJdbcTemplate.batchUpdate(query, param);
    }


    public void updateAnalysisFilter(String column, String condition, Object value1, Object value2, String enable, int filterId) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.UPDATE_ANALYSIS_FILTER, new Object[]{column, condition, value1, value2, enable, filterId});
    }

    public void updateAnalysisFilterGroup(String name, String enable, int id) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.UPDATE_ANALYSIS_FILTER_GROUP, new Object[]{name, enable, id});
    }

    public List<Filter> getAnalysisFilter(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_FILTER, id), new Filter());
    }

    public List<Filter> getAnalyticsFilter(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYTICS_FILTER, id), new Filter());
    }

    public List<AnalysisFilterGroup> getAnalysisFilterGroup(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_FILTER_GROUP, id), new AnalysisFilterGroup());
    }

    public Number insertAnalysisChart(AnalysisChart item) throws SQLException, JsonProcessingException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(SqlMap.Analysis.INSERT_ANALYSIS_CHART, item.getParameter(), keyHolder);
        return keyHolder.getKey();
    }

    public void updateAnalysisChart(AnalysisChart item) throws SQLException, JsonProcessingException {
        namedParameterJdbcTemplate.update(SqlMap.Analysis.UPDATE_ANALYSIS_CHART, item.getParameter());
    }

    public List<AnalysisChart> getAnalysisChart(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_CHART, id), new AnalysisChart());
    }

    public AnalysisChart getAnalysisChartId(String id) throws SQLException {
        return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_ANALYSIS_CHART_ID, id), new AnalysisChart());
    }

    public DataSet getDataSet(String id) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.DataSet.SELECT_DATA_SET_ID, id), new DataSet());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Map<String, Object>> getColumnInfo(String tableName) throws SQLException {
        return jdbcTemplate.queryForList(String.format("DESC %s", tableName));
    }

    public List<Filter> getDataSetFilter(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.DataSet.SELECT_DATA_SET_FILTER, id), new Filter());
    }

    public List<DataSetFilterGroup> getDataSetFilterGroup(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.DataSet.SELECT_DATA_SET_FILTER_GROUP, id), new DataSetFilterGroup());
    }

    public List<Map<String, Object>> getDataSetTable(String query) throws SQLException {
        log.info("analysis getDataSetTable: {}", query);
        return jdbcTemplate.queryForList(query);
    }

    public int getDataSetTableCount(String query) throws SQLException {
        return jdbcTemplate.queryForObject(query, Integer.class);
    }

    public void dropDataSetTable(String tableName) {
        try {
            jdbcTemplate.execute(String.format(SqlMap.Dashboard.DROP_TABLE, tableName));
        } catch (DataAccessException e) {
        }
    }

    public void createAnalysisDataSetTable(String srcTableName, String desTableName, String insertColumnStr, String where) throws SQLException {
        String query = null;
        if (where != null) {
            query = String.format("CREATE TABLE %s IGNORE AS SELECT %s FROM %s WHERE %s", desTableName, insertColumnStr, srcTableName, where);
        } else {
            query = String.format("CREATE TABLE %s IGNORE AS SELECT %s FROM %s", desTableName, insertColumnStr, srcTableName);
        }
        jdbcTemplate.execute(query);
    }

    public int insertDashboard(Dashboard dashboard) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("DASHBOARD").usingGeneratedKeyColumns("ID");
        Number key = simpleJdbcInsert.executeAndReturnKey(dashboard.getParameter());
        return key.intValue();
    }

    public int insertDashboardDataSet(DashboardDataSet dashboardDataSet) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("DASHBOARD_DATA_SET").usingGeneratedKeyColumns("ID");
        Number key = simpleJdbcInsert.executeAndReturnKey(dashboardDataSet.getParameter());
        return key.intValue();
    }

    public void insertDashboardChart(DashboardChart item) throws SQLException {
        namedParameterJdbcTemplate.update(SqlMap.Dashboard.INSERT_DASHBOARD_CHART, item.getParameter());
    }

    public void createShareAnalysisDataSetTable(String srcDbName, String srcTableName, String desTableName, String insertColumnStr, String where) throws SQLException {
        String query = null;
        if (where != null) {
            query = String.format("CREATE TABLE %s IGNORE AS SELECT %s FROM %s.%s WHERE %s", desTableName, insertColumnStr, srcDbName, srcTableName, where);
        } else {
            query = String.format("CREATE TABLE %s IGNORE AS SELECT %s FROM %s.%s", desTableName, insertColumnStr, srcDbName, srcTableName);
        }
        jdbcTemplate.execute(query);
    }

    public List<Map<String, Object>> selectChartTitle(String ids) throws SQLException {
        return jdbcTemplate.queryForList(String.format(SqlMap.Analysis.SELECT_ANALYSIS_CHART_TITLE, ids));
    }

    public List<Map<String, Object>> selectAddColumn(int id) throws SQLException {
        return jdbcTemplate.queryForList(String.format(SqlMap.Analysis.SELECT_DATA_SET_ADD_COLUMN, id));
    }

    public void insertAnalysisColumnInfo(int id, DataSet dataSet, String addColumn) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.INSERT_ANALYSIS_COLUMN_INFO,
                new Object[]{id, dataSet.getUseColumns(), dataSet.getOriginalColumn(), dataSet.getChangeColumn(), addColumn});
    }

    public List<Group> selectAnalysisGroupList(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.GROUP_LIST);
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_GROUP, listParam.get("where"), listParam.get("orderBy"), listParam.get("limit")), new Group());
    }

    public int selectAnalysisGroupListCount(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.GROUP_LIST);
        return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_ANALYSIS_GROUP_COUNT, listParam.get("where")), Integer.class);
    }

    public void updateAnalysisGroup(int id, String name, String description) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.UPDATE_ANALYSIS_GROUP, new Object[]{name, description, id});
    }

    public int insertAnalysisGroup(Group item) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("`GROUP`").usingGeneratedKeyColumns("ID")
                .usingColumns("NAME", "DESCRIPTION");
        Number key = simpleJdbcInsert.executeAndReturnKey(item.getParameter());
        return key.intValue();
    }

    public void deleteAnalysisGroup(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Analysis.DELETE_ANALYSIS_GROUP, id));
    }

    public void insertAnalysisGroupMapping(int id, int groupId) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.INSERT_ANALYSIS_GROUP_MAPPING, new Object[]{id, groupId, 0});
    }

    public List<Map<String, Object>> selectDataSetTables(List<String> list) throws SQLException {
        return jdbcTemplate.queryForList(String.format(SqlMap.Query.SELECT_DATA_SET_TABLES, String.join(",", list)));
    }

    public String getQueryTextFromAnalysisId(String id) throws SQLException {
        return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_ANALYSIS_QUERY_TEXT, id), String.class);
    }

    public List<Map<String, Object>> getColumnGroupBy(String column, String tableName) throws SQLException {
        return jdbcTemplate.queryForList(String.format(SqlMap.Analysis.SELECT_COLUMN_GROUP_BY, column, column, tableName, column));
    }

    public String getDataTable(String user, String tableName, String id) throws Exception {
        return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_DATA_SET, user, tableName, id), String.class);
    }

}
