package com.pancard.android.validation_class;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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
public class DrivingLicenceValidation_2 {
    // Licence variable
    boolean bo_licence_date = false;
    boolean licence_issues_date = false;
    boolean licence_till_date = false;
    boolean bo_licence_number = false;
    String pancardname;

    Context context;

    public DrivingLicenceValidation_2(Context context) {
        this.context = context;
        CommonScan.CARD_ISSUE_DATE = "";
        CommonScan.CARD_ISSUE_ADDRESS = "";
        CommonScan.CARD_UNIQE_NO = "";
        CommonScan.CARD_HOLDER_NAME = "";
        CommonScan.CARD_HOLDER_DOB = "";
        CommonScan.CARD_TILL_DATE = "";
        pancardname = "";

    }

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public boolean licenceVerification(Bitmap detectbitmap) {

        cutUpperHalfImage(changeBitmapContrastBrightness(detectbitmap, 5, -100));
        cutTillDateImage(changeBitmapContrastBrightness(detectbitmap, 5, -100));
        cutUpperHalfImage(detectbitmap);
        cutTillDateImage(detectbitmap);
        cutDOBDateImage(detectbitmap);
        if (bo_licence_number && licence_issues_date && licence_till_date) {
            cutLicenceNameImage(detectbitmap);

            if (CommonScan.CARD_HOLDER_NAME.length() > 5) {
                return true;
            }
//            cutLicenceNameImage(changeBitmapContrastBrightness(detectbitmap,5,-100));

//            cutAddressPortionImage(detectbitmap);

        }
        return false;
    }

    public void licenceNumberVerify(TextBlock textBlock) {


        for (int i = 0; i < textBlock.getComponents().size(); i++) {

            if (!isDrivingLicence(textBlock.getComponents().get(i).getValue())) {

                if (!isDrivingLicenceSecond(textBlock.getComponents().get(i).getValue())) {

                    if (!isDrivingLicenceThird(textBlock.getComponents().get(i).getValue())) {

                    } else {
                        CommonScan.CARD_UNIQE_NO = textBlock.getComponents().get(i).getValue();
                        bo_licence_number = true;
                        System.out.println("cut name detect number:== " + textBlock.getComponents().get(i).getValue());

                        break;
                    }
                } else {
                    CommonScan.CARD_UNIQE_NO = textBlock.getComponents().get(i).getValue();
                    bo_licence_number = true;

                    System.out.println("cut name detect number:== " + textBlock.getComponents().get(i).getValue());

                    break;
                }

            } else {
                CommonScan.CARD_UNIQE_NO = textBlock.getComponents().get(i).getValue();
                bo_licence_number = true;

                System.out.println("cut name detect number:== " + textBlock.getComponents().get(i).getValue());

                break;
            }

        }

    }

    public boolean isDrivingLicence(String State) {
        String STATE_PATTERN = "[A-Z]{2}[0-9]{2}[\\s]{1}[0-9]{11}";
        Pattern pattern = Pattern.compile(STATE_PATTERN);
        Matcher matcher = pattern.matcher(State);
        return matcher.matches();
    }

    public boolean isDrivingLicenceSecond(String State) {
        String STATE_PATTERN = "[A-Z]{2}[-]{1}[0-9]{13}";
        Pattern pattern = Pattern.compile(STATE_PATTERN);
        Matcher matcher = pattern.matcher(State);
        return matcher.matches();
    }

    public boolean isDrivingLicenceThird(String State) {
        String STATE_PATTERN = "[A-Z]{2}[0-9]{2}[-]{1}[0-9]{4}[-]{1}[0-9]{7}";
        Pattern pattern = Pattern.compile(STATE_PATTERN);
        Matcher matcher = pattern.matcher(State);
        return matcher.matches();
    }

    private Bitmap cutLicenceNameImage(Bitmap origialBitmap) {

        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth() - 150,
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(10, origialBitmap.getHeight() / 2 + 50, origialBitmap.getWidth() - 150, origialBitmap.getHeight() / 2 + 150);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);


        SparseArray<TextBlock> textBlocks = createCameraSource(cutBitmap);
//         pancardname += createCameraSource(newContrasBitmap);
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
//            imageText += textBlock.getValue();
            pancardname = textBlock.getComponents().get(0).getValue().toString();
            System.out.println("cut name detect name:== " + textBlock.getValue());
            break;
        }

//        id_pan_name.setImageBitmap(newContrasBitmap);

        if (pancardname != null) {
            if (pancardname.length() > 5) {

                if (pancardname.contains("GOVT") || pancardname.contains("INCOM") || pancardname.contains("TAX") || pancardname.contains("/") || pancardname.contains("TMENT")) {

//                duplicateName_value(oldBitmap);

                } else {

                    CommonScan.CARD_HOLDER_NAME = pancardname.trim().replaceAll("\\d", "");
                    CommonScan.CARD_HOLDER_NAME = CommonScan.CARD_HOLDER_NAME.replaceAll("['-+.^`~:,@&(*)#$!%]", "");
                    CommonScan.CARD_HOLDER_NAME = Normalizer.normalize(CommonScan.CARD_HOLDER_NAME, Normalizer.Form.NFD);
                    CommonScan.CARD_HOLDER_NAME = CommonScan.CARD_HOLDER_NAME.replaceAll("[^\\p{ASCII}]", "");
                }
            }
        }
        return cutBitmap;
    }

    public boolean LicenceDateVerify(String value) {
        if (value.contains("-")) {
            value = value.trim().replaceAll("-", "/");
        }
        if (value.contains("/")) {
            if (value.length() == 10) {
                if (validate(value)) {

                    return true;
                }
            } else if (value.length() == 11) {
                if (validate(value.substring(1))) {
                    return true;
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

    private SparseArray<TextBlock> createCameraSource(Bitmap bitmap) {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();


        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
        return textBlocks;
    }

    public void cutUpperHalfImage(Bitmap origialBitmap) {
        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth(),
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(70, 0, origialBitmap.getWidth() / 2 + 20, origialBitmap.getHeight() / 2 - 25);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);

        SparseArray<TextBlock> textBlocks = createCameraSource(cutBitmap);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            System.out.println("call methods");
            if (!bo_licence_number) {
                licenceNumberVerify(textBlock);
            }
            if (!licence_issues_date) {
                for (int j = 0; j < textBlock.getComponents().size(); j++) {

                    licence_issues_date = LicenceDateVerify(textBlock.getComponents().get(j).getValue());

                    if (licence_issues_date) {
                        if (textBlock.getComponents().get(j).getValue().length() == 11) {
                            System.out.println("cut name detect issue:== " + textBlock.getComponents().get(j).getValue().substring(1));
                            CommonScan.CARD_ISSUE_DATE = textBlock.getComponents().get(j).getValue().substring(1);

                            break;
                        } else if (textBlock.getComponents().get(j).getValue().length() == 10) {
                            System.out.println("cut name detect issue:== " + textBlock.getComponents().get(j).getValue());

                            CommonScan.CARD_ISSUE_DATE = textBlock.getComponents().get(j).getValue();

                            break;
                        }
                    }
                }

                android.util.Log.i("text detect from image", textBlock.getValue());
                android.util.Log.i("text getBoundingBox", String.valueOf(textBlock.getBoundingBox()));
            }
        }
    }

    public void cutTillDateImage(Bitmap origialBitmap) {
        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth() - 150,
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(origialBitmap.getWidth() / 2, origialBitmap.getHeight() / 3 - 30, origialBitmap.getWidth(), origialBitmap.getHeight() / 2);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);

        SparseArray<TextBlock> textBlocks = createCameraSource(cutBitmap);


        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            System.out.println("call methods");
            if (!licence_till_date) {
                for (int j = 0; j < textBlock.getComponents().size(); j++) {

                    licence_till_date = LicenceDateVerify(textBlock.getComponents().get(j).getValue());

                    if (licence_till_date) {
                        if (textBlock.getComponents().get(j).getValue().length() == 11) {

                            CommonScan.CARD_TILL_DATE = textBlock.getComponents().get(j).getValue().substring(1);

                            System.out.println("cut name detect date:== " + textBlock.getComponents().get(j).getValue().substring(1));

                            break;
                        } else if (textBlock.getComponents().get(j).getValue().length() == 10) {
                            CommonScan.CARD_TILL_DATE = textBlock.getComponents().get(j).getValue();

                            System.out.println("cut name detect name:== " + textBlock.getComponents().get(j).getValue());

                            break;
                        }
                    }
                }

                android.util.Log.i("text detect from image", textBlock.getValue());
                android.util.Log.i("text getBoundingBox", String.valueOf(textBlock.getBoundingBox()));
            }
        }
    }

    public void cutDOBDateImage(Bitmap origialBitmap) {
        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth() - 150,
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(70, origialBitmap.getHeight() / 2 - 20, origialBitmap.getWidth() / 2 + 20, origialBitmap.getHeight() / 2 + 50);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);

        SparseArray<TextBlock> textBlocks = createCameraSource(cutBitmap);


        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            System.out.println("call methods");
            if (!bo_licence_date) {
                for (int j = 0; j < textBlock.getComponents().size(); j++) {

                    bo_licence_date = LicenceDateVerify(textBlock.getComponents().get(j).getValue());

                    if (bo_licence_date) {
                        if (textBlock.getComponents().get(j).getValue().length() == 11) {

                            CommonScan.CARD_HOLDER_DOB = textBlock.getComponents().get(j).getValue().substring(1);

                            System.out.println("cut name detect date:== " + textBlock.getComponents().get(j).getValue().substring(1));

                            break;
                        } else if (textBlock.getComponents().get(j).getValue().length() == 10) {
                            CommonScan.CARD_HOLDER_DOB = textBlock.getComponents().get(j).getValue();

                            System.out.println("cut name detect name:== " + textBlock.getComponents().get(j).getValue());

                            break;
                        }
                    }
                }

                android.util.Log.i("text detect from image", textBlock.getValue());
                android.util.Log.i("text getBoundingBox", String.valueOf(textBlock.getBoundingBox()));
            }
        }
    }

    public void cutAddressPortionImage(Bitmap origialBitmap) {
        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth() - 150,
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(75, origialBitmap.getHeight() / 2 + 100, origialBitmap.getWidth() - 150, origialBitmap.getHeight());

        canvas.drawBitmap(origialBitmap, desRect, desRect, null);

        SparseArray<TextBlock> textBlocks = createCameraSource(cutBitmap);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            System.out.println("call methods");
            CommonScan.CARD_ISSUE_ADDRESS += " " + textBlock.getValue();
            android.util.Log.i("cut name detect address", textBlock.getValue());
        }
    }
}
