package com.pancard.android.utility;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";
    public Camera mCamera;
    protected List<Camera.Size> mPreviewSizeList;
    protected List<Camera.Size> mPictureSizeList;
    protected boolean mSurfaceConfiguring = false;
    protected Camera.Size mPreviewSize;
    protected Camera.Size mPictureSize;
    private SurfaceHolder mHolder;
    private boolean mInProgress;
    private boolean mInFocus;
    private int mCameraId = 0;
    private LayoutMode mLayoutMode;
    private int mCenterPosX = -1;
    private int mCenterPosY;
    private int mSurfaceChangedCallDepth = 0;
    private Camera.PreviewCallback callback;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mLayoutMode = LayoutMode.NoBlank;
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        int cameraId = 0;
        if (Camera.getNumberOfCameras() > cameraId) {
            mCameraId = cameraId;
        } else {
            mCameraId = 0;
        }

        mCamera = Camera.open(mCameraId);
        Camera.Parameters cameraParams = mCamera.getParameters();
        mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
        mPictureSizeList = cameraParams.getSupportedPictureSizes();
    }

    public void setCallback(Camera.PreviewCallback callback) {
        this.callback = callback;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.setPreviewCallback(callback);
            }
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            mSurfaceChangedCallDepth++;
            doSurfaceChanged(width, height);
            mSurfaceChangedCallDepth--;
        }
    }

    private void doSurfaceChanged(int width, int height) {
        mCamera.stopPreview();

        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = true;

        // The code in this if-statement is prevented from executed again when surfaceChanged is
        // called again due to the change of the layout size in this if-statement.
        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            Log.v(TAG, "Desired Preview Size - w: " + width + ", h: " + height);
            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            // Continue executing this method if this method is called recursively.
            // Recursive call of surfaceChanged is very special case, which is a path from
            // the catch clause at the end of this method.
            // The later part of this method should be executed as well in the recursive
            // invocation of this method, because the layout change made in this recursive
            // call will not trigger another invocation of this method.
            if (mSurfaceConfiguring && (mSurfaceChangedCallDepth <= 1)) {
                return;
            }
        }

        configureCameraParameters(cameraParams, portrait);
        mSurfaceConfiguring = false;

        try {
            byte[] previewBuffer = new byte[(mPreviewSize.height * mPreviewSize.width * 3) / 2];
            mCamera.addCallbackBuffer(previewBuffer);
            mCamera.setPreviewCallback(callback);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.w(TAG, "Failed to start preview: " + e.getMessage());

            // Remove failed size
            mPreviewSizeList.remove(mPreviewSize);
            mPreviewSize = null;

            // Reconfigure
            if (mPreviewSizeList.size() > 0) { // prevent infinite loop
                surfaceChanged(null, 0, width, height);
            } else {
                Log.w(TAG, "Gave up starting preview");
            }
        }

    }

    protected Camera.Size determinePreviewSize(boolean portrait, int reqWidth, int reqHeight) {
        // Meaning of width and height is switched for preview when portrait,
        // while it is the same as user's view for surface and metrics.
        // That is, width must always be larger than height for setPreviewSize.
//        int reqPreviewWidth; // requested width in terms of camera hardware
//        int reqPreviewHeight; // requested height in terms of camera hardware
//        if (portrait) {
//            reqPreviewWidth = reqHeight;
//            reqPreviewHeight = reqWidth;
//        } else {
//            reqPreviewWidth = reqWidth;
//            reqPreviewHeight = reqHeight;
//        }
//
//        Log.v(TAG, "Listing all supported preview sizes");
//        for (Camera.Size size : mPreviewSizeList) {
//            Log.v(TAG, "  w: " + size.width + ", h: " + size.height);
//        }
//        Log.v(TAG, "Listing all supported picture sizes");
//        for (Camera.Size size : mPictureSizeList) {
//            Log.v(TAG, "  w: " + size.width + ", h: " + size.height);
//        }
//
//        // Adjust surface size with the closest aspect-ratio
//        float reqRatio = ((float) reqPreviewWidth) / reqPreviewHeight;
//        float curRatio, deltaRatio;
//        float deltaRatioMin = Float.MAX_VALUE;
//        Camera.Size retSize = null;
//        for (Camera.Size size : mPreviewSizeList) {
//            curRatio = ((float) size.width) / size.height;
//            deltaRatio = Math.abs(reqRatio - curRatio);
//            if (deltaRatio < deltaRatioMin) {
//                deltaRatioMin = deltaRatio;
//                retSize = size;
//            }
//        }

        List<Camera.Size> sizeList = mPreviewSizeList;
        Camera.Size bestSize = sizeList.get(0);
        Log.d(TAG, "getSupportedPreviewSizes()  " + bestSize.width + "  " + bestSize.height);

//                bestSize.width = GlobalArea.display_width;
////                bestSize.height = GlobalArea.display_height;
        for (int i = 1; i < sizeList.size(); i++) {

            if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
                Log.d(TAG, "getSupportedPreviewSizes()   " + sizeList.get(i).width + "  " + sizeList.get(i).height);
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }

    protected Camera.Size determinePictureSize(Camera.Size previewSize) {
        Camera.Size retSize = null;
        for (Camera.Size size : mPictureSizeList) {
            if (size.equals(previewSize)) {
                return size;
            }
        }

        Log.v(TAG, "Same picture size not found.");

        // if the preview size is not supported as a picture size
        float reqRatio = ((float) previewSize.width) / previewSize.height;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        for (Camera.Size size : mPictureSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    protected boolean adjustSurfaceLayoutSize(Camera.Size previewSize, boolean portrait,
                                              int availableWidth, int availableHeight) {
        float tmpLayoutHeight, tmpLayoutWidth;
        if (portrait) {
            tmpLayoutHeight = previewSize.width;
            tmpLayoutWidth = previewSize.height;
        } else {
            tmpLayoutHeight = previewSize.height;
            tmpLayoutWidth = previewSize.width;
        }

        float factH, factW, fact;
        factH = availableHeight / tmpLayoutHeight;
        factW = availableWidth / tmpLayoutWidth;
        if (mLayoutMode == LayoutMode.FitToParent) {
            // Select smaller factor, because the surface cannot be set to the size larger than display metrics.
            if (factH < factW) {
                fact = factH;
            } else {
                fact = factW;
            }
        } else {
            if (factH < factW) {
                fact = factW;
            } else {
                fact = factH;
            }
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.getLayoutParams();

        int layoutHeight = (int) (tmpLayoutHeight * fact);
        int layoutWidth = (int) (tmpLayoutWidth * fact);
        Log.v(TAG, "Preview Layout Size - w: " + layoutWidth + ", h: " + layoutHeight);
        Log.v(TAG, "Scale factor: " + fact);

        boolean layoutChanged;
        if ((layoutWidth != this.getWidth()) || (layoutHeight != this.getHeight())) {
            layoutParams.height = layoutHeight;
            layoutParams.width = layoutWidth;
            if (mCenterPosX >= 0) {
                layoutParams.topMargin = mCenterPosY - (layoutHeight / 2);
                layoutParams.leftMargin = mCenterPosX - (layoutWidth / 2);
            }
            this.setLayoutParams(layoutParams); // this will trigger another surfaceChanged invocation.
            layoutChanged = true;
        } else {
            layoutChanged = false;
        }

        return layoutChanged;
    }

    public void setCameraDisplayOrientation(
            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * @param x X coordinate of center position on the screen. Set to negative value to unset.
     * @param y Y coordinate of center position on the screen.
     */
    public void setCenterPosition(int x, int y) {
        mCenterPosX = x;
        mCenterPosY = y;
    }

    protected void configureCameraParameters(Camera.Parameters cameraParams, boolean portrait) {
        // for 2.2 and later
        int angle;
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0: // This is display orientation
                angle = 90; // This is camera orientation
                break;
            case Surface.ROTATION_90:
                angle = 0;
                break;
            case Surface.ROTATION_180:
                angle = 270;
                break;
            case Surface.ROTATION_270:
                angle = 180;
                break;
            default:
                angle = 90;
                break;
        }
        Log.v(TAG, "angle: " + angle);
        mCamera.setDisplayOrientation(angle);

        if (Build.MODEL.equals("Nexus 5X")) {
            // rotate camera 180°
            mCamera.setDisplayOrientation(180);
        }

        setCameraDisplayOrientation(mCameraId, mCamera);
        cameraParams = mCamera.getParameters();

        Camera.Size bestSize;
        Camera.Size picureSize;
        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
        List<Camera.Size> PictureSize = mCamera.getParameters().getSupportedPictureSizes();
        picureSize = PictureSize.get(0);
        bestSize = sizeList.get(0);
        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }

        List<Integer> supportedPreviewFormats = cameraParams.getSupportedPreviewFormats();
        Iterator<Integer> supportedPreviewFormatsIterator = supportedPreviewFormats.iterator();
//        while(supportedPreviewFormatsIterator.hasNext()){
//            Integer previewFormat =supportedPreviewFormatsIterator.next();
//            if (previewFormat == ImageFormat.YV12) {
//                cameraParams.setPreviewFormat(previewFormat);
//            }
//        }

        cameraParams.setPreviewSize(bestSize.width, bestSize.height);

        cameraParams.setPictureSize(picureSize.width, picureSize.height);
//        boolean hasFlash = Scanner.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
//        if (hasFlash) {
//            cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//        }
//        camera.setParameters(param);

//        cameraParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
////        cameraParams.setPictureSize(mPictureSize.width, mPictureSize.height);
        Log.v(TAG, "Preview Actual Size - w: " + bestSize.width + ", h: " + bestSize.height);
        Log.v(TAG, "Picture Actual Size - w: " + picureSize.width + ", h: " + picureSize.height);

        cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO)‌​;
        cameraParams.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        mCamera.setParameters(cameraParams);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    public void stop() {
        if (null == mCamera) {
            return;
        }
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
    }

    public void setCamFocusMode() {

        if (null == mCamera) {
            return;
        }

        /* Set Auto focus */
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        mCamera.setParameters(parameters);
    }

    public static enum LayoutMode {
        FitToParent, // Scale to the size that no side is larger than the parent
        NoBlank // Scale to the size that no side is smaller than the parent
    }
}