package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Data
public class DataSet implements RowMapper<DataSet>, Serializable {
    private int id;
    private String name;
    private String srcType;
    @JsonIgnore
    private String srcConnection;
    private String dataTable;
    private String useColumns;
    private int groupId;
    private String groupName;
    private Date updateDate;
    private Date createDate;
    @JsonIgnore
    private String originalColumn;
    @JsonIgnore
    private String changeColumn;
    private HashMap<String, Object> conInfo;
    private int scheduleCount;
    private int status;

    public DataSet() {}

    public DataSet(String name, String srcType, String srcConnection, String dataTable, String useColumns,
                   List<List<String>> originalInfo, List<List<String>> changeInfo, boolean isUpdate) {
        this.name = name;
        this.srcType = srcType;
        this.srcConnection = srcConnection;
        this.dataTable = dataTable;
        this.useColumns = useColumns;
        this.status = isUpdate ? 1 : 0;
        setOriginalInfo(originalInfo);
        setChangeInfo(changeInfo);
    }

    public void setOriginalInfo(List<List<String>> originalInfo) {
        JSONArray original = new JSONArray();
        for (List<String> list : originalInfo) {
            original.put(new JSONArray(list));
        }
        this.originalColumn = original.toString().replaceAll("\"", "");
    }

    public void setChangeInfo(List<List<String>> changeInfo) {
        JSONArray change = new JSONArray();
        for (List<String> list : changeInfo) {
            change.put(new JSONArray(list));
        }
        this.changeColumn = change.toString().replaceAll("\"", "");
    }

    public void setSrcConnection(String srcConnection) {
        this.srcConnection = srcConnection;
        conInfo = new HashMap<>();
        try {
            JSONObject obj = new JSONObject(srcConnection);
            Iterator<String> iterator = obj.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                conInfo.put(key, obj.get(key));
            }
        } catch (Exception e) {}
    }

    public DataSet mapRow(ResultSet resultSet, int i) throws SQLException {
        DataSet dataSet = new DataSet();
        dataSet.setId(resultSet.getInt("ID"));
        dataSet.setName(resultSet.getString("NAME"));
        dataSet.setSrcType(resultSet.getString("SRC_TYPE"));
        dataSet.setSrcConnection(resultSet.getString("SRC_CONNECTION"));
        dataSet.setDataTable(resultSet.getString("DATA_TABLE"));
        dataSet.setUseColumns(resultSet.getString("USE_COLUMNS"));
        dataSet.setOriginalColumn(resultSet.getString("ORIGINAL_COLUMN"));
        dataSet.setChangeColumn(resultSet.getString("CHANGE_COLUMN"));
        dataSet.setUpdateDate(resultSet.getDate("UPDATE_DATE"));
        dataSet.setCreateDate(resultSet.getDate("CREATE_DATE"));
        dataSet.setGroupName(resultSet.getString("GROUP_NAME"));
        dataSet.setGroupId(resultSet.getInt("GROUP_ID"));
        dataSet.setStatus(resultSet.getInt("STATUS"));
        return dataSet;
    }

    @JsonIgnore
    public MapSqlParameterSource getParameter(boolean useId) {
        HashMap<String, Object> param = new HashMap<>();
        if (useId) {
            param.put("ID", id);
        }
        param.put("NAME", name);
        param.put("SRC_TYPE", srcType);
        param.put("SRC_CONNECTION", srcConnection);
        param.put("DATA_TABLE", dataTable);
        param.put("USE_COLUMNS", useColumns);
        param.put("ORIGINAL_COLUMN", originalColumn);
        param.put("CHANGE_COLUMN", changeColumn);
        param.put("STATUS", status);
        Date date = new Date();
        param.put("UPDATE_DATE", date);
        param.put("CREATE_DATE", date);
        return new MapSqlParameterSource(param);
    }
}
