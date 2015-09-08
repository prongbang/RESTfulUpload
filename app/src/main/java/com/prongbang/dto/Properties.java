package com.prongbang.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Created by prongbang on 9/4/2015.
 */
public class Properties {
//    @SerializedName("filename")
    private String filename;
//    @SerializedName("fileBytes")
    private byte[] fileBytes;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }
}
