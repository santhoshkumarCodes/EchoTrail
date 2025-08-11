package com.echotrail.serviceregistry.model;

public class InstanceInfo {
    private String hostName;
    private String app;
    private String ipAddr;
    private String status;
    private Port port;
    private DataCenterInfo dataCenterInfo;

    public InstanceInfo(String hostName, String app, String ipAddr, String status, Port port, DataCenterInfo dataCenterInfo) {
        this.hostName = hostName;
        this.app = app;
        this.ipAddr = ipAddr;
        this.status = status;
        this.port = port;
        this.dataCenterInfo = dataCenterInfo;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public DataCenterInfo getDataCenterInfo() {
        return dataCenterInfo;
    }

    public void setDataCenterInfo(DataCenterInfo dataCenterInfo) {
        this.dataCenterInfo = dataCenterInfo;
    }
}
