package com.pancard.android.activity.newflow.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pancard.android.Globalarea;
import com.pancard.android.activity.otheracivity.CommonScan;
import com.pancard.android.asyntask.ForceUpdateAsync;
import com.pancard.android.documentscanner.activities.MainActivity;
import com.pancard.android.fragment.CameraFragment;
import com.pancard.android.fragment.FilesFragment;
import com.pancard.android.fragment.SettingsFragment;
import com.pancard.android.liveedgedetection.ScanConstants;
import com.pancard.android.liveedgedetection.util.ScanUtils;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.FileOpration;
import com.pancard.android.utility.PreferenceManagement;
import com.pancard.android.validation_class.ReadImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NewHomeActivity extends AppCompatActivity {
    public static final int PICK_FILE_REQUEST_CODE = 1010;

    BottomAppBar bottomAppBar;
    BottomNavigationView bottomNavigationView;
    ConstraintLayout filesTab, cameraTab, settingsTab, bottomBaseLayout;
    ConnectivityChangeReceiver connectivityChangeReceiver;
    AdView adView;
    AdRequest adRequest;
    boolean back = false;

    AppCompatImageView imgFiles, imgCamera, imgSettings;
    TextView tvFiles, tvSetting;
    View viewDash;
    PreferenceManagement preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home);
        bindViews();
        initialize();
    }

    private void bindViews() {
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        bottomNavigationView = findViewById(R.id.navigation_view);
        filesTab = findViewById(R.id.my_files_tab);
        cameraTab = findViewById(R.id.camera_tab);
        settingsTab = findViewById(R.id.settings_tab);
        adView = findViewById(R.id.ad_view);

        imgFiles = findViewById(R.id.img_my_files);
        imgCamera = findViewById(R.id.img_camera);
        imgSettings = findViewById(R.id.img_settings);

        tvFiles = findViewById(R.id.tv_my_files);
        tvSetting = findViewById(R.id.tv_settings);

//        viewDash = findViewById(R.id.view_dash_files);
        bottomBaseLayout = findViewById(R.id.bottom_base);
    }

    private void initialize() {
        preferences = new PreferenceManagement(this);
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);

        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if (getIntent() != null) {
            String redirectTAG = getIntent().getStringExtra(Constants.START_FRAGMENT);
            if (redirectTAG != null && redirectTAG.equals(Constants.FILES_TAG)) {
                changeFragment(new FilesFragment(), Constants.FILES_TAG);
            } else if (redirectTAG != null && redirectTAG.equals(Constants.SETTINGS_TAG)) {
                changeFragment(new SettingsFragment(), Constants.SETTINGS_TAG);
            } else {
                changeFragment(new CameraFragment(), Constants.CAMERA_TAG);
            }
        } else {
            changeFragment(new CameraFragment(), Constants.CAMERA_TAG);
        }

        setupBillingClient();

        filesTab.setOnClickListener(view -> {
//            designChange(Constants.FILES_TAG);
            changeFragment(new FilesFragment(), Constants.FILES_TAG);
        });

        cameraTab.setOnClickListener(view -> {

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
            if (fragment instanceof CameraFragment) {
                // do something with f
                ((CameraFragment) fragment).clickPicture();

                if (Globalarea.document_image != null) {
                    Log.w("Got the image :", "1");
                } else {
                    Log.w("Null image :", "2");

                }
            }
//            CameraFragment cameraFragment = (CameraFragment) getSupportFragmentManager().findFragmentByTag(Constants.CAMERA_TAG);
//            if (cameraFragment != null && cameraFragment.isVisible()) {
//                // add your code here
//                cameraFragment.clickPicture();
//            }
            else {
//                designChange(Constants.CAMERA_TAG);
                changeFragment(new CameraFragment(), Constants.CAMERA_TAG);
            }

        });

        settingsTab.setOnClickListener(view -> {
//            designChange(Constants.SETTINGS_TAG);
            changeFragment(new SettingsFragment(), Constants.SETTINGS_TAG);
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.setting_tab:
                    changeFragment(new SettingsFragment(), Constants.SETTINGS_TAG);
                    break;

                case R.id.files_tab:
                    changeFragment(new FilesFragment(), Constants.FILES_TAG);
                    break;
            }

            return false;
        });
    }

//    private void designChange(String tag) {
//
//    }

    public void changeFragment(Fragment fragment, String tag) {
//        changeAnimateTab(tag);
        checkForSubscribedUserAds();
        changeTabColor(tag);

        Bundle bundle = new Bundle();
        bundle.putString("Fragment", tag);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_container, fragment, tag).commit();


//        if (tag.equals(Constants.LANDING_TAG) || tag.equals(Constants.ROOT_TAG)) {
//
//            fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment, tag).commit();
//
//        } else {
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment, tag).addToBackStack(tag).commit();
//
//        }
    }

    private void checkForSubscribedUserAds() {
        if (preferences.isProActive()) {
            Log.e("pro active", "yes");
            adView.setVisibility(View.GONE);
        } else {
            Log.e("pro active ", "no");
//            adView.setVisibility(View.VISIBLE);
        }

    }

    private void changeAnimateTab(String TAG) {

        ChangeBounds transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateInterpolator(1.0f));
        transition.setDuration(100);

        TransitionManager.beginDelayedTransition(bottomBaseLayout, transition);

        if (TAG.equals(Constants.FILES_TAG)) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(bottomBaseLayout);
            constraintSet.connect(viewDash.getId(), ConstraintSet.LEFT, filesTab.getId(), ConstraintSet.LEFT);
            constraintSet.connect(viewDash.getId(), ConstraintSet.RIGHT, filesTab.getId(), ConstraintSet.RIGHT);
            constraintSet.applyTo(bottomBaseLayout);
        } else if (TAG.equals(Constants.SETTINGS_TAG)) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(bottomBaseLayout);
            constraintSet.connect(viewDash.getId(), ConstraintSet.LEFT, settingsTab.getId(), ConstraintSet.LEFT);
            constraintSet.connect(viewDash.getId(), ConstraintSet.RIGHT, settingsTab.getId(), ConstraintSet.RIGHT);
            constraintSet.applyTo(bottomBaseLayout);
        } else if (TAG.equals(Constants.CAMERA_TAG)) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(bottomBaseLayout);
            constraintSet.connect(viewDash.getId(), ConstraintSet.LEFT, cameraTab.getId(), ConstraintSet.LEFT);
            constraintSet.connect(viewDash.getId(), ConstraintSet.RIGHT, cameraTab.getId(), ConstraintSet.RIGHT);
            constraintSet.applyTo(bottomBaseLayout);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("OnActivity Result : ", "Activity");

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
                    Intent intent = new Intent(this, CropActivity.class);
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

    private void changeTabColor(String tag) {

        switch (tag) {
            case Constants.FILES_TAG:
                imgFiles.setImageResource(R.drawable.my_files_blue);
                imgCamera.setImageResource(R.drawable.camera_navigation);
                imgSettings.setImageResource(R.drawable.settings);
                tvFiles.setTextColor(getResources().getColor(R.color.primary_color));
                tvSetting.setTextColor(getResources().getColor(R.color.light_gray));
                break;
            case Constants.CAMERA_TAG:
                imgFiles.setImageResource(R.drawable.my_files);
                imgCamera.setImageResource(R.drawable.camera_blue);
                imgSettings.setImageResource(R.drawable.settings);
                tvFiles.setTextColor(getResources().getColor(R.color.light_gray));
                tvSetting.setTextColor(getResources().getColor(R.color.light_gray));
                break;
            case Constants.SETTINGS_TAG:
                imgFiles.setImageResource(R.drawable.my_files);
                imgCamera.setImageResource(R.drawable.camera_navigation);
                imgSettings.setImageResource(R.drawable.settings_blue);
                tvFiles.setTextColor(getResources().getColor(R.color.light_gray));
                tvSetting.setTextColor(getResources().getColor(R.color.primary_color));
                break;
        }

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

    @Override
    protected void onResume() {
        super.onResume();

        forceUpdate();
        checkForSubscribedUserAds();

//        showDriveNote();
//        todo: Uncommnet below line for drive to local sync
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

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (fragment instanceof FilesFragment) {
            // do something with f
            if (((FilesFragment) fragment).isMultipleSelectionEnabled()) {
                ((FilesFragment) fragment).clearSelection();
                return;
            } else {
                changeFragment(new CameraFragment(), Constants.CAMERA_TAG);
            }

        } else if (fragment instanceof SettingsFragment) {
            changeFragment(new CameraFragment(), Constants.CAMERA_TAG);
        } else {
            if (back) {
                super.onBackPressed();
                return;
            }

            this.back = true;
            Toast.makeText(this, getResources().getString(R.string.click_back_exit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                back = false;
                finish();
            }, 100000);
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

    private void setupBillingClient() {

        BillingClient mBillingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

//                    Toast.makeText(NewHomeActivity.this, "Billing response ok", Toast.LENGTH_SHORT).show();
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
//                    Log.i(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
                    Toast.makeText(getApplicationContext(), "Something went wrong, Please try again..", Toast.LENGTH_LONG).show();
                } else {
//                    Log.w(TAG, "onPurchasesUpdated() got unknown resultCode: " + billingResult.getResponseCode());
                    Toast.makeText(getApplicationContext(), "Something went wrong, Please try again.. " + billingResult.getResponseCode(), Toast.LENGTH_LONG).show();

                }
            }
        }).build();

        mBillingClient.startConnection(new BillingClientStateListener() {
//            @Override
//            public void onBillingSetupFinished(@BillingClient.BillingResponseCode int billingResponseCode) {
//                if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
//                    // The billing client is ready. You can query purchases here.
//                }
//            }

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
//                Toast.makeText(NewHomeActivity.this, "billing setup done", Toast.LENGTH_SHORT).show();

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
//                    loadAllSKUs();
//                    Toast.makeText(getApplicationContext(),"here onServiceConnected ",Toast.LENGTH_LONG).show();
                    try {
                        Purchase.PurchasesResult ownedItems = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS);
//                                    .getPurchases(3,getPackageName(), ITEM_TYPE_SUBS, preferences.getPurchaseToken());
                        if (ownedItems.getPurchasesList() != null && ownedItems.getPurchasesList().size() > 0) {
                            purchasedFlow(ownedItems, mBillingClient);


//                      Toast.makeText(getApplicationContext()," 2 here onServiceConnected "+days,Toast.LENGTH_LONG).show();

                        } else {
                            show7DayUpgradeDialog();
                        }
                    } catch (Exception e) {
//                        Toast.makeText(NewHomeActivity.this, "Exception occured"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                } else {
//                    Toast.makeText(getApplicationContext(), "billing result is not okay", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                setupBillingClient();
            }
        });
    }

    private void show7DayUpgradeDialog() {

        long lastTimeStamp = preferences.getUpgradeDialogTimeStamp();

        if (lastTimeStamp == 0 || lastTimeStamp < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)) {
            preferences.setUpgradeDialogTimeStamp(System.currentTimeMillis());
            displayUpgradeDialog();
        }
    }

    private void displayUpgradeDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Upgrade to pro version")
                .setMessage("Join pro version for the unlimited scans and exports, no ads, OCR")
                .setCancelable(false)
                .setPositiveButton("Upgrade",
                        (dialog, id) -> {
                            Intent intent = new Intent(this, ProVersionActivity.class);
                            startActivity(intent);
                        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void purchasedFlow(Purchase.PurchasesResult ownedItems, BillingClient mBillingClient) {
        Purchase purchase = ownedItems.getPurchasesList().get(0);

//                                Toast.makeText(getApplicationContext()," 2 here onServiceConnected "+purchase.getOriginalJson(),Toast.LENGTH_LONG).show();


        long days = purchase.getPurchaseTime() / (60 * 60 * 24 * 1000);
        long currentDate = (System.currentTimeMillis() / (60 * 60 * 24 * 1000));

        if ((currentDate - days) > 365) {
//                                Toast.makeText(getApplicationContext(),"days : "+(currentDate -days),Toast.LENGTH_SHORT).show();
            preferences.setisProActive(false);
            preferences.setPurchaseToken(null);
            if (!preferences.isExpiredDiloag()) {
                preferences.setisExpiredDiloag(true);
                displayErrorDialog(getResources().getString(R.string.expired_sub));
            }

        } else {

            if (purchase.isAutoRenewing()) {
                if (preferences.getPurchaseToken() == null) {
                    acknowledgePurchase(purchase, mBillingClient);
                } else {
//                                    Toast.makeText(NewHomeActivity.this, "the purchase token is not here", Toast.LENGTH_SHORT).show();
                }
            } else {
                preferences.setisProActive(false);
                preferences.setPurchaseToken(null);
                if (!preferences.isExpiredDiloag()) {
                    preferences.setisExpiredDiloag(true);
                    displayErrorDialog(getResources().getString(R.string.expired_sub));
                }
            }
        }
    }

    private void acknowledgePurchase(Purchase purchase, BillingClient mBillingClient) {
//        Toast.makeText(this, "Ackknwodging the purchase", Toast.LENGTH_SHORT).show();
        preferences.setisProActive(true);
        preferences.setPurchaseToken(purchase.getPurchaseToken());
        checkForSubscribedUserAds();
//        Toast.makeText(this,"Result : "+String.valueOf(purchase.getPurchaseState()),Toast.LENGTH_LONG).show();
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            mBillingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
//                    Toast.makeText(getApplicationContext(),"onAcknowledgePurchaseResponse : "+purchase.getPurchaseToken(),Toast.LENGTH_LONG).show();
                    preferences.setPurchaseToken(purchase.getPurchaseToken());
                }
            });
        }

    }

    public void displayErrorDialog(String message) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Renew",
                        (dialog, id) -> {
                            Intent intent = new Intent(this, ProVersionActivity.class);
                            startActivity(intent);
                        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }
}
