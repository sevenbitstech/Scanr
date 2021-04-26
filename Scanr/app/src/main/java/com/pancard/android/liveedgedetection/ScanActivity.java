package com.pancard.android.liveedgedetection;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.core.OpenCVMethod;
import com.pancard.android.liveedgedetection.enums.ScanHint;
import com.pancard.android.liveedgedetection.interfaces.IScanner;
import com.pancard.android.liveedgedetection.util.ScanUtils;
import com.pancard.android.liveedgedetection.view.PolygonPoints;
import com.pancard.android.liveedgedetection.view.PolygonView;
import com.pancard.android.liveedgedetection.view.ProgressDialogFragment;
import com.pancard.android.liveedgedetection.view.ScanSurfaceView;
import com.pancard.android.validation_class.DocumentVerification;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import timber.log.Timber;

import static android.view.View.GONE;

/**
 * This class initiates camera and detects edges on live view
 */
public class ScanActivity extends AppCompatActivity implements IScanner, View.OnClickListener {
    public final static Stack<PolygonPoints> allDraggedPointsStack = new Stack<>();
    private static final String TAG = ScanActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    private static final String mOpenCvLibrary = "opencv_java3";
    private static ProgressDialogFragment progressDialogFragment;

    static {
        System.loadLibrary(mOpenCvLibrary);
    }

    AdRequest adRequest;
    ImageButton flash;
    private ViewGroup containerScan;
    private FrameLayout cameraPreviewLayout;
    private ScanSurfaceView mImageSurfaceView;
    private boolean isPermissionNotGranted;
    private TextView captureHintText;
    private LinearLayout captureHintLayout;
    private AdView adView;
    private PolygonView polygonView;
    private ImageView cropImageView, btn_camere;
    private View cropAcceptBtn;
    private View cropRejectBtn;
    private Bitmap copyBitmap;
    private FrameLayout cropLayout;
    private FrameLayout cropReplicaLayout;
    private InterstitialAd mInterstitialAd;
    private TextView tvDriveNote;
    private DocumentVerification documentVerification;

    /**
     * Resize a given bitmap to scale using the given height
     *
     * @return The resized bitmap
     */
    public static Bitmap getResizedBitmap(Bitmap bitmap, int maxHeight) {
        double ratio = bitmap.getHeight() / (double) maxHeight;
        int width = (int) (bitmap.getWidth() / ratio);
        return Bitmap.createScaledBitmap(bitmap, width, maxHeight, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_activtiy);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        init();
//        InitializeMobileAds();
    }

    private void init() {
        containerScan = findViewById(R.id.container_scan);
        cameraPreviewLayout = findViewById(R.id.camera_preview);
        captureHintLayout = findViewById(R.id.capture_hint_layout);
        captureHintText = findViewById(R.id.capture_hint_text);
        polygonView = findViewById(R.id.polygon_view);
        cropImageView = findViewById(R.id.crop_image_view);
        cropAcceptBtn = findViewById(R.id.crop_accept_btn);
        cropRejectBtn = findViewById(R.id.crop_reject_btn);
        cropLayout = findViewById(R.id.crop_layout);
        cropReplicaLayout = findViewById(R.id.crop_replica_layout);
        btn_camere = findViewById(R.id.btn_camere);
        tvDriveNote = findViewById(R.id.tv_note_drive);
        flash = findViewById(R.id.flash);
        adView = findViewById(R.id.ad_view);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        documentVerification = new DocumentVerification(ScanActivity.this);

        cropAcceptBtn.setOnClickListener(this);
        cropRejectBtn.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(containerScan);
            cropLayout.setVisibility(View.GONE);
            mImageSurfaceView.setPreviewCallback();
            btn_camere.setVisibility(View.VISIBLE);
        });

        btn_camere.setOnClickListener(v -> {
            mImageSurfaceView.camera.takePicture(mImageSurfaceView.mShutterCallBack, null, mImageSurfaceView.pictureCallback);
            btn_camere.setVisibility(GONE);
        });

        boolean hasFlash = Scanner.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (hasFlash) {
            flash.setVisibility(View.VISIBLE);
        } else {
            flash.setVisibility(View.GONE);
        }

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new OpenCVMethod().setFlashOn(!new OpenCVMethod().isFlashOn(mImageSurfaceView), mImageSurfaceView);
            }
        });

        checkCameraPermissions();
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, ScanActivity.this);
    }

    private void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            isPermissionNotGranted = true;
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Enable camera permission from settings", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            if (!isPermissionNotGranted) {
                mImageSurfaceView = new ScanSurfaceView(ScanActivity.this, this);
                cameraPreviewLayout.addView(mImageSurfaceView);
            } else {
                isPermissionNotGranted = false;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                onRequestCamera(grantResults);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        showDriveNote();

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

    private void onRequestCamera(int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(() -> runOnUiThread(() -> {
                mImageSurfaceView = new ScanSurfaceView(ScanActivity.this, ScanActivity.this);
                cameraPreviewLayout.addView(mImageSurfaceView);

            }), 500);

        } else {
            Toast.makeText(this, getString(R.string.camera_activity_permission_denied_toast), Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    @Override
    public void displayHint(ScanHint scanHint) {
        captureHintLayout.setVisibility(GONE);
        switch (scanHint) {
            case MOVE_CLOSER:
                captureHintText.setText(getResources().getString(R.string.move_closer));
                captureHintLayout.setBackground(getResources().getDrawable(R.drawable.hint_red));
                break;
            case MOVE_AWAY:
//                captureHintText.setText(getResources().getString(R.string.move_away));
//                captureHintLayout.setBackground(getResources().getDrawable(R.drawable.hint_red));
                captureHintText.setText(getResources().getString(R.string.hold_still));
                captureHintLayout.setBackground(getResources().getDrawable(R.drawable.hint_green));

                break;
            case ADJUST_ANGLE:
//                captureHintText.setText(getResources().getString(R.string.adjust_angle));
//                captureHintLayout.setBackground(getResources().getDrawable(R.drawable.hint_red));
                captureHintText.setText(getResources().getString(R.string.hold_still));
                captureHintLayout.setBackground(getResources().getDrawable(R.drawable.hint_green));

                break;
            case FIND_RECT:
//                captureHintText.setText(getResources().getString(R.string.finding_rect));
//                captureHintLayout.setBackground(getResources().getDrawable(R.drawable.hint_white));
                captureHintText.setText(getResources().getString(R.string.hold_still));
                captureHintLayout.setBackground(getResources().getDrawable(R.drawable.hint_green));

                break;
            case CAPTURING_IMAGE:
                captureHintText.setText(getResources().getString(R.string.hold_still));
                captureHintLayout.setBackground(getResources().getDrawable(R.drawable.hint_green));
                break;
            case NO_MESSAGE:
                captureHintLayout.setVisibility(GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPictureClicked(final Bitmap bitmap) {
        try {
            //todo: bitmap is original image
            Log.e("Document Captured", "Yes");
            btn_camere.setVisibility(GONE);
            copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//            Globalarea.original_image = bitmap;
//
//            String path = ScanUtils.saveToInternalMemory(bitmap, ScanConstants.IMAGE_DIR,
//                    ScanConstants.IMAGE_NAME, ScanActivity.this, 90)[0];
//
//            if(getIntent().getExtras() != null && getIntent().getExtras().getString("TAG_CAMERA") != null) {
//                Intent intent = new Intent()
//                        .putExtra(ScanConstants.SCANNED_RESULT, path)
//                        .putExtra("TAG_CAMERA", getIntent().getExtras().getString("TAG_CAMERA"));
//                setResult(Activity.RESULT_OK, intent);
//                //bitmap.recycle();
//                System.gc();
//                finish();
//            }


//            int height = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
//            int width = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getWidth();
//
//            copyBitmap = ScanUtils.resizeToScreenContentSize(copyBitmap, width, height);
//            Mat originalMat = new Mat(copyBitmap.getHeight(), copyBitmap.getWidth(), CvType.CV_8UC1);
//            Utils.bitmapToMat(copyBitmap, originalMat);
//            List<PointF> points;
//            ArrayList<PointF> points1FullScreen;
//            Map<Integer, PointF> pointFs = new HashMap<>();
//            Map<Integer, PointF> pointFsFullScreen = new HashMap<>();
//            try {
//                Quadrilateral quad = ScanUtils.detectLargestQuadrilateral(originalMat);
//                if (null != quad) {
//                    double resultArea = Math.abs(Imgproc.contourArea(quad.contour));
//                    double previewArea = originalMat.rows() * originalMat.cols();
//                    Log.e("resultArea : ", String.valueOf(resultArea));
//                    Log.e("previewArea : ", String.valueOf(previewArea));
////                    Log.e("Points : ","1");
//
////                    if (resultArea > previewArea * 0.08) {
//                    Log.e("Points : ", "1");
//                    points = new ArrayList<>();
//                    points.add(new PointF((float) quad.points[0].x, (float) quad.points[0].y));
//                    points.add(new PointF((float) quad.points[1].x, (float) quad.points[1].y));
//                    points.add(new PointF((float) quad.points[3].x, (float) quad.points[3].y));
//                    points.add(new PointF((float) quad.points[2].x, (float) quad.points[2].y));
//
//                    points =findPoints(originalMat);
////                    } else {
////                        Log.e("Points : ","2");
////                        points = ScanUtils.getPolygonDefaultPoints(copyBitmap);
////                    }
//
//                } else {
//                    Log.e("Points : ", "3");
//
//                    points = ScanUtils.getPolygonDefaultPoints(copyBitmap);
//                }
//
//                int index = -1;
//                for (PointF pointF : points) {
//                    pointFs.put(++index, pointF);
//                }
//
//                polygonView.setPoints(pointFs);

            //calculating total area
            //to get the full screen points
//                Display mdisp = getWindowManager().getDefaultDisplay();
//                android.graphics.Point mdispSize = new android.graphics.Point();
//                mdisp.getSize(mdispSize);

//                //todo: use it for full screeen.
//                int maxX = cropReplicaLayout.getWidth();
//                int maxY = cropReplicaLayout.getHeight();
//
//                int halfWidth = (maxX / 2);
//                int halfHeight = (maxY / 2);
//
//                int startX = (int) (cropReplicaLayout.getX());
//                int endX = (int) (cropReplicaLayout.getX() + maxX);
//                int startY = (int) (cropReplicaLayout.getY());
//                int endY = (int) (cropReplicaLayout.getY() + maxY);
//
//                points1FullScreen = new ArrayList<>();
//
////                points1FullScreen.add(new PointF(0,0));
////                points1FullScreen.add(new PointF(maxX,0));
////                points1FullScreen.add(new PointF(0,maxY));
////                points1FullScreen.add(new PointF(maxX,maxY));
//
////                android.graphics.Matrix matrix = cropReplicaLayout.getMatrix();
////                matrix.
//
//
//                points1FullScreen.add(new PointF(startX, startY));
//                points1FullScreen.add(new PointF(endX, startY));
//                points1FullScreen.add(new PointF(startX, endY));
//                points1FullScreen.add(new PointF(endX, endY));
//
//                int fullScreenIndex = -1;
//                for (PointF pointF : points1FullScreen) {
//                    pointFsFullScreen.put(++fullScreenIndex, pointF);
//                }
//
////                polygonView.setPoints(pointFs);
//
//                int padding = (int) getResources().getDimension(R.dimen.scan_padding);
//                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(copyBitmap.getWidth() + 2 * padding, copyBitmap.getHeight() + 2 * padding);
//                layoutParams.gravity = Gravity.CENTER;
//                polygonView.setLayoutParams(layoutParams);
//
//                DisplayMetrics displayMetrics = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//
//                int screenHeight = displayMetrics.heightPixels;
//                int screenWidth = displayMetrics.widthPixels;
//                int screenArea = screenHeight * screenWidth;
//
//                int polyHeight = polygonView.getHeight();
//                int polyWidth = polygonView.getWidth();
//
//                int polyArea = polyHeight * polyWidth;
////                float pointsArea = Math.abs(total);
//                int bitmapArea = copyBitmap.getWidth() * copyBitmap.getHeight();
//                Log.e("bitmap area", String.valueOf(bitmapArea));
//
//
//                if ((polyHeight) * 4 < screenHeight || (polyWidth * 4) < screenWidth
//                        || (polyArea * 16) < screenArea) {
//                    Log.e("yes", "lesss area");
//                    polygonView.setPoints(pointFsFullScreen);
//                } else {
//                    Log.e("poly area", String.valueOf(polyArea));
//                    Log.e("not", "less area");
//                }

//                TransitionManager.beginDelayedTransition(containerScan);
//                cropLayout.setVisibility(View.VISIBLE);
//
//                cropImageView.setImageBitmap(copyBitmap);
//                cropImageView.setScaleType(ImageView.ScaleType.FIT_XY);
//            } catch (Exception e) {
//                Log.e(TAG, e.getMessage(), e);
//            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onExceptionHandled(Exception e) {

    }

    @Override
    public void onOutOfMemory(OutOfMemoryError outOfMemoryError) {

    }

    private synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    private synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }

    @Override
    public void onClick(View view) {
        Log.e("On the ", "on click method");
        List<PointF> points = polygonView.getPointsShape(cropImageView);

        Bitmap croppedBitmap;

        if (ScanUtils.isScanPointsValid(points)) {
            Point point1 = new Point(points.get(0).x, points.get(0).y);
            Point point2 = new Point(points.get(1).x, points.get(1).y);
            Point point3 = new Point(points.get(2).x, points.get(2).y);
            Point point4 = new Point(points.get(3).x, points.get(3).y);
            croppedBitmap = ScanUtils.enhanceReceipt(copyBitmap, point1, point2, point3, point4);
        } else {
            croppedBitmap = copyBitmap;
        }

        String path = ScanUtils.saveToInternalMemory(croppedBitmap, ScanConstants.IMAGE_DIR,
                ScanConstants.IMAGE_NAME, ScanActivity.this, 90)[0];

        if (getIntent().getExtras() != null && getIntent().getExtras().getString("TAG_CAMERA") != null) {
            Intent intent = new Intent()
                    .putExtra(ScanConstants.SCANNED_RESULT, path)
                    .putExtra("TAG_CAMERA", getIntent().getExtras().getString("TAG_CAMERA"));
            setResult(Activity.RESULT_OK, intent);
            //bitmap.recycle();
            System.gc();
            finish();
        } else {
            Log.e("tag camera", "null");
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adView != null) {
            adView.destroy();
        }

    }

    private List<PointF> findPoints(Mat orig) {
        List<PointF> result = null;

        Mat image = new Mat();
//        Mat orig = new Mat();
//        org.opencv.android.Utils.bitmapToMat(mBitmap, image);
//        org.opencv.android.Utils.bitmapToMat(mBitmap, orig);

        Mat edges = edgeDetection(image);
        MatOfPoint2f largest = findLargestContour(edges);

        if (largest != null) {
            Point[] points = sortPoints(largest.toArray());
            result = new ArrayList<>();
            result.add(new PointF(Double.valueOf(points[0].x).floatValue(), Double.valueOf(points[0].y).floatValue()));
            result.add(new PointF(Double.valueOf(points[1].x).floatValue(), Double.valueOf(points[1].y).floatValue()));
            result.add(new PointF(Double.valueOf(points[2].x).floatValue(), Double.valueOf(points[2].y).floatValue()));
            result.add(new PointF(Double.valueOf(points[3].x).floatValue(), Double.valueOf(points[3].y).floatValue()));
            largest.release();
        } else {
            Timber.d("Can't find rectangle!");
        }

        edges.release();
        image.release();
        orig.release();

        return result;
    }

    /**
     * Detect the edges in the given Mat
     *
     * @param src A valid Mat object
     * @return A Mat processed to find edges
     */
    private Mat edgeDetection(Mat src) {
        Mat edges = new Mat();
        Imgproc.cvtColor(src, edges, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(edges, edges, new Size(5, 5), 0);
        Imgproc.Canny(edges, edges, 75, 200);
        return edges;
    }

    /**
     * Find the largest 4 point contour in the given Mat.
     *
     * @param src A valid Mat
     * @return The largest contour as a Mat
     */
    private MatOfPoint2f findLargestContour(Mat src) {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Get the 5 largest contours
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                double area1 = Imgproc.contourArea(o1);
                double area2 = Imgproc.contourArea(o2);
                return (int) (area2 - area1);
            }
        });
        if (contours.size() > 5) contours.subList(4, contours.size() - 1).clear();

        MatOfPoint2f largest = null;
        for (MatOfPoint contour : contours) {
            MatOfPoint2f approx = new MatOfPoint2f();
            MatOfPoint2f c = new MatOfPoint2f();
            contour.convertTo(c, CvType.CV_32FC2);
            Imgproc.approxPolyDP(c, approx, Imgproc.arcLength(c, true) * 0.02, true);

            if (approx.total() == 4 && Imgproc.contourArea(contour) > 150) {
                // the contour has 4 points, it's valid
                largest = approx;
                break;
            }
        }

        return largest;
    }

    /**
     * Apply a threshold to give the "scanned" look
     * <p>
     * NOTE:
     * See the following link for more info http://docs.opencv.org/3.1.0/d7/d4d/tutorial_py_thresholding.html#gsc.tab=0
     *
     * @param src A valid Mat
     * @return The processed Bitmap
     */
    private Bitmap applyThreshold(Mat src) {
//        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);

        // Some other approaches
//        Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15);
//        Imgproc.threshold(src, src, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

//        Imgproc.GaussianBlur(src, src, new Size(5, 5), 0);
//        Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);

        Bitmap bm = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(src, bm);

        return bm;
    }

    /**
     * Sort the points
     * <p>
     * The order of the points after sorting:
     * 0------->1
     * ^        |
     * |        v
     * 3<-------2
     * <p>
     * NOTE:
     * Based off of http://www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
     *
     * @param src The points to sort
     * @return An array of sorted points
     */
    private Point[] sortPoints(Point[] src) {
        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));
        Point[] result = {null, null, null, null};

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x);
            }
        };
        Comparator<Point> differenceComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x);
            }
        };

        result[0] = Collections.min(srcPoints, sumComparator);        // Upper left has the minimal sum
        result[2] = Collections.max(srcPoints, sumComparator);        // Lower right has the maximal sum
        result[1] = Collections.min(srcPoints, differenceComparator); // Upper right has the minimal difference
        result[3] = Collections.max(srcPoints, differenceComparator); // Lower left has the maximal difference

        return result;
    }


}