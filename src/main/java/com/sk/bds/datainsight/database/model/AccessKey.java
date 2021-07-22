package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class AccessKey implements RowMapper<AccessKey>, Serializable {

    private String id;
    @JsonIgnore
    private int userId;
    private String description;
    private String status;
    private String createDate;
    private int count;
    private String useDate;

    public AccessKey() {}

    public AccessKey(Map<String, Object> data) {
        id = UUID.randomUUID().toString().replaceAll("-", "");
        userId = (int)data.get("userId");
        description = (String)data.get("description");
        status = "1";
    }

    @Nullable
    @Override
    public AccessKey mapRow(ResultSet resultSet, int i) throws SQLException {
        AccessKey accessKey = new AccessKey();
        accessKey.setId(resultSet.getString("ID"));
        accessKey.setDescription(resultSet.getString("DESCRIPTION"));
        accessKey.setStatus("1".equals(resultSet.getString("STATUS")) ? "active" : "inactive");
        accessKey.setCreateDate(resultSet.getString("CREATE_DATE"));
        accessKey.setCount(resultSet.getInt("CNT"));
        accessKey.setUseDate(resultSet.getString("USE_DATE"));
        return accessKey;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("ID", id);
        param.put("USER_ID", userId);
        param.put("DESCRIPTION", description);
        param.put("STATUS", status);
        return new MapSqlParameterSource(param);
    }
}
