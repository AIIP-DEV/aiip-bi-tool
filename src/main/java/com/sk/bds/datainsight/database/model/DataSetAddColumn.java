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
public class DataSetAddColumn implements RowMapper<DataSetAddColumn>, Serializable {
    private int id;
    private int dataSetId;
    private String name;
    private String originalColumn;
    private String formula;
    private String type;
    private Date updateDate;
    private Date createDate;

    public DataSetAddColumn() {}

    public DataSetAddColumn(int dataSetId, String name, String originalColumn, String formula, String type) {
        this.dataSetId = dataSetId;
        this.name = name;
        this.originalColumn = originalColumn;
        this.formula = formula;
        this.type = type;
    }

    public DataSetAddColumn mapRow(ResultSet resultSet, int i) throws SQLException {
        DataSetAddColumn dataSet = new DataSetAddColumn();
        dataSet.setId(resultSet.getInt("ID"));
        dataSet.setDataSetId(resultSet.getInt("DATA_SET_ID"));
        dataSet.setName(resultSet.getString("NAME"));
        dataSet.setOriginalColumn(resultSet.getString("ORIGINAL_COLUMN"));
        dataSet.setFormula(resultSet.getString("FORMULA"));
        dataSet.setType(resultSet.getString("TYPE"));
        dataSet.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        dataSet.setCreateDate(resultSet.getDate("CREATE_DATE"));
        return dataSet;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("DATA_SET_ID", dataSetId);
        param.put("NAME", name);
        param.put("ORIGINAL_COLUMN", originalColumn);
        param.put("FORMULA", formula);
        param.put("TYPE", type);
        Date date = new Date();
        param.put("UPDATE_DATE", date);
        param.put("CREATE_DATE", date);
        return new MapSqlParameterSource(param);
    }
}
