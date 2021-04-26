package com.pancard.android.database;

public enum FileVersion {
    CROPPED("Cropped"),
    ORIGINAL("Original"),
    INFO("Info"),
    DELETED("Deleted");

    String stringValue;

    FileVersion(String toString) {
        stringValue = toString;
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
