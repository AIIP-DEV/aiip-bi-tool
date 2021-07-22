package com.sk.bds.datainsight.database.model;

import lombok.Data;
import org.springframework.jdbc.core.RowMapper;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

@Data
public class ChartInfo implements RowMapper<ChartInfo>, Serializable {

    private int id;
    private String name;
    private String type;
    private String[] value;

    public ChartInfo mapRow(ResultSet resultSet, int i) throws SQLException {
        ChartInfo chartInfo = new ChartInfo();
        chartInfo.setId(resultSet.getInt("ID"));
        chartInfo.setName(resultSet.getString("NAME"));
        chartInfo.setType(resultSet.getString("TYPE"));
        chartInfo.setValue(resultSet.getString("VALUE").split(","));
        return chartInfo;
    }
}
