package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class Report implements RowMapper<Report>, Serializable {

    private int id;
    private String name;
    private String thumbImg;
    private Date createDate;
    private Date updateDate;
    private String groupName;
    private Integer groupId;
    private String createUser;

    public Report() {}

    public Report(Map<String, Object> param) {
        name = param.get("name").toString();
        thumbImg = param.get("thumbImg").toString();
        createUser = param.get("createUser").toString();
    }

    public Report mapRow(ResultSet resultSet, int i) throws SQLException {
        Report report = new Report();
        report.setId(resultSet.getInt("ID"));
        report.setName(resultSet.getString("NAME"));
        report.setCreateDate(resultSet.getDate("CREATE_DATE"));
        report.setCreateDate(resultSet.getDate("UPDATE_DATE"));
        report.setGroupName(resultSet.getString("GROUP_NAME"));
        report.setGroupId(resultSet.getInt("GROUP_ID"));
        return report;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("NAME", name);
        param.put("THUMB_IMG", thumbImg);
        param.put("CREATE_USER", createUser);
        Date date = new Date();
        param.put("CREATE_DATE", date);
        param.put("UPDATE_DATE", date);
        return new MapSqlParameterSource(param);
    }
}
