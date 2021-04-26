package com.pancard.android.newflow.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.docscan.android.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pancard.android.activity.otheracivity.CommonScan;
import com.pancard.android.activity.otheracivity.DocumentScan;
import com.pancard.android.activity.otheracivity.HomeActivity;
import com.pancard.android.activity.otheracivity.QRCodeScanner;
import com.pancard.android.newflow.fragments.ListDocTypeFragment;
import com.pancard.android.utility.Constants;

public class BottomBarActivity extends AppCompatActivity {

    BottomAppBar bottomAppBar;
    boolean isBackPressedOnce = false;
    //    FloatingActionButton fabAdd;
    FloatingActionButton floatingActionButton1;
    FloatingActionButton floatingActionButton2;
    FrameLayout flDocTypeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_bar);

        bindView();
        initialize();

        FragmentManager fragmentManager = getSupportFragmentManager();
        ListDocTypeFragment listDocTypeFragment = new ListDocTypeFragment();

        fragmentManager.beginTransaction().replace(R.id.frameLayout, listDocTypeFragment, "start").commit();

        Log.e("doc listed", "yes");
    }

    private void bindView() {
        flDocTypeList = findViewById(R.id.frameLayout);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        bottomAppBar.replaceMenu(R.menu.bottom_menu);
//        fabAdd = findViewById(R.id.fab1);

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("click navigation", "yes");
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frameLayout, new ListDocTypeFragment(), "start").commit();
            }
        });

        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i("click menu", "yes");
                return true;
            }
        });

        floatingActionButton1 = findViewById(R.id.fab);
        floatingActionButton2 = findViewById(R.id.fab1);
        floatingActionButton1.setOnClickListener(getFabClickListener());
        floatingActionButton2.setOnClickListener(getFabClickListener());
    }

    private View.OnClickListener getFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View pressedView) {
//                Intent intent = new Intent(BottomBarActivity.this);
                registerForContextMenu(pressedView);
                openContextMenu(pressedView);
            }
        };
    }

    private void initialize() {
//        fabAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                moveToOldHome();
//            }
//        });
    }

    private void moveToOldHome() {
        Intent intent = new Intent(BottomBarActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    //fixme: why this is not being used?
    public void changeFragment(Fragment fragment, String tag) {
        Bundle bundle = new Bundle();
        bundle.putString("Fragment", tag);
        fragment.setArguments(bundle);

        //fixme: what is the need for this ?
        if (tag.equals(Constants.HOME_TAG) || tag.equals(Constants.ROOT_TAG)) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment, tag).commit();

        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment, tag).addToBackStack(tag).commit();
        }
    }

    @Override
    public void onBackPressed() {

        if (isBackPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.isBackPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                isBackPressedOnce = false;
                finish();
            }
        }, 100000);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Card Type");
//        menu.add(0, 1, 0, "PanCard");
        menu.add(0, 1, 0, "Barcode Scan");
        menu.add(0, 2, 0, "Passport");
        menu.add(0, 3, 0, "Business Card");
        menu.add(0, 4, 0, "Document ");
        menu.add(0, 5, 0, "Aadhar Card ");
        menu.add(0, 6, 0, "Credit Card ");
        menu.add(0, 7, 0, "Driving Licence ");
        menu.add(0, 8, 0, "Pancard ");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            onQrCodeScanner("");
        } else if (item.getItemId() == 2) {
            onPassportScan();

        } else if (item.getItemId() == 3) {

            onDocumentScan();

        } else if (item.getItemId() == 4) {

            goToDocumentScan();

        } else if (item.getItemId() == 5) {

            onQrCodeScanner(Constants.adharcard);

        } else if (item.getItemId() == 6) {

            onCreditCardScan();

        } else if (item.getItemId() == 7) {
            onFormatLicenseScan_1();
        } else if (item.getItemId() == 8) {
            onQrCodeScanner(Constants.pancard2);
        }
        return true;
    }

    public void onQrCodeScanner(String TAG) {
        Intent intent = new Intent(this, QRCodeScanner.class);
        intent.putExtra("TAG_CAMERA", TAG);
        startActivity(intent);
        finish();
    }

    public void onDocumentScan() {
        navigateOpenCvCameraScreen(Constants.businesscard);
    }

    public void onFormatLicenseScan_1() {
        navigateOpenCvCameraScreen("licence_1");
    }

    public void onPassportScan() {
        navigateOpenCvCameraScreen(Constants.passport);
    }

    private void goToDocumentScan() {
        Intent intent = new Intent(BottomBarActivity.this, DocumentScan.class);
        startActivity(intent);
        finish();
    }

    public void navigateOpenCvCameraScreen(String scanner_type) {
        openCommonScanner(scanner_type);
    }

    public void onCreditCardScan() {

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
                new android.app.AlertDialog.Builder(BottomBarActivity.this)
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

    private void openCommonScanner(String scannerType) {
        Intent intent = new Intent(this, CommonScan.class)
                .putExtra(CommonScan.SCANNER_TYPE, scannerType);
        startActivity(intent);
        finish();
    }
}