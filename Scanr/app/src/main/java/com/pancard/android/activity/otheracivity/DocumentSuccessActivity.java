package com.pancard.android.activity.otheracivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.scanactivity.DriveListActivity;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PermissionManager;
import com.pancard.android.utility.PreferenceManagement;

public class DocumentSuccessActivity extends AppCompatActivity {
    private final static int REQUEST_CODE_CAMERA = 1;
    PermissionManager permissionManager;
    String[] permissions = {Manifest.permission.CAMERA};
    ImageView imgBack;
    TextView tvDriveNote;
    Button btnViewAllDoc, btnScanMore;
    PreferenceManagement preferences;
    String whichcard;
    AlertDialog alertDialog;
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_success);

        bindViews();
        initialize();
    }

    private void bindViews() {
        imgBack = findViewById(R.id.tv_btn_back);
        tvDriveNote = findViewById(R.id.tv_note_drive);

        btnViewAllDoc = findViewById(R.id.btn_view_all_doc);
        btnScanMore = findViewById(R.id.btn_scan_more);
    }

    private void gotoHome() {
        Intent intent = new Intent(DocumentSuccessActivity.this, HomeActivity.class);
        intent.putExtra("TAG_CAMERA", whichcard);
        intent.putExtra("WhictActivity", "CardScanActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initialize() {
        preferences = Scanner.getInstance().getPreferences();
        permissionManager = new PermissionManager(this);
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        btnViewAllDoc.setOnClickListener(v -> gotoListActivity());

        btnScanMore.setOnClickListener(v -> cameraopen());

        imgBack.setOnClickListener(v -> gotoHome());

        //todo: Uncommnet below code register drive sync broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);

    }

    private void gotoListActivity() {
        Intent intent = new Intent(DocumentSuccessActivity.this, DriveListActivity.class);
        intent.putExtra("TAG_CAMERA", whichcard);
        startActivity(intent);
        finish();
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, DocumentSuccessActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDriveNote();
        if (getIntent().getStringExtra("TAG_CAMERA") != null) {
            whichcard = getIntent().getStringExtra("TAG_CAMERA");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //todo: Uncommnet below line unregister drive sync broadcast receiver
        unregisterReceiver(connectivityChangeReceiver);
    }

    @Override
    public void onBackPressed() {
        gotoHome();
    }

    private void cameraopen() {
        if (whichcard.equals(Constants.businesscard)) {
            activityStart(whichcard);
        } else if (whichcard.equals(Constants.document)) {
            if (permissionManager.hasPermissions(permissions)) {
                openDocumentScan();
            } else {
                permissionManager.requestPermissions(permissions, REQUEST_CODE_CAMERA);
            }
        } else if (whichcard.equals(Constants.pancard)) {
            activityStart(whichcard);

        } else if (whichcard.equals(Constants.licence)) {
            openDialog();
        } else if (whichcard.equals(Constants.passport)) {
            activityStart(whichcard);
        } else if (whichcard.equals(Constants.adharcard)) {
//            Intent intent = new Intent(this, QRCodeScanner.class);
            Intent intent = new Intent(this, QRCodeScanner.class);
            intent.putExtra("TAG_CAMERA", Constants.adharcard);
            startActivity(intent);
            finish();
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
                    new AlertDialog.Builder(DocumentSuccessActivity.this)
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

    private void activityStart(String tag) {
        Intent intent = new Intent(this, CommonScan.class)
                .putExtra(CommonScan.SCANNER_TYPE, tag);
        startActivity(intent);
        finish();
    }

    private void openDocumentScan() {
        Intent intent = new Intent(DocumentSuccessActivity.this, DocumentScan.class);
        startActivity(intent);
        finish();
    }

    public void openDialog() {

        LayoutInflater li = LayoutInflater.from(DocumentSuccessActivity.this);
        View promptsView = li.inflate(R.layout.prompt_dialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                DocumentSuccessActivity.this);

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
        activityStart("licence_1");
    }

    public void onFormatLicenseScan_2(View pressed) {
        activityStart("licence_2");
    }

    public void onFormatLicenseScan_3(View pressed) {
        activityStart("licence_3");
    }
}
