package com.sk.bds.datainsight.database.model;

import com.sk.bds.datainsight.util.Util;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Data
public class DrawInfo {
    private HashMap<String, Object> option;

    public DrawInfo(JSONObject info) throws Exception{
        if (info.has("option")) {
            option = new HashMap<>();
            Util.setMapFromJson(option, info.getJSONObject("option"));
        }
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("option", option);
        } catch (Exception e) {}
        return obj.toString();
    }
}
