package com.Tan.domain;
import java.io.Serializable;

public class InputData implements Serializable {
    private String head;
    private double[] data;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[] getData() {
        return data;
    }

    public String getHead() {
        return head;
    }

    public void setData(double[] data) {
        this.data = data;
    }

    public void setHead(String head) {
        this.head = head;
    }
}
