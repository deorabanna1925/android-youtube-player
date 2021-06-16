package com.deorabanna1925.youtubeplayer.model;

public class VideoLinks {
    private final int quality;
    private final String url;

    public VideoLinks(int quality, String url) {
        this.quality = quality;
        this.url = url;
    }

    public int getQuality() {
        return quality;
    }

    public String getUrl() {
        return url;
    }

}