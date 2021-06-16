package com.deorabanna1925.youtubeplayer.model;

public class Comments {

    private String authorImage;
    private String authorName;
    private String textOriginal;
    private String likeCount;
    private String publishedAt;

    public Comments() {
    }

    public Comments(String authorImage, String authorName, String textOriginal, String likeCount, String publishedAt) {
        this.authorImage = authorImage;
        this.authorName = authorName;
        this.textOriginal = textOriginal;
        this.likeCount = likeCount;
        this.publishedAt = publishedAt;
    }

    public String getAuthorImage() {
        return authorImage;
    }

    public void setAuthorImage(String authorImage) {
        this.authorImage = authorImage;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTextOriginal() {
        return textOriginal;
    }

    public void setTextOriginal(String textOriginal) {
        this.textOriginal = textOriginal;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
}