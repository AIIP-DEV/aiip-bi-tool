package com.sk.bds.datainsight.database.dao;

import com.sk.bds.datainsight.database.model.*;
import com.sk.bds.datainsight.util.SqlMap;
import com.sk.bds.datainsight.util.Util;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
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

public class DataSetDao {
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private Connection conn;
    private User user;

    public DataSetDao(SingleConnectionDataSource ds, User user) throws Exception {
        this.user = user;
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

    public void createDataSetTable(String tableName, String column) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.DataSet.CREATE_DATA_SET_TABLE, tableName, column));
    }

    public void dropDataSetTable(String tableName) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.DataSet.DROP_DATA_SET_TABLE, tableName));
    }

    public int insertDataSet(DataSet dataSet) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("DATA_SET").usingGeneratedKeyColumns("ID");
        Number key = simpleJdbcInsert.executeAndReturnKey(dataSet.getParameter(false));
        return key.intValue();
    }

    public int insertDataSetFilterGroup(int dataSetId, String name, String enable) throws SQLException {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert = simpleJdbcInsert.withTableName("DATA_SET_FILTER_GROUP")
                .usingGeneratedKeyColumns("ID")
                .usingColumns("DATA_SET_ID", "NAME", "ENABLE");
        Map<String, Object> param = new HashMap<>();
        param.put("DATA_SET_ID", dataSetId);
        param.put("NAME", name);
        param.put("ENABLE", enable);
        Number key = simpleJdbcInsert.executeAndReturnKey(param);
        return key.intValue();
    }

    public void insertDataSetFilter(Filter filter) throws SQLException {
        namedParameterJdbcTemplate.update(SqlMap.DataSet.INSERT_DATA_SET_FILTER, filter.getParameter());
    }

    public void insertDataSetAddColumn(DataSetAddColumn addColumn) throws SQLException {
        namedParameterJdbcTemplate.update(SqlMap.DataSet.INSERT_DATA_SET_ADD_COLUMN, addColumn.getParameter());
    }

    public void insertDataSetTable(String tableName, String insertColumns, String selectColumns, List<Map<String, Object>> values) throws SQLException {
        String query = String.format(SqlMap.DataSet.INSERT_DATA_SET_TABLE, tableName, insertColumns, selectColumns);
        Map<String, Object>[] param = values.toArray(new Map[values.size()]);
        namedParameterJdbcTemplate.batchUpdate(query, param);
    }

    public List<DataSet> getDataSetList(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.DATA_SET_LIST);
        return jdbcTemplate.query(String.format(SqlMap.DataSet.SELECT_DATA_SET, listParam.get("where"), listParam.get("orderBy"), listParam.get("limit")), new DataSet());
    }

    public int getDataSetListCount(JSONObject param) throws SQLException {
        Map<String, String> listParam = Util.getListParam(param, Util.DATA_SET_LIST);
        return jdbcTemplate.queryForObject(String.format(SqlMap.DataSet.SELECT_DATA_SET_COUNT, listParam.get("where")), Integer.class);
    }

    public int getDataSetUpdateCount() throws SQLException {
        return jdbcTemplate.queryForObject(SqlMap.DataSet.SELECT_DATA_SET_UPDATE_COUNT, Integer.class);
    }

    public DataSet getDataSet(String id) throws SQLException {
        try {
            return jdbcTemplate.queryForObject(String.format(SqlMap.DataSet.SELECT_DATA_SET_ID, id), new DataSet());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Filter> getDataSetFilter(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.DataSet.SELECT_DATA_SET_FILTER, id), new Filter());
    }

    public List<DataSetFilterGroup> getDataSetFilterGroup(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.DataSet.SELECT_DATA_SET_FILTER_GROUP, id), new DataSetFilterGroup());
    }

    public List<DataSetAddColumn> getDataSetAddColumn(String id) throws SQLException {
        return jdbcTemplate.query(String.format(SqlMap.DataSet.SELECT_DATA_SET_ADD_COLUMN, id), new DataSetAddColumn());
    }

    public List<Map<String, Object>> getDataSetTable(String query) throws SQLException {
        return jdbcTemplate.queryForList(query);
    }

    public void delDataSet(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.DataSet.DELETE_DATA_SET, id));
        jdbcTemplate.execute(String.format(SqlMap.DataSet.DELETE_DATA_SET_GROUP_MAPPING, id));
    }

    public void updateDataSet(DataSet dataSet) throws SQLException {
        namedParameterJdbcTemplate.update(SqlMap.DataSet.UPDATE_DATA_SET, dataSet.getParameter(true));
    }

    public void updateDataSet(int dataSetId, String useColumns, String originalColumn, String changeColumn, int status) throws SQLException {
        HashMap<String, Object> param = new HashMap<>();
        param.put("ID", dataSetId);
        param.put("USE_COLUMNS", useColumns);
        param.put("ORIGINAL_COLUMN", originalColumn);
        param.put("CHANGE_COLUMN", changeColumn);
        param.put("STATUS", status);
        if (useColumns == null || originalColumn == null || changeColumn == null) {
            namedParameterJdbcTemplate.update(SqlMap.DataSet.UPDATE_DATA_SET_IS_UPDATE, new MapSqlParameterSource(param));
        } else {
            namedParameterJdbcTemplate.update(SqlMap.DataSet.UPDATE_DATA_SET_INFO, new MapSqlParameterSource(param));
        }
    }

    public List<Map<String, Object>> getColumnInfo(String tableName) throws SQLException {
        return jdbcTemplate.queryForList(String.format("DESC %s", tableName));
    }

    public void delDataSetFilterGroup(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.DataSet.DELETE_DATA_SET_FILTER_GROUP, id));
    }

    public void delDataSetAddColumn(String id) throws SQLException {
        jdbcTemplate.execute(String.format(SqlMap.DataSet.DELETE_DATA_SET_ADD_COLUMN, id));
    }

    public void updateDataSetTable(String srcTableName, String desTableName, String selectColumn) throws SQLException {
        String query = String.format("INSERT IGNORE INTO %s SELECT %s FROM %s", desTableName, selectColumn, srcTableName);
        jdbcTemplate.execute(query);
    }

    public void insertDataSetGroupMapping(int id, int groupId) throws SQLException {
        jdbcTemplate.update(SqlMap.Analysis.INSERT_ANALYSIS_GROUP_MAPPING, new Object[]{id, groupId, 2});
    }
}
