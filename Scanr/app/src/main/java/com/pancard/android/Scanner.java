package com.pancard.android;

import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.StrictMode;

import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.PreferenceManagement;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/*
 * Created by seven-bits-pc11 on 18/1/17.
 */
public class Scanner extends MultiDexApplication {

    public static String Current_Version = "Current_Version";
    public static int Current_Version_Code = 0;
    public static InterstitialAd mInterstitialAd;
    private static Scanner mInstance;
    //    public Utils utils;
    Calendar myCalendar;
    ConnectionDetector connectionDetector;
    PreferenceManagement preferences;

    public static void startGame() {
        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
        if (!mInterstitialAd.isLoading()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    public static void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    public static synchronized Scanner getInstance() {
        return mInstance;
    }

    public static boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) Scanner.getInstance().getSystemService(CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {

            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {

            return false;
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        myCalendar = Calendar.getInstance();
        preferences = new PreferenceManagement(this);

        try {
            Fabric.with(this, new Crashlytics());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Scanner.Current_Version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            Scanner.Current_Version_Code = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Globalarea.CurrentDate = df.format(myCalendar.getTime());

//        MobileAds.initialize(this, "ca-app-pub-2404474835802386~9054675954");

        // Create the InterstitialAd and set the adUnitId.
//        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
//        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id));
//        startGame();
        connectionDetector = new ConnectionDetector(this);

        //fixme: Temporary fixed for File exposing while ShareIntent
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public PreferenceManagement getPreferences() {
        if (preferences != null)
            return preferences;
        else
            return new PreferenceManagement(this);
    }

    public ConnectionDetector getConnectionDetactor() {

        if (connectionDetector == null) {
            connectionDetector = new ConnectionDetector(this);
        }
        return connectionDetector;

    }
}
