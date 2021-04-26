package com.pancard.android.liveedgedetection.interfaces;

import android.graphics.Bitmap;

import com.pancard.android.liveedgedetection.enums.ScanHint;

public interface IScanner {
    void displayHint(ScanHint scanHint);

    void onPictureClicked(Bitmap bitmap);

    void onExceptionHandled(Exception e);

    void onOutOfMemory(OutOfMemoryError outOfMemoryError);
}