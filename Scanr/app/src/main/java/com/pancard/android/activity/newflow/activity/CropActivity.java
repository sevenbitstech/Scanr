package com.pancard.android.activity.newflow.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.docscan.android.BuildConfig;
import com.docscan.android.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.otheracivity.CommonScan;
import com.pancard.android.activity.scanactivity.CardScanActivity;
import com.pancard.android.database.DriveDocRepo;
import com.pancard.android.database.FileVersion;
import com.pancard.android.database.SyncStatus;
import com.pancard.android.documentscanner.OpenCVCallback;
import com.pancard.android.liveedgedetection.view.PolygonView;
import com.pancard.android.model.CardDetail;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.LocalFilesAndFolder;
import com.pancard.android.validation_class.ReadImage;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class CropActivity extends AppCompatActivity {

    //    public static  MagnifierView magnifierView;
    public static final int MAX_HEIGHT = 500;
    ImageView imgRight, imgWrong, imageview;
    ImageView imgBack;
    TextView tvDriveNote;
    //    Toolbar toolbar;
    int firstTime = 0;
    PolygonView mSelectionImageView;
    Button mButton;
    Bitmap mBitmap = Globalarea.document_image;
    Bitmap mResult;
    MaterialDialog mResultDialog;
    OpenCVCallback mOpenCVLoaderCallback;
    DriveDocRepo driveDocRepo;
    String fileloc;
    int i = 0;
    AdView adView;
    AdRequest adRequest;
    ConnectivityChangeReceiver connectivityChangeReceiver;
    MaterialAlertDialogBuilder saveDialog;
    TextView tvTitle;
    private int PICK_IMAGE_REQUEST = 1;

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

    public static RectF getImageBounds(ImageView imageView) {
        RectF bounds = new RectF();
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            imageView.getImageMatrix().mapRect(bounds, new RectF(drawable.getBounds()));
        }
        return bounds;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        bindViews();
        initialize();
    }

    private void bindViews() {
        imgRight = findViewById(R.id.img_right);
        imageview = findViewById(R.id.imageview);
        imgWrong = findViewById(R.id.img_wrong);
        imgBack = findViewById(R.id.img_back_button);
//        toolbar = findViewById(R.id.toolbar);
        mSelectionImageView = findViewById(R.id.polygonView);
        adView = findViewById(R.id.ad_view);
        tvDriveNote = findViewById(R.id.tv_note_drive);
        tvTitle = findViewById(R.id.tv_title_text);
    }

    private void initialize() {
        tvTitle.setText(getString(R.string.title_adjustment));
        driveDocRepo = new DriveDocRepo(this);
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);

        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                setLayout();
            }
        });

        imgWrong.setOnClickListener(view -> onBackPressed());
        imgBack.setOnClickListener(view -> onBackPressed());

        imgRight.setOnClickListener(v -> {
//            List<PointF> points = mSelectionImageView.getPoints();

            openFileNameDialog();
        });

        if (Scanner.getInstance().getPreferences().isProActive()) {
            adView.setVisibility(View.GONE);
        } else {
            adView.setVisibility(View.VISIBLE);
        }

        mOpenCVLoaderCallback = new OpenCVCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        break;
                    }

                    default: {
                        super.onManagerConnected(status);
                    }
                }
            }
        };

        initOpenCV();
        try {
//            mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Globalarea.gallery_image_uri);
//                    mSelectionImageView.setImageBitmap(getResizedBitmap(mBitmap, MAX_HEIGHT));
//                    magnifierView.setBitmap(mBitmap);

            imageview.setImageBitmap(getResizedBitmap(mBitmap, MAX_HEIGHT));
            List<PointF> points = findPoints();
            Map<Integer, PointF> pointFs = new HashMap<>();
            int index = -1;
            for (PointF pointF : points) {
                pointFs.put(++index, pointF);

            }
            mSelectionImageView.setPoints(pointFs, imageview.getImageMatrix());
        } catch (Exception e) {
            e.printStackTrace();
//            onResume();
        }
    }

    private void cropAndSave(String fileName) {
        List<PointF> points = mSelectionImageView.getPointsShape(imageview);

        if (mBitmap != null) {
            Mat orig = new Mat();
            org.opencv.android.Utils.bitmapToMat(mBitmap, orig);
            Mat transformed = perspectiveTransform(orig, points);
            mResult = applyThreshold(transformed);
            orig.release();
            transformed.release();
            //todo: save the image and then redirect to files fragment
//                if (mResultDialog.getCustomView() != null) {
//                    PhotoView photoView = (PhotoView) mResultDialog.getCustomView().findViewById(R.id.imageView);
//                    photoView.setImageBitmap(mResult);
//                    mResultDialog.show();
//                }


//
//                if (ScanUtils.isScanPointsValid(points)) {
//                    Point point1 = new Point(points.get(0).x, points.get(0).y);
//                    Point point2 = new Point(points.get(1).x, points.get(1).y);
//                    Point point3 = new Point(points.get(2).x, points.get(2).y);
//                    Point point4 = new Point(points.get(3).x, points.get(3).y);
//                    mResult = ScanUtils.enhanceReceipt(mBitmap, point1, point2, point3, point4);
//                } else {
//                    mResult = mBitmap;
//                }


            Calendar myCalendar = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd MMM  hh:mm:ss a", Locale.getDefault());
            String scanFormattedDate = df.format(myCalendar.getTime());

            String whichCard = Constants.document;
            String name = "Unknown";
            String originalImageName = "Unknown2";
            String publicGUID = null;
            String ocrText = ReadImage.createCameraSource(Globalarea.document_image, this);

            if (ocrText.trim().length() > 0)
                name = ocrText;


            if (Globalarea.documentPageList != null && Globalarea.documentPageList.size() > 0) {
                int size = Globalarea.getDocumentPageList().size();

                String suffix = "_" + size;

                //todo: handle it with new names
                name = Globalarea.documentPageList.get(0).getFolderName();
                name = name + suffix;

            } else {
//            name = name.substring(0, Math.min(name.length() - 1, 10)) + System.currentTimeMillis();

                publicGUID = String.valueOf(System.currentTimeMillis());
                String suffix = "_";

                name = publicGUID + suffix + whichCard + suffix + FileVersion.CROPPED.toString();
//            originalImageName = whichCard + suffix + "Original" + suffix + publicGUID;
                originalImageName = publicGUID + suffix + whichCard + suffix + FileVersion.ORIGINAL.toString();
            }

            File file = getFile(mResult, whichCard, name);
            File originalImageFile = getFile(mBitmap, whichCard, originalImageName);


            if (file != null) {
                long size = (file.length() / 1024);

                String textTobeSaved = ocrText;
                if (getIntent() != null) {
                    boolean ocr = getIntent().getBooleanExtra(Constants.KEY_OCR, true);
                    if (!ocr) {
                        textTobeSaved = ocrText + Constants.KEY_OCR_OFF_SCAN;
                    }
                }

                CardDetail cardDetail = new CardDetail(textTobeSaved,
                        scanFormattedDate.trim(), file.getAbsolutePath(), size, fileName);

                cardDetail.setWhichcard(whichCard);
                addDocument(file, cardDetail, whichCard, name, originalImageFile.getAbsolutePath(), publicGUID, fileName, false);

            }

//                Globalarea.document_image = mResult;
//                Globalarea.original_image = mBitmap;
//                CommonScan.CARD_HOLDER_NAME = ReadImage.createCameraSource(Globalarea.document_image, this);

        }
    }

    private void openFileNameDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_edit_document, null);
        final EditText etUsername = alertLayout.findViewById(R.id.tv_detail_text);

        saveDialog = new MaterialAlertDialogBuilder(this);

        // this is set the view from XML inside AlertDialog
        saveDialog.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        saveDialog.setCancelable(false)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null);
        AlertDialog dialog = saveDialog.create();
        dialog.setOnShowListener(dialogInterface -> {

            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            if (b != null) {
                b.setOnClickListener(view -> {
                    String docName = etUsername.getText().toString().trim();
                    if (docName.length() > 0) {
                        cropAndSave(docName);
                        dialog.dismiss();
                    } else
                        etUsername.setError("*Required");
//                        Toast.makeText(DragDropActivity.this, "Document Name "+docName , Toast.LENGTH_SHORT).show();
                });
            }
        });
        dialog.show();
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, this);
    }

    public File getFile(Bitmap bitmap, String tag, String fileName) {
        /* MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), card, "Detected Image", ""); */
        try {

            File imageFile;
//            File dir;
//            dir = new File(this.getExternalCacheDir() + "/" + getResources().getString(R.string.app_name), tag);
////            File file = new File(this.getExternalCacheDir(), "image.png");
//            boolean success = true;
//            if (!dir.exists()) {
//                success = dir.mkdirs();
//            }

//            if(FirebaseAuth.getInstance().getCurrentUser() != null) {
//                File subDir = LocalFilesAndFolder.getSubDir(tag, FirebaseAuth.getInstance().getCurrentUser().getUid());
            File subDir = LocalFilesAndFolder.getRootDirOfApp();

            if (subDir != null) {
//                Date date = new Date();
//                imageFile = new File(dir.getAbsolutePath()

                imageFile = new File(subDir.getAbsolutePath()
                        + File.separator
                        + fileName + ".jpg");
                Log.e("Image Path : ", imageFile.toString());
//                    if(!imageFile.exists()){
//                        imageFile.mkdir();
//                    }
                if (!imageFile.getParentFile().exists())
                    imageFile.getParentFile().mkdirs();
                if (!imageFile.exists())
                    imageFile.createNewFile();
//                    imageFile.createNewFile();


                ByteArrayOutputStream oStream = new ByteArrayOutputStream();

                // save image into gallery
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, oStream);

                FileOutputStream fOut = new FileOutputStream(imageFile);
                fOut.write(oStream.toByteArray());
                fOut.close();
                ContentValues values = new ContentValues();

                values.put(MediaStore.Images.Media.DATE_TAKEN,
                        System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATA,
                        imageFile.getAbsolutePath());
                return imageFile;
            } else {
                return null;
            }
//            }else {
//                Toast.makeText(this, "May be your session has expired. Please login again.", Toast.LENGTH_SHORT).show();
//                FirebaseAuth.getInstance().signOut();
//                preferences.removeAllData();
//                Globalarea.firebaseUser = null;
//                Intent intent = new Intent(this, SignInActivity.class);
//                startActivity(intent);
//                finish();
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

//        return null;
    }

    private void addDocument(File file, CardDetail cardDetail, String tag, String subFolderName, String originalImageFilePath, String public_guid, String fileName, boolean saveForNextPages) {

        String strCardDetails = Globalarea.getStringOfCardDetails(cardDetail);
//        Log.i("adding to localDB",strCardDetails);
        if (strCardDetails != null && file != null) {

            DriveDocModel driveDocModel = new DriveDocModel(public_guid, file.getAbsolutePath(), originalImageFilePath, strCardDetails, cardDetail.getWhichcard(), SyncStatus.unsynced.toString());
            driveDocModel.setFileName(fileName);

            if (Globalarea.documentPageList != null && Globalarea.documentPageList.size() > 0) {
                int size = Globalarea.getDocumentPageList().size();
                Log.e("past pages size", String.valueOf(size));

                String suffix = "_" + size;
                driveDocModel.setFolderName(subFolderName + suffix);

            } else {
                driveDocModel.setFolderName(subFolderName);
                Globalarea.addThePageIntoGetDocumentPageList(driveDocModel);
            }

            if (saveForNextPages) {
                Globalarea.documentPageList.add(driveDocModel);
                Log.e("save for next page", "yeah");
            } else {
                Globalarea.documentPageList = null;
            }

            int added = driveDocRepo.addDriveDocInfo(driveDocModel);

//            if (tag.equals(Constants.adharcard)) {
//                if(cardDetail.getIssue_date()!=null){
//                    //todo : Need to add Original image for aadhar back image
//                    DriveDocModel driveDocModelBack = new DriveDocModel(cardDetail.getIssue_date(), strCardDetails, cardDetail.getWhichcard(), SyncStatus.unsynced.toString());
//                    String suffix = "_" + "1";
//                    driveDocModel.setFolderName(subFolderName + suffix);
//
//
//                    String folderName = driveDocModel.getFolderName();
//                    Log.e("old string",folderName);
//                    int charSize = folderName.length();
//
//                    if (folderName.charAt(charSize - 2) == '_') {
//                        //todo: remove ....
//                        String newString = folderName.substring(0, charSize - 2);
//                        Log.e("new stirng", newString);
//
//                    }else {
//                        Log.e("no sub", "page found");
//                    }
//
//                    driveDocRepo.addDriveDocInfo(driveDocModelBack);
//                }
//            }

            if (added > 0) {
                Toast.makeText(CropActivity.this, "Successfully stored document.", Toast.LENGTH_SHORT).show();
                //todo: handle different ways for both new scan and more page scan
//                prepareUploadInDrive(file, cardDetail, tag, subFolderName);

                if (saveForNextPages) {
                    Intent intent = new Intent(CropActivity.this, CommonScan.class);
                    intent.putExtra(CommonScan.SCANNER_TYPE, tag);
                    startActivity(intent);
                    this.finish();
                } else
                    navigateToFiles();
            } else {
                Toast.makeText(CropActivity.this, "Error storing document.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToFiles() {

        Intent intent = new Intent(CropActivity.this, NewHomeActivity.class);
        intent.putExtra(Constants.SAVED_CARD, "true");
        intent.putExtra(Constants.START_FRAGMENT, Constants.FILES_TAG);
        startActivity(intent);
        finish();
        finishAffinity();
    }

    public void navigateActivity() {
//        Log.e("TAG CAMERA Is",data.getStringExtra("TAG_CAMERA"));
        Intent intent = new Intent(this, CardScanActivity.class);
        intent.putExtra("TAG_CAMERA", Constants.document);
        startActivity(intent);
        finish();
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_gallery) {
//            Intent intent = new Intent();
//            intent.setType("image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//        } else if (id == R.id.action_camera) {
//            // TODO Camera
//            openc();
////            new MaterialDialog.Builder(this)
////                    .title("TODO")
////                    .content("The camera is a TODO item.")
////                    .positiveText("OK")
////                    .show();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.crop_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_crop_full:

                return true;

            case R.id.menu_crop:

                return true;

            case R.id.menu_rotate_left:

                return true;

            case R.id.menu_rotate_right:

                return true;


        }
        return true;
    }

    private void openc() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        try {
            f = File.createTempFile("temppic", ".jpg", getApplicationContext().getCacheDir());
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(CropActivity.this, BuildConfig.APPLICATION_ID + ".provider", f));
                fileloc = Uri.fromFile(f) + "";
                Log.d("texts", "openc: " + fileloc);
                startActivityForResult(takePictureIntent, 3);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Mat perspectiveTransform(Mat src, List<PointF> points) {
        Point point1 = new Point(points.get(0).x, points.get(0).y);
        Point point2 = new Point(points.get(1).x, points.get(1).y);
        Point point3 = new Point(points.get(2).x, points.get(2).y);
        Point point4 = new Point(points.get(3).x, points.get(3).y);
        Point[] pts = {point1, point2, point3, point4};
        return fourPointTransform(src, sortPoints(pts));
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(connectivityChangeReceiver);

        if (adView != null) {
            adView.destroy();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//
//            Uri uri = data.getData();
//
//            try {
//                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                mSelectionImageView.setImageBitmap(getResizedBitmap(mBitmap, MAX_HEIGHT));
//                List<PointF> points = findPoints();
//                mSelectionImageView.setPoints(points);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }else   if(requestCode == 3 && resultCode == RESULT_OK) {
//            Log.d("texts", "onActivityResult: "+fileloc);
//            // fileloc is the uri of the file so do whatever with it
//
//            try {
//                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(fileloc));
//                mSelectionImageView.setImageBitmap(getResizedBitmap(mBitmap, MAX_HEIGHT));
//                List<PointF> points = findPoints();
//                mSelectionImageView.setPoints(points);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();

//        if (adView != null) {
//            adView.resume();
//        }

        if (adView != null) {
            adView.resume();
        }

        showDriveNote();
        setLayout();

    }

    public void setLayout() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                try {
//            mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Globalarea.gallery_image_uri);
//                    mSelectionImageView.setImageBitmap(getResizedBitmap(mBitmap, MAX_HEIGHT));
//                    magnifierView.setBitmap(mBitmap);
                    imageview.setImageBitmap(getResizedBitmap(mBitmap, MAX_HEIGHT));
                    List<PointF> points = findPoints();

                    int area = 1;
                    for (int i = 0; i < points.size(); i++) {

                        PointF pointF = points.get(i);
                        Log.e("point x" + i, String.valueOf(pointF.x));
                        Log.e("point y" + i, String.valueOf(pointF.y));

                    }
                    Log.e("point x : ", String.valueOf(points.get(0).x));
                    Log.e("point y : ", String.valueOf(points.get(0).y));

                    Map<Integer, PointF> pointFs = new HashMap<>();
                    int index = -1;
                    for (PointF pointF : points) {

                        pointFs.put(++index, pointF);
//                       points_set.add(pointF);
                    }
                    mSelectionImageView.setPoints(pointFs, imageview.getImageMatrix());
                    mSelectionImageView.setPoints(pointFs, imageview.getImageMatrix());

                    if (firstTime <= 2) {
                        Log.e("Error : ", "not set");
                        firstTime++;
                        onResume();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
//            onResume();
                }

            }
        }, 1);

    }

    private List<PointF> findPoints() {
        List<PointF> result = null;

        Mat image = new Mat();
        Mat orig = new Mat();
        org.opencv.android.Utils.bitmapToMat(getResizedBitmap(mBitmap, MAX_HEIGHT), image);
        org.opencv.android.Utils.bitmapToMat(mBitmap, orig);

        Mat edges = edgeDetection(image);
        MatOfPoint2f largest = findLargestContour(edges);

        double area = 0;
        if (largest != null) {
            Point[] points = sortPoints(largest.toArray());
            result = new ArrayList<>();

            PointF point1 = new PointF(Double.valueOf(points[0].x).floatValue(), Double.valueOf(points[0].y).floatValue());
            PointF point2 = new PointF(Double.valueOf(points[1].x).floatValue(), Double.valueOf(points[1].y).floatValue());
            PointF point3 = new PointF(Double.valueOf(points[3].x).floatValue(), Double.valueOf(points[3].y).floatValue());
            PointF point4 = new PointF(Double.valueOf(points[2].x).floatValue(), Double.valueOf(points[2].y).floatValue());

            result.add(point1);
            result.add(point2);
            result.add(point3);
            result.add(point4);

            area = findTheArea(point1, point2, point3, point4);
            Log.e("default area", String.valueOf(area));

            largest.release();
        } else {
            //todo: make it full screen
            Timber.d("Can't find rectangle!");
        }

        if (result == null || area < 50.0) {
            //todo: make it full screen
            Log.e("found area", String.valueOf(area));
            Log.e("Can't find rectangle!", "Menual points");
            //calculating total area
            //to get the full screen points
//                Display mdisp = getWindowManager().getDefaultDisplay();
//                android.graphics.Point mdispSize = new android.graphics.Point();
//                mdisp.getSize(mdispSize);
            result = new ArrayList<>();

            //full screen way....


//            RectF newImageBounds = getImageBounds(imageview);
//            Log.e("top", String.valueOf(newImageBounds.top));
//            Log.e("bottom", String.valueOf(newImageBounds.bottom));
//            Log.e("left", String.valueOf(newImageBounds.left));
//            Log.e("right", String.valueOf(newImageBounds.right));


            //image only.
//            result.add(new PointF(newImageBounds.left, newImageBounds.top));
//            result.add(new PointF(newImageBounds.right, newImageBounds.top));
//            result.add(new PointF(newImageBounds.left, newImageBounds.bottom));
//            result.add(new PointF(newImageBounds.right, newImageBounds.bottom));

            //full image view
//            result.add(new PointF(imageview.getLeft(), imageview.getTop()));
//            result.add(new PointF(imageview.getRight(), imageview.getTop()));
//            result.add(new PointF(imageview.getLeft(), imageview.getBottom()));
//            result.add(new PointF(imageview.getRight(), imageview.getBottom()));


            //older way..........

            //todo: use it for full screeen.
            int maxX = imageview.getWidth();
            int maxY = imageview.getHeight();

            int halfWidth = (maxX / 2);
            int halfHeight = (maxY / 2);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            float height = displayMetrics.heightPixels / 2.0f;
            float width = displayMetrics.widthPixels / 2.0f;

            float heightImg = imageview.getMeasuredHeight() / 2.0f;
            float widthImg = imageview.getMeasuredWidth() / 2.0f;

            float heightNewImg = imageview.getDrawable().getIntrinsicHeight();
            float widthNewImg = imageview.getDrawable().getIntrinsicWidth();

            android.graphics.Point size = new android.graphics.Point();
            Display display = getWindowManager().getDefaultDisplay();
            display.getSize(size);

            int newWidth = size.x;
            int newHeight = size.y;

//            int startX = (width / 5);
//            int endX = (width / 2);
//            int startY = (height / 5);
//            int endY = (height / 3);

//            float startX = (width / 5.0f);
//            float endX = (width / 2.0f);
//            float startY = (height / 5.0f);
//            float endY = (height / 2.0f);

            float startX = (width / 5.0f);
            float endX = (widthNewImg);
            float startY = (height / 5.0f);
            float endY = (heightNewImg);

            //working okay.
//            float startX = (width / 5.0f);
//            float endX = (widthImg / 2.0f);
//            float startY = (height / 5.0f);
//            float endY = (heightImg / 2.0f);


            float centreX = imageview.getX() + imageview.getWidth() / 2.0f;
            float centreY = imageview.getY() + imageview.getHeight() / 2.0f;

            int[] coords = {0, 0};
            imageview.getLocationOnScreen(coords);
            int absoluteTop = coords[1];
            int absoluteBottom = coords[1] + imageview.getHeight();
            int absoluteRight = coords[0] + imageview.getWidth();

//            result.add(new PointF(0, 0));
//            result.add(new PointF(absoluteRight-20, 0));
//            result.add(new PointF(0, absoluteBottom-20));
//            result.add(new PointF(absoluteRight-20, absoluteBottom-20));

//            double xScaleFactor= scaledWidth/imgbitmap.getWidth();
//            double yScaleFactor= scaledHeight/imgbitmap.getHeight();
//
//            android.graphics.Point canvas_point1 = new android.graphics.Point((int)((point1.x*xScaleFactor)),(int)((point1.y*yScaleFactor)));
//            android.graphics.Point canvas_point2 = new android.graphics.Point((int)((point2.x*xScaleFactor)),(int)((point2.y*yScaleFactor)));
//            canvas.drawLine(canvas_point1.x,canvas_point1.y, canvas_point2.x, canvas_point2.y, paint);
//

            Rect rectf = new Rect();

//For coordinates location relative to the screen/display
            imageview.getGlobalVisibleRect(rectf);

            float real_x = (centreX * 640.0f) / (float) displayMetrics.widthPixels;
            float real_y = (centreY * 480.0f) / (float) displayMetrics.heightPixels;

            int startX1 = (int) (halfWidth / 8.0f);
            int topY1 = (int) (halfHeight / 8.0f);
            int endX1 = (int) (centreX + halfWidth / 4.0f);
            int bottomY1 = (int) (centreY - halfHeight / 4.0f);


//            result.add(new PointF(0, 0));
//            result.add(new PointF(endX1, 0));
//            result.add(new PointF(0, bottomY1));
//            result.add(new PointF(endX1, bottomY1));


            PointF point1 = new PointF(0, 0);
            PointF point2 = new PointF(endX, 0);
            PointF point3 = new PointF(0, endY);
            PointF point4 = new PointF(endX, endY);

            //somewhat acceptable
            result.add(point1);
            result.add(point2);
            result.add(point3);
            result.add(point4);

            //old result
//            result.add(new PointF(startX, startY));
//            result.add(new PointF(endX, startY));
//            result.add(new PointF(startX, endY));
//            result.add(new PointF(endX, endY));


            double area1 = findTheArea(point1, point2, point3, point4);
            Log.e("manual area", String.valueOf(area1));

        }

        edges.release();
        image.release();
        orig.release();

        return result;
    }

    private double findTheArea(PointF point1, PointF point2, PointF point3, PointF point4) {
        double sum1 = (point1.x * point2.y) - (point1.y * point2.x);
        double sum2 = (point2.x * point3.y) - (point2.y * point3.x);
        double sum3 = (point3.x * point4.y) - (point3.y * point4.x);
        double sum4 = (point4.x * point1.y) - (point4.y * point1.x);

        double finalSum = sum1 + sum2 + sum3 + sum4;
        double finalAns = finalSum / 2.0;

        return Math.abs(finalAns);
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
        Collections.sort(contours, (o1, o2) -> {
            double area1 = Imgproc.contourArea(o1);
            double area2 = Imgproc.contourArea(o2);
            return (int) (area2 - area1);
        });
        if (contours.size() > 5) {
            contours.subList(4, contours.size() - 1).clear();
        }

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
     * Attempt to load OpenCV via statically compiled libraries.  If they are not found, then load
     * using OpenCV Manager.
     */
    private void initOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            Timber.d("Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mOpenCVLoaderCallback);
        } else {
            Timber.d("OpenCV library found inside package. Using it!");
            mOpenCVLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
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

    /**
     * NOTE:
     * Based off of http://www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
     *
     * @param src
     * @param pts
     * @return
     */
    private Mat fourPointTransform(Mat src, Point[] pts) {
        double ratio = src.size().height / (double) MAX_HEIGHT;

        Point ul = pts[0];
        Point ur = pts[1];
        Point lr = pts[2];
        Point ll = pts[3];

        double widthA = Math.sqrt(Math.pow(lr.x - ll.x, 2) + Math.pow(lr.y - ll.y, 2));
        double widthB = Math.sqrt(Math.pow(ur.x - ul.x, 2) + Math.pow(ur.y - ul.y, 2));
        double maxWidth = Math.max(widthA, widthB) * ratio;

        double heightA = Math.sqrt(Math.pow(ur.x - lr.x, 2) + Math.pow(ur.y - lr.y, 2));
        double heightB = Math.sqrt(Math.pow(ul.x - ll.x, 2) + Math.pow(ul.y - ll.y, 2));
        double maxHeight = Math.max(heightA, heightB) * ratio;

        Mat resultMat = new Mat(Double.valueOf(maxHeight).intValue(), Double.valueOf(maxWidth).intValue(), CvType.CV_8UC4);

        Mat srcMat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dstMat = new Mat(4, 1, CvType.CV_32FC2);
        srcMat.put(0, 0, ul.x * ratio, ul.y * ratio, ur.x * ratio, ur.y * ratio, lr.x * ratio, lr.y * ratio, ll.x * ratio, ll.y * ratio);
        dstMat.put(0, 0, 0.0, 0.0, maxWidth, 0.0, maxWidth, maxHeight, 0.0, maxHeight);

        Mat M = Imgproc.getPerspectiveTransform(srcMat, dstMat);
        Imgproc.warpPerspective(src, resultMat, M, resultMat.size());

        srcMat.release();
        dstMat.release();
        M.release();

        return resultMat;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, NewHomeActivity.class);
        startActivity(intent);
        finish();
    }
}
