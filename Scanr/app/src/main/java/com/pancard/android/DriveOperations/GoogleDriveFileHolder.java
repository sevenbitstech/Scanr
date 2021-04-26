package com.pancard.android.DriveOperations;

import com.google.api.client.util.DateTime;

public class GoogleDriveFileHolder {

    public String id;
    public String name;
    public DateTime modifiedTime;
    public long size;
    public DateTime createdTime;
    public Boolean starred;
    public String webContentUrl;
    public String webViewUrl;
    public String mimeType;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Boolean getStarred() {
        return starred;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getWebContentUrl() {
        return webContentUrl;
    }

    public void setWebContentUrl(String webContentUrl) {
        this.webContentUrl = webContentUrl;
    }

    public String getWebViewUrl() {
        return webViewUrl;
    }

    public void setWebViewUrl(String webViewUrl) {
        this.webViewUrl = webViewUrl;
    }
}
