package com.pancard.android.activity.newflow.activity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.database.DriveDocRepo;
import com.pancard.android.database.FileVersion;
import com.pancard.android.database.SyncStatus;
import com.pancard.android.fragment.TextDetailBottomSheet;
import com.pancard.android.model.CardDetail;
import com.pancard.android.receiver.ConnectivityChangeReceiver;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.GlideApp;
import com.pancard.android.utility.PermissionManager;
import com.pancard.android.utility.PreferenceManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class DocumentDetailsActivity extends AppCompatActivity {

    private static final int GALLERY_PERMISSION_CODE = 102;
    ImageView documentImage;
    View bottomSheet;
    AdView adView;
    AdRequest adRequest;
    ImageView imgUpArrow;
    DriveDocRepo driveDocRepo;
    DriveDocModel driveDocModel;
    CardDetail cardDetail;
    Toolbar toolbar;
    TextView tvTitle;
    ImageView imgBack;
    ImageView imgShare, imgDelete, imgPdf;
    PermissionManager permissionManager;
    String[] permissionsStorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    boolean isSharing;
    private TextView tvDriveNote;
    private ConnectivityChangeReceiver connectivityChangeReceiver;
    private PreferenceManagement preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_details);
        bindViews();
        initialise();

    }

    private void bindViews() {
        documentImage = findViewById(R.id.img_doc_image);
        bottomSheet = findViewById(R.id.bottom_sheet);
        adView = findViewById(R.id.ad_view);
        imgUpArrow = findViewById(R.id.img_up_arrow);
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title_text);
        imgBack = findViewById(R.id.img_back_button);
        tvDriveNote = findViewById(R.id.tv_note_drive);
        imgShare = findViewById(R.id.img_share);
        imgDelete = findViewById(R.id.img_delete);
        imgPdf = findViewById(R.id.img_pdf);
    }

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, DocumentDetailsActivity.this);
    }

    private void initialise() {
        setSupportActionBar(toolbar);

        preferences = Scanner.getInstance().getPreferences();
        permissionManager = new PermissionManager(this);
        driveDocRepo = new DriveDocRepo(this);
        cardDetail = new CardDetail();
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);

        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if (getIntent() != null && getIntent().getSerializableExtra(Constants.KEY_DRIVE_DOC) != null) {
            driveDocModel = (DriveDocModel) getIntent().getSerializableExtra(Constants.KEY_DRIVE_DOC);
        }
//        List<DriveDocModel> driveDocModelList = driveDocRepo.getAllDriveDocs();
//        if (driveDocModelList != null && driveDocModelList.size() > 0) {
//            driveDocModel = driveDocModelList.get(0);
//        }
        else {
            Toast.makeText(this, "No file found", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }

        showDocumentDetail();

        imgBack.setOnClickListener(view -> onBackPressed());
        imgUpArrow.setOnClickListener(view -> showBottomSheet());
        imgShare.setOnClickListener(view -> shareData());
        imgDelete.setOnClickListener(view -> showDeleteDialog());
        imgPdf.setOnClickListener(view -> checkPermissionAndSavePdf());
//        documentImage.setOnClickListener(view -> showBottomSheet());
    }

    private void showDocumentDetail() {

//        if (driveDocModel.getCardDetail() != null) {
//            cardDetail = driveDocModel.getCardDetail();
//        }

        String fileName = driveDocModel.getFileName();
        if (driveDocModel.getJsonText() != null) {
            Gson gson = new Gson();
            cardDetail = gson.fromJson(driveDocModel.getJsonText(), CardDetail.class);

            String cardData = cardDetail.getCard_name();
            if (cardData.contains(Constants.KEY_OCR_OFF_SCAN)) {
                String displayString = driveDocModel.getWhichCard() + "_" + FileVersion.CROPPED.toString() + "_" + driveDocModel.getPublicGuid();
                if (fileName != null)
                    tvTitle.setText(fileName);
                else
                    tvTitle.setText(displayString);
                imgUpArrow.setVisibility(View.GONE);
            } else {
                String displayName = cardDetail.get2WordOr15CharDisplayString();

//            String[] words = displayName.split(" ");
//
//            String twoWords="";
//            if(words.length>2){
//                twoWords = words[0]+" "+words[1];
//                int totalCharSize = twoWords.length();
//
//                displayName = twoWords.substring(0, Math.min(twoWords.length() - 1, 15));
//
//            }else {
//                displayName = displayName.substring(0, Math.min(displayName.length() - 1, 15));
//            }

                String displayString = driveDocModel.getWhichCard() + "_" + FileVersion.CROPPED.toString() + "_" + driveDocModel.getPublicGuid();
                if (fileName != null)
                    tvTitle.setText(fileName);
                else
                    tvTitle.setText(displayName);
                imgUpArrow.setVisibility(View.VISIBLE);

            }

        } else {
            String displayString = driveDocModel.getWhichCard() + "_" + FileVersion.CROPPED.toString() + "_" + driveDocModel.getPublicGuid();

            if (fileName != null)
                tvTitle.setText(fileName);
            else
                tvTitle.setText(displayString);

            imgUpArrow.setVisibility(View.GONE);

        }

        if (driveDocModel.getImagePath() != null) {
            GlideApp.with(this)
                    .load(new File(driveDocModel.getImagePath()))
                    .placeholder(R.drawable.logo_icon)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .thumbnail(0.1f)
                    .into(documentImage);
        } else {
            documentImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.logo_icon));
        }


    }

    private String getAllCardDetails() {
        Log.e("getting details", "ys");
        String allCardDetails = "";
        if (cardDetail != null) {
            if (cardDetail.getCard_name() != null) {

                allCardDetails = allCardDetails + cardDetail.getCard_name();
            }

//            if (cardDetail.getCard_unique_no() != null) {
//                allCardDetails = allCardDetails + "\n" + cardDetail.getCard_unique_no();
//            }
//
////        if(cardDetail.getIssue_date() != null) {
////            allCardDetails = allCardDetails + cardDetail.getIssue_date();
////        }
//
//            if (cardDetail.getIssue_address() != null) {
//                allCardDetails = allCardDetails + "\n" + cardDetail.getIssue_address();
//            }
//
//            if (cardDetail.getDate_of_birth() != null) {
//                allCardDetails = allCardDetails + "\n" + cardDetail.getDate_of_birth();
//            }
//
//            if (cardDetail.getBirth_place() != null) {
//                allCardDetails = allCardDetails + "\n" + cardDetail.getBirth_place();
//            }
//
//            if (cardDetail.getScan_time() != null) {
//                allCardDetails = allCardDetails + "\n" + cardDetail.getScan_time();
//            }
//
//            if (cardDetail.getTill_date() != null) {
//                allCardDetails = allCardDetails + cardDetail.getTill_date() + "\n";
//            }

        } else {

            Log.e("card detail", "is null");
        }

        return allCardDetails;
    }

    public void showBottomSheet() {

        String allCardDetails = getAllCardDetails();
        if (allCardDetails != null && allCardDetails.trim().length() > 0) {
            TextDetailBottomSheet textDetailBottomSheet = TextDetailBottomSheet.newInstance(allCardDetails);
            textDetailBottomSheet.setEditListener(new TextDetailBottomSheet.OnUpdateDocument() {

                @Override
                public void onUpdateDocument(String oldText, String newText) {
                    textDetailBottomSheet.dismiss();
                    if (driveDocModel != null) {
                        cardDetail.setCard_name(newText);
                        String jsonCardDetails = Globalarea.getStringOfCardDetails(cardDetail);
                        driveDocModel.setJsonText(jsonCardDetails);
                        driveDocModel.setSyncStatus(SyncStatus.updated.toString());
                        int updated = driveDocRepo.updateDriveDoc(driveDocModel);
                        if (updated > 0) {
                            Toast.makeText(DocumentDetailsActivity.this, "Document updated Successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            textDetailBottomSheet.show(getSupportFragmentManager(), Constants.TEXT_DETAIL_BOTTOM_SHEET_TAG);
        } else {
            Toast.makeText(this, "No text with the image", Toast.LENGTH_SHORT).show();
        }

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels;
//        int newHeight = (int)(height*0.75);
//
//
//        BottomSheetBehavior sheetBehavior = BottomSheetBehavior.from(bottomSheet);
////        sheetBehavior.setPeekHeight(newHeight);
////        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//
//
//        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
//            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
////            btn_bottom_sheet.setText("Close sheet");
//        } else {
//            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
////            btn_bottom_sheet.setText("Expand sheet");
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDriveNote();

        if (Scanner.getInstance().getPreferences().isProActive()) {
            adView.setVisibility(View.GONE);
        } else {
            adView.setVisibility(View.VISIBLE);
        }

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
//        unregisterReceiver(broadcastReceiver);
        //todo: Uncommnet below line unregister drive sync broadcast receiver

        unregisterReceiver(connectivityChangeReceiver);

        if (adView != null) {
            adView.destroy();
        }

        GlideApp.get(this).clearMemory();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_doc_detail, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//
//            case R.id.menu_share:
//                shareData();
//                return true;
//
//            case R.id.menu_delete:
//                showDeleteDialog();
//                return true;
//
//
//        }
//        return true;
//    }

    public void showDeleteDialog() {
        new MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.confirm_delete))
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, id) -> {
                            Globalarea.actionFire = true;
//                            Globalarea.SpecificCard = handler.getSingleRow(whichcard, Globalarea.SpecificCard.getScan_time());
//
//                            if (Globalarea.SpecificCard.getStatus().equals("true") || Globalarea.SpecificCard.getStatus().equals("update")) {
//                                handler.sqliteInsertData(new CardDetail(whichcard, Globalarea.SpecificCard.getScan_time(), Globalarea.SpecificCard.getImage_size()), Constants.delete, null);
//
//                            }
//                            File file = new File(cardDetail.getImage_url());
//                            if (file.exists()) {
//                                file.delete();
//                            }
//                            if (whichcard.equals(Constants.adharcard)) {
//                                File _file = new File(cardDetail.getIssue_date());
//                                if (_file.exists()) {
//                                    _file.delete();
//                                }
//                            }

//                            callDriveDeleteDocument();

                            callLocalDeleteDocument();

//                            PreferenceManagement preferences = Scanner.getInstance().getPreferences();
//                            preferences.setSizeDetail(preferences.getSizeDetail() - Globalarea.SpecificCard.getImage_size());

//                            handler.deletRowData(whichcard, Globalarea.SpecificCard.getScan_time());
//                                retriveandSetOneSection.deleteCardData(Globalarea.SpecificCard.getScan_time(), whichcard, Globalarea.sizeDetail.getAvailableSpace() + Globalarea.SpecificCard.getImage_size(), Globalarea.sizeDetail.getUsedSpace() - Globalarea.SpecificCard.getImage_size());
//                            moveActivity();
                        }).setNegativeButton("No", null).show();
    }

    public void checkPermissionAndSavePdf() {

        if (permissionManager.hasPermissions(permissionsStorage)) {
            createPdf();
        } else {
            if (!preferences.isShowedStoragePermissionDialog() || permissionManager.shouldRequestPermission(DocumentDetailsActivity.this, permissionsStorage))
                permissionManager.showPermissionRequireDialog(DocumentDetailsActivity.this, getResources().getString(R.string.storage_permission_for),
                        new PermissionManager.OnPermissionRequireDialog() {
                            @Override
                            public void onAcceptPermissionDialog() {
                                Log.e("go permission", "yes");
                                if (!preferences.isShowedStoragePermissionDialog() || permissionManager.shouldRequestPermission(DocumentDetailsActivity.this, permissionsStorage)) {
                                    permissionManager.requestPermissions(permissionsStorage, GALLERY_PERMISSION_CODE);
                                } else {
                                    permissionManager.openSettingDialog(DocumentDetailsActivity.this, getResources().getString(R.string.storage_permission_for));
                                }
                            }

                            @Override
                            public void onCancelPermissionDialog() {
                                Log.e("go permission", "no");
                            }
                        });

            else
                permissionManager.openSettingDialog(DocumentDetailsActivity.this, getResources().getString(R.string.storage_permission_access));
        }
    }

    private void callLocalDeleteDocument() {
        if (driveDocModel != null) {
            driveDocModel.setSyncStatus(SyncStatus.deleted.toString());
            int deleted = driveDocRepo.addDriveDocInfo(driveDocModel);
            if (deleted > 0) {
                Toast.makeText(this, "Document Deleted Successfully.", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }
    }

    private void shareData() {
        try {

            if (isSharing) {
                return;
            }

            isSharing = true;

            String shareBody;
            String subject;
//            if (whichcard.equals(Constants.businesscard)) {
//                shareBody = "Business Card Detail : " + edittext_name.getText().toString().trim();
//
//                subject = "Business Card Information";
//
//            } else if (whichcard.equals(Constants.document)) {
            shareBody = "Document Detail : " + getAllCardDetails();
            if (cardDetail.getCard_name().contains(Constants.KEY_OCR_OFF_SCAN)) {
                shareBody = "";
            }

            subject = "Document Information";

//            } else if (whichcard.equals(Constants.pancard)) {
//                shareBody = "PanCard Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
//                        + "\n" + " PanCard Number : " + cardDetail.getCard_unique_no();
//
//                subject = "PanCard Information";
//
//            } else if (whichcard.equals(Constants.creditCard)) {
//                shareBody = "Credit Card Type : " + cardDetail.getCard_name() + "\n" + " Expiry Date : " + cardDetail.getDate_of_birth()
//                        + "\n" + " Credit Card Number : " + cardDetail.getCard_unique_no();
//
//                subject = "PanCard Information";
//
//            } else if (whichcard.equals(Constants.licence)) {
//                shareBody = "Licence Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
//                        + "\n" + " Licence Number : " + cardDetail.getCard_unique_no()
//                        + "\n" + " Licence Issue Date : " + cardDetail.getIssue_date()
//                        + "\n" + " Licence Valid Till : " + cardDetail.getTill_date()
//                        + "\n" + " Address : " + cardDetail.getIssue_address();
//                subject = "Driving Licence Information";
//
//            } else if (whichcard.equals(Constants.passport)) {
//                shareBody = "Passport Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
//                        + "\n" + " Passport Number : " + cardDetail.getCard_unique_no()
//                        + "\n" + " Passport Issue Date : " + cardDetail.getIssue_date()
//                        + "\n" + " Passport Valid Till : " + cardDetail.getTill_date()
//                        + "\n" + " BirthCity : " + cardDetail.getBirth_place()
//                        + "\n" + " IssueCity : " + cardDetail.getIssue_address();
//
//                subject = "PassPort Information";
//
//            } else if (whichcard.equals(Constants.adharcard)) {
//                shareBody = "Adhar Card Holder's Name : " + cardDetail.getCard_name() + "\n" + " Date of Birth : " + cardDetail.getDate_of_birth()
//                        + "\n" + " Adhar Card Number : " + cardDetail.getCard_unique_no()
//                        + "\n" + " Adhar Card BirthCity : " + cardDetail.getBirth_place()
//                        + "\n" + " Adhar Card Address : " + cardDetail.getIssue_address();
////                Log.e("sharebodys",shareBody);
//                subject = "Adhar Card Information";
//
//            }

            File file = null;
            if (driveDocModel.getImagePath() != null) {
//                String[] urlSeparated = cardDetail.getImage_url().split("/");
//                String fileName = urlSeparated[urlSeparated.length-1];
//                file = new File(getCacheDir(),fileName);
                file = new File(driveDocModel.getImagePath());
            }

            ArrayList<Uri> files = new ArrayList<>();

            if (file != null) {
                Uri uri;

//                uri = Uri.fromFile(file);
                uri = FileProvider.getUriForFile(this, "com.docscan.android.provider", file);
//                uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);
                Log.i("File path", file.getPath());
                files.add(uri);

//                if (whichcard.equals(Constants.adharcard)) {
//                    if (cardDetail.getIssue_date() != null) {
//                        File _file = new File(cardDetail.getIssue_date());
//                        Uri _uri = Uri.fromFile(_file);
//                        files.add(_uri);
//                    }
//                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                startActivity(Intent.createChooser(shareIntent, "Share Via"));
                isSharing = false;
            } else {
                Toast.makeText(this, "File could not be found.", Toast.LENGTH_SHORT).show();
                isSharing = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            isSharing = false;
        }
    }

    private void createPdf() {
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

        File direct = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        String directPath = direct.getPath() + "/Scanr/";
        direct = new File(directPath);
        if (!direct.exists()) {
            direct.mkdirs();
        }

        if (driveDocModel.getPdfFilePath() != null) {

//            File file = new File(driveDocModel.getPdfFilePath());

            Log.e("pdf file path", driveDocModel.getPdfFilePath());

            File src = new File(driveDocModel.getPdfFilePath());
            File dst = new File(direct, src.getName());

            if (src.exists()) {
                copyFile(src, dst);
                addNotification("Download Successfully", "Open the Scanr folder from download folder in your file manager and check the downloaded PDF there.", dst.getAbsolutePath());
            } else {
                createNewPdf(direct);
                Toast.makeText(this, "pdf file does not exist", Toast.LENGTH_SHORT).show();
            }


        } else {
            createNewPdf(direct);
        }
    }

    private void createNewPdf(File direct) {
        CardDetail cardDetail = Globalarea.getCardDetailOfString(driveDocModel.getJsonText());
//        String documentName = cardDetail.card_name;
        String documentName = tvTitle.getText().toString();

        if (driveDocModel.getImagePath() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(new File(driveDocModel.getImagePath()).getPath());
            if (bitmap != null) {
                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);

                Canvas canvas = page.getCanvas();


                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#ffffff"));
                canvas.drawPaint(paint);

                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

                paint.setColor(Color.BLUE);
                canvas.drawBitmap(bitmap, 0, 0, null);
                document.finishPage(page);

                File file = getDownloadableFile(documentName, direct);

                try {
                    document.writeTo(new FileOutputStream(file));
                    addNotification("Download Successfully", "Open the Scanr folder from download folder in your file manager and check the downloaded PDF there.", file.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("File error : ", e.toString());
                    Toast.makeText(this, "Pdf is not downloaded yet, please check your permission.", Toast.LENGTH_LONG).show();
                    addNotification("Download Failed", "Pdf is not downloaded yet, please try again.", null);
                }

                // close the document
                document.close();

            } else {
                Toast.makeText(this, "Sorry! We can not create PDF from the non-existent image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Sorry! We can not create the PDF. The image path can not be found.", Toast.LENGTH_SHORT).show();
        }

    }

    private File getDownloadableFile(String documentName, File directory) {

        if (documentName.replace("\n", "").length() > 10) {
            documentName = documentName.replace("\n", "").substring(0, 9);
        }
        // write the document content
        String targetPdf = directory.getPath() + "/" + documentName.replace("\n", "") + ".pdf";
        File filePath = new File(targetPdf);
        if (!filePath.exists()) {
            try {
                filePath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    public void copyFile(File sourceFile, File destFile) {
        try {
            if (!destFile.getParentFile().exists())
                destFile.getParentFile().mkdirs();

            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            FileChannel source = null;
            FileChannel destination = null;

            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNotification(String title, String text, String filepath) {
        Log.e("notification display :", text);
        try {
//            Uri path = Uri.fromFile(new File(filepath));
            Uri path = FileProvider.getUriForFile(this, "com.docscan.android.provider", new File(filepath));
            Intent objIntent = new Intent(Intent.ACTION_VIEW);
            objIntent.setDataAndType(path, "application/pdf");
            objIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            objIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(objIntent);//Starting the pdf viewer

//        if (permissionManager.hasPermissions(permissionsStorage)) {

//            NotificationManager mNotificationManager;

//            NotificationCompat.Builder mBuilder =
//                    new NotificationCompat.Builder(getActivity(), "notify_001");
////        Intent ii = new Intent(getActivity(), NewHomeActivity.class);
////        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, ii, 0);
//
//
//            Uri path = Uri.fromFile(new File(filepath));
//            Intent objIntent = new Intent(Intent.ACTION_VIEW);
//            objIntent.setDataAndType(path, "application/pdf");
//            objIntent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
////            startActivity(objIntent);//Starting the pdf viewer
//
//
////            Intent intent = new Intent();
////            intent.setAction(Intent.ACTION_VIEW);
////            File file = new File(filepath); //
////            intent.setDataAndType(Uri.fromFile(file), "application/pdf"); //
//            PendingIntent pIntent = PendingIntent.getActivity(getActivity(), 0, objIntent, 0);
//
//            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
//            bigText.bigText(text);
////        bigText.setBigContentTitle("Today's Bible Verse");
//            bigText.setSummaryText(text);
//
//            mBuilder.setContentIntent(pIntent);
//            mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
//            mBuilder.setContentTitle(tital);
//            mBuilder.setContentText(text);
//            mBuilder.setSmallIcon(R.drawable.ds_logo);
//            mBuilder.setPriority(Notification.PRIORITY_MAX);
//            mBuilder.setStyle(bigText);
//
//            mNotificationManager =
//                    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
//
//// === Removed some obsoletes
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                String channelId = "Your_channel_id";
//                NotificationChannel channel = new NotificationChannel(
//                        channelId,
//                        "Channel human readable title",
//                        NotificationManager.IMPORTANCE_HIGH);
//                mNotificationManager.createNotificationChannel(channel);
//                mBuilder.setChannelId(channelId);
//            }
//
//            mNotificationManager.notify(0, mBuilder.build());
//        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}