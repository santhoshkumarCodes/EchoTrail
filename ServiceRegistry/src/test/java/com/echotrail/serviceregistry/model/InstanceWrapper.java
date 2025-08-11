package com.echotrail.serviceregistry.model;

public class InstanceWrapper {
    private InstanceInfo instance;

    public InstanceWrapper(InstanceInfo instance) {
        this.instance = instance;
    }

    public InstanceInfo getInstance() {
        return instance;
    }

    public void setInstance(InstanceInfo instance) {
        this.instance = instance;
    }
}
