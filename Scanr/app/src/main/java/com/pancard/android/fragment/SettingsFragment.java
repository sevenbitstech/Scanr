package com.pancard.android.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.newflow.activity.NewHomeActivity;
import com.pancard.android.activity.newflow.activity.ProVersionActivity;
import com.pancard.android.activity.otheracivity.ContactUs;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.PreferenceManagement;

import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    TextView tvScanrPro;
    AdView adView;
    AdRequest adRequest;
    TextView tvTitle;
    //    ConstraintLayout proLayout;
    private ConstraintLayout proLayout, shareLayout, helpLayout;
    private PreferenceManagement preferences;
    private boolean checkedByApp;
    private SwitchCompat switchCloud;
    private ImageView imgEnterPro;
    private ImageView imgBackButton;
    private TextView tvDriveNote;
    private DriveServiceHelper mDriveServiceHelper;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        bindViews(view);
        initialize();
        return view;
    }

    private void bindViews(View view) {
        switchCloud = view.findViewById(R.id.switch_cloud);
//        imgEnterHelp = view.findViewById(R.id.img_enter_help);
//        imgEnterShare = view.findViewById(R.id.img_enter_share);
        imgBackButton = view.findViewById(R.id.img_back_button);
        imgEnterPro = view.findViewById(R.id.img_enter_pro);
        proLayout = view.findViewById(R.id.pro_layout);
        shareLayout = view.findViewById(R.id.share_layout);
        helpLayout = view.findViewById(R.id.help_layout);
        adView = view.findViewById(R.id.ad_view);
        tvDriveNote = view.findViewById(R.id.tv_note_drive);
        tvScanrPro = view.findViewById(R.id.tv_pro);
        tvTitle = view.findViewById(R.id.tv_title_text);
    }

    private void initialize() {

        tvTitle.setText(getString(R.string.action_settings));
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        preferences = Scanner.getInstance().getPreferences();

        shareLayout.setOnClickListener(view -> shareTheApp());

        helpLayout.setOnClickListener(view -> openHelp());

        initDriveSync();

        imgBackButton.setOnClickListener(view -> {
            if (getActivity() instanceof NewHomeActivity)
                getActivity().onBackPressed();
        });

        switchCloud.setChecked(preferences.isDriveConnected());
        switchCloud.setOnCheckedChangeListener((compoundButton, isChecked) -> {

            Log.e("checked change", "listener call");
            if (checkedByApp) {
                Log.e("checked by app", "true");
                checkedByApp = false;
                return;
            }


            if (isChecked) {

                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
                if (account != null) {
                    preferences.setIsDriveConnected(true);
                    showDriveNote();
                    Toast.makeText(getActivity(), getString(R.string.str_drive_sync_enable_msg), Toast.LENGTH_SHORT).show();
                    initDriveSync();

                } else
                    Globalarea.moveToInfoScreen(getActivity());
            } else {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
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

    }

    public void initDriveSync() {
        if (new ConnectionDetector(getActivity()).isConnectingToInternet()) {
            preferences = Scanner.getInstance().getPreferences();
            setGoogleDriveService(Scanner.getInstance().getApplicationContext());
            if (mDriveServiceHelper != null) {
                Globalarea.setDbToDriveSync(mDriveServiceHelper);
            }
        } else {
            Log.i("Not Connected", "Internet");
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

    private void openHelp() {
        Intent intent = new Intent(getActivity(), ContactUs.class);
        startActivity(intent);
    }

    private void shareTheApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.docscan.android");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        startActivity(Intent.createChooser(intent, "Share"));
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, getActivity());
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
    public void onResume() {
        super.onResume();
        showDriveNote();
        checkForSubscribedUserAds();

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
    public void onDestroyView() {
        super.onDestroyView();

        if (adView != null) {
            adView.destroy();
        }
    }

    private void checkForSubscribedUserAds() {
        if (Scanner.getInstance().getPreferences().isProActive()) {
            Log.e("pro active", "yes");
            subscribedUser();
            adView.setVisibility(View.GONE);
        } else {
            Log.e("pro active ", "no");
            nonSubScribedUser();
            adView.setVisibility(View.VISIBLE);
        }

    }

    private void nonSubScribedUser() {
        if (getActivity() != null)
            imgEnterPro.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.enter_arrow_white));
        tvScanrPro.setText(getString(R.string.str_upgrade_pro));
        proLayout.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ProVersionActivity.class);
            startActivity(intent);
        });

    }

    private void subscribedUser() {
        if (getActivity() != null)
            imgEnterPro.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.checkbox_selected));
        tvScanrPro.setText(getString(R.string.str_subscribed_pro));
        proLayout.setOnClickListener(view -> Toast.makeText(getActivity(), getString(R.string.str_already_subscribed), Toast.LENGTH_SHORT).show());

    }
}
