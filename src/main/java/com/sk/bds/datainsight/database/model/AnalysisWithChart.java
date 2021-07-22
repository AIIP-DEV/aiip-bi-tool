package com.sk.bds.datainsight.database.model;

import com.sk.bds.datainsight.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.RowMapper;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisWithChart implements RowMapper<AnalysisWithChart>, Serializable {

    private int id;
    private Integer dataSetId;
    private String name;
    private String subName;
    private String srcType;
    private String srcName;
    private String image;
    private List<AnalysisChart> charts;
    private String dataTable;
    private String createUser;
    private Date updateDate;
    private Date createDate;
    private int groupId;
    private String groupName;
    private Integer queryId;
    private List<Object> variableInfo;
    private int status;

    public AnalysisWithChart mapRow(ResultSet resultSet, int i) throws SQLException {
        AnalysisWithChart analysisWithChart = new AnalysisWithChart();
        analysisWithChart.setId(resultSet.getInt("ID"));
        analysisWithChart.setDataSetId((Integer)resultSet.getObject("DATA_SET_ID"));
        analysisWithChart.setName(resultSet.getString("NAME"));
        analysisWithChart.setSubName(resultSet.getString("SUB_NAME"));
        analysisWithChart.setQueryId((Integer)resultSet.getObject("QUERY_ID"));
        if (analysisWithChart.getQueryId() != null) {
            analysisWithChart.setSrcType("QUERY");
            analysisWithChart.setSrcName(resultSet.getString("QUERY_NAME"));
        } else {
            analysisWithChart.setSrcType(resultSet.getString("SRC_TYPE"));
            analysisWithChart.setSrcName(resultSet.getString("SRC_NAME"));
        }
        analysisWithChart.setImage(resultSet.getString("THUMB_IMG"));
        try {
//            analysisWithChart.setChart();
        } catch (Exception e) {
            throw new SQLException(e);
        }
        analysisWithChart.setDataTable(resultSet.getString("DATA_TABLE"));
        analysisWithChart.setCreateUser(resultSet.getString("CREATE_USER"));
        analysisWithChart.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        analysisWithChart.setCreateDate(resultSet.getDate("CREATE_DATE"));
        String variableInfo = resultSet.getString("VARIABLE_INFO");
        if (variableInfo != null) {
            List<Object> variableList = new ArrayList<>();
            try {
                Util.setListFromJson(variableList, new JSONArray(variableInfo));
            } catch (Exception e) {
                throw new SQLException(e);
            }
            analysisWithChart.setVariableInfo(variableList);
        }
        analysisWithChart.setGroupName(resultSet.getString("GROUP_NAME"));
        analysisWithChart.setGroupId(resultSet.getInt("GROUP_ID"));
        return analysisWithChart;
    }
}
