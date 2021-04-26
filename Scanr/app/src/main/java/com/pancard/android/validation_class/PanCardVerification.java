package com.pancard.android.validation_class;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.pancard.android.activity.otheracivity.CommonScan;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by seven-bits-pc11 on 2/2/17.
 */
public class PanCardVerification {
    boolean Pdate = false;
    boolean Pnumber = false;
    boolean Pname = false;
    Context context;

    public PanCardVerification(Context context) {
        this.context = context;
    }


    public boolean PancardVerification(Bitmap detectbitmap) {

        Pnumber = false;
        Pdate = false;
        Pname = false;

        // A text recognizer is created to find text.  An associated multi-processor instance
        // is set to receive the text recognition results, track the text, and maintain
        // graphics for each text block on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each text block.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        Frame imageFrame = new Frame.Builder()

                .setBitmap(detectbitmap)
                .build();

        String imageText = null;
        android.util.Log.i("text detect from image", " read text");

        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            if (!Pnumber) {
                pancardNumberVerify(textBlock.getValue(), textBlock);
            }

            if (!Pdate) {
                pancardDateVerify(textBlock);
            }

            android.util.Log.i("text detect from image", textBlock.getValue());
            android.util.Log.i("text getBoundingBox", String.valueOf(textBlock.getBoundingBox()));

        }

        if (Pnumber && Pdate) {

            cutNameImage(detectbitmap);
        }


        if (Pnumber && Pdate && Pname) {

            return true;

        }

        if (!textRecognizer.isOperational()) {

            android.util.Log.w("Reader", "Detector dependencies are not yet available.");

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = context.registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {

            }
        }
        return false;
    }

    private Bitmap cutNameImage(Bitmap origialBitmap) {

        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth() - 150,
                origialBitmap.getHeight() / 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(5, origialBitmap.getHeight() / 4 - 10, origialBitmap.getWidth() - 150, origialBitmap.getHeight() / 3 + 25);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);

//        id_pan_name.setImageBitmap(cutBitmap);


        String pancardname = createCameraSource(cutBitmap);
//         pancardname += createCameraSource(newContrasBitmap);

//        id_pan_name.setImageBitmap(newContrasBitmap);

        if (pancardname.length() > 5) {

            if (pancardname.contains("GOVT") || pancardname.contains("INCOM") || pancardname.contains("TAX") || pancardname.contains("/") || pancardname.contains("TMENT")) {

//                duplicateName_value(oldBitmap);

            } else {
                Pname = true;
                CommonScan.CARD_HOLDER_NAME = pancardname.trim().replaceAll("\\d", "");
                CommonScan.CARD_HOLDER_NAME = CommonScan.CARD_HOLDER_NAME.replaceAll("['-+.^`~:,@&(*)#$!%]", "");
                CommonScan.CARD_HOLDER_NAME = Normalizer.normalize(CommonScan.CARD_HOLDER_NAME, Normalizer.Form.NFD);
                CommonScan.CARD_HOLDER_NAME = CommonScan.CARD_HOLDER_NAME.replaceAll("[^\\p{ASCII}]", "");
            }
        } else {

        }
        return cutBitmap;
    }

    private String createCameraSource(Bitmap bitmap) {

        String imageText = "";
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();


        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
//            imageText += textBlock.getValue();
            imageText = textBlock.getComponents().get(0).getValue().toString();
            System.out.println("cut name detect name:== " + textBlock.getValue());
        }


        if (!textRecognizer.isOperational()) {

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = context.registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {

            }
        }
        return imageText;
    }


    public void pancardNumberVerify(String num, TextBlock textBlock) {
        Pattern pattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");

        Matcher matcher = pattern.matcher(num);

// Check if pattern matches
        if (matcher.find()) {
//         System.out.println("Pancard number :=== " + matcher.group(0));
            CommonScan.CARD_UNIQE_NO = matcher.group(0);
            Pnumber = true;
        }
    }

    public boolean pancardDateVerify(TextBlock textBlock) {


        for (int i = 0; i < textBlock.getComponents().size(); i++) {

            if (textBlock.getComponents().get(i).getValue().contains("/")) {


                if (textBlock.getComponents().get(i).getValue().length() == 10) {
                    if (validate(textBlock.getComponents().get(i).getValue())) {
//                        Pancard.pancard_date = textBlock.getComponents().get(i).getValue();
                        CommonScan.CARD_HOLDER_DOB = textBlock.getComponents().get(i).getValue();
                        System.out.println("Pancard date :=== " + textBlock.getComponents().get(i).getValue());
//                        Pancard_detail.ract.add(textBlock.getBoundingBox());
                        Pdate = true;

                        return true;
                    }
                } else if (textBlock.getComponents().get(i).getValue().length() == 11) {
                    if (validate(textBlock.getComponents().get(i).getValue().substring(1))) {
//                        Pancard.pancard_date = textBlock.getComponents().get(i).getValue().substring(1);
                        System.out.println("Pancard date :=== " + textBlock.getComponents().get(i).getValue().substring(1));
                        CommonScan.CARD_HOLDER_DOB = textBlock.getComponents().get(i).getValue().substring(1);
//                        Pancard_detail.ract.add(textBlock.getBoundingBox());
                        Pdate = true;

                        return true;
                    }
                }

            }
        }
        return false;
    }

    public boolean validate(String date) {

        String DATE_VALIDATION_PATTERN = "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)";
        Pattern pattern = Pattern.compile(DATE_VALIDATION_PATTERN);

        Matcher matcher = pattern.matcher(date);

        if (matcher.matches()) {

            matcher.reset();

            if (matcher.find()) {
                return true;

            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
