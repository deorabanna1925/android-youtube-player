package com.deorabanna1925.youtubeplayer.model;

public class Notes {

    private String stamp;
    private String time;
    private String text;

    public Notes() {
    }

    public Notes(String stamp, String time, String text) {
        this.stamp = stamp;
        this.time = time;
        this.text = text;
    }

    public String getStamp() {
        return stamp;
    }

    public void setStamp(String stamp) {
        this.stamp = stamp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}