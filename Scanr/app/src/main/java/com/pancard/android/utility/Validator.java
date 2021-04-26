package com.pancard.android.utility;

import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 7/7/2016.
 */
public class Validator {

    public Validator() {

    }

    public static boolean isValidPanno(String Panno) {
        String PANNO_PATTERN = "[A-Z]{5}[0-9]{4}[A-Z]{1}";

        Pattern pattern = Pattern.compile(PANNO_PATTERN);
        Matcher matcher = pattern.matcher(Panno);
        return matcher.matches();
    }

    public static boolean isValidPanname(String Panname) {
        String PANNO_PATTERN = "^([a-zA-Z_][a-zA-Z_ ]*[a-zA-Z_])$";
        Pattern pattern = Pattern.compile(PANNO_PATTERN);
        Matcher matcher = pattern.matcher(Panname);
        return matcher.matches();
    }

    public static boolean isDrivingLicence(String State) {
        String STATE_PATTERN = "[A-Z]{2}[0-9]{2}[\\s]{1}[0-9]{11}";
        Pattern pattern = Pattern.compile(STATE_PATTERN);
        Matcher matcher = pattern.matcher(State);
        return matcher.matches();
    }


    public static boolean passportNumberVerify(String PassportNo) {

        String PASSNO_PATTERN = "(([a-zA-Z]{1})\\d{7})";
        Pattern pattern = Pattern.compile(PASSNO_PATTERN);
        Matcher matcher = pattern.matcher(PassportNo);
        return matcher.matches();
    }


    public static boolean isDrivingLicenceSecond(String State) {

        String STATE_PATTERN = "[A-Z]{2}[-]{1}[0-9]{13}";
        Pattern pattern = Pattern.compile(STATE_PATTERN);
        Matcher matcher = pattern.matcher(State);
        return matcher.matches();
    }

    public static boolean isDrivingLicenceThird(String State) {
        String STATE_PATTERN = "[A-Z]{2}[0-9]{2}[-]{1}[0-9]{4}[-]{1}[0-9]{7}";
        Pattern pattern = Pattern.compile(STATE_PATTERN);
        Matcher matcher = pattern.matcher(State);
        return matcher.matches();
    }

    public static boolean IsValidDate(EditText pan_edt_birthdate, EditText pan_edt_date_through, EditText pan_edt_date_till, TextView error_msg, ScrollView scrollView) {
        String BirthYear = "";
        String ThroughYear = "";
        String TillYear = "";

        String Birth = "";
        String Through = "";
        String Till = "";

        BirthYear = pan_edt_birthdate.getText().toString();
        ThroughYear = pan_edt_date_through.getText().toString();
        TillYear = pan_edt_date_till.getText().toString();

        System.out.println("Value of birth year" + BirthYear);
        System.out.println("Value of Through Year" + ThroughYear);
        System.out.println("Value of Till year" + TillYear);

        if (!BirthYear.equals("") && !ThroughYear.equals("") && !TillYear.equals("")) {

            System.out.println("Inside If");

            Birth = BirthYear.substring(BirthYear.length() - 4, BirthYear.length());
            Through = ThroughYear.substring(ThroughYear.length() - 4, ThroughYear.length());
            Till = TillYear.substring(TillYear.length() - 4, TillYear.length());


            int BirthYr = Integer.parseInt(Birth);
            int ThroughYr = Integer.parseInt(Through);
            int TillYr = Integer.parseInt(Till);

            System.out.println("BirthYr" + BirthYr);

            if (ThroughYr <= BirthYr) {
                focusOnView(pan_edt_date_through, scrollView);
                error_msg.setVisibility(View.VISIBLE);
                error_msg.setText("Please Enter Valid Through Date");
                return false;
            } else if (TillYr <= ThroughYr) {
                focusOnView(pan_edt_date_till, scrollView);
                error_msg.setVisibility(View.VISIBLE);
                error_msg.setText("Please Enter Valid Till Date");
                return false;
            } else if (TillYr <= BirthYr) {
                focusOnView(pan_edt_date_till, scrollView);
                error_msg.setVisibility(View.VISIBLE);
                error_msg.setText("Please Enter Valid Till Date");
                return false;
            }
        } else if (!BirthYear.equals("") && !ThroughYear.equals("") && TillYear.equals("")) {
            System.out.println("Inside first else If");

            Birth = BirthYear.substring(BirthYear.length() - 4, BirthYear.length());
            Through = ThroughYear.substring(ThroughYear.length() - 4, ThroughYear.length());

            int BirthYr = Integer.parseInt(Birth);
            int ThroughYr = Integer.parseInt(Through);


            System.out.println("BirthYr" + BirthYr);

            if (ThroughYr <= BirthYr) {
                focusOnView(pan_edt_date_through, scrollView);
                error_msg.setVisibility(View.VISIBLE);
                error_msg.setText("Please Enter Valid Through Date");
                return false;
            }
        } else if (!BirthYear.equals("") && ThroughYear.equals("") && !TillYear.equals("")) {
            System.out.println("Inside second else If");

            Birth = BirthYear.substring(BirthYear.length() - 4, BirthYear.length());
            Till = TillYear.substring(TillYear.length() - 4, TillYear.length());

            int BirthYr = Integer.parseInt(Birth);
            int TillYr = Integer.parseInt(Till);

            System.out.println("BirthYr" + BirthYr);

            if (TillYr <= BirthYr) {
                focusOnView(pan_edt_date_till, scrollView);
                error_msg.setVisibility(View.VISIBLE);
                error_msg.setText("Please Enter Valid Till Date");
                return false;
            }
        } else if (!BirthYear.equals("") && ThroughYear.equals("") && TillYear.equals("")) {
            error_msg.setVisibility(View.GONE);
            System.out.println("Inside Third else If");
            return true;
        } else if (BirthYear.equals("") && ThroughYear.equals("") && TillYear.equals("")) {
            error_msg.setVisibility(View.GONE);
            return true;
        } else if (BirthYear.equals("") && !ThroughYear.equals("") && !TillYear.equals("")) {
            error_msg.setVisibility(View.GONE);
            return true;
        }

        return true;
    }

    public static void focusOnView(final EditText editText, final ScrollView scrollView) {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, editText.getTop());
            }
        });
    }

    public static boolean licenceNumberVerify(String Licence_number) {

        if (!Validator.isDrivingLicence(Licence_number)) {

            if (!Validator.isDrivingLicenceSecond(Licence_number)) {

                if (!Validator.isDrivingLicenceThird(Licence_number)) {

                } else {
                    return true;
                }
            } else {
                return true;
            }

        } else {
            return true;
        }
        return false;
    }


}