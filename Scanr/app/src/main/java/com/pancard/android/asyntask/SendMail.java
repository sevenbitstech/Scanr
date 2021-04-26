package com.pancard.android.asyntask;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.mail.GMailSender;
import com.pancard.android.utility.PreferenceManagement;

/**
 * Created by seven-bits-pc11 on 10/3/17.
 */

public class SendMail extends AsyncTask<Void, Void, Boolean> {


    String body;
    Context context;
    String errorMessage;
    PreferenceManagement preferences;
    private GMailSender m;

    public SendMail(String body, Context context) {
        m = new GMailSender("user@tristonsoft.com", "Creative2000");

        this.body = body;
        this.context = context;
        preferences = Scanner.getInstance().getPreferences();
    }


    @Override
    protected Boolean doInBackground(Void... params) {
        if (Scanner.getInstance().getConnectionDetactor().isConnectingToInternet()) {
            String[] toArr = {Globalarea.firebaseUser.getEmail()};
            m.setTo(toArr);
            m.setFrom("user@tristonsoft.com");
            m.setSubject("New PIN for document scanner application");
            m.setBody("Your new PIN is " + body + " now you can access your card detail.\n \n Thanks,\n Document Scanner Team");

            try {

                if (m.send()) {
                    // success
                    System.out.println("Email log : Email was sent successfully");
                    return true;
                } else {
                    // failure
                    errorMessage = "Email was not sent";
                    System.out.println("Email log : Email was not sent");
                    return false;

                }
            } catch (Exception e) {
                // some other problem
                errorMessage = "There was a problem sending the email";
                System.out.println("Email log : There was a problem sending the email");
                e.printStackTrace();
                return false;

            }
        } else {
            errorMessage = "Check internet connection!!!";
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if (aBoolean) {
//            sendSecurityStatus(true,body);
            Globalarea.actionFire = true;

            preferences.setPinUpdate(true);
            preferences.setPinUpdatedInFirebase(false);
            preferences.setPin(body);
            Toast.makeText(context, "We have sent you PIN!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
        }

    }
}