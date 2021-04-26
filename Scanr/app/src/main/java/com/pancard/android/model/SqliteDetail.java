package com.pancard.android.model;

import android.graphics.Bitmap;

/**
 * Created by seven-bits-pc11 on 19/5/17.
 */
public class SqliteDetail {


    // Common variable
    public int id;
    public String scan_time;
    public Bitmap image_Bitmap;
    public String image_url;
    public int image_size;
    public String date_of_birth;
    public String card_unique_no;
    public String card_name;

    // Passport and driving licence variable
    public String issue_date;
    public String till_date;
    public String issue_address;
    public String birth_place;
    public String status;
    public String whichcard;

    public SqliteDetail() {

    }


    public SqliteDetail(String whichcard, String scan_time, int image_size) {
        this.scan_time = scan_time;
        this.image_size = image_size;
        this.whichcard = whichcard;
    }

    public SqliteDetail(int id, String whichcard, String scan_time, int image_size) {
        this.scan_time = scan_time;
        this.image_size = image_size;
        this.id = id;
        this.whichcard = whichcard;

    }

    // Document and business card use this constructor
    public SqliteDetail(int id, String card_name, String scan_time, String image_url, int image_size, String status) {
        this.id = id;
        this.card_name = card_name;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;
        this.status = status;
    }

    // Document and business card use this constructor
    public SqliteDetail(String card_name, String scan_time, String image_url, int image_size, String status) {
        this.card_name = card_name;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;
        this.status = status;

    }

    public SqliteDetail(int id, String card_name, String date_of_birth, String card_unique_no, String scan_time,
                        String image_url, int image_size, String status) {
        this.id = id;

        this.card_name = card_name;
        this.date_of_birth = date_of_birth;
        this.card_unique_no = card_unique_no;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;
        this.status = status;

    }

    public SqliteDetail(String card_name, String date_of_birth, String card_unique_no, String scan_time,
                        String image_url, int image_size, String status) {
        this.card_name = card_name;
        this.date_of_birth = date_of_birth;
        this.card_unique_no = card_unique_no;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;
        this.status = status;

    }


    public SqliteDetail(int id, String card_name, String card_unique_no, String date_of_birth,
                        String issue_date, String till_date,
                        String issue_address, String scan_time, String image_url, int image_size, String status) {
        this.id = id;

        this.card_name = card_name;
        this.card_unique_no = card_unique_no;
        this.date_of_birth = date_of_birth;
        this.issue_date = issue_date;
        this.till_date = till_date;
        this.issue_address = issue_address;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;
        this.status = status;


    }

    public SqliteDetail(String card_name, String card_unique_no, String date_of_birth,
                        String issue_date, String till_date,
                        String issue_address, String scan_time, String image_url, int image_size, String status) {

        this.card_name = card_name;
        this.card_unique_no = card_unique_no;
        this.date_of_birth = date_of_birth;
        this.issue_date = issue_date;
        this.till_date = till_date;
        this.issue_address = issue_address;
        this.scan_time = scan_time;
        this.image_url = image_url;
        this.image_size = image_size;
        this.status = status;


    }

    public SqliteDetail(int id, String card_name, String card_unique_no, String date_of_birth, String issue_date,
                        String till_date, String birth_place, String issue_address, String image_url,
                        String scan_time, int image_size, String status) {
        this.id = id;

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
        this.status = status;


    }

    public SqliteDetail(String card_name, String card_unique_no, String date_of_birth, String issue_date,
                        String till_date, String birth_place, String issue_address, String image_url,
                        String scan_time, int image_size, String status) {
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
        this.status = status;


    }

    public SqliteDetail(CardDetail cardDetail) {

        scan_time = cardDetail.getScan_time();
//        image_Bitmap = ;
        image_url = cardDetail.getImage_url();
//        image_size= ;
        date_of_birth = cardDetail.getDate_of_birth();
        card_unique_no = cardDetail.getCard_unique_no();
        card_name = cardDetail.getCard_name();

        // Passport and driving licence variable
        issue_date = cardDetail.getIssue_date();
        till_date = cardDetail.getTill_date();
        issue_address = cardDetail.getIssue_address();
        birth_place = cardDetail.getBirth_place();
//        public String status;
        whichcard = cardDetail.getWhichcard();

    }

//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }

    public String getScan_time() {
        return scan_time;
    }

    public void setScan_time(String scan_time) {
        this.scan_time = scan_time;
    }

    public Bitmap getImage_Bitmap() {
        return image_Bitmap;
    }

    public void setImage_Bitmap(Bitmap image_Bitmap) {
        this.image_Bitmap = image_Bitmap;
    }

    public int getImage_size() {
        return image_size;
    }

    public void setImage_size(int image_size) {
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

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWhichcard() {
        return whichcard;
    }

    public void setWhichcard(String whichcard) {
        this.whichcard = whichcard;
    }
}
