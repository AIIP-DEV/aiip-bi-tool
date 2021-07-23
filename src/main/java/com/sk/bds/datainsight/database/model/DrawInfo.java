package com.sk.bds.datainsight.database.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@NoArgsConstructor
public class DrawInfo {

    Map<String, Object> body;

    public DrawInfo(Map<String, Object> map) {
        this.body = map;
    }

}
