package com.sk.bds.datainsight.database.model;

import com.sk.bds.datainsight.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.springframework.jdbc.core.RowMapper;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analysis implements RowMapper<Analysis>, Serializable {

    private int id;
    private Integer dataSetId;
    private String name;
    private String subName;
    private String srcType;
    private String srcName;
    private String image;
    private Integer cntChart;
    private String dataTable;
    private String createUser;
    private Date updateDate;
    private Date createDate;
    private int groupId;
    private String groupName;
    private Integer queryId;
    private List<Object> variableInfo;
    private int status;


    public Analysis mapRow(ResultSet resultSet, int i) throws SQLException {
        Analysis analysis = new Analysis();
        analysis.setId(resultSet.getInt("ID"));
        analysis.setDataSetId((Integer)resultSet.getObject("DATA_SET_ID"));
        analysis.setName(resultSet.getString("NAME"));
        analysis.setSubName(resultSet.getString("SUB_NAME"));
        analysis.setQueryId((Integer)resultSet.getObject("QUERY_ID"));
        if (analysis.getQueryId() != null) {
            analysis.setSrcType("QUERY");
            analysis.setSrcName(resultSet.getString("QUERY_NAME"));
        } else {
            analysis.setSrcType(resultSet.getString("SRC_TYPE"));
            analysis.setSrcName(resultSet.getString("SRC_NAME"));
        }
        analysis.setImage(resultSet.getString("THUMB_IMG"));
        analysis.setCntChart(resultSet.getInt("CNT_CHART"));
        analysis.setDataTable(resultSet.getString("DATA_TABLE"));
        analysis.setCreateUser(resultSet.getString("CREATE_USER"));
        analysis.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        analysis.setCreateDate(resultSet.getDate("CREATE_DATE"));
        String variableInfo = resultSet.getString("VARIABLE_INFO");
        if (variableInfo != null) {
            List<Object> variableList = new ArrayList<>();
            try {
                Util.setListFromJson(variableList, new JSONArray(variableInfo));
            } catch (Exception e) {
                throw new SQLException(e);
            }
            analysis.setVariableInfo(variableList);
        }
        analysis.setGroupName(resultSet.getString("GROUP_NAME"));
        analysis.setGroupId(resultSet.getInt("GROUP_ID"));
        return analysis;
    }
}
