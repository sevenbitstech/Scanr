package com.pancard.android.activity.scanactivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.docscan.android.R;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.otheracivity.ContactUs;
import com.pancard.android.activity.otheracivity.HomeActivity;
import com.pancard.android.activity.otheracivity.SettingActivity;
import com.pancard.android.database.DriveDocRepo;
import com.pancard.android.database.SyncStatus;
import com.pancard.android.listener.TaskListener;
import com.pancard.android.model.CardDetail;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.Firebase_ImageLoader;
import com.pancard.android.utility.GlideApp;
import com.pancard.android.utility.PreferenceManagement;
import com.pancard.android.utility.Validator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SpecificPage extends AppCompatActivity implements TaskListener {

    EditText edittext_name, edittext_number, edittext_dob, edittext_validthrough, edittext_validtilldate, edittext_bithplace, edittext_issueplace;
    TextView error_msg;
    ScrollView scrollView;
    Button btn_save, btn_cancel, btn_edit, btn_delete, btn_share, btn_nxt;
    LinearLayout Linear_btns, Linear_edit;
    ImageView specific_image;
    Calendar myCalendar;
    //    RetriveandSetOneSection retriveandSetOneSection;
    ProgressDialog pgdialog;
    ActionBar actionBar;
    LinearLayout date_portion, address_portion, linearlayout_common;
    String whichcard;
    //    DatabaseHandler handler;
    Firebase_ImageLoader firebase_imageLoader;
    DriveDocModel driveDocModel;
    CardDetail cardDetail;
    boolean imageorder = false;
    ConnectionDetector connectionDetector;
    //    boolean isCreatingTextFile;
    PreferenceManagement preferences;
    String DOCUMENT_ORIGINAL;
    String DOCUMENT_TYPE = Constants.document;
    //    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            new FirebaseManagement(context).init(context);
//        }
//    };
    private DriveServiceHelper mDriveServiceHelper;
    private DriveDocRepo driveDocRepo;
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_licence);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        firebase_imageLoader = new Firebase_ImageLoader(SpecificPage.this);
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

//        retriveandSetOneSection = new RetriveandSetOneSection(this);
        cardDetail = new CardDetail();
        pgdialog = new ProgressDialog(SpecificPage.this);
        pgdialog.setMessage(getString(R.string.msg_please_wait));
        pgdialog.setCancelable(false);
        myCalendar = Calendar.getInstance();
        date_portion = findViewById(R.id.date_portion);
        address_portion = findViewById(R.id.address_portion);
        linearlayout_common = findViewById(R.id.linearlayout_common);
//        handler = new DatabaseHandler(this);

        btn_edit = findViewById(R.id.btn_edit);
        btn_delete = findViewById(R.id.btn_delete);
        btn_save = findViewById(R.id.btn_save);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_share = findViewById(R.id.btn_share);
        btn_nxt = findViewById(R.id.btn_nxt);

        error_msg = findViewById(R.id.txt_error);
        scrollView = findViewById(R.id.scrollView);
        edittext_name = findViewById(R.id.edt_Name);
        edittext_number = findViewById(R.id.edt_No);
        edittext_dob = findViewById(R.id.edt_Dob);
        edittext_validthrough = findViewById(R.id.edt_ValidThrough);
        edittext_validtilldate = findViewById(R.id.edt_ValidTill);
        edittext_bithplace = findViewById(R.id.edt_BirthPlace);
        edittext_issueplace = findViewById(R.id.edt_IssuePlace);

        Linear_edit = findViewById(R.id.linear_edit);
        Linear_btns = findViewById(R.id.linear_btns);
        specific_image = findViewById(R.id.specific_image);

        preferences = Scanner.getInstance().getPreferences();


        actionBar = getSupportActionBar();

//        new DownloadImage().execute();

        Bitmap bmp = BitmapFactory.decodeFile(Globalarea.SpecificCard.getImage_url());
        specific_image.setImageBitmap(bmp);
        Globalarea.document_image = bmp;

//        specific_image.setImageBitmap(Globalarea.bitmapArrayList.get(Globalarea.position));
        //       firebase_imageLoader.DisplayImage(Globalarea.SpecificCard.getImage_url(),specific_image,400,R.drawable.ds_logo);

        //todo: Uncomment below code for separate DB with Firebase userid
//        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
//            driveDocRepo = new DriveDocRepo(this, FirebaseAuth.getInstance().getCurrentUser().getUid());
//        }else {
//            Toast.makeText(this, "May be your session has expired. Please login again.", Toast.LENGTH_SHORT).show();
//            FirebaseAuth.getInstance().signOut();
//            preferences.removeAllData();
//            Globalarea.firebaseUser = null;
//            Intent intent = new Intent(this, SignInActivity.class);
//            startActivity(intent);
//            finish();
//        }

        driveDocRepo = new DriveDocRepo(this);

        //todo: Uncomment below line for unable the initialization of the googel drive service
//        setGoogleDriveService();

        if (getIntent() != null) {
            driveDocModel = (DriveDocModel) getIntent().getSerializableExtra(Constants.CARD_DATAT);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String data = extras.getString("TAG_CAMERA");
            if (data != null) {
                DOCUMENT_ORIGINAL = data;
                data = DOCUMENT_TYPE;
                whichcard = DOCUMENT_TYPE;
                Scanner.showInterstitial();
                if (data.equals(Constants.businesscard)) {
                    documentDetail();

                } else if (data.equals(Constants.document)) {
                    Log.e("showing", "document");
                    documentDetail();

                } else if (data.equals(Constants.pancard)) {
                    pancardDetail();

                } else if (data.equals(Constants.creditCard)) {
                    creditcardDetail();

                } else if (data.equals(Constants.licence)) {
                    licenceDetail();

                } else if (data.equals(Constants.passport)) {
                    passportDetail();

                } else if (data.equals(Constants.adharcard)) {
                    passportDetail();

                }
            }
        }

        connectionDetector = new ConnectionDetector(this);

        specific_image.setOnClickListener(v -> {
            Intent intent = new Intent(SpecificPage.this, FullScreenImageviewerActivity.class);
            intent.putExtra("TAG_CAMERA", whichcard);
            intent.putExtra("activity", "specificactivity");
            intent.putExtra(Constants.CARD_DATAT, driveDocModel);
            startActivity(intent);
//                startActivity(Globalarea.getIntentForFileExplorer(Globalarea.SpecificCard.getImage_url()));
        });


        btn_cancel.setOnClickListener(v -> {
            specific_image.setClickable(true);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            Linear_btns.setVisibility(View.GONE);
            Linear_edit.setVisibility(View.VISIBLE);

            cancleData();
        });

        btn_edit.setOnClickListener(v -> {
            specific_image.setClickable(false);
            Linear_btns.setVisibility(View.VISIBLE);
            Linear_edit.setVisibility(View.GONE);

            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            editData();
        });

        btn_save.setOnClickListener(v -> {

//            updateFileOfDrive();
            //upload file to drive


            saveData();
        });


        btn_delete.setOnClickListener(v -> dialog_open());

        btn_share.setOnClickListener(v -> ShareData());
//        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET"));

        //todo: Uncommnet below code register drive sync broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);
    }

    // Update Drive Document method
//    private void updateFileOfDrive() {
//
//        if (connectionDetector != null && connectionDetector.isConnectingToInternet()) {
//
//            if (cardDetail != null) {
//
//                String subFolderName = driveDocModel.getFolderName();
//                getFileIdAndUpload(subFolderName, cardDetail);
//            } else {
//                pgdialog.dismiss();
//                Toast.makeText(this, "Something is wrong with the selected file. Please try again", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(this, getString(R.string.internetConnectionFail), Toast.LENGTH_SHORT).show();
//            pgdialog.dismiss();
//        }
//
//    }

//    private void getFileIdAndUpload(String subFolderName,
//                                    CardDetail cardDetail) {
//        if (subFolderName != null) {
//
//            ScanRDriveOperations.getFolderIdOrCreate(subFolderName,
//                    mDriveServiceHelper, new ScanRDriveOperations.OnCompleteDriveOperations() {
//                        @Override
//                        public void onSuccess(String folderId) {
////                            Log.e("my folder id", "is: " + folderId);
////                            Log.e("my folder id", "is: " + driveDocModel.getFolderId());
//                            pgdialog.dismiss();
//                            updateTextFile(cardDetail, folderId);
//                        }
//
//                        @Override
//                        public void onFailure(Exception e, String message) {
//                            pgdialog.dismiss();
//                            Toast.makeText(SpecificPage.this, message, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        } else {
//            pgdialog.dismiss();
//        }
//    }

//    private void updateTextFile(CardDetail cardDetail, String folderId) {
//
//        pgdialog.show();
//        isCreatingTextFile = true;
//        Gson gson = new Gson();
//        String jsonText = gson.toJson(cardDetail);
//
//        String fileid = driveDocModel.getTextfileId();
//
//        if (fileid != null) {
//
//            mDriveServiceHelper.updateTextFile(fileid, jsonText, folderId)
//                    .addOnSuccessListener(googleDriveFileHolder -> {
//                        isCreatingTextFile = false;
//                        Log.i("uploaded text file", "yes");
//                        pgdialog.dismiss();
//                        Toast.makeText(this, "Successfully updated the text file in the drive", Toast.LENGTH_SHORT).show();
////                    openActivity();
//                    })
//                    .addOnFailureListener(e -> {
//                        isCreatingTextFile = false;
//                        pgdialog.dismiss();
//                        Log.i("uploaded text file", "no");
//                        e.printStackTrace();
//                        Toast.makeText(this, "Issue in updating a file into the drive", Toast.LENGTH_SHORT).show();
//
//                    });
//
//        }
//
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
////        if (Globalarea.isInternetOn()) {
////            if (Globalarea.actionFire) {
////                Globalarea.actionFire = false;
////                new FirebaseManagement(this).init(this);
////            }
////        }
//    }

    private void saveData() {

        pgdialog.setMessage(getString(R.string.msg_please_wait));
        pgdialog.setCancelable(false);


        if (whichcard.equals(Constants.licence)) {
            if (!licenceNumberVerify(edittext_number.getText().toString().trim())) {
                edittext_number.setError(getString(R.string.error_licence_number));
                edittext_number.setFocusable(true);
                edittext_number.requestFocus();
            } else {
                if (Validator.IsValidDate(edittext_dob, edittext_validthrough, edittext_validtilldate, error_msg, scrollView)) {
//                    pgdialog.show();
                    error_msg.setVisibility(View.GONE);
                    String card_holder_name;
                    if (edittext_name.getText().toString().trim().equals("")) {
                        card_holder_name = "Unknown";
                    } else {
                        card_holder_name = edittext_name.getText().toString().trim();
                    }
                    cardDetail = new CardDetail(card_holder_name, edittext_number.getText().toString().trim(),
                            edittext_dob.getText().toString().trim(), edittext_validthrough.getText().toString().trim(),
                            edittext_validtilldate.getText().toString().trim(),
                            edittext_issueplace.getText().toString().trim(), cardDetail.getScan_time(),
                            cardDetail.getImage_url(), cardDetail.getImage_size());
//                    updateCard();
//                    updateFileOfDrive();
                    updateDocumentFromLocal();
                }
            }
        } else if (whichcard.equals(Constants.passport)) {
            if (IsValidPassport()) {
//                pgdialog.show();
                if (Validator.IsValidDate(edittext_dob, edittext_validthrough, edittext_validtilldate, error_msg, scrollView)) {

                    error_msg.setVisibility(View.GONE);
                    String card_holder_name;

                    if (edittext_name.getText().toString().trim().equals("")) {
                        card_holder_name = "Unknown";
                    } else {
                        card_holder_name = edittext_name.getText().toString().trim();
                    }
//                    fillModelValue(card_holder_name);
                    cardDetail = new CardDetail(card_holder_name, edittext_number.getText().toString().trim(),
                            edittext_dob.getText().toString().trim(), edittext_validthrough.getText().toString().trim(),
                            edittext_validtilldate.getText().toString().trim(),
                            edittext_bithplace.getText().toString().trim(), edittext_issueplace.getText().toString().trim(),
                            cardDetail.getImage_url(), cardDetail.getScan_time(), cardDetail.getImage_size());

//                    updateCard();
//                    updateFileOfDrive();
                    updateDocumentFromLocal();
                }
            }
        } else if (whichcard.equals(Constants.adharcard)) {

            cardDetail = new CardDetail(edittext_name.getText().toString().trim(), edittext_number.getText().toString().trim(),
                    edittext_dob.getText().toString().trim(), edittext_validthrough.getText().toString().trim(),
                    edittext_validtilldate.getText().toString().trim(),
                    edittext_bithplace.getText().toString().trim(), edittext_issueplace.getText().toString().trim(),
                    cardDetail.getImage_url(), cardDetail.getScan_time(), cardDetail.getImage_size());
//                  updateFileOfDrive();
//                  updateCard();
            updateDocumentFromLocal();

        } else if (whichcard.equals(Constants.businesscard) || whichcard.equals(Constants.document)) {
            if (IsValidBusinessCard()) {
//                pgdialog.show();
                cardDetail = new CardDetail(edittext_name.getText().toString().trim()
                        , cardDetail.getScan_time(), cardDetail.getImage_url(),
                        cardDetail.getImage_size());
//                updateFileOfDrive();
//                updateCard();
                updateDocumentFromLocal();
            }
        } else if (whichcard.equals(Constants.pancard)) {
            if (IsValidPanCard()) {
//                    handler.updatePancardList(edittext_name.getText().toString().trim(), edt_birthdate.getText().toString().trim(), edt_panno.getText().toString().trim(), Globalarea.SpecificCard.getId());
//                displayProgressDialog();
                cardDetail = new CardDetail(edittext_name.getText().toString().trim(),
                        edittext_dob.getText().toString().trim(),
                        edittext_number.getText().toString().trim(),
                        cardDetail.getScan_time(),
                        cardDetail.getImage_url(), cardDetail.getImage_size());
//                updateFileOfDrive();
//                updateCard();
                updateDocumentFromLocal();
            }
        } else if (whichcard.equals(Constants.creditCard)) {
            if (edittext_number.getText().toString().trim().length() > 9) {
//                    handler.updatePancardList(edittext_name.getText().toString().trim(), edt_birthdate.getText().toString().trim(), edt_panno.getText().toString().trim(), Globalarea.SpecificCard.getId());
//                displayProgressDialog();
                cardDetail = new CardDetail(edittext_name.getText().toString().trim(),
                        edittext_dob.getText().toString().trim(),
                        edittext_number.getText().toString().trim(),
                        cardDetail.getScan_time(),
                        cardDetail.getImage_url(), cardDetail.getImage_size());
//                  updateFileOfDrive();
//                  updateCard();
                updateDocumentFromLocal();
            } else {
                Toast.makeText(this, getString(R.string.error_creditcard_number), Toast.LENGTH_LONG).show();
            }
        }

    }

    private void updateDocumentFromLocal() {
        if (driveDocModel != null) {
            driveDocModel.setCardDetail(cardDetail);
            String strUpdatedCardDetails = Globalarea.getStringOfCardDetails(driveDocModel.getCardDetail());

            driveDocModel.setCardDetail(driveDocModel.getCardDetail());
            driveDocModel.setJsonText(strUpdatedCardDetails);
            driveDocModel.setSyncStatus(SyncStatus.unsynced.toString());

            int updated = driveDocRepo.addDriveDocInfo(driveDocModel);
            if (updated > 0) {
                Toast.makeText(this, "Document Updated Successfully.", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private void updateCard() {
//        specific_image.setClickable(true);
//        Globalarea.actionFire = true;
////        Globalarea.SpecificCard = handler.getSingleRow(whichcard, Globalarea.SpecificCard.getScan_time());
////        Globalarea.SpecificCard =  cardDetail;
////        if (Globalarea.SpecificCard.getStatus().equals("true")) {
////            handler.updateSqliteRow(cardDetail, whichcard, "update");
////        } else {
////            handler.updateSqliteRow(cardDetail, whichcard, "false");
////
////        }
//
//        Toast.makeText(getApplicationContext(), getString(R.string.success_saved_info), Toast.LENGTH_LONG).show();
//        Linear_btns.setVisibility(View.GONE);
//        Linear_edit.setVisibility(View.VISIBLE);
//        cancleData();
//        actionBar.setTitle(edittext_name.getText().toString());
//    }

//    public void displayProgressDialog() {
//        pgdialog.setMessage(getString(R.string.msg_please_wait));
//        pgdialog.setCancelable(false);
//        pgdialog.show();
//    }


    private void editData() {
        edittext_name.setEnabled(true);
        edittext_number.setEnabled(true);
        edittext_dob.setEnabled(true);
        edittext_validthrough.setEnabled(true);
        edittext_validtilldate.setEnabled(true);
        edittext_issueplace.setEnabled(true);
        edittext_bithplace.setEnabled(true);
        edittext_name.requestFocus();

        edittext_name.setTextColor(getResources().getColor(R.color.salmon));
        edittext_number.setTextColor(getResources().getColor(R.color.salmon));
        edittext_dob.setTextColor(getResources().getColor(R.color.salmon));
        edittext_validthrough.setTextColor(getResources().getColor(R.color.salmon));
        edittext_validtilldate.setTextColor(getResources().getColor(R.color.salmon));
        edittext_issueplace.setTextColor(getResources().getColor(R.color.salmon));
        edittext_bithplace.setTextColor(getResources().getColor(R.color.salmon));
    }

    private void cancleData() {
//        Globalarea.SpecificCard = handler.getSingleRow(whichcard, Globalarea.SpecificCard.getScan_time());

        edittext_name.setEnabled(false);
        edittext_number.setEnabled(false);
        edittext_dob.setEnabled(false);
        edittext_validthrough.setEnabled(false);
        edittext_validtilldate.setEnabled(false);
        edittext_bithplace.setEnabled(false);
        edittext_issueplace.setEnabled(false);

        setDataInEditText();

        edittext_name.setTextColor(getResources().getColor(R.color.black));
        edittext_number.setTextColor(getResources().getColor(R.color.black));
        edittext_dob.setTextColor(getResources().getColor(R.color.black));
        edittext_validthrough.setTextColor(getResources().getColor(R.color.black));
        edittext_validtilldate.setTextColor(getResources().getColor(R.color.black));
        edittext_issueplace.setTextColor(getResources().getColor(R.color.black));
        edittext_bithplace.setTextColor(getResources().getColor(R.color.black));

    }


    private void ShareData() {
        try {

            String shareBody = null;
            String subject = null;
            if (whichcard.equals(Constants.businesscard)) {
                shareBody = "Business Card Detail : " + edittext_name.getText().toString().trim();

                subject = "Business Card Information";

            } else if (whichcard.equals(Constants.document)) {
                shareBody = "Document Detail : " + edittext_name.getText().toString().trim();
                subject = "Document Information";

            } else if (whichcard.equals(Constants.pancard)) {
                shareBody = "PanCard Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
                        + "\n" + " PanCard Number : " + cardDetail.getCard_unique_no();

                subject = "PanCard Information";

            } else if (whichcard.equals(Constants.creditCard)) {
                shareBody = "Credit Card Type : " + cardDetail.getCard_name() + "\n" + " Expiry Date : " + cardDetail.getDate_of_birth()
                        + "\n" + " Credit Card Number : " + cardDetail.getCard_unique_no();

                subject = "PanCard Information";

            } else if (whichcard.equals(Constants.licence)) {
                shareBody = "Licence Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
                        + "\n" + " Licence Number : " + cardDetail.getCard_unique_no()
                        + "\n" + " Licence Issue Date : " + cardDetail.getIssue_date()
                        + "\n" + " Licence Valid Till : " + cardDetail.getTill_date()
                        + "\n" + " Address : " + cardDetail.getIssue_address();
                subject = "Driving Licence Information";

            } else if (whichcard.equals(Constants.passport)) {
                shareBody = "Passport Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
                        + "\n" + " Passport Number : " + cardDetail.getCard_unique_no()
                        + "\n" + " Passport Issue Date : " + cardDetail.getIssue_date()
                        + "\n" + " Passport Valid Till : " + cardDetail.getTill_date()
                        + "\n" + " BirthCity : " + cardDetail.getBirth_place()
                        + "\n" + " IssueCity : " + cardDetail.getIssue_address();

                subject = "PassPort Information";

            } else if (whichcard.equals(Constants.adharcard)) {
                shareBody = "Adhar Card Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
                        + "\n" + " Adhar Card Number : " + cardDetail.getCard_unique_no()
                        + "\n" + " Adhar Card BirthCity : " + cardDetail.getBirth_place()
                        + "\n" + " Adhar Card Address : " + cardDetail.getIssue_address();
//                Log.e("sharebodys",shareBody);
                subject = "Adhar Card Information";

            }

            File file = null;
            if (cardDetail.getImage_url() != null) {
//                String[] urlSeparated = cardDetail.getImage_url().split("/");
//                String fileName = urlSeparated[urlSeparated.length-1];
//                file = new File(getCacheDir(),fileName);
                file = new File(cardDetail.getImage_url());
            }

            ArrayList<Uri> files = new ArrayList<>();

            if (file != null) {
                Uri uri;

//                uri = Uri.fromFile(file);
                uri = FileProvider.getUriForFile(this, "com.docscan.android.provider", file);
//                uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);
                Log.i("File path", file.getPath());
                files.add(uri);

                if (whichcard.equals(Constants.adharcard)) {
                    if (cardDetail.getIssue_date() != null) {
                        File _file = new File(cardDetail.getIssue_date());
                        Uri _uri = Uri.fromFile(_file);
                        files.add(_uri);
                    }
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                startActivity(Intent.createChooser(shareIntent, "Share Via"));
            } else {
                Toast.makeText(this, "File could not be found.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void documentDetail() {
        Log.e("setting ", "doc detail");
        edittext_name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        String allCardDetails;

        edittext_name.setText(cardDetail.getCard_name());

        edittext_name.setHint("Detail");
        if (actionBar != null)
            actionBar.setTitle(edittext_name.getText().toString());
        edittext_number.setVisibility(View.GONE);
        edittext_dob.setVisibility(View.GONE);

        setDataInEditText();

        allCardDetails = getAllCardDetails();
        edittext_name.setText(allCardDetails);
    }

    private String getAllCardDetails() {
        Log.e("getting details", "ys");
        String allCardDetails = "";
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

        return allCardDetails;
    }

    private void pancardDetail() {
        edittext_number.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        if (cardDetail != null) {
            edittext_name.setText(cardDetail.getCard_name());
            edittext_dob.setText(cardDetail.getDate_of_birth());
            edittext_number.setText(cardDetail.getCard_unique_no());
        }
        if (actionBar != null)
            actionBar.setTitle(edittext_name.getText().toString());

        setDataInEditText();
        setPanCardManagement();

    }

    private void creditcardDetail() {

        edittext_dob.setHint(getString(R.string.hint_expiry));
        edittext_name.setHint(getString(R.string.hint_card_type));
        edittext_number.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext_number.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        if (cardDetail != null) {
            edittext_name.setText(cardDetail.getCard_name());
            edittext_dob.setText(cardDetail.getDate_of_birth());
            edittext_number.setText(cardDetail.getCard_unique_no());
        }
        if (actionBar != null)
            actionBar.setTitle(edittext_name.getText().toString());

        setDataInEditText();
        setPanCardManagement();

    }

    private void passportDetail() {
        date_portion.setVisibility(View.VISIBLE);
        address_portion.setVisibility(View.VISIBLE);

        if (whichcard.equals(Constants.adharcard)) {

            edittext_number.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
            edittext_number.setInputType(InputType.TYPE_CLASS_NUMBER);
            date_portion.setVisibility(View.GONE);
        } else {
            edittext_number.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        }

        if (whichcard.equals(Constants.adharcard)) {
            final Bitmap front = BitmapFactory.decodeFile(cardDetail.getImage_url());
            final Bitmap back = BitmapFactory.decodeFile(cardDetail.getIssue_date());

            Globalarea.firstDisplayImage = front;
            Globalarea.secondDisplayImage = back;
            btn_nxt.setOnClickListener(v -> {
                if (imageorder) {
                    specific_image.setImageBitmap(front);
                    Globalarea.firstDisplayImage = front;
                    Globalarea.secondDisplayImage = back;
                    imageorder = false;
                } else {
                    imageorder = true;
                    Globalarea.firstDisplayImage = back;
                    Globalarea.secondDisplayImage = front;
                    specific_image.setImageBitmap(back);
                }

            });

            if (back != null) {
                btn_nxt.setVisibility(View.VISIBLE);
            } else {
                Log.e("issue with back image", "of aadhar");
            }

        }

        setDataInEditText();

        dateManagement();
    }

    private void licenceDetail() {
        date_portion.setVisibility(View.VISIBLE);
        address_portion.setVisibility(View.VISIBLE);
        setDataInEditText();
        dateManagement();
    }

    private void setDataInEditText() {

        if (driveDocModel != null) {
            cardDetail = driveDocModel.getCardDetail();
        } else {
            Toast.makeText(this, "Could not find the document", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
//            folderHolderData = getIntent().getStringExtra(Constants.FOLDER_HOLDER_DATA);
//            fileHolderData = getIntent().getStringExtra(Constants.FILE_HOLDER_DATA);
//            folderHolderIdData = getIntent().getStringExtra(Constants.FOLDER_HOLDER_ID_DATA);


        if (cardDetail != null) {
            if (!cardDetail.getCard_name().equals("Unknown")) {
                edittext_name.setText(cardDetail.getCard_name());
            }
//            edittext_number.setText(Globalarea.SpecificCard.getCard_unique_no());
//            edittext_dob.setText(Globalarea.SpecificCard.getDate_of_birth());
//            edittext_validthrough.setText(Globalarea.SpecificCard.getIssue_date());
//            edittext_validtilldate.setText(Globalarea.SpecificCard.getTill_date());
//            edittext_issueplace.setText(Globalarea.SpecificCard.getIssue_address());
//            edittext_bithplace.setText(Globalarea.SpecificCard.getBirth_place());
//            actionBar.setTitle(edittext_name.getText().toString());
//            edittext_bithplace.setText(Globalarea.SpecificCard.getBirth_place());


            edittext_number.setText(cardDetail.getCard_unique_no());
            edittext_dob.setText(cardDetail.getDate_of_birth());
            edittext_validthrough.setText(cardDetail.getIssue_date());
            edittext_validtilldate.setText(cardDetail.getTill_date());
            edittext_issueplace.setText(cardDetail.getIssue_address());
            edittext_bithplace.setText(cardDetail.getBirth_place());
            if (actionBar != null)
                actionBar.setTitle(edittext_name.getText().toString());
            edittext_bithplace.setText(cardDetail.getBirth_place());

            if (driveDocModel.getImagePath() != null) {
                GlideApp.with(this)
                        .load(new File(driveDocModel.getImagePath()))
                        .placeholder(R.drawable.ds_logo)
                        .into(specific_image);

            }
        }
    }

    private void setPanCardManagement() {

        edittext_dob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edittext_dob.setFocusable(true);
                edittext_dob.requestFocus();
                edittext_dob.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edittext_dob.setOnClickListener(v -> {

//                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.);

            if (whichcard.equals(Constants.pancard)) {
                if (cardDetail.getDate_of_birth().contains("/")) {

                    showDatePickerDialog();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter valid date..", Toast.LENGTH_LONG).show();
                }
            } else {
                showTodayDatePickerDialog(edittext_dob, "Credit");
            }
        });

    }

    private void showDatePickerDialog() {
        String[] split = edittext_dob.getText().toString().trim().split("/");
        int day = Integer.valueOf(split[0]);
        int month = Integer.valueOf(split[1]);
        int year = Integer.valueOf(split[2]);

        System.out.println("Date:" + day + " " + month + " " + year);
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, monthOfYear, dayOfMonth) -> {

            myCalendar.set(Calendar.YEAR, year1);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(edittext_dob, "/");

        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                dateSetListener, year, month - 1, day);
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();
    }

    private void dateManagement() {

        edittext_dob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edittext_dob.setFocusable(true);
                edittext_dob.requestFocus();
                edittext_dob.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edittext_dob.setOnClickListener(v -> {

//                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.);
            if (edittext_dob.getText().toString().contains("-")) {
                showDatePickerDialog(edittext_dob, "dob", "-");
            } else {

                // TODO Auto-generated method stub
                if (edittext_dob.getText().toString().contains("/")) {

                    showDatePickerDialog(edittext_dob, "dob", "/");
                } else {
                    showTodayDatePickerDialog(edittext_dob, "dob");

//                        Toast.makeText(getApplicationContext(), "Please enter valid date..", Toast.LENGTH_LONG).show();
                }
            }
        });
        edittext_validthrough.setOnClickListener(v -> {

//                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.);
            if (edittext_validthrough.getText().toString().contains("-")) {
                showDatePickerDialog(edittext_validthrough, "issue", "-");
            } else {


                if (edittext_validthrough.getText().toString().contains("/")) {

                    showDatePickerDialog(edittext_validthrough, "issue", "/");
                } else {
                    showTodayDatePickerDialog(edittext_validthrough, "issue");

//                        Toast.makeText(getApplicationContext(), "Please enter valid date..", Toast.LENGTH_LONG).show();
                }
            }
        });
        edittext_validtilldate.setOnClickListener(v -> {

//                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.);
            if (edittext_validtilldate.getText().toString().contains("-")) {
                showDatePickerDialog(edittext_validtilldate, "ValidTill", "-");
            } else {

                if (edittext_validtilldate.getText().toString().contains("/")) {

                    showDatePickerDialog(edittext_validtilldate, "ValidTill", "/");
                } else {
                    showTodayDatePickerDialog(edittext_validtilldate, "ValidTill");

//                        Toast.makeText(getApplicationContext(), "Please enter valid date..", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void showDatePickerDialog(final TextView textView, String type, final String dateFormat) {
        String[] split = textView.getText().toString().trim().split(dateFormat);
        int day = Integer.valueOf(split[0]);
        int month = Integer.valueOf(split[1]);
        int year = Integer.valueOf(split[2]);

        System.out.println("Date:" + day + " " + month + " " + year);
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, monthOfYear, dayOfMonth) -> {

            myCalendar.set(Calendar.YEAR, year1);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(textView, dateFormat);

        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                dateSetListener, year, month - 1, day);
        if (!type.equals("ValidTill"))
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
//        datePickerDialog.getDatePicker().setMinDate(new Date(myCalendar.getTime().getYear() - year, month, day).getTime());
//        System.out.println("minimum year :== " + myCalendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTodayDatePickerDialog(final TextView textView, String type) {

        String[] split = Globalarea.CurrentDate.trim().split("/");
        int day = Integer.valueOf(split[0]);
        int month = Integer.valueOf(split[1]);
        int year = Integer.valueOf(split[2]);
        System.out.println("Date:" + day + " " + month + " " + year);
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, monthOfYear, dayOfMonth) -> {


            myCalendar.set(Calendar.YEAR, year1);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            if (edittext_dob.getText().toString().contains("-") || edittext_validthrough.getText().toString().contains("-") || edittext_validtilldate.getText().toString().contains("-"))
                updateLabel(textView, "-");
            else
                updateLabel(textView, "/");

        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                dateSetListener, year, month - 1, day);

//                       DatePickerDialog datePickerDialog = new DatePickerDialog(this,
//                       dateSetListener, myCalendar
        //                  .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
//                       myCalendar.get(Calendar.DAY_OF_MONTH));

        if (type.equals("Credit")) {
            datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
        } else {
            if (!type.equals("ValidTill"))
                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());

        }
//        datePickerDialog.getDatePicker().setMinDate(new Date(myCalendar.getTime().getYear() - year, month, day).getTime());
//        System.out.println("minimum year :== " + myCalendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void updateLabel(TextView textView, String dateFormat) {

        String myFormat;
        if (!whichcard.equals(Constants.creditCard)) {
            if (dateFormat.equals("-")) {
                myFormat = "dd-MM-yyyy"; //In which you need put here
            } else {
                myFormat = "dd/MM/yyyy";
            }
        } else {
            myFormat = "MM/yyyy";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        textView.setText(sdf.format(myCalendar.getTime()));
    }

    private boolean IsValidPassport() {
        String passport_number = edittext_number.getText().toString();

        if (passport_number.equals("")) {
            edittext_number.setError("Enter Number");
            edittext_number.setFocusable(true);
            edittext_number.requestFocus();
            return false;
        } else if (!Validator.passportNumberVerify(passport_number)) {
            edittext_number.setError("Please Enter Valid Number");
            return false;
        }

        return true;
    }

    private boolean IsValidBusinessCard() {
        String pName = edittext_name.getText().toString();

        if (pName.equals("")) {
            edittext_name.setError("Enter Information");
            edittext_name.setFocusable(true);
            edittext_name.requestFocus();
            return false;
        }

        return true;
    }

    private boolean IsValidPanCard() {
        String pName = edittext_name.getText().toString();
        String panNo = edittext_number.getText().toString();
        String panBirth = edittext_dob.getText().toString();
        if (pName.equals("")) {
            edittext_name.setError("Enter Name");
            edittext_name.setFocusable(true);
            edittext_name.requestFocus();
            return false;
        } else if (panNo.equals("")) {
            edittext_number.setError("Enter PanNumber");
            edittext_number.setFocusable(true);
            edittext_number.requestFocus();
            return false;
        } else if (panBirth.equals("")) {
            edittext_dob.setError("Enter Date");
            edittext_dob.setFocusable(true);
            edittext_dob.requestFocus();
            return false;
        } else if (!Validator.isValidPanname(pName.trim())) {
            edittext_name.setError("Enter Valid Name");
            edittext_name.setFocusable(true);
            edittext_name.requestFocus();
            return false;
        } else if (!Validator.isValidPanno(panNo)) {
            edittext_number.setError("Enter Valid PanNumber");
            edittext_number.setFocusable(true);
            edittext_number.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveActivity();
    }

    public void dialog_open() {
        new AlertDialog.Builder(SpecificPage.this)
                .setMessage(getString(R.string.confirm_delete))
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, id) -> {
                            Globalarea.actionFire = true;
//                            Globalarea.SpecificCard = handler.getSingleRow(whichcard, Globalarea.SpecificCard.getScan_time());
//
//                            if (Globalarea.SpecificCard.getStatus().equals("true") || Globalarea.SpecificCard.getStatus().equals("update")) {
//                                handler.sqliteInsertData(new CardDetail(whichcard, Globalarea.SpecificCard.getScan_time(), Globalarea.SpecificCard.getImage_size()), Constants.delete, null);
//
//                            }
//                            File file = new File(cardDetail.getImage_url());
//                            if (file.exists()) {
//                                file.delete();
//                            }
//                            if (whichcard.equals(Constants.adharcard)) {
//                                File _file = new File(cardDetail.getIssue_date());
//                                if (_file.exists()) {
//                                    _file.delete();
//                                }
//                            }

//                            callDriveDeleteDocument();

                            callLocalDeleteDocument();

//                            PreferenceManagement preferences = Scanner.getInstance().getPreferences();
//                            preferences.setSizeDetail(preferences.getSizeDetail() - Globalarea.SpecificCard.getImage_size());

//                            handler.deletRowData(whichcard, Globalarea.SpecificCard.getScan_time());
//                                retriveandSetOneSection.deleteCardData(Globalarea.SpecificCard.getScan_time(), whichcard, Globalarea.sizeDetail.getAvailableSpace() + Globalarea.SpecificCard.getImage_size(), Globalarea.sizeDetail.getUsedSpace() - Globalarea.SpecificCard.getImage_size());
//                            moveActivity();
                        }).setNegativeButton("No", null).show();
    }

    private void callLocalDeleteDocument() {
        if (driveDocModel != null) {
            driveDocModel.setSyncStatus(SyncStatus.deleted.toString());
            int deleted = driveDocRepo.addDriveDocInfo(driveDocModel);
            if (deleted > 0) {
                Toast.makeText(this, "Document Deleted Successfully.", Toast.LENGTH_SHORT).show();
                moveActivity();
            }
        }
    }

    public boolean licenceNumberVerify(String Licence_number) {

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

    @Override
    public void onTaskFinished(String Token) {
        pgdialog.dismiss();
        if (Token.equals("Update")) {

            Toast.makeText(getApplicationContext(), getString(R.string.success_saved_info), Toast.LENGTH_LONG).show();
            Linear_btns.setVisibility(View.GONE);
            Linear_edit.setVisibility(View.VISIBLE);
            cancleData();
            if (actionBar != null)
                actionBar.setTitle(edittext_name.getText().toString());
        } else if (Token.equals("Delete")) {
            moveActivity();
        }
    }

    @Override
    public void onTaskFinished(CardDetail Token) {

    }

    @Override
    public void onTaskError(String Token) {
        pgdialog.dismiss();
        if (Token.equals(Constants.UploadError)) {
            dialog_open(getResources().getString(R.string.UploadError));
        } else if (Token.equals(Constants.InternetConnectionFail)) {
            dialog_open(getResources().getString(R.string.internetConnectionFail));
        }
    }

    public void dialog_open(String message) {
        new android.app.AlertDialog.Builder(SpecificPage.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, id) -> {

                            System.out.println(" User is Agree to Delete the File ");
                            Intent intent = new Intent(SpecificPage.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }).show();
    }


    @Override
    public void onTaskError(String Token, String errormessage) {

    }

    @Override
    public void onTaskFinished(String Token, String taskResponse) {

    }

    private void moveActivity() {

//        Intent scantype = this.getIntent();
//        if (scantype.getStringExtra("WhictActivity").equals("CardScanActivity")) {
//            Intent intent = new Intent(SpecificPage.this, HomeActivity.class);
//            startActivity(intent);
//            finish();
//        } else if (scantype.getStringExtra("WhictActivity").equals("ListActivity")) {


//        Intent intent = new Intent(SpecificPage.this, ListActivity.class);
        Intent intent = new Intent(SpecificPage.this, DriveListActivity.class);
        intent.putExtra("TAG_CAMERA", whichcard);
        startActivity(intent);
        finish();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(broadcastReceiver);
        //todo: Uncommnet below line unregister drive sync broadcast receiver
        unregisterReceiver(connectivityChangeReceiver);
    }

//    /**
//     * Created by seven-bits-pc11 on 17/5/17.
//     */
//    public class DownloadImage extends AsyncTask<String, String, Boolean> {
//        Bitmap bitmap;
//        String message = "Oops Something went wrong!!";
//
//        public DownloadImage() {
//
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            pgdialog.setMessage("Please Wait!!!");
//            pgdialog.setCancelable(false);
//            pgdialog.show();
//        }
//
//        protected Boolean doInBackground(String... urls) {
//            if (Scanner.getInstance().getConnectionDetactor().isConnectingToInternet()) {
//
//                try {
//                    bitmap = firebase_imageLoader.getBitmap(Globalarea.SpecificCard.getImage_url(), 400);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    message = "Oops Something went wrong!!";
//                    return false;
//                }
//            } else {
//                System.out.println("*****Please connect to working Internet connection");
//                message = "Check internet connection!!!";
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            super.onPostExecute(aBoolean);
//            pgdialog.dismiss();
//            if (aBoolean) {
//                specific_image.setImageBitmap(bitmap);
//
//            } else {
//                dialog_open(message);
//            }
//        }
//    }

//    private void setGoogleDriveService() {
//
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//
//        if (account == null) {
////            loginWithGoogle();
//            Toast.makeText(this, "You are not logged in! Please login again", Toast.LENGTH_SHORT).show();
//            onBackPressed();
//        } else {
//
//            GoogleAccountCredential credential =
//                    GoogleAccountCredential.usingOAuth2(
//                            this, Collections.singleton(DriveScopes.DRIVE));
//            credential.setSelectedAccount(account.getAccount());
//            com.google.api.services.drive.Drive googleDriveService =
//                    new com.google.api.services.drive.Drive.Builder(
//                            AndroidHttp.newCompatibleTransport(),
//                            new GsonFactory(),
//                            credential)
//                            .setApplicationName(getString(R.string.app_name))
//                            .build();
//
//            mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
//            Log.e("drive service", "helper setup");
//
//        }
//    }

    // Delete Drive Document Method
//    public void callDriveDeleteDocument() {
//        if (connectionDetector != null && connectionDetector.isConnectingToInternet()) {
//
//            List<DriveDocModel> driveDocModelList = new ArrayList<>();
//            driveDocModelList.add(driveDocModel);
//
//            pgdialog.show();
//            if (driveDocModelList.size() > 0) {
//                ScanRDriveOperations.deleteFolder(driveDocModelList, mDriveServiceHelper, 0, new ScanRDriveOperations.OnCompleteDriveOperations() {
//
//                    @Override
//                    public void onSuccess(String message) {
//                        if (message != null) {
//                            deleteFromMetadata(new MetadataModel(driveDocModel));
//                        } else {
//                            pgdialog.dismiss();
//                            Toast.makeText(SpecificPage.this, "Folder Not removed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Exception e, String message) {
//                        pgdialog.dismiss();
//                        Toast.makeText(SpecificPage.this, "Error removing folder from the Drive", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } else {
//                Toast.makeText(SpecificPage.this, "Please select document you want to delete!", Toast.LENGTH_SHORT).show();
//            }
//
//        } else {
//            Toast.makeText(this, getString(R.string.internetConnectionFail), Toast.LENGTH_SHORT).show();
//        }
//    }

//    private void deleteFromMetadata(MetadataModel metadataModel) {
//
//        List<MetadataModel> metadataModelArrayList = new ArrayList<>();
//        metadataModelArrayList.add(metadataModel);
//
//        ScanRDriveOperations.findMetadataDocForOperation(mDriveServiceHelper,
//                new ScanRDriveOperations.OnCompleteMetaDataQueries() {
//                    @Override
//                    public void onSuccessFullMetadataContent(String metadataFileId,
//                                                             List<MetadataModel> metadataModels) {
//
//                        Log.e("found metadata content", String.valueOf(metadataModels.size()));
//                        ScanRDriveOperations.deleteDocInMetadata(mDriveServiceHelper,
//                                metadataFileId, metadataModels, metadataModelArrayList,
//                                new ScanRDriveOperations.OnCompleteDriveOperations() {
//                                    @Override
//                                    public void onSuccess(String message) {
//                                        pgdialog.dismiss();
//                                        Toast.makeText(SpecificPage.this, "Documents removed Successfully", Toast.LENGTH_SHORT).show();
//                                        moveActivity();
//                                    }
//
//                                    @Override
//                                    public void onFailure(Exception e, String message) {
//                                        pgdialog.dismiss();
//                                        e.printStackTrace();
//                                        Toast.makeText(SpecificPage.this, "Failed to update metadata doc", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    }
//
//                    @Override
//                    public void onFailure(Exception e, String message) {
//                        pgdialog.dismiss();
//                        e.printStackTrace();
//                        Toast.makeText(SpecificPage.this, "Failed to find metadata doc", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.more_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.help:
                Intent intent = new Intent(this, ContactUs.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.setting:
                Intent intent2 = new Intent(this, SettingActivity.class);
                startActivity(intent2);
                finish();
                return true;

            case R.id.view_files:
                Globalarea.openAllFilesMenu(this);
                return true;
        }
        return true;
    }
}
