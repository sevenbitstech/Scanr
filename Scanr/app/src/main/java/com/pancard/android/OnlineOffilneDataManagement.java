package com.pancard.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pancard.android.model.SqliteDetail;

import java.util.ArrayList;


/**
 * Created by seven-bits-pc11 on 17/5/16.
 */
public class OnlineOffilneDataManagement extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "DocumentScanner";

    private static final String TABLE_PANCARD_DETAIL = "pancard_detail";
    private static final String TABLE_DRIVING_LICENCE_DETAIL = "driving_licence_detail";
    private static final String TABLE_PASSPORT_DETAIL = "passport_detail";
    private static final String TABLE_DOCUMENT_DETAIL = "document_detail";

    private static final String KEY_ID = "id";

    //SqliteDetail variable
    private static final String KEY_NAME = "pancard_name";
    private static final String KEY_DATE = "pancard_date";
    private static final String KEY_NUMBER = "pancard_number";
    private static final String KEY_SCAN_TIME = "pancard_scan_time";
    private static final String KEY_PHOTO = "Photo";


    // Driving licence variable
    private static final String KEY_LICENCE_NAME = "licence_name";
    private static final String KEY_LICENCE_NUMBER = "licence_number";
    private static final String KEY_LICENCE_DOB_DATE = "licence_dob_date";
    private static final String KEY_LICENCE_ISSUE_DATE = "licence_issue_date";
    private static final String KEY_LICENCE_TILL_DATE = "licence_till_date";
    private static final String KEY_LICENCE_ADDRESS = "licence_Address";
    private static final String KEY_LICENCE_SCAN_TIME = "licence_scan_time";
    private static final String KEY_LICENCE_PHOTO = "Photo";


    // Passport variable
    private static final String KEY_PASSPORT_NAME = "passport_name";
    private static final String KEY_PASSPORT_NUMBER = "passport_number";
    private static final String KEY_PASSPORT_DOB = "passport_dob";
    private static final String KEY_PASSPORT_ISSUE_DATE = "passport_issue_date";
    private static final String KEY_PASSPORT_TILL_DATE = "passport_till_date";
    private static final String KEY_PASSPORT_BIRTHCITY = "passport_birth_city";
    private static final String KEY_PASSPORT_ISSUECITY = "passport_issue_city";
    private static final String KEY_PASSPORT_SCAN_TIME = "passport_scan_time";
    private static final String KEY_PASSPORT_PHOTO = "Photo";

    //Document Scanning variable
    private static final String KEY_DOCUMENT_NAME = "document_name";
    private static final String KEY_DOCUMENT_PHOTO = "document_image";


    public OnlineOffilneDataManagement(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DETAIL_TABLE = "CREATE TABLE " + TABLE_PANCARD_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT ,"
                + KEY_DATE + " TEXT," + KEY_NUMBER + " TEXT, " + KEY_SCAN_TIME + " TEXT, " + KEY_PHOTO
                + " blob not null" + ")";

        String CREATE_LICENCE_DETAIL_TABLE = "CREATE TABLE " + TABLE_DRIVING_LICENCE_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LICENCE_NAME + " TEXT ,"
                + KEY_LICENCE_NUMBER + " TEXT," + KEY_LICENCE_DOB_DATE + " TEXT, " + KEY_LICENCE_ISSUE_DATE + " TEXT, "
                + KEY_LICENCE_TILL_DATE + " TEXT, " + KEY_LICENCE_ADDRESS + " TEXT, " + KEY_LICENCE_SCAN_TIME + " TEXT, "
                + KEY_LICENCE_PHOTO + " blob not null" + ")";

        String CREATE_PASSPORT_DETAIL_TABLE = "CREATE TABLE " + TABLE_PASSPORT_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PASSPORT_NAME + " TEXT ,"
                + KEY_PASSPORT_NUMBER + " TEXT," + KEY_PASSPORT_DOB + " TEXT, " + KEY_PASSPORT_ISSUE_DATE + " TEXT, "
                + KEY_PASSPORT_TILL_DATE + " TEXT, " + KEY_PASSPORT_BIRTHCITY + " TEXT, " + KEY_PASSPORT_ISSUECITY + " TEXT, " + KEY_PASSPORT_SCAN_TIME + " TEXT, "
                + KEY_PASSPORT_PHOTO + " blob not null" + ")";

        String CREATE_DOCUMENT_TABLE = "CREATE TABLE " + TABLE_DOCUMENT_DETAIL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DOCUMENT_NAME + " TEXT ,"
                + KEY_SCAN_TIME + " TEXT, " + KEY_DOCUMENT_PHOTO
                + " blob not null" + ")";


        db.execSQL(CREATE_DETAIL_TABLE);
        db.execSQL(CREATE_LICENCE_DETAIL_TABLE);
        db.execSQL(CREATE_PASSPORT_DETAIL_TABLE);
        db.execSQL(CREATE_DOCUMENT_TABLE);


        System.out.println("table created ");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANCARD_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRIVING_LICENCE_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSPORT_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENT_DETAIL);
        // Create tables again
        onCreate(db);
        db.close();
    }

    // Getting All Contacts
    public ArrayList<SqliteDetail> getAllDatas() {
        ArrayList<SqliteDetail> dataList = new ArrayList<SqliteDetail>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PANCARD_DETAIL;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PHOTO));
                SqliteDetail data = new SqliteDetail();
                data.setId(Integer.parseInt(cursor.getString(0)));
                data.setCard_name(cursor.getString(1));
                data.setDate_of_birth(cursor.getString(2));
                data.setCard_unique_no(cursor.getString(3));
                data.setScan_time(cursor.getString(4));

                data.setImage_Bitmap(Utility.getPhoto(blob));
                // Adding contact to list
                dataList.add(data);

            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();

        // return contact list
        return dataList;
    }

    // Licence database method


    // Getting All Contacts
    public ArrayList<SqliteDetail> getAllLicence() {
        ArrayList<SqliteDetail> dataList = new ArrayList<SqliteDetail>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DRIVING_LICENCE_DETAIL;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_LICENCE_PHOTO));
                SqliteDetail data = new SqliteDetail();
                data.setId(Integer.parseInt(cursor.getString(0)));
                data.setCard_name(cursor.getString(1));
                data.setCard_unique_no(cursor.getString(2));
                data.setDate_of_birth(cursor.getString(3));
                data.setIssue_date(cursor.getString(4));
                data.setTill_date(cursor.getString(5));
                data.setIssue_address(cursor.getString(6));
                data.setScan_time(cursor.getString(7));

                data.setImage_Bitmap(Utility.getPhoto(blob));
                // Adding contact to list
                dataList.add(data);

////                readAndExecuteSQLScript(db,context1,DATABASE_NAME);
//                System.out.println(" foreign all data := " + Integer.parseInt(cursor.getString(0)));

            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();

        // return contact list
        return dataList;
    }


    // Passport database method

    // Getting All Contacts
    public ArrayList<SqliteDetail> getAllPassport() {
        ArrayList<SqliteDetail> dataList = new ArrayList<SqliteDetail>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PASSPORT_DETAIL;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_PASSPORT_PHOTO));
                SqliteDetail data = new SqliteDetail();
                data.setId(Integer.parseInt(cursor.getString(0)));
                data.setCard_name(cursor.getString(1));
                data.setCard_unique_no(cursor.getString(2));
                data.setDate_of_birth(cursor.getString(3));
                data.setIssue_date(cursor.getString(4));
                data.setTill_date(cursor.getString(5));
                data.setBirth_place(cursor.getString(6));
                data.setIssue_address(cursor.getString(7));
                data.setScan_time(cursor.getString(8));

                data.setImage_Bitmap(Utility.getPhoto(blob));
                // Adding contact to list
                dataList.add(data);

            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();

        // return contact list
        return dataList;
    }


    // Getting All Contacts
    public ArrayList<SqliteDetail> getAllDocument() {
        ArrayList<SqliteDetail> dataList = new ArrayList<SqliteDetail>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DOCUMENT_DETAIL;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_DOCUMENT_PHOTO));
                SqliteDetail data = new SqliteDetail();
                data.setId(Integer.parseInt(cursor.getString(0)));
                data.setCard_name(cursor.getString(1));
                data.setScan_time(cursor.getString(2));
                data.setImage_Bitmap(Utility.getPhoto(blob));
                // Adding contact to list
                dataList.add(data);

            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();

        // return contact list
        return dataList;
    }

    public void deletepancard(String Id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_PANCARD_DETAIL, "Id=" + Id, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletepassport(String Id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_PASSPORT_DETAIL, "Id=" + Id, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletelicence(String Id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_DRIVING_LICENCE_DETAIL, "Id=" + Id, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletedocument(String Id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_DOCUMENT_DETAIL, "Id=" + Id, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}