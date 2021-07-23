package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.bds.datainsight.util.Util;
import lombok.Data;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Options {
    Map<String, Object> option;
    public Options(JSONObject jsonObject) {
        option = new HashMap<>();
        try {
            Util.setMapFromJson(option, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Options(Object object) {
        option = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.convertValue(object, Map.class);
    }

    public Options setOptions(List<Map<String, Object>> maps) {
        Map<String, Object> newMap = new HashMap<>();
        for(Map<String, Object> element: maps) {
            System.out.println("element = " + element.keySet());
//            for(Map.Entry<String, Object> entry : element.entrySet()) {
//                newMap.put(entry.getKey(), entry.getValue());
//            }
        }
        return new Options(newMap);
    }

    public String toString() {
        return new JSONObject(option).toString();
    }
}
