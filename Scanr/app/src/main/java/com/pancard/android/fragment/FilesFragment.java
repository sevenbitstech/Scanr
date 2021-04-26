package com.pancard.android.fragment;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.docscan.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.newflow.activity.DocumentDetailsActivity;
import com.pancard.android.activity.newflow.activity.DragDropActivity;
import com.pancard.android.activity.newflow.activity.NewHomeActivity;
import com.pancard.android.adapter.StaggeredGridAdapter;
import com.pancard.android.database.DriveDocRepo;
import com.pancard.android.database.SyncStatus;
import com.pancard.android.listener.AdapterDelegate;
import com.pancard.android.model.CardDetail;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PermissionManager;
import com.pancard.android.utility.PreferenceManagement;
import com.pancard.android.utility.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilesFragment extends Fragment implements AdapterDelegate {

    private static final int GALLERY_PERMISSION_CODE = 102;
    //    private Button btnDocDetails;
    private String[] permissionsStorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ImageView imgBack, img_close_button;
    private PermissionManager permissionManager;
    private ImageView imgClearSearch;
    private PreferenceManagement preferences;
    private RecyclerView rvItemList;
    //    private GridView gridView;
    private TextView tvDriveNote;
    private StaggeredGridLayoutManager gridLayoutManager;
    private StaggeredGridAdapter staggeredGridAdapter;
    private DriveDocRepo driveDocRepo;
    private List<DriveDocModel> driveDocList;
    private TextView tvNoFiles;
    private ImageView imgShare, imgCreatePdf, imgSequence, imgDelete;
    private DriveServiceHelper mDriveServiceHelper;
    private EditText etSearchView;
    private AdView adView;
    private AdRequest adRequest;

    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        bindViews(view);
        initialise();
        return view;
    }

    private void bindViews(View view) {
//        btnDocDetails = view.findViewById(R.id.btn_open_document_detail);
//        gridView = view.findViewById(R.id.gridview);
        rvItemList = view.findViewById(R.id.rvItemList);
        tvNoFiles = view.findViewById(R.id.tv_no_files);
        imgShare = view.findViewById(R.id.img_share);
        imgDelete = view.findViewById(R.id.img_delete);
        imgSequence = view.findViewById(R.id.img_pdf);
        tvDriveNote = view.findViewById(R.id.tv_note_drive);
        imgBack = view.findViewById(R.id.img_back_button);
        adView = view.findViewById(R.id.ad_view);
        img_close_button = view.findViewById(R.id.img_close_button);
        permissionManager = new PermissionManager(getActivity());
        etSearchView = view.findViewById(R.id.et_search);
        imgClearSearch = view.findViewById(R.id.img_clear_search);
        imgCreatePdf = view.findViewById(R.id.img_pdf_create);
    }

    private void initialise() {
        driveDocList = new ArrayList<>();
//        btnDocDetails.setOnClickListener(view -> {
//            Intent intent = new Intent(getActivity(), DocumentDetailsActivity.class);
//            startActivity(intent);
//        });

        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        ArrayList productList1 = new ArrayList<>();
        setupListView(productList1);
        staggeredGridAdapter.setAdapterDelegate(this);

        preferences = Scanner.getInstance().getPreferences();
        driveDocRepo = new DriveDocRepo(getContext());

        displayListItem();

        imgBack.setOnClickListener(view -> {
            if (getActivity() instanceof NewHomeActivity)
                getActivity().onBackPressed();
        });
        img_close_button.setOnClickListener(view -> {
            if (getActivity() instanceof NewHomeActivity)
                clearSelection();
        });

        etSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {

                if (count > 0) {
                    imgClearSearch.setVisibility(View.VISIBLE);
                } else {
                    imgClearSearch.setVisibility(View.GONE);
                }

                if (staggeredGridAdapter != null) {
                    staggeredGridAdapter.getFilter().filter(text);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSearchView.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (getActivity() != null)
                    Utils.hideKeyBoard(getActivity(), etSearchView);

                if (staggeredGridAdapter != null)
                    staggeredGridAdapter.getFilter().filter(etSearchView.getText().toString());
            }
            return false;
        });

        imgClearSearch.setOnClickListener(v -> etSearchView.setText(""));

        imgCreatePdf.setOnClickListener(view -> {

//            if(staggeredGridAdapter.getSelectedCount()>0){
//                gotoMultiplePdfDownload();
//            }else {
            staggeredGridAdapter.setSelectionEnabled(true);
            showMultipleSelectionMenu();

            Animation sgAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_grow_shrink);
            imgSequence.startAnimation(sgAnimation);
//                imgCreatePdf.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.confirm_36));
//            }
        });

        initDriveSync();
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

    public void initDriveSync() {
        if (new ConnectionDetector(getActivity()).isConnectingToInternet()) {
            preferences = Scanner.getInstance().getPreferences();
            setGoogleDriveService(Scanner.getInstance().getApplicationContext());
            if (mDriveServiceHelper != null) {
                Globalarea.setDbToDriveSync(mDriveServiceHelper);
            }
        } else {
            Log.i("Not Connected", "Internet");
        }
    }

    private void setGoogleDriveService(Context context) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (preferences.isDriveConnected()) {
            if (account != null) {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        context, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());
                com.google.api.services.drive.Drive googleDriveService =
                        new com.google.api.services.drive.Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName(context.getString(R.string.app_name))
                                .build();

                mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                Log.e("drive service", "helper setup");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        showDriveNote();

        checkForSubscribedUserAds();

        if (adView != null) {
            adView.resume();
        }

        if (staggeredGridAdapter != null) {
            if (!isMultipleSelectionEnabled()) {
                displayListItem();
            }
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

    private void showDriveNote() {
        Globalarea.getNoteTextView(tvDriveNote, getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void displayListItem() {
        getLocalDocumentsDetails();
    }


    private void getLocalDocumentsDetails() {

        //todo: do something with multiple pages i.e. folder name with _

        List<DriveDocModel> driveDocModelList = driveDocRepo.getAllDriveDocs();
        List<DriveDocModel> currentDocModelList = new ArrayList<>();

        if (driveDocModelList != null) {
            Log.e("size", String.valueOf(driveDocModelList.size()));

            Gson gson = new Gson();
            TypeToken<CardDetail> token = new TypeToken<CardDetail>() {
            };
            for (DriveDocModel driveDocModel : driveDocModelList) {

                CardDetail cardDetail = gson.fromJson(driveDocModel.getJsonText(), token.getType());
                if (cardDetail != null) {
                    driveDocModel.setCardDetail(cardDetail);
                    currentDocModelList.add(driveDocModel);
                }
            }

            if (currentDocModelList.size() > 0) {
                rvItemList.setVisibility(View.VISIBLE);
                setDocumentsWithData(currentDocModelList);
                setupAdapter();
            } else {
                rvItemList.setVisibility(View.GONE);
                showNoFiles();
            }

        }
    }

    private void showNoFiles() {
        rvItemList.setVisibility(View.GONE);
        rvItemList.setVisibility(View.GONE);
        tvNoFiles.setVisibility(View.VISIBLE);
    }

    private void hideNoFiles() {
        rvItemList.setVisibility(View.VISIBLE);
        rvItemList.setVisibility(View.VISIBLE);
        tvNoFiles.setVisibility(View.GONE);
    }

    private void setDocumentsWithData(List<DriveDocModel> googleDriveFileHolderList) {

        Log.e("setting docs", "with data");
        List<DriveDocModel> newDriveDocModels = new ArrayList<>();
        if (googleDriveFileHolderList != null) {
            for (DriveDocModel googleDriveFileHolder : googleDriveFileHolderList) {
                if (googleDriveFileHolder != null) {
                    newDriveDocModels.add(googleDriveFileHolder);
                }
            }
        }
        driveDocList.clear();
        driveDocList.addAll(newDriveDocModels);

    }

    private void setupAdapter() {

        if (driveDocList != null && driveDocList.size() > 0) {
            Collections.reverse(driveDocList);
            setupListView(driveDocList);
            staggeredGridAdapter.setAdapterDelegate(this);
            rvItemList.setLayoutManager(gridLayoutManager);
            rvItemList.setAdapter(staggeredGridAdapter);

            staggeredGridAdapter.notifyDataSetChangedWithFilter(etSearchView.getText().toString().trim(), areZeroFiles -> {
                if (areZeroFiles)
                    showNoFiles();
                else
                    hideNoFiles();
            });

            if (isMultipleSelectionEnabled())
                showMultipleSelectionMenu();
            else
                hideMultipleSelectionMenu();

            multipleMenuItemClickListener();
            rvItemList.setVisibility(View.VISIBLE);
        } else {
            Log.e("no files", "in the db");
            hideMultipleSelectionMenu();
            showNoFiles();
            rvItemList.setVisibility(View.GONE);
        }
    }

    private void multipleMenuItemClickListener() {
        imgSequence.setOnClickListener(view -> {
            gotoMultiplePdfDownload();

//            Toast.makeText(getActivity(), "Coming soon...", Toast.LENGTH_SHORT).show();
        });
        imgShare.setOnClickListener(view -> multipleShareFiles());
        imgDelete.setOnClickListener(view -> mutipleDelete());
    }

    private void gotoMultiplePdfDownload() {
        List<DriveDocModel> selectedModels = staggeredGridAdapter.getSelectedItems();

        if (selectedModels != null && selectedModels.size() > 0) {
//            for (DriveDocModel selectedModel:selectedModels) {
//                Log.e("My drivedoc",selectedModel.getFolderName());
//            }
            String jsonSelectedFiles = Globalarea.getStringOfDriveDocModelList(selectedModels);
            if (jsonSelectedFiles != null) {
                Intent intent = new Intent(getActivity(), DragDropActivity.class);
                intent.putExtra(Constants.FILES_SELECTED, jsonSelectedFiles);
                intent.putExtra(Constants.KEY_OCR, false);
                startActivity(intent);
                Log.e("selected items", jsonSelectedFiles);
                Log.e("total items selcted", String.valueOf(selectedModels.size()));
                clearSelection();
            }
        } else {
            Toast.makeText(getActivity(), "Please select some images to arrange pages and create PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void mutipleDelete() {

        List<DriveDocModel> selectedModels = staggeredGridAdapter.getSelectedItems();

        Log.e("total items selcted", String.valueOf(selectedModels.size()));
        new MaterialAlertDialogBuilder(getActivity())
                .setMessage("Are you sure you want to Delete " + selectedModels.size() + " files?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, id) -> {
                            if (selectedModels.size() > 0) {
                                Globalarea.actionFire = true;
                                callLocalDeleteDocument(selectedModels);
//                                deleteItem.clear();
//                                displayListItem();
//                                mode.finish();
                            }
                        }).setNegativeButton("No", null).show();
    }

    private void multipleShareFiles() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Files");
        intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

        ArrayList<Uri> files = new ArrayList<>();

        List<DriveDocModel> selectedModels = staggeredGridAdapter.getSelectedItems();

        for (DriveDocModel driveDocModel : selectedModels /* List of the files you want to send */) {
            File file = new File(driveDocModel.getImagePath());

            Uri uri = FileProvider.getUriForFile(getActivity(), "com.docscan.android.provider", file);


//            Uri uri = Uri.fromFile(file);
            files.add(uri);
        }

        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        startActivity(intent);
        clearSelection();
    }

    private void setupListView(List<DriveDocModel> driveDocModelList) {
//        staggeredGridAdapter = new StaggeredGridAdapter(getContext(), driveDocList,);
        String[] popupItems = {"Share", "Delete"};
        staggeredGridAdapter = new StaggeredGridAdapter(getActivity(), driveDocModelList,
                (position, driveDocModel, id, fileName) -> {
                    CardDetail cardDetail = Globalarea.getCardDetailOfString(driveDocModel.getJsonText());
                    switch (id) {
                        case 0:

//                            if (cardDetail.bitmap == null)
//                                Log.e("Download Image", cardDetail.card_name);

                            if (driveDocModel.getImagePath() != null) {
                                Bitmap bitmap = BitmapFactory.decodeFile(new File(driveDocModel.getImagePath()).getPath());
                                if (bitmap != null)
                                    checkPermissionAndSavePdf(bitmap, fileName, driveDocModel);
                                else {
                                    Toast.makeText(getActivity(), "Sorry! We can not create PDF from the non-existent image.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Sorry! We can not create the PDF. The image path can not be found.", Toast.LENGTH_SHORT).show();
                            }

//                    openEditDetailsActivity(employee,isMainnet);
                            break;
                        case 1:
                            Log.e("Share Image", "Yes");
//                    openVerifyQRActivity(employee);

                            Globalarea.shareData(getActivity(), cardDetail, driveDocModel);
                            break;
                        case 2:
                            Log.e("Delete Image", "Yes");
//                    openArchivedTimesheet(employee);
                            showDeleteDialog(driveDocModel);
                            break;
                    }
                });
    }

    public void showDeleteDialog(DriveDocModel driveDocModel) {
        new MaterialAlertDialogBuilder(getActivity())
                .setMessage(getString(R.string.confirm_delete))
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, id) -> {
                            Globalarea.actionFire = true;
                            callLocalDeleteDocument(driveDocModel);

                        }).setNegativeButton("No", null).show();
    }

    private void callLocalDeleteDocument(DriveDocModel driveDocModel) {
        if (driveDocModel != null) {
            driveDocModel.setSyncStatus(SyncStatus.deleted.toString());
            int deleted = driveDocRepo.addDriveDocInfo(driveDocModel);
            if (deleted > 0) {
                Toast.makeText(getActivity(), "Document Deleted Successfully.", Toast.LENGTH_SHORT).show();
                displayListItem();
            }
        }
    }

    private void callLocalDeleteDocument(List<DriveDocModel> driveDocModelsTobeDeleted) {
        if (driveDocModelsTobeDeleted != null && driveDocModelsTobeDeleted.size() > 0) {
            for (DriveDocModel driveDocModel : driveDocModelsTobeDeleted) {
                driveDocModel.setSyncStatus(SyncStatus.deleted.toString());
                int deleted = driveDocRepo.addDriveDocInfo(driveDocModel);
                if (deleted > 0) {
                    Toast.makeText(getActivity(), "Files Deleted Successfully.", Toast.LENGTH_SHORT).show();
                    clearSelection();
                    displayListItem();
                }
            }
        }
    }

    @Override
    public void onClick(View view, int position) {

    }

//    @Override
//    public void onClicked(View view, Object object) {
//
//    }

    @Override
    public void onClicked(View view, Object object, int position) {
        DriveDocModel driveDocModel = (DriveDocModel) object;

        Intent intent = new Intent(getActivity(), DocumentDetailsActivity.class);
        intent.putExtra(Constants.KEY_DRIVE_DOC, driveDocModel);
        startActivity(intent);

    }

    @Override
    public void onLongClick(View view, Object object, int position) {
        showMultipleSelectionMenu();
    }

    public boolean isMultipleSelectionEnabled() {
        return staggeredGridAdapter.isSelectionEnabled();
    }

    public void clearSelection() {
        hideMultipleSelectionMenu();
        staggeredGridAdapter.unselectAllItem();
        img_close_button.setVisibility(View.GONE);
        imgBack.setVisibility(View.VISIBLE);
    }

    public void showMultipleSelectionMenu() {
        img_close_button.setVisibility(View.VISIBLE);
        imgBack.setVisibility(View.GONE);

        imgSequence.setVisibility(View.VISIBLE);
        imgShare.setVisibility(View.VISIBLE);
        imgDelete.setVisibility(View.VISIBLE);
        imgCreatePdf.setVisibility(View.GONE);

    }

    public void hideMultipleSelectionMenu() {
        imgSequence.clearAnimation();

        imgSequence.setVisibility(View.GONE);
        imgShare.setVisibility(View.GONE);
        imgDelete.setVisibility(View.GONE);

        imgCreatePdf.setVisibility(View.VISIBLE);
    }
//    @Override
//    public void onLongClicked(View view, Object object) {
//
//    }


    private void createPdf(Bitmap bitmap, String documentName, DriveDocModel driveDocModel) {
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
                createNewPdf(bitmap, documentName, direct);
//                Toast.makeText(getActivity(), "pdf file does not exist", Toast.LENGTH_SHORT).show();
            }

        } else {
            createNewPdf(bitmap, documentName, direct);
        }
    }

    private void createNewPdf(Bitmap bitmap, String documentName, File direct) {
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
            addNotification("Download Successfully", "Open the Scanr folder from download folder in your file manager and check the downloaded PDF there.",
                    file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("File error : ", e.toString());
            Toast.makeText(getActivity(), "Pdf is not downloaded yet, please check your permission.", Toast.LENGTH_LONG).show();
            addNotification("Download Failed", "Pdf is not downloaded yet, please try again.", null);
        }

        // close the document
        document.close();
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

    private void addNotification(String tital, String text, String filepath) {
        Log.e("notification display :", text);
        try {
//            Uri path = Uri.fromFile(new File(filepath));
            Uri path = FileProvider.getUriForFile(getActivity(), "com.docscan.android.provider", new File(filepath));
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

    public void checkPermissionAndSavePdf(Bitmap bitmap, String documentName, DriveDocModel driveDocModel) {

        if (permissionManager.hasPermissions(permissionsStorage)) {
            createPdf(bitmap, documentName, driveDocModel);
        } else {
            if (!preferences.isShowedStoragePermissionDialog() || permissionManager.shouldRequestPermission(getActivity(), permissionsStorage))
                permissionManager.showPermissionRequireDialog(getActivity(), getResources().getString(R.string.storage_permission_for),
                        new PermissionManager.OnPermissionRequireDialog() {
                            @Override
                            public void onAcceptPermissionDialog() {
                                Log.e("go permission", "yes");
                                if (!preferences.isShowedStoragePermissionDialog() || permissionManager.shouldRequestPermission(getActivity(), permissionsStorage)) {
                                    permissionManager.requestPermissions(FilesFragment.this, permissionsStorage, GALLERY_PERMISSION_CODE);
                                } else {
                                    permissionManager.openSettingDialog(getActivity(), getResources().getString(R.string.storage_permission_for));
                                }
                            }

                            @Override
                            public void onCancelPermissionDialog() {
                                Log.e("go permission", "no");
                            }
                        });

            else
                permissionManager.openSettingDialog(getActivity(), getResources().getString(R.string.storage_permission_access));
        }
    }
}
