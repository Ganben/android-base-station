package net.chaoc.blescanner.models;

/**
 * Created by yejun on 11/1/16.
 * Copyright (C) 2016 qinyejun
 */

public class YunbaPayload {
    private String apid;
    private String payload;
    private int rssi;

    public YunbaPayload(String apid, String payload, int rssi) {
        this.apid = apid;
        this.payload = payload;
        this.rssi = rssi;
    }

    public String getApid() {
        return apid;
    }

    public void setApid(String apid) {
        this.apid = apid;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
