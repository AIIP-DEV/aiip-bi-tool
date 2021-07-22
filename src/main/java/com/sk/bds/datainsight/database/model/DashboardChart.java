package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sk.bds.datainsight.util.Util;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Data
public class DashboardChart extends Chart implements RowMapper<DashboardChart>, Serializable {

    private int dashboardId;
    private Integer dashboardDataId;

    public DashboardChart() {}

    public DashboardChart(int dashboardId, Integer dashboardDataId, AnalysisChart chart) throws Exception {
        this.dashboardId = dashboardId;
        this.dashboardDataId = dashboardDataId;
        name = chart.getName();
        subName = chart.getSubName();
        this.chart = chart.getChart();
        axis = chart.getAxis();
        value = chart.getValue();
        group = chart.getGroup();
        line = chart.getLine();
        flag = chart.getFlag();
        drawInfo = chart.getDrawInfo();
        valueInfoSetting();
    }

    public DashboardChart(Map<String, Object> param) throws Exception {
        if (param.get("id") != null) {
            id = Integer.parseInt(param.get("id").toString());
        }
        dashboardId = Integer.parseInt(param.get("dashboardId").toString());
        dashboardDataId = Integer.parseInt(param.get("dashboardDataId").toString());
        name = param.get("name") == null ? null : param.get("name").toString();
        subName = param.get("subName") == null ? null : param.get("subName").toString();
        chart = Integer.parseInt(param.get("chart").toString());
        Map<String, Object> valueInfo = (Map<String, Object>)param.get("valueInfo");
        axis = valueInfo.get("axis") == null ? null : valueInfo.get("axis").toString();
        value = valueInfo.get("value") == null ? null : valueInfo.get("value").toString();
        group = valueInfo.get("group") == null ? null : valueInfo.get("group").toString();
        line = valueInfo.get("line") == null ? null : valueInfo.get("line").toString();
        flag = valueInfo.get("flag") == null ? null : valueInfo.get("flag").toString();
        Map<String, Object> drawInfoMap = (Map<String, Object>)param.get("drawInfo");
        JSONObject info = new JSONObject();
        Util.setJsonFromMap(info, drawInfoMap);
        drawInfo = new DrawInfo(info);
        valueInfoSetting();
    }

    public DashboardChart mapRow(ResultSet resultSet, int i) throws SQLException {
        DashboardChart dashboardChart = new DashboardChart();
        dashboardChart.setId(resultSet.getInt("ID"));
        dashboardChart.setDashboardId(resultSet.getInt("DASHBOARD_ID"));
        dashboardChart.setDashboardDataId((Integer)resultSet.getObject("DASHBOARD_DATA_ID"));
        dashboardChart.setName(resultSet.getString("NAME"));
        dashboardChart.setSubName(resultSet.getString("SUB_NAME"));
        dashboardChart.setChart(resultSet.getInt("CHART_ID"));
        dashboardChart.setAxis(resultSet.getString("AXIS"));
        dashboardChart.setValue(resultSet.getString("VALUE"));
        dashboardChart.setGroup(resultSet.getString("GROUP"));
        dashboardChart.setLine(resultSet.getString("LINE"));
        dashboardChart.setFlag(resultSet.getString("FLAG"));
//        try {
//            dashboardChart.setDrawInfo(new DrawInfo(new JSONObject(resultSet.getString("DRAW_INFO"))));
//        } catch (Exception e) {
//            throw new SQLException(e);
//        }
        dashboardChart.setCreateDate(resultSet.getDate("CREATE_DATE"));
        dashboardChart.valueInfoSetting();
        return dashboardChart;
    }

    private void valueInfoSetting() {
        this.valueInfo = new ValueInfo(axis, value, group, line, flag);
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        if (id != null) {
            param.put("ID", id);
        }
        param.put("DASHBOARD_ID", dashboardId);
        param.put("DASHBOARD_DATA_ID", dashboardDataId);
        param.put("NAME", name);
        param.put("SUB_NAME", subName);
        param.put("CHART_ID", chart);
        param.put("AXIS", axis);
        param.put("VALUE", value);
        param.put("GROUP", group);
        param.put("LINE", line);
        param.put("FLAG", flag);
//        param.put("DRAW_INFO", drawInfo.toString());
        Date date = new Date();
        if (createDate == null) {
            param.put("CREATE_DATE", date);
        } else {
            param.put("CREATE_DATE", createDate);
        }

        return new MapSqlParameterSource(param);
    }
}
