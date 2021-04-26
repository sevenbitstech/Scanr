package com.pancard.android.activity.otheracivity.custom;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.images.Size;

import java.io.IOException;

public class CameraSourcePreview extends ViewGroup {
    private static final String TAG = "CameraSourcePreview";
    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;
    private GraphicOverlay mOverlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mStartRequested = false;
        this.mSurfaceAvailable = false;
        this.mSurfaceView = new SurfaceView(context);
        this.mSurfaceView.getHolder().addCallback(new CameraSourcePreview.SurfaceCallback());
        this.addView(this.mSurfaceView);
    }

    @RequiresPermission("android.permission.CAMERA")
    public void start(CameraSource cameraSource) throws IOException, SecurityException {
        if (cameraSource == null) {
            this.stop();
        }

        this.mCameraSource = cameraSource;
        if (this.mCameraSource != null) {
            this.mStartRequested = true;
            this.startIfReady();
        }

    }

    @RequiresPermission("android.permission.CAMERA")
    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException, SecurityException {
        Log.i("starting camera", "yes");
        this.mOverlay = overlay;
        this.start(cameraSource);
    }

    public void stop() {
        if (this.mCameraSource != null) {
            this.mCameraSource.stop();
        }

    }

    public void release() {
        if (this.mCameraSource != null) {
            this.mCameraSource.release();
        }

    }

    @RequiresPermission("android.permission.CAMERA")
    private void startIfReady() throws IOException, SecurityException {
        if (this.mStartRequested && this.mSurfaceAvailable) {
            this.mCameraSource.start(this.mSurfaceView.getHolder());
            if (this.mOverlay != null) {
                Size size = this.mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (this.isPortraitMode()) {
                    this.mOverlay.setCameraInfo(min, max, this.mCameraSource.getCameraFacing());
                } else {
                    this.mOverlay.setCameraInfo(max, min, this.mCameraSource.getCameraFacing());
                }

                this.mOverlay.clear();
            }

            this.mStartRequested = false;
        }

    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int previewWidth = 320;
        int previewHeight = 240;
        if (this.mCameraSource != null) {
            Size size = this.mCameraSource.getPreviewSize();
            if (size != null) {
                previewWidth = size.getWidth();
                previewHeight = size.getHeight();
            }
        }

        int viewWidth;
        if (this.isPortraitMode()) {
            viewWidth = previewWidth;
            previewWidth = previewHeight;
            previewHeight = viewWidth;
        }

        viewWidth = right - left;
        int viewHeight = bottom - top;
        int childXOffset = 0;
        int childYOffset = 0;
        float widthRatio = (float) viewWidth / (float) previewWidth;
        float heightRatio = (float) viewHeight / (float) previewHeight;
        int childWidth;
        int childHeight;
        if (widthRatio > heightRatio) {
            childWidth = viewWidth;
            childHeight = (int) ((float) previewHeight * widthRatio);
            childYOffset = (childHeight - viewHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * heightRatio);
            childHeight = viewHeight;
            childXOffset = (childWidth - viewWidth) / 2;
        }

        for (int i = 0; i < this.getChildCount(); ++i) {
            this.getChildAt(i).layout(-1 * childXOffset, -1 * childYOffset, childWidth - childXOffset, childHeight - childYOffset);
        }

        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            this.startIfReady();
        } catch (IOException var17) {
            Log.e("CameraSourcePreview", "Could not start camera source.", var17);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isPortraitMode() {
        int orientation = this.mContext.getResources().getConfiguration().orientation;
        if (orientation == 2) {
            return false;
        } else if (orientation == 1) {
            return true;
        } else {
            Log.d("CameraSourcePreview", "isPortraitMode returning false by default");
            return false;
        }
    }

    private class SurfaceCallback implements Callback {
        private SurfaceCallback() {
        }

        public void surfaceCreated(SurfaceHolder surface) {
            CameraSourcePreview.this.mSurfaceAvailable = true;

            try {
                CameraSourcePreview.this.startIfReady();
            } catch (SecurityException var3) {
                Log.e("CameraSourcePreview", "Do not have permission to start the camera", var3);
            } catch (IOException var4) {
                Log.e("CameraSourcePreview", "Could not start camera source.", var4);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void surfaceDestroyed(SurfaceHolder surface) {
            CameraSourcePreview.this.mSurfaceAvailable = false;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }
}
