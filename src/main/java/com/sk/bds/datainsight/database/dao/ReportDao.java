package com.sk.bds.datainsight.database.dao;

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
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ReportDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final Connection conn;
    private User user;

    public ReportDao(SingleConnectionDataSource ds) throws Exception {
        conn = ds.getConnection();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(ds);
        jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
    }

    public ReportDao(SingleConnectionDataSource ds, User user) throws Exception {
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

    public List<Report> getReportList(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.REPORT_LIST);
        return jdbcTemplate.query(String.format(SqlMap.Report.SELECT_REPORT, listParam.get("where"), listParam.get("orderBy"), listParam.get("limit")), new Report());
    }

    public int getReportListCount(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.REPORT_LIST);
        return jdbcTemplate.queryForObject(String.format(SqlMap.Report.SELECT_REPORT_COUNT, listParam.get("where")), Integer.class);
    }

    public Report getReport(String id) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Report.SELECT_REPORT_ID, id), new Report());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<ReportChart> getReportChartList(String ReportId) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.Report.SELECT_REPORT_CHART, ReportId), new ReportChart());
    }

    public ReportChart getReportChartById(String reportId) throws SQLException{
        return jdbcTemplate.queryForObject(String.format(SqlMap.Report.SELECT_REPORT_CHART_DRAW_INFO, reportId), new ReportChart());
    }

//
//    public List<SqlMap.ReportChart> getReportChartListByDataId(String dataId) throws SQLException {
//        return jdbcTemplate.query(String.format(SqlMap.Report.SELECT_Report_CHART_DATA_ID, dataId), new SqlMap.ReportChart());
//    }
//
//    public SqlMap.ReportChart getReportChart(String id) throws SQLException {
//        return jdbcTemplate.queryForObject(String.format(SqlMap.Report.SELECT_Report_CHART_ID, id), new SqlMap.ReportChart());
//    }
//
//    public List<SqlMap.ReportDataSet> getReportDataSetList(String ReportId) throws SQLException {
//        return jdbcTemplate.query(String.format(SqlMap.Report.SELECT_Report_DATA_SET, SqlMap.ReportId), new SqlMap.ReportDataSet());
//    }
//
//    public SqlMap.ReportDataSet getReportDataSet(String id) throws SQLException {
//        return jdbcTemplate.queryForObject(String.format(SqlMap.Report.SELECT_Report_DATA_SET_ID, id), new SqlMap.ReportDataSet());
//    }
//
//    public void delReportDataSet(String id) throws SQLException {
//        jdbcTemplate.execute(String.format(SqlMap.Report.DELETE_Report_DATA_SET, id));
//    }
//
    public void delReport(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.Report.DELETE_REPORT, id));
        jdbcTemplate.execute(String.format(SqlMap.Report.DELETE_REPORT_GROUP_MAPPING, id));
    }
//
    public void updateReport(String id, Object name, Object thumbImg, Object user) throws SQLException {
        String column = String.format("NAME = '%s'", name);
        if (thumbImg != null) {
            column = String.format("%s, THUMB_IMG = '%s'", thumbImg);
        }
        jdbcTemplate.execute(String.format(SqlMap.Report.UPDATE_REPORT, column, id, user));
    }
//
//    public void createInitTable() throws SQLException {
//        jdbcTemplate.execute(SqlMap.Report.CREATE_QUERY);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_DATA_SET);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_DATA_SET_FILTER_GROUP);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_DATA_SET_FILTER);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_DATA_SET_ADD_COLUMN);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_CHART_INFO);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_ECHART_INFO);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_CHART_AGGREGATE);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_CHART_FORMAT);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_Report);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_Report_DATA_SET);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_Report_CHART);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_ANALYSIS);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_ANALYSIS_CHART);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_ANALYSIS_FILTER_GROUP);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_ANALYSIS_FILTER);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_ANALYSIS_COLUMN_INFO);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_Report_FILTER_GROUP_INFO);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_Report_FILTER_INFO);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_Report_COLUMN_INFO);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_ORIGINAL);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_GROUP);
//        jdbcTemplate.execute(SqlMap.Report.CREATE_GROUP_MAPPING);
//    }
//
//    public void insertInitData(String tableName, List<Map<String, Object>> data) throws SQLException {
//        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
//        simpleJdbcInsert.withTableName(tableName);
//        for (Map<String, Object> map : data) {
//            simpleJdbcInsert.execute(map);
//        }
//    }
//
//    public List<Filter> getAnalysisFilter(String id) throws SQLException {
//        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_FILTER, id), new Filter());
//    }
//
//    public List<AnalysisFilterGroup> getAnalysisFilterGroup(String id) throws SQLException {
//        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_FILTER_GROUP, id), new AnalysisFilterGroup());
//    }
//
    public void dropDataTable(String tableName) {
        try {
            jdbcTemplate.execute(String.format(SqlMap.Report.DROP_TABLE, tableName));
        } catch (DataAccessException ignored) {}
    }
//
//    public Analysis getAnalysisId(String analysisId) throws SQLException {
//        try {
//            return jdbcTemplate.queryForObject(String.format(SqlMap.Analysis.SELECT_ANALYSIS_ID, analysisId), new Analysis());
//        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
//            return null;
//        }
//    }
//
    public int insertReport(Report Report) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("REPORT").usingGeneratedKeyColumns("ID");
        Number key = simpleJdbcInsert.executeAndReturnKey(Report.getParameter());
        return key.intValue();
    }
//
//    public int insertReportDataSet(SqlMap.ReportDataSet ReportDataSet) throws SQLException {
//        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
//        simpleJdbcInsert.withTableName("Report_DATA_SET").usingGeneratedKeyColumns("ID");
//        Number key = simpleJdbcInsert.executeAndReturnKey(SqlMap.ReportDataSet.getParameter());
//        return key.intValue();
//    }
//
//    public List<AnalysisChart> getAnalysisChart(String id) throws SQLException {
//        return jdbcTemplate.query(String.format(SqlMap.Analysis.SELECT_ANALYSIS_CHART, id), new AnalysisChart());
//    }
//
    public void insertReportChart(ReportChart item) throws SQLException {
        namedParameterJdbcTemplate.update(SqlMap.Report.INSERT_REPORT_CHART, item.getParameter());
    }
//
    public void updateReportChart(ReportChart item) throws SQLException {
        namedParameterJdbcTemplate.update(SqlMap.Report.UPDATE_REPORT_CHART, item.getParameter());
    }
//
//    public void delReportChart(String id) throws SQLException {
//        jdbcTemplate.execute(String.format(SqlMap.Report.DELETE_Report_CHART, id));
//    }
//
//    public List<Map<String, Object>> getColumnInfo(String tableName) throws SQLException {
//        return jdbcTemplate.queryForList(String.format("DESC %s", tableName));
//    }
//
//    public void createReportDataSetTable(String srcTableName, String desTableName, String insertColumnStr, String where) throws SQLException {
//        String query = null;
//        if (where != null) {
//            query = String.format("CREATE TABLE %s IGNORE AS SELECT %s FROM %s WHERE %s", desTableName, insertColumnStr, srcTableName, where);
//        } else {
//            query = String.format("CREATE TABLE %s IGNORE AS SELECT %s FROM %s", desTableName, insertColumnStr, srcTableName);
//        }
//        jdbcTemplate.execute(query);
//    }
//
//    public int insertReportFilterGroupInfo(SqlMap.ReportFilterGroup filterGroup) throws SQLException {
//        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
//        simpleJdbcInsert.withTableName("Report_FILTER_GROUP_INFO").usingGeneratedKeyColumns("ID");
//        Number key = simpleJdbcInsert.executeAndReturnKey(filterGroup.getParameter());
//        return key.intValue();
//    }
//
//    public void insertReportFilterInfo(List<Map<String, Object>> values) throws SQLException {
//        Map<String, Object>[] param = values.toArray(new Map[values.size()]);
//        namedParameterJdbcTemplate.batchUpdate(SqlMap.Report.INSERT_Report_FILTER_INFO, param);
//    }
//
//    public void insertReportColumnInfo(int ReportDataSetId, int dataSetId) throws SQLException {
//        jdbcTemplate.execute(String.format(SqlMap.Report.INSERT_Report_COLUMN_INFO, SqlMap.ReportDataSetId, dataSetId));
//    }
//
//    public List<ChartInfo> getChartInfo() throws  SQLException {
//        return jdbcTemplate.query(SqlMap.Analysis.SELECT_CHART_INFO, new ChartInfo());
//    }
//
//    public List<Map<String, Object>> getDataTableData(String tableName, String columns) throws SQLException {
//        return jdbcTemplate.queryForList(String.format("SELECT %s FROM %s", columns, tableName));
//    }
//
    public void insertReportGroupMapping(int id, int groupId) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.INSERT_ANALYSIS_GROUP_MAPPING, id, groupId, 1);
    }
//
//    public String getQueryTextFromQueryId(String id) throws SQLException {
//        return jdbcTemplate.queryForObject(String.format(SqlMap.Report.SELECT_QUERY_TEXT, id), String.class);
//    }
//
//    public List<Map<String, Object>> selectDataSetTables(List<String> list) throws SQLException {
//        return jdbcTemplate.queryForList(String.format(SqlMap.Query.SELECT_DATA_SET_TABLES, String.join(",", list)));
//    }
//
//    public List<Map<String, Object>> getDataSetTable(String query) throws SQLException {
//        log.info("Report getDataSetTable: {}", query);
//        return jdbcTemplate.queryForList(query);
//    }
//
//    public List<Filter> getReportFilter(int ReportDataSetId, int queryId) throws SQLException {
//        return jdbcTemplate.query(String.format(SqlMap.Report.SELECT_Report_FILTER, SqlMap.ReportDataSetId, queryId), new Filter());
//    }
//
//    public List<SqlMap.ReportFilterGroup> getReportFilterGroup(int ReportDataSetId, int queryId) throws SQLException {
//        return jdbcTemplate.query(String.format(SqlMap.Report.SELECT_Report_FILTER_GROUP, SqlMap.ReportDataSetId, queryId), new SqlMap.ReportFilterGroup());
//    }

    public Integer getGroupCount(String groupId) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Report.SELECT_GROUP_BY_NAME, groupId), Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public void createGroup(String groupId) throws SQLException {
        HashMap<String, Object> param = new HashMap<>();
        param.put("NAME", groupId);
        param.put("DESCRIPTION", groupId);

        namedParameterJdbcTemplate.update(SqlMap.Report.INSERT_GROUP_TABLE, new MapSqlParameterSource(param));
    }

    public Integer getGroupId(String groupId) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.Report.SELECT_GROUP_BY_ID, groupId), Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

}
