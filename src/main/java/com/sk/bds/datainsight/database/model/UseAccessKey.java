package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

@Data
public class UseAccessKey implements RowMapper<UseAccessKey>, Serializable {
    String title;
    @JsonIgnore
    int chartId;
    String export;
    String lastUsed;

    @Nullable
    @Override
    public UseAccessKey mapRow(ResultSet resultSet, int i) throws SQLException {
        UseAccessKey useAccessKey = new UseAccessKey();
        useAccessKey.setChartId(resultSet.getInt("ANALYSIS_CHART_ID"));
        useAccessKey.setExport(resultSet.getString("CREATE_DATE"));
        useAccessKey.setLastUsed(resultSet.getString("LAST_USED_DATE"));
        return useAccessKey;
    }
}
