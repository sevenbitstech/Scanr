package com.pancard.android.asyntask;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.listener.TaskListener;
import com.pancard.android.model.CardDetail;
import com.pancard.android.utility.Firebase_ImageLoader;

import java.util.ArrayList;

/**
 * Created by seven-bits-pc11 on 8/6/17.
 */
public class FirebaseDownloaderTask extends AsyncTask<String, String, Boolean> {
    TaskListener taskListener;
    String message = "Oops Something went wrong!!";
    ArrayList<CardDetail> imageURL = new ArrayList<>();
    //   private int height;
    Firebase_ImageLoader firebase_imageLoader;

    public FirebaseDownloaderTask(TaskListener taskListener, ArrayList<CardDetail> imageURL) {
        this.taskListener = taskListener;
        this.imageURL = imageURL;
        //    this.height = height;
        firebase_imageLoader = new Firebase_ImageLoader(Scanner.getInstance());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {

            if (Scanner.getInstance().getConnectionDetactor().isConnectingToInternet()) {

                for (int i = 0; i < Globalarea.listOfUrl.size(); i++) {

                    Bitmap bitmap = firebase_imageLoader.getBitmap(Globalarea.listOfUrl.get(i), 400);
                }

                return true;
            } else {
                System.out.println("*****Please connect to working Internet connection");
                message = "Check internet connection!!!";
                return false;
            }

        } catch (Exception ex) {
            message = "Oops Something went wrong!!";
            System.out.println("doInBackground Error : " + ex.toString());
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            taskListener.onTaskFinished("GetAllData");

        } else {
            if (message == null) {
                message = "Oops Something went wrong..";
            }
            taskListener.onTaskFinished(message);
        }
    }

}