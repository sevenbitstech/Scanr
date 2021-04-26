package com.pancard.android.activity.otheracivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.docscan.android.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.PreferenceManagement;

public class SettingActivity extends AppCompatActivity {

    SwitchCompat switchCloud;
    PreferenceManagement preferences;
    boolean checkedByApp;
    boolean firstSettingCheckStatus;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView tvDriveNote;
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        bindViews();
        initialize();
    }

    private void bindViews() {
        switchCloud = findViewById(R.id.switch_cloud);
        tvDriveNote = findViewById(R.id.tv_note_drive);
    }

    private void initialize() {
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(getResources().getString(R.string.app_name));

        preferences = Scanner.getInstance().getPreferences();
        firstSettingCheckStatus = true;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        switchCloud.setChecked(preferences.isDriveConnected());

        switchCloud.setOnCheckedChangeListener((compoundButton, isChecked) -> {

            Log.e("checked change", "listener call");
            if (checkedByApp) {
                Log.e("checked by app", "true");
                checkedByApp = false;
                return;
            }


            if (isChecked) {
//                Intent intent = new Intent(this, InfoActivity.class);
//                startActivity(intent);
//                finish();

                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {
                    preferences.setIsDriveConnected(true);
                    showDriveNote();
                    Toast.makeText(SettingActivity.this, getString(R.string.str_drive_sync_enable_msg), Toast.LENGTH_SHORT).show();
                } else
                    Globalarea.moveToInfoScreen(SettingActivity.this);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Alert")
                        .setMessage("Are you sure you want to turn off the auto sync your documents?")
                        .setPositiveButton("Yes, Please turn off.", (dialogInterface, i) -> {
                            signout();
                            dialogInterface.dismiss();
                        })
                        .setNegativeButton("cancel", (dialogInterface, i) -> {
                            checkedByApp = true;
                            switchCloud.setChecked(true);
                            dialogInterface.dismiss();
                        });

                builder.show();

                //todo: again show that yellow note

//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        //todo: again show that yellow note
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(SettingActivity.this, "We can not sign you out and unsync your account right now. So Please try agian.", Toast.LENGTH_SHORT).show();
//                        switchCloud.setChecked(true);
//                    }
//                });
            }
        });

        //todo: Uncommnet below code register drive sync broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);
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

            case R.id.view_files:
                Globalarea.openAllFilesMenu(this);
                return true;
        }
        return true;
    }

    private void signout() {
//        mGoogleSignInClient.signOut()
//                .addOnSuccessListener(aVoid -> {
        preferences.setIsDriveConnected(false);
        showDriveNote();
//            checkedByApp = true;
//            switchCloud.setChecked(false);
//        })
//                .addOnFailureListener(e -> {
//                    e.printStackTrace();
//                    Toast.makeText(SettingActivity.this, "Failed to turn off the auto sync. Please try again.", Toast.LENGTH_SHORT).show();
//                    checkedByApp = true;
//                    switchCloud.setChecked(true);
//                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
        finishAffinity();
//        finish();
//        finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDriveNote();

//        if (firstSettingCheckStatus) {
//            firstSettingCheckStatus = false;
//        } else {

//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //todo: Uncommnet below line unregister drive sync broadcast receiver
        unregisterReceiver(connectivityChangeReceiver);
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, SettingActivity.this);
    }
}
