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
public class Dashboard implements RowMapper<Dashboard>, Serializable {

    private int id;
    private String name;
    private String subName;
    private String image;
    private Integer cntDataSet;
    private Integer cntChart;
    private Integer cntQuery;
    private String createUser;
    private Date updateDate;
    private Date createDate;
    private String groupName;
    private Integer groupId;

    public Dashboard() {}

    public Dashboard(Map<String, Object> param) {
        name = (String)param.get("name");
        subName = (String)param.get("subName");
        image = (String)param.get("thumbImg");
        createUser = (String)param.get("createUser");
    }

    public Dashboard mapRow(ResultSet resultSet, int i) throws SQLException {
        Dashboard dashboard = new Dashboard();
        dashboard.setId(resultSet.getInt("ID"));
        dashboard.setName(resultSet.getString("NAME"));
        dashboard.setSubName(resultSet.getString("SUB_NAME"));
        dashboard.setImage(resultSet.getString("THUMB_IMG"));
        dashboard.setCntDataSet(resultSet.getInt("CNT_DATA_SET"));
        dashboard.setCntChart(resultSet.getInt("CNT_CHART"));
        dashboard.setCntQuery(resultSet.getInt("CNT_QUERY"));
        dashboard.setCreateUser(resultSet.getString("CREATE_USER"));
        dashboard.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        dashboard.setCreateDate(resultSet.getDate("CREATE_DATE"));
        dashboard.setGroupName(resultSet.getString("GROUP_NAME"));
        dashboard.setGroupId(resultSet.getInt("G_ID"));
        return dashboard;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("NAME", name);
        param.put("SUB_NAME", subName);
        param.put("THUMB_IMG", image);
        param.put("CREATE_USER", createUser);
        Date date = new Date();
        param.put("UPDATE_DATE", date);
        param.put("CREATE_DATE", date);
        return new MapSqlParameterSource(param);
    }
}
