package com.Tan.domain;
import java.io.Serializable;

/**
 * @author TanJifeng
 * @Description 用户上传的数据文件实体类
 * @date 2020/6/14 19:38
 */
public class SaveFile implements Serializable {
    private int index;//主键
    private String uid;//上传者的uid
    private String filename;//文件名
    private String url;//存储的文件地址（R可识别的地址，如：C://A//*.*）
    private String time;//上传时间

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
