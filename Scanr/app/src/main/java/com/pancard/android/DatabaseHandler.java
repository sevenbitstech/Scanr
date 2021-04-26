package com.pancard.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pancard.android.model.CardDetail;
import com.pancard.android.model.SqliteDetail;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PreferenceManagement;

import java.util.ArrayList;


/**
 * Created by seven-bits-pc11 on 17/5/16.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_NAME = "DocumentScannerApp";

    private static final String TABLE_PANCARD_DETAIL = "pancard_detail";
    private static final String TABLE_DRIVING_LICENCE_DETAIL = "driving_licence_detail";
    private static final String TABLE_PASSPORT_DETAIL = "passport_detail";
    private static final String TABLE_DOCUMENT_DETAIL = "document_detail";
    private static final String TABLE_A4_DOCUMENT_SACN_DETAIL = "businesscard_detail";
    private static final String TABLE_DELETE_DETAIL = "delete_detail";
    private static final String TABLE_ADHARCARD_DETAIL = "adharcard_detail";
    private static final String TABLE_CREDITCARDCARD_DETAIL = "creditcard_detail";

    private static final String KEY_ID = "_id";

    //cardDetail variable
    private static final String KEY_NAME = "Person_name";
    private static final String KEY_BIRTH_DATE = "Person_date_of_birth";
    private static final String KEY_NUMBER = "Person_unique_number";
    private static final String KEY_SCAN_TIME = "Person_scan_time";
    private static final String KEY_PHOTO = "Person_Photo";
    private static final String KEY_ISSUE_DATE = "Person_issue_date";
    private static final String KEY_TILL_DATE = "Person_till_date";
    private static final String KEY_ISSUE_ADDRESS = "Person_Address";
    private static final String KEY_BIRTH_CITY = "Person_birth_city";
    private static final String KEY_STATUS = "Status";
    private static final String KEY_IMAGE_SIZE = "image_size";
    Context context;
    private PreferenceManagement preferences;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        preferences = Scanner.getInstance().getPreferences();
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DETAIL_TABLE = "CREATE TABLE " + TABLE_PANCARD_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                + KEY_BIRTH_DATE + " TEXT," + KEY_NUMBER + " TEXT, " + KEY_SCAN_TIME + " TEXT unique, " + KEY_PHOTO
                + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

        String CREATE_LICENCE_DETAIL_TABLE = "CREATE TABLE " + TABLE_DRIVING_LICENCE_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                + KEY_NUMBER + " TEXT," + KEY_BIRTH_DATE + " TEXT, " + KEY_ISSUE_DATE + " TEXT, "
                + KEY_TILL_DATE + " TEXT, " + KEY_ISSUE_ADDRESS + " TEXT, " + KEY_SCAN_TIME + " TEXT unique, "
                + KEY_PHOTO + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

        String CREATE_PASSPORT_DETAIL_TABLE = "CREATE TABLE " + TABLE_PASSPORT_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                + KEY_NUMBER + " TEXT," + KEY_BIRTH_DATE + " TEXT, " + KEY_ISSUE_DATE + " TEXT, "
                + KEY_TILL_DATE + " TEXT, " + KEY_BIRTH_CITY + " TEXT, " + KEY_ISSUE_ADDRESS + " TEXT, "
                + KEY_SCAN_TIME + " TEXT unique, "
                + KEY_PHOTO + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";


        String CREATE_DOCUMENT_TABLE = "CREATE TABLE " + TABLE_DOCUMENT_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                + KEY_SCAN_TIME + " TEXT unique, " + KEY_PHOTO
                + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

        db.execSQL(CREATE_DETAIL_TABLE);
        db.execSQL(CREATE_LICENCE_DETAIL_TABLE);
        db.execSQL(CREATE_PASSPORT_DETAIL_TABLE);
        db.execSQL(CREATE_DOCUMENT_TABLE);

        String CREATE_BUSINESS_CARD_TABLE = "CREATE TABLE " + TABLE_A4_DOCUMENT_SACN_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                + KEY_SCAN_TIME + " TEXT unique, " + KEY_PHOTO
                + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

        String CREATE_DELETE_TABLE = "CREATE TABLE " + TABLE_DELETE_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ," + KEY_SCAN_TIME + " TEXT unique, "
                + KEY_IMAGE_SIZE + " INTEGER " + ")";

        String CREATE_ADHAR_CARD_DETAIL_TABLE = "CREATE TABLE " + TABLE_ADHARCARD_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                + KEY_NUMBER + " TEXT," + KEY_BIRTH_DATE + " TEXT, " + KEY_ISSUE_DATE + " TEXT, "
                + KEY_TILL_DATE + " TEXT, " + KEY_BIRTH_CITY + " TEXT, " + KEY_ISSUE_ADDRESS + " TEXT, "
                + KEY_SCAN_TIME + " TEXT unique, "
                + KEY_PHOTO + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

        String CREATE_TABLE_CREDITCARDCARD_DETAIL = "CREATE TABLE " + TABLE_CREDITCARDCARD_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                + KEY_BIRTH_DATE + " TEXT," + KEY_NUMBER + " TEXT, " + KEY_SCAN_TIME + " TEXT unique, " + KEY_PHOTO
                + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";


        db.execSQL(CREATE_TABLE_CREDITCARDCARD_DETAIL);
        db.execSQL(CREATE_ADHAR_CARD_DETAIL_TABLE);
        db.execSQL(CREATE_BUSINESS_CARD_TABLE);
        db.execSQL(CREATE_DELETE_TABLE);
        System.out.println("table created ");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                // we want both updates, so no break statement here...
            case 2:
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANCARD_DETAIL);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRIVING_LICENCE_DETAIL);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSPORT_DETAIL);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENT_DETAIL);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_A4_DOCUMENT_SACN_DETAIL);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELETE_DETAIL);

                String CREATE_DETAIL_TABLE = "CREATE TABLE " + TABLE_PANCARD_DETAIL + "("
                        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                        + KEY_BIRTH_DATE + " TEXT," + KEY_NUMBER + " TEXT, " + KEY_SCAN_TIME + " TEXT unique, " + KEY_PHOTO
                        + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

                String CREATE_LICENCE_DETAIL_TABLE = "CREATE TABLE " + TABLE_DRIVING_LICENCE_DETAIL + "("
                        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                        + KEY_NUMBER + " TEXT," + KEY_BIRTH_DATE + " TEXT, " + KEY_ISSUE_DATE + " TEXT, "
                        + KEY_TILL_DATE + " TEXT, " + KEY_ISSUE_ADDRESS + " TEXT, " + KEY_SCAN_TIME + " TEXT unique, "
                        + KEY_PHOTO + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

                String CREATE_PASSPORT_DETAIL_TABLE = "CREATE TABLE " + TABLE_PASSPORT_DETAIL + "("
                        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                        + KEY_NUMBER + " TEXT," + KEY_BIRTH_DATE + " TEXT, " + KEY_ISSUE_DATE + " TEXT, "
                        + KEY_TILL_DATE + " TEXT, " + KEY_BIRTH_CITY + " TEXT, " + KEY_ISSUE_ADDRESS + " TEXT, "
                        + KEY_SCAN_TIME + " TEXT unique, "
                        + KEY_PHOTO + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

                String CREATE_DOCUMENT_TABLE = "CREATE TABLE " + TABLE_DOCUMENT_DETAIL + "("
                        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                        + KEY_SCAN_TIME + " TEXT unique, " + KEY_PHOTO
                        + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

                String CREATE_BUSINESS_CARD_TABLE = "CREATE TABLE " + TABLE_A4_DOCUMENT_SACN_DETAIL + "("
                        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                        + KEY_SCAN_TIME + " TEXT unique, " + KEY_PHOTO
                        + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

                String CREATE_DELETE_TABLE = "CREATE TABLE " + TABLE_DELETE_DETAIL + "("
                        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ," + KEY_SCAN_TIME + " TEXT unique, "
                        + KEY_IMAGE_SIZE + " INTEGER " + ")";

                db.execSQL(CREATE_BUSINESS_CARD_TABLE);
                db.execSQL(CREATE_DELETE_TABLE);
                db.execSQL(CREATE_DOCUMENT_TABLE);
                db.execSQL(CREATE_PASSPORT_DETAIL_TABLE);
                db.execSQL(CREATE_LICENCE_DETAIL_TABLE);
                db.execSQL(CREATE_DETAIL_TABLE);

            case 3:
                String CREATE_ADHAR_CARD_DETAIL_TABLE = "CREATE TABLE " + TABLE_ADHARCARD_DETAIL + "("
                        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                        + KEY_NUMBER + " TEXT," + KEY_BIRTH_DATE + " TEXT, " + KEY_ISSUE_DATE + " TEXT, "
                        + KEY_TILL_DATE + " TEXT, " + KEY_BIRTH_CITY + " TEXT, " + KEY_ISSUE_ADDRESS + " TEXT, "
                        + KEY_SCAN_TIME + " TEXT unique, "
                        + KEY_PHOTO + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";

                db.execSQL(CREATE_ADHAR_CARD_DETAIL_TABLE);

            case 4:
                String CREATE_TABLE_CREDITCARDCARD_DETAIL = "CREATE TABLE " + TABLE_CREDITCARDCARD_DETAIL + "("
                        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                        + KEY_BIRTH_DATE + " TEXT," + KEY_NUMBER + " TEXT, " + KEY_SCAN_TIME + " TEXT unique, " + KEY_PHOTO
                        + " TEXT," + KEY_STATUS + " TEXT," + KEY_IMAGE_SIZE + " INTEGER " + ")";


                db.execSQL(CREATE_TABLE_CREDITCARDCARD_DETAIL);


        }

    }


    public void sqliteInsertData(CardDetail cardDetail, String tag, String status) {
//        long i = 0;
        try {
            Log.e("add new ", tag);

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = InsertDataInContentValue(cardDetail, tag, true, status);
            if (tag.equals(Constants.document)) {
                db.insertOrThrow(TABLE_A4_DOCUMENT_SACN_DETAIL, null, contentValues);

            } else if (tag.equals(Constants.businesscard)) {
                db.insertOrThrow(TABLE_DOCUMENT_DETAIL, null, contentValues);

            } else if (tag.equals(Constants.pancard)) {
                db.insertOrThrow(TABLE_PANCARD_DETAIL, null, contentValues);

            } else if (tag.equals(Constants.creditCard)) {
                db.insertOrThrow(TABLE_CREDITCARDCARD_DETAIL, null, contentValues);

            } else if (tag.equals(Constants.passport)) {
                db.insertOrThrow(TABLE_PASSPORT_DETAIL, null, contentValues);

            } else if (tag.equals(Constants.licence)) {
                db.insertOrThrow(TABLE_DRIVING_LICENCE_DETAIL, null, contentValues);

            } else if (tag.equals(Constants.adharcard)) {
                db.insertOrThrow(TABLE_ADHARCARD_DETAIL, null, contentValues);

            } else if (tag.equals(Constants.delete)) {
//                Log.e("add new ", cardDetail.getWhichcard());
                ContentValues values = new ContentValues();
                values.put(KEY_NAME, cardDetail.getWhichcard());
                values.put(KEY_SCAN_TIME, cardDetail.getScan_time());
                values.put(KEY_IMAGE_SIZE, cardDetail.getImage_size());
                db.insertOrThrow(TABLE_DELETE_DETAIL, null, values);

            }
            db.close();
        } catch (Exception e) {
            preferences.setSizeDetail((int) (preferences.getSizeDetail() - cardDetail.getImage_size()));
            System.out.println("catch 1 sqlite : ");
            e.printStackTrace();
        }
//        return i;
    }

    private ContentValues InsertDataInContentValue(CardDetail cardDetail, String tag, boolean insert, String status) {
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, cardDetail.getCard_name());

        values.put(KEY_STATUS, status);

        if (tag.equals(Constants.pancard) || tag.equals(Constants.creditCard)) {

            values.put(KEY_NUMBER, cardDetail.getCard_unique_no());
            values.put(KEY_BIRTH_DATE, cardDetail.getDate_of_birth());


        } else if (tag.equals(Constants.passport)) {
            values.put(KEY_NUMBER, cardDetail.getCard_unique_no());

            values.put(KEY_BIRTH_DATE, cardDetail.getDate_of_birth());
            values.put(KEY_ISSUE_DATE, cardDetail.getIssue_date());
            values.put(KEY_TILL_DATE, cardDetail.getTill_date());
            values.put(KEY_ISSUE_ADDRESS, cardDetail.getIssue_address());
            values.put(KEY_BIRTH_CITY, cardDetail.getBirth_place());

        } else if (tag.equals(Constants.licence)) {
            values.put(KEY_NUMBER, cardDetail.getCard_unique_no());

            values.put(KEY_BIRTH_DATE, cardDetail.getDate_of_birth());
            values.put(KEY_ISSUE_DATE, cardDetail.getIssue_date());
            values.put(KEY_TILL_DATE, cardDetail.getTill_date());
            values.put(KEY_ISSUE_ADDRESS, cardDetail.getIssue_address());

        } else if (tag.equals(Constants.adharcard)) {
            values.put(KEY_NUMBER, cardDetail.getCard_unique_no());

            values.put(KEY_BIRTH_DATE, cardDetail.getDate_of_birth());
            if (insert) {
                values.put(KEY_ISSUE_DATE, cardDetail.getIssue_date());
            }
            values.put(KEY_TILL_DATE, cardDetail.getTill_date());
            values.put(KEY_ISSUE_ADDRESS, cardDetail.getIssue_address());
            values.put(KEY_BIRTH_CITY, cardDetail.getBirth_place());
        }
        if (insert) {

            values.put(KEY_PHOTO, cardDetail.getImage_url());
            values.put(KEY_IMAGE_SIZE, cardDetail.getImage_size());

            values.put(KEY_SCAN_TIME, cardDetail.getScan_time());
        }
        return values;
    }

    public ArrayList<SqliteDetail> GetAllTableData(String tag) {
        ArrayList<SqliteDetail> dataList = new ArrayList<SqliteDetail>();
        try {

            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = null;
            if (tag.equals(Constants.document) || tag.equals(Constants.businesscard)) {

//                String selectQuery = "SELECT  * FROM " + TABLE_A4_DOCUMENT_SACN_DETAIL;
                if (tag.equals(Constants.document))
                    cursor = db.rawQuery("SELECT  * FROM " + TABLE_A4_DOCUMENT_SACN_DETAIL, null);
                else
                    cursor = db.rawQuery("SELECT  * FROM " + TABLE_DOCUMENT_DETAIL, null);

                if (cursor.moveToFirst()) {
                    do {
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                        SqliteDetail data = new SqliteDetail();
                        data.setId(Integer.parseInt(cursor.getString(0)));
                        data.setCard_name(cursor.getString(1).replace("\n", " ").replace("\r", " "));
                        data.setScan_time(cursor.getString(2));

                        data.setImage_url(cursor.getString(3));

                        data.setStatus(cursor.getString(4));
                        data.setImage_size(Integer.parseInt(cursor.getString(5)));
                        // Adding contact to list
                        dataList.add(data);

                    } while (cursor.moveToNext());

                }
                cursor.close();

            } else if (tag.equals(Constants.pancard) || tag.equals(Constants.creditCard)) {
//                String selectQuery = "SELECT  * FROM " + TABLE_PANCARD_DETAIL;
                if (tag.equals(Constants.pancard))
                    cursor = db.rawQuery("SELECT  * FROM " + TABLE_PANCARD_DETAIL, null);
                else
                    cursor = db.rawQuery("SELECT  * FROM " + TABLE_CREDITCARDCARD_DETAIL, null);

                if (cursor.moveToFirst()) {
                    do {
                        SqliteDetail data = new SqliteDetail();
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));

                        data.setId(Integer.parseInt(cursor.getString(0)));
                        data.setCard_name(cursor.getString(1).replace("\n", " ").replace("\r", " "));
                        data.setDate_of_birth(cursor.getString(2));
                        data.setCard_unique_no(cursor.getString(3));
                        data.setScan_time(cursor.getString(4));

                        data.setImage_url(cursor.getString(5));

                        data.setStatus(cursor.getString(6));
                        data.setImage_size(Integer.parseInt(cursor.getString(7)));
                        // Adding contact to list
                        dataList.add(data);

                    } while (cursor.moveToNext());

                }
                cursor.close();
            } else if (tag.equals(Constants.passport)) {
                String selectQuery = "SELECT  * FROM " + TABLE_PASSPORT_DETAIL;
                cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        SqliteDetail data = new SqliteDetail();
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                        data.setId(Integer.parseInt(cursor.getString(0)));
                        data.setCard_name(cursor.getString(1).replace("\n", " ").replace("\r", " "));
                        data.setCard_unique_no(cursor.getString(2));
                        data.setDate_of_birth(cursor.getString(3));
                        data.setIssue_date(cursor.getString(4));
                        data.setTill_date(cursor.getString(5));
                        data.setBirth_place(cursor.getString(6));
                        data.setIssue_address(cursor.getString(7));
                        data.setScan_time(cursor.getString(8));

                        data.setImage_url(cursor.getString(9));

                        data.setStatus(cursor.getString(10));
                        data.setImage_size(Integer.parseInt(cursor.getString(11)));
                        // Adding contact to list
                        dataList.add(data);

                    } while (cursor.moveToNext());

                }
                cursor.close();
            } else if (tag.equals(Constants.adharcard)) {
                String selectQuery = "SELECT  * FROM " + TABLE_ADHARCARD_DETAIL;
                cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        SqliteDetail data = new SqliteDetail();
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                        data.setId(Integer.parseInt(cursor.getString(0)));
                        data.setCard_name(cursor.getString(1).replace("\n", " ").replace("\r", " "));
                        data.setCard_unique_no(cursor.getString(2));
                        data.setDate_of_birth(cursor.getString(3));
                        data.setIssue_date(cursor.getString(4));
                        data.setTill_date(cursor.getString(5));
                        data.setBirth_place(cursor.getString(6));
                        data.setIssue_address(cursor.getString(7));
                        data.setScan_time(cursor.getString(8));

                        data.setImage_url(cursor.getString(9));

                        data.setStatus(cursor.getString(10));
                        data.setImage_size(Integer.parseInt(cursor.getString(11)));
                        // Adding contact to list
                        dataList.add(data);

                    } while (cursor.moveToNext());

                }
                cursor.close();
            } else if (tag.equals(Constants.licence)) {
                // Select All Query
                String selectQuery = "SELECT  * FROM " + TABLE_DRIVING_LICENCE_DETAIL;
                cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                        SqliteDetail data = new SqliteDetail();
                        data.setId(Integer.parseInt(cursor.getString(0)));
                        data.setCard_name(cursor.getString(1).replace("\n", " ").replace("\r", " "));
                        data.setCard_unique_no(cursor.getString(2));
                        data.setDate_of_birth(cursor.getString(3));
                        data.setIssue_date(cursor.getString(4));
                        data.setTill_date(cursor.getString(5));
                        data.setIssue_address(cursor.getString(6));
                        data.setScan_time(cursor.getString(7));
                        data.setImage_url(cursor.getString(8));

                        data.setStatus(cursor.getString(9));
                        data.setImage_size(Integer.parseInt(cursor.getString(10)));
                        // Adding contact to list
                        dataList.add(data);


                    } while (cursor.moveToNext());

                }
                cursor.close();
            } else if (tag.equals(Constants.delete)) {
                // Select All Query
                String selectQuery = "SELECT  * FROM " + TABLE_DELETE_DETAIL;
                cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {

                        SqliteDetail data = new SqliteDetail();
                        data.setId(Integer.parseInt(cursor.getString(0)));
                        data.setWhichcard(cursor.getString(1));
                        data.setScan_time(cursor.getString(2));

                        data.setImage_size(Integer.parseInt(cursor.getString(3)));

                        dataList.add(data);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            db.close();

            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            return dataList;
        }
    }

    public ArrayList<CardDetail> getFalseValue(String tag, String status) {
        ArrayList<CardDetail> dataList = new ArrayList<CardDetail>();
        try {


            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = null;
            if (tag.equals(Constants.document) || tag.equals(Constants.businesscard)) {
                if (tag.equals(Constants.businesscard)) {
                    String selectQuery = "SELECT * FROM  " + TABLE_DOCUMENT_DETAIL + "   where " + KEY_STATUS + "='" + status + "'";
                    cursor = db.rawQuery(selectQuery, null);

                } else {
                    String selectQuery = "SELECT * FROM  " + TABLE_A4_DOCUMENT_SACN_DETAIL + "   where " + KEY_STATUS + "='" + status + "'";
                    cursor = db.rawQuery(selectQuery, null);
                }

                if (cursor.moveToFirst()) {
                    do {
                        CardDetail data = new CardDetail();
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                        data.setCard_name(cursor.getString(1));
                        data.setScan_time(cursor.getString(2));

                        data.setImage_url(cursor.getString(3));
                        data.setImage_size(Integer.parseInt(cursor.getString(5)));
                        // Adding contact to list
                        dataList.add(data);
//                    if (dataList.size() > 2) {
//                        Globalarea.actionFire = true;
//                        break;
//                    }
                    } while (cursor.moveToNext());

                }
                cursor.close();
            } else if (tag.equals(Constants.pancard) || tag.equals(Constants.creditCard)) {

                if (tag.equals(Constants.pancard)) {
                    String selectQuery = "SELECT * FROM  " + TABLE_PANCARD_DETAIL + "   where " + KEY_STATUS + "='" + status + "'";

                    cursor = db.rawQuery(selectQuery, null);

                } else {
                    String selectQuery = "SELECT * FROM  " + TABLE_CREDITCARDCARD_DETAIL + "   where " + KEY_STATUS + "='" + status + "'";
                    cursor = db.rawQuery(selectQuery, null);
                }

                if (cursor.moveToFirst()) {
                    do {
                        CardDetail data = new CardDetail();
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                        data.setCard_name(cursor.getString(1));
                        data.setDate_of_birth(cursor.getString(2));
                        data.setCard_unique_no(cursor.getString(3));
                        data.setScan_time(cursor.getString(4));

                        data.setImage_url(cursor.getString(5));
                        data.setImage_size(Integer.parseInt(cursor.getString(7)));
                        // Adding contact to list
                        dataList.add(data);
//                    if (dataList.size() > 4) {
//                        Globalarea.actionFire = true;
//                        break;
//                    }
                    } while (cursor.moveToNext());

                }
                cursor.close();
            } else if (tag.equals(Constants.passport)) {
                String selectQuery = "SELECT * FROM  " + TABLE_PASSPORT_DETAIL + "   where " + KEY_STATUS + "='" + status + "'";
//            String selectQuery = "SELECT  * FROM " + TABLE_PASSPORT_DETAIL;
                cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        CardDetail data = new CardDetail();
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                        data.setCard_name(cursor.getString(1));
                        data.setCard_unique_no(cursor.getString(2));
                        data.setDate_of_birth(cursor.getString(3));
                        data.setIssue_date(cursor.getString(4));
                        data.setTill_date(cursor.getString(5));
                        data.setBirth_place(cursor.getString(6));
                        data.setIssue_address(cursor.getString(7));
                        data.setScan_time(cursor.getString(8));

                        data.setImage_url(cursor.getString(9));
                        data.setImage_size(Integer.parseInt(cursor.getString(11)));
                        // Adding contact to list
                        dataList.add(data);
//                    if (dataList.size() > 4) {
//                        Globalarea.actionFire = true;
//                        break;
//                    }
                    } while (cursor.moveToNext());

                }
                cursor.close();
            } else if (tag.equals(Constants.adharcard)) {
                String selectQuery = "SELECT * FROM  " + TABLE_ADHARCARD_DETAIL + "   where " + KEY_STATUS + "='" + status + "'";
//            String selectQuery = "SELECT  * FROM " + TABLE_PASSPORT_DETAIL;
                cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        CardDetail data = new CardDetail();
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                        data.setCard_name(cursor.getString(1));
                        data.setCard_unique_no(cursor.getString(2));
                        data.setDate_of_birth(cursor.getString(3));
                        data.setIssue_date(cursor.getString(4));
                        data.setTill_date(cursor.getString(5));
                        data.setBirth_place(cursor.getString(6));
                        data.setIssue_address(cursor.getString(7));
                        data.setScan_time(cursor.getString(8));

                        data.setImage_url(cursor.getString(9));
                        data.setImage_size(Integer.parseInt(cursor.getString(11)));
                        // Adding contact to list
                        dataList.add(data);
//                    if (dataList.size() > 4) {
//                        Globalarea.actionFire = true;
//                        break;
//                    }
                    } while (cursor.moveToNext());

                }
                cursor.close();
            } else if (tag.equals(Constants.licence)) {
                // Select All Query
                String selectQuery = "SELECT * FROM  " + TABLE_DRIVING_LICENCE_DETAIL + " where " + KEY_STATUS + "='" + status + "'";
//            String selectQuery = "SELECT  * FROM " + TABLE_DRIVING_LICENCE_DETAIL;
                cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
//                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                        CardDetail data = new CardDetail();
                        data.setCard_name(cursor.getString(1));
                        data.setCard_unique_no(cursor.getString(2));
                        data.setDate_of_birth(cursor.getString(3));
                        data.setIssue_date(cursor.getString(4));
                        data.setTill_date(cursor.getString(5));
                        data.setIssue_address(cursor.getString(6));
                        data.setScan_time(cursor.getString(7));
                        data.setImage_url(cursor.getString(8));
                        data.setImage_size(Integer.parseInt(cursor.getString(10)));
                        // Adding contact to list
                        dataList.add(data);
//                    if (dataList.size() > 4) {
//                        Globalarea.actionFire = true;
//                        break;
//                    }
                    } while (cursor.moveToNext());

                }
                cursor.close();
            }

            db.close();

            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            return dataList;
        }
    }

    public SqliteDetail getSingleRow(String whichcard, String scantime) {
        SQLiteDatabase db = this.getReadableDatabase();
        SqliteDetail contact = new SqliteDetail();
        if (whichcard.equals(Constants.document)) {

            String selectQuery = "SELECT  * FROM " + TABLE_A4_DOCUMENT_SACN_DETAIL + " where " + KEY_SCAN_TIME + "='" + scantime + "'";
            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    contact = new SqliteDetail(Integer.parseInt(c.getString(0)),
                            c.getString(1), c.getString(2), c.getString(3), Integer.parseInt(c.getString(5)), c.getString(4));


                } while (c.moveToNext());

            }
            c.close();
        } else if (whichcard.equals(Constants.businesscard)) {

            String selectQuery = "SELECT  * FROM " + TABLE_DOCUMENT_DETAIL + " where " + KEY_SCAN_TIME + "='" + scantime + "'";


            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    contact = new SqliteDetail(Integer.parseInt(c.getString(0)),
                            c.getString(1), c.getString(2), c.getString(3), Integer.parseInt(c.getString(5)), c.getString(4));

                } while (c.moveToNext());

            }
            c.close();
        } else if (whichcard.equals(Constants.pancard)) {
            String selectQuery = "SELECT  * FROM " + TABLE_PANCARD_DETAIL + " where " + KEY_SCAN_TIME + "='" + scantime + "'";

            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    contact = new SqliteDetail(Integer.parseInt(c.getString(0)),
                            c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5),
                            Integer.parseInt(c.getString(7)), c.getString(6));


                } while (c.moveToNext());

            }
            c.close();
        } else if (whichcard.equals(Constants.creditCard)) {
            String selectQuery = "SELECT  * FROM " + TABLE_CREDITCARDCARD_DETAIL + " where " + KEY_SCAN_TIME + "='" + scantime + "'";

            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    contact = new SqliteDetail(Integer.parseInt(c.getString(0)),
                            c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5),
                            Integer.parseInt(c.getString(7)), c.getString(6));


                } while (c.moveToNext());

            }
            c.close();
        } else if (whichcard.equals(Constants.licence)) {
            String selectQuery = "SELECT  * FROM " + TABLE_DRIVING_LICENCE_DETAIL + " where " + KEY_SCAN_TIME + "='" + scantime + "'";

            Cursor c = db.rawQuery(selectQuery, null);


            if (c.moveToFirst()) {
                do {
                    contact = new SqliteDetail(Integer.parseInt(c.getString(0)),
                            c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8),
                            Integer.parseInt(c.getString(10)), c.getString(9));


                } while (c.moveToNext());

            }
            c.close();
        } else if (whichcard.equals(Constants.passport)) {
            String selectQuery = "SELECT  * FROM " + TABLE_PASSPORT_DETAIL + " where " + KEY_SCAN_TIME + "='" + scantime + "'";

            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    contact = new SqliteDetail(Integer.parseInt(c.getString(0)),
                            c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7)
                            , c.getString(9), c.getString(8),
                            Integer.parseInt(c.getString(11)), c.getString(10));


                } while (c.moveToNext());
            }
            c.close();
        } else if (whichcard.equals(Constants.adharcard)) {
            String selectQuery = "SELECT  * FROM " + TABLE_ADHARCARD_DETAIL + " where " + KEY_SCAN_TIME + "='" + scantime + "'";

            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    contact = new SqliteDetail(Integer.parseInt(c.getString(0)),
                            c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7)
                            , c.getString(9), c.getString(8),
                            Integer.parseInt(c.getString(11)), c.getString(10));


                } while (c.moveToNext());

            }
            c.close();
        }

        db.close();

        return contact;
    }

    public void deletRowData(String tag, String id) {
        try {

            SQLiteDatabase db = this.getWritableDatabase();
            if (tag.equals(Constants.document)) {

                db.delete(TABLE_A4_DOCUMENT_SACN_DETAIL, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.businesscard)) {

                db.delete(TABLE_DOCUMENT_DETAIL, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.pancard)) {

                db.delete(TABLE_PANCARD_DETAIL, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.creditCard)) {

                db.delete(TABLE_CREDITCARDCARD_DETAIL, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.passport)) {

                db.delete(TABLE_PASSPORT_DETAIL, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.adharcard)) {

                db.delete(TABLE_ADHARCARD_DETAIL, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.licence)) {

                db.delete(TABLE_DRIVING_LICENCE_DETAIL, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.delete)) {

                db.delete(TABLE_DELETE_DETAIL, KEY_SCAN_TIME + "='" + id + "'", null);
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public void updateSqliteRow(CardDetail cardDetail, String tag, String status) {
        try {
            Log.e("Udapate", status);
            String id = cardDetail.getScan_time();
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = InsertDataInContentValue(cardDetail, tag, false, status);
            if (tag.equals(Constants.document)) {
                db.update(TABLE_A4_DOCUMENT_SACN_DETAIL, contentValues, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.businesscard)) {
                db.update(TABLE_DOCUMENT_DETAIL, contentValues, KEY_SCAN_TIME + "='" + id + "'", null);
            } else if (tag.equals(Constants.pancard)) {
                db.update(TABLE_PANCARD_DETAIL, contentValues, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.creditCard)) {
                db.update(TABLE_CREDITCARDCARD_DETAIL, contentValues, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.passport)) {
                db.update(TABLE_PASSPORT_DETAIL, contentValues, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.adharcard)) {
                db.update(TABLE_ADHARCARD_DETAIL, contentValues, KEY_SCAN_TIME + "='" + id + "'", null);

            } else if (tag.equals(Constants.licence)) {
                db.update(TABLE_DRIVING_LICENCE_DETAIL, contentValues, KEY_SCAN_TIME + "='" + id + "'", null);

            }
            db.close();
        } catch (Exception e) {
            preferences.setSizeDetail((int) (preferences.getSizeDetail() - cardDetail.getImage_size()));
            e.printStackTrace();
        }
    }
}