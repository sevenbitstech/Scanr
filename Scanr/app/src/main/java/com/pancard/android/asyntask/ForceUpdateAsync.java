package com.pancard.android.asyntask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.docscan.android.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pancard.android.Scanner;
import com.pancard.android.utility.PreferenceManagement;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class ForceUpdateAsync extends AsyncTask<String, String, JSONObject> {

    private PreferenceManagement preferences;
    private String whatsNewText;
    private String latestVersion;
    private String currentVersion;
    private Context context;

    public ForceUpdateAsync(String currentVersion, Context context) {
        this.currentVersion = currentVersion;
        this.context = context;
        preferences = Scanner.getInstance().getPreferences();
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        try {
            Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + context.getPackageName() + "&hl=en")
                    .timeout(30000)
//                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();

            if (document != null) {
                Elements elements = document.getElementsContainingText("What's New");
                for (Element ele : elements) {
                    if (ele.siblingElements() != null) {
                        if (ele.text().startsWith("What's New") && !ele.text().replace("What's New", "").trim().isEmpty()) {

                            whatsNewText = ele.text();
                        }
                    }
                }

                Elements element = document.getElementsContainingOwnText("Current Version");
                for (Element ele : element) {
                    if (ele.siblingElements() != null) {
                        Elements sibElemets = ele.siblingElements();
                        for (Element sibElemet : sibElemets) {
                            latestVersion = sibElemet.text();
                            Log.e("Document 1 : ", latestVersion);

                        }
                    }
                }
            }

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {

        if (latestVersion != null) {
            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                Log.e("latestVersion : ", latestVersion);
                Log.e("currentVersion : ", currentVersion);


                String storedLatestVersion = preferences.getLatestVersion();
                if (storedLatestVersion != null && !storedLatestVersion.equals(latestVersion)) {
                    preferences.setLatestVersionNever(false);
//                    DocVerifier.getInstance().getUtils().setPrefrences("LatestVersionNever", null);
                }
                if (!preferences.isLatestVersionNever()) {
                    checkVersionChange(currentVersion, latestVersion, whatsNewText);
                }
            }
        }
        super.onPostExecute(jsonObject);
    }

    private void showForceUpdateDialog(String whatsNewText) {

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);

        alertDialogBuilder.setTitle(context.getString(R.string.youAreNotUpdatedTitle));
        alertDialogBuilder.setMessage(whatsNewText);
//        alertDialogBuilder.setMessage(context.getString(R.string.youAreNotUpdatedMessage) + " " + latestVersion + context.getString(R.string.youAreNotUpdatedMessage1));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.update, (dialog, which) -> {
            dialog.cancel();
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
        });
        alertDialogBuilder.show();
    }

    private void checkVersionChange(String oldVersion, String newVersion, String whatsNewText) {

        if (!oldVersion.contains(".") || !newVersion.contains(".")) {
            return;
        }

        String[] oldVparts = oldVersion.split("\\."); // escape .
        String[] newVparts = newVersion.split("\\."); // escape .
//        String part1 = parts[0];
//        String part2 = parts[1];

        if (oldVparts.length < 2 || newVparts.length < 2) {
            return;
        }

        if (whatsNewText == null) {
            whatsNewText = context.getString(R.string.youAreNotUpdatedMessage);
        }

        int oldPart0 = Integer.parseInt(oldVparts[0]);
        int oldPart1 = Integer.parseInt(oldVparts[1]);
        int oldPart2 = Integer.parseInt(oldVparts[2]);

        int newPart0 = Integer.parseInt(newVparts[0]);
        int newPart1 = Integer.parseInt(newVparts[1]);
        int newPart2 = Integer.parseInt(newVparts[2]);

        if (oldPart0 < newPart0) {
//            if (!oldVparts[0].equals("0"))
            showForceUpdateDialog(whatsNewText);
        } else if (oldPart0 == newPart0) {
            if (oldPart1 < newPart1) {
                showUpdateDialog(whatsNewText);
            } else if (oldPart1 == newPart1) {
                if (oldPart2 < newPart2) {
                    showUpdateDialog(whatsNewText);
                }
            }
        }
    }

    private void showUpdateDialog(String whatsNewText) {

        if (!((Activity) context).isFinishing()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);

            alertDialogBuilder.setTitle(context.getString(R.string.youAreNotUpdatedTitle));
            alertDialogBuilder.setMessage(whatsNewText);

            alertDialogBuilder.setPositiveButton(R.string.update, (dialog, which) -> {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
                dialog.cancel();
            }).setNeutralButton(R.string.not_now, (dialog, which) -> dialog.dismiss())
                    .setNegativeButton(R.string.never, (dialog, which) -> {
                        dialog.dismiss();
                        preferences.setLatestVersion(latestVersion);
                        preferences.setLatestVersionNever(true);
                    });
            alertDialogBuilder.show();
        }

    }
}
