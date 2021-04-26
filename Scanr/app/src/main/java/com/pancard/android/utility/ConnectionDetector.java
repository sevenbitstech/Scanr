package com.pancard.android.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionDetector {

    private Context _context;

    public ConnectionDetector(Context context) {
        this._context = context;
    }

    public static boolean isInternetAccessible() {
        try {

            System.out.println("ConnectionDetector InternetAccessiblity is Testting..");

            HttpURLConnection urlc = (HttpURLConnection) (new URL("https://www.google.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException ex) {
            System.out.println("ConnectionDetector Error:=" + ex.toString());
            return false;
        }
    }

    /**
     * Checking for all possible internet providers
     **/
//    public boolean isConnectingToInternet() {
//
//        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivity != null) {
//            NetworkInfo[] info = connectivity.getAllNetworkInfo();
//            if (info != null)
//                for (int i = 0; i < info.length; i++)
//                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//                        return true;
//                    }
//
//        }
//        return false;
//    }
    public boolean isConnectingToInternet() {

        return isConnectingToInternet1();

//        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivity != null) {
//
//            NetworkInfo wifi = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//            NetworkInfo mobile = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//
//            if (wifi.isConnectedOrConnecting()) {
//                System.out.println("ConnectionDetector Wifi Network is Connected");
//                return isInternetAccessible();
//            } else if (mobile.isConnectedOrConnecting()) {
//                System.out.println("ConnectionDetector Mobile Data is Connected");
//                return isInternetAccessible();
//            }
//        }
//        return false;
    }

    public boolean isConnectingToInternet1() {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }
}
