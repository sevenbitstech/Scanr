package com.pancard.android.asyntask;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.docscan.android.R;
import com.pancard.android.DatabaseHandler;
import com.pancard.android.Scanner;
import com.pancard.android.model.CardDetail;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.Firebase_ImageLoader;
import com.pancard.android.utility.PreferenceManagement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by seven-bits-pc11 on 17/5/17.
 */
public class DownloadImage extends AsyncTask<String, String, Boolean> {

    private Firebase_ImageLoader firebase_imageLoader;
    private DatabaseHandler handler;
    private String tag;
    private Context context;
    private String message;
    private ArrayList<CardDetail> cardDetails;
    private int image_size;
    private PreferenceManagement preferences;

    public DownloadImage(Context context, String tag, ArrayList<CardDetail> cardDetails) {
        preferences = Scanner.getInstance().getPreferences();
        this.tag = tag;
        this.context = context;
        firebase_imageLoader = new Firebase_ImageLoader(Scanner.getInstance());
        handler = new DatabaseHandler(context);
        this.cardDetails = cardDetails;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    protected Boolean doInBackground(String... urls) {
        if (Scanner.getInstance().getConnectionDetactor().isConnectingToInternet()) {

            try {
                for (int i = 0; i < cardDetails.size(); i++) {
                    Bitmap bitmap = firebase_imageLoader.getBitmap(cardDetails.get(i).getImage_url(), 400);
                    Log.e("first url : ", cardDetails.get(i).getImage_url());
                    image_size = (int) cardDetails.get(i).getImage_size();
                    File file = CaptureImage(bitmap, tag);
                    cardDetails.get(i).setImage_url(file.getAbsolutePath());

                    if (tag.equals(Constants.adharcard)) {
                        Log.e("second url : ", cardDetails.get(i).getIssue_date());
                        Bitmap _bitmap = firebase_imageLoader.getBitmap(cardDetails.get(i).getIssue_date(), 400);
                        File _file = CaptureImage(_bitmap, tag);
                        cardDetails.get(i).setIssue_date(_file.getAbsolutePath());
                    }

                    preferences.setSizeDetail((int) (preferences.getSizeDetail() + cardDetails.get(i).getImage_size()));
                    System.out.println("Size of cards : " + preferences.getSizeDetail());
                    if (preferences.getSizeDetail() < 20000) {
                        handler.sqliteInsertData(cardDetails.get(i), tag, "true");
                    } else {

                        preferences.setSizeDetail((int) (preferences.getSizeDetail() - cardDetails.get(i).getImage_size()));
                        message = context.getResources().getString(R.string.UploadError);
                        return false;
                    }
//                    IsertDataSqlite(cardDetails.get(i), bitmap);
                }
            } catch (Exception e) {
                preferences.setSizeDetail((preferences.getSizeDetail() - image_size));

                e.printStackTrace();
                message = context.getResources().getString(R.string.SomethingWentWrong);
                return false;
            }
        } else {
            preferences.setSizeDetail((preferences.getSizeDetail() - image_size));

            System.out.println("*****Please connect to working Internet connection");
            message = context.getResources().getString(R.string.internetConnectionFail);
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            preferences.setStringPreference(tag + "Get List", "0");
        } else {
            if (message == null) {
                message = context.getResources().getString(R.string.SomethingWentWrong);
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    public File CaptureImage(Bitmap bitmap, String tag) {
        /* MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), card, "Detected Image", ""); */
        try {
            File imageFile;
            File dir;
            dir = new File(context.getExternalCacheDir() + "/" + context.getResources().getString(R.string.app_name), tag);
//            File file = new File(this.getExternalCacheDir(), "image.png");
            boolean success = true;
            if (!dir.exists()) {
                success = dir.mkdirs();
            }
            if (success) {
                Date date = new Date();
                imageFile = new File(dir.getAbsolutePath()
                        + File.separator
                        + new java.sql.Timestamp(date.getTime()).toString()
                        + "Image.jpg");

                imageFile.createNewFile();
            } else {
                return null;
            }
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

            // save image into gallery
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

            FileOutputStream fout = new FileOutputStream(imageFile);
            fout.write(ostream.toByteArray());
            fout.close();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN,
                    System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA,
                    imageFile.getAbsolutePath());
            return imageFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

//    private void IsertDataSqlite(CardDetail cardDetail, Bitmap bitmap) {
//        SqliteDetail sqliteDetail = new SqliteDetail();
//        if (tag.equals(Constants.businesscard) || tag.equals(Constants.document)) {
//            sqliteDetail = new SqliteDetail(cardDetail.getCard_name(), cardDetail.getScan_time(), bitmap, (int) cardDetail.getImage_size(), "true");
//
//        } else if (tag.equals(Constants.pancard)) {
//            sqliteDetail = new SqliteDetail(cardDetail.getCard_name(), cardDetail.getDate_of_birth(),
//                    cardDetail.getCard_unique_no(), cardDetail.getScan_time()
//                    , bitmap, (int) cardDetail.getImage_size(), "true");
//
//        } else if (tag.equals(Constants.licence)) {
//            sqliteDetail = new SqliteDetail(cardDetail.getCard_name(), cardDetail.getCard_unique_no(),
//                    cardDetail.getDate_of_birth(), cardDetail.getIssue_date(), cardDetail.getTill_date(),
//                    cardDetail.getBirth_place(),
//                    cardDetail.getIssue_address(), bitmap,
//                    cardDetail.getScan_time(), (int) cardDetail.getImage_size(), "true");
//
//        } else if (tag.equals(Constants.passport)) {
//            sqliteDetail = new SqliteDetail(cardDetail.getCard_name(), cardDetail.getCard_unique_no(),
//                    cardDetail.getDate_of_birth(), cardDetail.getIssue_date(), cardDetail.getTill_date(),
//                    cardDetail.getBirth_place(),
//                    cardDetail.getIssue_address(), bitmap,
//                    cardDetail.getScan_time(), (int) cardDetail.getImage_size(), "true");
//
//        }
//        handler.sqliteInsertData(sqliteDetail, tag);
//    }