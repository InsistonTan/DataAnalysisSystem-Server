package com.Tan.domain;

import java.io.Serializable;
import java.util.List;

public class RequestData implements Serializable {
    private String action;//前端请求的操作
    private List<InputData> dataList;//前端数据列表
    private double singleData;//前端单个数据（用于某些特殊的单个参数）

    public double getSingleData() {
        return singleData;
    }

    public void setSingleData(double singleData) {
        this.singleData = singleData;
    }

    public List<InputData> getDataList() {
        return dataList;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDataList(List<InputData> dataList) {
        this.dataList = dataList;
    }
}
