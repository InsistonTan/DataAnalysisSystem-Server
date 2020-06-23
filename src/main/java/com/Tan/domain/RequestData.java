package com.Tan.domain;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
/*
* @Description:用于接收前端的复杂数据
* @author:TanJifeng
* @date:null
* */
public class RequestData implements Serializable {
    private String action;//前端请求的操作
    private List<InputData> dataList;//前端数据列表
    private double singleData;//前端单个数据（用于某些特殊的单个参数）
    private String singleStr;//（用于某些特殊的单个参数）
    private Map mapData;//用于某些特殊的参数

    public String getSingleStr() {
        return singleStr;
    }

    public void setSingleStr(String singleStr) {
        this.singleStr = singleStr;
    }

    public Map getMapData() {
        return mapData;
    }

    public void setMapData(Map mapData) {
        this.mapData = mapData;
    }

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
