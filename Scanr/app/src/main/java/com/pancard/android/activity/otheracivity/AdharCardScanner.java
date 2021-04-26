//package com.pancard.android.activity.otheracivity;
//
//import android.content.Intent;
//import android.content.pm.ActivityInfo;
//import android.graphics.BitmapFactory;
//import android.graphics.ImageFormat;
//import android.hardware.Camera;
//import android.hardware.Camera.AutoFocusCallback;
//import android.hardware.Camera.Parameters;
//import android.hardware.Camera.PreviewCallback;
//import android.hardware.Camera.Size;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.util.SparseArray;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.docscan.android.R;
//import com.google.android.gms.vision.Frame;
//import com.google.android.gms.vision.barcode.Barcode;
//import com.google.android.gms.vision.barcode.BarcodeDetector;
//import com.pancard.android.Globalarea;
//import com.pancard.android.utility.CameraPreviewScanner;
//import com.pancard.android.utility.Constants;
//
//import net.sourceforge.zbar.Config;
//import net.sourceforge.zbar.Image;
//import net.sourceforge.zbar.ImageScanner;
//import net.sourceforge.zbar.Symbol;
//import net.sourceforge.zbar.SymbolSet;
//
//import java.nio.ByteBuffer;
//
//public class AdharCardScanner extends AppCompatActivity {
//
//    public static int ExtraFields = 7;
//
////    static {
////        System.loadLibrary("iconv");
////    }
//
//    TextView scanText;
//    Button scanButton;
////    ImageScanner scanner;
//    String FromActivity = "";
//    private Camera mCamera;
//    private CameraPreviewScanner mPreview;
//    private Handler autoFocusHandler;
//    private boolean barcodeScanned = false;
//    private boolean previewing = true;
//    PreviewCallback previewCb = new PreviewCallback() {
//        public void onPreviewFrame(byte[] data, Camera camera) {
//
//            System.out.println("**Scanner Callback is Calling...");
////            Parameters parameters = camera.getParameters();
////            Size size = parameters.getPreviewSize();
//
////            Image barcode = new Image(size.width, size.height, "Y800");
////            barcode.setData(data);
//
//            BarcodeDetector detector =
//                    new BarcodeDetector.Builder(getApplicationContext())
//                            .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
//                            .build();
//            if(!detector.isOperational()){
//                Toast.makeText(getApplicationContext(),"Could not set up the detector!",Toast.LENGTH_LONG).show();
//                return;
//            }
//
//            Frame frame = new Frame.Builder().setBitmap(BitmapFactory.decodeByteArray(data, 0, data.length)).build();
//            SparseArray<Barcode> barcodes = detector.detect(frame);
////            Barcode thisCode = barcodes.valueAt(0);
//            String result =  barcodes.valueAt(0).displayValue;
//
//            if (result != null) {
//
//                System.out.println("**Scanner Callback Result is : " + String.valueOf(result));
//                previewing = false;
//                mCamera.setPreviewCallback(null);
//                mCamera.stopPreview();
//
//                System.out.println("**Camera preview is stopped");
//
////                SymbolSet syms = scanner.getResults();
//
//                String Result = null;
////                for (Symbol sym : syms) {
////
////                    Result = sym.getData();
////                    //scanText.setText("QR Code Result :" + sym.getData());
////                    System.out.println("**QR Code Result is : " + sym.getData());
////                    barcodeScanned = true;
////                }
//
////                if (Result == null) {
////                    Toast.makeText(AdharCardScanner.this, "Sorry invalid QR Code", Toast.LENGTH_SHORT).show();
////                } else {
//                    Log.e("QR code Result :", Result);
//                    Globalarea.adharCardText = Result;
//                    OpenScanActivity();
////                }
//
//            }else {
//                Toast.makeText(AdharCardScanner.this, "Sorry invalid QR Code", Toast.LENGTH_SHORT).show();
//            }
//        }
//    };
//    private Runnable doAutoFocus = new Runnable() {
//        public void run() {
//            if (previewing)
//                mCamera.autoFocus(autoFocusCB);
//        }
//    };
//    // Mimic continuous auto-focusing
//    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
//        public void onAutoFocus(boolean success, Camera camera) {
//            autoFocusHandler.postDelayed(doAutoFocus, 1000);
//        }
//    };
//
//    /**
//     * A safe way to get an instance of the Camera object.
//     */
//    public static Camera getCameraInstance() {
//        Camera c = null;
//        try {
//            c = Camera.open();
//        } catch (Exception e) {
//        }
//        return c;
//    }
//
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_scanner);
//
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//        init();
//
//
////        cameraSource = new CameraSource.Builder(this, barcodeDetector)
////                .setRequestedPreviewSize(1600, 1024)
////                .setAutoFocusEnabled(true) //you should add this feature
////                .build();
//
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        init();
//    }
//
//    private void init(){
//        autoFocusHandler = new Handler();
//        mCamera = getCameraInstance();
//
//        /* Instance barcode scanner */
////        scanner = new ImageScanner();
////        scanner.setConfig(0, Config.X_DENSITY, 3);
////        scanner.setConfig(0, Config.Y_DENSITY, 3);
//
//        mPreview = new CameraPreviewScanner(this, mCamera, previewCb, autoFocusCB);
//        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
//        preview.addView(mPreview);
//
//        scanText = (TextView) findViewById(R.id.scanText);
//
//        scanButton = (Button) findViewById(R.id.ScanButton);
//
//        scanButton.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                if (barcodeScanned) {
//                    barcodeScanned = false;
//                    // scanText.setText("Scanning...");
//                    mCamera.setPreviewCallback(previewCb);
//                    mCamera.startPreview();
//                    previewing = true;
//                    mCamera.autoFocus(autoFocusCB);
//                }
//            }
//        });
//    }
//
//    public void onPause() {
//        super.onPause();
////        releaseCamera();
//    }
//
//    private void releaseCamera() {
//        if (mCamera != null) {
//            previewing = false;
//            mCamera.setPreviewCallback(null);
//            mCamera.release();
//            mCamera = null;
//        }
//    }
//
//    private void OpenScanActivity() {
//
//        Intent intent = new Intent(AdharCardScanner.this, CommonScan.class);
//        intent.putExtra(CommonScan.SCANNER_TYPE, Constants.adharcard);
//        startActivity(intent);
//        this.finish();
//
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent home_Intent = new Intent(AdharCardScanner.this, HomeActivity.class);
//        startActivity(home_Intent);
//        this.finish();
//    }
//
//
//}