package com.sk.bds.datainsight.database.model;

import lombok.Data;

@Data
public class ValueInfo {

    private String axis;
    private String value;
    private String group;
    private String line;
    private String flag;

    public ValueInfo() {}

    public ValueInfo(String axis, String value, String group, String line, String flag) {
        this.axis = axis;
        this.value = value;
        this.group = group;
        this.line = line;
        this.flag = flag;
    }
}
