package com.pancard.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by seven-bits-pc11 on 24/5/17.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (isOnline(context)) {
            try {
//            Toast.makeText(context, "Internet Connection is Available...", Toast.LENGTH_LONG).show();
                context.sendBroadcast(new Intent("INTERNET"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }  //            Toast.makeText(context, "Please Check internet connection!!!", Toast.LENGTH_LONG).show();

    }

//	public boolean isInternetOn() {
//
//		// get Connectivity Manager object to check connection
//		ConnectivityManager connec = (ConnectivityManager) Scanner.getInstance().getSystemService(Scanner.getInstance().CONNECTIVITY_SERVICE);
//
//		// Check for network connections
//		if (connec != null && connec.getNetworkInfo(0) != null && connec.getNetworkInfo(1) != null) {
//			if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
//					connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
//					connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
//					connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
//
//
//				return true;
//
//			} else if (
//					connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
//							connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
//
//				return false;
//			}
//		}
//		return false;
//	}

    public boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        }
        return false;
    }
}