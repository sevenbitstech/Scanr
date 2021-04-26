package com.pancard.android.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.newflow.activity.CropActivity;
import com.pancard.android.activity.newflow.activity.NewHomeActivity;
import com.pancard.android.activity.otheracivity.CommonScan;
import com.pancard.android.activity.otheracivity.HomeActivity;
import com.pancard.android.liveedgedetection.ScanActivity;
import com.pancard.android.liveedgedetection.ScanConstants;
import com.pancard.android.liveedgedetection.enums.ScanHint;
import com.pancard.android.liveedgedetection.interfaces.IScanner;
import com.pancard.android.liveedgedetection.util.ScanUtils;
import com.pancard.android.liveedgedetection.view.ScanSurfaceView;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.FileOpration;
import com.pancard.android.utility.PermissionManager;
import com.pancard.android.utility.PreferenceManagement;
import com.pancard.android.validation_class.ReadImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment implements IScanner, View.OnClickListener {

    public static final int PICK_FILE_REQUEST_CODE = 1010;
    private static final int IMPROVED_SCANNER_REQUEST_CODE = 101;
    private static final int GALLERY_PERMISSION_CODE = 102;
    private static final String TAG = ScanActivity.class.getSimpleName();

    String[] cameraPermission = {Manifest.permission.CAMERA};
    String[] permissionsStorage = {Manifest.permission.READ_EXTERNAL_STORAGE};
    ImageButton btn_galleryImage;
    PermissionManager permissionManager;
    PreferenceManagement preferences;
    ConstraintLayout consViewPermission;
    AdRequest adRequest;
    AdView adView;
    ImageButton flash;
    private Button tempButton, btnPermissionAccess;
    private ViewGroup containerScan;
    private FrameLayout cameraPreviewLayout;
    private ScanSurfaceView mImageSurfaceView;

    private Bitmap copyBitmap;

    private TextView tvDriveNote;

    private SwitchCompat switchOcr;
    private DriveServiceHelper mDriveServiceHelper;

    public CameraFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        bindViews(view);
        initialise();
        return view;
    }

    private void bindViews(View view) {
        tempButton = view.findViewById(R.id.btn_temp);
        consViewPermission = view.findViewById(R.id.cons_permission_access);
        btnPermissionAccess = view.findViewById(R.id.btn_ask_permission);
        btn_galleryImage = view.findViewById(R.id.btn_galleryImage);
        adView = view.findViewById(R.id.ad_view);
        containerScan = view.findViewById(R.id.container_scan);
        cameraPreviewLayout = view.findViewById(R.id.camera_preview);


        tvDriveNote = view.findViewById(R.id.tv_note_drive);
        flash = view.findViewById(R.id.flash);

        switchOcr = view.findViewById(R.id.switch_cloud);
    }

    private void initialise() {
        preferences = Scanner.getInstance().getPreferences();
        permissionManager = new PermissionManager(getActivity());
        tempButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
        });
        btnPermissionAccess.setOnClickListener(v -> {
            checkPermissions(IMPROVED_SCANNER_REQUEST_CODE);
        });

        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        btn_galleryImage.setOnClickListener(v -> checkPermissionAndOpenGallery());

//        initDriveSync();


    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, getActivity());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent = Intent.createChooser(intent, "Select file");
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

//    private void onRequestCamera(int[] grantResults) {
//        if (grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            if (getActivity() != null) {
//                new Handler().postDelayed(() -> getActivity().runOnUiThread(() -> {
//                    //todo: Check below code
//                    mImageSurfaceView = new ScanSurfaceView(getActivity(), this);
//                    cameraPreviewLayout.addView(mImageSurfaceView);
//
//                }), 500);
//            }
//        } else {
//            if (getActivity() != null) {
//                Toast.makeText(getActivity(), getString(R.string.camera_activity_permission_denied_toast), Toast.LENGTH_SHORT).show();
//                //todo:Cehck below line
//                getActivity().finish();
//            }
//        }
//    }

    private void onPermissionGranted() {

        new Handler().postDelayed(() -> {
            if (getActivity() != null) {
                mImageSurfaceView = new ScanSurfaceView(getActivity(), this);
                cameraPreviewLayout.addView(mImageSurfaceView);
            }
        }, 500);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("OnActivity Result : ", "Fragment");

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            sendFile(data.getData(), data);
        }
    }

    public void clickPicture() {
//        btn_camere.setOnClickListener(v -> {

        Log.e("clicked ", "picture");
        try {
            if (mImageSurfaceView != null && mImageSurfaceView.camera != null)
                mImageSurfaceView.camera.takePicture(mImageSurfaceView.mShutterCallBack, null, mImageSurfaceView.pictureCallback);

        } catch (Exception e) {
            e.printStackTrace();
        }

//            btn_camere.setVisibility(GONE);
//        });
    }


    @Override
    public void displayHint(ScanHint scanHint) {

    }

    @Override
    public void onPictureClicked(final Bitmap bitmap) {
        try {
            //todo: bitmap is original image
            Log.e("Document Captured", "Yes");
//            btn_camere.setVisibility(GONE);
            copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Globalarea.original_image = bitmap;
            Globalarea.document_image = bitmap;

            String path = ScanUtils.saveToInternalMemory(bitmap, ScanConstants.IMAGE_DIR,
                    ScanConstants.IMAGE_NAME, getActivity(), 90)[0];

//            //todo: check below if condition
            if (getActivity() != null) {
//                if(getActivity().getIntent().getExtras() != null && getActivity().getIntent().getExtras().getString("TAG_CAMERA") != null) {
//                    Intent intent = new Intent(getActivity(), NewHomeActivity.class)
//                            .putExtra(ScanConstants.SCANNED_RESULT, path)
//                            .putExtra("TAG_CAMERA", Constants.document);
//                    getActivity().setResult(Activity.RESULT_OK, intent);
//                    //bitmap.recycle();
//                    System.gc();
//                    getActivity().finish();

                Bitmap scanBitmap = ScanUtils.decodeBitmapFromFile(path, ScanConstants.IMAGE_NAME);
                Globalarea.document_image = scanBitmap;
                CommonScan.CARD_HOLDER_NAME = ReadImage.createCameraSource(Globalarea.document_image, getActivity());

                if (CommonScan.CARD_HOLDER_NAME != null) {
                    CommonScan.CARD_HOLDER_NAME = CommonScan.CARD_HOLDER_NAME.trim();
                }
                if (CommonScan.CARD_HOLDER_NAME == null) {
                    CommonScan.CARD_HOLDER_NAME = "New Scanner";
                }
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
//
//                if (data.getStringExtra("TAG_CAMERA") != null) {
//                    Log.e("TAG CAMERA Is", data.getStringExtra("TAG_CAMERA"));

                boolean isOcr = switchOcr.isChecked();

                Intent intent = new Intent(getActivity(), CropActivity.class);
                intent.putExtra("TAG_CAMERA", Constants.document);
                intent.putExtra(Constants.KEY_OCR, isOcr);
                startActivity(intent);
                getActivity().finish();
//                    finish();
//                }

//                }else {
//                    Log.e("Document Captured","getIntent null");
//                }
            } else {
                Log.e("Document Captured", "activity null");
            }

//
//            int height = getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
//            int width = getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT).getWidth();
//
//            copyBitmap = ScanUtils.resizeToScreenContentSize(copyBitmap, width, height);
//            Mat originalMat = new Mat(copyBitmap.getHeight(), copyBitmap.getWidth(), CvType.CV_8UC1);
//            Utils.bitmapToMat(copyBitmap, originalMat);
//            List<PointF> points = new ArrayList<>();
//            ArrayList<PointF> points1FullScreen;
//            Map<Integer, PointF> pointFs = new HashMap<>();
//            Map<Integer, PointF> pointFsFullScreen = new HashMap<>();
//
//                points = ScanUtils.detectLargestQuadrilateral(originalMat);
////                if (null != quad) {
////                    double resultArea = Math.abs(Imgproc.contourArea(quad.contour));
////                    double previewArea = originalMat.rows() * originalMat.cols();
////                    Log.e("resultArea : ", String.valueOf(resultArea));
////                    Log.e("previewArea : ", String.valueOf(previewArea));
//////                    Log.e("Points : ","1");
////
//////                    if (resultArea > previewArea * 0.08) {
////                    Log.e("Points : ", "1");
////                    points = new ArrayList<>();
////                    points.add(new PointF((float) quad.points[0].x, (float) quad.points[0].y));
////                    points.add(new PointF((float) quad.points[1].x, (float) quad.points[1].y));
////                    points.add(new PointF((float) quad.points[3].x, (float) quad.points[3].y));
////                    points.add(new PointF((float) quad.points[2].x, (float) quad.points[2].y));
//////                    } else {
//////                        Log.e("Points : ","2");
//////                        points = ScanUtils.getPolygonDefaultPoints(copyBitmap);
//////                    }
////
////                } else {
////                    Log.e("Points : ", "3");
//
////                    points = ScanUtils.getPolygonDefaultPoints(copyBitmap);
////                }
//
//
//            if(points != null){
//                int index = -1;
//                for (PointF pointF : points) {
//                    pointFs.put(++index, pointF);
//                }
//
//                polygonView.setPoints(pointFs,cropImageView.getImageMatrix());
//            }else {
//
//                //calculating total area
//                //to get the full screen points
////                Display mdisp = getWindowManager().getDefaultDisplay();
////                android.graphics.Point mdispSize = new android.graphics.Point();
////                mdisp.getSize(mdispSize);
//
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
//
////
//
////                polygonView.setPoints(pointFs);
//
//                int padding = (int) getResources().getDimension(R.dimen.scan_padding);
//                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(copyBitmap.getWidth() + 2 * padding, copyBitmap.getHeight() + 2 * padding);
//                layoutParams.gravity = Gravity.CENTER;
//                polygonView.setLayoutParams(layoutParams);
//
//                DisplayMetrics displayMetrics = new DisplayMetrics();
//                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
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
//                    polygonView.setPoints(pointFsFullScreen,cropImageView.getImageMatrix());
//                } else {
//                    Log.e("poly area", String.valueOf(polyArea));
//                    Log.e("not", "less area");
//                }
//            }
//                TransitionManager.beginDelayedTransition(containerScan);
//                cropLayout.setVisibility(View.VISIBLE);
//
//                cropImageView.setImageBitmap(copyBitmap);
//                cropImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public void onExceptionHandled(Exception e) {
        e.printStackTrace();
        ///todo: reload the camera fragment
//        Toast.makeText(getActivity(), "failed to set camera parameters", Toast.LENGTH_SHORT).show();
        FirebaseCrashlytics.getInstance().recordException(e);

        if (getActivity() instanceof NewHomeActivity) {
            ((NewHomeActivity) getActivity()).changeFragment(new CameraFragment(), Constants.CAMERA_TAG);
        }

//        mImageSurfaceView = null;
////        cameraPreviewLayout.removeAllViews();
//        checkPermissions(IMPROVED_SCANNER_REQUEST_CODE);
    }

    @Override
    public void onOutOfMemory(OutOfMemoryError outOfMemoryError) {
        outOfMemoryError.printStackTrace();

        Toast.makeText(getActivity(), "Device is on low memory.", Toast.LENGTH_SHORT).show();
        if (getActivity() instanceof NewHomeActivity) {
            ((NewHomeActivity) getActivity()).changeFragment(new CameraFragment(), Constants.CAMERA_TAG);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == GALLERY_PERMISSION_CODE) {
            preferences.setShowedStoragePermissionDialog(true);
        } else {
            preferences.setShowedCameraPermissionDialog(true);
        }

        if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                if (permissionManager.isPermissionsGranted(grantResults)) {
                    openGallery();
                }
            }
        }

        if (requestCode == IMPROVED_SCANNER_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (permissionManager.isPermissionsGranted(grantResults)) {
//                        gotoImprovedScanner(scannerType);
                    consViewPermission.setVisibility(View.GONE);
                    tempButton.setVisibility(View.GONE);
                    onPermissionGranted();
                } else {
//                    btnPermissionAccess.setText("Go to Setting");
//                    btnPermissionAccess.setOnClickListener(v -> {
//                        permissionManager.openSettingDialog(getActivity(), getResources().getString(R.string.camera_permission_access));
//                    });
                    consViewPermission.setVisibility(View.VISIBLE);
                    tempButton.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_no_required_permission), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_no_required_permission), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void checkPermissions(int permissionFor) {

        if (permissionManager.hasPermissions(cameraPermission)) {
//            if(permissionFor == IMPROVED_SCANNER_REQUEST_CODE) {
//                Intent intent = new Intent(getActivity(), ScanActivity.class);
//                intent.putExtra("TAG_CAMERA",TAG);
//                startActivityForResult(intent, 101);
            onPermissionGranted();
//            }
            consViewPermission.setVisibility(View.GONE);
            tempButton.setVisibility(View.GONE);
        } else {
            if (!preferences.isShowedCameraPermissionDialog() || permissionManager.shouldRequestPermission(getActivity(), cameraPermission)) {
                permissionManager.showPermissionRequireDialog(getActivity(), getResources().getString(R.string.camera_permission_for),
                        new PermissionManager.OnPermissionRequireDialog() {
                            @Override
                            public void onAcceptPermissionDialog() {
                                Log.e("go permission", "yes");
                                if (getActivity() != null) {
                                    if (permissionManager.shouldRequestPermission(getActivity(), cameraPermission)) {
                                        permissionManager.requestPermissions(CameraFragment.this, cameraPermission, permissionFor);
                                    } else if (!preferences.isShowedCameraPermissionDialog()) {
                                        permissionManager.requestPermissions(CameraFragment.this, cameraPermission, permissionFor);
                                    } else {
                                        btnPermissionAccess.setText("Go to Setting");
                                        btnPermissionAccess.setOnClickListener(v -> {
                                            permissionManager.openSettingDialog(getActivity(), getResources().getString(R.string.camera_permission_access));
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelPermissionDialog() {
                                consViewPermission.setVisibility(View.VISIBLE);
                                tempButton.setVisibility(View.GONE);
                                Log.e("go permission", "no");
                            }
                        });

            } else {
                btnPermissionAccess.setText("Go to Setting");
                btnPermissionAccess.setOnClickListener(v -> {
                    permissionManager.openSettingDialog(getActivity(), getResources().getString(R.string.camera_permission_access));
                });
                consViewPermission.setVisibility(View.VISIBLE);
                tempButton.setVisibility(View.GONE);
            }
        }
    }

    public void checkPermissionAndOpenGallery() {

        if (permissionManager.hasPermissions(permissionsStorage)) {
            openGallery();
        } else {
            if (!preferences.isShowedStoragePermissionDialog() || permissionManager.shouldRequestPermission(getActivity(), permissionsStorage)) {
                permissionManager.showPermissionRequireDialog(getActivity(), getResources().getString(R.string.storage_permission_for),
                        new PermissionManager.OnPermissionRequireDialog() {
                            @Override
                            public void onAcceptPermissionDialog() {
                                Log.e("go permission", "yes");
                                if (!preferences.isShowedStoragePermissionDialog() || permissionManager.shouldRequestPermission(getActivity(), permissionsStorage)) {
                                    if (isAdded())
                                        permissionManager.requestPermissions(CameraFragment.this, permissionsStorage, GALLERY_PERMISSION_CODE);
                                } else {
                                    if (getActivity() != null)
                                        permissionManager.openSettingDialog(getActivity(), getResources().getString(R.string.storage_permission_for));
                                }
                            }

                            @Override
                            public void onCancelPermissionDialog() {
                                Log.e("go permission", "no");
                            }
                        });

            } else {
                if (getActivity() != null) {
                    permissionManager.openSettingDialog(getActivity(), getResources().getString(R.string.storage_permission_access));
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermissions(IMPROVED_SCANNER_REQUEST_CODE);
        showDriveNote();
        checkForSubscribedUserAds();

        if (adView != null) {
            adView.resume();
        }

    }

    private void checkForSubscribedUserAds() {
        if (Scanner.getInstance().getPreferences().isProActive()) {
            Log.e("pro active", "yes");
            adView.setVisibility(View.GONE);
        } else {
            Log.e("pro active ", "no");
            adView.setVisibility(View.VISIBLE);
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
    public void onDestroyView() {
        super.onDestroyView();

        if (adView != null) {
            adView.destroy();
        }
    }


    //todo: check this method
    private void sendFile(Uri uri, Intent data) {
        try {
            String filePath;
            System.out.println("Data : " + uri);

            FileOpration fileOpration = new FileOpration();
            if (uri.getScheme().equals("content")) {
                filePath = fileOpration.getPath(uri, getActivity());

            } else {
                filePath = uri.getPath();
            }

            if (filePath == null && data != null) {
                filePath = googleDriveFilePath(data.getData());
            }

            if (filePath != null) {
                Log.e("path ", filePath);
                Log.e("Selected path ", "yes");
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                CommonScan.CARD_IMAGE = Globalarea.document_image = Globalarea.original_image = bitmap;
                CommonScan.CARD_HOLDER_NAME = ReadImage.createCameraSource(Globalarea.document_image, getActivity());
                CommonScan.ORIGIANL_CARD_IMAGE = Globalarea.original_image;
//                selectImageChoose();
                Globalarea.document_image = Globalarea.original_image;
                Globalarea.gallery_image_uri = uri;
                Intent intent = new Intent(getActivity(), CropActivity.class);
//                intent.putExtra("TAG_CAMERA", data.getExtras().getString("TAG_CAMERA") );
                startActivity(intent);
//                finish();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Unknown path",
                        Toast.LENGTH_LONG).show();
                Log.e("Bitmap", "Unknown path");

            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError outOfMemoryError) {
            outOfMemoryError.printStackTrace();
            Toast.makeText(getActivity(), "Device on low memory", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof NewHomeActivity) {
                ((NewHomeActivity) getActivity()).changeFragment(new CameraFragment(), Constants.CAMERA_TAG);
            }
        }
    }


    private String googleDriveFilePath(Uri uri) {

        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);

            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            String extension = mime.getExtensionFromMimeType(getActivity().getContentResolver().getType(uri));

            int randomPIN = (int) (Math.random() * 9000) + 1000;
            File file = new FileOpration().CaptureImage(inputStream, randomPIN + "documentscanner." + extension);
            return file.getAbsolutePath();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onClick(View v) {

    }
}
