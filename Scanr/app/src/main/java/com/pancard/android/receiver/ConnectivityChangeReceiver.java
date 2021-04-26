package com.pancard.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.docscan.android.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.utility.PreferenceManagement;

import java.util.Collections;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    private DriveServiceHelper mDriveServiceHelper;
    private PreferenceManagement preference;

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.i("Connection",intent.getAction());
        if (intent.getAction() != null && intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            boolean isConnected = checkInternet(Scanner.getInstance().getApplicationContext());
            if (isConnected) {
                preference = Scanner.getInstance().getPreferences();
                setGoogleDriveService(Scanner.getInstance().getApplicationContext());
                if (mDriveServiceHelper != null) {
                    Globalarea.setDbToDriveSync(mDriveServiceHelper);
                }
//                ComponentName comp = new ComponentName(context.getPackageName(),
//                        DriveSyncService.class.getName());
//                intent.putExtra("isNetworkConnected",isConnected);
//                context.startService(intent.setComponent(comp));
//                Toast.makeText(Scanner.getInstance().getApplicationContext(), "Network is available", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("Not Connected", "Internet");
//                Toast.makeText(Scanner.getInstance().getApplicationContext(), "Network is not available", Toast.LENGTH_SHORT).show();
            }

        }
    }

    boolean checkInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = false;
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting() && cm.isDefaultNetworkActive()) {
            isConnected = true;
        }

        return isConnected;
    }

    private void setGoogleDriveService(Context context) {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

        if (preference.isDriveConnected()) {
            if (account != null) {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        context, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());
                com.google.api.services.drive.Drive googleDriveService =
                        new com.google.api.services.drive.Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName(context.getString(R.string.app_name))
                                .build();

                mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                Log.e("drive service", "helper setup");

            }
        }

    }
}
