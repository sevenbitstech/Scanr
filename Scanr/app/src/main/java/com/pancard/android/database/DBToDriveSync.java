package com.pancard.android.database;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.DriveOperations.MetadataModel;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.model.CardDetail;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.FileOpration;
import com.pancard.android.utility.PreferenceManagement;

import java.io.File;
import java.util.List;

public class DBToDriveSync {

    public static final String TAG = "DBToDriveSync";
    private DriveDocRepo driveDocRepo;
    private PreferenceManagement preferences;
    private Context context;
    private DriveServiceHelper driveServiceHelper;
    private ConnectionDetector connectionDetector;

    public DBToDriveSync(Context context, DriveServiceHelper driveServiceHelper) {

        Log.e(TAG, "in constructor");

        this.context = context;
        driveDocRepo = new DriveDocRepo(context);
        preferences = Scanner.getInstance().getPreferences();
        this.driveServiceHelper = driveServiceHelper;
        connectionDetector = new ConnectionDetector(context);
    }

    public void startSyncing() {

        if (preferences.isDriveConnected()) {
            Log.e(TAG, "started syncing");
//            updateUpdatedRecords();
            Globalarea.isDriveSyncInProgress = true;

            uploadIntoRootFolder();
//            uploadUnSyncedModelsWithRetry();
//            deleteDeletedRecords();
        }
    }

    private void uploadIntoRootFolder() {
//        Toast.makeText(context, "uploading into root", Toast.LENGTH_SHORT).show();
        DriveDocModel driveDocModel = driveDocRepo.getDriveDocModelByStatus(SyncStatus.unsynced.toString());

        if (driveDocModel == null) {
            driveDocModel = driveDocRepo.getDriveDocModelByStatus(SyncStatus.deleted.toString());

//            if (driveDocModel == null) {
//                driveDocModel = driveDocRepo.getDriveDocModelByStatus(SyncStatus.deletedDriveFailed.toString());
//            }

            if (driveDocModel != null) {
                renameDeletedFiles(driveDocModel, 0);
                return;
            }
//            else {
//                driveDocModel = driveDocRepo.getDriveDocModelByStatus(SyncStatus.syncFailed.toString());
//            }
        }

        if (driveDocModel != null) {

            String folderName = driveDocModel.getFolderName();
            if (folderName != null) {
                int charSize = folderName.length();


                //todo: for multiple iamge remove "_"
                if (folderName.charAt(charSize - 2) == '_') {

//                    Toast.makeText(context, "multiple images", Toast.LENGTH_SHORT).show();
                    String newString = folderName.substring(0, charSize - 2);
                    Log.e("new stirng", newString);

                    startUploadInRoot(driveDocModel, newString, 0);

                } else {
//                    Toast.makeText(context, "single images", Toast.LENGTH_SHORT).show();
                    startUploadInRoot(driveDocModel, folderName, 0);
                }

            } else {
                //stop here
                Globalarea.isDriveSyncInProgress = false;
            }

        } else {
            //stop here.
            Globalarea.isDriveSyncInProgress = false;
            Globalarea.setDriveToDbSync(driveServiceHelper);
        }

    }

    private void renameDeletedFiles(DriveDocModel driveDocModel, int retryCounter) {

        String suffix = "_";
        String publicGUID = driveDocModel.getPublicGuid();
        String whichCard = driveDocModel.getWhichCard();

        String commonName = publicGUID + suffix + whichCard;
        String textFileName = commonName + suffix + FileVersion.INFO.toString() + suffix
                + FileVersion.DELETED + ".txt";
        String name = commonName + suffix + FileVersion.CROPPED.toString() + suffix
                + FileVersion.DELETED + ".jpg";
        String originalImageName = commonName + suffix + FileVersion.ORIGINAL.toString() + suffix
                + FileVersion.DELETED + ".jpg";

        if (driveDocModel.getTextfileId() != null) {
            addDeletedKeywordFileInTextFile(driveDocModel, textFileName, name, originalImageName, retryCounter);
        } else {
            addDeletedKeywordFileInImageFile(driveDocModel, name, originalImageName, retryCounter);
        }
    }

    private void addDeletedKeywordFileInTextFile(DriveDocModel driveDocModel, String textFileName,
                                                 String imageName, String originalImageName,
                                                 int retryCounter) {

        if (driveDocModel.getTextfileId() != null) {
            driveServiceHelper.renameFile(driveDocModel.getTextfileId(), textFileName)
                    .addOnSuccessListener(aVoid -> addDeletedKeywordFileInImageFile(driveDocModel,
                            imageName, originalImageName, retryCounter))
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        handleFailureNewFlowDeleted(driveDocModel, retryCounter);
                    });
        } else {
            addDeletedKeywordFileInImageFile(driveDocModel, imageName, originalImageName, retryCounter);
        }
    }

    private void addDeletedKeywordFileInImageFile(DriveDocModel driveDocModel,
                                                  String imageFileName, String originalImageName,
                                                  int retryCounter) {

        if (driveDocModel.getImagefileId() != null) {
            driveServiceHelper.renameFile(driveDocModel.getImagefileId(), imageFileName)
                    .addOnSuccessListener(aVoid -> addDeletedKeywordFileInOriginalImageFile(driveDocModel,
                            originalImageName, retryCounter))
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        handleFailureNewFlowDeleted(driveDocModel, retryCounter);
                    });
        } else {
            addDeletedKeywordFileInOriginalImageFile(driveDocModel, originalImageName, retryCounter);
        }

    }

    private void addDeletedKeywordFileInOriginalImageFile(DriveDocModel driveDocModel,
                                                          String originalImageName, int retryCounter) {

        if (driveDocModel.getOriginalImageId() != null) {
            driveServiceHelper.renameFile(driveDocModel.getOriginalImageId(), originalImageName)
                    .addOnSuccessListener(aVoid -> {
                        changeStatusInDb(driveDocModel, SyncStatus.deletedDrive.toString());
                        uploadIntoRootFolder();
                    }).addOnFailureListener(e -> {
                e.printStackTrace();
                handleFailureNewFlowDeleted(driveDocModel, retryCounter);
            });
        } else {
            changeStatusInDb(driveDocModel, SyncStatus.deletedDrive.toString());
            uploadIntoRootFolder();
        }

    }

    private void startUploadInRoot(DriveDocModel drivedocModel, String folderName, int retryCounter) {

        if (drivedocModel != null) {
            final int counter = retryCounter;

            ScanRDriveOperations.getRootFolderIdOrCreate(driveServiceHelper, new ScanRDriveOperations.OnCompleteDriveOperations() {
                @Override
                public void onSuccess(String parentId) {

                    CardDetail cardDetail = Globalarea.getCardDetailOfString(drivedocModel.getJsonText());

                    String suffix = "_";
                    String publicGUID = drivedocModel.getPublicGuid();
                    String whichCard = drivedocModel.getWhichCard();
                    String textFileName = publicGUID + suffix + whichCard + suffix + FileVersion.INFO.toString() + ".txt";

                    File imageFile = null;
                    File originalFile = null;
                    if (cardDetail.getImage_url() != null) {
                        imageFile = new File(cardDetail.getImage_url());
                    }
                    if (drivedocModel.getOriginalImagePath() != null) {
                        originalFile = new File(drivedocModel.getOriginalImagePath());
                    }

//                    Toast.makeText(context, "starting file operation", Toast.LENGTH_SHORT).show();

                    FileOpration fileOpration = new FileOpration();
                    String mimeType = fileOpration.getMimeType(Uri.fromFile(imageFile), context);
                    String mimeTypeOriginal = fileOpration.getMimeType(Uri.fromFile(originalFile), context);
//                    File finalImageFile = imageFile;
                    File finalOriginalImageFile = originalFile;

                    if (imageFile == null || originalFile == null) {
                        drivedocModel.setSyncStatus(SyncStatus.synced.toString());
                        int updated = driveDocRepo.updateDriveDoc(drivedocModel);

                        if (updated > 0) {
                            if (connectionDetector.isConnectingToInternet1())
                                uploadIntoRootFolder();
                            else
                                Globalarea.isDriveSyncInProgress = false;
                        } else {
                            if (connectionDetector.isConnectingToInternet1())
                                uploadIntoRootFolder();
                            else
                                Globalarea.isDriveSyncInProgress = false;
                        }

                    }

//                    Toast.makeText(context, "uploading the image file", Toast.LENGTH_SHORT).show();

                    driveServiceHelper.uploadFile(imageFile, mimeType, parentId)
                            .addOnSuccessListener(googleDriveFileHolder -> {
                                String imageFileId = googleDriveFileHolder.id;

                                driveServiceHelper.uploadFile(finalOriginalImageFile, mimeTypeOriginal, parentId)
                                        .addOnSuccessListener(googleDriveFileHolder1 -> {
                                            String originalImageFileId = googleDriveFileHolder1.id;

                                            String content = drivedocModel.getJsonText();
                                            driveServiceHelper.createTextFile(textFileName, content, parentId)
                                                    .addOnSuccessListener(googleDriveFileHolder11 -> {
//                                                    Toast.makeText(context, "setting textfile id", Toast.LENGTH_SHORT).show();
                                                        String textFileId = googleDriveFileHolder11.id;

//                                                    Toast.makeText(context, "setting drive doc model", Toast.LENGTH_SHORT).show();
                                                        drivedocModel.setImagefileId(imageFileId);
                                                        drivedocModel.setTextfileId(textFileId);
                                                        drivedocModel.setOriginalImageId(originalImageFileId);
                                                        drivedocModel.setSyncStatus(SyncStatus.synced.toString());

//                                                    Toast.makeText(context, "updating in the db", Toast.LENGTH_SHORT).show();
                                                        int updated = driveDocRepo.updateDriveDoc(drivedocModel);

                                                        if (drivedocModel.getPdfFilePath() != null) {
                                                            Log.e("pdf path", drivedocModel.getPdfFilePath());
                                                            File pdfFile = new File(drivedocModel.getPdfFilePath());
                                                            String pdfMimeType = fileOpration.getMimeType(Uri.fromFile(pdfFile), context);

                                                            if (pdfFile.exists()) {
                                                                driveServiceHelper.uploadFile(pdfFile, pdfMimeType, parentId)
                                                                        .addOnSuccessListener(googleDriveFileHolder2 -> {
                                                                            String pdfId = googleDriveFileHolder2.id;
                                                                            drivedocModel.setTextfileId(pdfId);
                                                                            drivedocModel.setSyncStatus(SyncStatus.synced.toString());

                                                                            int updated1 = driveDocRepo.updateDriveDoc(drivedocModel);
                                                                            uploadSuccess(updated1);
                                                                        }).addOnFailureListener(e -> {
                                                                    e.printStackTrace();
                                                                    if (connectionDetector.isConnectingToInternet1()) {
                                                                        drivedocModel.setSyncStatus(SyncStatus.pdfSyncFailed.toString());
                                                                    } else {
                                                                        Globalarea.isDriveSyncInProgress = false;
                                                                    }
                                                                });
                                                            } else {
                                                                uploadSuccess(updated);
                                                            }

                                                        } else {
                                                            uploadSuccess(updated);
                                                        }

                                                    }).addOnFailureListener(e -> {
                                                e.printStackTrace();
                                                handleFailureNewFlow(counter, drivedocModel, folderName);
                                            });
                                        }).addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    handleFailureNewFlow(counter, drivedocModel, folderName);
                                });

                            }).addOnFailureListener(e -> {
                        e.printStackTrace();
                        handleFailureNewFlow(counter, drivedocModel, folderName);
                    });
                }

                @Override
                public void onFailure(Exception e, String message) {
                    e.printStackTrace();
                    handleFailureNewFlow(counter, drivedocModel, folderName);
                }
            });
        }

    }

    private void uploadSuccess(int updated) {
        if (updated > 0) {
//                                                        Toast.makeText(context, "updated successfully", Toast.LENGTH_SHORT).show();
            if (connectionDetector.isConnectingToInternet1())
                uploadIntoRootFolder();
            else
                Globalarea.isDriveSyncInProgress = false;
        } else {
//                                                        Toast.makeText(context, "not updated", Toast.LENGTH_SHORT).show();
            if (connectionDetector.isConnectingToInternet1())
                uploadIntoRootFolder();
            else
                Globalarea.isDriveSyncInProgress = false;
        }
    }

//    private void uploadUnSyncedModelsWithRetry() {
//        DriveDocModel driveDocModel = driveDocRepo.getDriveDocModelByStatus(SyncStatus.unsynced.toString());
//        uploadUnSyncedModels(driveDocModel,0);
//    }

    private void uploadUnSyncedModels(DriveDocModel driveDocModel, int retryCounter, String subFolderName) {


        if (driveDocModel != null) {
            String folderName = driveDocModel.getWhichCard();
//            String subFolderName = driveDocModel.getFolderName();

            MetadataModel metadataModel = new MetadataModel(subFolderName,
                    null, null, null,
                    folderName, SyncStatus.unsynced.toString());

            final int counter = retryCounter;
            ScanRDriveOperations.findMetadataDocForOperation(driveServiceHelper,
                    new ScanRDriveOperations.OnCompleteMetaDataQueries() {
                        @Override
                        public void onSuccessFullMetadataContent(String metadataFileId, List<MetadataModel> metadataModels) {
                            Log.e("got the metadata models", String.valueOf(metadataModels.size()));


                            ScanRDriveOperations.addDocInMetadata(driveServiceHelper,
                                    metadataFileId, metadataModels, metadataModel,
                                    new ScanRDriveOperations.OnCompleteDriveOperations() {
                                        @Override
                                        public void onSuccess(String jsonMetadataList) {
                                            Log.e(TAG, "added successfully in metadata");
                                            List<MetadataModel> newMetadataModels =
                                                    Globalarea.getMetadataListOfString(jsonMetadataList);
                                            ScanRDriveOperations.getCategoryFolderAndCreateFolder(folderName, subFolderName,
                                                    driveServiceHelper, new ScanRDriveOperations.OnCompleteDriveOperations() {
                                                        @Override
                                                        public void onSuccess(String folderId) {
                                                            Log.e(TAG, "successfully created the doc folder");

                                                            driveDocModel.setFolderId(folderId);
                                                            metadataModel.setDocFolderID(folderId);

                                                            uploadInTheDrive(driveDocModel, metadataModel,
                                                                    metadataFileId, newMetadataModels, 0);
                                                        }

                                                        @Override
                                                        public void onFailure(Exception e, String message) {
                                                            e.printStackTrace();
                                                            handleFailure(counter, driveDocModel, subFolderName);
                                                            //todo: what to do in this case? stop syncing or fetch for next?
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onFailure(Exception e, String message) {
                                            e.printStackTrace();
                                            handleFailure(counter, driveDocModel, subFolderName);
                                            //todo: what to do in this case? stop syncing or fetch for next?
                                        }
                                    });
                        }

                        @Override
                        public void onFailure(Exception e, String message) {
                            e.printStackTrace();
                            handleFailure(counter, driveDocModel, subFolderName);
                            //stop here.
                        }
                    });
        } else {
            Globalarea.isDriveSyncInProgress = false;
            Log.e(TAG, "no more unsynced record in the db");
            //stop here.
        }

    }

    private void handleFailureNewFlow(int currentRetryCounter, DriveDocModel driveDocModel,
                                      String subFolderName) {
        if (connectionDetector.isConnectingToInternet1()) {

            if (currentRetryCounter < 3) {
                startUploadInRoot(driveDocModel, subFolderName, currentRetryCounter + 1);
            } else {
                changeStatusInDb(driveDocModel, SyncStatus.syncFailed.toString());
                uploadIntoRootFolder();
            }

        } else {
            Globalarea.isDriveSyncInProgress = false;
        }
    }

    private void handleFailureNewFlowDeleted(DriveDocModel driveDocModel, int currentRetryCounter) {
        if (connectionDetector.isConnectingToInternet1()) {

            if (currentRetryCounter < 3) {
                renameDeletedFiles(driveDocModel, currentRetryCounter + 1);
            } else {
                changeStatusInDb(driveDocModel, SyncStatus.deletedDriveFailed.toString());
                uploadIntoRootFolder();
            }

        } else {
            Globalarea.isDriveSyncInProgress = false;
        }
    }

    private void handleFailure(int currentRetryCounter, DriveDocModel driveDocModel, String subFolderName) {
        if (connectionDetector.isConnectingToInternet1()) {

            if (currentRetryCounter < 3) {
                uploadUnSyncedModels(driveDocModel, currentRetryCounter + 1, subFolderName);
            } else {
                changeStatusInDb(driveDocModel, SyncStatus.syncFailed.toString());
                uploadUnSyncedModelsWithRetry();
            }

        } else {
            Globalarea.isDriveSyncInProgress = false;
        }
    }

    private void changeStatusInDb(DriveDocModel driveDocModel, String status) {
        //todo: change status in db as SyncStatus.synceFailed
        int statusUpdated = driveDocRepo.updateSyncStatus(driveDocModel, status);
        if (statusUpdated > 0) {
            Log.e("Status changed:", status);
        }
    }


    private void uploadUnSyncedModelsWithRetry() {
        DriveDocModel driveDocModel = driveDocRepo.getDriveDocModelByStatus(SyncStatus.unsynced.toString());

        if (driveDocModel != null) {

            String folderName = driveDocModel.getFolderName();
            if (folderName != null) {
                int charSize = folderName.length();

                if (folderName.charAt(charSize - 2) == '_') {
                    //todo: remove ....
                    String newString = folderName.substring(0, charSize - 2);
                    Log.e("new stirng", newString);
                    uploadUnSyncedModels(driveDocModel, 0, newString);

                } else {
                    uploadUnSyncedModels(driveDocModel, 0, folderName);
                }

            } else {
                //stop here
                Globalarea.isDriveSyncInProgress = false;
            }

        } else {
            //stop here.
            Globalarea.isDriveSyncInProgress = false;
        }

    }

    private void uploadInTheDrive(DriveDocModel driveDocModel, MetadataModel metadataModel,
                                  String metadataFileId, List<MetadataModel> metadataModels,
                                  int uploadDriveCounter) {

        CardDetail cardDetail = Globalarea.getCardDetailOfString(driveDocModel.getJsonText());
        String textFileName = ScanRDriveOperations.JSON_FILE_NAME;

        File imageFile = null;
        if (cardDetail.getImage_url() != null) {
//                        Log.e("Getting image file","yes");
//                        String[] urlSeparated = cardDetail.getImage_url().split("/");
//                        String fileName = urlSeparated[urlSeparated.length-1];
            imageFile = new File(cardDetail.getImage_url());
        }

        FileOpration fileOpration = new FileOpration();
        String mimeType = fileOpration.getMimeType(Uri.fromFile(imageFile), context);
        File finalImageFile = imageFile;

        driveServiceHelper.createTextFile(textFileName, driveDocModel.getJsonText(), driveDocModel.getFolderId())
                .addOnSuccessListener(googleDriveFileHolder -> {
                    Log.i("uploaded text file", "yes");

                    ScanRDriveOperations.uploadAndRenameImageFile(finalImageFile, mimeType,
                            driveDocModel.getFolderId(), driveServiceHelper,

                            new ScanRDriveOperations.OnCompleteDriveOperations() {
                                @Override
                                public void onSuccess(String fileId) {
                                    Log.i(TAG, "uploaded amd renamed image file");

                                    updateMetadataAndDatabase(metadataModel, driveDocModel,
                                            fileId, googleDriveFileHolder.getId(),
                                            metadataFileId, metadataModels, 0);
                                }

                                @Override
                                public void onFailure(Exception e, String message) {
                                    e.printStackTrace();
                                    if (connectionDetector.isConnectingToInternet1()) {
                                        handleFailureUploadDrive(driveDocModel, metadataModel,
                                                metadataFileId, metadataModels, uploadDriveCounter);
                                    }

                                }
                            });

                })
                .addOnFailureListener(e -> {
                    if (connectionDetector.isConnectingToInternet1()) {
                        handleFailureUploadDrive(driveDocModel, metadataModel, metadataFileId,
                                metadataModels, uploadDriveCounter);
                    }
                });

    }

    private void handleFailureUploadDrive(DriveDocModel driveDocModel, MetadataModel metadataModel,
                                          String metadataFileId, List<MetadataModel> metadataModels,
                                          int uploadDriveCounter) {
        if (uploadDriveCounter < 3) {
            uploadInTheDrive(driveDocModel, metadataModel, metadataFileId, metadataModels,
                    uploadDriveCounter + 1);
        } else {
            changeStatusInDb(driveDocModel, SyncStatus.syncFailed.toString());
            uploadUnSyncedModelsWithRetry();
        }
    }

    private void updateMetadataAndDatabase(MetadataModel metadataModel, DriveDocModel driveDocModel,
                                           String imageFileId, String textFileId,
                                           String metadataFileId, List<MetadataModel> metadataModels,
                                           int counterMetadata) {

        driveDocModel.setImagefileId(imageFileId);
        metadataModel.setImageFileId(imageFileId);

        driveDocModel.setTextfileId(textFileId);
        metadataModel.setJsonFileID(textFileId);

        int posInList = ScanRDriveOperations.getPosInList(metadataModels, metadataModel);
        if (posInList > -1) {
            metadataModel.setSyncStatus(SyncStatus.synced.toString());
            metadataModels.get(posInList).resetAfterSync(metadataModel);
            Gson gson = new Gson();
            String finalJson = gson.toJson(metadataModels);

            ScanRDriveOperations.updateMetaDataContent(driveServiceHelper, finalJson, metadataFileId,
                    new ScanRDriveOperations.OnCompleteDriveOperations() {
                        @Override
                        public void onSuccess(String message) {
                            Log.e(TAG, "successfullly updated in the metadata ");
                            driveDocModel.setSyncStatus(SyncStatus.synced.toString());
                            int updated = driveDocRepo.updateDriveDoc(driveDocModel);

                            if (updated > 0) {
                                if (connectionDetector.isConnectingToInternet1())
                                    uploadUnSyncedModelsWithRetry();
                            } else {
                                if (connectionDetector.isConnectingToInternet1())
                                    uploadUnSyncedModelsWithRetry();
                                else
                                    Globalarea.isDriveSyncInProgress = false;
                            }
                        }

                        @Override
                        public void onFailure(Exception e, String message) {
                            e.printStackTrace();
                            //todo: what to do here?
                            if (counterMetadata < 3) {
                                if (connectionDetector.isConnectingToInternet1())
                                    updateMetadataAndDatabase(metadataModel, driveDocModel,
                                            imageFileId, textFileId,
                                            metadataFileId, metadataModels, counterMetadata + 1);
                            } else {
                                changeStatusInDb(driveDocModel, SyncStatus.metadataSyncFailed.toString());
                                uploadUnSyncedModelsWithRetry();
                            }

//                    if (connectionDetector.isConnectingToInternet1())
//                        uploadUnSyncedModels();
                        }
                    });


        } else {
            Log.e(TAG, "does not contain");
            Globalarea.isDriveSyncInProgress = false;
            //todo: what to do here?
        }
    }

    private void deleteDeletedRecords() {
        if (!preferences.isDriveConnected()) {
            return;
        }

        DriveDocModel driveDocModel = driveDocRepo.getDriveDocModelByStatus(SyncStatus.deleted.toString());

        if (driveDocModel != null) {

            Log.e(TAG, "deleting the record");
            MetadataModel metadataModel = new MetadataModel(driveDocModel);

            ScanRDriveOperations.findMetadataDocForOperation(driveServiceHelper,
                    new ScanRDriveOperations.OnCompleteMetaDataQueries() {
                        @Override
                        public void onSuccessFullMetadataContent(String metadataFileId, List<MetadataModel> metadataModels) {

                            int posInList = ScanRDriveOperations.getPosInList(metadataModels, metadataModel);
                            if (posInList > -1) {

                                Log.e(TAG, "updating the records in the metadata file object list");
                                metadataModels.get(posInList).setSyncStatus(SyncStatus.deleted.toString());

                                Gson gson = new Gson();
                                String finalJson = gson.toJson(metadataModels);

                                ScanRDriveOperations.updateMetaDataContent(driveServiceHelper,
                                        finalJson, metadataFileId, new ScanRDriveOperations.OnCompleteDriveOperations() {
                                            @Override
                                            public void onSuccess(String message) {
                                                deleteFromDrive(driveDocModel, metadataModel,
                                                        metadataFileId, metadataModels);
                                            }

                                            @Override
                                            public void onFailure(Exception e, String message) {
                                                e.printStackTrace();
                                                Log.e(TAG, "issue in updating metadata content");
                                                if (connectionDetector.isConnectingToInternet1()) {
                                                    uploadUnSyncedModelsWithRetry();
                                                }
                                            }
                                        });

                            } else {
                                Log.e(TAG, "not present in the metadata file");
                                deleteFromDb(driveDocModel);
                            }

                        }

                        @Override
                        public void onFailure(Exception e, String message) {
                            Log.e(TAG, "metadata file content get issue");
                            e.printStackTrace();
                            if (connectionDetector.isConnectingToInternet1()) {
                                uploadUnSyncedModelsWithRetry();
                            }
                        }
                    });

        } else {
            Log.e(TAG, "no more deleted records in db");
            uploadUnSyncedModelsWithRetry();
        }
    }

    private void deleteFromDrive(DriveDocModel driveDocModel, MetadataModel metadataModel,
                                 String metadataFileId, List<MetadataModel> metadataModels) {

        Log.e(TAG, "deleting from the drive folder");
        driveServiceHelper.deleteFolderFile(driveDocModel.getFolderId())
                .addOnSuccessListener(aVoid -> {
                    //remove from metadata

                    int posInList = ScanRDriveOperations.getPosInList(metadataModels, metadataModel);
                    if (posInList > -1) {
                        metadataModels.remove(posInList);

                        Gson gson = new Gson();
                        String finalJson = gson.toJson(metadataModels);

                        ScanRDriveOperations.updateMetaDataContent(driveServiceHelper,
                                finalJson, metadataFileId, new ScanRDriveOperations.OnCompleteDriveOperations() {
                                    @Override
                                    public void onSuccess(String message) {
                                        deleteFromDb(driveDocModel);
                                    }

                                    @Override
                                    public void onFailure(Exception e, String message) {
                                        //todo: discuss about the case when metadata contains the deleted case,
                                        e.printStackTrace();
                                        if (connectionDetector.isConnectingToInternet1()) {
                                            deleteDeletedRecords();
                                        }
                                    }
                                });

                    } else {
                        Log.e(TAG, "metadata does not contain the model");
                        deleteFromDb(driveDocModel);

                    }

                }).addOnFailureListener(e -> {
            e.printStackTrace();

            if (connectionDetector.isConnectingToInternet1()) {
                uploadUnSyncedModelsWithRetry();
            }
        });
    }

    private void deleteFromDb(DriveDocModel driveDocModel) {
        int deleted = driveDocRepo.deleteById(driveDocModel);
        if (deleted > 0) {
            deleteDeletedRecords();
        } else {
            Log.e(TAG, "issue in deleting the record form db");
            //todo: ask someone about this case.
        }
    }

//    private void updateUpdatedRecords() {
//
//        if (!preferences.isDriveConnected()) {
//            return;
//        }
//
//        DriveDocModel driveDocModel = driveDocRepo.getDriveDocModelByStatus(SyncStatus.updated.toString());
//
//        if (driveDocModel != null) {
//
//            Log.e(TAG, "updating the record");
//            driveServiceHelper.saveFile(driveDocModel.getTextfileId(),
//                    ScanRDriveOperations.JSON_FILE_NAME,
//                    driveDocModel.getJsonText()).addOnSuccessListener(aVoid -> {
//
//                int updated = driveDocRepo.updateSyncStatus(driveDocModel, SyncStatus.synced.toString());
//
//                if (updated > 0) {
//                    Log.e(TAG, "successfully updated one record, now moving to next.");
//                    updateUpdatedRecords();
//                } else {
//                    Log.e(TAG, "not updated in the database.");
//                    //todo: ask what to do in this case.
//                }
//
//            }).addOnFailureListener(e -> {
//                e.printStackTrace();
//                Log.e(TAG, "failed to update record");
//
//                if (connectionDetector.isConnectingToInternet1()) {
//                    deleteDeletedRecords();
//                }
//            });
//        } else {
//            Log.e(TAG, "no more updated records in the db");
//            deleteDeletedRecords();
//        }
//
//    }
}