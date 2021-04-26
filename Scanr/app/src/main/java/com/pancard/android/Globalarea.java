package com.pancard.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.docscan.android.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.DriveOperations.MetadataModel;
import com.pancard.android.activity.newflow.activity.NewHomeActivity;
import com.pancard.android.activity.otheracivity.InfoActivity;
import com.pancard.android.activity.otheracivity.SettingActivity;
import com.pancard.android.activity.scanactivity.DriveListActivity;
import com.pancard.android.database.DBToDriveSync;
import com.pancard.android.database.DriveToDbSync;
import com.pancard.android.model.CardDetail;
import com.pancard.android.model.SecurityStatus;
import com.pancard.android.model.SizeDetail;
import com.pancard.android.model.SqliteDetail;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PreferenceManagement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seven-bits-pc11 on 13/1/17.
 */
public class Globalarea {

    public static String Card_type_activity = "name";
    public static String CurrentDate = "name";

    public static String adharCardText = "AdharCardText";
    public static String panCard2Text = "PanCard2Text";

    public static SqliteDetail SpecificCard = new SqliteDetail();
//    public static CardDetail SpecificCard = new CardDetail();

    public static FirebaseUser firebaseUser;

    // Display width and height
    public static int seekbarProgress = 0;
    public static int maxZoomLevel = 0;
    public static VerticalSeekBar seekbarZoom;

    public static int display_width;
    public static int display_height;

    public static Bitmap document_image;
    public static Uri gallery_image_uri;
    public static Bitmap adharCard_back_image;
    public static Bitmap original_image;

    public static Bitmap firstDisplayImage;
    public static Bitmap secondDisplayImage;

    public static ArrayList<CardDetail> cardDetail = new ArrayList<>();

    public static SizeDetail sizeDetail = new SizeDetail(20000, 20000, 0);
    public static SecurityStatus securityStatus = new SecurityStatus();

    public static boolean actionFire = false;

    public static ArrayList<String> listOfUrl = new ArrayList<>();

    public static List<DriveDocModel> documentPageList = new ArrayList<>();

    public static int gridHeight = 0;


    public static boolean isDriveSyncInProgress = false;
    public static boolean isDbSyncInProgress = false;

    public static boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) Scanner.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

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

    public static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static int pxToDp(Context context, int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);

    }

    public static int getSize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Log.i("TAG", "WM     " + wm);

        Point size = new Point();
        Display display = wm.getDefaultDisplay();
        display.getSize(size);
        int heighty = size.y;
        double heightDouble = heighty / 2.5; // this will make the screen display 2 cardview's and have half of the third one peeking. replace 2.5 with any decimal you desire to make your last cardview peek a certain amount
        int height = (int) Math.rint(heightDouble);
        Log.i("TAG", "height     " + heighty);
        return heighty;

    }

    public static String getStringOfCardDetails(CardDetail cardDetail) {
        Gson gson = new Gson();
        return gson.toJson(cardDetail);
    }

    public static CardDetail getCardDetailOfString(String strCardDetail) {
        Gson gson = new Gson();
        TypeToken<CardDetail> token = new TypeToken<CardDetail>() {
        };
        return gson.fromJson(strCardDetail, token.getType());
    }

    public static List<MetadataModel> getMetadataListOfString(String strMetadataList) {
        Gson gson = new Gson();
        TypeToken<List<MetadataModel>> token = new TypeToken<List<MetadataModel>>() {
        };
        return gson.fromJson(strMetadataList, token.getType());
    }

    public static List<DriveDocModel> getDriveDocListOfString(String strDriveDocModelList) {
        Gson gson = new Gson();
        TypeToken<List<DriveDocModel>> token = new TypeToken<List<DriveDocModel>>() {
        };
        return gson.fromJson(strDriveDocModelList, token.getType());
    }

    public static String getStringOfDriveDocModelList(List<DriveDocModel> driveDocModelList) {
        Gson gson = new Gson();
        TypeToken<List<DriveDocModel>> token = new TypeToken<List<DriveDocModel>>() {
        };
        return gson.toJson(driveDocModelList, token.getType());
    }

    public static String getStringOfDriveDoc(DriveDocModel driveDocModel) {
        Gson gson = new Gson();
        return gson.toJson(driveDocModel);
    }

    public static DriveDocModel getDriveDocOfString(String strDriveDoc) {
        Gson gson = new Gson();
        TypeToken<DriveDocModel> token = new TypeToken<DriveDocModel>() {
        };
        return gson.fromJson(strDriveDoc, token.getType());
    }

    public static void moveToSetting(Activity activity) {
        Intent intent = new Intent(activity, NewHomeActivity.class);
        intent.putExtra(Constants.SAVED_CARD, "true");
        intent.putExtra(Constants.START_FRAGMENT, Constants.SETTINGS_TAG);
        activity.startActivity(intent);
        activity.finish();
        activity.finishAffinity();
    }

    public static void moveToInfoScreen(Activity activity) {
        Intent intent = new Intent(activity, InfoActivity.class);
        activity.startActivity(intent);
    }

    public static void getNoteTextView(TextView textViewNote, Activity activity) {
        PreferenceManagement preferenceManagement = new PreferenceManagement(activity);

        if (!(activity instanceof InfoActivity) && !(activity instanceof SettingActivity)) {
            textViewNote.setOnClickListener(v -> {
                if (GoogleSignIn.getLastSignedInAccount(activity) != null) {
                    Globalarea.moveToSetting(activity);
                } else {
                    Globalarea.moveToInfoScreen(activity);
                }
            });
        }

        if (preferenceManagement.isDriveConnected()) {
            textViewNote.setVisibility(View.GONE);
        } else {
            textViewNote.setText(activity.getResources().getString(R.string.str_note_drive));
            textViewNote.setVisibility(View.VISIBLE);
        }
    }

    public static void setDbToDriveSync(DriveServiceHelper mDriveServiceHelper) {
        if (mDriveServiceHelper != null) {
            DBToDriveSync dbToDriveSync = new DBToDriveSync(Scanner.getInstance().getApplicationContext(), mDriveServiceHelper);
            if (!isDriveSyncInProgress)
                dbToDriveSync.startSyncing();
            else {
                Log.e("Upload inprogress", "yes");
            }
        }
    }

    public static void setDriveToDbSync(DriveServiceHelper mDriveServiceHelper) {
        if (mDriveServiceHelper != null) {
            DriveToDbSync driveToDbSync = new DriveToDbSync(Scanner.getInstance().getApplicationContext(), mDriveServiceHelper);
            if (!isDbSyncInProgress && !isDriveSyncInProgress)
                driveToDbSync.startSyncing();
            else {
                Log.e("download inprogress", "yes");
            }
        }
    }

    public static void addThePageIntoGetDocumentPageList(DriveDocModel driveDocModel) {

        if (documentPageList == null)
            documentPageList = new ArrayList<>();

        documentPageList.add(driveDocModel);

    }

    public static List<DriveDocModel> getDocumentPageList() {
        return documentPageList;
    }

    public static void openAllFilesMenu(Activity activity) {
        startActivity(activity, Constants.document);
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setTitle("Card Type").setItems(new String[]
//                {
//                        "Pan Card", "Passport", "Driving Licence", "Business Card", "Document", "Aadhaar Card", "Credit Card"
//                }, new DialogInterface.OnClickListener()
//        {
//            public void onClick(DialogInterface dialog, int which)
//            {
//
//                if (which == 0) {
//
//                startActivity(activity,Constants.pancard);
//
//        } else if (which == 1) {
//
//                startActivity(activity, Constants.passport);
//
//        } else if (which == 2) {
//
//                startActivity(activity, Constants.licence);
//
//        } else if (which == 3) {
//
//                startActivity(activity, Constants.businesscard);
//
//        } else if (which == 4) {
//
//                startActivity(activity, Constants.document);
//
//        } else if (which == 5) {
//
//                startActivity(activity, Constants.adharcard);
//
//        } else if (which == 6) {
//
//                startActivity(activity, Constants.creditCard);
//
//        }
//            }
//        });
//        builder.show();
    }

    private static void startActivity(Activity activity, String tag) {
        Intent intent = new Intent(activity, DriveListActivity.class);
        intent.putExtra("TAG_CAMERA", tag);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void shareData(Activity activity, CardDetail cardDetail, DriveDocModel driveDocModel) {
        try {

            String shareBody;
            String subject;
//            if (whichcard.equals(Constants.businesscard)) {
//                shareBody = "Business Card Detail : " + edittext_name.getText().toString().trim();
//
//                subject = "Business Card Information";
//
//            } else if (whichcard.equals(Constants.document)) {
            shareBody = "Document Detail : " + getAllCardDetails(cardDetail);
            subject = "Document Information";

            if (cardDetail.getCard_name().contains(Constants.KEY_OCR_OFF_SCAN)) {
                shareBody = "";
            }

//            } else if (whichcard.equals(Constants.pancard)) {
//                shareBody = "PanCard Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
//                        + "\n" + " PanCard Number : " + cardDetail.getCard_unique_no();
//
//                subject = "PanCard Information";
//
//            } else if (whichcard.equals(Constants.creditCard)) {
//                shareBody = "Credit Card Type : " + cardDetail.getCard_name() + "\n" + " Expiry Date : " + cardDetail.getDate_of_birth()
//                        + "\n" + " Credit Card Number : " + cardDetail.getCard_unique_no();
//
//                subject = "PanCard Information";
//
//            } else if (whichcard.equals(Constants.licence)) {
//                shareBody = "Licence Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
//                        + "\n" + " Licence Number : " + cardDetail.getCard_unique_no()
//                        + "\n" + " Licence Issue Date : " + cardDetail.getIssue_date()
//                        + "\n" + " Licence Valid Till : " + cardDetail.getTill_date()
//                        + "\n" + " Address : " + cardDetail.getIssue_address();
//                subject = "Driving Licence Information";
//
//            } else if (whichcard.equals(Constants.passport)) {
//                shareBody = "Passport Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
//                        + "\n" + " Passport Number : " + cardDetail.getCard_unique_no()
//                        + "\n" + " Passport Issue Date : " + cardDetail.getIssue_date()
//                        + "\n" + " Passport Valid Till : " + cardDetail.getTill_date()
//                        + "\n" + " BirthCity : " + cardDetail.getBirth_place()
//                        + "\n" + " IssueCity : " + cardDetail.getIssue_address();
//
//                subject = "PassPort Information";
//
//            } else if (whichcard.equals(Constants.adharcard)) {
//                shareBody = "Adhar Card Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
//                        + "\n" + " Adhar Card Number : " + cardDetail.getCard_unique_no()
//                        + "\n" + " Adhar Card BirthCity : " + cardDetail.getBirth_place()
//                        + "\n" + " Adhar Card Address : " + cardDetail.getIssue_address();
////                Log.e("sharebodys",shareBody);
//                subject = "Adhar Card Information";
//
//            }

            File file = null;
            if (driveDocModel.getImagePath() != null) {
//                String[] urlSeparated = cardDetail.getImage_url().split("/");
//                String fileName = urlSeparated[urlSeparated.length-1];
//                file = new File(getCacheDir(),fileName);
                file = new File(driveDocModel.getImagePath());
            }

            ArrayList<Uri> files = new ArrayList<>();

            if (file != null) {
                Uri uri;

//                uri = Uri.fromFile(file);
                uri = FileProvider.getUriForFile(activity, "com.docscan.android.provider", file);
//                uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);
                Log.i("File path", file.getPath());
                files.add(uri);

//                if (whichcard.equals(Constants.adharcard)) {
//                    if (cardDetail.getIssue_date() != null) {
//                        File _file = new File(cardDetail.getIssue_date());
//                        Uri _uri = Uri.fromFile(_file);
//                        files.add(_uri);
//                    }
//                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                activity.startActivity(Intent.createChooser(shareIntent, "Share Via"));
            } else {
                Toast.makeText(activity, "File could not be found.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getAllCardDetails(CardDetail cardDetail) {
        Log.e("getting details", "ys");
        String allCardDetails = "";
        if (cardDetail != null) {
            if (cardDetail.getCard_name() != null) {

                allCardDetails = allCardDetails + cardDetail.getCard_name();
            }

            if (cardDetail.getCard_unique_no() != null) {
                allCardDetails = allCardDetails + "\n" + cardDetail.getCard_unique_no();
            }

//        if(cardDetail.getIssue_date() != null) {
//            allCardDetails = allCardDetails + cardDetail.getIssue_date();
//        }

            if (cardDetail.getIssue_address() != null) {
                allCardDetails = allCardDetails + "\n" + cardDetail.getIssue_address();
            }

            if (cardDetail.getDate_of_birth() != null) {
                allCardDetails = allCardDetails + "\n" + cardDetail.getDate_of_birth();
            }

            if (cardDetail.getBirth_place() != null) {
                allCardDetails = allCardDetails + "\n" + cardDetail.getBirth_place();
            }

            if (cardDetail.getScan_time() != null) {
                allCardDetails = allCardDetails + "\n" + cardDetail.getScan_time();
            }

            if (cardDetail.getTill_date() != null) {
                allCardDetails = allCardDetails + cardDetail.getTill_date() + "\n";
            }

        } else {

            Log.e("card detail", "is null");
        }

        return allCardDetails;
    }
}
