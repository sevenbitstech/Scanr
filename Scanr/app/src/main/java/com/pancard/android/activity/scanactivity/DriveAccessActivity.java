package com.pancard.android.activity.scanactivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.activity.otheracivity.HomeActivity;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.PreferenceManagement;

public class DriveAccessActivity extends AppCompatActivity {

    final int GOOGLE_REQUEST_CODE = 103;
    PreferenceManagement preferences;
    CheckBox checkBoxDrivePermission;

    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_access);

        bindView();
        initialize();
    }

    private void bindView() {
        checkBoxDrivePermission = findViewById(R.id.checkbox_drive_allow);
        btnSave = findViewById(R.id.btn_save);
    }

    private void initialize() {

        preferences = new PreferenceManagement(this);

        setCheckBoxForDrivePermission();

        checkBoxDrivePermission.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("is checked", String.valueOf(isChecked));
                if (preferences.isShowedDriveDialog()) {

                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBoxDrivePermission.isChecked()) {
                    loginWithGoogle();
                } else {
                    preferences.setIsDriveConnected(false);
                    Toast.makeText(DriveAccessActivity.this, getString(R.string.str_drive_sync_disable_msg), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void setCheckBoxForDrivePermission() {
        if (preferences.isDriveConnected()) {
            checkBoxDrivePermission.setChecked(true);
        } else {
            checkBoxDrivePermission.setChecked(false);
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DriveAccessActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

//    private void setGoogleDriveService() {
//
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//
//        if (account == null) {
//            loginWithGoogle();
////            Toast.makeText(this, "You are not logged in! Please login again", Toast.LENGTH_SHORT).show();
////            onBackPressed();
//        } else {
//            Log.e("drive service", "already added");
//            Toast.makeText(this, "You are not logged in! Please login again", Toast.LENGTH_SHORT).show();
//
//        }
//    }

    private void loginWithGoogle() {
        if (!new ConnectionDetector(this).isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.internetConnectionFail), Toast.LENGTH_SHORT).show();
            return;
        }

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
                    preferences.setShowedDriveDialog(true);
                    Toast.makeText(DriveAccessActivity.this, getString(R.string.str_drive_sync_enable_msg), Toast.LENGTH_SHORT).show();
//                    setGoogleDriveService();
                }

                @Override
                public void onFailure(Exception e, String message) {
                    String strMessage = "Sorry! we could not log you in! Please try again later!";

                    if (message != null)
                        strMessage = strMessage + message;

                    Toast.makeText(DriveAccessActivity.this, strMessage, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            });

        }
    }
}
