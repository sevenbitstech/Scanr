package com.pancard.android.activity.newflow.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.otheracivity.CommonScan;
import com.pancard.android.adapter.DragDropListAdapter;
import com.pancard.android.custom.ImageBitmapMerge;
import com.pancard.android.database.DriveDocRepo;
import com.pancard.android.database.FileVersion;
import com.pancard.android.database.SyncStatus;
import com.pancard.android.dragdrophelper.OnStartDragListener;
import com.pancard.android.dragdrophelper.SimpleItemTouchHelperCallback;
import com.pancard.android.model.CardDetail;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.LocalFilesAndFolder;
import com.pancard.android.validation_class.ReadImage;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import uk.co.senab.photoview.PhotoView;

public class DragDropActivity extends AppCompatActivity implements OnStartDragListener {

    //    public static  MagnifierView magnifierView;
//    public static final int MAX_HEIGHT = 500;
    ImageView imgRight, imgWrong;
    ImageView imgBack;
    TextView tvDriveNote;
    DriveDocRepo driveDocRepo;
    //    String fileloc;
//    int i = 0;
    AdView adView;
    AdRequest adRequest;
    ConnectivityChangeReceiver connectivityChangeReceiver;
    RecyclerView recyclerView;
    List<DriveDocModel> driveDocModelList;
    DragDropListAdapter adapter;
    Bitmap bitmapMergedImage;
    TextView tvTitle;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    //    MaterialAlertDialogBuilder saveDialog;
    private androidx.appcompat.app.AlertDialog saveDialog;
    //    private int PICK_IMAGE_REQUEST = 1;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_drop);
        bindViews();
        initialize();
    }

    private void bindViews() {
        imgRight = findViewById(R.id.img_right);
        imgWrong = findViewById(R.id.img_wrong);
        imgBack = findViewById(R.id.img_back_button);
//        toolbar = findViewById(R.id.toolbar);
        adView = findViewById(R.id.ad_view);
        tvDriveNote = findViewById(R.id.tv_note_drive);
        recyclerView = findViewById(R.id.recyclerView);
        tvTitle = findViewById(R.id.tv_title_text);
        progressBar = findViewById(R.id.progress);
    }

    private void initialize() {
        tvTitle.setText(getString(R.string.title_arrange));
//        progressdialog = new ProgressDialog(this);
        driveDocRepo = new DriveDocRepo(this);
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);

        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        imgWrong.setOnClickListener(view -> onBackPressed());
        imgBack.setOnClickListener(view -> onBackPressed());

        imgRight.setOnClickListener(v -> {
            if (adapter.driveDocModels != null && adapter.driveDocModels.size() > 0) {
//                for (DriveDocModel driveDocModel:driveDocModelList) {
//                    Log.e("My adapter",driveDocModel.getFolderName());
//                }

                bitmapMergedImage = mergeMultiple2();
                if (bitmapMergedImage != null) {
                    showEditDocumentDialog();
                }
            }
        });

        if (getIntent() != null) {
            String strDriveDocModels = getIntent().getStringExtra(Constants.FILES_SELECTED);

            if (strDriveDocModels != null && strDriveDocModels.length() > 0) {
                List<DriveDocModel> driveDocModels = Globalarea.getDriveDocListOfString(strDriveDocModels);

                if (driveDocModels != null && driveDocModels.size() > 0) {
                    driveDocModelList = driveDocModels;
                    setDragDropAdapter();
                }
            }
        }
    }

    private void prepareAddDocument(String docName, String pdfFilePath) {

        Log.e("pdf file path preparing", pdfFilePath);
        if (bitmapMergedImage != null) {
            showProgress();

            Calendar myCalendar = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd MMM  hh:mm:ss a", Locale.getDefault());
            String scanFormattedDate = df.format(myCalendar.getTime());

            String whichCard = Constants.document;
            String name;
            String originalImageName = "Unknown2";
            String publicGUID = null;
            String ocrText = ReadImage.createCameraSource(bitmapMergedImage, this);

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

            File file = getFile(bitmapMergedImage, whichCard, name);
            File originalImageFile = getFile(bitmapMergedImage, whichCard, originalImageName);

            if (file != null) {
                long size = (file.length() / 1024);

//                String textTobeSaved = ocrText;
//                if (getIntent() != null) {
//                    boolean ocr = getIntent().getBooleanExtra(Constants.KEY_OCR, true);
//                    if (!ocr) {
//                        textTobeSaved = ocrText + Constants.KEY_OCR_OFF_SCAN;
//                    }
//                }

                Log.e("fetched text", ocrText);
                CardDetail cardDetail = new CardDetail(ocrText,
                        scanFormattedDate.trim(), file.getAbsolutePath(), size, docName);
                cardDetail.setPdfFilePath(pdfFilePath);

                cardDetail.setWhichcard(whichCard);
                addDocument(file, cardDetail, whichCard, name, originalImageFile.getAbsolutePath(),
                        publicGUID, docName, pdfFilePath, false);
            } else {
                Toast.makeText(this, "Error in creating the PDF. Can not merge the images.",
                        Toast.LENGTH_SHORT).show();
            }

//                Globalarea.document_image = mResult;
//                Globalarea.original_image = mBitmap;
//                CommonScan.CARD_HOLDER_NAME = ReadImage.createCameraSource(Globalarea.document_image, this);

        } else {
            Toast.makeText(this, "No image found to create the PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideProgress() {
        Log.e("hiding ", "yes");
//        Toast.makeText(this, "hide", Toast.LENGTH_SHORT).show();
//        progressBar.setVisibility(View.GONE);
        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog.dismiss();
        }
    }

    private void showProgress() {
        Log.e("showing", "yes");
//        Toast.makeText(this, "show", Toast.LENGTH_SHORT).show();
//        progressBar.setVisibility(View.VISIBLE);
//        Toast.makeText(this, "Showing dialog", Toast.LENGTH_SHORT).show();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
    }

    private Bitmap mergeMultiple2() {
        ImageBitmapMerge imageBitmapMerge = new
                ImageBitmapMerge(this, this, adapter.driveDocModels);

        return imageBitmapMerge.startBitmapMerge();
    }


    private void showPhotoView(Bitmap bitmap) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DragDropActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_document_scan_result, null);
        PhotoView photoView = mView.findViewById(R.id.imageView);
        photoView.setImageBitmap(bitmap);
        mBuilder.setView(mView);
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void showEditDocumentDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_edit_document, null);
        final EditText etUsername = alertLayout.findViewById(R.id.tv_detail_text);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
//        saveDialog = new MaterialAlertDialogBuilder(getApplicationContext());

        // this is set the view from XML inside AlertDialog
        builder.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        builder.setCancelable(false)
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    String docName = etUsername.getText().toString().trim();
                    if (docName.length() > 0) {
                        dialogInterface.dismiss();
                        //todo: hide keyboard here.
                        String pdfFilePath = createPdf(docName);
//                        String pdfFilePath = createPDFPdfBox(docName);

                        if (pdfFilePath != null) {
                            Log.e("returned path", pdfFilePath);
//                                dialogInterface.dismiss();
                            prepareAddDocument(docName, pdfFilePath);
                        } else {
                            //toast is already shown
                            Log.e("no pdf path", "found");
                        }

                    } else
                        etUsername.setError("*Required");
//                        Toast.makeText(DragDropActivity.this, "Document Name "+docName , Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        saveDialog = builder.show();
//        AlertDialog dialog = saveDialog.create();
//        dialog.setOnShowListener(dialogInterface -> {
//
//            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//
//            if (b != null) {
//                b.setOnClickListener(view -> {
//
//                });
//            }
//        });
//        dialog.show();

    }

    private String createPdf(String docName) {
//        WindowManager wm = (WindowManager)getActivity(). getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        float hight = displaymetrics.heightPixels ;
//        float width = displaymetrics.widthPixels ;
//
//        int convertHighet = (int) hight, convertWidth = (int) width;

//        Resources mResources = getResources();
//        Bitmap bitmap = BitmapFactory.decodeResource(mResources, R.drawable.screenshot);

        showProgress();
        List<DriveDocModel> driveDocModels = adapter.driveDocModels;


//        CardDetail cardDetail = Globalarea.getCardDetailOfString(driveDocModel.getJsonText());
//        String documentName = cardDetail.card_name;

        try {
            PdfDocument document = new PdfDocument();

            for (int i = 0; i < driveDocModels.size(); i++) {
                DriveDocModel driveDocModel = driveDocModels.get(i);
                Bitmap bitmapOriginal = BitmapFactory.decodeFile(new File(driveDocModel.getImagePath()).getPath());

                // Compress bitmap
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapOriginal.compress(Bitmap.CompressFormat.JPEG, 15, stream);
                byte[] bitmapData = stream.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);

                float bitmapHeight = (float) bitmap.getHeight();
                float bitmapWidth = (float) bitmap.getWidth();
//            Log.e("bitmap width", String.valueOf(bitmapWidth));
//            Log.e("bitmap width", String.valueOf(bitmapHeight));

                //Create an A4 sized page 595 x 842 in Postscript points.
                int pageWidth = 595;
                int pageHeight = 842;
//            float ratioMax = (float) pageWidth / (float) pageHeight;
//            Log.e("page ratio", String.valueOf(ratioMax));

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);

                Canvas canvas = page.getCanvas();

                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#ffffff"));
                canvas.drawPaint(paint);

                float aspect_ratio = bitmapWidth / bitmapHeight;
//            float pdfAspectRatio;
//            Log.e("aspect ratio", String.valueOf(aspect_ratio));

                if (bitmap.getWidth() > pageWidth && bitmap.getHeight() > pageHeight) {
                    Log.e("re scaling", "yes");
                    if (bitmapHeight > bitmapWidth) {
//                    Log.e("height is ", "greater");
                        int newWidth = (int) (pageHeight * aspect_ratio);

//                    pdfAspectRatio = ((float) newWidth) / ((float) pageHeight);
//                    Log.e("pdf aspect ratio", String.valueOf(pdfAspectRatio));

                        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, pageHeight, true);
                    } else if (bitmapWidth > bitmapHeight) {
//                    Log.e("width is ", "greater");
                        int newHeight = (int) (pageWidth / aspect_ratio);

//                    pdfAspectRatio = (float) pageWidth / (float) newHeight;
//                    Log.e("pdf aspect ratio", String.valueOf(pdfAspectRatio));

                        bitmap = Bitmap.createScaledBitmap(bitmap, pageWidth, newHeight, true);
                    } else {
                        bitmap = Bitmap.createScaledBitmap(bitmap, pageWidth, pageHeight, true);
                    }

                } else if (bitmapWidth > pageWidth) {
                    Log.e("bitmap width is", "greateer");
                    float newHeight = (float) pageWidth / aspect_ratio;
                    bitmap = Bitmap.createScaledBitmap(bitmap, pageWidth, (int) newHeight, true);
                } else if (bitmapHeight > pageHeight) {
                    Log.e("bitmap height is", "greater");
                    int newWidth = (int) (pageHeight * aspect_ratio);
                    bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, pageHeight, true);
                } else {
                    Log.e("using", "original height and width");
                    bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                }


                float left = pageWidth / 2.0f - bitmap.getWidth() / 2.0f;
                float top = pageHeight / 2.0f - bitmap.getHeight() / 2.0f;

                paint.setColor(Color.BLUE);
                canvas.drawBitmap(bitmap, left, top, null);
                document.finishPage(page);

            }

            //todo: give file name as per naming conventions
            File rootDirOfApp = LocalFilesAndFolder.getRootDirOfApp();
            File pdfFile;

            if (rootDirOfApp != null) {

                if (docName.replace("\n", "").length() > 30) {
                    docName = docName.replace("\n", "").substring(0, 29);
                }

                pdfFile = new File(rootDirOfApp.getAbsolutePath()
                        + File.separator
                        + docName + ".pdf");
                Log.e("PDF Path : ", pdfFile.toString());

                if (!pdfFile.getParentFile().exists())
                    pdfFile.getParentFile().mkdirs();
                if (!pdfFile.exists())
                    pdfFile.createNewFile();

                document.writeTo(new FileOutputStream(pdfFile));
                Log.e("created ", "pdf");
//                Toast.makeText(this, "created pdf", Toast.LENGTH_SHORT).show();

                //close document
                document.close();
                Log.e("path returning", pdfFile.getAbsolutePath());
//                hideProgress();
                return pdfFile.getAbsolutePath();

            } else {
                Log.e("root dir", "null");
//                hideProgress();
                return null;
            }


        } catch (IOException e) {
            e.printStackTrace();
            Log.e("File error : ", e.toString());
            Toast.makeText(this, "Pdf not created.", Toast.LENGTH_LONG).show();
//            hideProgress();
            return null;
        } catch (OutOfMemoryError outOfMemoryError) {
            outOfMemoryError.printStackTrace();
            Toast.makeText(this, "Device on low memory", Toast.LENGTH_LONG).show();
//            hideProgress();
            return null;
        }
    }

    public String createPDFPdfBox(String docName) {

        List<DriveDocModel> driveDocModels = adapter.driveDocModels;

        try {

            PDDocument document = new PDDocument();

            for (int i = 0; i < driveDocModels.size(); i++) {
                DriveDocModel driveDocModel = driveDocModels.get(i);


                InputStream targetStream = new FileInputStream(new File(driveDocModel.getImagePath()).getPath());
                PDImageXObject ximage = JPEGFactory.createFromStream(document, targetStream);

                PDPage page = new PDPage(new PDRectangle(ximage.getWidth(), ximage.getHeight()));
                document.addPage(page);

                PDPageContentStream contentStream;
                contentStream = new PDPageContentStream(document, page);

                contentStream.drawImage(ximage, 0, 0);
                contentStream.close();


//                float bitmapHeight = (float) bitmap.getHeight();
//                float bitmapWidth = (float) bitmap.getWidth();
//            Log.e("bitmap width", String.valueOf(bitmapWidth));
//            Log.e("bitmap width", String.valueOf(bitmapHeight));

                //Create an A4 sized page 595 x 842 in Postscript points.
//                int pageWidth = 595;
//                int pageHeight = 842;
//            float ratioMax = (float) pageWidth / (float) pageHeight;
//            Log.e("page ratio", String.valueOf(ratioMax));

//                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
//                PdfDocument.Page page = document.startPage(pageInfo);
//
//                Canvas canvas = page.getCanvas();
//
//                Paint paint = new Paint();
//                paint.setColor(Color.parseColor("#ffffff"));
//                canvas.drawPaint(paint);
//
//                float aspect_ratio = bitmapWidth / bitmapHeight;
//            float pdfAspectRatio;
//            Log.e("aspect ratio", String.valueOf(aspect_ratio));
//


//                float left = pageWidth / 2.0f - bitmap.getWidth() / 2.0f;
//                float top = pageHeight / 2.0f - bitmap.getHeight() / 2.0f;
//
//                paint.setColor(Color.BLUE);
//                canvas.drawBitmap(bitmap, left, top, null);
//                document.finishPage(page);

            }

            //todo: give file name as per naming conventions
            File rootDirOfApp = LocalFilesAndFolder.getRootDirOfApp();
            File pdfFile;

            if (rootDirOfApp != null) {

                if (docName.replace("\n", "").length() > 30) {
                    docName = docName.replace("\n", "").substring(0, 29);
                }

                String path = rootDirOfApp.getAbsolutePath()
                        + File.separator
                        + docName + ".pdf";

                pdfFile = new File(path);
                Log.e("PDF Path : ", pdfFile.toString());

                if (!pdfFile.getParentFile().exists())
                    pdfFile.getParentFile().mkdirs();
//                if (!pdfFile.exists())
//                    pdfFile.createNewFile();

                document.save(path);
                System.out.println("PDF created");
                document.close();

                return path;
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

//    public void createPDF() throws FileNotFoundException, DocumentException {
//
//        //Create document file
//        Document document = new Document();
//        try {
//            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss a");
//            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmm");
//            FileOutputStream outputStream = new FileOutputStream(
//                    new File(Environment.getExternalStoragePublicDirectory(
//                            Environment.DIRECTORY_DOWNLOADS) + "/AndroPDF"
//                            , "AndroPDF_" + dateFormat.format(Calendar.getInstance().getTime()) + ".pdf"
//                    ));
//
//            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
//
//            //Open the document
//            document.open();
//
//            document.setPageSize(PageSize.A4);
//            document.addCreationDate();
//            document.addAuthor("AndroPDF");
//            document.addCreator("http://chonchol.me");
//
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


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
//                    if(!imageFile.exists()){ = new ProgressDialog(this);
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

    private void setDragDropAdapter() {

        adapter = new DragDropListAdapter(DragDropActivity.this, driveDocModelList, this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(DragDropActivity.this));

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        recyclerView.setHasFixedSize(true);

        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }


        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

    }


    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, this);
    }

    private void addDocument(File file, CardDetail cardDetail, String tag,
                             String subFolderName, String originalImageFilePath,
                             String public_guid, String fileName, String pdfFilePath,
                             boolean saveForNextPages) {

        Log.e("pdf file path", pdfFilePath);

        String strCardDetails = Globalarea.getStringOfCardDetails(cardDetail);
        if (strCardDetails != null && file != null) {
            Log.i("adding to localDB", strCardDetails);
            DriveDocModel driveDocModel = new DriveDocModel(public_guid, file.getAbsolutePath(), originalImageFilePath, strCardDetails, cardDetail.getWhichcard(), SyncStatus.unsynced.toString());
            driveDocModel.setFileName(fileName);
            driveDocModel.setPdfFilePath(pdfFilePath);

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

            if (added > 0) {
                Toast.makeText(DragDropActivity.this, "Successfully stored document.", Toast.LENGTH_SHORT).show();
                //todo: handle different ways for both new scan and more page scan
//                prepareUploadInDrive(file, cardDetail, tag, subFolderName);

                if (saveForNextPages) {
//                    hideProgress();
                    Intent intent = new Intent(DragDropActivity.this, CommonScan.class);
                    intent.putExtra(CommonScan.SCANNER_TYPE, tag);
                    startActivity(intent);
                    this.finish();
                } else
                    navigateToFiles();
            } else {
                Toast.makeText(DragDropActivity.this, "Error storing document.", Toast.LENGTH_SHORT).show();
            }
        }
        hideProgress();
    }

    private void navigateToFiles() {

//        hideProgress();
        Intent intent = new Intent(this, NewHomeActivity.class);
        intent.putExtra(Constants.SAVED_CARD, "true");
        intent.putExtra(Constants.START_FRAGMENT, Constants.FILES_TAG);
        startActivity(intent);
        finish();
        finishAffinity();
    }


//    public void navigateActivity() {
////        Log.e("TAG CAMERA Is",data.getStringExtra("TAG_CAMERA"));
//        Intent intent = new Intent(this, CardScanActivity.class);
//        intent.putExtra("TAG_CAMERA", Constants.document);
//        startActivity(intent);
//        finish();
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

    @Override
    public void onResume() {
        super.onResume();

        if (Scanner.getInstance().getPreferences().isProActive()) {
            adView.setVisibility(View.GONE);
        } else {
            adView.setVisibility(View.VISIBLE);
        }

        if (adView != null) {
            adView.resume();
        }
        showDriveNote();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
