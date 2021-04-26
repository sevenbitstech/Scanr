package com.pancard.android.activity.otheracivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.docscan.android.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.scanactivity.CardScanActivity;
import com.pancard.android.core.OpenCVMethod;
import com.pancard.android.utility.CameraPreview;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.DrawView;
import com.pancard.android.utility.PermissionManager;
import com.pancard.android.validation_class.ReadImage;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DocumentScan extends Activity {

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.v("OpenCV", "init OpenCV");
        }
    }

    int Countdown = 0, EmptyObject = 0, count = 0;
    Rect cutRect = null, previousRect = null;
    Button btn_image;
    ImageButton flash;
    TextView tv_message;
    CameraPreview cameraPreview;
    DrawView draw_path;
    Handler handler;
    Mat mRgba, detect_Mat, resultMat;
    List<Point> ExtraCutPoint = new ArrayList<>();
    List<Point> defaultPoint = new ArrayList<>();
    PermissionManager permissionManager;
    String[] permissions = {Manifest.permission.CAMERA};
    private InterstitialAd mInterstitialAd;

    private boolean DisplayCount = false, ClickCaptureButoon = false, dontCapture = false, appOpenFirst = false, ObjectNotDetect = false;
    private ImageView btn_camera;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i("openCV", "OpenCV loaded successfully");
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    private TextView tvDriveNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        permissionManager = new PermissionManager(this);


        if (permissionManager.hasPermissions(permissions)) {
            try {
                setContentView(R.layout.activity_custom_camera);
                init();
            } catch (Exception e) {
                new AlertDialog.Builder(DocumentScan.this)
                        .setMessage(getResources().getString(R.string.error_camera_permission2))
                        .setTitle("Error")
                        .setCancelable(false).setCancelable(false).setNegativeButton("Finish", (dialog, which) -> finish()).show();
            }

            Log.e("In", "DocumentScan");
            boolean hasFlash = Scanner.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if (hasFlash) {
                flash.setVisibility(View.VISIBLE);
            } else {
                flash.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_camera_permission), Toast.LENGTH_SHORT).show();
            onBackPressed();
//            new AlertDialog.Builder(DocumentScan.this)
//                    .setMessage("Document Scanner doesn't have permission to Access camera. You can change app permission.")
//                    .setTitle("Error")
//                    .setCancelable(false).show();
        }


//        int permission = PermissionChecker.checkSelfPermission(DocumentScan.this, Manifest.permission.CAMERA);

//        if (permission == PermissionChecker.PERMISSION_GRANTED) {
//
//        } else {
//
//        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (permissionManager.hasPermissions(permissions)) {
            if (!OpenCVLoader.initDebug()) {
                Log.d("openCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
            } else {
                Log.d("openCV", "OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
            if (cameraPreview.mCamera == null) {
                cameraPreview.mCamera = Camera.open();
                init();
            }
            handler.postDelayed(new Runnable() {
                public void run() {
                    Toast.makeText(DocumentScan.this, getResources().getString(R.string.change_surface_message), Toast.LENGTH_LONG).show();
                    handler.postDelayed(this, 13000);
                }
            }, 10000);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_camera_permission), Toast.LENGTH_SHORT).show();
            onBackPressed();
        }


//        try {
//            int permission = PermissionChecker.checkSelfPermission(DocumentScan.this, Manifest.permission.CAMERA);
//
//            if (permission == PermissionChecker.PERMISSION_GRANTED) {
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        InitializeMobileAds();
    }

    private void InitializeMobileAds() {
        Log.e("ad", "setting up");
        MobileAds.initialize(this);

        // Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id));

        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
//                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                Log.e("on", "ad closed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.e("failed to load int", String.valueOf(i));
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }
        });
    }

    public void init() {

        cameraPreview = findViewById(R.id.cameraPreview);
        btn_camera = findViewById(R.id.btn_camere);
        btn_image = findViewById(R.id.btn_image);
        flash = findViewById(R.id.flash);

        tv_message = findViewById(R.id.message);
        draw_path = findViewById(R.id.draw_path);
        cameraPreview.setCamFocusMode();
        handler = new Handler();
        tvDriveNote = findViewById(R.id.tv_note_drive);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cutRect != null && !DisplayCount) {
                    ClickCaptureButoon = true;
                    AutoClickCapture();
                }
            }
        });

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OpenCVMethod().setFlashOn(!new OpenCVMethod().isFlashOn(cameraPreview), cameraPreview);
            }
        });

        cameraPreview.setCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                try {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    Mat mYuv = new Mat(size.height + size.height / 2, size.width, CvType.CV_8UC1);
                    mYuv.put(0, 0, data);
                    Mat mRGB = new Mat();
                    Imgproc.cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2RGB_NV21, 3);
                    Mat dst = new Mat();
                    Core.flip(mRGB.t(), dst, 1);
                    mRgba = dst;
                    findSquaresCamera(mRgba);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        showDriveNote();
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, DocumentScan.this);
    }

    private void findSquaresCamera(Mat image) {


        //todo: image is original bitmap.
        List<MatOfPoint> squares = new ArrayList<>();

        int N = 8;
        final Mat original_Mat = image;
        double ratio = image.size().height / 200;
        int height = Double.valueOf(image.size().height / ratio).intValue();
        int width = Double.valueOf(image.size().width / ratio).intValue();

        Size size = new Size(width, height);

        resultMat = new Mat(size, CvType.CV_8UC4);
        Imgproc.resize(image, resultMat, size);

        Mat smallerImg = new Mat(new Size(resultMat.width() / 2, resultMat.height() / 2), resultMat.type());

        Mat gray = new Mat(resultMat.size(), resultMat.type());

        Mat gray0 = new Mat(resultMat.size(), CvType.CV_8U);

        // down-scale and upscale the image to filter out the noise
        Imgproc.pyrDown(resultMat, smallerImg, smallerImg.size());
        Imgproc.pyrUp(smallerImg, resultMat, resultMat.size());
        // find squares in every color plane of the image

        new OpenCVMethod().extractChannel(resultMat, gray, 0);

        for (int l = 1; l < N; l++) {
            Imgproc.threshold(gray, gray0, (l + 1) * 255 / N, 255, Imgproc.THRESH_BINARY);

            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(gray0, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            MatOfPoint approx;

            for (int i = 0; i < contours.size(); i++) {
                approx = new OpenCVMethod().approxPolyDP(contours.get(i), Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) * 0.02, true);

                double area = Imgproc.contourArea(approx);

                if (area > 5000) {

                    if (approx.toArray().length == 4 &&
                            Math.abs(Imgproc.contourArea(approx)) > 1000 &&
                            Imgproc.isContourConvex(approx)) {

                        double maxCosine = 0;
                        Rect bitmap_rect = null;
                        for (int j = 2; j < 5; j++) {
                            // find the maximum cosine of the angle between joint edges
                            double cosine = Math.abs(new OpenCVMethod().angle(approx.toArray()[j % 4], approx.toArray()[j - 2], approx.toArray()[j - 1]));
                            maxCosine = Math.max(maxCosine, cosine);
                            bitmap_rect = new Rect(approx.toArray()[j % 4], approx.toArray()[j - 2]);
                            if (previousRect == null)
                                previousRect = bitmap_rect;
                        }

                        if (maxCosine < 1.2)
                            squares.add(approx);

                        List<Point> CameraPoints = new ArrayList<>();
                        List<Point> points = approx.toList();

                        ExtraCutPoint = new OpenCVMethod().calculateCamreraPoint(points, ExtraCutPoint, mRgba, resultMat);

                        for (int j = 2; j < ExtraCutPoint.size() + 1; j++) {
                            cutRect = new Rect(ExtraCutPoint.get(j % ExtraCutPoint.size()), ExtraCutPoint.get(j - 2));
                        }

                        detect_Mat = mRgba.submat(cutRect);

                        for (int k = 0; k < points.size(); k++) {
                            Point point = points.get(k);
                            Point cameraPoint = new Point(
                                    (point.x * cameraPreview.getWidth()) / resultMat.width(),
                                    (point.y * cameraPreview.getHeight()) / resultMat.height());

                            CameraPoints.add(cameraPoint);
                            System.out.println("point of rect : " + k + "  " + points.get(k));
                            System.out.println("point of rect : " + k + "  " + cameraPoint);
                        }

                        for (int j = 2; j < 5; j++) {
                            bitmap_rect = new Rect(CameraPoints.get(j % 4), CameraPoints.get(j - 2));
                            if (previousRect == null)
                                previousRect = bitmap_rect;
                        }

                        Log.e("rect_width", String.valueOf(bitmap_rect.width));
                        Log.e("rect_height", String.valueOf(bitmap_rect.height));

                        if (bitmap_rect.width > Globalarea.display_width / 3 && bitmap_rect.height > Globalarea.display_height / 3) {

                            if (CameraPoints.size() == 4) {

                                if (bitmap_rect.width < cameraPreview.getWidth() - cameraPreview.getWidth() * 0.1 && bitmap_rect.height < cameraPreview.getHeight() - cameraPreview.getHeight() * 0.2) {
                                    if (bitmap_rect.width > cameraPreview.getWidth() / 2.5 && bitmap_rect.height > cameraPreview.getHeight() / 2) {

//                                        displayTextMessage("Document is Valid");

                                        tv_message.setVisibility(View.GONE);

                                        if (previousRect != null) {
                                            if ((bitmap_rect.width > previousRect.width - 25 || bitmap_rect.width < previousRect.width + 25) &&
                                                    (bitmap_rect.height > previousRect.height - 25 || bitmap_rect.height < previousRect.height + 25)) {

                                                previousRect = bitmap_rect;
                                                count += 1;

                                                if (count < 9) {

                                                    if (appOpenFirst) {
                                                        count = 10;
                                                    }
                                                }

                                            } else {
                                                if (!appOpenFirst)
//                                                    reloadActivity();
                                                    EmptyObject = 0;
                                                previousRect = bitmap_rect;
                                                count = 0;
                                            }
                                        }
                                    } else {
                                        Log.e("reloadActivity  1", "");
                                        reloadActivity();
                                        previousRect = null;
                                        count = 0;
                                        EmptyObject = 0;

                                        displayTextMessage("Move Forward");
                                    }
                                } else {
                                    Log.e("reloadActivity  1", "2");
                                    EmptyObject = 0;
                                    if (!DisplayCount) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                draw_path.setPath(null, false);
                                            }
                                        });
                                    }
                                    count = 0;
                                    reloadActivity();
                                    displayTextMessage(getResources().getString(R.string.camera_move_backward));
                                }

                                final Path path = new Path();
                                path.moveTo((float) CameraPoints.get(0).x,
                                        (float) CameraPoints.get(0).y);
                                path.lineTo((float) CameraPoints.get(1).x,
                                        (float) CameraPoints.get(1).y);
                                path.lineTo((float) CameraPoints.get(2).x,
                                        (float) CameraPoints.get(2).y);
                                path.lineTo((float) CameraPoints.get(3).x,
                                        (float) CameraPoints.get(3).y);
                                path.lineTo((float) CameraPoints.get(0).x,
                                        (float) CameraPoints.get(0).y);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (count > 9) {

                                            draw_path.setPath(path, true);
                                            System.out.println("Countdown start :" + count);

                                            if (count == 10 && !DisplayCount && !ClickCaptureButoon) {

                                                DisplayCount = true;
                                                Log.e("Detect 10 frame Capture", "");
                                                CountDownDisplay(original_Mat);
                                            }

                                        } else {

                                            draw_path.setPath(path, false);
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.e("reloadActivity  1", "3");
                            EmptyObject = 0;

                            reloadActivity();
                        }
                    } else {
                        if (ObjectNotDetect) {
                            EmptyObject += 1;
                            if (EmptyObject == 5) {
//                                reloadActivity();
                            }
                            System.out.println("Empty object not detect : " + EmptyObject);
                        }
                    }
                }
            }
        }
    }

    private void displayTextMessage(final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_message.setVisibility(View.VISIBLE);

                tv_message.setText(message);
            }
        });

    }

    private void reloadActivity() {

        if (dontCapture) {
            dontCapture = false;
            ObjectNotDetect = false;

            if (handler != null)
                handler.removeMessages(0);
            DisplayCount = false;

            Countdown = 0;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_image.clearAnimation();
                    btn_image.setVisibility(View.GONE);

                    draw_path.setPath(null, false);
                }
            });
        }
    }

    private void CountDownDisplay(Mat original_Mat) {
        handler.postDelayed(new Runnable() {
            public void run() {

                Countdown += 1;
//                if (btn_image.getVisibility() != View.VISIBLE)
                btn_image.setVisibility(View.VISIBLE);
                if (Countdown == 1) {
                    dontCapture = true;

                    btn_image.setText("3");
                    handler.postDelayed(this, 1000);

                } else if (Countdown == 2) {

                    btn_image.setText("2");
                    handler.postDelayed(this, 1000);

                } else if (Countdown == 3) {

                    btn_image.setText("1");
                    handler.postDelayed(this, 1000);

                } else {
                    ObjectNotDetect = true;
                    btn_image.setText("");
                    btn_image.setBackground(getResources().getDrawable(R.drawable.right));

//                    checkBlur(original_Mat);
                    AutoClickCapture();
                    if (handler != null)
                        handler.removeMessages(0);

                }

            }
        }, 1000);
    }

    public void AutoClickCapture() {
        try {
            captureBitmap();

        } catch (Exception ex) {
            System.out.println("create bitmap error" + ex.getMessage());
//            displayAlert();

        }
    }

    private void captureBitmap() {
        try {
            cameraPreview.stop();
            if (cutRect != null && detect_Mat != null) {
                defaultPoint.clear();

                Globalarea.document_image = new OpenCVMethod().warpDisplayImage(mRgba, detect_Mat, ExtraCutPoint);
                CommonScan.CARD_HOLDER_NAME = ReadImage.createCameraSource(Globalarea.document_image, this);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
//
                Intent intent = new Intent(DocumentScan.this, CardScanActivity.class);
                intent.putExtra("TAG_CAMERA", Constants.document);
                startActivity(intent);
                finish();

            } else {
//                displayAlert();
            }
        } catch (Exception ex) {
            System.out.println("create bitmap error" + ex.getMessage());
//            displayAlert();

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dontCapture = false;
        ObjectNotDetect = false;
        if (handler != null)
            handler.removeMessages(0);
        DisplayCount = false;
        Countdown = 0;
//        Intent intent = new Intent(DocumentScan.this, BottomBarActivity.class);
        Intent intent = new Intent(DocumentScan.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null)
            handler.removeMessages(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null)
            handler.removeMessages(0);
    }
}