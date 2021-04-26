package com.pancard.android.activity.otheracivity.custom;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.pancard.android.Globalarea;

import java.io.IOException;
import java.lang.Thread.State;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class CameraSource {
    @SuppressLint({"InlinedApi"})
    public static final int CAMERA_FACING_BACK = 0;
    @SuppressLint({"InlinedApi"})
    public static final int CAMERA_FACING_FRONT = 1;
    private static final String TAG = "OpenCameraSource";
    private static final int DUMMY_TEXTURE_NAME = 100;
    private static final float ASPECT_RATIO_TOLERANCE = 0.01F;
    private final Object mCameraLock;
    public Camera mCamera;
    private Context mContext;
    private int mFacing;
    private int mRotation;
    private Size mPreviewSize;
    private float mRequestedFps;
    private int mRequestedPreviewWidth;
    private int mRequestedPreviewHeight;
    private String mFocusMode;
    private String mFlashMode;
    private SurfaceView mDummySurfaceView;
    private SurfaceTexture mDummySurfaceTexture;
    private Thread mProcessingThread;
    private FrameProcessingRunnable mFrameProcessor;
    private Map<byte[], ByteBuffer> mBytesToByteBuffer;

    private CameraSource() {
        this.mCameraLock = new Object();
        this.mFacing = 0;
        this.mRequestedFps = 30.0F;
        this.mRequestedPreviewWidth = 1024;
        this.mRequestedPreviewHeight = 768;
        this.mFocusMode = null;
        this.mFlashMode = null;
        this.mBytesToByteBuffer = new HashMap();
    }

    private static int getIdForRequestedCamera(int facing) {
        CameraInfo cameraInfo = new CameraInfo();

        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return i;
            }
        }

        return -1;
    }

    private static CameraSource.SizePair selectSizePair(Camera camera, int desiredWidth, int desiredHeight) {
        List<SizePair> validPreviewSizes = generateValidPreviewSizeList(camera);
        CameraSource.SizePair selectedPair = null;
        int minDiff = 2147483647;
        Iterator var6 = validPreviewSizes.iterator();

        while (var6.hasNext()) {
            CameraSource.SizePair sizePair = (CameraSource.SizePair) var6.next();
            Size size = sizePair.previewSize();
            int diff = Math.abs(size.getWidth() - desiredWidth) + Math.abs(size.getHeight() - desiredHeight);
            if (diff < minDiff) {
                selectedPair = sizePair;
                minDiff = diff;
            }
        }

        return selectedPair;
    }

    private static List<SizePair> generateValidPreviewSizeList(Camera camera) {
        Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        List<SizePair> validPreviewSizes = new ArrayList();
        Iterator var5 = supportedPreviewSizes.iterator();

        while (true) {
            Camera.Size previewSize;
            while (var5.hasNext()) {
                previewSize = (Camera.Size) var5.next();
                float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;
                Iterator var8 = supportedPictureSizes.iterator();

                while (var8.hasNext()) {
                    Camera.Size pictureSize = (Camera.Size) var8.next();
                    float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                    if (Math.abs(previewAspectRatio - pictureAspectRatio) < 0.01F) {
                        validPreviewSizes.add(new CameraSource.SizePair(previewSize, pictureSize));
                        break;
                    }
                }
            }

            if (validPreviewSizes.size() == 0) {
                Log.w("OpenCameraSource", "No preview sizes have a corresponding same-aspect-ratio picture size");
                var5 = supportedPreviewSizes.iterator();

                while (var5.hasNext()) {
                    previewSize = (Camera.Size) var5.next();
                    validPreviewSizes.add(new CameraSource.SizePair(previewSize, (Camera.Size) null));
                }
            }

            return validPreviewSizes;
        }
    }

    public void release() {
        synchronized (this.mCameraLock) {
            this.stop();
            this.mFrameProcessor.release();
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start() throws IOException {
        synchronized (this.mCameraLock) {
            if (this.mCamera != null) {
                return this;
            } else {
                this.mCamera = this.createCamera();
                if (VERSION.SDK_INT >= 11) {
                    this.mDummySurfaceTexture = new SurfaceTexture(100);
                    this.mCamera.setPreviewTexture(this.mDummySurfaceTexture);
                } else {
                    this.mDummySurfaceView = new SurfaceView(this.mContext);
                    this.mCamera.setPreviewDisplay(this.mDummySurfaceView.getHolder());
                }

                this.mCamera.startPreview();
                this.mProcessingThread = new Thread(this.mFrameProcessor);
                this.mFrameProcessor.setActive(true);
                this.mProcessingThread.start();
                return this;
            }
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start(SurfaceHolder surfaceHolder) throws IOException {
        synchronized (this.mCameraLock) {
            if (this.mCamera != null) {
                return this;
            } else {
                this.mCamera = this.createCamera();
                //fixme: mCamera is null and throwing nullpointerexception sometimes
                this.mCamera.setPreviewDisplay(surfaceHolder);
                this.mCamera.startPreview();
                this.mProcessingThread = new Thread(this.mFrameProcessor);
                this.mFrameProcessor.setActive(true);
                this.mProcessingThread.start();
                return this;
            }
        }
    }

    public void stop() {
        synchronized (this.mCameraLock) {
            this.mFrameProcessor.setActive(false);
            if (this.mProcessingThread != null) {
                try {
                    this.mProcessingThread.join();
                } catch (InterruptedException var5) {
                    Log.d("OpenCameraSource", "Frame processing thread interrupted on release.");
                }

                this.mProcessingThread = null;
            }

            this.mBytesToByteBuffer.clear();
            if (this.mCamera != null) {
                this.mCamera.stopPreview();
                this.mCamera.setPreviewCallbackWithBuffer((PreviewCallback) null);

                try {
                    if (VERSION.SDK_INT >= 11) {
                        this.mCamera.setPreviewTexture((SurfaceTexture) null);
                    } else {
                        this.mCamera.setPreviewDisplay((SurfaceHolder) null);
                    }
                } catch (Exception var4) {
                    Log.e("OpenCameraSource", "Failed to clear camera preview: " + var4);
                }

                this.mCamera.release();
                this.mCamera = null;
            }

        }
    }

    public Size getPreviewSize() {
        return this.mPreviewSize;
    }

    public int getCameraFacing() {
        return this.mFacing;
    }

    public int doZoom(float scale) {
        synchronized (this.mCameraLock) {
            if (this.mCamera == null) {
                return 0;
            } else {
                int currentZoom = 0;
                Parameters parameters = this.mCamera.getParameters();
                if (!parameters.isZoomSupported()) {
                    Log.w("OpenCameraSource", "Zoom is not supported on this device");
                    return currentZoom;
                } else {
                    int maxZoom = parameters.getMaxZoom();
                    currentZoom = parameters.getZoom() + 1;
                    float newZoom;
                    if (scale > 1.0F) {
                        newZoom = (float) currentZoom + scale * (float) (maxZoom / 10);
                    } else {
                        newZoom = (float) currentZoom * scale;
                    }

                    currentZoom = Math.round(newZoom) - 1;
                    if (currentZoom < 0) {
                        currentZoom = 0;
                    } else if (currentZoom > maxZoom) {
                        currentZoom = maxZoom;
                    }

                    parameters.setZoom(currentZoom);
                    this.mCamera.setParameters(parameters);
                    return currentZoom;
                }
            }
        }
    }

    public void takePicture(CameraSource.ShutterCallback shutter, CameraSource.PictureCallback jpeg) {
        synchronized (this.mCameraLock) {
            if (this.mCamera != null) {
                CameraSource.PictureStartCallback startCallback = new CameraSource.PictureStartCallback();
                startCallback.mDelegate = shutter;
                CameraSource.PictureDoneCallback doneCallback = new CameraSource.PictureDoneCallback();
                doneCallback.mDelegate = jpeg;
                this.mCamera.takePicture(startCallback, (Camera.PictureCallback) null, (Camera.PictureCallback) null, doneCallback);
            }

        }
    }

    @Nullable
    public String getFocusMode() {
        return this.mFocusMode;
    }

    public boolean setFocusMode(String mode) {
        synchronized (this.mCameraLock) {
            if (this.mCamera != null && mode != null) {
                Parameters parameters = this.mCamera.getParameters();
                if (parameters.getSupportedFocusModes().contains(mode)) {
                    parameters.setFocusMode(mode);
                    this.mCamera.setParameters(parameters);
                    this.mFocusMode = mode;
                    return true;
                }
            }

            return false;
        }
    }

    @Nullable
    public String getFlashMode() {
        return this.mFlashMode;
    }

    public boolean setFlashMode(String mode) {
        synchronized (this.mCameraLock) {
            if (this.mCamera != null && mode != null) {
                Parameters parameters = this.mCamera.getParameters();
                if (parameters.getSupportedFlashModes().contains(mode)) {
                    parameters.setFlashMode(mode);
                    this.mCamera.setParameters(parameters);
                    this.mFlashMode = mode;
                    return true;
                }
            }

            return false;
        }
    }

    public void autoFocus(@Nullable CameraSource.AutoFocusCallback cb) {
        synchronized (this.mCameraLock) {
            if (this.mCamera != null) {
                CameraSource.CameraAutoFocusCallback autoFocusCallback = null;
                if (cb != null) {
                    autoFocusCallback = new CameraSource.CameraAutoFocusCallback();
                    autoFocusCallback.mDelegate = cb;
                }

                this.mCamera.autoFocus(autoFocusCallback);
            }

        }
    }

    public void cancelAutoFocus() {
        synchronized (this.mCameraLock) {
            if (this.mCamera != null) {
                this.mCamera.cancelAutoFocus();
            }

        }
    }

    @TargetApi(16)
    public boolean setAutoFocusMoveCallback(@Nullable CameraSource.AutoFocusMoveCallback cb) {
        if (VERSION.SDK_INT < 16) {
            return false;
        } else {
            synchronized (this.mCameraLock) {
                if (this.mCamera != null) {
                    CameraSource.CameraAutoFocusMoveCallback autoFocusMoveCallback = null;
                    if (cb != null) {
                        autoFocusMoveCallback = new CameraSource.CameraAutoFocusMoveCallback();
                        autoFocusMoveCallback.mDelegate = cb;
                    }

                    this.mCamera.setAutoFocusMoveCallback(autoFocusMoveCallback);
                }

                return true;
            }
        }
    }

    @SuppressLint({"InlinedApi"})
    private Camera createCamera() {
        try {

            int requestedCameraId = getIdForRequestedCamera(this.mFacing);
            if (requestedCameraId == -1) {
                throw new RuntimeException("Could not find requested camera.");
            } else {
                Camera camera = Camera.open(requestedCameraId);
                CameraSource.SizePair sizePair = selectSizePair(camera, this.mRequestedPreviewWidth, this.mRequestedPreviewHeight);
                if (sizePair == null) {
                    throw new RuntimeException("Could not find suitable preview size.");
                } else {
                    Size pictureSize = sizePair.pictureSize();
                    this.mPreviewSize = sizePair.previewSize();
                    int[] previewFpsRange = this.selectPreviewFpsRange(camera, this.mRequestedFps);
                    if (previewFpsRange == null) {
                        throw new RuntimeException("Could not find suitable preview frames per second range.");
                    } else {
                        Parameters parameters = camera.getParameters();
                        if (pictureSize != null) {
                            parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
                        }

                        parameters.setPreviewSize(this.mPreviewSize.getWidth(), this.mPreviewSize.getHeight());
                        parameters.setPreviewFpsRange(previewFpsRange[0], previewFpsRange[1]);
                        parameters.setPreviewFormat(17);
                        this.setRotation(camera, parameters, requestedCameraId);
                        if (this.mFocusMode != null) {
                            if (parameters.getSupportedFocusModes().contains(this.mFocusMode)) {
                                parameters.setFocusMode(this.mFocusMode);
                            } else {
                                Log.i("OpenCameraSource", "Camera focus mode: " + this.mFocusMode + " is not supported on this device.");
                            }
                        }

                        this.mFocusMode = parameters.getFocusMode();
                        if (this.mFlashMode != null) {
                            if (parameters.getSupportedFlashModes().contains(this.mFlashMode)) {
                                parameters.setFlashMode(this.mFlashMode);
                            } else {
                                Log.i("OpenCameraSource", "Camera flash mode: " + this.mFlashMode + " is not supported on this device.");
                            }
                        }

                        this.mFlashMode = parameters.getFlashMode();

                        if (Globalarea.seekbarZoom != null)
                            Globalarea.seekbarZoom.setMax(parameters.getMaxZoom());

                        Globalarea.maxZoomLevel = parameters.getMaxZoom();

                        if (Globalarea.seekbarProgress > 0) {
                            Globalarea.seekbarZoom.setProgress(Globalarea.seekbarProgress - 1);
                            parameters.setZoom(Globalarea.seekbarProgress);
                            Globalarea.seekbarProgress = 0;
                        }
                        camera.setParameters(parameters);
                        camera.setPreviewCallbackWithBuffer(new CameraSource.CameraPreviewCallback());
                        camera.addCallbackBuffer(this.createPreviewBuffer(this.mPreviewSize));
                        camera.addCallbackBuffer(this.createPreviewBuffer(this.mPreviewSize));
                        camera.addCallbackBuffer(this.createPreviewBuffer(this.mPreviewSize));
                        camera.addCallbackBuffer(this.createPreviewBuffer(this.mPreviewSize));
                        return camera;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int[] selectPreviewFpsRange(Camera camera, float desiredPreviewFps) {
        int desiredPreviewFpsScaled = (int) (desiredPreviewFps * 1000.0F);
        int[] selectedFpsRange = null;
        int minDiff = 2147483647;
        List<int[]> previewFpsRangeList = camera.getParameters().getSupportedPreviewFpsRange();
        Iterator var7 = previewFpsRangeList.iterator();

        while (var7.hasNext()) {
            int[] range = (int[]) var7.next();
            int deltaMin = desiredPreviewFpsScaled - range[0];
            int deltaMax = desiredPreviewFpsScaled - range[1];
            int diff = Math.abs(deltaMin) + Math.abs(deltaMax);
            if (diff < minDiff) {
                selectedFpsRange = range;
                minDiff = diff;
            }
        }

        return selectedFpsRange;
    }

    private void setRotation(Camera camera, Parameters parameters, int cameraId) {
        @SuppressLint("WrongConstant") WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
        int degrees = 0;
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 90;
                break;
            case 2:
                degrees = 180;
                break;
            case 3:
                degrees = 270;
                break;
            default:
                Log.e("OpenCameraSource", "Bad rotation value: " + rotation);
        }

        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int angle;
        int displayAngle;
        if (cameraInfo.facing == 1) {
            angle = (cameraInfo.orientation + degrees) % 360;
            displayAngle = 360 - angle;
        } else {
            angle = (cameraInfo.orientation - degrees + 360) % 360;
            displayAngle = angle;
        }

        this.mRotation = angle / 90;
        camera.setDisplayOrientation(displayAngle);
        parameters.setRotation(angle);
    }

    private byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(17);
        long sizeInBits = (long) (previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel);
        int bufferSize = (int) Math.ceil((double) sizeInBits / 8.0D) + 1;
        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (buffer.hasArray() && buffer.array() == byteArray) {
            this.mBytesToByteBuffer.put(byteArray, buffer);
            return byteArray;
        } else {
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }
    }

    public interface AutoFocusMoveCallback {
        void onAutoFocusMoving(boolean var1);
    }

    public interface AutoFocusCallback {
        void onAutoFocus(boolean var1);
    }

    public interface PictureCallback {
        void onPictureTaken(byte[] var1);
    }

    public interface ShutterCallback {
        void onShutter();
    }

    private static class SizePair {
        private Size mPreview;
        private Size mPicture;

        public SizePair(Camera.Size previewSize, Camera.Size pictureSize) {
            this.mPreview = new Size(previewSize.width, previewSize.height);
            if (pictureSize != null) {
                this.mPicture = new Size(pictureSize.width, pictureSize.height);
            }

        }

        public Size previewSize() {
            return this.mPreview;
        }

        public Size pictureSize() {
            return this.mPicture;
        }
    }

    public static class Builder {
        private final Detector<?> mDetector;
        private CameraSource mCameraSource = new CameraSource();

        public Builder(Context context, Detector<?> detector) {
            if (context == null) {
                throw new IllegalArgumentException("No context supplied.");
            } else if (detector == null) {
                throw new IllegalArgumentException("No detector supplied.");
            } else {
                this.mDetector = detector;
                this.mCameraSource.mContext = context;
            }
        }

        public CameraSource.Builder setRequestedFps(float fps) {
            if (fps <= 0.0F) {
                throw new IllegalArgumentException("Invalid fps: " + fps);
            } else {
                this.mCameraSource.mRequestedFps = fps;
                return this;
            }
        }

        public CameraSource.Builder setFocusMode(String mode) {
            this.mCameraSource.mFocusMode = mode;
            return this;
        }

        public CameraSource.Builder setFlashMode(String mode) {
            this.mCameraSource.mFlashMode = mode;
            return this;
        }

        public CameraSource.Builder setRequestedPreviewSize(int width, int height) {
            int MAX = 1000000;
            if (width > 0 && width <= 1000000 && height > 0 && height <= 1000000) {
                this.mCameraSource.mRequestedPreviewWidth = width;
                this.mCameraSource.mRequestedPreviewHeight = height;
                return this;
            } else {
                throw new IllegalArgumentException("Invalid preview size: " + width + "x" + height);
            }
        }

        public CameraSource.Builder setFacing(int facing) {
            if (facing != 0 && facing != 1) {
                throw new IllegalArgumentException("Invalid camera: " + facing);
            } else {
                this.mCameraSource.mFacing = facing;
                return this;
            }
        }

        public CameraSource build() {
            this.mCameraSource.mFrameProcessor = this.mCameraSource.new FrameProcessingRunnable(this.mDetector);
            return this.mCameraSource;
        }
    }

    private class FrameProcessingRunnable implements Runnable {
        private final Object mLock = new Object();
        private Detector<?> mDetector;
        private long mStartTimeMillis = SystemClock.elapsedRealtime();
        private boolean mActive = true;
        private long mPendingTimeMillis;
        private int mPendingFrameId = 0;
        private ByteBuffer mPendingFrameData;

        FrameProcessingRunnable(Detector<?> detector) {
            this.mDetector = detector;
        }

        @SuppressLint({"Assert"})
        void release() {
            assert CameraSource.this.mProcessingThread.getState() == State.TERMINATED;
            if (this.mDetector != null) {
                this.mDetector.release();
                this.mDetector = null;
            }
        }

        void setActive(boolean active) {
            synchronized (this.mLock) {
                this.mActive = active;
                this.mLock.notifyAll();
            }
        }

        void setNextFrame(byte[] data, Camera camera) {
            synchronized (this.mLock) {
                if (this.mPendingFrameData != null) {
                    camera.addCallbackBuffer(this.mPendingFrameData.array());
                    this.mPendingFrameData = null;
                }

                if (!CameraSource.this.mBytesToByteBuffer.containsKey(data)) {
                    Log.d("OpenCameraSource", "Skipping frame.  Could not find ByteBuffer associated with the image data from the camera.");
                } else {
                    this.mPendingTimeMillis = SystemClock.elapsedRealtime() - this.mStartTimeMillis;
                    ++this.mPendingFrameId;
                    this.mPendingFrameData = (ByteBuffer) CameraSource.this.mBytesToByteBuffer.get(data);
                    this.mLock.notifyAll();
                }
            }
        }

        public void run() {
            while (true) {
                Frame outputFrame;
                ByteBuffer data;
                synchronized (this.mLock) {
                    while (this.mActive && this.mPendingFrameData == null) {
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException var13) {
                            Log.d("OpenCameraSource", "Frame processing loop terminated.", var13);
                            return;
                        }
                    }

                    if (!this.mActive) {
                        return;
                    }

                    outputFrame = (new com.google.android.gms.vision.Frame.Builder()).setImageData(this.mPendingFrameData, CameraSource.this.mPreviewSize.getWidth(), CameraSource.this.mPreviewSize.getHeight(), 17).setId(this.mPendingFrameId).setTimestampMillis(this.mPendingTimeMillis).setRotation(CameraSource.this.mRotation).build();
                    data = this.mPendingFrameData;
                    this.mPendingFrameData = null;
                }

                try {
                    this.mDetector.receiveFrame(outputFrame);
                } catch (Throwable var11) {
                    Log.e("OpenCameraSource", "Exception thrown from receiver.", var11);
                } finally {
                    CameraSource.this.mCamera.addCallbackBuffer(data.array());
                }
            }
        }
    }

    private class CameraPreviewCallback implements PreviewCallback {
        private CameraPreviewCallback() {
        }

        public void onPreviewFrame(byte[] data, Camera camera) {
            CameraSource.this.mFrameProcessor.setNextFrame(data, camera);
        }
    }

    @TargetApi(16)
    private class CameraAutoFocusMoveCallback implements Camera.AutoFocusMoveCallback {
        private CameraSource.AutoFocusMoveCallback mDelegate;

        private CameraAutoFocusMoveCallback() {
        }

        public void onAutoFocusMoving(boolean start, Camera camera) {
            if (this.mDelegate != null) {
                this.mDelegate.onAutoFocusMoving(start);
            }

        }
    }

    private class CameraAutoFocusCallback implements Camera.AutoFocusCallback {
        private CameraSource.AutoFocusCallback mDelegate;

        private CameraAutoFocusCallback() {
        }

        public void onAutoFocus(boolean success, Camera camera) {
            if (this.mDelegate != null) {
                this.mDelegate.onAutoFocus(success);
            }

        }
    }

    private class PictureDoneCallback implements Camera.PictureCallback {
        private CameraSource.PictureCallback mDelegate;

        private PictureDoneCallback() {
        }

        public void onPictureTaken(byte[] data, Camera camera) {
            if (this.mDelegate != null) {
                this.mDelegate.onPictureTaken(data);
            }

            synchronized (CameraSource.this.mCameraLock) {
                if (CameraSource.this.mCamera != null) {
                    CameraSource.this.mCamera.startPreview();
                }

            }
        }
    }

    private class PictureStartCallback implements Camera.ShutterCallback {
        private CameraSource.ShutterCallback mDelegate;

        private PictureStartCallback() {
        }

        public void onShutter() {
            if (this.mDelegate != null) {
                this.mDelegate.onShutter();
            }

        }
    }
}
