package com.pancard.android.activity.otheracivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor.Builder;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.pancard.android.Globalarea;
import com.pancard.android.activity.otheracivity.custom.BarcodeGraphic;
import com.pancard.android.activity.otheracivity.custom.BarcodeGraphicTracker;
import com.pancard.android.activity.otheracivity.custom.BarcodeTrackerFactory;
import com.pancard.android.activity.otheracivity.custom.CameraSource;
import com.pancard.android.activity.otheracivity.custom.CameraSourcePreview;
import com.pancard.android.activity.otheracivity.custom.GraphicOverlay;
import com.pancard.android.activity.otheracivity.custom.MaterialBarcodeScanner;
import com.pancard.android.activity.otheracivity.custom.MaterialBarcodeScannerBuilder;
import com.pancard.android.activity.otheracivity.custom.SoundPoolPlayer;
import com.pancard.android.activity.scanactivity.QRCodeResultActivity;
import com.pancard.android.core.XmlStringParsing;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.FileOpration;
import com.pancard.android.utility.PermissionManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;


public class MaterialBarcodeScannerActivity extends AppCompatActivity {
    public static final int GALLERY_PERMISSION_QR_CODE = 201;
    private static final int RC_HANDLE_GMS = 9001;
    private static final String TAG = "MaterialBarcodeScanner";
    private final static String GALLERY = "gallery";
    private final static String PDF = "pdf";
    final int GALLERY_CODE = 100;
    final int PDF_CODE = 200;
    private final int TIMER_DELAY = 15 * 1000;
    public int currentZoomLevel = 0;
    Handler handler = new Handler();
    Runnable runnable;
    ProgressDialog waitDialog;
    //    public static VerticalSeekBar Globalarea.seekbarZoom;
    CameraSource mCameraSource;
    ImageView img_plus, img_minus, imgBackArrow, imgGallery;
    PermissionManager permissionManager;
    String[] cameraPermission = {Manifest.permission.CAMERA};
    String[] storagePermission = {Manifest.permission.READ_EXTERNAL_STORAGE};
    String apiMessage = "There is an issue in submitting this request for Private QR.";
    String whichCard;
    private MaterialBarcodeScanner mMaterialBarcodeScanner;
    private MaterialBarcodeScannerBuilder mMaterialBarcodeScannerBuilder;
    private BarcodeDetector barcodeDetector;
    private CameraSourcePreview mCameraSourcePreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private SoundPoolPlayer mSoundPoolPlayer;
    private boolean mDetectionConsumed = false;
    private boolean mFlashOn = false;
    private boolean isShowingDialog = false;
    private TextView tvDriveNote;

    public MaterialBarcodeScannerActivity() {
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (this.getWindow() != null) {
            this.getWindow().setFlags(1024, 1024);
        } else {
            Log.e("MaterialBarcodeScanner", "Barcode scanner could not go into fullscreen mode!");
        }

        setContentView(R.layout.barcode_capture);
        waitDialog = new ProgressDialog(this);

        Globalarea.seekbarProgress = 0;
        Globalarea.seekbarZoom = findViewById(R.id.seek_bar);
        img_minus = findViewById(R.id.img_minus);
        img_plus = findViewById(R.id.img_plus);
        permissionManager = new PermissionManager(this);
        imgBackArrow = findViewById(R.id.img_back_arrow1);
        imgGallery = findViewById(R.id.img_gallery);
        tvDriveNote = findViewById(R.id.tv_note_drive);

        getBundle();

        img_minus.setOnClickListener(v -> {
            if (Globalarea.seekbarZoom.getProgress() >= 3) {
                Globalarea.seekbarZoom.setProgress(Globalarea.seekbarZoom.getProgress() - 2);
            } else {
                Globalarea.seekbarZoom.setProgress(0);
            }

        });
        img_plus.setOnClickListener(v -> {
            if (Globalarea.seekbarZoom.getProgress() < Globalarea.maxZoomLevel - 1) {
                Globalarea.seekbarZoom.setProgress(Globalarea.seekbarZoom.getProgress() + 2);
            } else {
                Globalarea.seekbarZoom.setProgress(Globalarea.maxZoomLevel);
            }
        });
        imgBackArrow.setOnClickListener(view -> onBackPressed());
        imgGallery.setOnClickListener(v -> checkPermissionAndOpenStorage(GALLERY));

    }

    private void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            whichCard = bundle.getString("TAG_CAMERA");
//            Log.e("which card", whichCard);
        }

        //todo: handle later on
//        if (whichCard == null)
//            Snackbar.make(container, "Scan QR Code or Barcode..", Snackbar.LENGTH_LONG).show();
//        else if (whichCard.equals(Constants.adharcard))
//            Snackbar.make(container, "Scan QR Code of Aadhaar Card..", Snackbar.LENGTH_LONG).show();
//        else if (whichCard.equals(Constants.pancard2)) {
//            Snackbar.make(container, "Scan QR Code of Pan Card..", Snackbar.LENGTH_LONG).show();
//        }
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, MaterialBarcodeScannerActivity.this);
    }

    private void checkPermissionsAndStartCamera() {
        Log.e("check permisson", "start camera");
        if (permissionManager.hasPermissions(cameraPermission)) {
            Log.e("check permisson", "start camera 1");

            runTimer();
            startScan();
            zoomScanner();

        } else {
            Toast.makeText(MaterialBarcodeScannerActivity.this, "You don't have camera permission", Toast.LENGTH_LONG).show();
            onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        showDriveNote();

        Log.e("on", "resume");
        if (isShowingDialog) {
            return;
        }

        if (this.mCameraSourcePreview != null) {
            startCameraSource();
//            restartCameraActivity();
        }
//        Globalarea.seekbarZoom.setProgress(1);
        checkPermissionsAndStartCamera();

    }

    private void startScan() {
        /**
         * Build a new MaterialBarcodeScanner
         */
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(MaterialBarcodeScannerActivity.this)
                .withEnableAutoFocus(true)
                .withBleepEnabled(true)
                .withBackfacingCamera()
                .withCenterTracker()
                .withText("Scanning...")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
//                        barcodeResult = barcode;
//                        result.setText(barcode.rawValue);
                        if (barcode.rawValue != null) {
                            Log.w("Scan Text : ", barcode.rawValue);

                            validateQRCode(barcode.rawValue);
                        } else {
                            Toast.makeText(MaterialBarcodeScannerActivity.this, "Invalid QR Code!!!", Toast.LENGTH_SHORT).show();

                        }
                    }
                })
                .build();
//        materialBarcodeScanner.startScan();
        onMaterialBarcodeScanner(materialBarcodeScanner);

    }

    private void zoomScanner() {

        Globalarea.seekbarZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @SuppressLint("LongLogTag")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                Log.d(TAG, "progress:" + progress);
                new getData().execute(String.valueOf(progress));
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onStartTrackingTouch");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onStartTrackingTouch");
            }

        });

        if (mCameraSource != null && mCameraSource.mCamera != null) {
            Camera.Parameters params = mCameraSource.mCamera.getParameters();
            Globalarea.seekbarZoom.setMax(params.getMaxZoom());
            Globalarea.maxZoomLevel = params.getMaxZoom();
            if (Globalarea.seekbarZoom.getProgress() > 0)
                Globalarea.seekbarZoom.setProgress(Globalarea.seekbarZoom.getProgress() - 1);

            if (Globalarea.seekbarProgress > 0) {
                Globalarea.seekbarZoom.setProgress(Globalarea.seekbarProgress - 1);
                Globalarea.seekbarProgress = 0;
            }

        }
    }

    @Subscribe(
            sticky = true,
            threadMode = ThreadMode.MAIN
    )
    public void onMaterialBarcodeScanner(MaterialBarcodeScanner materialBarcodeScanner) {
        this.mMaterialBarcodeScanner = materialBarcodeScanner;
        this.mMaterialBarcodeScannerBuilder = this.mMaterialBarcodeScanner.getMaterialBarcodeScannerBuilder();
        this.barcodeDetector = this.mMaterialBarcodeScanner.getMaterialBarcodeScannerBuilder().getBarcodeDetector();
        this.startCameraSource();
        this.setupLayout();

    }

    private void setupLayout() {
        TextView topTextView = this.findViewById(R.id.topText);
//        Assert.assertNotNull(topTextView);
        String topText = this.mMaterialBarcodeScannerBuilder.getText();
        if (!this.mMaterialBarcodeScannerBuilder.getText().equals("")) {
            topTextView.setText(topText);
        }

        this.setupButtons();
        this.setupCenterTracker();
    }

    private void setupCenterTracker() {
        if (this.mMaterialBarcodeScannerBuilder.getScannerMode() == 2) {
            ImageView centerTracker = this.findViewById(R.id.barcode_square);
            centerTracker.setImageResource(this.mMaterialBarcodeScannerBuilder.getTrackerResourceID());
            this.mGraphicOverlay.setVisibility(View.GONE);
        }

    }

    private void updateCenterTrackerForDetectedState() {
        if (this.mMaterialBarcodeScannerBuilder.getScannerMode() == 2) {
            final ImageView centerTracker = this.findViewById(R.id.barcode_square);
            this.runOnUiThread(() -> centerTracker.setImageResource(MaterialBarcodeScannerActivity.this.mMaterialBarcodeScannerBuilder.getTrackerDetectedResourceID()));
        }

    }

    private void setupButtons() {
//        LinearLayout flashOnButton = findViewById(R.id.flashIconButton);
//        final ImageView flashToggleIcon = findViewById(R.id.flashIcon);

//        Assert.assertNotNull(flashOnButton);
//        flashOnButton.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                if (MaterialBarcodeScannerActivity.this.mFlashOn) {
//                    flashToggleIcon.setBackgroundResource(R.drawable.ic_flash_on_white_24dp);
//                    MaterialBarcodeScannerActivity.this.disableTorch();
//                } else {
//                    flashToggleIcon.setBackgroundResource(R.drawable.ic_flash_off_white_24dp);
//                    MaterialBarcodeScannerActivity.this.enableTorch();
//                }
//
//                MaterialBarcodeScannerActivity.this.mFlashOn = MaterialBarcodeScannerActivity.this.mFlashOn ^ true;
//            }
//        });
//        if (this.mMaterialBarcodeScannerBuilder.isFlashEnabledByDefault()) {
//            flashToggleIcon.setBackgroundResource(R.drawable.ic_flash_off_white_24dp);
//        }

    }

    private void startCameraSource() throws SecurityException {
        this.mSoundPoolPlayer = new SoundPoolPlayer(this);
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.getApplicationContext());
        if (code != 0) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, code, 9001);
            dialog.setCancelable(false);
            dialog.show();
        }

        this.mGraphicOverlay = this.findViewById(R.id.graphicOverlay);
        BarcodeGraphicTracker.NewDetectionListener listener = new BarcodeGraphicTracker.NewDetectionListener() {
            public void onNewDetection(Barcode barcode) {
                if (!MaterialBarcodeScannerActivity.this.mDetectionConsumed) {
                    MaterialBarcodeScannerActivity.this.mDetectionConsumed = true;
                    Log.d("MaterialBarcodeScanner", "Barcode detected! - " + barcode.displayValue);
                    EventBus.getDefault().postSticky(barcode);
                    MaterialBarcodeScannerActivity.this.updateCenterTrackerForDetectedState();


                    MaterialBarcodeScannerActivity.this.mGraphicOverlay.postDelayed(() -> {
//                            MaterialBarcodeScannerActivity.this.finish();
                        if (barcode.rawValue != null) {
                            Log.w("Scan Text : ", barcode.rawValue);

                            validateQRCode(barcode.rawValue);
                        } else {
                            Toast.makeText(MaterialBarcodeScannerActivity.this, "Invalid QR Code!!!", Toast.LENGTH_SHORT).show();

                        }
                    }, 50L);
                }

            }
        };
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(this.mGraphicOverlay, listener, this.mMaterialBarcodeScannerBuilder.getTrackerColor());

        if (this.barcodeDetector != null) {
            this.barcodeDetector.setProcessor((new Builder(barcodeFactory)).build());
        }

        mCameraSource = this.mMaterialBarcodeScannerBuilder.getCameraSource();
        if (mCameraSource != null) {
            try {
                this.mCameraSourcePreview = this.findViewById(R.id.preview);
                this.mCameraSourcePreview.start(mCameraSource, this.mGraphicOverlay);
            } catch (IOException var6) {
                Log.e("MaterialBarcodeScanner", "Unable to start camera source.", var6);

                mCameraSource.release();
                mCameraSource = null;
            } catch (Exception e) {
                Log.e("MaterialBarcodeScanner", "Unable to start camera source.");

                mCameraSource.release();
                mCameraSource = null;
            }
        } else {
            Log.e("MaterialBarcodeScanner", "Unable to start camera source. 1");
        }
    }

    private void enableTorch() throws SecurityException {
        this.mMaterialBarcodeScannerBuilder.getCameraSource().setFlashMode("torch");

        try {
            this.mMaterialBarcodeScannerBuilder.getCameraSource().start();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    private void disableTorch() throws SecurityException {
        this.mMaterialBarcodeScannerBuilder.getCameraSource().setFlashMode("off");

        try {
            this.mMaterialBarcodeScannerBuilder.getCameraSource().start();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    protected void onPause() {
        super.onPause();
        if (handler != null)
            handler.removeMessages(0);
        if (this.mCameraSourcePreview != null) {
            this.mCameraSourcePreview.stop();
        }
        Globalarea.seekbarProgress = Globalarea.seekbarZoom.getProgress();
    }


    protected void onDestroy() {
        super.onDestroy();
        if (this.isFinishing()) {
            this.clean();
        }

    }

    private void clean() {
        EventBus.getDefault().removeStickyEvent(MaterialBarcodeScanner.class);
        if (this.mCameraSourcePreview != null) {
            this.mCameraSourcePreview.release();
            this.mCameraSourcePreview = null;
        }

        if (this.mSoundPoolPlayer != null) {
            this.mSoundPoolPlayer.release();
            this.mSoundPoolPlayer = null;
        }

    }

    private void validateQRCode(String text) {
        if (text != null && text.trim().length() > 0) {
            Log.e("qr text", text);

            processRawValue(text);

        } else if (text != null && text.length() > 0) {
            Toast.makeText(MaterialBarcodeScannerActivity.this, "The QR code contains blank space! Please scan another QR Code", Toast.LENGTH_SHORT).show();
//            if (cameraPreview.mCamera != null) {
//                cameraPreview.mCamera.startPreview();
//            }
//            startCameraSource();
            restartCameraActivity();

        } else {
            Toast.makeText(MaterialBarcodeScannerActivity.this, "No valid QR Code found!!!", Toast.LENGTH_SHORT).show();
//            if (cameraPreview.mCamera != null) {
//                cameraPreview.mCamera.startPreview();
//
//            }
//            startCameraSource();
            restartCameraActivity();
        }
    }

    private void processRawValue(final String rawValue) {
//        runOnUiThread(() -> {
//            mCameraSource.stop();
//            Log.e("on", "UI thread");
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

            startActivity(new Intent(MaterialBarcodeScannerActivity.this, QRCodeResultActivity.class));
            finish();
        }
//        });
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
            Toast.makeText(MaterialBarcodeScannerActivity.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
            nextActivityWithError(Constants.pancard2);
        }
    }


    public void nextActivityWithError(String whichCard) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(250);
        // Changed to BottomBarActivity as home activity
//        Intent intent = new Intent(QRCodeScanner.this, BottomBarActivity.class);
        Intent intent = new Intent(MaterialBarcodeScannerActivity.this, HomeActivity.class);
        intent.putExtra(Constants.ErrorOfQRcode, "Information is not valid, Please scan again....");
        intent.putExtra(Constants.WHICH_ERROR, whichCard);
        startActivity(intent);
        this.finish();
    }

    private void OpenScanActivity() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(250);
        Intent intent = new Intent(MaterialBarcodeScannerActivity.this, CommonScan.class);
        intent.putExtra(CommonScan.SCANNER_TYPE, whichCard);
        startActivity(intent);
        this.finish();

    }


//    private void openWebView(String text) {
//
////        if (isWebViewOpened) {
////            return;
////        }
//
////        isWebViewOpened = true;
//
//
//        Globalarea.seekbarProgress = 0;
//        Log.e("opening ", "web view");
////        Intent intent = new Intent(MaterialBarcodeScannerActivity.this, WebViewActivity.class);
////        intent.putExtra(Constants.KEY_LINK, text);
////        startActivity(intent);
////        finish();
//    }


//    private void showProgress() {
//        if (waitDialog != null) {
//            waitDialog.setCancelable(false);
//            waitDialog.setTitle("Alert");
//            waitDialog.setMessage("Please Wait..");
//            waitDialog.show();
//        }
//    }
//
//    private void hideProgress() {
//        if (waitDialog != null) {
//            waitDialog.dismiss();
//        }
//    }

//    public void openCooErrorDialog(String message, String title, boolean isError) {
//
////        if (context == null) {
////            return;
////        }
//
//        if (handler != null)
//            handler.removeMessages(0);
////        if (cameraPreview != null) {
////            if (cameraPreview.mCamera != null)
////                cameraPreview.mCamera.stopPreview();
////        }
//        if (this.mCameraSourcePreview != null) {
//            this.mCameraSourcePreview.stop();
//            if (this.mCameraSource != null)
//                mCameraSource.stop();
//        }
//        VDGCommonDialog errorDialog = new VDGCommonDialog(MaterialBarcodeScannerActivity.this, R.style.Theme_Dialog_Smart_Login);
//        errorDialog.setCancelable(false);
//
//        if (isError) {
//            errorDialog.setTitleText(title, R.color.color_vdg_red);
//        } else {
//            errorDialog.setTitleText(title);
//        }
//
//        errorDialog.setSubTitleText(message)
//                .setPrimaryButton(getString(R.string.ok), v -> {
//                    errorDialog.dismiss();
//                    onBackPressed();
//
//                })
//                .setOnCloseListener(v -> {
//                    errorDialog.dismiss();
//                    onBackPressed();
//                })
//                .show();
//    }

    private void restartCameraActivity() {
        Globalarea.seekbarProgress = Globalarea.seekbarZoom.getProgress();
        finish();
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void checkPermissionAndOpenStorage(String storageType) {

        if (permissionManager.hasPermissions(storagePermission)) {
            switch (storageType) {
                case GALLERY:
                    openGallery();
                    break;

                case PDF:
                    openPdf();
                    break;
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                boolean showRationale = shouldShowRequestPermissionRationale(storagePermission[0]);

                if (!showRationale) {
                    if (handler != null)
                        handler.removeMessages(0);
//                    if (cameraPreview.mCamera != null) {
//                        cameraPreview.mCamera.stopPreview();
//
//                    }

                    isShowingDialog = true;
                    stopCameraAndHandler();

                    permissionManager.openSettingDialog(this,
                            "Please grant the storage permission from your phone's settings",
                            () -> {


                                Log.i("close", "by cancel/close");
                                if (this.mCameraSourcePreview != null) {
                                    startCameraSource();
                                }

//                                if (cameraPreview != null && cameraPreview.mCamera == null) {
//                                    cameraPreview.mCamera = Camera.open();
//
//                                } else if (cameraPreview != null && cameraPreview.mCamera != null) {
//                                    Log.w("Camera cancel : ", "yes 1");
//                                    cameraPreview.mCamera.startPreview();
//                                }

                                zoomScanner();
                            });
                } else {
                    permissionManager.requestPermissions(storagePermission, GALLERY_PERMISSION_QR_CODE);
                }
            }
        }
    }

    private void stopCameraAndHandler() {
        if (handler != null)
            handler.removeMessages(0);
        if (this.mCameraSourcePreview != null) {
            this.mCameraSourcePreview.stop();
        }
    }

    private void openGallery() {
        releaseCameraByOtherApps();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
    }

    private void openPdf() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a PDF File which contains QR code"), PDF_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    private void runTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runnable = this;
                Toast.makeText(MaterialBarcodeScannerActivity.this, "Please Place QR code inside the camera", Toast.LENGTH_SHORT).show();
                handler.postDelayed(runnable, TIMER_DELAY);
            }
        }, TIMER_DELAY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (!permissionManager.hasPermissions(storagePermission)) {
                Toast.makeText(this, "You don't have permission to access Storage", Toast.LENGTH_SHORT).show();
                return;
            }

            if (data != null && data.getData() != null) {

                Uri uri = data.getData();

                FileOpration fileOperation = new FileOpration();
                String mimeType = fileOperation.getMimeType(uri, this);
                Log.e("mime type", mimeType);

                String path = fileOperation.getPath(uri, this);

                if (requestCode == GALLERY_CODE) {

//                    if (mimeType.equals("image/*")) {
                    getQRTextFromImage(path);
//                    } else {
//                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
//                    }
                }
            }
        } else {

            Log.e("Camera cancel : ", "yes");

//            Intent intent = new Intent(this, MaterialBarcodeScannerActivity.class);
//            startActivity(intent);
//            intent.putExtra(Constants.KEY_LOCATION, getIntent().getStringExtra(Constants.KEY_LOCATION));
//            this.overridePendingTransition(0, 0);
//            finish();


//            if (cameraPreview != null && cameraPreview.mCamera != null) {
//                Log.w("Camera cancel : ", "yes 1");
//                cameraPreview.mCamera.startPreview();
//            } else if (cameraPreview != null && cameraPreview.mCamera == null) {
//                Log.w("Camera cancel : ", "yes 2");
////                cameraPreview.mCamera = Camera.open();
//                restartCameraActivity();
//            }
        }
    }

    private void getQRTextFromImage(String path) {
        Bitmap bitmap = getBitmap(path);

        if (bitmap != null) {
            String qrTextImage = readImage(bitmap);
            validateQRCode(qrTextImage);
        } else {
            Toast.makeText(this, "no path found", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmap(String path) {
        if (path != null) {
            Log.e("path", path);
            return BitmapFactory.decodeFile(path);
        }
        return null;
    }

//    private void getQRTextFromPdf(String path) {
//        Log.e("get qr", "from pdf");
//        if (path != null) {
//            File file = new File(path);
//            PdfToBitmap pdfToBitmap = new PdfToBitmap(new PdfToBitmap.OnCompleteListener() {
//                @Override
//                public void onGeneratedBitmaps(List<Bitmap> bitmaps) {
//                    String qrTextPdf = null;
//
//                    for (Bitmap bitmap : bitmaps) {
//                        String result = readImage(bitmap);
//
//                        if (result != null) {
//                            qrTextPdf = result;
//                            break;
//                        }
//                    }
//                    validateQRCode(qrTextPdf);
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Toast.makeText(MaterialBarcodeScannerActivity.this, "Sorry! We could not generate bitmap from the PDF!", Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
//            });
//            pdfToBitmap.execute(file);
//        } else {
//            Toast.makeText(this, "No filepath found", Toast.LENGTH_SHORT).show();
//            Log.e("file ", "No filepath found");
//        }
//    }

//    public String decodeQRImage(Bitmap bMap) {
//
//        String decoded = null;
//
//        if (bMap != null) {
//
//            int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
//            bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(),
//                    bMap.getHeight());
//            LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(),
//                    bMap.getHeight(), intArray);
//            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//
//            Reader reader = new QRCodeReader();
//            try {
//                Result result = reader.decode(bitmap);
//                decoded = result.getText();
//            } catch (NotFoundException | ChecksumException | FormatException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Toast.makeText(this, "no image found", Toast.LENGTH_SHORT).show();
//        }
//
//        return decoded;
//    }

    private String readImage(Bitmap bMap) {
        String scanResult = null;
        try {
            if (bMap != null) {
                BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

                Frame frame = new Frame.Builder().setBitmap(bMap).build();
                SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);


                // Check if at least one barcode was detected
                if (barcodes.size() != 0) {
                    // Display the QR code's message
                    scanResult = barcodes.valueAt(0).displayValue;
                    Log.e("Read from image : ", scanResult);
                } else {
                    Toast.makeText(this, "no qr code found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "no image found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return scanResult;
    }

    private void releaseCameraByOtherApps() {

        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            Log.e(e.getMessage(), "camera is in use by another app");
        } finally {
            if (camera != null) {
                camera.release();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        this.finish();
    }

    @SuppressLint("StaticFieldLeak")
    private class getData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... progress) {
            // perform operation you want with String "progress"
            try {
                // YOur code here in set zoom for pinch zooming, sth like this
                if (mCameraSource != null && mCameraSource.mCamera != null) {
                    if (mCameraSource.mCamera.getParameters().isZoomSupported()) {

                        Camera.Parameters params = mCameraSource.mCamera.getParameters();
                        params.set("mode", "smart-auto");
                        if (params.isSmoothZoomSupported())
                            params.setZoom(Integer.parseInt(progress[0]));
                        else {
                            params.setZoom(Integer.parseInt(progress[0]));
                        }
                        mCameraSource.mCamera.setParameters(params);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String progressResult) {
            // do whatever you want in this thread like
            // textview.setText(progressResult)
            super.onPostExecute(progressResult);
        }
    }
}
