package com.pancard.android.activity.scanactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.DriveOperations.MetadataModel;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.otheracivity.CommonScan;
import com.pancard.android.activity.otheracivity.ContactUs;
import com.pancard.android.activity.otheracivity.DocumentScan;
import com.pancard.android.activity.otheracivity.HomeActivity;
import com.pancard.android.activity.otheracivity.MaterialBarcodeScannerActivity;
import com.pancard.android.activity.otheracivity.SettingActivity;
import com.pancard.android.adapter.DocumentAdapter;
import com.pancard.android.database.DriveDocRepo;
import com.pancard.android.database.SyncStatus;
import com.pancard.android.listener.TaskListener;
import com.pancard.android.listview_design.PinnedHeaderListView;
import com.pancard.android.listview_design.SearchablePinnedHeaderListViewAdapter;
import com.pancard.android.liveedgedetection.ScanActivity;
import com.pancard.android.liveedgedetection.ScanConstants;
import com.pancard.android.liveedgedetection.util.ScanUtils;
import com.pancard.android.model.CardDetail;
import com.pancard.android.model.SqliteDetail;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PermissionManager;
import com.pancard.android.utility.PreferenceManagement;
import com.pancard.android.utility.ScanrDialog;
import com.pancard.android.validation_class.ReadImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DriveListActivity extends AppCompatActivity implements TaskListener {
    private final static int REQUEST_CODE_CAMERA = 1;
    private static final int OPEN_CV_SCANNER_REQUEST_CODE = 1;
    private static final int QR_SCAN_REQUEST_CODE = 2;
    private static final int IMPROVED_SCANNER_REQUEST_CODE = 3;
    final int GOOGLE_REQUEST_CODE = 102;
    public TextView tvNoValue;
    ActionBar actionBar;
    ArrayList<SqliteDetail> documents_list;
    ArrayList<DriveDocModel> deleteItem;
    ArrayList<MetadataModel> deleteItemMetadata;
    ArrayList<DriveDocModel> driveDocList;
    ArrayList<SqliteDetail> deleteDriveDocItem;
    String searchString;
    ProgressDialog dialog;
    String whichcard;
    AlertDialog alertDialog;
    SearchView searchView;
    ConnectionDetector connectionDetector;
    AdRequest adRequest;
    PreferenceManagement preferences;
    PermissionManager permissionManager;
    String[] permissions = {Manifest.permission.CAMERA};
    String scannerType;
    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private DriveServiceHelper mDriveServiceHelper;
    private LayoutInflater mInflater;
    private PinnedHeaderListView mListView;
    private DocumentAdapter mAdapter;
    private DriveDocRepo driveDocRepo;
    private TextView tvDriveNote;
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        initialize();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        actionBar = getSupportActionBar();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String data = extras.getString("TAG_CAMERA");
            if (data != null) {
                whichcard = data;
                displayListItem();
            }
        }

        //todo: Uncommnet below code register drive sync broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);

    }

    private void bindViews() {
        tvNoValue = findViewById(R.id.no_value);
        mListView = findViewById(android.R.id.list);
        tvDriveNote = findViewById(R.id.tv_note_drive);
        adView = (AdView) findViewById(R.id.ad_view);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void initialize() {
        preferences = Scanner.getInstance().getPreferences();
        permissionManager = new PermissionManager(this);
        mInflater = LayoutInflater.from(DriveListActivity.this);
        connectionDetector = new ConnectionDetector(this);
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        Globalarea.Card_type_activity = "Document";

        documents_list = new ArrayList<>();
        deleteItem = new ArrayList<>();

        //todo: Uncomment below code for separate DB with Firebase userid
//        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
//            driveDocRepo = new DriveDocRepo(this, FirebaseAuth.getInstance().getCurrentUser().getUid());
//        }else {
//            //todo: make logout a common
//            Toast.makeText(this, "May be your session has expired. Please login again.", Toast.LENGTH_SHORT).show();
//            FirebaseAuth.getInstance().signOut();
//            preferences.removeAllData();
//            Globalarea.firebaseUser = null;
//            Intent intent = new Intent(this, SignInActivity.class);
//            startActivity(intent);
//            finish();
//        }

        driveDocRepo = new DriveDocRepo(this);

        driveDocList = new ArrayList<>();
        deleteDriveDocItem = new ArrayList<>();

        dialog = new ProgressDialog(this);

        if (preferences.isDriveConnected()) {
            setGoogleDriveService();
        }

//        mListView.setOnItemClickListener((parent, view, position, id) -> {
////            Globalarea.SpecificCard = handler.getSingleRow(whichcard, mAdapter.getItem(position).getScan_time());
//
//            DriveDocModel driveDocModel = driveDocList.get(position);
//
//            Globalarea.SpecificCard = new SqliteDetail(driveDocModel.getCardDetail());
//            Intent intent = new Intent(DriveListActivity.this, SpecificPage.class);
//            intent.putExtra("TAG_CAMERA", whichcard);
//            intent.putExtra("WhictActivity", "ListActivity");
//            startActivity(intent);
//            finish();
//        });

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                mode.setTitle(mListView.getCheckedItemCount() + " items selected");

                if (checked) {
                    deleteItem.add(mAdapter.getItem(position));
                } else {
                    deleteItem.remove(mAdapter.getItem(position));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete_all) {
                    Log.i("deleting items", String.valueOf(deleteItem.size()));
                    dialog_open(mode);
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                deleteItem.clear();
            }
        });

    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, DriveListActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(connectivityChangeReceiver);

        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Globalarea.isInternetOn()) {
//            if (Globalarea.actionFire) {
//                Globalarea.actionFire = false;
//                new FirebaseManagement(this).init(this);
//            }
//        }
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

    private void setGoogleDriveService() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account == null) {
            if (!preferences.isShowedDriveDialog()) {
                showGoogleDrivePermissionDialog();
            }
//            Toast.makeText(this, "You are not logged in! Please login again", Toast.LENGTH_SHORT).show();
//            onBackPressed();
        } else {

            if (preferences.isDriveConnected()) {
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
            } else {
                if (!preferences.isShowedDriveDialog()) {
                    showGoogleDrivePermissionDialog();
                }
            }


        }
    }

    private void loginWithGoogle() {

        Intent signInIntent = ScanRDriveOperations.getGoogleSignInIntent(this);
        startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);

    }

    public void dialog_open(final ActionMode mode) {
        if (searchView != null) {
            if (!searchView.isIconified()) {
                searchView.setIconified(true);
            }
        }

        new AlertDialog.Builder(DriveListActivity.this)
                .setMessage("Are you sure you want to Delete " + deleteItem.size() + " records?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, id) -> {
                            if (deleteItem.size() > 0) {
                                Globalarea.actionFire = true;
                                callLocalDeleteDocument();
                                deleteItem.clear();
                                displayListItem();
                                mode.finish();
                            }
                        }).setNegativeButton("No", null).show();
    }

    private void callLocalDeleteDocument() {
        if (deleteItem != null && deleteItem.size() > 0) {
            for (DriveDocModel driveDocModel : deleteItem) {
                driveDocModel.setSyncStatus(SyncStatus.deleted.toString());
                int deleted = driveDocRepo.addDriveDocInfo(driveDocModel);
                if (deleted > 0) {
                    Toast.makeText(this, "Document Deleted Successfully.", Toast.LENGTH_SHORT).show();
                    getLocalDocumentsDetails();
                }
            }
        }
    }

    private void displayListItem() {
        getLocalDocumentsDetails();
    }

    private void getLocalDocumentsDetails() {

        //todo: do something with multiple pages i.e. folder name with _

        List<DriveDocModel> driveDocModelList = driveDocRepo.getAllDriveDocs();
        List<DriveDocModel> currentDocModelList = new ArrayList<>();

        if (driveDocModelList != null) {
            Log.e("size", String.valueOf(driveDocModelList.size()));

            Gson gson = new Gson();
            TypeToken<CardDetail> token = new TypeToken<CardDetail>() {
            };
            for (DriveDocModel driveDocModel : driveDocModelList) {

                CardDetail cardDetail = gson.fromJson(driveDocModel.getJsonText(), token.getType());
                if (cardDetail != null) {

                    driveDocModel.setCardDetail(cardDetail);
                    currentDocModelList.add(driveDocModel);

                }
            }
            setDocumentsWithData(currentDocModelList);
            Log.i("setting adapater", "yes");
            setupAdapter();
        }
    }

    private void setDocumentsWithData(List<DriveDocModel> googleDriveFileHolderList) {

        List<DriveDocModel> newDriveDocModels = new ArrayList<>();
        if (googleDriveFileHolderList != null) {
            for (DriveDocModel googleDriveFileHolder : googleDriveFileHolderList) {
                if (googleDriveFileHolder != null) {
                    newDriveDocModels.add(googleDriveFileHolder);
                }
            }
        }
        driveDocList.clear();
        driveDocList.addAll(newDriveDocModels);

    }

    private void setupAdapter() {
        if (driveDocList != null && driveDocList.size() > 0) {
            Log.e("call loop 1: ", "1");
            mAdapter = new DocumentAdapter(this, driveDocList, new SearchablePinnedHeaderListViewAdapter.OnNoValues() {
                @Override
                public void onNoValues(int size) {
                    if (size > 0) {
                        tvNoValue.setVisibility(View.GONE);
                    } else {
                        tvNoValue.setVisibility(View.VISIBLE);
                    }
                }
            });
            //Fixme: commented below code, Check After complete Drive Sync Process

            mListView.setOnItemClickListener((parent, view, position, id) -> {
                DriveDocModel driveDocModel = driveDocList.get(position);
                Globalarea.SpecificCard = new SqliteDetail(driveDocModel.getCardDetail());

                if (driveDocModel.getCardDetail() != null) {
                    if (driveDocModel.getPublicGuid() != null && driveDocModel.getOriginalImagePath() != null) {
                        Log.e("Public GUId", driveDocModel.getPublicGuid());
                        Log.e("Original Image Path", driveDocModel.getOriginalImagePath());
                    }
                    passDataToEditSpecificActivity(driveDocModel);
                }
            });

            mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.dark_theme));
            mListView.setPinnedHeaderView(mInflater.inflate(R.layout.pinned_header_listview_side_header, mListView, false));
            mListView.setAdapter(mAdapter);
            mListView.setOnScrollListener(mAdapter);
            mListView.setEnableHeaderTransparencyChanges(false);
        } else {
            Log.e("call loop 1: ", "2");

            tvNoValue.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        MenuItem search = menu.findItem(R.id.menuItem_search);
        getMenuInflater().inflate(R.menu.more_menu, menu);
        SearchView searchView = (SearchView) search.getActionView();

//        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menuItem_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                searchString = newText;
                performSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_Camera:
                cameraopen();
                return true;
            case R.id.menuItem_Refresh:
                if (searchView != null) {
                    if (searchView.isIconified()) {
                        displayListItem();
                    }
                } else {
                    displayListItem();

                }
                return true;

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

    private void gotoEditDriveDocument() {
        if (driveDocList != null && driveDocList.size() > 0) {
            DriveDocModel driveDocModel = driveDocList.get(0);

            if (driveDocModel != null && driveDocModel.getCardDetail() != null) {

                passDataToEditSpecificActivity(driveDocModel);

//                    ScanRDriveOperations.downloadFiles(driveDocList,mDriveServiceHelper,driveDocModel,0,new ScanRDriveOperations.OnCompleteDriveQueries() {
//
//                        @Override
//                        public void onSuccessfulDriveQuery(List<DriveDocModel> googleDriveFileHolderList) {
//                            Toast.makeText(DriveListActivity.this, "Download Success", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onFailure(Exception e, String message) {
//                            Toast.makeText(DriveListActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
//                        }
//
//                    },
//                            new ScanRDriveOperations.OnCompleteDriveFileRead() {
//
//                                @Override
//                                public void onSuccessfulDriveFileRead(CardDetail cardDetailModel, Bitmap imageCard,String fileId) {
//                                        if(cardDetailModel != null) {
//                                            if(cardDetailModel.getCard_name() != null) {
//                                                Log.i("carddetails data",cardDetailModel.getCard_name());
//                                                Toast.makeText(DriveListActivity.this, "Successfully got model from drive.", Toast.LENGTH_SHORT).show();
//
//
//                                            }
//
//                                        }
//                                }
//
//                                @Override
//                                public void onFailure(Exception e, String message) {
//                                    Toast.makeText(DriveListActivity.this, "Successfully got model from drive.", Toast.LENGTH_SHORT).show();
//                                }
//                            });
            }
        }
    }

    private void passDataToEditSpecificActivity(DriveDocModel driveDocModel) {


        Intent intent = new Intent(DriveListActivity.this, SpecificPage.class);
        intent.putExtra("TAG_CAMERA", whichcard);
        intent.putExtra(Constants.CARD_DATAT, driveDocModel);
        intent.putExtra("WhictActivity", "ListActivity");
//        intent.putExtra(Constants.FOLDER_HOLDER_DATA, selectedDocumetFolder.getName());
//        intent.putExtra(Constants.FOLDER_HOLDER_ID_DATA, selectedDocumetFolder.getId());
//        intent.putExtra(Constants.FILE_HOLDER_DATA, fileId);
        startActivity(intent);
        finish();
    }

    public void performSearch(final String queryText) {
        if (mAdapter != null) {
            mAdapter.getFilter().filter(queryText);
            mAdapter.setHeaderViewVisible(TextUtils.isEmpty(queryText));
        }
    }

    @Override
    public void onTaskFinished(String Token) {
        try {
            dialog.dismiss();
            if (Token != null) {
                if (Token.equals("GetAllData")) {
                    displayListItem();
                    deleteItem.clear();

                } else if (Token.equals("NoDataAvailable")) {
                    Globalarea.cardDetail.clear();
                    displayListItem();
                } else {

                    new AlertDialog.Builder(DriveListActivity.this)
                            .setMessage(Token)
                            .setTitle("Error")
                            .setCancelable(false).setNegativeButton("Finish", (dialog, which) -> {
                        Intent intent = new Intent(DriveListActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }).show();
                }
            } else {

                new AlertDialog.Builder(DriveListActivity.this)
                        .setMessage(getString(R.string.error_something_wrong))
                        .setTitle("Error")
                        .setCancelable(false).setNegativeButton("Finish", (dialog, which) -> {
                    Intent intent = new Intent(DriveListActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void dialog_open(String message) {
        new AlertDialog.Builder(DriveListActivity.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK",
                        (dialog, id) -> {

                            System.out.println(" User is Agree to Delete the File ");
                            Intent intent = new Intent(DriveListActivity.this, HomeActivity.class);
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


    // Adapter class

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(DriveListActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void cameraopen() {
        if (whichcard.equals(Constants.businesscard)) {
            navigateOpenCvCameraScreen(whichcard);
        } else if (whichcard.equals(Constants.document)) {
            gotoImprovedScanner(whichcard);
        } else if (whichcard.equals(Constants.pancard)) {
            navigateOpenCvCameraScreen(whichcard);
        } else if (whichcard.equals(Constants.licence)) {
            openDialog();
        } else if (whichcard.equals(Constants.passport)) {
            navigateOpenCvCameraScreen(whichcard);
        } else if (whichcard.equals(Constants.adharcard)) {
            openQrScanner(Constants.adharcard);
        } else if (whichcard.equals(Constants.creditCard)) {
            try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.creditcard.android");
                if (launchIntent != null) {
                    launchIntent.putExtra("io.card.payment.requireExpiry", true); // default: false
                    launchIntent.putExtra("io.card.payment.requireCVV", true); // default: false
                    launchIntent.putExtra("io.card.payment.requirePostalCode", true); // default: false
                    startActivity(launchIntent);//null pointer check in case package name was not found
                    finish();
                } else {
                    new AlertDialog.Builder(DriveListActivity.this)
                            .setTitle("Hint")
                            .setMessage("For the credit card scan you have to download Credit Card Scanner Application from play store..")
                            .setPositiveButton("Install",
                                    (dialog, id) -> {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.creditcard.android")));
                                        dialog.dismiss();
                                    }).setNegativeButton("Cancel", null).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        if (requestCode == REQUEST_CODE_CAMERA) {
//
//            if (grantResults.length > 0) {
//                boolean permissionGranted = true;
//
//                for (int result : grantResults) {
//                    if (result != PackageManager.PERMISSION_GRANTED) {
//                        permissionGranted = false;
//                        break;
//                    }
//                }
//
//                if (permissionGranted) {
//                    openDocumentScan();
//                } else
//                    requestDialog();
//            } else {
//                requestDialog();
//            }
//        }

        if (requestCode == OPEN_CV_SCANNER_REQUEST_CODE || requestCode == QR_SCAN_REQUEST_CODE ||
                requestCode == IMPROVED_SCANNER_REQUEST_CODE) {

            preferences.setShowedCameraPermissionDialog(true);
            if (grantResults.length > 0) {
                if (permissionManager.isPermissionsGranted(grantResults)) {
                    if (requestCode == OPEN_CV_SCANNER_REQUEST_CODE) {
                        navigateOpenCvCameraScreen(scannerType);
                    } else if (requestCode == QR_SCAN_REQUEST_CODE) {
                        openQrScanner(scannerType);
                    } else {
                        gotoImprovedScanner(scannerType);
                    }
                } else
                    Toast.makeText(this, getResources().getString(R.string.error_no_required_permission), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.error_no_required_permission), Toast.LENGTH_LONG).show();
            }
        }

    }

    private void requestDialog() {
        new AlertDialog.Builder(DriveListActivity.this)
                .setMessage("Document Scanner needs permission to Access camera. Do you want to grant it?")
                .setTitle("Error")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    if (permissionManager.shouldRequestPermission(DriveListActivity.this, permissions)) {
                        permissionManager.requestPermissions(permissions, REQUEST_CODE_CAMERA);
                    } else {
                        permissionManager.openSettingDialog(DriveListActivity.this, getString(R.string.camera_permission_access));
                    }
                }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
    }

    private void openDocumentScan() {
        Intent intent = new Intent(DriveListActivity.this, DocumentScan.class);
        startActivity(intent);
        finish();
    }

    private void navigateOpenCvCameraScreen(String scanner_type) {
        this.scannerType = scanner_type;
        checkPermissions(OPEN_CV_SCANNER_REQUEST_CODE, scanner_type);
    }

    public void openQrScanner(String scanner_type) {
        this.scannerType = scanner_type;
        checkPermissions(QR_SCAN_REQUEST_CODE, scanner_type);
    }

    public void gotoImprovedScanner(String scannerType) {
//        if(checkPermissions(IMPROVED_SCANNER_REQUEST_CODE)) {
//            startActivityForResult(new Intent(this, ScanActivity.class), 101);
//        }
        this.scannerType = scannerType;
        checkPermissions(IMPROVED_SCANNER_REQUEST_CODE, scannerType);

    }

    public void openDialog() {

        LayoutInflater li = LayoutInflater.from(DriveListActivity.this);
        View promptsView = li.inflate(R.layout.prompt_dialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                DriveListActivity.this);

        // set prompts.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialog, which) -> alertDialog.dismiss());

        // create alert dialog
        alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_REQUEST_CODE) {

            ScanRDriveOperations.handleGoogleSignInActivityResult(this, data, new ScanRDriveOperations.OnCompleteGoogleSignIn() {
                @Override
                public void onSuccessfulGoogleSignIn(GoogleSignInAccount account) {
                    preferences.setIsDriveConnected(true);
                    Log.i("Setting up", "drive service");
                    setGoogleDriveService();
                }

                @Override
                public void onFailure(Exception e, String message) {
                    String strMessage = "Sorry! we could not log you in! Please try again later!";

                    if (message != null)
                        strMessage = strMessage + message;

                    Toast.makeText(DriveListActivity.this, strMessage, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            });

        }

        if (requestCode == 101) {
            if (data != null && data.getExtras() != null && data.getExtras().getString(ScanConstants.SCANNED_RESULT) != null) {
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
                Intent intent = new Intent(this, CardScanActivity.class);
                intent.putExtra("TAG_CAMERA", Constants.document);
                startActivity(intent);
                finish();
            }
        }
    }

    public void showGoogleDrivePermissionDialog() {
        ScanrDialog scanrDialog = new ScanrDialog(this, R.style.Theme_Dialog_SCANR);
        scanrDialog.setTitleText(getString(R.string.str_title_drive), R.color.black)
                .setSubTitleText(getString(R.string.str_new_drive_permission))
                .setPrimaryButton("Allow", v -> {
                    scanrDialog.dismiss();
                    loginWithGoogle();
                    preferences.setShowedDriveDialog(true);
                })
                .setSecondaryButton("Deny", v -> {
                    scanrDialog.dismiss();
                    preferences.setShowedDriveDialog(true);
//                    onBackPressed();
                })
                .removeClose(true);
        scanrDialog.setCancelable(false);

        scanrDialog.show();
    }

    public void callDriveDeleteDocument() {
        if (connectionDetector != null && connectionDetector.isConnectingToInternet()) {

            if (preferences.isDriveConnected()) {

                if (deleteItem != null && deleteItem.size() > 0) {

                    dialog.show();
                    dialog.setMessage("Please wait");
                    ScanRDriveOperations.deleteFolder(deleteItem, mDriveServiceHelper, 0, new ScanRDriveOperations.OnCompleteDriveOperations() {

                        @Override
                        public void onSuccess(String message) {
                            dialog.dismiss();
                            if (message != null) {

                                deleteItemMetadata = new ArrayList<>();
                                for (DriveDocModel driveDocModel : deleteItem) {
                                    deleteItemMetadata.add(new MetadataModel(driveDocModel));
                                }

                                deleteFromMetadata();
                            } else {
                                Toast.makeText(DriveListActivity.this, "Folder Not removed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Exception e, String message) {
                            dialog.dismiss();
                            Toast.makeText(DriveListActivity.this, "Error removing folder from the Drive", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            } else {
                showGoogleDrivePermissionDialog();
            }

        } else {
            Toast.makeText(this, getString(R.string.internetConnectionFail), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFromMetadata() {

        dialog.show();
        dialog.setMessage("Please wait");
        ScanRDriveOperations.findMetadataDocForOperation(mDriveServiceHelper,
                new ScanRDriveOperations.OnCompleteMetaDataQueries() {
                    @Override
                    public void onSuccessFullMetadataContent(String metadataFileId,
                                                             List<MetadataModel> metadataModels) {

                        Log.e("found metadata content", String.valueOf(metadataModels.size()));
                        ScanRDriveOperations.deleteDocInMetadata(mDriveServiceHelper,
                                metadataFileId, metadataModels, deleteItemMetadata,
                                new ScanRDriveOperations.OnCompleteDriveOperations() {
                                    @Override
                                    public void onSuccess(String message) {
                                        dialog.dismiss();
                                        Toast.makeText(DriveListActivity.this, "Documents removed Successfully", Toast.LENGTH_SHORT).show();
                                        displayListItem();
                                    }

                                    @Override
                                    public void onFailure(Exception e, String message) {
                                        dialog.dismiss();
                                        e.printStackTrace();
                                        Toast.makeText(DriveListActivity.this, "Failed to update metadata doc", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Exception e, String message) {
                        dialog.dismiss();
                        e.printStackTrace();
                        Toast.makeText(DriveListActivity.this, "Failed to find metadata doc", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void checkPermissions(int permissionFor, String TAG) {

        if (permissionManager.hasPermissions(permissions)) {
            if (permissionFor == OPEN_CV_SCANNER_REQUEST_CODE) {
                Intent intent = new Intent(this, CommonScan.class).putExtra(CommonScan.SCANNER_TYPE, TAG);
                startActivity(intent);
                finish();
            } else if (permissionFor == IMPROVED_SCANNER_REQUEST_CODE) {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra("TAG_CAMERA", TAG);
                startActivityForResult(intent, 101);
//                startActivityForResult(new Intent(this, ScanActivity.class), 101);
            } else if (permissionFor == QR_SCAN_REQUEST_CODE) {
                Intent intent = new Intent(this, MaterialBarcodeScannerActivity.class);
                if (TAG != null) {
                    intent.putExtra("TAG_CAMERA", TAG);
                }
                startActivity(intent);
                finish();
            }

        } else {

            if (!preferences.isShowedCameraPermissionDialog() || permissionManager.shouldRequestPermission(DriveListActivity.this, permissions)) {

                permissionManager.showPermissionRequireDialog(this, getResources().getString(R.string.camera_permission_for),
                        new PermissionManager.OnPermissionRequireDialog() {
                            @Override
                            public void onAcceptPermissionDialog() {
                                Log.e("go permission", "yes");
                                permissionManager.requestPermissions(permissions, permissionFor);
                            }

                            @Override
                            public void onCancelPermissionDialog() {
                                Log.e("go permission", "no");
                            }
                        });

            } else {
                permissionManager.openSettingDialog(DriveListActivity.this, getResources().getString(R.string.camera_permission_access));
            }
        }
    }
}
