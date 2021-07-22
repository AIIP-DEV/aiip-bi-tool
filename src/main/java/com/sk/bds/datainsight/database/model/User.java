package com.sk.bds.datainsight.database.model;

import lombok.Data;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class User implements RowMapper<User> {
    private int id;
    private String ssoId;
    private int limitSize;
    private String dbUrl;
    private String dbPort;
    private String dbId;
    private String dbPwd;
    private String dbName;
    private String delYn;
    private Date updateDate;
    private Date createDate;

    public User mapRow(ResultSet resultSet, int i) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("ID"));
        user.setSsoId(resultSet.getString("SSO_ID"));
        user.setLimitSize(resultSet.getInt("LIMIT_SIZE"));
        user.setDbUrl(resultSet.getString("DB_URL"));
        user.setDbPort(resultSet.getString("DB_PORT"));
        user.setDbId(resultSet.getString("DB_ID"));
        user.setDbPwd(resultSet.getString("DB_PWD"));
        user.setDbName(resultSet.getString("DB_NAME"));
        user.setDelYn(resultSet.getString("DEL_YN"));
        user.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        user.setCreateDate(resultSet.getDate("CREATE_DATE"));
        return user;
    }
}
