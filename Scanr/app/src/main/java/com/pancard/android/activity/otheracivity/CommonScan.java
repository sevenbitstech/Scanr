package com.pancard.android.activity.otheracivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
import com.google.android.gms.ads.AdView;
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
import com.pancard.android.validation_class.CreditCardVerification;
import com.pancard.android.validation_class.DocumentVerification;
import com.pancard.android.validation_class.DrivingLicenceValidation_1;
import com.pancard.android.validation_class.DrivingLicenceValidation_2;
import com.pancard.android.validation_class.DrivingLicenceValidation_3;
import com.pancard.android.validation_class.PanCardVerification;
import com.pancard.android.validation_class.PassportVerification;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class CommonScan extends Activity {

    public static String SCANNER_TYPE;
    public static String CARD_HOLDER_NAME;
    public static String CARD_UNIQE_NO;
    public static Bitmap CARD_IMAGE;
    public static Bitmap ORIGIANL_CARD_IMAGE;
    public static Bitmap CARD_FRONT_IMAGE;
    public static String CARD_HOLDER_DOB;
    public static String CARD_ISSUE_DATE;
    public static String CARD_TILL_DATE;
    public static String CARD_ISSUE_ADDRESS;
    public static String CARD_BIRTH_PLACE;
    public static String PASSPORT_SURNAME = "";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.v("OpenCV", "init OpenCV");
        }
    }

    AdRequest adRequest;
    int Countdown = 0, EmptyObject = 0, conunt = 0;
    Rect cutRect = null, previousRect = null;
    Button btn_image;
    ImageButton flash;
    TextView tv_message, adharCardWarning;
    CameraPreview cameraPreview;
    DrawView draw_path;
    Handler handler;
    Mat mRgba, detect_Mat, resultMat;
    List<Point> ExtraCutPoint = new ArrayList<>();
    List<Point> defultpoint = new ArrayList<>();
    Bitmap rotatedBitmap;
    Bitmap originalBitmap;
    boolean CHECK_VALID = false;
    boolean cardValidation = false;
    DrivingLicenceValidation_1 drivingLicenceValidation_1;
    DrivingLicenceValidation_2 drivingLicenceValidation_2;
    DrivingLicenceValidation_3 drivingLicenceValidation_3;
    PassportVerification passportValidation;
    DocumentVerification documentValidation;
    AlertDialog alertDialog;
    PermissionManager permissionManager;
    String[] permissions = {Manifest.permission.CAMERA};
    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private boolean DisplayCount = false;
    private boolean doNotCapture = false;
    private boolean ObjectNotDetect = false;
    private ImageView btn_camera;
    private TextView tvDriveNote;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("openCV", "OpenCV loaded successfully");
//                    mOpenCvCameraView.enableView();
//                    mOpenCvCameraView.setOnTouchListener(DocumentDetectionActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
//    private TextView tvDriveNote;

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
                e.printStackTrace();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_camera_permission), Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    public void init() {

        drivingLicenceValidation_1 = new DrivingLicenceValidation_1(CommonScan.this);
        drivingLicenceValidation_2 = new DrivingLicenceValidation_2(CommonScan.this);
        drivingLicenceValidation_3 = new DrivingLicenceValidation_3(CommonScan.this);
        passportValidation = new PassportVerification(CommonScan.this);
        documentValidation = new DocumentVerification(CommonScan.this);
        adView = (AdView) findViewById(R.id.ad_view);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        cameraPreview = findViewById(R.id.cameraPreview);
        btn_camera = findViewById(R.id.btn_camere);
        btn_image = findViewById(R.id.btn_image);
        flash = findViewById(R.id.flash);
        tvDriveNote = findViewById(R.id.tv_note_drive);

        adharCardWarning = findViewById(R.id.adhar_card_warning);
        tv_message = findViewById(R.id.message);
        draw_path = findViewById(R.id.draw_path);
        cameraPreview.setCamFocusMode();
        handler = new Handler();

        final Intent scanType = this.getIntent();

        if (scanType.getStringExtra(SCANNER_TYPE).equals(Constants.businesscard)) {
            btn_camera.setVisibility(View.VISIBLE);
        } else {
            btn_camera.setVisibility(View.GONE);
        }
        if (scanType.getStringExtra(SCANNER_TYPE).equals(Constants.adharcard)) {
            adharCardWarning.setVisibility(View.VISIBLE);
        }
        boolean hasFlash = Scanner.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (hasFlash) {
            flash.setVisibility(View.VISIBLE);
        } else {
            flash.setVisibility(View.GONE);
        }

        btn_camera.setOnClickListener(v -> {

            if (mRgba != null) {
                AutoClickCapture(scanType.getStringExtra(SCANNER_TYPE));
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
        Globalarea.getNoteTextView(tvDriveNote, CommonScan.this);
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
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_camera_permission), Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
//        InitializeMobileAds();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adView != null) {
            adView.destroy();
        }
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


    private void findRectangle(Mat src) throws Exception {
        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        List<Mat> blurredChannel = new ArrayList<Mat>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<Mat>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;

        double maxArea = 0;
        int maxId = -1;

        for (int c = 0; c < 3; c++) {
            int[] ch = {c, 0};
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));

            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    Imgproc.Canny(gray0, gray, 10, 20, 3, true); // true ?
                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1); // 1
                    // ?
                } else {
                    Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                            Imgproc.THRESH_BINARY,
                            (src.width() + src.height()) / 200, t);
                }

                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.02, true);

                    if (approxCurve.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;

                        List<Point> curves = approxCurve.toList();
                        for (int j = 2; j < 5; j++) {

                            double cosine = Math.abs(angle(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3) {
                            maxArea = area;
                            maxId = contours.indexOf(contour);
                        }
                    }
                }
            }
        }

        if (maxId >= 0) {
            Imgproc.drawContours(src, contours, maxId, new Scalar(255, 0, 0,
                    .8), 8);

        }
    }


    private double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    private void findSquaresCamera(Mat image) {

        //todo: you got your mat here.
        List<MatOfPoint> squares = new ArrayList<>();

        int N = 2;
        double ratio = image.size().height / 200;
        int height = Double.valueOf(image.size().height / ratio).intValue();
        int width = Double.valueOf(image.size().width / ratio).intValue();

//        Size size = new Size(100, 140);
        Size size = new Size(width, height);

        Mat originalMat = new Mat(size, CvType.CV_8UC4);
        resultMat = originalMat;
//        resultMat = image;
        Imgproc.resize(image, resultMat, size);
//        Imgproc.medianBlur(resultMat, resultMat, 3);

        Mat smallerImg = new Mat(new Size(resultMat.width() / 2, resultMat.height() / 2), resultMat.type());

        Mat gray = new Mat(resultMat.size(), resultMat.type());

        Mat gray0 = new Mat(resultMat.size(), CvType.CV_8U);

        // down-scale and upscale the image to filter out the noise
        Imgproc.pyrDown(resultMat, smallerImg, smallerImg.size());
        Imgproc.pyrUp(smallerImg, resultMat, resultMat.size());

//      Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGBA2BGRA, 4);
//      Imgproc.GaussianBlur(resultMat, resultMat, new Size(11, 11), 0);
//      Imgproc.Canny(resultMat, resultMat, 50, 200);

        new OpenCVMethod().extractChannel(resultMat, gray, 0);

        // try several threshold levels

        for (int l = 1; l < N; l++) {

            Imgproc.Canny(gray, gray0, 20, 60);
//            Imgproc.threshold(gray, gray0, (l + 1) * 255 / N, 255, Imgproc.THRESH_BINARY);

            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

            Imgproc.findContours(gray0, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            MatOfPoint approx;

            for (int i = 0; i < contours.size(); i++) {

                approx = new OpenCVMethod().approxPolyDP(contours.get(i), Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) * 0.02, true);

                if (approx.toArray().length == 4 &&
                        Math.abs(Imgproc.contourArea(approx)) > 1000) {

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

                    if (maxCosine < 0.3)
                        squares.add(approx);

                    List<Point> Camerapoints = new ArrayList<>();
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

                        Camerapoints.add(cameraPoint);
                    }

                    for (int j = 2; j < 5; j++) {
                        bitmap_rect = new Rect(Camerapoints.get(j % 4), Camerapoints.get(j - 2));
                        if (previousRect == null)
                            previousRect = bitmap_rect;
                    }

                    Log.e("rect_width", String.valueOf(bitmap_rect.width));
                    Log.e("rect_height", String.valueOf(bitmap_rect.height));

                    if (Camerapoints.size() == 4) {

                        if (bitmap_rect.width < cameraPreview.getWidth() - cameraPreview.getWidth() * 0.05
                                && bitmap_rect.height < cameraPreview.getHeight() - cameraPreview.getHeight() * 0.2) {
                            if (bitmap_rect.width > cameraPreview.getWidth() / 2.5 && bitmap_rect.height > cameraPreview.getHeight() / 5) {

                                tv_message.setVisibility(View.GONE);
                                conunt += 1;

                                if (conunt == 5) {
                                    Log.e("Detect 10 frame Capture", "");
//                                    CountDownDispaly();
                                    callValidationCard(originalMat);

                                }
                            } else {

                                reloadActivity();
                                previousRect = null;
                                conunt = 0;
                                EmptyObject = 0;

                                displayTextMessage(getResources().getString(R.string.camera_move_forward));
                            }
                        } else {

                            EmptyObject = 0;
                            if (!DisplayCount) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        draw_path.setPath(null, false);
                                    }
                                });
                            }
                            conunt = 0;
                            reloadActivity();
                            displayTextMessage(getResources().getString(R.string.camera_move_backward));
                        }

                        final Path path = new Path();
                        path.moveTo((float) Camerapoints.get(0).x,
                                (float) Camerapoints.get(0).y);
                        path.lineTo((float) Camerapoints.get(1).x,
                                (float) Camerapoints.get(1).y);
                        path.lineTo((float) Camerapoints.get(2).x,
                                (float) Camerapoints.get(2).y);
                        path.lineTo((float) Camerapoints.get(3).x,
                                (float) Camerapoints.get(3).y);
                        path.lineTo((float) Camerapoints.get(0).x,
                                (float) Camerapoints.get(0).y);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (conunt > 4) {
                                    draw_path.setPath(path, true);
                                } else {
                                    draw_path.setPath(path, false);
                                }
                            }
                        });
                    }
                } else {
                    if (ObjectNotDetect) {
                        EmptyObject += 1;
                    }
                }
            }
        }
    }

    public void callValidationCard(Mat originalMat) {
        if (!cardValidation) {
            cardValidation = true;

            Bitmap originalBitmap = null;
            try {
                originalBitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resultMat, originalBitmap);
                this.originalBitmap = originalBitmap;

                identifyCard();
            } catch (Exception e) {
                e.printStackTrace();
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

        if (doNotCapture) {
            doNotCapture = false;
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


    public void AutoClickCapture(String rtag) {

        if (rtag.contains("licence")) {
            rtag = Constants.licence;
        }
        try {
            if (ExtraCutPoint.size() > 1) {
                cameraPreview.stop();

                Bitmap duplicate_image = new OpenCVMethod().warpDisplayImage(mRgba, detect_Mat, ExtraCutPoint);
                Bitmap originalBitmap = null;
                try {
                    originalBitmap = Bitmap.createBitmap(detect_Mat.cols(), detect_Mat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(detect_Mat, originalBitmap);
                    this.originalBitmap = originalBitmap;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                documentValidation.documentVerification(duplicate_image);
                captureBitmap(rtag, duplicate_image);
            } else {
                final String finalRtag = rtag;
                cameraPreview.mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                        cameraPreview.stop();
                        Bitmap loadedImage = BitmapFactory.decodeByteArray(data, 0,
                                data.length);

                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        originalBitmap = loadedImage;
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(loadedImage, Globalarea.display_height, Globalarea.display_width, true);
                        rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                        documentValidation.documentVerification(rotatedBitmap);
                        captureBitmap(finalRtag, rotatedBitmap);

                    }
                });
            }

        } catch (Exception ex) {
            System.out.println("create bitmap error" + ex.getMessage());
//            displayAlert();

        }
    }

    private void captureBitmap(String whichActivity, Bitmap bitmap) {
        try {

            //todo: you get the cropped image here.
            if (mRgba != null) {

                defultpoint.clear();

                Globalarea.document_image = bitmap;
                Globalarea.original_image = originalBitmap;
//                Log.e("DocumentImage", String.valueOf(bitmap.getAllocationByteCount()));
//                Log.e("OriginalImage", String.valueOf(originalBitmap.getAllocationByteCount()));

                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);

                CARD_IMAGE = bitmap;
                Globalarea.adharCard_back_image = CARD_FRONT_IMAGE;

                Intent intent = new Intent(CommonScan.this, CardScanActivity.class);
                intent.putExtra("TAG_CAMERA", whichActivity);
                startActivity(intent);
                finish();
            }
        } catch (Exception ex) {
            System.out.println("create bitmap error" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        doNotCapture = false;
        ObjectNotDetect = false;
        if (handler != null)
            handler.removeMessages(0);
        DisplayCount = false;
        Countdown = 0;
        Globalarea.documentPageList = null;
//        Intent intent = new Intent(CommonScan.this, BottomBarActivity.class);
        Intent intent = new Intent(CommonScan.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void identifyCard() {
        Intent scanType = this.getIntent();
        //todo: duplicate iamge id the one.
        Bitmap duplicate_image = new OpenCVMethod().warpDisplayImage(mRgba, detect_Mat, ExtraCutPoint);
        if (scanType.getStringExtra(SCANNER_TYPE).equals(Constants.passport)) {
            Bitmap cutBitmap = Bitmap.createBitmap(duplicate_image.getWidth(),
                    duplicate_image.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(cutBitmap);
            android.graphics.Rect desRect = new android.graphics.Rect(400, duplicate_image.getHeight() / 4 - 60, duplicate_image.getWidth(), duplicate_image.getHeight() / 3 + 80);
            canvas.drawBitmap(duplicate_image, desRect, desRect, null);
            Passport(cutBitmap, duplicate_image);

        } else if (scanType.getStringExtra(SCANNER_TYPE).equals("licence_1")) {

            VerifyDrivingLicence(duplicate_image, 1);

        } else if (scanType.getStringExtra(SCANNER_TYPE).equals("licence_2")) {

            VerifyDrivingLicence(duplicate_image, 2);

        } else if (scanType.getStringExtra(SCANNER_TYPE).equals("licence_3")) {
            if (drivingLicenceValidation_3.bo_licence_number && drivingLicenceValidation_3.licence_issues_date) {
                new OpenCVMethod().setFlashOn(false, cameraPreview);
            }
            VerifyDrivingLicence(duplicate_image, 3);

        } else if (scanType.getStringExtra(SCANNER_TYPE).equals(Constants.businesscard)) {
//            mCardScanner.pauseScanning();
            openDialog(duplicate_image);
        } else if (scanType.getStringExtra(SCANNER_TYPE).equals(Constants.adharcard)) {

            AdharCard(duplicate_image);
        } else if (scanType.getStringExtra(SCANNER_TYPE).equals(Constants.pancard)) {
            verifyPanCard(duplicate_image, 1);
        } else if (scanType.getStringExtra(SCANNER_TYPE).equals(Constants.pancard2)) {
            verifyPanCard(duplicate_image, 2);
        }
    }

    private void AdharCard(Bitmap duplicate_image) {
        if (alertDialog != null) {
            if (!alertDialog.isShowing()) {
                if (CARD_FRONT_IMAGE != null && adharCardWarning.getText().toString().equals(Constants.AADHAR_BACK)) {
                    cameraPreview.stop();
                    captureBitmap(Constants.adharcard, duplicate_image);
                }
            }
        } else {
            cameraPreview.mCamera.stopPreview();
            CARD_FRONT_IMAGE = duplicate_image;
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(250);
            displayAlert(Constants.AADHAR_BACK);
        }

    }

    private void creditCard(Bitmap bitmap) {
        CreditCardVerification panCardValidation = new CreditCardVerification(CommonScan.this);
        CHECK_VALID = panCardValidation.PancardVerification(bitmap);
        if (CHECK_VALID) {
            CommonScan.CARD_HOLDER_NAME = CommonScan.CARD_UNIQE_NO;
            cameraPreview.stop();
            captureBitmap(Constants.businesscard, bitmap);
        } else {
            cardValidation = false;
            conunt = 0;
        }
    }

    private void verifyPanCard(Bitmap scaledCard, int type) {

        PanCardVerification pancardValidation = new PanCardVerification(CommonScan.this);

        if (type == 1) {
            CHECK_VALID = pancardValidation.PancardVerification(scaledCard);
        } else if (type == 2) {
            CHECK_VALID = true;
        }

        if (CHECK_VALID) {
            cameraPreview.stop();
            if (type == 1)
                captureBitmap(Constants.pancard, scaledCard);
            if (type == 2)
                captureBitmap(Constants.pancard2, scaledCard);
        } else {
            cardValidation = false;
            conunt = 0;
        }
    }

    private void Passport(Bitmap detectbitmap, Bitmap scaledCard) {
        CHECK_VALID = passportValidation.PassPortVerification(detectbitmap, scaledCard);

        if (CHECK_VALID) {
            cameraPreview.stop();
            captureBitmap(Constants.passport, scaledCard);

        } else {
            cardValidation = false;
            conunt = 0;
        }
    }

    private void VerifyDrivingLicence(Bitmap scaledCard, int i) {

        if (i == 1) {
            CHECK_VALID = drivingLicenceValidation_1.licenceVerification(scaledCard);
        } else if (i == 2) {
            CHECK_VALID = drivingLicenceValidation_2.licenceVerification(scaledCard);
        } else if (i == 3) {
            CHECK_VALID = drivingLicenceValidation_3.licenceVerification(scaledCard);
        }

        if (CHECK_VALID) {
            cameraPreview.stop();
            captureBitmap(Constants.licence, scaledCard);
        } else {
            cardValidation = false;
            conunt = 0;
        }

    }

    public void openDialog(Bitmap bitmap) {

        CHECK_VALID = documentValidation.documentVerification(bitmap);

        if (CHECK_VALID) {
            cameraPreview.stop();
            captureBitmap(Constants.businesscard, bitmap);
        } else {
            cardValidation = false;
            conunt = 0;
        }
    }

    private void displayAlert(final String s) {

        alertDialog = new AlertDialog.Builder(CommonScan.this)
                .setMessage(s)
                .setTitle(getResources().getString(R.string.hint))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.capture),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                cameraPreview.mCamera.startPreview();

                                cardValidation = false;
                                conunt = 0;
                                adharCardWarning.setText(s);

                            }
                        })
//                .setNegativeButton("Continuous", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        cameraPreview.stop();
//                        captureBitmap(Constants.adharcard, duplicat_bitmap);
//                    }
//                })
                .show();
    }
}