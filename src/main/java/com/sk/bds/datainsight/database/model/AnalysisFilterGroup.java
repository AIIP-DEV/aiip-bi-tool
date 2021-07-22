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
public class AnalysisFilterGroup extends FilterGroup implements RowMapper<AnalysisFilterGroup>, Serializable {
    public int analysisId;

    public AnalysisFilterGroup() {}

    public AnalysisFilterGroup(int analysisId, String name, boolean enable) {
        this.analysisId = analysisId;
        this.name = name;
        this.enable = enable;
    }

    public AnalysisFilterGroup mapRow(ResultSet resultSet, int i) throws SQLException {
        AnalysisFilterGroup filterGroup = new AnalysisFilterGroup();
        filterGroup.setId(resultSet.getInt("ID"));
        filterGroup.setName(resultSet.getString("NAME"));
        filterGroup.setAnalysisId(resultSet.getInt("ANALYSIS_ID"));
        filterGroup.setEnable("true".equals(resultSet.getString("ENABLE")));
        filterGroup.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        filterGroup.setCreateDate(resultSet.getDate("CREATE_DATE"));
        return filterGroup;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("ANALYSIS_ID", analysisId);
        param.put("NAME", name);
        param.put("ENABLE", "" + enable);
        Date date = new Date();
        param.put("UPDATE_DATE", date);
        param.put("CREATE_DATE", date);
        return new MapSqlParameterSource(param);
    }
}
