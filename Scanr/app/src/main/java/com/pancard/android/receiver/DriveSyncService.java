package com.pancard.android.receiver;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.docscan.android.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.DriveOperations.MetadataModel;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.database.DriveDocRepo;
import com.pancard.android.database.SyncStatus;
import com.pancard.android.model.CardDetail;
import com.pancard.android.utility.FileOpration;
import com.pancard.android.utility.PreferenceManagement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DriveSyncService extends IntentService {

    private static final String TAG = "InternetService";
    //    private Context context;
    PreferenceManagement preferences;
    boolean isUploadingImage, isCreatingTextFile;
    List<DriveDocModel> mdriveDocModels;
    private DriveDocRepo driveDocRepo;
    private DriveServiceHelper mDriveServiceHelper;
    private MetadataModel metadataModel = null;

    public DriveSyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        initialize();

        checkConnection(intent);
    }

    private void initialize() {
        preferences = Scanner.getInstance().getPreferences();

        setGoogleDriveService();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            driveDocRepo = new DriveDocRepo(this, FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }

    private void checkConnection(Intent intent) {
        if (intent != null && intent.getExtras() != null && intent.getExtras().getBoolean("isNetworkConnected")) {
            Bundle extras = intent.getExtras();
            boolean isNetworkConnected = extras.getBoolean("isNetworkConnected");
            Log.d("internet ", isNetworkConnected + "");

            if (isNetworkConnected) {
                Log.e("Doing sync", "yes");
                getUnsyncDocAndUpload();
                getMetadataUnsyncedAndUpload();
                getDeletedAndUpdateMetadata();
                getDeletedMetadataAndUpdate();
            }
        }
    }

    private void getDeletedAndUpdateMetadata() {
        if (driveDocRepo != null) {
            List<DriveDocModel> deleteDriveDocModelList = driveDocRepo.getDocsBySyncStatus(SyncStatus.deleted.toString());

            if (deleteDriveDocModelList != null && deleteDriveDocModelList.size() > 0) {
                Log.i("Delete unsync", String.valueOf(deleteDriveDocModelList.size()));
                callDriveDeleteDocument(deleteDriveDocModelList);
            }
        }
    }

    private void getMetadataUnsyncedAndUpload() {
        if (driveDocRepo != null && preferences.isDriveConnected()) {

            List<DriveDocModel> driveDocModelList = driveDocRepo.getDocsBySyncStatus(SyncStatus.metadataUnsynced.toString());

            if (driveDocModelList != null && driveDocModelList.size() > 0) {
                Log.e("Metadata Unsynced Data", String.valueOf(driveDocModelList.size()));

                for (int i = 0; i < driveDocModelList.size(); i++) {
//                    updateTextFile(driveDocModelList.get(i).getCardDetail(),driveDocModelList.get(i).getFolderId(),i);
                    saveMetadata(driveDocModelList.get(i));
                }


            }
        }
    }

    private void getUnsyncDocAndUpload() {
        if (driveDocRepo != null) {

            List<DriveDocModel> driveDocModelList = driveDocRepo.getDocsBySyncStatus(SyncStatus.unsynced.toString());

            if (driveDocModelList != null && driveDocModelList.size() > 0) {
                Log.e("Unsynced Data", String.valueOf(driveDocModelList.size()));
                mdriveDocModels = driveDocModelList;
                uploadToDrive();
            }
        }
    }

    private void uploadToDrive() {
        if (preferences.isDriveConnected()) {

            for (int startCounter = 0; startCounter < mdriveDocModels.size(); startCounter++) {

                CardDetail cardDetail = Globalarea.getCardDetailOfString(mdriveDocModels.get(startCounter).getJsonText());
                String TAG = mdriveDocModels.get(startCounter).getWhichCard();

                File imageFile = null;
                if (cardDetail.getImage_url() != null) {
//                        Log.e("Getting image file","yes");
//                        String[] urlSeparated = cardDetail.getImage_url().split("/");
//                        String fileName = urlSeparated[urlSeparated.length-1];
                    imageFile = new File(cardDetail.getImage_url());
                }

                prepareUploadInDrive(imageFile, cardDetail, TAG, startCounter);

            }
        }
    }

    private void prepareUploadInDrive(File file, CardDetail cardDetail, String tag, int currentCounter) {
        FileOpration fileOpration = new FileOpration();
        String mimeType = fileOpration.getMimeType(Uri.fromFile(file), this);

        if (file.exists() && mimeType != null && cardDetail != null) {

//                String name = "";
//                if (cardDetail.getCard_name() != null && cardDetail.getCard_name().trim().length() > 0) {
//                    name = cardDetail.getCard_name();
//                } else {
//                    name = "Unknown";
//                }

            // String subFolderName = name.substring(0, Math.min(name.length() - 1, 10)) + System.currentTimeMillis();
            if (mdriveDocModels.get(currentCounter).getFolderId() != null) {
                String folderId = mdriveDocModels.get(currentCounter).getFolderId();
                Log.i("Updating file", "Yes");
                updateTextFile(cardDetail, folderId, currentCounter);
            } else {
                String subFolderName = mdriveDocModels.get(currentCounter).getFolderName();
                getFolderIdAndUpload(file, mimeType, tag, subFolderName, cardDetail, currentCounter);
            }


        } else {
            Log.e("Error", "Something is wrong with the selected file. Please try again");
//                Toast.makeText(this, "Something is wrong with the selected file. Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFolderIdAndUpload(File file, String mimeType,
                                      String folderName, String subFolderName,
                                      CardDetail cardDetail, int currentCounter) {
        if (folderName != null) {

            ScanRDriveOperations.getCategoryFolderAndCreateFolder(folderName, subFolderName,
                    mDriveServiceHelper, new ScanRDriveOperations.OnCompleteDriveOperations() {
                        @Override
                        public void onSuccess(String folderId) {
                            Log.e("folder id", "is: " + folderId);
                            metadataModel = new MetadataModel(folderName, subFolderName, folderId);

                            mdriveDocModels.get(currentCounter).setFolderId(folderId);
                            mdriveDocModels.get(currentCounter).setFolderName(folderName);

                            uploadImage(file, mimeType, folderId, currentCounter);
                            createTextFile(cardDetail, folderId, currentCounter);

                        }

                        @Override
                        public void onFailure(Exception e, String message) {

                            Log.e("Error Category folder", message);
//                            Toast.makeText(CardScanActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setGoogleDriveService() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (preferences.isDriveConnected()) {
            if (account != null) {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());
                com.google.api.services.drive.Drive googleDriveService =
                        new com.google.api.services.drive.Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName(getString(R.string.app_name))
                                .build();

                mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                Log.e("drive service", "helper setup");

            }
        }

    }

    private void uploadImage(File file, String mimeType, String folderId, int currentCounter) {
        isUploadingImage = true;

        ScanRDriveOperations.uploadAndRenameImageFile(file, mimeType, folderId, mDriveServiceHelper,
                new ScanRDriveOperations.OnCompleteDriveOperations() {
                    @Override
                    public void onSuccess(String fileId) {
                        isUploadingImage = false;
                        Log.i("uploaded", "yes");

                        mdriveDocModels.get(currentCounter).setImagefileId(fileId);

//                        Toast.makeText(CardScanActivity.this, "Successfully uploaded the file in the drive", Toast.LENGTH_SHORT).show();
                        metadataModel.setImageFileId(fileId);
                        saveMetadata(mdriveDocModels.get(currentCounter));
                    }

                    @Override
                    public void onFailure(Exception e, String message) {
                        isUploadingImage = false;
                        Log.i("uploaded", "no");
                        e.printStackTrace();
//                        Toast.makeText(CardScanActivity.this, "Issue in uploading a file into the drive", Toast.LENGTH_SHORT).show();
                        saveMetadata(mdriveDocModels.get(currentCounter));
                    }
                });
    }

    private void saveMetadata(DriveDocModel driveDocModel) {
//        if (isImage)
//            metadataModel.setImageFileId(fileID);
//        else
//            metadataModel.setJsonFileID(fileID);

        if (isCreatingTextFile || isUploadingImage) {
            Log.e("returning", "Process is going on");
            return;
        }

        List<MetadataModel> metadataModelList = new ArrayList<>();
        metadataModelList.add(metadataModel);

        Log.e("saving", "into metadata");

        Log.e("saving", "into metadata");
        ScanRDriveOperations.findMetadataDocForOperation(mDriveServiceHelper,
                new ScanRDriveOperations.OnCompleteMetaDataQueries() {
                    @Override
                    public void onSuccessFullMetadataContent(String metadataFileId, List<MetadataModel> metadataModels) {

                        Log.e("got the metadata models", String.valueOf(metadataModels.size()));
                        ScanRDriveOperations.addDocInMetadata(mDriveServiceHelper,
                                metadataFileId, metadataModels, metadataModel, new ScanRDriveOperations.OnCompleteDriveOperations() {
                                    @Override
                                    public void onSuccess(String message) {
                                        Log.e("added successfully ", "in metadata");

                                        int metaDataStatus = driveDocRepo.updateSyncStatus(driveDocModel, SyncStatus.synced.toString());
                                        if (metaDataStatus > 0) {
                                            int metaDataUpdated = driveDocRepo.updateDriveDoc(driveDocModel);
                                            if (metaDataUpdated > 0) {
                                                Log.e("Metadata synced", "true");
                                            }
                                        }

                                    }

                                    @Override
                                    public void onFailure(Exception e, String message) {
                                        Log.e("metadata add failed", message);
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Exception e, String message) {
                        Log.i("Failure metadata add", "failure");
                    }
                });

    }

    private void createTextFile(CardDetail cardDetail, String folderId, int currentCounter) {

        isCreatingTextFile = true;
        Gson gson = new Gson();
        String jsonText = gson.toJson(cardDetail);

        String fileName = ScanRDriveOperations.JSON_FILE_NAME;

        mDriveServiceHelper.createTextFile(fileName, jsonText, folderId)
                .addOnSuccessListener(googleDriveFileHolder -> {
                    isCreatingTextFile = false;
                    Log.i("uploaded text file", "yes");
//                    Toast.makeText(this, "Successfully created the text file in the drive", Toast.LENGTH_SHORT).show();
                    metadataModel.setJsonFileID(googleDriveFileHolder.getId());

                    mdriveDocModels.get(currentCounter).setTextfileId(googleDriveFileHolder.getId());

//                    Update to local DB with Unsynced Metadata
                    int updatedStatus = driveDocRepo.updateSyncStatus(mdriveDocModels.get(currentCounter), SyncStatus.metadataUnsynced.toString());
                    if (updatedStatus > 0) {
                        int updatedModel = driveDocRepo.updateDriveDoc(mdriveDocModels.get(currentCounter));
                        if (updatedModel > 0) {
                            Log.e("Updated to", "metadata unsynced");
                        }

                    }

                    saveMetadata(mdriveDocModels.get(currentCounter));

                })
                .addOnFailureListener(e -> {
                    isCreatingTextFile = false;
                    Log.i("uploaded text file", "no");
                    e.printStackTrace();
//                    Toast.makeText(this, "Issue in creating a file into the drive", Toast.LENGTH_SHORT).show();
                    saveMetadata(mdriveDocModels.get(currentCounter));
                });
    }

    private void updateTextFile(CardDetail cardDetail, String folderId, int currentCounter) {

        Gson gson = new Gson();
        String jsonText = gson.toJson(cardDetail);

        String fileid = mdriveDocModels.get(currentCounter).getTextfileId();

        if (fileid != null) {

            mDriveServiceHelper.updateTextFile(fileid, jsonText, folderId)
                    .addOnSuccessListener(googleDriveFileHolder -> {
                        Log.i("updated text file", "yes");

                        int updated = driveDocRepo.updateSyncStatus(mdriveDocModels.get(currentCounter), SyncStatus.synced.toString());
                        if (updated > 0) {
                            Log.i("updated text file local", "yes");
                        }

                    })
                    .addOnFailureListener(e -> {
                        Log.i("updated text file", "no");
                        e.printStackTrace();
                    });
        }

    }

    public void callDriveDeleteDocument(List<DriveDocModel> deleteDriveDocModelList) {
        if (preferences.isDriveConnected()) {

            if (deleteDriveDocModelList != null && deleteDriveDocModelList.size() > 0) {
                ScanRDriveOperations.deleteFolder(deleteDriveDocModelList, mDriveServiceHelper, 0, new ScanRDriveOperations.OnCompleteDriveOperations() {

                    @Override
                    public void onSuccess(String driveDocDetails) {
                        if (driveDocDetails != null) {

                            int deleted = driveDocRepo.updateSyncStatus(Globalarea.getDriveDocOfString(driveDocDetails), SyncStatus.deletedMetaData.toString());
                            if (deleted > 0) {
                                Log.e("Deleted file/folder", "yes");
                            }

                            List<MetadataModel> deleteItemMetadata = new ArrayList<>();
                            for (DriveDocModel driveDocModel : deleteDriveDocModelList) {

                                driveDocModel.setCardDetail(Globalarea.getCardDetailOfString(driveDocModel.getJsonText()));
                                deleteItemMetadata.add(new MetadataModel(driveDocModel));
                            }

                            deleteFromMetadata(deleteItemMetadata, deleteDriveDocModelList);
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String message) {
                        Log.i("Drive delete error", message);
                    }
                });

            }

        }
    }

    private void deleteFromMetadata(List<MetadataModel> deleteItemMetadata, List<DriveDocModel> deleteDriveDocModelList) {

        ScanRDriveOperations.findMetadataDocForOperation(mDriveServiceHelper,
                new ScanRDriveOperations.OnCompleteMetaDataQueries() {
                    @Override
                    public void onSuccessFullMetadataContent(String metadataFileId,
                                                             List<MetadataModel> metadataModels) {

                        Log.e("found metadata content", String.valueOf(metadataModels.size()));
                        ScanRDriveOperations.deleteDocInMetadata(mDriveServiceHelper,
                                metadataFileId, metadataModels, deleteItemMetadata,
                                new ScanRDriveOperations.OnCompleteDriveOperations() {
                                    @Override
                                    public void onSuccess(String message) {

                                        for (DriveDocModel driveDocModel : deleteDriveDocModelList) {

                                            int deletedMetadata = driveDocRepo.deleteById(driveDocModel);
                                            if (deletedMetadata > 0) {
                                                Log.i("deleted", "metadata");
                                            }
                                        }

                                    }

                                    @Override
                                    public void onFailure(Exception e, String message) {
                                        e.printStackTrace();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Exception e, String message) {
                        e.printStackTrace();
                        Log.e("Delete metadata failure", "failure");
                    }
                });

    }

    private void getDeletedMetadataAndUpdate() {
        if (driveDocRepo != null) {
            List<DriveDocModel> deleteDriveDocModelList = driveDocRepo.getDocsBySyncStatus(SyncStatus.deletedMetaData.toString());

            if (deleteDriveDocModelList != null && deleteDriveDocModelList.size() > 0) {
                Log.i("Delete unsync", String.valueOf(deleteDriveDocModelList.size()));
                callDriveDeleteDocument(deleteDriveDocModelList);

                List<MetadataModel> deleteItemMetadata = new ArrayList<>();
                for (DriveDocModel driveDocModel : deleteDriveDocModelList) {

                    driveDocModel.setCardDetail(Globalarea.getCardDetailOfString(driveDocModel.getJsonText()));
                    deleteItemMetadata.add(new MetadataModel(driveDocModel));
                }

                deleteFromMetadata(deleteItemMetadata, deleteDriveDocModelList);

            }
        }
    }
}
