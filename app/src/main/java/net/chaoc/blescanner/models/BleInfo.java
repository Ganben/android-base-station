package net.chaoc.blescanner.models;

import java.util.Date;

/**
 * 蓝牙设备信息
 * Created by yejun on 10/29/16.
 * Copyright (C) 2016 qinyejun
 */

public class BleInfo {
    private String name;          //设备名
    private int rssi;             //信号强度
    private String payload;       //自定义内容
    private Date timestamp;       //扫描时间

    public BleInfo(String name, int rssi, String payload, Date timestamp) {
        this.name = name;
        this.rssi = rssi;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {

        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
