package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ScheduleDetail {
    @JsonIgnore
    private String startTime;
    @JsonIgnore
    private String endTime;
    private String stepName;
    private String stepStart;
    private String stepEnd;
    private String status;
    private String message;
}
