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
public class Query implements RowMapper<Query>, Serializable {

    private int id;
    private String name;
    private String description;
    private String queryText;
    private int groupId;
    private String groupName;
    private List<Object> variableInfo;
    private Date createDate;
    private Date updateDate;

    public Query mapRow(ResultSet resultSet, int i) throws SQLException {
        Query query = new Query();
        query.setId(resultSet.getInt("ID"));
        query.setName(resultSet.getString("NAME"));
        query.setDescription(resultSet.getString("DESCRIPTION"));
        query.setQueryText(resultSet.getString("QUERY_TEXT"));
        query.setGroupId(resultSet.getInt("GROUP_ID"));
        query.setGroupName(resultSet.getString("GROUP_NAME"));
        query.setCreateDate(resultSet.getDate("CREATE_DATE"));
        query.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        String variable = resultSet.getString("VARIABLE_INFO");
        if (variable != null) {
            List<Object> variableInfo = new ArrayList<>();
            try {
                JSONArray array = new JSONArray(variable);
                Util.setListFromJson(variableInfo, array);
                query.setVariableInfo(variableInfo);
            } catch (Exception e) {}
        }
        return query;
    }
}
