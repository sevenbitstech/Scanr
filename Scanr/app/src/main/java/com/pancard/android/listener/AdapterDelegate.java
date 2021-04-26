package com.pancard.android.listener;

import android.view.View;

public interface AdapterDelegate {

    /**
     * Interface for Recycler View Click listener
     **/

    void onClick(View view, int position);

//    void onClicked(View view, Object object);

    void onClicked(View view, Object object, int position);

    void onLongClick(View view, Object object, int position);

//    void onLongClicked(View view, Object object);
}