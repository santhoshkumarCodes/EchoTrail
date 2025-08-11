package com.echotrail.serviceregistry.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataCenterInfo {
    @JsonProperty("@class")
    private String className;
    private String name;

    public DataCenterInfo(String className, String name) {
        this.className = className;
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
