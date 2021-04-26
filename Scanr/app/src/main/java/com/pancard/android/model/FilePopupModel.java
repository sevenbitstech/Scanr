package com.pancard.android.model;

import android.graphics.drawable.Drawable;

public class FilePopupModel {
    public long id;
    public Drawable iconRes;
    public String category;

    public FilePopupModel(Drawable iconRes, String category) {
        this.iconRes = iconRes;
        this.category = category;
    }
}
