package com.pancard.android.validation_class;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.pancard.android.activity.otheracivity.CommonScan;


/**
 * Created by seven-bits-pc11 on 2/2/17.
 */
public class DocumentVerification {
    boolean Dscan = false;
    Context context;

    public DocumentVerification(Context context) {
        this.context = context;
        CommonScan.CARD_HOLDER_NAME = "";
    }

    public boolean documentVerification(Bitmap detectbitmap) {
        CommonScan.CARD_HOLDER_NAME = "";
        SparseArray<TextBlock> textBlocks = createCameraSource(detectbitmap);
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            CommonScan.CARD_HOLDER_NAME += "\n" + textBlock.getValue();
            System.out.println("detect text block :== " + textBlock.getValue());
        }

        if (CommonScan.CARD_HOLDER_NAME.trim().length() > 3) {
            return true;
        }
        return false;
    }

    private SparseArray<TextBlock> createCameraSource(Bitmap bitmap) {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();


        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
        return textBlocks;
    }
}
