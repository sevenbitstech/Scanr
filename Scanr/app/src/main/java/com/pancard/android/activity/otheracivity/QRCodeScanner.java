package com.pancard.android.activity.otheracivity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.docscan.android.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.Snackbar;
import com.pancard.android.Globalarea;
import com.pancard.android.activity.scanactivity.QRCodeResultActivity;
import com.pancard.android.core.XmlStringParsing;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PermissionManager;

import java.io.IOException;

public class QRCodeScanner extends AppCompatActivity {
    private static final int RC_HANDLE_CAMERA_PERM = 11;
    String whichCard;
    SurfaceView surfaceView;
    CameraSource mCameraSource;
    ObjectAnimator animator;
    View scannerLayout;
    View scannerBar;
    ConstraintLayout container;
    BarcodeDetector barcodeDetector;
    PermissionManager permissionManager;
    String[] permissions = {Manifest.permission.CAMERA};
    private TextView tvDriveNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scanner);
        bindViews();
        setAnimation();
        getBundle();
        permissionManager = new PermissionManager(this);
//        if (permissionManager.hasPermissions(permissions))
//            createCameraSource();
//        else
//            requestCameraPermission();
    }

    private void bindViews() {
        container = findViewById(R.id.container);
        surfaceView = findViewById(R.id.camera_view_barcode);
        scannerLayout = findViewById(R.id.scannerLayout);
        scannerBar = findViewById(R.id.scannerBar);

        tvDriveNote = findViewById(R.id.tv_note_drive);
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, QRCodeScanner.this);
    }

    private void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            whichCard = bundle.getString("TAG_CAMERA");
//            Log.e("which card", whichCard);
        }
        if (whichCard == null)
            Snackbar.make(container, "Scan QR Code or Barcode..", Snackbar.LENGTH_LONG).show();
        else if (whichCard.equals(Constants.adharcard))
            Snackbar.make(container, "Scan QR Code of Aadhaar Card..", Snackbar.LENGTH_LONG).show();
        else if (whichCard.equals(Constants.pancard2)) {
            Snackbar.make(container, "Scan QR Code of Pan Card..", Snackbar.LENGTH_LONG).show();
        }
    }

    private void setAnimation() {
        animator = null;

        ViewTreeObserver vto = scannerLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                scannerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float destination = scannerLayout.getY() + scannerLayout.getHeight();

                animator = ObjectAnimator.ofFloat(scannerBar, "translationY",
                        scannerLayout.getY(), destination);

                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(2000);
                animator.start();
            }
        });
    }

//    private boolean checkForPermission() {
//        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//        return rc == PackageManager.PERMISSION_GRANTED;
//    }

    private void requestCameraPermission() {
        if (permissionManager.shouldRequestPermission(this, permissions)) {
            permissionManager.requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);

            View.OnClickListener listener = view -> permissionManager.requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);

            container.setOnClickListener(listener);
            Snackbar.make(container, "request permission?", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, listener).show();

        }

//        Log.w("request permission", "Camera permission is not granted. Requesting permission");
//
//        final String[] permissions = new String[]{Manifest.permission.CAMERA};
//
//        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
//            return;
//        }
//
//        View.OnClickListener listener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ActivityCompat.requestPermissions(QRCodeScanner.this, permissions, RC_HANDLE_CAMERA_PERM);
//            }
//        };
//
//        container.setOnClickListener(listener);
//        Snackbar.make(container, "request permission?", Snackbar.LENGTH_INDEFINITE)
//                .setAction(android.R.string.ok, listener).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == RC_HANDLE_CAMERA_PERM && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("permission", "Camera permission granted - initialize the camera source");
            // permission granted, so create the camera source
            createCameraSource();
        }
    }

    @SuppressLint("InlinedApi")
    private void createCameraSource() {

        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE | Barcode.ISBN).build();

        if (!barcodeDetector.isOperational()) {
            Log.w("barcode detection", "Detector dependencies are not yet available.");

            if (hasLowStorage()) {
                Snackbar.make(container, "No Storage In Device", Snackbar.LENGTH_SHORT).show();
                Log.w("error", "storage has not enough space");
            }
        }

        Log.e("created", "camera source");
        surfaceView.getHolder().addCallback(getSurfaceHolderCallBack());
        barcodeDetector.setProcessor(getBarcodeProcessor());

        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 480)
                .setRequestedFps(15.0f);

        builder.setAutoFocusEnabled(true);
        mCameraSource = builder.build();
    }

    private boolean hasLowStorage() {
        IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
        return registerReceiver(null, lowStorageFilter) != null;
    }

    private SurfaceHolder.Callback getSurfaceHolderCallBack() {
        return new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    if (permissionManager.hasPermissions(permissions))
                        mCameraSource.start(surfaceView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE ERROR", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCameraSource.stop();
            }
        };
    }

    private Detector.Processor<Barcode> getBarcodeProcessor() {

        return new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Log.i("releasing", "barcode processor");

                mCameraSource.release();
                surfaceView.getHolder().removeCallback(getSurfaceHolderCallBack());
                barcodeDetector.release();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barCodes = detections.getDetectedItems();
                if (barCodes.size() != 0) {
                    Log.e("received a detection", barCodes.valueAt(0).rawValue);
//                    release();
                    processRawValue(barCodes.valueAt(0).rawValue);
                }
            }
        };
    }

    private void processRawValue(final String rawValue) {
        runOnUiThread(() -> {
            mCameraSource.stop();
            Log.e("on", "UI thread");
            if (whichCard != null && whichCard.equals(Constants.adharcard)) {
                Globalarea.adharCardText = rawValue;
                handleAadharCardRawValue(rawValue);
            } else if (whichCard != null && whichCard.equals(Constants.pancard2)) {
                Globalarea.panCard2Text = rawValue;
                handlePanCardRawValue(rawValue);
            } else {
                Globalarea.adharCardText = rawValue;
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(250);

                startActivity(new Intent(QRCodeScanner.this, QRCodeResultActivity.class));
                finish();
            }
        });
    }

    private void handleAadharCardRawValue(String rawValue) {
        new XmlStringParsing().processScannedData(rawValue);
        if (CommonScan.CARD_HOLDER_NAME != null && CommonScan.CARD_UNIQE_NO != null && rawValue.trim().length() > 50
                && CommonScan.CARD_HOLDER_NAME.trim().length() > 1 && CommonScan.CARD_UNIQE_NO.trim().length() > 11
        ) {
            OpenScanActivity();
        } else
            nextActivityWithError(Constants.adharcard);
    }

    private void handlePanCardRawValue(String rawValue) {
        parsePanString(rawValue);

        if (CommonScan.CARD_HOLDER_NAME != null && CommonScan.CARD_UNIQE_NO != null && CommonScan.CARD_HOLDER_DOB != null) {
            OpenScanActivity();
        } else {
            Log.e("null in", "qr code scanner");
            nextActivityWithError(Constants.pancard2);
        }
    }

    private void parsePanString(String panText) {
        try {
            String[] panData = panText.split("\n");

            CommonScan.CARD_HOLDER_NAME = panData[0].substring(panData[0].indexOf(":") + 1);
            CommonScan.CARD_HOLDER_DOB = panData[2].substring(panData[2].indexOf(":") + 1);
            CommonScan.CARD_UNIQE_NO = panData[3].substring(panData[3].indexOf(":") + 1);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            Toast.makeText(QRCodeScanner.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
            nextActivityWithError(Constants.pancard2);
        }
    }

    public void nextActivityWithError(String whichCard) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(250);
        // Changed to BottomBarActivity as home activity
//        Intent intent = new Intent(QRCodeScanner.this, BottomBarActivity.class);
        Intent intent = new Intent(QRCodeScanner.this, HomeActivity.class);
        intent.putExtra(Constants.ErrorOfQRcode, "Information is not valid, Please scan again....");
        intent.putExtra(Constants.WHICH_ERROR, whichCard);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (permissionManager.hasPermissions(permissions))
            createCameraSource();
        else {
            Toast.makeText(getApplicationContext(), "the app does not have the permission to access camera", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        showDriveNote();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraSource != null) {
            mCameraSource.stop();
            surfaceView.getHolder().removeCallback(getSurfaceHolderCallBack());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void OpenScanActivity() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(250);
        Intent intent = new Intent(QRCodeScanner.this, CommonScan.class);
        intent.putExtra(CommonScan.SCANNER_TYPE, whichCard);
        startActivity(intent);
        this.finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Changed to BottomBarActivity as home activity
//        Intent intent = new Intent(QRCodeScanner.this, BottomBarActivity.class);
        Intent intent = new Intent(QRCodeScanner.this, HomeActivity.class);
        startActivity(intent);
        this.finish();
    }
}

//नाम / Name : NAME_OF_CARD_HOLDER
//        पिता का नाम / Father’s Name : FATHER'S_NAME_OF_CARD_HOLDER
//        जन्म कि तारीख / Date of Birth : DOB
//        पैन / PAN : PAN_NUMBER