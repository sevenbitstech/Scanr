package com.pancard.android.validation_class;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by seven-bits-pc11 on 19/5/17.
 */
public class ReadImage {

    public static String createCameraSource(Bitmap bitmap, Context context) {

        ArrayList<Integer> topBlock = new ArrayList<>();     // rect top edge
        ArrayList<String> blocks = new ArrayList<>();


        ArrayList<Integer> copy_topBlock = new ArrayList<>();
        String text = "";

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        Frame imageFrame = new Frame.Builder()

                .setBitmap(bitmap)
                .build();

        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            for (int k = 0; k < textBlock.getComponents().size(); k++) {
                topBlock.add(textBlock.getComponents().get(k).getBoundingBox().top);
                copy_topBlock.add(textBlock.getComponents().get(k).getBoundingBox().top);
                blocks.add(textBlock.getComponents().get(k).getValue());
                System.out.println("Read text from image := " + textBlock.getComponents().get(k).getValue());
            }
        }

        ArrayList<Integer> sequnce = new ArrayList<>();

        for (int i = 0; i < topBlock.size(); i++) {

            int min = Collections.min(copy_topBlock);
            sequnce.add(topBlock.indexOf(min));
            copy_topBlock.remove(copy_topBlock.indexOf(min));
        }

        for (int j = 0; j < sequnce.size(); j++) {
            String textBlock = blocks.get(sequnce.get(j));

            text += " " + textBlock;

            text += "\n";
        }
        return text;
    }


}
