package com.pancard.android.activity.otheracivity.custom;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.docscan.android.R;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;
import com.pancard.android.activity.otheracivity.MaterialBarcodeScannerActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MaterialBarcodeScanner {
    public static final int RC_HANDLE_CAMERA_PERM = 2;
    public static final int SCANNER_MODE_FREE = 1;
    public static final int SCANNER_MODE_CENTER = 2;
    protected final MaterialBarcodeScannerBuilder mMaterialBarcodeScannerBuilder;
    private FrameLayout mContentView;
    private MaterialBarcodeScanner.OnResultListener onResultListener;

    public MaterialBarcodeScanner(@NonNull MaterialBarcodeScannerBuilder materialBarcodeScannerBuilder) {
        this.mMaterialBarcodeScannerBuilder = materialBarcodeScannerBuilder;
    }

    public void setOnResultListener(MaterialBarcodeScanner.OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    @Subscribe(
            sticky = true,
            threadMode = ThreadMode.MAIN
    )
    public void onBarcodeScannerResult(Barcode barcode) {
        this.onResultListener.onResult(barcode);
        EventBus.getDefault().removeStickyEvent(barcode);
        EventBus.getDefault().unregister(this);
        this.mMaterialBarcodeScannerBuilder.clean();
    }

    public void startScan() {
        EventBus.getDefault().register(this);
        if (this.mMaterialBarcodeScannerBuilder.getActivity() == null) {
            throw new RuntimeException("Could not start scan: Activity reference lost (please rebuild the MaterialBarcodeScanner before calling startScan)");
        } else {
            int mCameraPermission = ActivityCompat.checkSelfPermission(this.mMaterialBarcodeScannerBuilder.getActivity(), "android.permission.CAMERA");
            if (mCameraPermission != 0) {
                this.requestCameraPermission();
            } else {
                EventBus.getDefault().postSticky(this);
                Intent intent = new Intent(this.mMaterialBarcodeScannerBuilder.getActivity(), MaterialBarcodeScannerActivity.class);
                this.mMaterialBarcodeScannerBuilder.getActivity().startActivity(intent);
            }

        }
    }

    private void requestCameraPermission() {
        final String[] mPermissions = new String[]{"android.permission.CAMERA"};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.mMaterialBarcodeScannerBuilder.getActivity(), "android.permission.CAMERA")) {
            ActivityCompat.requestPermissions(this.mMaterialBarcodeScannerBuilder.getActivity(), mPermissions, 2);
        } else {
            OnClickListener listener = new OnClickListener() {
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(MaterialBarcodeScanner.this.mMaterialBarcodeScannerBuilder.getActivity(), mPermissions, 2);
                }
            };
            Snackbar.make(this.mMaterialBarcodeScannerBuilder.mRootView, R.string.permission_camera_rationale, -2).setAction(17039370, listener).show();
        }
    }

    public MaterialBarcodeScannerBuilder getMaterialBarcodeScannerBuilder() {
        return this.mMaterialBarcodeScannerBuilder;
    }

    public interface OnResultListener {
        void onResult(Barcode var1);
    }
}
