package com.sk.bds.datainsight.database.dao;

import com.sk.bds.datainsight.database.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserDao {
    User getUserById(int id);

    User getUserBySsoId(String id);

    List<Map<String, Object>> getUsers(int id);

    String getUserDbName(int userId);

    void insertUser(Map<String, Object> param);

    List<Map<String, Object>> getTableData(String tableName);
}
