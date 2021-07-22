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
import java.util.Map;

@Data
public class Group implements RowMapper<Group>, Serializable {

    private int id;
    private String name;
    private String description;
    private String typeCnt;
    private Date createDate;
    private Date updateDate;

    public Group() {}

    public Group(Map<String, Object> param) {
        this.name = String.valueOf(param.get("name"));
        this.description = String.valueOf(param.get("description"));
    }

    public Group mapRow(ResultSet resultSet, int i) throws SQLException {
        Group group = new Group();
        group.setId(resultSet.getInt("ID"));
        group.setName(resultSet.getString("NAME"));
        group.setDescription(resultSet.getString("DESCRIPTION"));
        group.setTypeCnt(resultSet.getString("TYPE_CNT"));
        group.setCreateDate(resultSet.getDate("CREATE_DATE"));
        group.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        return group;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("NAME", name);
        param.put("DESCRIPTION", description);
        Date date = new Date();
        param.put("UPDATE_DATE", date);
        param.put("CREATE_DATE", date);
        return new MapSqlParameterSource(param);
    }
}
