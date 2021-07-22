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
public class DashboardFilterGroup extends FilterGroup implements RowMapper<DashboardFilterGroup>, Serializable {

    private Integer dashboardDataSetId;
    private Integer dataSetId;
    private Integer queryId;
    private Date createDate;

    public DashboardFilterGroup() {}

    public DashboardFilterGroup(int dashboardDataSetId, Integer dataSetId, Integer queryId) {
        this.dashboardDataSetId = dashboardDataSetId;
        this.dataSetId = dataSetId;
        this.queryId = queryId;
    }

    public DashboardFilterGroup mapRow(ResultSet resultSet, int i) throws SQLException {
        DashboardFilterGroup filterGroup = new DashboardFilterGroup();
        filterGroup.setId(resultSet.getInt("ID"));
        filterGroup.setDashboardDataSetId(resultSet.getInt("DASHBOARD_DATA_SET_ID"));
        filterGroup.setDataSetId((Integer)resultSet.getObject("DATA_SET_ID"));
        filterGroup.setQueryId((Integer)resultSet.getObject("QUERY_ID"));
        filterGroup.setCreateDate(resultSet.getDate("CREATE_DATE"));
        filterGroup.setEnable(true);
        return filterGroup;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("DASHBOARD_DATA_SET_ID", dashboardDataSetId);
        param.put("DATA_SET_ID", dataSetId);
        param.put("QUERY_ID", queryId);
        Date date = new Date();
        param.put("CREATE_DATE", date);
        return new MapSqlParameterSource(param);
    }
}
