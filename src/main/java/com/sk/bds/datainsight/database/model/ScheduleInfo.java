package com.sk.bds.datainsight.database.model;

import lombok.Data;

@Data
public class ScheduleInfo {
    private String target;
    private String src;
    private String srcType;

    public ScheduleInfo(DataSet dataSet) {
        src = dataSet.getSrcConnection();
        srcType = dataSet.getSrcType();
    }
}
