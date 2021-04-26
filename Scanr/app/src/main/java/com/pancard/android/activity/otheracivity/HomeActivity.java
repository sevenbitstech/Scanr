package com.pancard.android.activity.otheracivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.pancard.android.DatabaseHandler;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.common.SignInActivity;
import com.pancard.android.activity.scanactivity.CardScanActivity;
import com.pancard.android.activity.scanactivity.DriveListActivity;
import com.pancard.android.asyntask.ForceUpdateAsync;
import com.pancard.android.database.SyncDriveToDB;
import com.pancard.android.documentscanner.activities.MainActivity;
import com.pancard.android.listener.TaskListener;
import com.pancard.android.liveedgedetection.ScanActivity;
import com.pancard.android.liveedgedetection.ScanConstants;
import com.pancard.android.liveedgedetection.util.ScanUtils;
import com.pancard.android.model.CardDetail;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.AppRater;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.FileOpration;
import com.pancard.android.utility.PermissionManager;
import com.pancard.android.utility.PreferenceManagement;
import com.pancard.android.utility.ScanrDialog;
import com.pancard.android.validation_class.ReadImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class HomeActivity extends AppCompatActivity implements TaskListener {
    public static final int PICK_FILE_REQUEST_CODE = 1010;
    protected static final String TAG = HomeActivity.class.getSimpleName();
    private static final int INITIAL_REQUEST_CODE = 1;
    private static final int DOC_SCAN_REQUEST_CODE = 2;
    private static final int OPEN_CV_SCANNER_REQUEST_CODE = 3;
    private static final int GALLERY_PERMISSION_CODE = 4;
    private static final int QR_SCAN_REQUEST_CODE = 5;
    private static final int IMPROVED_SCANNER_REQUEST_CODE = 6;
    final int GOOGLE_REQUEST_CODE = 104;
    LinearLayout licenceFormat, panCardFormats;
    boolean back = false;
    PermissionManager permissionManager;
    ConnectionDetector connectionDetector;
    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
    String[] cameraPermission = {Manifest.permission.CAMERA};
    String[] permissionsStorage = {Manifest.permission.READ_EXTERNAL_STORAGE};
    String[] permissionsReadWriteStorage = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    AdRequest adRequest;
    String scannerType;
    PreferenceManagement preferences;
    //    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            new FirebaseManagement(context).init(context);
//        }
//    };
    private AdView adView;
    private DriveServiceHelper mDriveServiceHelper;
    private GoogleSignInClient mGoogleSignInClient;
    private InterstitialAd mInterstitialAd;
    //    private Handler backgroundDriveHandler;
//    private DriveDocRepo driveDocRepo;
    private ConnectivityChangeReceiver connectivityChangeReceiver;
    private TextView tvDriveNote;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        permissionManager = new PermissionManager(this);
        preferences = Scanner.getInstance().getPreferences();
        connectionDetector = new ConnectionDetector(this);
        connectivityChangeReceiver = new ConnectivityChangeReceiver();
        tvDriveNote = findViewById(R.id.tv_note_drive);

//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            driveDocRepo = new DriveDocRepo(this, FirebaseAuth.getInstance().getCurrentUser().getUid());
//        } else {
//            Toast.makeText(this, "May be your session has expired. Please login again.", Toast.LENGTH_SHORT).show();
//            logout();
//        }
        adView = findViewById(R.id.ad_view);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
//        InitializeMobileAds();

        AppRater.app_launched(this);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Globalarea.display_width = size.x;
        Globalarea.display_height = size.y;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(getResources().getString(R.string.app_name));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        licenceFormat = findViewById(R.id.licence_formate);
        panCardFormats = findViewById(R.id.pancard_formats);

//        if (!permissionManager.hasPermissions(permissions)) {
//            permissionManager.requestPermissions(permissions, INITIAL_REQUEST_CODE);
//        }

//        startGame();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String data = extras.getString("mUri");
            String Card_number = extras.getString("CardNumber");
            byte[] CreditCardImage = extras.getByteArray("CreditCardImage");
            String CardType = extras.getString("CardType");
            String CreditCardExpiryDate = extras.getString("CreditCardExpiryDate");
            String QrCodeError = extras.getString(Constants.ErrorOfQRcode);
            if (data != null) {
                try {
                    CommonScan.CARD_IMAGE = Globalarea.document_image = Globalarea.original_image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(data));
                    CommonScan.CARD_HOLDER_NAME = ReadImage.createCameraSource(Globalarea.document_image, this);
                    CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
                    selectImageChoose();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (Card_number != null && CreditCardImage != null) {
                CommonScan.CARD_IMAGE = BitmapFactory.decodeByteArray(CreditCardImage, 0, CreditCardImage.length);
                CommonScan.CARD_HOLDER_NAME = CardType;
                CommonScan.CARD_UNIQE_NO = Card_number;
                CommonScan.CARD_HOLDER_DOB = CreditCardExpiryDate;
                Intent intent = new Intent(HomeActivity.this, CardScanActivity.class);
                intent.putExtra("TAG_CAMERA", Constants.creditCard);
                startActivity(intent);
                finish();
            }
            if (QrCodeError != null) {

                final String whichCard = getIntent().getStringExtra(Constants.WHICH_ERROR);
                new android.app.AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Scanning Error")
                        .setMessage(QrCodeError)
                        .setPositiveButton("Scan Again",
                                (dialog, id) -> {
//                                        Intent intent = new Intent(HomeActivity.this, QRCodeScanner.class);
                                    Intent intent = new Intent(HomeActivity.this, QRCodeScanner.class);
                                    intent.putExtra("TAG_CAMERA", whichCard);
                                    startActivity(intent);
                                    finish();
                                }).setNegativeButton("Cancel", null).show();
            }
        }
//        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET"));

        //todo: Uncommnet below code register drive sync broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);

//        getDocumentDetailsFromDrive();
        if (Scanner.getInstance().getFilesDir().exists()) {
//            System.out.println("----oji_magazine_pages folder is created in device internal storage");
//            cacheDir = orchid.getInstance().getDir(".oji_magazine_pages", Context.MODE_PRIVATE);
            File cacheDir = Scanner.getInstance().getCacheDir();
            Log.e("Image Pathe : ", cacheDir.getAbsolutePath());
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            } else {
                System.out.println("----oji_magazine_pages folder is exist in device");
            }
        }
    }

//    private void InitializeMobileAds() {
//        Log.e("ad", "setting up");
//        MobileAds.initialize(this);
//
//        // Create the InterstitialAd and set the adUnitId.
//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id));
//
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());
//
//
//        mInterstitialAd.setAdListener(new AdListener() {
//            @Override
//            public void onAdClosed() {
////                mInterstitialAd.loadAd(new AdRequest.Builder().build());
//            }
//
//            @Override
//            public void onAdFailedToLoad(int i) {
//                super.onAdFailedToLoad(i);
//                Log.e("failed to load int", String.valueOf(i));
//            }
//
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
////                mInterstitialAd.show();
//            }
//        });
//    }

    @Override
    protected void onResume() {
        super.onResume();

        forceUpdate();
        showDriveNote();

        //todo: Uncommnet below line for drive to local sync
//        getDocumentDetailsFromDrive();

//        if (Globalarea.isInternetOn()) {
//            if (Globalarea.actionFire) {
//                Globalarea.actionFire = false;
//                new FirebaseManagement(this).init(this);
//            }
//        }
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

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, HomeActivity.this);
    }

    private void selectImageChoose() {

        Intent intent = new Intent(HomeActivity.this, CardScanActivity.class);
        intent.putExtra("TAG_CAMERA", Constants.document);
        startActivity(intent);
        finish();

        //todo: uncomment below line for give option for Type of document
//        final CharSequence[] items = {"Business Card", "Document", "Cancel"};
//
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select Type..");
//        builder.setCancelable(false);
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int item) {
//
//                if (items[item].equals("Business Card")) {
//                    Intent intent = new Intent(HomeActivity.this, CardScanActivity.class);
//                    intent.putExtra("TAG_CAMERA", Constants.businesscard);
//                    startActivity(intent);
//                    finish();
//                } else if (items[item].equals("Document")) {
//                    Intent intent = new Intent(HomeActivity.this, CardScanActivity.class);
//                    intent.putExtra("TAG_CAMERA", Constants.document);
//                    startActivity(intent);
//                    finish();
//                } else if (items[item].equals("BarCode OR QR code Scan")) {
//                    Globalarea.adharCardText = QRcodeScanFromImage.scanQRImage(Globalarea.document_image);
//                    Intent intent = new Intent(HomeActivity.this, QRCodeResultActivity.class);
//                    intent.putExtra("TAG_CAMERA", Constants.document);
//                    startActivity(intent);
//                    finish();
//                } else {
//                    dialog.dismiss();
//                }
//            }
//        });
//        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(broadcastReceiver);
        //todo: Uncommnet below line unregister drive sync broadcast receiver

        unregisterReceiver(connectivityChangeReceiver);

        if (adView != null) {
            adView.destroy();
        }
    }

//    public void startGame() {
//        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
//        Log.e("start", "game");
//        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
//            Log.e("ad is", "loaded");
//            AdRequest adRequest = new AdRequest.Builder().build();
//            Log.e("is test device", String.valueOf(adRequest.isTestDevice(this)));
//            mInterstitialAd.loadAd(adRequest);
//        }
//    }
//
//    public void showInterstitial() {
//        // Show the ad if it's ready. Otherwise toast and restart the game.
//        Log.e("show", "interstitial");
//
//        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//        } else {
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("text/plain");
//            intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.docscan.android");
//            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
//            startActivity(Intent.createChooser(intent, "Share"));
//            System.out.println("ad is not loaded");
//        }
//    }

    public void onPanCardScan(View pressed) {
        if (panCardFormats.getVisibility() == View.VISIBLE) {
            panCardFormats.setVisibility(View.GONE);
        } else {
            panCardFormats.setVisibility(View.VISIBLE);
        }
    }


    public void onPanCardScan1(View pressed) {
        navigateOpenCvCameraScreen(Constants.pancard);
    }

    public void onPassPortScan(View pressed) {
        navigateOpenCvCameraScreen(Constants.passport);
    }

    public void onDocumentScan(View pressed) {
        gotoImprovedScanner(Constants.businesscard);
    }

    public void onGeneralScan(View pressed) {

        if (permissionManager.hasPermissions(cameraPermission)) {
            goToDocumentScan();
        } else {
            if (permissionManager.shouldRequestPermission(this, cameraPermission)) {
                permissionManager.requestPermissions(cameraPermission, DOC_SCAN_REQUEST_CODE);
            } else {
                permissionManager.openSettingDialog(this, getResources().getString(R.string.camera_permission_access));
            }
        }
    }

    private void goToDocumentScan() {
        Intent intent = new Intent(HomeActivity.this, DocumentScan.class);
        startActivity(intent);
        finish();
    }

    public void onLicenseScan(View pressed) {
        if (licenceFormat.getVisibility() == View.VISIBLE) {
            licenceFormat.setVisibility(View.GONE);
        } else {
            licenceFormat.setVisibility(View.VISIBLE);
        }
    }

    public void onFormatLicenseScan_1(View pressed) {
        navigateOpenCvCameraScreen("licence_1");

    }

    public void onFormatLicenseScan_2(View pressed) {
        navigateOpenCvCameraScreen("licence_2");

    }

    public void onFormatLicenseScan_3(View pressed) {
        navigateOpenCvCameraScreen("licence_3");
    }

    public void navigateOpenCvCameraScreen(String scanner_type) {
        this.scannerType = scanner_type;
//        if(checkPermissions(OPEN_CV_SCANNER_REQUEST_CODE)) {
//            openCommonScanner(scanner_type);
//        }
        checkPermissions(OPEN_CV_SCANNER_REQUEST_CODE, scanner_type);

//        this.scannerType = scanner_type;
//        if (permissionManager.hasPermissions(cameraPermission)) {
//            openCommonScanner(scanner_type);
//        } else {
//            if (permissionManager.shouldRequestPermission(this, cameraPermission)) {
//                permissionManager.requestPermissions(cameraPermission, OPEN_CV_SCANNER_REQUEST_CODE);
//            } else {
//                permissionManager.openSettingDialog(this, getResources().getString(R.string.camera_permission_access));
//            }
//        }
    }

    private void openCommonScanner(String scannerType) {
        Intent intent = new Intent(this, CommonScan.class)
                .putExtra(CommonScan.SCANNER_TYPE, scannerType);
        startActivity(intent);
        finish();
    }

    public void onImproveGeneralScan(View view) {
        gotoImprovedScanner(Constants.document);
    }

    public void gotoImprovedScanner(String scannerType) {
        checkPermissions(IMPROVED_SCANNER_REQUEST_CODE, scannerType);
    }

    public void onPanCardScan2(View pressed) {
        scannerType = Constants.pancard2;
        openQrScanner(Constants.pancard2);
    }

    public void onAadharCardScan(View pressed) {
        scannerType = Constants.adharcard;
        openQrScanner(Constants.adharcard);
    }

    public void onBarcodeScan(View pressed) {
        scannerType = null;
        openQrScanner(null);
    }

    public void openQrScanner(String TAG) {
        checkPermissions(QR_SCAN_REQUEST_CODE, TAG);
    }

    public void onAllCard(View pressed) {

//        if (!checkLock() && preferences.getFirstOpen().equals("open")) {
//            preferences.setFirstOpen("unopen");
//            Intent intent = new Intent(HomeActivity.this, AppLockScrenn.class);
//            intent.putExtra(Constants.ActivityName, "FirstOpen");
//            startActivity(intent);
//            finish();
//        } else {
//        registerForContextMenu(pressed);
//        openContextMenu(pressed);
        Globalarea.openAllFilesMenu(this);
//        }

    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.setHeaderTitle("Card Type");
//        menu.add(0, 1, 0, "Pan Card");
//        menu.add(0, 2, 0, "Passport");
//        menu.add(0, 3, 0, "Driving Licence ");
//        menu.add(0, 4, 0, "Business Card");
//        menu.add(0, 5, 0, "Document ");
//        menu.add(0, 6, 0, "Aadhaar Card ");
//        menu.add(0, 7, 0, "Credit Card ");
//    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        if (item.getItemId() == 1) {
//
//            startActivity(Constants.pancard);
//
//        } else if (item.getItemId() == 2) {
//
//            startActivity(Constants.passport);
//
//        } else if (item.getItemId() == 3) {
//
//            startActivity(Constants.licence);
//
//        } else if (item.getItemId() == 4) {
//
//            startActivity(Constants.businesscard);
//
//        } else if (item.getItemId() == 5) {
//
//            startActivity(Constants.document);
//
//        } else if (item.getItemId() == 6) {
//
//            startActivity(Constants.adharcard);
//
//        } else if (item.getItemId() == 7) {
//
//            startActivity(Constants.creditCard);
//
//        }
//        return true;
//    }

//    private boolean checkLock() {
//        if (preferences.getPin() != null && preferences.getPin().trim().length() == 4) {
//            preferences.setFirstOpen("unopen");
//            return true;
//        }
//        return false;
//    }

//    private void callLockScreen(String tag) {
//        Intent intent = new Intent(HomeActivity.this, AppLockScrenn.class);
//        intent.putExtra(Constants.ActivityName, tag);
//        startActivity(intent);
//        finish();
//    }

    private void startActivity(String tag) {
        DatabaseHandler handler = new DatabaseHandler(this);

        //todo: Uncomment below code for checking local document size
//        if (handler.GetAllTableData(tag).size() > 0) {
//            if (checkLock()) {
//                callLockScreen(tag);
//            } else {
        Intent intent = new Intent(HomeActivity.this, DriveListActivity.class);
        intent.putExtra("TAG_CAMERA", tag);
        startActivity(intent);
        finish();
//            }
//        } else {
//            Toast.makeText(this, getString(R.string.error_no_card), Toast.LENGTH_LONG).show();
//        }
    }

    public void onShare(View pressed) {
//        showInterstitial();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.docscan.android");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        startActivity(Intent.createChooser(intent, "Share"));
    }

//    public void dialog_open(String message) {
//        new android.app.AlertDialog.Builder(HomeActivity.this)
//                .setTitle("Forgot PIN")
//                .setMessage(message)
//                .setPositiveButton("Agree",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,
//                                                int id) {
//                                if (checkLock()) {
//
//                                    int randomPIN = (int) (Math.random() * 9000) + 1000;
////                                    int PIN = Integer.parseInt(Scanner.getInstance().getUtils().getPreferences(Constants.pin).trim());
//                                    new SendMail(String.valueOf(randomPIN), HomeActivity.this).execute();
//                                }
//                            }
//                        }).show();
//    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() ");
    }

    @Override
    public void onBackPressed() {

        if (licenceFormat.getVisibility() == View.VISIBLE) {

            licenceFormat.setVisibility(View.GONE);
//            return;
        } else {

            if (back) {
                super.onBackPressed();
                return;
            }

            this.back = true;
            Toast.makeText(this, getResources().getString(R.string.click_back_exit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    back = false;
                    finish();
                }
            }, 100000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.more_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logoutItem = menu.findItem(R.id.logout);
        MenuItem driveItem = menu.findItem(R.id.setting);

        //todo: Uncomment below code for Logout button visibility
//        if (Globalarea.firebaseUser != null) {
//            logoutItem.setVisible(true);
//        } else {
//            logoutItem.setVisible(false);
//        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                Intent intent = new Intent(this, ContactUs.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.profile:
                Intent intent1 = new Intent(this, ProfileActivity.class);
                startActivity(intent1);
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
//            case R.id.pincode:
//                if (checkLock()) {
//                    PIN_dialog_open(true);
//                } else {
//                    PIN_dialog_open(false);
//                }
//
//                return true;

            case R.id.logout:
                logout();
                return true;

        }
        return false;
    }

    private void logout() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            FirebaseAuth.getInstance().signOut();
            preferences.removeAllData();
            Globalarea.firebaseUser = null;
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        });
    }

//    public void PIN_dialog_open(boolean lock) {
//        if (lock) {
//            selecPINOption();
//        } else {
//            new android.app.AlertDialog.Builder(HomeActivity.this)
//                    .setMessage("Security Setting")
//                    .setPositiveButton("Set PIN",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog,
//                                                    int id) {
//                                    Intent intent2 = new Intent(HomeActivity.this, AppLockScrenn.class);
//                                    intent2.putExtra(Constants.ActivityName, "FirstOpen");
//                                    startActivity(intent2);
//                                    finish();
//                                }
//                            }).show();
//        }
//    }

//    private void selecPINOption() {
//        final CharSequence[] items = {"Change PIN", "Remove PIN", "Forgot PIN"};
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
//        builder.setTitle("Security Setting..");
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int item) {
//
//                if (items[item].equals("Change PIN")) {
//                    Intent intent2 = new Intent(HomeActivity.this, AppLockScrenn.class);
//                    intent2.putExtra(Constants.ActivityName, "ChangePIN");
//                    startActivity(intent2);
//                    finish();
//                } else if (items[item].equals("Remove PIN")) {
//                    Intent intent2 = new Intent(HomeActivity.this, AppLockScrenn.class);
//                    intent2.putExtra(Constants.ActivityName, "RemovePIN");
//                    startActivity(intent2);
//                    finish();
//                } else if (items[item].equals("Forgot PIN")) {
//                    dialog_open("We will send your New PIN on ' " + Globalarea.firebaseUser.getEmail() + " ' EmailID.\n \nDo you Agree with that? ");
//                }
//            }
//        });
//        builder.show();
//    }

    @Override
    public void onTaskFinished(String Token) {

    }

    @Override
    public void onTaskFinished(CardDetail Token) {

    }

    @Override
    public void onTaskError(String Token) {

    }

    @Override
    public void onTaskError(String Token, String errormessage) {

    }

    @Override
    public void onTaskFinished(String Token, String taskResponse) {

    }

    public void onCreditCardScan(View view) {

        try {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.creditcard.android");
            if (launchIntent != null) {
                launchIntent.putExtra("io.card.payment.requireExpiry", true); // default: false
                launchIntent.putExtra("io.card.payment.requireCVV", true); // default: false
                launchIntent.putExtra("io.card.payment.requirePostalCode", true); // default: false
                launchIntent.putExtra("BackInMainApp", true); // default: false
                startActivity(launchIntent);//null pointer check in case package name was not found
                finish();
            } else {
                new android.app.AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Hint")
                        .setMessage("For the credit card scan you have to download Credit Card Scanner Application from play store..")
                        .setPositiveButton("Install",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.creditcard.android")));
                                        dialog.dismiss();
                                    }
                                }).setNegativeButton("Cancel", null).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkPermissionAndOpenGallery(View view) {

        if (permissionManager.hasPermissions(permissionsStorage)) {
            openGallery();
        } else {
            if (!preferences.isShowedStoragePermissionDialog() || permissionManager.shouldRequestPermission(this, permissionsStorage))
                permissionManager.showPermissionRequireDialog(this, getResources().getString(R.string.storage_permission_for),
                        new PermissionManager.OnPermissionRequireDialog() {
                            @Override
                            public void onAcceptPermissionDialog() {
                                Log.e("go permission", "yes");
                                if (!preferences.isShowedStoragePermissionDialog() || permissionManager.shouldRequestPermission(HomeActivity.this, permissionsStorage)) {
                                    permissionManager.requestPermissions(permissionsStorage, GALLERY_PERMISSION_CODE);
                                } else {
                                    permissionManager.openSettingDialog(HomeActivity.this, getResources().getString(R.string.storage_permission_for));
                                }
                            }

                            @Override
                            public void onCancelPermissionDialog() {
                                Log.e("go permission", "no");
                            }
                        });

            else
                permissionManager.openSettingDialog(this, getResources().getString(R.string.storage_permission_access));
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent = Intent.createChooser(intent, "Select file");
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            sendFile(data.getData(), data);
        } else if (requestCode == 101) {
            if (data != null && data.getExtras() != null && data.getExtras().getString(ScanConstants.SCANNED_RESULT) != null
                    && data.getExtras().getString("TAG_CAMERA") != null) {
                String filePath = data.getExtras().getString(ScanConstants.SCANNED_RESULT);
                Bitmap scanBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME);
                Globalarea.document_image = scanBitmap;
                CommonScan.CARD_HOLDER_NAME = ReadImage.createCameraSource(Globalarea.document_image, this);

                if (CommonScan.CARD_HOLDER_NAME != null) {
                    CommonScan.CARD_HOLDER_NAME = CommonScan.CARD_HOLDER_NAME.trim();
                }
                if (CommonScan.CARD_HOLDER_NAME == null) {
                    CommonScan.CARD_HOLDER_NAME = "New Scanner";
                }
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
//
                if (data.getStringExtra("TAG_CAMERA") != null) {
                    Log.e("TAG CAMERA Is", data.getStringExtra("TAG_CAMERA"));
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("TAG_CAMERA", data.getExtras().getString("TAG_CAMERA"));
                    startActivity(intent);
//                    finish();
                }

            }
        } else if (data == null) {
            Log.e("data", "null");
//            dialog_open("No Media is chosen ");
        }
    }

    //todo: check this method
    private void sendFile(Uri uri, Intent data) {
        try {
            String filePath;
            System.out.println("Data : " + uri);

            FileOpration fileOpration = new FileOpration();
            if (uri.getScheme().equals("content")) {
                filePath = fileOpration.getPath(uri, this);

            } else {
                filePath = uri.getPath();
            }

            if (filePath == null && data != null) {
                filePath = googleDriveFilePath(data.getData());
            }

            if (filePath != null) {
                Log.e("path ", filePath);
                Log.e("Selected path ", "yes");
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                CommonScan.CARD_IMAGE = Globalarea.document_image = Globalarea.original_image = bitmap;
                CommonScan.CARD_HOLDER_NAME = ReadImage.createCameraSource(Globalarea.document_image, this);
                CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
//                selectImageChoose();
                Globalarea.document_image = Globalarea.original_image;
                Globalarea.gallery_image_uri = uri;
                Intent intent = new Intent(this, MainActivity.class);
//                intent.putExtra("TAG_CAMERA", data.getExtras().getString("TAG_CAMERA") );
                startActivity(intent);
//                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Unknown path",
                        Toast.LENGTH_LONG).show();
                Log.e("Bitmap", "Unknown path");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String googleDriveFilePath(Uri uri) {

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            String extension = mime.getExtensionFromMimeType(getContentResolver().getType(uri));

            int randomPIN = (int) (Math.random() * 9000) + 1000;
            File file = new FileOpration().CaptureImage(inputStream, randomPIN + "documentscanner." + extension);
            return file.getAbsolutePath();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == DOC_SCAN_REQUEST_CODE || requestCode == OPEN_CV_SCANNER_REQUEST_CODE ||
                requestCode == GALLERY_PERMISSION_CODE || requestCode == QR_SCAN_REQUEST_CODE ||
                requestCode == IMPROVED_SCANNER_REQUEST_CODE) {

            if (requestCode != GALLERY_PERMISSION_CODE) {
                preferences.setShowedCameraPermissionDialog(true);
            } else {
                preferences.setShowedStoragePermissionDialog(true);
            }

            if (grantResults.length > 0) {
                if (permissionManager.isPermissionsGranted(grantResults)) {
                    if (requestCode == DOC_SCAN_REQUEST_CODE)
                        goToDocumentScan();
                    else if (requestCode == OPEN_CV_SCANNER_REQUEST_CODE) {
                        if (scannerType != null)
                            openCommonScanner(scannerType);
                    } else if (requestCode == QR_SCAN_REQUEST_CODE) {
                        openQrScanner(scannerType);
                    } else if (requestCode == IMPROVED_SCANNER_REQUEST_CODE) {
                        gotoImprovedScanner(scannerType);
                    } else {
                        openGallery();
                    }
                } else
                    Toast.makeText(this, getResources().getString(R.string.error_no_required_permission), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.error_no_required_permission), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getDocumentDetailsFromDrive() {

        if (connectionDetector != null && connectionDetector.isConnectingToInternet()) {
            if (permissionManager.hasPermissions(permissionsReadWriteStorage)) {

                Log.e("getting", "from metadata");
                if (mDriveServiceHelper != null && !preferences.isSyncDriveToDb()) {
                    Log.e(" synced", "started");

                    //todo: Uncomment below code for passign firebase userid as LocalDatabase name
//                    if (FirebaseAuth.getInstance().getCurrentUser() != null)
//                        SyncDriveToDB.startSyncDriveToDb(this, mDriveServiceHelper, FirebaseAuth.getInstance().getCurrentUser().getUid());
//                    else
//                        logout();

                    SyncDriveToDB.startSyncDriveToDb(this, mDriveServiceHelper, null);
                }
            } else {
                permissionManager.requestPermissions(permissionsReadWriteStorage, 123);
            }
        } else {
            Log.e("internet not available", "yes");
        }
    }

    private void setGoogleDriveService() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account == null) {
//            if (!preferences.isShowedDriveDialog()) {
//                showGoogleDrivePermissionDialog();
//            }
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

    public void showGoogleDrivePermissionDialog() {
        ScanrDialog scanrDialog = new ScanrDialog(this, R.style.Theme_Dialog_SCANR);
        scanrDialog.setTitleText(getString(R.string.str_title_drive), R.color.black)
                .setSubTitleText(getString(R.string.str_new_drive_permission))
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

    private void loginWithGoogle() {

        Intent signInIntent = ScanRDriveOperations.getGoogleSignInIntent(this);
        startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void forceUpdate() {
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String currentVersion = packageInfo.versionName;
            new ForceUpdateAsync(currentVersion, this).execute();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void checkPermissions(int permissionFor, String TAG) {

        if (permissionManager.hasPermissions(cameraPermission)) {
            if (permissionFor == OPEN_CV_SCANNER_REQUEST_CODE) {
                openCommonScanner(scannerType);
            } else if (permissionFor == IMPROVED_SCANNER_REQUEST_CODE) {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra("TAG_CAMERA", TAG);
                startActivityForResult(intent, 101);
            } else if (permissionFor == QR_SCAN_REQUEST_CODE) {
//                Intent intent = new Intent(this, QRCodeScanner.class);
                Intent intent = new Intent(this, MaterialBarcodeScannerActivity.class);
                if (TAG != null) {
                    intent.putExtra("TAG_CAMERA", TAG);
                }
                startActivity(intent);
                finish();
            }

        } else {
            if (!preferences.isShowedCameraPermissionDialog() || permissionManager.shouldRequestPermission(HomeActivity.this, cameraPermission)) {
                permissionManager.showPermissionRequireDialog(this, getResources().getString(R.string.camera_permission_for),
                        new PermissionManager.OnPermissionRequireDialog() {
                            @Override
                            public void onAcceptPermissionDialog() {
                                Log.e("go permission", "yes");
                                permissionManager.requestPermissions(cameraPermission, permissionFor);
                            }

                            @Override
                            public void onCancelPermissionDialog() {
                                Log.e("go permission", "no");
                            }
                        });

            } else {
                permissionManager.openSettingDialog(HomeActivity.this, getResources().getString(R.string.camera_permission_access));
            }
        }
    }
}
