package com.pancard.android.model;

import android.graphics.drawable.Drawable;

public class Category {

    private String name;
    private Drawable drawableBackground;
    private Drawable backgroundLL;

    public Category(String name, Drawable drawableBackground, Drawable backgroundLL) {
        this.name = name;
        this.drawableBackground = drawableBackground;
        this.backgroundLL = backgroundLL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getDrawableBackground() {
        return drawableBackground;
    }

    public void setDrawableBackground(Drawable drawableBackground) {
        this.drawableBackground = drawableBackground;
    }

    public Drawable getBackgroundLL() {
        return backgroundLL;
    }

    public void setBackgroundLL(Drawable backgroundLL) {
        this.backgroundLL = backgroundLL;
    }

}
