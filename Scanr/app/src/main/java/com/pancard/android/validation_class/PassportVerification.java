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
public class PassportVerification {

    boolean passportdate = false;
    boolean passportnumber = false;
    boolean passportsurname = false;
    boolean passportname = false;
    boolean passportthroughdate = false;
    boolean passporttilldate = false;
    boolean passportbirthplace = false;
    boolean passportissueplace = false;

    Context context;

    public PassportVerification(Context context) {

        CommonScan.PASSPORT_SURNAME = "";
        CommonScan.CARD_HOLDER_NAME = "";
        CommonScan.CARD_UNIQE_NO = "";
        CommonScan.CARD_HOLDER_DOB = "";
        CommonScan.CARD_ISSUE_DATE = "";
        CommonScan.CARD_TILL_DATE = "";
        CommonScan.CARD_ISSUE_ADDRESS = "";
        CommonScan.CARD_BIRTH_PLACE = "";
        this.context = context;
    }

    public boolean PassPortVerification(Bitmap detectbitmap, Bitmap scalbitmap) {

//        passportnumber = false;
//        passportdate = false;
//        passportsurname = false;
//        passportname = false;
//        passportthroughdate = false;
//        passporttilldate = false;
//        passportbirthplace = false;
//        passportissueplace = false;

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context.getApplicationContext()).build();

        Frame imageFrame = new Frame.Builder()

                .setBitmap(detectbitmap)
                .build();

        String imageText = null;
        android.util.Log.i("text detect from image", " read text");

        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            System.out.println("call methods");

            if (textBlock.getBoundingBox().top < detectbitmap.getHeight() / 2 + 50) {
                System.out.println("call methods");
                if (!passportnumber) {
                    passportNumberVerify(textBlock);
                }

                if (!passportdate) {
                    PassPortDateVerify(textBlock);
                }
            }
            android.util.Log.i("text detect from image", textBlock.getValue());
            android.util.Log.i("text getBoundingBox", String.valueOf(textBlock.getBoundingBox()));

        }

        if (passportnumber && passportdate) {

            System.out.println("both values are true");

            PassportSurnameImage(scalbitmap);
            PassportBirthCity(scalbitmap);
            PassportIssueCityImage(scalbitmap);
            PassportNameImage(scalbitmap);
            PassportDateImage(scalbitmap);
            PassportSecondDateImage(scalbitmap);

            return true;
        } else {
            System.out.println("some issue in both values are true");
            return false;
        }
    }


    public void passportNumberVerify(TextBlock textBlock) {

        Pattern pattern = Pattern.compile("(([a-zA-Z]{1})\\d{7})");
        for (int i = 0; i < textBlock.getComponents().size(); i++) {
            Matcher matcher = pattern.matcher(textBlock.getComponents().get(i).getValue().trim().replaceAll(" ", ""));

            if (matcher.find()) {
                System.out.println("Passport number :=== " + matcher.group(0));
                CommonScan.CARD_UNIQE_NO = matcher.group(0);
                passportnumber = true;
                break;
            }
        }
    }

    public void PassPortDateVerify(TextBlock textBlock) {


        for (int i = 0; i < textBlock.getComponents().size(); i++) {

            if (textBlock.getComponents().get(i).getValue().contains("/")) {

                if (textBlock.getComponents().get(i).getValue().length() == 10) {
                    if (validate(textBlock.getComponents().get(i).getValue())) {
                        CommonScan.CARD_HOLDER_DOB = textBlock.getComponents().get(i).getValue();
                        System.out.println("Passport date :=== " + textBlock.getComponents().get(i).getValue());
                        passportdate = true;


                    }
                } else if (textBlock.getComponents().get(i).getValue().length() == 11) {
                    if (validate(textBlock.getComponents().get(i).getValue().substring(1))) {
                        System.out.println("Passport Birth date :=== " + textBlock.getComponents().get(i).getValue().substring(1));
                        CommonScan.CARD_HOLDER_DOB = textBlock.getComponents().get(i).getValue().substring(1);
                        passportdate = true;

                    }
                }

            }
        }

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

    private SparseArray<TextBlock> createDateCameraSource(Bitmap bitmap) {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();


        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
        return textBlocks;
    }

    private Bitmap PassportSurnameImage(Bitmap origialBitmap) {

        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth(),
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(200, origialBitmap.getHeight() / 4 - 20, origialBitmap.getWidth() - 150, origialBitmap.getHeight() / 4 + 15);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);
        String PassportSurname = createCameraSource(cutBitmap);

        System.out.println("Surname name read from image" + PassportSurname);
        if (PassportSurname.length() > 1) {
            passportsurname = true;
            CommonScan.PASSPORT_SURNAME = PassportSurname;
        } else {
            System.out.println("not getting surnmae from passport");
        }

        return cutBitmap;
    }

    private Bitmap PassportNameImage(Bitmap origialBitmap) {

        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth(),
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(200, origialBitmap.getHeight() / 4 + 10, origialBitmap.getWidth() - 90, origialBitmap.getHeight() / 3 + 30);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);
        String PassportName = createCameraSource(cutBitmap);

        System.out.println("Name read from image" + PassportName);


        if (PassportName.length() > 1) {
            passportname = true;
            CommonScan.CARD_HOLDER_NAME = PassportName.trim().replaceAll("\\d", "");
            CommonScan.CARD_HOLDER_NAME = CommonScan.CARD_HOLDER_NAME.replaceAll("['-+.^`~:,@&(*)#$!%/]", "");
            CommonScan.CARD_HOLDER_NAME = Normalizer.normalize(CommonScan.CARD_HOLDER_NAME, Normalizer.Form.NFD);
            CommonScan.CARD_HOLDER_NAME = CommonScan.CARD_HOLDER_NAME.replaceAll("[^\\p{ASCII}]", "");
        } else {
            System.out.println("not getting nmae from passport");
        }

        return cutBitmap;
    }

    private Bitmap PassportBirthCity(Bitmap origialBitmap) {

        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth(),
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(200, origialBitmap.getHeight() / 2 - 15, origialBitmap.getWidth(), origialBitmap.getHeight() / 2 + 40);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);

        String PassportCity = createCameraSource(cutBitmap);

        System.out.println("BirthCity name read from image" + PassportCity);

        if (PassportCity.length() > 1) {
            passportbirthplace = true;
            CommonScan.CARD_BIRTH_PLACE = PassportCity.replaceAll("\\d", "");
            CommonScan.CARD_BIRTH_PLACE = CommonScan.CARD_BIRTH_PLACE.replaceAll("['-+.^`~:,@&(*)#$!%/]", "");
            CommonScan.CARD_BIRTH_PLACE = Normalizer.normalize(CommonScan.CARD_BIRTH_PLACE, Normalizer.Form.NFD);
            CommonScan.CARD_BIRTH_PLACE = CommonScan.CARD_BIRTH_PLACE.replaceAll("[^\\p{ASCII}]", "");

        } else {
            System.out.println("not getting city from passport");
        }

        return cutBitmap;
    }

    private Bitmap PassportIssueCityImage(Bitmap origialBitmap) {

        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth(),
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(200, origialBitmap.getHeight() / 2 + 20, origialBitmap.getWidth(), origialBitmap.getHeight() / 2 + 70);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);
        String PassportCity = createCameraSource(cutBitmap);

        System.out.println("IssueCity name read from image" + PassportCity);

        if (PassportCity.length() > 1) {
            passportissueplace = true;
            CommonScan.CARD_ISSUE_ADDRESS = PassportCity.trim().replaceAll("\\d", "");
            CommonScan.CARD_ISSUE_ADDRESS = CommonScan.CARD_ISSUE_ADDRESS.replaceAll("['-+.^`~:,@&(*)#$!%]", "");
            CommonScan.CARD_ISSUE_ADDRESS = Normalizer.normalize(CommonScan.CARD_ISSUE_ADDRESS, Normalizer.Form.NFD);
            CommonScan.CARD_ISSUE_ADDRESS = CommonScan.CARD_ISSUE_ADDRESS.replaceAll("[^\\p{ASCII}]", "");
        } else {
            System.out.println("not getting city from passport");
        }

        return cutBitmap;
    }

    private void PassportDateImage(Bitmap origialBitmap) {

        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth(),
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(250, origialBitmap.getHeight() / 2 + 50, origialBitmap.getWidth() - 170, origialBitmap.getHeight() / 2 + 105);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);

        SparseArray<TextBlock> textBlocks = createDateCameraSource(cutBitmap);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            System.out.println("call methods");

            if (!passportthroughdate) {
                for (int j = 0; j < textBlock.getComponents().size(); j++) {

                    passportthroughdate = LicenceDateVerify(textBlock.getComponents().get(j).getValue());

                    if (passportthroughdate) {
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

//        String PassportIssueDate = createCameraSource(cutBitmap);
//        System.out.println("DOI Date read from image" + PassportIssueDate);
//
//
//        if (PassportIssueDate.length() > 1) {
//            passportthroughdate = true;
//            CommonScan.CARD_ISSUE_DATE = PassportIssueDate;
//
//        } else {
//            System.out.println("not getting issue date from passport");
//        }


//        return cutBitmap;


    private void PassportSecondDateImage(Bitmap origialBitmap) {

        Bitmap cutBitmap = Bitmap.createBitmap(origialBitmap.getWidth(),
                origialBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(cutBitmap);
        Rect desRect = new Rect(420, origialBitmap.getHeight() / 2 + 50, origialBitmap.getWidth(), origialBitmap.getHeight() / 2 + 105);
        canvas.drawBitmap(origialBitmap, desRect, desRect, null);

        SparseArray<TextBlock> textBlocks = createDateCameraSource(cutBitmap);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            System.out.println("call methods");

            if (!passporttilldate) {
                for (int j = 0; j < textBlock.getComponents().size(); j++) {

                    passporttilldate = LicenceDateVerify(textBlock.getComponents().get(j).getValue());

                    if (passporttilldate) {
                        if (textBlock.getComponents().get(j).getValue().length() == 11) {
                            System.out.println("cut name detect issue:== " + textBlock.getComponents().get(j).getValue().substring(1));
                            CommonScan.CARD_TILL_DATE = textBlock.getComponents().get(j).getValue().substring(1);

                            break;
                        } else if (textBlock.getComponents().get(j).getValue().length() == 10) {
                            System.out.println("cut name detect issue:== " + textBlock.getComponents().get(j).getValue());

                            CommonScan.CARD_TILL_DATE = textBlock.getComponents().get(j).getValue();

                            break;
                        }
                    }
                }

                android.util.Log.i("text detect from image", textBlock.getValue());
                android.util.Log.i("text getBoundingBox", String.valueOf(textBlock.getBoundingBox()));
            }
        }

//        String Passporttilldate = createCameraSource(cutBitmap);
//
//        System.out.println("Date second read from image" + Passporttilldate);
//
//        if (Passporttilldate.length() > 1) {
//            passporttilldate = true;
//            CommonScan.CARD_TILL_DATE = Passporttilldate;
//
//        } else {
//            System.out.println("not getting issue date from passport");
//        }
//
//        return cutBitmap;
    }
}
