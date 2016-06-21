package com.note.Model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/5/29.
 *
 */

/** 实现 Serializable 接口是为了能够序列化，在跳转的时候 传递 对象过去 onject */
public class ListViewBean implements Serializable {

    /** 一篇日记所  含有  的东西在这里设置 */

    /**
     * content : 日记内容
     * time : 日记发表时间
     * title : 日记标题
     */

    private String content;
    private String time;
    private String title;
    /**
     * id : 日记的id
     */

    private int id;

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
