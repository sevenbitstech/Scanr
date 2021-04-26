package com.pancard.android.model;

/**
 * Created by seven-bits-pc11 on 18/5/17.
 */
public class SizeDetail {

    public long totalSpace;
    public long availableSpace;
    public long usedSpace;

    public SizeDetail() {

    }

    public SizeDetail(long totalSpace, long availableSpace, long usedSpace) {
        this.totalSpace = totalSpace;
        this.availableSpace = availableSpace;
        this.usedSpace = usedSpace;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public long getAvailableSpace() {
        return availableSpace;
    }

    public void setAvailableSpace(long availableSpace) {
        this.availableSpace = availableSpace;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }
}
