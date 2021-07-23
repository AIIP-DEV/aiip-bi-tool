package com.sk.bds.datainsight.echart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.bds.datainsight.util.Constants;
import jnr.ffi.Struct;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
public class OptionHelper {
    private Map<String, Object> option;
    private String type;
    private List<Map<String, Object>> rawData; // db data

    public OptionHelper(Map<String, Object> option, String type) {
        this.option = option;
        this.type = type;
    }

    public static OptionHelper of(Map<String, Object> option, String type) {
        return new OptionHelper(option, type);
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.option);
    }

    public Map<String, Object> getXAxis() {
        return Optional.ofNullable((Map<String, Object>) option.get(Constants.ECHART_XAXIS)).orElse(new HashMap<>());
    }

    public Map<String, Object> getYAxis() {
        return Optional.ofNullable((Map<String, Object>) option.get(Constants.ECHART_YAXIS)).orElse(new HashMap<>());
    }

    public Map<String, Object> getSeries() {
        return Optional.ofNullable((Map<String, Object>) option.get(Constants.ECHART_SERIES)).orElse(new HashMap<>());
    }
    // series.data 구분
    public List<Map<String, Object>> getData() {
        return Optional.ofNullable((List<Map<String, Object>>) option.get(Constants.ECHART_SERIES)).orElse(new ArrayList<>());
    }

    public void setChartData(Object data, String insertTo) {
        // TODO insert data to option.insertTo
        Map<String, Object> obj = this.from(insertTo);
        obj.put("data", data);
    }

    public OptionHelper putWhereThen(String where, String key, Object value) {
        Map<String, Object> whereObj = (Map<String, Object>) option.get(where);
        whereObj.put(key, value);
        return this;
    }

    public OptionHelper putThen(String key, Object value) {
        option.put(key, value);
        return this;
    }


    public Map<String, Object> from(String objectKey) {
        // TODO get (key, value) from option
        return (Map<String, Object>) option.get(objectKey);
    }


}
