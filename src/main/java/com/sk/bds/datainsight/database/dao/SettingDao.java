package com.sk.bds.datainsight.database.dao;

import com.sk.bds.datainsight.database.model.AccessKey;
import com.sk.bds.datainsight.database.model.ScheduleDetail;
import com.sk.bds.datainsight.database.model.UseAccessKey;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface SettingDao {

    void insertAccessKey(AccessKey accessKey);

    void updateAccessKeyDescription(Map<String, Object> param);

    void updateAccessKeyStatus(Map<String, Object> param);

    List<AccessKey> getAccessKey(Map<String, Object> param);

    int getAccessKeyCount(Map<String, Object> param);

    void deleteAccessKey(List<String> keys);

    List<UseAccessKey> getUseAccessKeyInfo(String key);

    long insertExportChart(Map<String, Object> param);

    void insertAccessKeyLog(Map<String, Object> param);

    void updateExportChart(int exportId);

    String selectAccessKeyStatus(String accessKey);

    Map<String, Object> getDataSetSchedule(Map<String, Object> param);

    List<Map<String, Object>> getDataSetScheduleList(Map<String, Object> param);

    void deleteDataSetSchedule(String id);

    void insertDataSetSchedule(Map<String, Object> param);

    List<ScheduleDetail> getDataSetScheduleDetail(int id);

    int selectDataSetScheduleOnetime(Map<String, Object> param);

    void insertDataSetScheduleOnetime(Map<String, Object> param);

    void deleteDataSetScheduleOnetime(String id);

    List<ScheduleDetail> getDataSetOnetimeScheduleDetail(int id);

    List<Map<String, Object>> getDataSetScheduleCount(List<Integer> param);

    void updateScheduleOnetime(Map<String, Object> param);

}
