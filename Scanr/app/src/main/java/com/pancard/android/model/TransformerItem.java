package com.pancard.android.model;

import androidx.viewpager.widget.ViewPager;

public class TransformerItem {

    final String title;

    final Class<? extends ViewPager.PageTransformer> clazz;

    public TransformerItem(Class<? extends ViewPager.PageTransformer> clazz) {

        this.clazz = clazz;

        title = clazz.getSimpleName();

    }

    public Class<? extends ViewPager.PageTransformer> getClazz() {

        return clazz;

    }

    @Override

    public String toString() {

        return title;

    }

}
