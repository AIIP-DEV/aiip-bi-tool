package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import com.google.gson.JsonElement;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.*;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Chart {

    public Integer id;
    public int chart;
    public String name;
    public String subName;
    @JsonIgnore
    public String axis;
    @JsonIgnore
    public String value;
    @JsonIgnore
    public String group;
    @JsonIgnore
    public String line;
    @JsonIgnore
    public String flag;
    public ValueInfo valueInfo;
    public Date createDate;
    public Object option;
    public String type;
    @JsonIgnore
    public Map<String, Object> data;


    @JsonIgnore
    public JSONObject drawInfo;


    public Map<String, Object> layout;

    public String thumbnail;
    public Integer analysisChartId;

}
