package com.pancard.android.activity.otheracivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.PreferenceManagement;

import java.util.Collections;

public class InfoActivity extends AppCompatActivity {

    final int GOOGLE_REQUEST_CODE = 103;
    PreferenceManagement preferences;
    Button btnGotIt, btnTryAgain;
    TextView tvInfo, tvDriveNote;
    private DriveServiceHelper mDriveServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_info);

        bindViews();
        initialize();

    }

    private void bindViews() {
        btnGotIt = findViewById(R.id.btn_got_it);
        btnTryAgain = findViewById(R.id.btn_try_again);
        tvInfo = findViewById(R.id.tv_info);
        tvDriveNote = findViewById(R.id.tv_note_drive);
    }

    private void initialize() {
        preferences = Scanner.getInstance().getPreferences();
        hideTryAgain();
        btnGotIt.setOnClickListener(view -> loginWithGoogle());
        btnTryAgain.setOnClickListener(view -> loginWithGoogle());
    }

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
//                    preferences.setShowedDriveDialog(true);
                    Toast.makeText(InfoActivity.this, getString(R.string.str_drive_sync_enable_msg), Toast.LENGTH_SHORT).show();

                    if (new ConnectionDetector(InfoActivity.this).isConnectingToInternet()) {
                        preferences = Scanner.getInstance().getPreferences();
                        setGoogleDriveService(Scanner.getInstance().getApplicationContext());
                        if (mDriveServiceHelper != null) {
                            Globalarea.setDbToDriveSync(mDriveServiceHelper);
                        }
                    } else {
                        Log.i("Not Connected", "Internet");
                    }

                    onBackPressed();
                }

                @Override
                public void onFailure(Exception e, String message) {
                    showTryAgain();
                }
            });

        }
    }

    private void setGoogleDriveService(Context context) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (preferences.isDriveConnected()) {
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

    private void showTryAgain() {
        btnTryAgain.setVisibility(View.VISIBLE);
        btnGotIt.setVisibility(View.GONE);
        tvInfo.setText(getString(R.string.str_failed_drive_integration));
    }

    private void hideTryAgain() {
        btnTryAgain.setVisibility(View.GONE);
        btnGotIt.setVisibility(View.VISIBLE);
        tvInfo.setText(getString(R.string.str_new_drive_permission));
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, InfoActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        showDriveNote();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
//        Intent intent = new Intent(InfoActivity.this,SettingActivity.class);
//        startActivity(intent);
//        finish();
//        finishAffinity();
    }

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
