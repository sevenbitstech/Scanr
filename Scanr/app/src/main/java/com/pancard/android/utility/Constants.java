package com.pancard.android.utility;

import java.util.ArrayList;

public class Constants {
    public static final String HOME_TAG = "home_tag";
    public static final String ROOT_TAG = "root";
    public static final String FILES_TAG = "files_tag";
    public static final String CAMERA_TAG = "camera_tag";
    public static final String SETTINGS_TAG = "settings_tag";
    public static final String TEXT_DETAIL_BOTTOM_SHEET_TAG = "text_detail_bottom_sheet_tag";
    public static final String KEY_BOTTOM_SHEET = "bottom sheet key";
    public static final String KEY_DRIVE_DOC = "drive doc key";
    public static final String KEY_OCR = "ocr key";
    public static final String KEY_OCR_OFF_SCAN = "__ocroffscan";
    public static final String FILES_SELECTED = "files_selected";
    public static String ARG_UID = "uid";
    //    public static String pin = "PIN";
//    public static String pinUpdate = "PINUpdate";
    public static String ActivityName = "ActivityName";
    public static String UploadError = "uploaderror";
    public static String InternetConnectionFail = "InternetConnectionFail";
    public static String SomethingWentWrong = "SomethingWentWrong";
    public static String document = "Document";
    public static String businesscard = "Business Card";
    public static String pancard = "Pancard";
    public static String pancard2 = "Pancard_new";
    public static String licence = "Licence";
    public static String delete = "Delete";
    public static String passport = "Passport";
    public static String adharcard = "Aadhar Card";
    public static String creditCard = "Credit Card";
    public static String ErrorOfQRcode = "ErrorOfQRcode";
    public static String WHICH_ERROR = "which_card_error";
    public static String AADHAR_BACK = "Capture Back Image of AADHAR CARD";
    public static String CARD_DATAT = "Carddata";
    public static String SAVED_CARD = "card saved success";
    public static String START_FRAGMENT = "fragment to start";
    //    public static String FOLDER_HOLDER_ID_DATA = "folder holder id data";
//    public static String FILE_HOLDER_DATA = "file holder data";
    public static ArrayList<String> tagList = new ArrayList<>();

    public static ArrayList<String> initArrayTag() {
        if (tagList.size() > 3) {
            return tagList;
        } else {
            tagList.clear();
            tagList.add(document);
            tagList.add(businesscard);
            tagList.add(pancard);
            tagList.add(passport);
            tagList.add(licence);
            return tagList;
        }

    }
}
