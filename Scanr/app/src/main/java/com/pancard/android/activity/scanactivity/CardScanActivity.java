package com.pancard.android.activity.scanactivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.pancard.android.DatabaseHandler;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.DriveOperations.MetadataModel;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.otheracivity.CommonScan;
import com.pancard.android.activity.otheracivity.ContactUs;
import com.pancard.android.activity.otheracivity.HomeActivity;
import com.pancard.android.activity.otheracivity.SettingActivity;
import com.pancard.android.core.XmlStringParsing;
import com.pancard.android.database.DriveDocRepo;
import com.pancard.android.database.FileVersion;
import com.pancard.android.database.SyncStatus;
import com.pancard.android.listener.TaskListener;
import com.pancard.android.model.CardDetail;
import com.pancard.android.model.SqliteDetail;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.FileOpration;
import com.pancard.android.utility.LocalFilesAndFolder;
import com.pancard.android.utility.PermissionManager;
import com.pancard.android.utility.PreferenceManagement;
import com.pancard.android.utility.ScanrDialog;
import com.pancard.android.utility.Validator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CardScanActivity extends AppCompatActivity implements TaskListener {

    private static final int STORAGE_PERMISSION_REQUEST_CODE_SAVE = 101;
    private static final int STORAGE_PERMISSION_REQUEST_CODE_NEXT_PAGES = 103;
    private static final int INITIAL_PERMISSION_REQUEST_CODE = 2;
    final int GOOGLE_REQUEST_CODE = 102;
    EditText etName, text_dob, textCardNo, et_issue_place, et_birth_place, et_date_through, et_date_till;
    Button btn_save, btn_cancel, btn_nxt, btnScanMorePages;
    Bitmap card, originalBitmap;
    ImageView image;
    String scanFormattedDate;
    Calendar myCalendar;
    ProgressDialog dialog;
    String whichCard;
    PermissionManager permissionManager;
    String[] permissionsWrite = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    AdRequest adRequest;
    LinearLayout linearLayoutCommon, linerLayoutPanCard, linearLayoutDate, linearLayoutPlace, linearLayoutDatePlace;
    TextView error_msg, txt_personal_info;
    ScrollView scrollView;
    CardDetail cardDetail;
    SqliteDetail sqliteDetail;
    DatabaseHandler handler;
    boolean imageOrder = true;
    PreferenceManagement preferences;
    ConnectionDetector connectionDetector;
    boolean isUploadingImage, isCreatingTextFile;
    MetadataModel metadataModel = null;
    DriveDocRepo driveDocRepo;
    View view_personal_info;
    boolean isNavigateTOScreen = false;
    MenuItem menuItem;
    private AdView adView;
    private DriveServiceHelper mDriveServiceHelper;
    private TextView tvDriveNote;
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_scan);

        bindViews();
        initialise();

        Log.d("in", "card scan");
    }

    private void initialise() {

        preferences = Scanner.getInstance().getPreferences();
        handler = new DatabaseHandler(CardScanActivity.this);
        sqliteDetail = new SqliteDetail();
        connectionDetector = new ConnectionDetector(this);
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

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

        permissionManager = new PermissionManager(this);
//        if (!permissionManager.hasPermissions(permissions))
//            permissionManager.requestPermissions(permissions, INITIAL_PERMISSION_REQUEST_CODE);

        dialog = new ProgressDialog(CardScanActivity.this);
        cardDetail = new CardDetail();
        myCalendar = Calendar.getInstance();

//        Scanner.startGame();
        //todo: Uncomment below line for setting google drive service on initialization
//        setGoogleDriveService();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setDetails(extras);
        }

        btn_cancel.setOnClickListener(v -> dialog_open(getString(R.string.confirm_descard)));

        btn_save.setOnClickListener(v -> {
            checkPermissionAndSaveData(false, STORAGE_PERMISSION_REQUEST_CODE_SAVE);
        });

        btnScanMorePages.setOnClickListener(view -> {
            checkPermissionAndSaveData(true, STORAGE_PERMISSION_REQUEST_CODE_NEXT_PAGES);
        });

        image.setOnClickListener(v -> {
            CommonScan.CARD_HOLDER_NAME = etName.getText().toString();
            CommonScan.CARD_UNIQE_NO = textCardNo.getText().toString();
            CommonScan.CARD_HOLDER_DOB = text_dob.getText().toString();
            CommonScan.CARD_ISSUE_DATE = et_date_through.getText().toString();
            CommonScan.CARD_TILL_DATE = et_date_till.getText().toString();
            CommonScan.CARD_ISSUE_ADDRESS = et_issue_place.getText().toString();
            CommonScan.CARD_BIRTH_PLACE = et_birth_place.getText().toString();
            Intent intent = new Intent(CardScanActivity.this, FullScreenImageviewerActivity.class);
            intent.putExtra("TAG_CAMERA", whichCard);
            intent.putExtra("activity", "scanactivity");
            startActivity(intent);
        });
//        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET"));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);

    }

    private void checkPermissionAndSaveData(boolean saveForNextPages, int requestCodes) {
        if (permissionManager.hasPermissions(permissionsWrite)) {
            //todo: uncomment below code for drive permission dialog

//                if (preferences.isDriveConnected()) {
//                    saveDataToFirebase();
//                } else {
//                    showGoogleDrivePermissionDialog();
//                }
            saveDataToFirebase(saveForNextPages);

        } else {
            if (!preferences.isShowedWriteStoragePermissionDialog() || permissionManager.shouldRequestPermission(CardScanActivity.this, permissionsWrite)) {

                permissionManager.showPermissionRequireDialog(this, getResources().getString(R.string.storage_permission_for),
                        new PermissionManager.OnPermissionRequireDialog() {
                            @Override
                            public void onAcceptPermissionDialog() {
                                Log.e("go permission", "yes");
                                permissionManager.requestPermissions(permissionsWrite, STORAGE_PERMISSION_REQUEST_CODE_SAVE);
                            }

                            @Override
                            public void onCancelPermissionDialog() {
                                Log.e("go permission", "no");
                            }
                        });

            } else {
                permissionManager.openSettingDialog(CardScanActivity.this, getResources().getString(R.string.storage_permission_access));
            }
        }
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, CardScanActivity.this);
    }

    private void setDetails(Bundle extras) {
        String data = extras.getString("TAG_CAMERA");
        Log.e("card type", data);

        if (data != null) {
            whichCard = data;
            Log.e("which card", whichCard);
            Scanner.showInterstitial();
            if (data.equals(Constants.businesscard)) {
                CommonScan.CARD_IMAGE = Globalarea.document_image;
                CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
                setBusinessDataTextview(Constants.businesscard);
                Log.e("in", "business card");

            } else if (data.equals(Constants.document)) {
                CommonScan.CARD_IMAGE = Globalarea.document_image;
                CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
                System.out.println("card holder: " + CommonScan.CARD_HOLDER_NAME);
                setBusinessDataTextview(Constants.document);

            } else if (data.equals(Constants.pancard)) {
                CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
                setPanCardDataTextView();
            } else if (data.equals(Constants.pancard2)) {
                CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
                parsePanString(Globalarea.panCard2Text);
                setPanCardDataTextView();
                whichCard = Constants.pancard;
            } else if (data.equals(Constants.creditCard)) {
                CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
                Globalarea.document_image = CommonScan.CARD_IMAGE;
                setCreditCardFlow();

            } else if (data.equals(Constants.licence) || data.equals(Constants.passport)) {
                CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
                setLicenceDataTextView();
                Log.e("data", data);

            } else if (data.equals(Constants.adharcard)) {
                CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
                setAdharCardDetail();
                Log.e("data", data);
            }
        }
    }

    private void bindViews() {
        linearLayoutCommon = findViewById(R.id.common_docs);
        linerLayoutPanCard = findViewById(R.id.llpancard);
        linearLayoutDate = findViewById(R.id.llDate);
        linearLayoutPlace = findViewById(R.id.llplace);
        linearLayoutDatePlace = findViewById(R.id.llDatePlace);

        etName = findViewById(R.id.etName);
        textCardNo = findViewById(R.id.etCardno);
        text_dob = findViewById(R.id.etbirthdate);
        et_date_through = findViewById(R.id.etDocumentDateValidThrough);
        et_date_till = findViewById(R.id.etDocumentDateValidTill);
        et_birth_place = findViewById(R.id.etDocumentBirthPlace);
        et_issue_place = findViewById(R.id.etDocumentPlaceIssue);
        scrollView = findViewById(R.id.scrollView);
        error_msg = findViewById(R.id.txt_error);

        image = findViewById(R.id.img_document);
        btn_save = findViewById(R.id.btn_save);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_nxt = findViewById(R.id.btn_nxt);
        btnScanMorePages = findViewById(R.id.btn_scan_more_page);

        tvDriveNote = findViewById(R.id.tv_note_drive);
        txt_personal_info = findViewById(R.id.txt_personal_info);
        view_personal_info = findViewById(R.id.view_personal_info);
        adView = (AdView) findViewById(R.id.ad_view);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    private void setGoogleDriveService() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account == null) {
            if (!preferences.isShowedDriveDialog()) {
                showGoogleDrivePermissionDialog();
            }
//            loginWithGoogle();
//            Toast.makeText(this, "You are not logged in! Please login again", Toast.LENGTH_SHORT).show();
//            onBackPressed();
        } else {

            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            com.google.api.services.drive.Drive googleDriveService =
                    new com.google.api.services.drive.Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName(getString(R.string.app_name))
                            .build();

            mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
            Log.e("drive service", "helper setup");

        }
    }

    private void parsePanString(String panText) {
        try {
            String[] panData = panText.split("\n");

            CommonScan.CARD_HOLDER_NAME = panData[0].substring(panData[0].indexOf(":") + 1);
            Log.e("changed holder name", "in line 95 QR code scanner");
            CommonScan.CARD_HOLDER_DOB = panData[2].substring(panData[2].indexOf(":") + 1);
            CommonScan.CARD_UNIQE_NO = panData[3].substring(panData[3].indexOf(":") + 1);

            Log.e("name", CommonScan.CARD_HOLDER_NAME);
            Log.e("dob", CommonScan.CARD_HOLDER_DOB);
            Log.e("unique no", CommonScan.CARD_UNIQE_NO);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            Toast.makeText(CardScanActivity.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(broadcastReceiver);
        //todo: Uncommnet below line unregister drive sync broadcast receiver
        if (connectivityChangeReceiver != null)
            unregisterReceiver(connectivityChangeReceiver);

        if (adView != null) {
            adView.destroy();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        showDriveNote();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {


        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d("REQUEST_CODE_SAVE", "GET_ACCOUNTS granted - initialize the camera source");
            preferences.setShowedWriteStoragePermissionDialog(true);
            if (requestCode == STORAGE_PERMISSION_REQUEST_CODE_SAVE)
                saveDataToFirebase(false);
            else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE_NEXT_PAGES)
                saveDataToFirebase(true);

        } else {
            Toast.makeText(this, getString(R.string.error_storage_permission), Toast.LENGTH_SHORT).show();
        }


    }

    private void saveDataToFirebase(boolean saveForNextPages) {
        if (whichCard.equals(Constants.businesscard) || whichCard.equals(Constants.document)) {
            if (etName.getText().toString().trim().length() < 1) {
                etName.setError(getString(R.string.error_scan_info));
                etName.setFocusable(true);
                etName.requestFocus();
            } else {
                addData(whichCard, saveForNextPages);
            }
        } else if (whichCard.equals(Constants.pancard)) {
            if (IsValidPanCard()) {
                addData(Constants.pancard, saveForNextPages);
            }
        } else if (whichCard.equals(Constants.creditCard)) {
            if (textCardNo.getText().toString().trim().length() > 9) {
                addData(Constants.creditCard, saveForNextPages);
            } else {
                textCardNo.setError(getString(R.string.error_mobile_number));
                textCardNo.setFocusable(true);
                textCardNo.requestFocus();
            }
        } else if (whichCard.equals(Constants.licence)) {
            if (!Validator.licenceNumberVerify(textCardNo.getText().toString().trim())) {
                textCardNo.setError(getString(R.string.error_licence_number));
                textCardNo.setFocusable(true);
                textCardNo.requestFocus();
            } else {
                if (Validator.IsValidDate(text_dob, et_date_through, et_date_till, error_msg, scrollView)) {
                    error_msg.setVisibility(View.GONE);
                    addData(Constants.licence, saveForNextPages);
                }
            }
        } else if (whichCard.equals(Constants.passport)) {
            if (IsValid()) {
                if (Validator.IsValidDate(text_dob, et_date_through, et_date_till, error_msg, scrollView)) {
                    error_msg.setVisibility(View.GONE);
                    addData(Constants.passport, saveForNextPages);
                }
            }
        } else if (whichCard.equals(Constants.adharcard)) {
            addData(Constants.adharcard, saveForNextPages);
        }
    }

    private void addData(String tag, boolean saveForNextPages) {

        SimpleDateFormat df = new SimpleDateFormat("dd MMM  hh:mm:ss a", Locale.getDefault());
        scanFormattedDate = df.format(myCalendar.getTime());

        //fixme: file is null sometimes
        String name = "Unknown";
        String originalImageName = "Unknown2";
        String publicGUID = null;

        if (etName.getText().toString().trim().length() > 0)
            name = etName.getText().toString().trim();


        if (Globalarea.documentPageList != null && Globalarea.documentPageList.size() > 0) {
            int size = Globalarea.getDocumentPageList().size();

            String suffix = "_" + size;

            //todo: handle it with new names
            name = Globalarea.documentPageList.get(0).getFolderName();
            name = name + suffix;

        } else {
//            name = name.substring(0, Math.min(name.length() - 1, 10)) + System.currentTimeMillis();

            publicGUID = String.valueOf(System.currentTimeMillis());
            String suffix = "_";

            name = publicGUID + suffix + whichCard + suffix + FileVersion.CROPPED.toString();
//            originalImageName = whichCard + suffix + "Original" + suffix + publicGUID;
            originalImageName = publicGUID + suffix + whichCard + suffix + FileVersion.ORIGINAL.toString();
        }

        File file = CaptureImage(card, tag, name);
        File originalImageFile = CaptureImage(originalBitmap, tag, originalImageName);

//        RetriveandSetOneSection retriveandSetOneSection = new RetriveandSetOneSection(this);

        if (file != null) {
            setCardDetail(tag, file, name, originalImageFile, publicGUID, saveForNextPages);
        }

//        Globalarea.SpecificCard = cardDetail;

//        if (!Globalarea.isInternetOn()) {
//
//            if (!preferences.isSyncOnlineMessage()) {
//                preferences.setSyncOnlineMessage(true);
//                new android.app.AlertDialog.Builder(CardScanActivity.this)
//                        .setTitle(getString(R.string.hint))
//                        .setMessage(getResources().getString(R.string.SyncMessage))
//                        .setCancelable(false)
//                        .setPositiveButton("Ok",
//                                (dialog, id) -> dialog.dismiss()).show();
//            } else {
//                openActivity();
//            }
//        } else {
//            openActivity();
//        }
    }

    private void setCardDetail(String tag, File file, String subFolderName, File originalImageFile, String public_guid, boolean saveForNextPages) {
        long size = (file.length() / 1024);
        if (tag.equals(Constants.businesscard) || tag.equals(Constants.document)) {
            cardDetail = new CardDetail(etName.getText().toString().trim(),
                    scanFormattedDate.trim(), file.getAbsolutePath(), size);

        } else if (tag.equals(Constants.pancard) || tag.equals(Constants.creditCard)) {
            cardDetail = new CardDetail(etName.getText().toString(),
                    text_dob.getText().toString(), textCardNo.getText().toString(), scanFormattedDate.trim(), file.getAbsolutePath(), size);

        } else if (tag.equals(Constants.licence) || tag.equals(Constants.passport) || tag.equals(Constants.adharcard)) {
            String name;
            if (etName.getText().toString().trim().equals("")) {
                name = "Unknown";
            } else {
                name = etName.getText().toString().trim();
            }
            cardDetail = new CardDetail(name, textCardNo.getText().toString().trim(),
                    text_dob.getText().toString().trim(), et_date_through.getText().toString().trim(),
                    et_date_till.getText().toString().trim(), et_birth_place.getText().toString().trim(), et_issue_place.getText().toString().trim(),
                    file.getAbsolutePath(), scanFormattedDate.trim(), size);
            if (tag.equals(Constants.adharcard)) {
                //fixme: file is null sometimes

                //todo: fix multiple image with aadhar card
                String backName = subFolderName + "_" + "1";
                File back_file = CaptureImage(Globalarea.adharCard_back_image, tag, backName);
                cardDetail.setIssue_date(back_file.getAbsolutePath());
                long size1 = (back_file.length() / 1024);
                cardDetail.setImage_size(size + size1);
            }
        }

//        dialog.setMessage(getString(R.string.msg_please_wait));
//        dialog.setCancelable(false);
//        dialog.show();


        //        Globalarea.actionFire = true;
        System.out.println("Size of cards : " + preferences.getSizeDetail());
        preferences.setSizeDetail((int) (preferences.getSizeDetail() + size));
        //todo: remove below if else for 20mb space limit in sqlite
//            if (preferences.getSizeDetail() < 20000) {
        handler.sqliteInsertData(cardDetail, whichCard, "false");

        //todo: keep this commented (Don't uncomment this if else while enable above if else)
        //this commented portion was to send card info in firebase
//                if (Globalarea.isInternetOn()) {
//                    if (Globalarea.firebaseUser != null) {
//                        Globalarea.actionFire = false;
//                        new FirebaseManagement(this).sendcardInfoDirect(this, cardDetail, whichCard, "false");
//                    } else {
//                        Globalarea.actionFire = true;
//                    }
//                }
//            } else {
//                Toast.makeText(CardScanActivity.this, getResources().getString(R.string.UploadError), Toast.LENGTH_LONG).show();
//                preferences.setSizeDetail((int) (preferences.getSizeDetail() - cardDetail.getImage_size()));
//            }

        //todo: Check why need to set Whichcard in global CardDetail model
        cardDetail.setWhichcard(whichCard);
        addDocument(file, cardDetail, tag, subFolderName, originalImageFile.getAbsolutePath(), public_guid, saveForNextPages);

        //upload file to drive... moved to the another method
    }

    private void addDocument(File file, CardDetail cardDetail, String tag, String subFolderName, String originalImageFilePath, String public_guid, boolean saveForNextPages) {

        String strCardDetails = Globalarea.getStringOfCardDetails(cardDetail);
//        Log.i("adding to localDB",strCardDetails);
        if (strCardDetails != null && file != null) {

            DriveDocModel driveDocModel = new DriveDocModel(public_guid, file.getAbsolutePath(), originalImageFilePath, strCardDetails, cardDetail.getWhichcard(), SyncStatus.unsynced.toString());

            if (Globalarea.documentPageList != null && Globalarea.documentPageList.size() > 0) {
                int size = Globalarea.getDocumentPageList().size();
                Log.e("past pages size", String.valueOf(size));

                String suffix = "_" + size;
                driveDocModel.setFolderName(subFolderName + suffix);

            } else {
                driveDocModel.setFolderName(subFolderName);
                Globalarea.addThePageIntoGetDocumentPageList(driveDocModel);
            }

            if (saveForNextPages) {
                Globalarea.documentPageList.add(driveDocModel);
                Log.e("save for next page", "yeah");
            } else {
                Globalarea.documentPageList = null;
            }

            int added = driveDocRepo.addDriveDocInfo(driveDocModel);

            if (whichCard.equals(Constants.adharcard)) {
                if (cardDetail.getIssue_date() != null) {
                    //todo : Need to add Original image for aadhar back image
                    DriveDocModel driveDocModelBack = new DriveDocModel(cardDetail.getIssue_date(), strCardDetails, cardDetail.getWhichcard(), SyncStatus.unsynced.toString());
                    String suffix = "_" + "1";
                    driveDocModel.setFolderName(subFolderName + suffix);


                    String folderName = driveDocModel.getFolderName();
                    Log.e("old string", folderName);
                    int charSize = folderName.length();

                    if (folderName.charAt(charSize - 2) == '_') {
                        //todo: remove ....
                        String newString = folderName.substring(0, charSize - 2);
                        Log.e("new stirng", newString);

                    } else {
                        Log.e("no sub", "page found");
                    }

                    driveDocRepo.addDriveDocInfo(driveDocModelBack);
                }
            }

            if (added > 0) {
                Toast.makeText(CardScanActivity.this, "Successfully stored document.", Toast.LENGTH_SHORT).show();
                //todo: handle different ways for both new scan and more page scan
//                prepareUploadInDrive(file, cardDetail, tag, subFolderName);

                if (saveForNextPages) {
                    Intent intent = new Intent(CardScanActivity.this, CommonScan.class);
                    intent.putExtra(CommonScan.SCANNER_TYPE, whichCard);
                    startActivity(intent);
                    this.finish();
                } else
                    openActivity();
            } else {
                Toast.makeText(CardScanActivity.this, "Error storing document.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void prepareUploadInDrive(File file, CardDetail cardDetail, String tag, String subFolderName) {
        if (connectionDetector != null && connectionDetector.isConnectingToInternet()) {
            FileOpration fileOpration = new FileOpration();
            String mimeType = fileOpration.getMimeType(Uri.fromFile(file), this);

            if (file.exists() && mimeType != null && cardDetail != null) {

//                String name = "";
//                if (cardDetail.getCard_name() != null && cardDetail.getCard_name().trim().length() > 0) {
//                    name = cardDetail.getCard_name();
//                } else {
//                    name = "Unknown";
//                }

                // String subFolderName = name.substring(0, Math.min(name.length() - 1, 10)) + System.currentTimeMillis();
                getFolderIdAndUpload(file, mimeType, tag, subFolderName, cardDetail);
            } else {
                dialog.dismiss();
                Toast.makeText(this, "Something is wrong with the selected file. Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.internetConnectionFail), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

    private void getFolderIdAndUpload(File file, String mimeType,
                                      String folderName, String subFolderName,
                                      CardDetail cardDetail) {
        if (folderName != null) {

            ScanRDriveOperations.getCategoryFolderAndCreateFolder(folderName, subFolderName,
                    mDriveServiceHelper, new ScanRDriveOperations.OnCompleteDriveOperations() {
                        @Override
                        public void onSuccess(String folderId) {
                            Log.e("folder id", "is: " + folderId);
                            dialog.dismiss();
                            metadataModel = new MetadataModel(folderName, subFolderName, folderId);

                            uploadImage(file, mimeType, folderId);
                            createTextFile(cardDetail, folderId);
                        }

                        @Override
                        public void onFailure(Exception e, String message) {
                            dialog.dismiss();
                            Toast.makeText(CardScanActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            dialog.dismiss();
        }
    }

    private void createTextFile(CardDetail cardDetail, String folderId) {

        dialog.show();
        isCreatingTextFile = true;
        Gson gson = new Gson();
        String jsonText = gson.toJson(cardDetail);

        String fileName = ScanRDriveOperations.JSON_FILE_NAME;

        mDriveServiceHelper.createTextFile(fileName, jsonText, folderId)
                .addOnSuccessListener(googleDriveFileHolder -> {
                    isCreatingTextFile = false;
                    Log.i("uploaded text file", "yes");
                    Toast.makeText(this, "Successfully created the text file in the drive", Toast.LENGTH_SHORT).show();
                    metadataModel.setJsonFileID(googleDriveFileHolder.getId());
                    saveMetadata();

                })
                .addOnFailureListener(e -> {
                    isCreatingTextFile = false;
                    Log.i("uploaded text file", "no");
                    e.printStackTrace();
                    Toast.makeText(this, "Issue in creating a file into the drive", Toast.LENGTH_SHORT).show();
                    saveMetadata();
                });
    }

    private void uploadImage(File file, String mimeType, String folderId) {
        dialog.show();
        isUploadingImage = true;

        ScanRDriveOperations.uploadAndRenameImageFile(file, mimeType, folderId, mDriveServiceHelper,
                new ScanRDriveOperations.OnCompleteDriveOperations() {
                    @Override
                    public void onSuccess(String fileId) {
                        isUploadingImage = false;
                        Log.i("uploaded", "yes");
                        Toast.makeText(CardScanActivity.this, "Successfully uploaded the file in the drive", Toast.LENGTH_SHORT).show();
                        metadataModel.setImageFileId(fileId);
                        saveMetadata();
                    }

                    @Override
                    public void onFailure(Exception e, String message) {
                        isUploadingImage = false;
                        Log.i("uploaded", "no");
                        e.printStackTrace();
                        Toast.makeText(CardScanActivity.this, "Issue in uploading a file into the drive", Toast.LENGTH_SHORT).show();
                        saveMetadata();
                    }
                });
    }

    private void saveMetadata() {
//        if (isImage)
//            metadataModel.setImageFileId(fileID);
//        else
//            metadataModel.setJsonFileID(fileID);

        if (isCreatingTextFile || isUploadingImage) {
            Log.e("returning", "Process is going on");
            return;
        }

        List<MetadataModel> metadataModelList = new ArrayList<>();
        metadataModelList.add(metadataModel);

        Log.e("saving", "into metadata");
        ScanRDriveOperations.findMetadataDocForOperation(mDriveServiceHelper,
                new ScanRDriveOperations.OnCompleteMetaDataQueries() {
                    @Override
                    public void onSuccessFullMetadataContent(String metadataFileId, List<MetadataModel> metadataModels) {

                        Log.e("got the metadata models", String.valueOf(metadataModels.size()));
                        ScanRDriveOperations.addDocInMetadata(mDriveServiceHelper,
                                metadataFileId, metadataModels, metadataModel, new ScanRDriveOperations.OnCompleteDriveOperations() {
                                    @Override
                                    public void onSuccess(String message) {
                                        Log.e("added successfully ", "in metadata");
                                        dialog.dismiss();
                                        openActivity();
                                    }

                                    @Override
                                    public void onFailure(Exception e, String message) {
                                        dialog.dismiss();
                                        Toast.makeText(CardScanActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Exception e, String message) {
                        dialog.dismiss();
                        Toast.makeText(CardScanActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openActivity() {

        Globalarea.documentPageList = null;
//        Intent intent = new Intent(CardScanActivity.this, DocumentSuccessActivity.class);
        Intent intent = new Intent(CardScanActivity.this, DriveListActivity.class);
        intent.putExtra("TAG_CAMERA", whichCard);
        intent.putExtra("WhictActivity", "CardScanActivity");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    public void setBusinessDataTextview(String businesscard) {
        linearLayoutCommon.setVisibility(View.VISIBLE);
        etName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        System.out.println("Inside setData TextView");
        if (CommonScan.CARD_IMAGE != null) {
            card = CommonScan.CARD_IMAGE;
            originalBitmap = CommonScan.ORIGIANL_CARD_IMAGE;
            System.out.println("in side cart not null");
            image.setImageBitmap(card);


            if (CommonScan.CARD_HOLDER_NAME.length() > 1) {
                etName.setText(CommonScan.CARD_HOLDER_NAME.trim());
            } else {
                etName.setText("");
            }

            if (businesscard.contains(Constants.document)) {
//                etName.setVisibility(View.GONE);
                view_personal_info.setVisibility(View.GONE);
                txt_personal_info.setVisibility(View.GONE);
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        }
    }

    public void setPanCardDataTextView() {
        linerLayoutPanCard.setVisibility(View.VISIBLE);
        if (CommonScan.CARD_HOLDER_DOB != null && CommonScan.CARD_UNIQE_NO != null && CommonScan.CARD_HOLDER_NAME != null && CommonScan.CARD_IMAGE != null) {
            card = CommonScan.CARD_IMAGE;
            originalBitmap = CommonScan.ORIGIANL_CARD_IMAGE;
            image.setImageBitmap(card);

            if (CommonScan.CARD_HOLDER_DOB.length() > 1) {
                text_dob.setText(CommonScan.CARD_HOLDER_DOB);

            } else {
                text_dob.setText("");
            }

            if (CommonScan.CARD_UNIQE_NO.length() > 1) {
                textCardNo.setText(CommonScan.CARD_UNIQE_NO);

            } else {
                textCardNo.setText("");
            }

            if (CommonScan.CARD_HOLDER_NAME.length() > 1) {
                etName.setText(CommonScan.CARD_HOLDER_NAME);

            } else {
                etName.setText("");
            }
        }
        setPanCardDate();
    }

    private void setCreditCardFlow() {
        linerLayoutPanCard.setVisibility(View.VISIBLE);
        card = CommonScan.CARD_IMAGE;
        originalBitmap = CommonScan.ORIGIANL_CARD_IMAGE;
        text_dob.setHint(getString(R.string.hint_expiry));
        etName.setHint(getString(R.string.hint_card_type));
        textCardNo.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(16);
        textCardNo.setFilters(fArray);
        if (card != null) {

            image.setImageBitmap(card);

            if (CommonScan.CARD_HOLDER_DOB != null) {
                text_dob.setText(CommonScan.CARD_HOLDER_DOB);

            } else {
                text_dob.setText("");
            }

            if (CommonScan.CARD_UNIQE_NO != null) {
                textCardNo.setText(CommonScan.CARD_UNIQE_NO);

            } else {
                textCardNo.setText("");
            }

            if (CommonScan.CARD_HOLDER_NAME != null) {
                etName.setText(CommonScan.CARD_HOLDER_NAME);

            } else {
                etName.setText("");
            }
            setCreditCardDate();

        }
    }

    public void setLicenceDataTextView() {
        linerLayoutPanCard.setVisibility(View.VISIBLE);
        linearLayoutCommon.setVisibility(View.VISIBLE);
        linearLayoutDatePlace.setVisibility(View.VISIBLE);
        linearLayoutDate.setVisibility(View.VISIBLE);
        linearLayoutPlace.setVisibility(View.VISIBLE);

        if (whichCard.equals(Constants.adharcard)) {
            linearLayoutDate.setVisibility(View.GONE);

        }


        Log.e("Inside setData TextView", "adf");
        if (CommonScan.CARD_IMAGE != null) {
            card = CommonScan.CARD_IMAGE;
            originalBitmap = CommonScan.ORIGIANL_CARD_IMAGE;

            System.out.println("in side cart not null");
            image.setImageBitmap(card);

            if (CommonScan.CARD_HOLDER_NAME.length() > 1) {
                etName.setText(CommonScan.CARD_HOLDER_NAME);
            } else {
                etName.setText("");
            }

            if (CommonScan.CARD_UNIQE_NO.length() > 1) {
                textCardNo.setText(CommonScan.CARD_UNIQE_NO);
            } else {
                textCardNo.setText("");
            }

            if (CommonScan.CARD_HOLDER_DOB.length() > 1) {
                text_dob.setText(CommonScan.CARD_HOLDER_DOB);
            } else {
                text_dob.setText("");
            }

            if (CommonScan.CARD_ISSUE_DATE.length() > 1) {
                et_date_through.setText(CommonScan.CARD_ISSUE_DATE);
            } else {
                et_date_through.setText("");
            }

            if (CommonScan.CARD_BIRTH_PLACE.length() > 1) {
                et_birth_place.setText(CommonScan.CARD_BIRTH_PLACE);
            } else {
                et_birth_place.setText("");
            }
            if (CommonScan.CARD_ISSUE_ADDRESS.length() > 1) {
                et_issue_place.setText(CommonScan.CARD_ISSUE_ADDRESS);
            } else {
                et_issue_place.setText("");
            }

            if (CommonScan.CARD_TILL_DATE.length() > 1) {
                et_date_till.setText(CommonScan.CARD_TILL_DATE);
            } else {
                et_date_till.setText("");
            }

        }

        setDate();
    }


    private void setAdharCardDetail() {
        if (Globalarea.adharCardText != null) {

            CommonScan.CARD_ISSUE_ADDRESS = "";
            Log.e("adhar card text :", Globalarea.adharCardText);

            new XmlStringParsing().processScannedData(Globalarea.adharCardText);

            Bitmap bitmap = CommonScan.CARD_IMAGE;
            CommonScan.CARD_IMAGE = Globalarea.adharCard_back_image;
            Globalarea.document_image = CommonScan.CARD_IMAGE;
            CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
            Globalarea.adharCard_back_image = bitmap;

            btn_nxt.setVisibility(View.VISIBLE);
            Globalarea.firstDisplayImage = Globalarea.document_image;
            Globalarea.secondDisplayImage = Globalarea.adharCard_back_image;
            btn_nxt.setOnClickListener(v -> {
                if (imageOrder) {
                    image.setImageBitmap(Globalarea.adharCard_back_image);
                    Globalarea.firstDisplayImage = Globalarea.adharCard_back_image;
                    Globalarea.secondDisplayImage = Globalarea.document_image;
                    imageOrder = false;
                } else {
                    imageOrder = true;
                    image.setImageBitmap(card);
                    Globalarea.firstDisplayImage = Globalarea.document_image;
                    Globalarea.secondDisplayImage = Globalarea.adharCard_back_image;
                }
            });

            setLicenceDataTextView();
        }
    }

    private void setPanCardDate() {
        text_dob.setOnClickListener(v -> {
            if (text_dob.getText().toString().contains("/")) {
                showDatePickerDialog(text_dob.getText().toString().trim());
            } else {
                Toast.makeText(getApplicationContext(), "Please enter valid date..", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setCreditCardDate() {
        text_dob.setOnClickListener(v -> showTodayDatePickerDialog(text_dob, "Credit"));
    }

    private void setDate() {
        text_dob.setOnClickListener(v -> {

//                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.);
            if (text_dob.getText().toString().contains("-")) {
                showDatePickerDialog(text_dob, "dob", "-");
            } else {

                if (text_dob.getText().toString().contains("/")) {

                    showDatePickerDialog(text_dob, "dob", "/");
                } else {
                    showTodayDatePickerDialog(text_dob, "dob");

//                        Toast.makeText(getApplicationContext(), "Please enter valid date..", Toast.LENGTH_LONG).show();
                }
            }
        });
        et_date_through.setOnClickListener(v -> {

//                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.);
            if (et_date_through.getText().toString().contains("-")) {
                showDatePickerDialog(et_date_through, "issue", "-");
            } else {

                if (et_date_through.getText().toString().contains("/")) {

                    showDatePickerDialog(et_date_through, "issue", "/");
                } else {
                    showTodayDatePickerDialog(et_date_through, "issue");

//                        Toast.makeText(getApplicationContext(), "Please enter valid date..", Toast.LENGTH_LONG).show();
                }
            }
        });
        et_date_till.setOnClickListener(v -> {

//                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.);
            if (et_date_till.getText().toString().contains("-")) {
                showDatePickerDialog(et_date_till, "ValidTill", "-");
            } else {
                if (et_date_till.getText().toString().contains("/")) {

                    showDatePickerDialog(et_date_till, "ValidTill", "/");
                } else {
                    showTodayDatePickerDialog(et_date_till, "ValidTill");
//                      Toast.makeText(getApplicationContext(), "Please enter valid date..", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showDatePickerDialog(String date) {

        try {
            String[] split = date.split("/");
            int day = Integer.valueOf(split[0]);
            int month = Integer.valueOf(split[1]);
            int year = Integer.valueOf(split[2]);

            System.out.println("Date:" + day + " " + month + " " + year);
            DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, monthOfYear, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR, year1);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateLabel();

            };

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    dateSetListener, year, month - 1, day);
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());

            datePickerDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_incorrect_date), Toast.LENGTH_SHORT).show();
        }

    }

    private void updateLabel() {

        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        text_dob.setText(sdf.format(myCalendar.getTime()));
    }

    private void showDatePickerDialog(final TextView textView, String type, final String dateFormat) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_incorrect_date), Toast.LENGTH_SHORT).show();
        }
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

            if (text_dob.getText().toString().contains("-") || et_date_through.getText().toString().contains("-") || et_date_till.getText().toString().contains("-"))
                updateLabel(textView, "-");
            else
                updateLabel(textView, "/");

        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                dateSetListener, year, month - 1, day);

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

    private void updateLabel(TextView textView, String dateFormate) {

        String myFormat;
        if (dateFormate.equals("-")) {
            myFormat = "dd-MM-yyyy"; //In which you need put here
        } else {
            myFormat = "dd/MM/yyyy";
        }

        if (whichCard.equals(Constants.creditCard)) {
            myFormat = "MM/yyyy";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        textView.setText(sdf.format(myCalendar.getTime()));
    }

    private boolean IsValidPanCard() {
        String pName = etName.getText().toString().trim();
        String panNo = textCardNo.getText().toString().trim();
        String panBirth = text_dob.getText().toString().trim();

        if (pName.equals("")) {
            etName.setError("Enter Name");
            etName.setFocusable(true);
            etName.requestFocus();
            return false;
        } else if (panNo.equals("")) {
            textCardNo.setError("Enter PanNumber");
            textCardNo.setFocusable(true);
            textCardNo.requestFocus();
            return false;
        } else if (panBirth.equals("")) {
            text_dob.setError("Enter Date");
            text_dob.setFocusable(true);
            text_dob.requestFocus();
            return false;
        } else if (!Validator.isValidPanname(pName.trim())) {
            etName.setError("Enter Valid Name");
            etName.setFocusable(true);
            etName.requestFocus();
            return false;
        } else if (!Validator.isValidPanno(panNo)) {
            textCardNo.setError("Enter Valid PanNumber");
            textCardNo.setFocusable(true);
            textCardNo.requestFocus();
            return false;
        }

        return true;
    }

    private boolean IsValid() {
        String passport_number = textCardNo.getText().toString();

        if (passport_number.equals("")) {
            textCardNo.setError("Enter Number");
            textCardNo.setFocusable(true);
            textCardNo.requestFocus();
            return false;
        } else if (!Validator.passportNumberVerify(passport_number)) {
            textCardNo.setError("Please Enter Valid Number");
            return false;
        }

        return true;
    }

    public File CaptureImage(Bitmap bitmap, String tag, String fileName) {
        /* MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), card, "Detected Image", ""); */
        try {

            File imageFile;
//            File dir;
//            dir = new File(this.getExternalCacheDir() + "/" + getResources().getString(R.string.app_name), tag);
////            File file = new File(this.getExternalCacheDir(), "image.png");
//            boolean success = true;
//            if (!dir.exists()) {
//                success = dir.mkdirs();
//            }

//            if(FirebaseAuth.getInstance().getCurrentUser() != null) {
//                File subDir = LocalFilesAndFolder.getSubDir(tag, FirebaseAuth.getInstance().getCurrentUser().getUid());
            File subDir = LocalFilesAndFolder.getRootDirOfApp();

            if (subDir != null) {
//                Date date = new Date();
//                imageFile = new File(dir.getAbsolutePath()

                imageFile = new File(subDir.getAbsolutePath()
                        + File.separator
                        + fileName + ".jpg");
                Log.e("Image Path : ", imageFile.toString());
//                    if(!imageFile.exists()){
//                        imageFile.mkdir();
//                    }
                if (!imageFile.getParentFile().exists())
                    imageFile.getParentFile().mkdirs();
                if (!imageFile.exists())
                    imageFile.createNewFile();
//                    imageFile.createNewFile();


                ByteArrayOutputStream oStream = new ByteArrayOutputStream();

                // save image into gallery
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, oStream);

                FileOutputStream fOut = new FileOutputStream(imageFile);
                fOut.write(oStream.toByteArray());
                fOut.close();
                ContentValues values = new ContentValues();

                values.put(MediaStore.Images.Media.DATE_TAKEN,
                        System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATA,
                        imageFile.getAbsolutePath());
                return imageFile;
            } else {
                return null;
            }
//            }else {
//                Toast.makeText(this, "May be your session has expired. Please login again.", Toast.LENGTH_SHORT).show();
//                FirebaseAuth.getInstance().signOut();
//                preferences.removeAllData();
//                Globalarea.firebaseUser = null;
//                Intent intent = new Intent(this, SignInActivity.class);
//                startActivity(intent);
//                finish();
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

//        return null;
    }

    public void dialog_open(String message) {
        new android.app.AlertDialog.Builder(CardScanActivity.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, id) -> {
                            if (!isNavigateTOScreen) {
                                Globalarea.documentPageList = null;
                                System.out.println(" User is Agree to Delete the File ");
                                Intent intent = new Intent(CardScanActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                if (menuItem != null)
                                    onOptionsItemSelected(menuItem);
                            }

                        }).setNegativeButton("No", (dialog, id) -> {
            isNavigateTOScreen = false;
        }).show();
    }

    @Override
    public void onTaskFinished(String Token) {

        try {
            if (Token.equals("set")) {
                if (btn_save != null && btn_cancel != null) {
//                    Globalarea.SpecificCard = sqliteDetail;
                    Intent intent = new Intent(CardScanActivity.this, SpecificPage.class);
                    intent.putExtra("TAG_CAMERA", whichCard);
                    intent.putExtra("WhictActivity", "CardScanActivity");
                    dialog.dismiss();
                    startActivity(intent);
                    finish();
                }
            } else if (Token.equals(Constants.SomethingWentWrong)) {
                dialog.dismiss();
                dialog_open(getResources().getString(R.string.SomethingWentWrong));
            }
        } catch (Exception e) {
            dialog.dismiss();
            dialog_open(getResources().getString(R.string.SomethingWentWrong));
        }
    }

    @Override
    public void onTaskFinished(CardDetail Token) {

    }

    @Override
    public void onTaskError(String Token) {
        try {
            dialog.dismiss();

            if (Token.equals(Constants.UploadError)) {
                dialog_open(getResources().getString(R.string.UploadError));
            } else if (Token.equals(Constants.InternetConnectionFail)) {
                dialog_open(getResources().getString(R.string.internetConnectionFail));
            } else if (Token.equals(Constants.SomethingWentWrong)) {
                dialog_open(getResources().getString(R.string.SomethingWentWrong));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskError(String Token, String errormessage) {

    }

    @Override
    public void onTaskFinished(String Token, String taskResponse) {

    }

    @Override
    public void onBackPressed() {
        Globalarea.documentPageList = null;
        Intent intent = new Intent(CardScanActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loginWithGoogle() {

        Intent signInIntent = ScanRDriveOperations.getGoogleSignInIntent(this);
        startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_REQUEST_CODE) {

            ScanRDriveOperations.handleGoogleSignInActivityResult(this, data, new ScanRDriveOperations.OnCompleteGoogleSignIn() {
                @Override
                public void onSuccessfulGoogleSignIn(GoogleSignInAccount account) {
                    preferences.setIsDriveConnected(true);
                    setGoogleDriveService();
                }

                @Override
                public void onFailure(Exception e, String message) {
                    String strMessage = "Sorry! we could not log you in! Please try again later!";

                    if (message != null)
                        strMessage = strMessage + message;

                    Toast.makeText(CardScanActivity.this, strMessage, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            });

        }
    }

    public void showGoogleDrivePermissionDialog() {
        ScanrDialog scanrDialog = new ScanrDialog(this, R.style.Theme_Dialog_SCANR);
        scanrDialog.setTitleText(getString(R.string.str_title_drive), R.color.black)
                .setSubTitleText(getString(R.string.str_drive_permission))
                .setPrimaryButton("Allow", v -> {
                    preferences.setShowedDriveDialog(true);
                    scanrDialog.dismiss();
                    loginWithGoogle();
                })
                .setSecondaryButton("Deny", v -> {
                    preferences.setShowedDriveDialog(true);
                    scanrDialog.dismiss();
//                    onBackPressed();
                })
                .removeClose(true);
        scanrDialog.setCancelable(false);
        scanrDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.more_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (!isNavigateTOScreen) {
            menuItem = item;
            isNavigateTOScreen = true;
            dialog_open(getString(R.string.confirm_descard));
            return true;
        }

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
