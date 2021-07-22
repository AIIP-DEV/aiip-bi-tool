package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Data
public class DataSetFilterGroup extends FilterGroup implements RowMapper<DataSetFilterGroup>, Serializable {
    private int dataSetId;

    public DataSetFilterGroup() {
        super();
    }

    public DataSetFilterGroup(int dataSetId, String name, boolean enable) {
        super();
        this.dataSetId = dataSetId;
        this.name = name;
        this.enable = enable;
    }

    public DataSetFilterGroup mapRow(ResultSet resultSet, int i) throws SQLException {
        DataSetFilterGroup filterGroup = new DataSetFilterGroup();
        filterGroup.setId(resultSet.getInt("ID"));
        filterGroup.setName(resultSet.getString("NAME"));
        filterGroup.setDataSetId(resultSet.getInt("DATA_SET_ID"));
        filterGroup.setEnable("true".equals(resultSet.getString("ENABLE")));
        filterGroup.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        filterGroup.setCreateDate(resultSet.getDate("CREATE_DATE"));
        return filterGroup;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("DATA_SET_ID", dataSetId);
        param.put("NAME", name);
        param.put("ENABLE", "" + enable);
        Date date = new Date();
        param.put("UPDATE_DATE", date);
        param.put("CREATE_DATE", date);
        return new MapSqlParameterSource(param);
    }
}
