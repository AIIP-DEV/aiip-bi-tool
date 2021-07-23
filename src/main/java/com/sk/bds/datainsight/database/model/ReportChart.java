package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sk.bds.datainsight.util.JsonUtil;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Data
public class ReportChart extends Chart implements RowMapper<ReportChart>, Serializable {

    private int reportId;

    public ReportChart() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public ReportChart(Map<String, Object> param) throws Exception {
        if (param.get("id") != null) {
            id = Integer.parseInt(param.get("id").toString());
        }
        reportId = Integer.parseInt(param.get("reportId").toString());
        name = param.get("name") == null ? null : param.get("name").toString();
        analysisChartId = param.get("analysis_chart_id") == null ? null : Integer.parseInt(param.get("analysis_chart_id").toString());
        type = param.get("type") == null ? null : param.get("type").toString();


//        ObjectMapper objectMapper = new ObjectMapper();

//        drawInfo = new DrawInfo(param);
        drawInfo = new JSONObject(param);
    }

    public ReportChart mapRow(ResultSet resultSet, int i) throws SQLException {
        ReportChart reportChart = new ReportChart();
        reportChart.setId(resultSet.getInt("ID"));
        reportChart.setReportId(resultSet.getInt("REPORT_ID"));
        reportChart.setName(resultSet.getString("NAME"));
        reportChart.setAnalysisChartId(resultSet.getInt("ANALYSIS_CHART_ID"));
        reportChart.setType(resultSet.getString("TYPE"));
        try {
//            reportChart.setDrawInfo(new DrawInfo((Map<String, Object>)resultSet.getObject("DRAW_INFO")));
//            reportChart.setDrawInfo((JSONObject) resultSet.getObject("DRAW_INFO"));
            reportChart.setLayout((convertJSONstringToMap((String) resultSet.getObject("DRAW_INFO"))));
        } catch (Exception e) {
            throw new SQLException(e);
        }

        reportChart.setCreateDate(resultSet.getDate("CREATE_DATE"));
        return reportChart;
    }

    public static Map<String,Object> convertJSONstringToMap(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();

        map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});

        return map;
    }
    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        if (id != null) {
            param.put("ID", id);
        }
        param.put("REPORT_ID", reportId);
        param.put("NAME", name);
        param.put("ANALYSIS_CHART_ID", analysisChartId);
        param.put("TYPE", type);

 
//        String draw = null;
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            draw = objectMapper.writeValueAsString(drawInfo);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        param.put("DRAW_INFO", draw);


        param.put("DRAW_INFO", drawInfo.toString());


        Date date = new Date();
        if (createDate == null) {
            param.put("CREATE_DATE", date);
        } else {
            param.put("CREATE_DATE", createDate);
        }

        return new MapSqlParameterSource(param);
    }
}
