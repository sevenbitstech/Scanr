package com.pancard.android.database;

import androidx.annotation.NonNull;

public enum SyncStatus {

    synced("synced"),
    unsynced("unsynced"),
    syncFailed("syncFailed"),
    metadataSyncFailed("metadataSyncFailed"),
    metadataUnsynced("metadataUnsynced"),
    deleted("deleted"),
    deletedDrive("deletedDrive"),
    deletedDriveFailed("deletedDriveFailed"),
    deletedMetaData("deletedMetaData"),
    updated("updated"),
    pdfSyncFailed("pdfSyncFailed");


    String status;

    SyncStatus(String strStatus) {
        status = strStatus;
    }

    @NonNull
    @Override
    public String toString() {
        return status;
    }
}
