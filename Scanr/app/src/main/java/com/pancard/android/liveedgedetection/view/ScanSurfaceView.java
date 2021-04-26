package com.pancard.android.liveedgedetection.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.pancard.android.Globalarea;
import com.pancard.android.liveedgedetection.ScanConstants;
import com.pancard.android.liveedgedetection.enums.ScanHint;
import com.pancard.android.liveedgedetection.interfaces.IScanner;
import com.pancard.android.liveedgedetection.util.ScanUtils;

import java.io.IOException;
import java.util.List;

/**
 * This class previews the live images from the camera
 */

public class ScanSurfaceView extends FrameLayout implements SurfaceHolder.Callback {
    private static final String TAG = ScanSurfaceView.class.getSimpleName();
    private final Context context;
    public final Camera.ShutterCallback mShutterCallBack = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            if (context != null) {
                AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (null != mAudioManager)
                    mAudioManager.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
            }
        }
    };
    private final IScanner iScanner;
    public Camera camera;
    SurfaceView mSurfaceView;
    //todo: uncomment scanCanvasView to show borders
//    private final ScanCanvasView scanCanvasView;
    private int vWidth = 0;
    private int vHeight = 0;
    private CountDownTimer autoCaptureTimer;
    private int secondsLeft;
    private boolean isAutoCaptureScheduled;
    private Camera.Size previewSize;
    private boolean isCapturing = false;
    public final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();
            if (iScanner != null)
                iScanner.displayHint(ScanHint.NO_MESSAGE);
            clearAndInvalidateCanvas();

            try {
                Bitmap bitmap = ScanUtils.decodeBitmapFromByteArray(data,
                        ScanConstants.HIGHER_SAMPLING_THRESHOLD, ScanConstants.HIGHER_SAMPLING_THRESHOLD);

                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                Globalarea.document_image = bitmap;
                if (iScanner != null)
                    iScanner.onPictureClicked(bitmap);
                postDelayed(() -> isCapturing = false, 3000);
            } catch (Exception e) {
                if (iScanner != null)
                    iScanner.onExceptionHandled(e);
            } catch (OutOfMemoryError outOfMemoryError) {
                if (iScanner != null)
                    iScanner.onOutOfMemory(outOfMemoryError);
            }

        }
    };

    public ScanSurfaceView(Context context, IScanner iScanner) {
        super(context);
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);
        this.context = context;
//        this.scanCanvasView = new ScanCanvasView(context);
//        addView(scanCanvasView);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        this.iScanner = iScanner;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            requestLayout();
            openCamera();
            if (this.camera != null) {
                this.camera.setPreviewDisplay(holder);
                this.camera.setDisplayOrientation(ScanUtils.configureCameraAngle((Activity) context));
            }

            setPreviewCallback();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void clearAndInvalidateCanvas() {
//        scanCanvasView.clear();
        invalidateCanvas();
    }

    public void invalidateCanvas() {
//        scanCanvasView.invalidate();
    }

    private void openCamera() {
        try {
            if (camera == null) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                int defaultCameraId = 0;
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        defaultCameraId = i;
                    }
                }
                camera = Camera.open(defaultCameraId);
                Camera.Parameters cameraParams = camera.getParameters();

//                List<String> flashModes = cameraParams.getSupportedFlashModes();
//                if (null != flashModes && flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
//                    cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//                }

                camera.setParameters(cameraParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
            iScanner.onExceptionHandled(e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("surface", "changed");
        if (camera != null) {
            camera.stopPreview();

            if (vWidth == vHeight) {
                return;
            }
            if (previewSize == null)
                previewSize = ScanUtils.getOptimalPreviewSize(camera, vWidth, vHeight);

            openCamera();

            if (camera == null)
                return;

            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.set("orientation", "portrait");
//                parameters.setRotation(90);

                camera.setDisplayOrientation(ScanUtils.configureCameraAngle((Activity) context));

                parameters.setPreviewSize(previewSize.width, previewSize.height);
                if (parameters.getSupportedFocusModes() != null
                        && parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (parameters.getSupportedFocusModes() != null
                        && parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }

                Camera.Size size = ScanUtils.determinePictureSize(camera, parameters.getPreviewSize());
                List<Camera.Size> allPictureSizes = camera.getParameters().getSupportedPictureSizes();
//        Log.w("picture size : ",allPictureSizes.toString());

                List<Camera.Size> allSizes = parameters.getSupportedPictureSizes();
                Camera.Size size1 = allPictureSizes.get(0); // get top size
                for (int i = 0; i < allSizes.size(); i++) {
                    if (allSizes.get(i).width > size.width)
                        size1 = allSizes.get(i);
                }
                //set max Picture Size
                parameters.setPictureSize(size1.width, size1.height);
//        parameters.setPictureSize(PictureSize.get(0).width, PictureSize.get(0).height);
                parameters.setPictureFormat(ImageFormat.JPEG);

                camera.setParameters(parameters);
                requestLayout();
                setPreviewCallback();

            } catch (RuntimeException e) {
                e.printStackTrace();
                iScanner.onExceptionHandled(e);
//                Toast.makeText(context, "Something went wrong with the camera.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreviewAndFreeCamera();
    }
//
//    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
//        @Override
//        public void onPreviewFrame(byte[] data, Camera camera) {
//            if (null != camera) {
//                try {
//                    Camera.Size pictureSize = camera.getParameters().getPreviewSize();
////                    Log.d(TAG, "onPreviewFrame - received image " + pictureSize.width + "x" + pictureSize.height);
//
//                    Mat yuv = new Mat(new Size(pictureSize.width, pictureSize.height * 1.5), CV_8UC1);
//                    yuv.put(0, 0, data);
//
//                    Mat mat = new Mat(new Size(pictureSize.width, pictureSize.height), CvType.CV_8UC4);
//                    Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2BGR_NV21, 4);
//                    yuv.release();
//
//                    Size originalPreviewSize = mat.size();
//                    int originalPreviewArea = mat.rows() * mat.cols();
//
////                    Quadrilateral largestQuad = ScanUtils.detectLargestQuadrilateral(mat);
//                    clearAndInvalidateCanvas();
//
//                    mat.release();
//
////                    if (null != largestQuad) {
////                        drawLargestRect(largestQuad.contour, largestQuad.points, originalPreviewSize, originalPreviewArea);
////                    } else {
////                        showFindingReceiptHint();
////                    }
//                } catch (Exception e) {
//                    showFindingReceiptHint();
//                }
//            }
//        }
//    };

    private void stopPreviewAndFreeCamera() {
        if (camera != null) {
            // Call stopPreview() to stop updating the preview surface.
            camera.stopPreview();
            camera.setPreviewCallback(null);
            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            camera.release();
            camera = null;
        }
    }

    public void setPreviewCallback() {
        if (this.camera != null) {
            this.camera.startPreview();
//            this.camera.setPreviewCallback(previewCallback);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        vWidth = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        vHeight = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(vWidth, vHeight);
        previewSize = ScanUtils.getOptimalPreviewSize(camera, vWidth, vHeight);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() > 0) {

            try {

                int width = r - l;
                int height = b - t;

                int previewWidth = width;
                int previewHeight = height;

                if (previewSize != null) {
                    previewWidth = previewSize.width;
                    previewHeight = previewSize.height;

                    int displayOrientation = ScanUtils.configureCameraAngle((Activity) context);
                    if (displayOrientation == 90 || displayOrientation == 270) {
                        previewWidth = previewSize.height;
                        previewHeight = previewSize.width;
                    }

//                Log.d(TAG, "previewWidth:" + previewWidth + " previewHeight:" + previewHeight);
                }

                int nW;
                int nH;
                int top;
                int left;

                float scale = 1.0f;

                // Center the child SurfaceView within the parent.
                if (width * previewHeight < height * previewWidth) {
//                Log.d(TAG, "center horizontally");
                    int scaledChildWidth = (int) ((previewWidth * height / previewHeight) * scale);
                    nW = (width + scaledChildWidth) / 2;
                    nH = (int) (height * scale);
                    top = 0;
                    left = (width - scaledChildWidth) / 2;
                } else {
//                Log.d(TAG, "center vertically");
                    int scaledChildHeight = (int) ((previewHeight * width / previewWidth) * scale);
                    nW = (int) (width * scale);
                    nH = (height + scaledChildHeight) / 2;
                    top = (height - scaledChildHeight) / 2;
                    left = 0;
                }
                if (mSurfaceView != null)
                    mSurfaceView.layout(left, top, nW, nH);


                //            scanCanvasView.layout(left, top, nW, nH);

//            Log.d("layout", "left:" + left);
//            Log.d("layout", "top:" + top);
//            Log.d("layout", "right:" + nW);
//            Log.d("layout", "bottom:" + nH);

            } catch (Exception e) {
                e.printStackTrace();
                if (iScanner != null)
                    iScanner.onExceptionHandled(e);
            }

        }
    }
}