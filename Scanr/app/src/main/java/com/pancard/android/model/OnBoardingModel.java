package com.pancard.android.model;

import android.graphics.drawable.Drawable;

public class OnBoardingModel {

    private Drawable drawable;
    private String description;
    private String title;

    public OnBoardingModel(Drawable drawable, String title, String description) {
        this.drawable = drawable;
        this.description = description;
        this.title = title;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
