package com.echotrail.serviceregistry.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Port {
    @JsonProperty("@enabled")
    private String enabled;
    @JsonProperty("$")
    private String port;

    public Port(String enabled, String port) {
        this.enabled = enabled;
        this.port = port;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
