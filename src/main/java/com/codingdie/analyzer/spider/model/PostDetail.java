package com.codingdie.analyzer.spider.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupeng on 17-7-18.
 */
public class PostDetail {
    private String title;
    private String author;
    private String time;
    private int  pageCount;
    private List<PostFloor> floors=new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<PostFloor> getFloors() {
        return floors;
    }

    public void setFloors(List<PostFloor> floors) {
        this.floors = floors;
    }
}
