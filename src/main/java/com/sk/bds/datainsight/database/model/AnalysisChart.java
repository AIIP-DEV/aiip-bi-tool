package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.sk.bds.datainsight.util.JsonUtil;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class AnalysisChart extends Chart implements RowMapper<AnalysisChart>, Serializable {

    private int analysisId;
    private Date updateDate;

    public AnalysisChart() {
    }

    public AnalysisChart(Map<String, Object> param) throws Exception {
        if (param.get("id") != null) {
            id = Integer.parseInt(param.get("id").toString());
        }
        type = param.get("type") == null ? null : param.get("type").toString();
        analysisId = Integer.parseInt(param.get("analysisId").toString());
        name = param.get("name") == null ? null : param.get("name").toString();
        subName = param.get("subName") == null ? null : param.get("subName").toString();
        Map<String, Object> valueInfo = (Map<String, Object>) param.get("valueInfo");
        axis = valueInfo.get("axis") == null ? null : valueInfo.get("axis").toString();
        value = valueInfo.get("value") == null ? null : valueInfo.get("value").toString();
        group = valueInfo.get("group") == null ? null : valueInfo.get("group").toString();
        line = valueInfo.get("line") == null ? null : valueInfo.get("line").toString();
        flag = valueInfo.get("flag") == null ? null : valueInfo.get("flag").toString();
        option = param.get("option");
        thumbnail = param.get("thumbnail") == null ? null : param.get("thumbnail").toString();
        valueInfoSetting();
    }

    public AnalysisChart mapRow(ResultSet resultSet, int i) throws SQLException {
        AnalysisChart analysisChart = new AnalysisChart();
        analysisChart.setId(resultSet.getInt("ID"));
        analysisChart.setAnalysisId(resultSet.getInt("ANALYSIS_ID"));
        analysisChart.setName(resultSet.getString("NAME"));
        analysisChart.setChart(resultSet.getInt("CHART_ID"));
        analysisChart.setAxis(resultSet.getString("AXIS"));
        analysisChart.setValue(resultSet.getString("VALUE"));
        analysisChart.setGroup(resultSet.getString("GROUP"));
        analysisChart.setLine(resultSet.getString("LINE"));
        analysisChart.setFlag(resultSet.getString("FLAG"));
        analysisChart.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        analysisChart.setCreateDate(resultSet.getDate("CREATE_DATE"));
        analysisChart.setType(resultSet.getString("TYPE"));
        try {

//            analysisChart.setOption(convertJSONStringToMap(resultSet.getString("OPTION")));
//            String object = resultSet.getString("OPTION").replaceAll(" \\ ", "");
//            analysisChart.setOption(convertJSONStringToMap(object));
//            JSONObject parser = new JSONObject(object);
            analysisChart.setOption(convertJSONStringToMap(resultSet.getString("OPTION")));
//            System.out.println(resultSet.getString("OPTION"));
//            System.out.println(resultSet.getString("OPTION").getClass());
//            Map map = JsonUtil.unmarshal(resultSet.getString("OPTION"), Map.class);
//            System.out.println(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        analysisChart.setThumbnail(resultSet.getString("THUMBNAIL"));
        analysisChart.valueInfoSetting();
        return analysisChart;
    }

    private Map<String, Object> convertJSONStringToMap(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        return map;
    }

    private void valueInfoSetting() {
        this.valueInfo = new ValueInfo(axis, value, group, line, flag);
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() throws Exception {
        HashMap<String, Object> param = new HashMap<>();
        if (id != null) {
            param.put("ID", id);
        }
        param.put("TYPE", type);
        param.put("ANALYSIS_ID", analysisId);
        param.put("NAME", name);
        param.put("AXIS", axis);
        param.put("VALUE", value);
        param.put("GROUP", group);
        param.put("LINE", line);
        param.put("FLAG", flag);
//        ObjectMapper mapper = new ObjectMapper();
//        option = mapper.writeValueAsString(option);
        param.put("OPTION", JsonUtil.marshal(option));
        param.put("THUMBNAIL", thumbnail);
        Date date = new Date();
        param.put("UPDATE_DATE", date);
        if (createDate == null) {
            param.put("CREATE_DATE", date);
        } else {
            param.put("CREATE_DATE", createDate);
        }

        return new MapSqlParameterSource(param);
    }
}
