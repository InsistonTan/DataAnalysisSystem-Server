package com.Tan.domain;
import java.io.Serializable;
/*
 * @Description:用于存放变量的数据，包括变量名、数据（double型和String型）、变量类型
 * @author:TanJifeng
 * @date:null
 * */
public class InputData implements Serializable {
    private String head;
    private String[] heads;
    private double[] data;
    private String type;
    private String[] strData;

    public String[] getStrData() {
        return strData;
    }

    public void setStrData(String[] strData) {
        this.strData = strData;
    }

    public String[] getHeads()
    {
        return heads;
    }

    public void setHeads(String[] heads)
    {
        this.heads = heads;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public double[] getData() {
            return data;
        }
    public void setData(double[] data) {
        this.data = data;
    }
}
