package com.sk.bds.datainsight.database.dao;

import com.sk.bds.datainsight.database.model.*;
import com.sk.bds.datainsight.util.SqlMap;
import com.sk.bds.datainsight.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
public class DashboardDao {
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private Connection conn;
    private User user;

    public DashboardDao(SingleConnectionDataSource ds) throws Exception {
        conn = ds.getConnection();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(ds);
        jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
    }

    public DashboardDao(SingleConnectionDataSource ds, User user) throws Exception {
        this.user = user;
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

    public List<Dashboard> getDashboardList(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.DASHBOARD_LIST);
        return jdbcTemplate.query(String.format(SqlMap.Dashboard.SELECT_DASHBOARD, listParam.get("where"), listParam.get("orderBy"), listParam.get("limit")), new Dashboard());
    }

    public int getDashboardListCount(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.DASHBOARD_LIST);
        return jdbcTemplate.queryForObject(String.format(SqlMap.Dashboard.SELECT_DASHBOARD_COUNT, listParam.get("where")), Integer.class);
    }

    public Dashboard getDashboard(String id) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Dashboard.SELECT_DASHBOARD_ID, id), new Dashboard());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<DashboardChart> getDashboardChartList(String dashboardId) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Dashboard.SELECT_DASHBOARD_CHART, dashboardId), new DashboardChart());
    }

    public List<DashboardChart> getDashboardChartListByDataId(String dataId) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Dashboard.SELECT_DASHBOARD_CHART_DATA_ID, dataId), new DashboardChart());
    }

    public DashboardChart getDashboardChart(String id) throws SQLException {
        return jdbcTemplate.queryForObject(String.format(SqlMap.Dashboard.SELECT_DASHBOARD_CHART_ID, id), new DashboardChart());
    }

    public List<DashboardDataSet> getDashboardDataSetList(String dashboardId) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Dashboard.SELECT_DASHBOARD_DATA_SET, dashboardId), new DashboardDataSet());
    }

    public DashboardDataSet getDashboardDataSet(String id) throws SQLException {
        return jdbcTemplate.queryForObject(String.format(SqlMap.Dashboard.SELECT_DASHBOARD_DATA_SET_ID, id), new DashboardDataSet());
    }

    public void delDashboardDataSet(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Dashboard.DELETE_DASHBOARD_DATA_SET, id));
    }

    public void delDashboard(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Dashboard.DELETE_DASHBOARD, id));
        jdbcTemplate.execute(String.format(SqlMap.Dashboard.DELETE_DASHBOARD_GROUP_MAPPING, id));
    }

    public void updateDashboard(String id, Object name, Object subName) throws SQLException {
        String column = String.format("NAME = '%s'", name);
        if (subName != null) {
            column = String.format("%s, SUB_NAME = '%s'", subName);
        }
        jdbcTemplate.execute(String.format(SqlMap.Dashboard.UPDATE_DASHBOARD, column, id));
    }

    public void createInitTable() throws SQLException {
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_QUERY);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DATA_SET);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DATA_SET_FILTER_GROUP);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DATA_SET_FILTER);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DATA_SET_ADD_COLUMN);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_CHART_INFO);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_ECHART_INFO);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_CHART_AGGREGATE);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_CHART_FORMAT);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DASHBOARD);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DASHBOARD_DATA_SET);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DASHBOARD_CHART);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_ANALYSIS);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_ANALYSIS_CHART);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_ANALYSIS_FILTER_GROUP);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_ANALYSIS_FILTER);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_ANALYSIS_COLUMN_INFO);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DASHBOARD_FILTER_GROUP_INFO);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DASHBOARD_FILTER_INFO);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_DASHBOARD_COLUMN_INFO);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_ORIGINAL);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_GROUP);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_GROUP_MAPPING);
        // for report
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_REPORT);
        jdbcTemplate.execute(SqlMap.Dashboard.CREATE_REPORT_CHART);
    }

    public void insertInitData(String tableName, List<Map<String, Object>> data) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName(tableName);
        for (Map<String, Object> map : data) {
            simpleJdbcInsert.execute(map);
        }
    }

    public List<Filter> getAnalysisFilter(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_FILTER, id), new Filter());
    }

    public List<AnalysisFilterGroup> getAnalysisFilterGroup(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_FILTER_GROUP, id), new AnalysisFilterGroup());
    }

    public void dropDataTable(String tableName) {
        try {
            jdbcTemplate.execute(String.format(SqlMap.Dashboard.DROP_TABLE, tableName));
        } catch (DataAccessException e) {}
    }

    public Analysis getAnalysisId(String analysisId) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_ANALYSIS_ID, analysisId), new Analysis());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
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

    public List<AnalysisChart> getAnalysisChart(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_CHART, id), new AnalysisChart());
    }

    public void insertDashboardChart(DashboardChart item) throws SQLException {
        namedParameterJdbcTemplate.update(SqlMap.Dashboard.INSERT_DASHBOARD_CHART, item.getParameter());
    }

    public void updateDashboardChart(DashboardChart item) throws SQLException {
        namedParameterJdbcTemplate.update(SqlMap.Dashboard.UPDATE_DASHBOARD_CHART, item.getParameter());
    }

    public void delDashboardChart(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Dashboard.DELETE_DASHBOARD_CHART, id));
    }

    public List<Map<String, Object>> getColumnInfo(String tableName) throws SQLException {
        return jdbcTemplate.queryForList(String.format("DESC %s", tableName));
    }

    public void createDashboardDataSetTable(String srcTableName, String desTableName, String insertColumnStr, String where) throws SQLException {
        String query = null;
        if (where != null) {
            query = String.format("CREATE TABLE %s IGNORE AS SELECT %s FROM %s WHERE %s", desTableName, insertColumnStr, srcTableName, where);
        } else {
            query = String.format("CREATE TABLE %s IGNORE AS SELECT %s FROM %s", desTableName, insertColumnStr, srcTableName);
        }
        jdbcTemplate.execute(query);
    }

    public int insertDashboardFilterGroupInfo(DashboardFilterGroup filterGroup) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("DASHBOARD_FILTER_GROUP_INFO").usingGeneratedKeyColumns("ID");
        Number key = simpleJdbcInsert.executeAndReturnKey(filterGroup.getParameter());
        return key.intValue();
    }

    public void insertDashboardFilterInfo(List<Map<String, Object>> values) throws SQLException {
        Map<String, Object>[] param = values.toArray(new Map[values.size()]);
        namedParameterJdbcTemplate.batchUpdate(SqlMap.Dashboard.INSERT_DASHBOARD_FILTER_INFO, param);
    }

    public void insertDashboardColumnInfo(int dashboardDataSetId, int dataSetId) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Dashboard.INSERT_DASHBOARD_COLUMN_INFO, dashboardDataSetId, dataSetId));
    }

    public List<ChartInfo> getChartInfo() throws  SQLException {
        return jdbcTemplate.query(SqlMap.Analysis.SELECT_CHART_INFO, new ChartInfo());
    }

    public List<Map<String, Object>> getDataTableData(String tableName, String columns) throws SQLException {
        return jdbcTemplate.queryForList(String.format("SELECT %s FROM %s", columns, tableName));
    }

    public void insertDashboardGroupMapping(int id, int groupId) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.INSERT_ANALYSIS_GROUP_MAPPING, new Object[]{id, groupId, 1});
    }

    public String getQueryTextFromQueryId(String id) throws SQLException {
        return jdbcTemplate.queryForObject(String.format(SqlMap.Dashboard.SELECT_QUERY_TEXT, id), String.class);
    }

    public List<Map<String, Object>> selectDataSetTables(List<String> list) throws SQLException {
        return jdbcTemplate.queryForList(String.format(SqlMap.Query.SELECT_DATA_SET_TABLES, String.join(",", list)));
    }

    public List<Map<String, Object>> getDataSetTable(String query) throws SQLException {
        log.info("dashboard getDataSetTable: {}", query);
        return jdbcTemplate.queryForList(query);
    }

    public List<Filter> getDashboardFilter(int dashboardDataSetId, int queryId) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Dashboard.SELECT_DASHBOARD_FILTER, dashboardDataSetId, queryId), new Filter());
    }

    public List<DashboardFilterGroup> getDashboardFilterGroup(int dashboardDataSetId, int queryId) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Dashboard.SELECT_DASHBOARD_FILTER_GROUP, dashboardDataSetId, queryId), new DashboardFilterGroup());
    }

}
