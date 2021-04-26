package com.pancard.android.activity.otheracivity.custom;


import android.app.Activity;
import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.docscan.android.R;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.barcode.BarcodeDetector.Builder;


public class MaterialBarcodeScannerBuilder {
    protected Activity mActivity;
    protected ViewGroup mRootView;
    protected CameraSource mCameraSource;
    protected BarcodeDetector mBarcodeDetector;
    protected boolean mUsed = false;
    protected int mFacing = 0;
    protected boolean mAutoFocusEnabled = false;
    protected MaterialBarcodeScanner.OnResultListener onResultListener;
    protected int mTrackerColor = Color.parseColor("#F44336");
    protected boolean mBleepEnabled = false;
    protected boolean mFlashEnabledByDefault = false;
    protected int mBarcodeFormats = 0;
    protected String mText = "";
    protected int mScannerMode = 1;
    protected int mTrackerResourceID;
    protected int mTrackerDetectedResourceID;

    public MaterialBarcodeScannerBuilder() {
        this.mTrackerResourceID = R.drawable.material_barcode_square_512;
        this.mTrackerDetectedResourceID = R.drawable.material_barcode_square_512_green;
    }

    public MaterialBarcodeScannerBuilder(@NonNull Activity activity) {
        this.mTrackerResourceID = R.drawable.material_barcode_square_512;
        this.mTrackerDetectedResourceID = R.drawable.material_barcode_square_512_green;
        this.mRootView = (ViewGroup) activity.findViewById(16908290);
        this.mActivity = activity;
    }

    public MaterialBarcodeScannerBuilder withResultListener(@NonNull MaterialBarcodeScanner.OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
        return this;
    }

    public MaterialBarcodeScannerBuilder withActivity(@NonNull Activity activity) {
        this.mRootView = (ViewGroup) activity.findViewById(16908290);
        this.mActivity = activity;
        return this;
    }

    public MaterialBarcodeScannerBuilder withBackfacingCamera() {
        this.mFacing = 0;
        return this;
    }

    public MaterialBarcodeScannerBuilder withFrontfacingCamera() {
        this.mFacing = 1;
        return this;
    }

    public MaterialBarcodeScannerBuilder withCameraFacing(int cameraFacing) {
        this.mFacing = cameraFacing;
        return this;
    }

    public MaterialBarcodeScannerBuilder withEnableAutoFocus(boolean enabled) {
        this.mAutoFocusEnabled = enabled;
        return this;
    }

    public MaterialBarcodeScannerBuilder withTrackerColor(int color) {
        this.mTrackerColor = color;
        return this;
    }

    public MaterialBarcodeScannerBuilder withBleepEnabled(boolean enabled) {
        this.mBleepEnabled = enabled;
        return this;
    }

    public MaterialBarcodeScannerBuilder withText(String text) {
        this.mText = text;
        return this;
    }

    public MaterialBarcodeScannerBuilder withFlashLightEnabledByDefault() {
        this.mFlashEnabledByDefault = true;
        return this;
    }

    public MaterialBarcodeScannerBuilder withBarcodeFormats(int barcodeFormats) {
        this.mBarcodeFormats = barcodeFormats;
        return this;
    }

    public MaterialBarcodeScannerBuilder withOnly2DScanning() {
        this.mBarcodeFormats = 1775;
        return this;
    }

    public MaterialBarcodeScannerBuilder withOnly3DScanning() {
        this.mBarcodeFormats = 6416;
        return this;
    }

    public MaterialBarcodeScannerBuilder withOnlyQRCodeScanning() {
        this.mBarcodeFormats = 256;
        return this;
    }

    public MaterialBarcodeScannerBuilder withCenterTracker() {
        this.mScannerMode = 2;
        return this;
    }

    public MaterialBarcodeScannerBuilder withCenterTracker(int trackerResourceId, int detectedTrackerResourceId) {
        this.mScannerMode = 2;
        this.mTrackerResourceID = trackerResourceId;
        this.mTrackerDetectedResourceID = detectedTrackerResourceId;
        return this;
    }

    public MaterialBarcodeScanner build() {
        if (this.mUsed) {
            throw new RuntimeException("You must not reuse a MaterialBarcodeScanner builder");
        } else if (this.mActivity == null) {
            throw new RuntimeException("Please pass an activity to the MaterialBarcodeScannerBuilder");
        } else {
            this.mUsed = true;
            this.buildMobileVisionBarcodeDetector();
            MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScanner(this);
            materialBarcodeScanner.setOnResultListener(this.onResultListener);
            return materialBarcodeScanner;
        }
    }

    private void buildMobileVisionBarcodeDetector() {
        String focusMode = "fixed";
        if (this.mAutoFocusEnabled) {
            focusMode = "continuous-picture";
        }

        this.mBarcodeDetector = (new Builder(this.mActivity)).setBarcodeFormats(this.mBarcodeFormats).build();
        this.mCameraSource = (new CameraSource.Builder(this.mActivity, this.mBarcodeDetector)).setFacing(this.mFacing).setFlashMode(this.mFlashEnabledByDefault ? "torch" : null).setFocusMode(focusMode).build();
    }

    public Activity getActivity() {
        return this.mActivity;
    }

    public BarcodeDetector getBarcodeDetector() {
        return this.mBarcodeDetector;
    }

    public CameraSource getCameraSource() {
        return this.mCameraSource;
    }

    public int getTrackerColor() {
        return this.mTrackerColor;
    }

    public String getText() {
        return this.mText;
    }

    public boolean isBleepEnabled() {
        return this.mBleepEnabled;
    }

    public boolean isFlashEnabledByDefault() {
        return this.mFlashEnabledByDefault;
    }

    public int getTrackerDetectedResourceID() {
        return this.mTrackerDetectedResourceID;
    }

    public int getTrackerResourceID() {
        return this.mTrackerResourceID;
    }

    public int getScannerMode() {
        return this.mScannerMode;
    }

    public void clean() {
        this.mActivity = null;
    }
}
