//package com.pancard.android.activity;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.graphics.Camera;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.FrameLayout;
//
//import com.docscan.android.R;
//import com.pancard.android.utility.CameraPreview;
//
//public class Custom_CameraActivity extends AppCompatActivity {
//    private Camera mCamera;
//    private CameraPreview mCameraPreview;
//
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//        mCamera = getCameraInstance();
//        mCameraPreview = new CameraPreview(this, mCamera);
//        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
//        preview.addView(mCameraPreview);
//
//        Button captureButton = (Button) findViewById(R.id.button_capture);
//        captureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCamera.takePicture(null, null, mPicture);
//            }
//        });
//    }
//
//    /**
//     * Helper method to access the camera returns null if it cannot get the
//     * camera or does not exist
//     *
//     * @return
//     */
//    private Camera getCameraInstance() {
//        Camera camera = null;
//        try {
//            camera = Camera.open();
//        } catch (Exception e) {
//            // cannot get camera or does not exist
//        }
//        return camera;
//    }
//
//    PictureCallback mPicture = new PictureCallback() {
//        @Override
//        public void onPictureTaken(byte[] data, Camera camera) {
//            File pictureFile = getOutputMediaFile();
//            if (pictureFile == null) {
//                return;
//            }
//            try {
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                fos.close();
//            } catch (FileNotFoundException e) {
//
//            } catch (IOException e) {
//            }
//        }
//    };
//
//    private static File getOutputMediaFile() {
//        File mediaStorageDir = new File(
//                Environment
//                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//                "MyCameraApp");
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                Log.d("MyCameraApp", "failed to create directory");
//                return null;
//            }
//        }
//        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
//                .format(new Date());
//        File mediaFile;
//        mediaFile = new File(mediaStorageDir.getPath() + File.separator
//                + "IMG_" + timeStamp + ".jpg");
//
//        return mediaFile;
//    }
//}