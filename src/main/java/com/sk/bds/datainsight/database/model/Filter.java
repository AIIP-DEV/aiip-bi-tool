package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

@Data
public class Filter implements RowMapper<Filter>, Serializable {
    public int id;
    public int groupId;
    public String column;
    public String condition;
    public String value1;
    public String value2;
    public boolean enable;
    public Date updateDate;
    public Date createDate;


    public Filter() {}

    public Filter(int groupId, String column, String condition, Object value1, Object value2, boolean enable) {
        this.groupId = groupId;
        this.column = column;
        this.condition = condition;
        this.value1 = value1 != null ? value1.toString() : null;
        this.value2 = value2 != null ? value2.toString() : null;
        this.enable = enable;
    }

    public boolean getEnable() {
        return enable;
    }

    public Filter mapRow(ResultSet resultSet, int i) throws SQLException {
        Filter filter = new Filter();
        filter.setId(resultSet.getInt("ID"));
        filter.setGroupId(resultSet.getInt("GROUP_ID"));
        filter.setColumn(resultSet.getString("COLUMN"));
        filter.setCondition(resultSet.getString("CONDITION"));
        filter.setValue1(resultSet.getString("VALUE1"));
        filter.setValue2(resultSet.getString("VALUE2"));
        try {
            filter.setEnable("true".equals(resultSet.getString("ENABLE")));
        } catch (Exception e) {
            filter.setEnable(true);
        }
        filter.setCreateDate(resultSet.getDate("CREATE_DATE"));
        try {
            filter.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        } catch (Exception e) {}
        return filter;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("GROUP_ID", groupId);
        param.put("COLUMN", column);
        param.put("CONDITION", condition);
        param.put("VALUE1", value1);
        param.put("VALUE2", value2);
        param.put("ENABLE", "" + enable);
        Date date = new Date();
        param.put("UPDATE_DATE", date);
        param.put("CREATE_DATE", date);
        return new MapSqlParameterSource(param);
    }
}
