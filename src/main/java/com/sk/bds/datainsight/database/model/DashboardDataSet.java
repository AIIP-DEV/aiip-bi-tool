package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sk.bds.datainsight.util.Util;
import lombok.Data;
import org.json.JSONArray;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Data
public class DashboardDataSet implements RowMapper<DashboardDataSet>, Serializable {

    private Integer id;
    private int dashboardId;
    private Integer dataSetId;
    private String dataTable;
    private Date createDate;
    private Integer queryId;
    private List<Object> variableInfo;

    public DashboardDataSet() {}

    public DashboardDataSet(Map<String, Object> param) {
        dashboardId = (int)param.get("dashboardId");
        dataSetId = (Integer)param.get("dataSetId");
        dataTable = (String)param.get("dataTable");
        queryId = (Integer)param.get("queryId");
        variableInfo = (List<Object>)param.get("variableInfo");
    }

    public DashboardDataSet mapRow(ResultSet resultSet, int i) throws SQLException {
        DashboardDataSet dashboardDataSet = new DashboardDataSet();
        dashboardDataSet.setId(resultSet.getInt("ID"));
        dashboardDataSet.setDashboardId(resultSet.getInt("DASHBOARD_ID"));
        dashboardDataSet.setDataSetId(resultSet.getInt("DATA_SET_ID"));
        dashboardDataSet.setDataTable(resultSet.getString("DATA_TABLE"));
        dashboardDataSet.setCreateDate(resultSet.getDate("CREATE_DATE"));
        dashboardDataSet.setQueryId((Integer)resultSet.getObject("QUERY_ID"));
        String variableInfo = resultSet.getString("VARIABLE_INFO");
        if (variableInfo != null) {
            List<Object> variableList = new ArrayList<>();
            try {
                Util.setListFromJson(variableList, new JSONArray(variableInfo));
            } catch (Exception e) {
                throw new SQLException(e);
            }
            dashboardDataSet.setVariableInfo(variableList);
        }
        return dashboardDataSet;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() throws SQLException{
        HashMap<String, Object> param = new HashMap<>();
        param.put("DASHBOARD_ID", dashboardId);
        param.put("DATA_SET_ID", dataSetId);
        param.put("DATA_TABLE", dataTable);
        Date date = new Date();
        param.put("CREATE_DATE", date);
        param.put("QUERY_ID", queryId);
        try {
            if (variableInfo != null) {
                JSONArray array = new JSONArray();
                Util.setJsonFromList(array, variableInfo);
                param.put("VARIABLE_INFO", array.toString());
            }
        } catch (Exception e) {
            throw new SQLException(e);
        }
        return new MapSqlParameterSource(param);
    }
}
