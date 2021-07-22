package com.sk.bds.datainsight.database.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class FilterGroup implements Serializable {
    public int id;
    public String name;
    public boolean enable;
    public Date updateDate;
    public Date createDate;
    public List<Filter> filter;

    public FilterGroup() {
        filter = new ArrayList<>();
    }

    public boolean getEnable() {
        return enable;
    }

    public void addFilter(List<Filter> filters) {
        for(Filter filter : filters) {
            if (filter.groupId == id) {
                this.filter.add(filter);
            }
        }
    }
}
