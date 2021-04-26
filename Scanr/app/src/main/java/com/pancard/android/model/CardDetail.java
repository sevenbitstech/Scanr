package com.pancard.android.model;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by seven-bits-pc11 on 19/5/17.
 */
public class CardDetail implements Serializable {


    // Common variable
//   public int id;
    @SerializedName("scan_time")
    public String scan_time;
    @SerializedName("image_url")
    public String image_url;
    @SerializedName("image_size")
    public long image_size;
    @SerializedName("date_of_birth")
    public String date_of_birth;
    @SerializedName("card_unique_no")
    public String card_unique_no;
    @SerializedName("card_name")
    public String card_name;
    @SerializedName("bitmap")
    public Bitmap bitmap;
    // Passport and driving licence variable
    @SerializedName("issue_date")
    public String issue_date;
    @SerializedName("till_date")
    public String till_date;
    @SerializedName("issue_address")
    public String issue_address;
    @SerializedName("birth_place")
    public String birth_place;
    @SerializedName("cardDetail")
    public CardDetail cardDetail;
    @SerializedName("whichcard")
    public String whichcard;
    @SerializedName("fileName")
    public String fileName;
    @SerializedName("pdfFilePath")
    public String pdfFilePath;

    public CardDetail() {

    }

    public CardDetail(String whichcard, String scan_time, int image_size) {
        this.scan_time = scan_time;
        this.image_size = image_size;
        this.whichcard = whichcard;
    }

    // Document and business card use this constructor
    public CardDetail(String card_name, String scan_time, String image_url, long image_size) {
        this.card_name = card_name;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;
    }

    //todo: this is used in the new flow
    public CardDetail(String card_name, String scan_time, String image_url, long image_size, String fileName) {
        this.card_name = card_name;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;
        this.fileName = fileName;
    }

//    public CardDetail(CardDetail cardDetail, Bitmap bitmap) {
//        this.cardDetail = cardDetail;
//        this.bitmap = bitmap;
//    }

    public CardDetail(String card_name, String date_of_birth, String card_unique_no, String scan_time, String image_url, long image_size) {
        this.card_name = card_name;
        this.date_of_birth = date_of_birth;
        this.card_unique_no = card_unique_no;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;
    }


    public CardDetail(String card_name, String card_unique_no, String date_of_birth,
                      String issue_date, String till_date,
                      String issue_address, String scan_time, String image_url, long image_size) {

        this.card_name = card_name;
        this.card_unique_no = card_unique_no;
        this.date_of_birth = date_of_birth;
        this.issue_date = issue_date;
        this.till_date = till_date;
        this.issue_address = issue_address;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;

    }

    public CardDetail(String card_name, String card_unique_no, String date_of_birth, String issue_date,
                      String till_date, String birth_place, String issue_address, String image_url,
                      String scan_time, long image_size) {
        this.card_name = card_name;
        this.card_unique_no = card_unique_no;
        this.date_of_birth = date_of_birth;
        this.issue_date = issue_date;
        this.till_date = till_date;
        this.birth_place = birth_place;
        this.issue_address = issue_address;
        this.image_url = image_url;
        this.scan_time = scan_time;
        this.image_size = image_size;

    }

//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }

    public String getWhichcard() {
        return whichcard;
    }

    public void setWhichcard(String whichcard) {
        this.whichcard = whichcard;
    }

    public String getScan_time() {
        return scan_time;
    }

    public void setScan_time(String scan_time) {
        this.scan_time = scan_time;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public long getImage_size() {
        return image_size;
    }

    public void setImage_size(long image_size) {
        this.image_size = image_size;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getCard_unique_no() {
        return card_unique_no;
    }

    public void setCard_unique_no(String card_unique_no) {
        this.card_unique_no = card_unique_no;
    }

    public String getCard_name() {
        return card_name;
    }

    public void setCard_name(String card_name) {
        this.card_name = card_name;
    }

    public String getIssue_date() {
        return issue_date;
    }

    public void setIssue_date(String issue_date) {
        this.issue_date = issue_date;
    }

    public String getTill_date() {
        return till_date;
    }

    public void setTill_date(String till_date) {
        this.till_date = till_date;
    }

    public String getIssue_address() {
        return issue_address;
    }

    public void setIssue_address(String issue_address) {
        this.issue_address = issue_address;
    }

    public String getBirth_place() {
        return birth_place;
    }

    public void setBirth_place(String birth_place) {
        this.birth_place = birth_place;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
    }

    //    public CardDetail getCardDetail() {
//        return cardDetail;
//    }
//
//    public void setCardDetail(CardDetail cardDetail) {
//        this.cardDetail = cardDetail;
//    }

    public String get2WordOr15CharDisplayString() {
        String displayName = getCard_name();

        String[] words = displayName.split(" ");

        String twoWords = "";
        if (words.length > 2) {
            twoWords = words[0] + " " + words[1];
            int totalCharSize = twoWords.length();

            displayName = twoWords.substring(0, Math.min(twoWords.length() - 1, 15));

        } else {
            if (displayName.length() < 5) {
                displayName = "Document";
            }
            displayName = displayName.substring(0, displayName.length() - 1);
        }

        return displayName;
    }
}
