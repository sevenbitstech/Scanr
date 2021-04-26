package com.pancard.android.database;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.DriveOperations.GoogleDriveFileHolder;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.model.CardDetail;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.LocalFilesAndFolder;
import com.pancard.android.utility.PreferenceManagement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DriveToDbSync {

    public static final String TAG = "DriveToDBSync";
    private static final String textExtension = ".txt";
    private static final String imageExtension = ".jpg";
    private static final String suffix = "_";
    private DriveDocRepo driveDocRepo;
    private PreferenceManagement preferences;
    private DriveServiceHelper driveServiceHelper;
    private ConnectionDetector connectionDetector;
    private PreferenceManagement preferenceManagement;
    private Context context;

    public DriveToDbSync(Context context, DriveServiceHelper driveServiceHelper) {
        Log.e(TAG, "in constructor");

        this.context = context;
        driveDocRepo = new DriveDocRepo(context);
        preferences = Scanner.getInstance().getPreferences();
        this.driveServiceHelper = driveServiceHelper;
        connectionDetector = new ConnectionDetector(context);
        preferenceManagement = Scanner.getInstance().getPreferences();
    }

    private static String getTextFileName(String publicGuid, String whichCard) {
        return publicGuid + suffix + whichCard + suffix + FileVersion.INFO.toString() + textExtension;
    }

    private static String getCroppedFileName(String publicGuid, String whichCard) {
        return publicGuid + suffix + whichCard + suffix + FileVersion.CROPPED.toString() + imageExtension;
    }

    private static String getOriginalFileName(String publicGuid, String whichCard) {
        return publicGuid + suffix + whichCard + suffix + FileVersion.ORIGINAL.toString() + imageExtension;
    }

    private static int getPositionInList
            (List<GoogleDriveFileHolder> googleDriveFileHolderList, String fileName) {

        for (int i = 0; i < googleDriveFileHolderList.size(); i++) {
            GoogleDriveFileHolder fileHolder = googleDriveFileHolderList.get(i);
            if (fileHolder.name.equals(fileName) || fileHolder.name.contains(fileName)) {
                return i;
            }
        }

        return -1;
    }

    public void startSyncing() {

        if (preferences.isDriveConnected() && !preferenceManagement.isSyncDriveToDb()) {
            Log.e(TAG, "started syncing");

            Globalarea.isDbSyncInProgress = true;
            getFilesFromRoot();

        } else {
            Log.e("not connected", "not synced");
        }
    }

    private void getFilesFromRoot() {
        Log.e("downloading files", "from root");
        ScanRDriveOperations.startDownloadingAllFiles(driveServiceHelper, new ScanRDriveOperations.OnCompleteDriveFileQueries() {
            @Override
            public void onSuccessfulDriveFileQueries(List<GoogleDriveFileHolder> googleDriveFileHolders, String parentId) {

                Log.e("got ", "result");
                if (googleDriveFileHolders != null && googleDriveFileHolders.size() > 0) {
                    List<GoogleDriveFileHolder> fileHolders = getFilesWithUnderScore(googleDriveFileHolders);
                    processFilesWithUnderScore(fileHolders, parentId);
                } else {
                    preferenceManagement.setSyncDriveToDb(true);
                    Globalarea.isDbSyncInProgress = false;
                }

            }

            @Override
            public void onFailure(Exception e, String message) {
                e.printStackTrace();
                preferenceManagement.setSyncDriveToDb(true);
                Globalarea.isDbSyncInProgress = false;
            }
        });

    }

    private List<GoogleDriveFileHolder> getFilesWithUnderScore(List<GoogleDriveFileHolder> googleDriveFileHolders) {
        List<GoogleDriveFileHolder> fileHolders = new ArrayList<>();

        for (GoogleDriveFileHolder googleDriveFileHolder : googleDriveFileHolders) {
            String name = googleDriveFileHolder.name;
            String[] propertiesOfName = name.split("_");

            if (propertiesOfName.length == 3) {
                fileHolders.add(googleDriveFileHolder);
            }
        }

        return fileHolders;
    }

    private void processFilesWithUnderScore(List<GoogleDriveFileHolder> googleDriveFileHolders, String parentFolderId) {

        if (googleDriveFileHolders != null && googleDriveFileHolders.size() > 0) {
            Log.e("found size", String.valueOf(googleDriveFileHolders.size()));
            GoogleDriveFileHolder firstFile = googleDriveFileHolders.get(0);

            String fullName = firstFile.name;
//        String id = firstFile.id;

            String[] propertiesOfName = fullName.split("_");

//            String publicGuid = propertiesOfName[0];
//            String whichCard = propertiesOfName[1];
//            String version = propertiesOfName[2];

            if (propertiesOfName.length != 3) {
                //length 4 includes the files with the length 4. (deleted is additionally appended)
                //todo: omit this file and pick next
                String[] filesToRemove = new String[1];
                filesToRemove[0] = fullName;
                removeTheseFilesFromListAndPickNext(googleDriveFileHolders, filesToRemove, parentFolderId);
                return;
            }

            String publicGuid = propertiesOfName[0];
            String whichCard = propertiesOfName[1];
            String version = propertiesOfName[2];

            Log.e("file parts", "guid: " + publicGuid + ",whichcard: " + whichCard + ",version:" + version);

            String croppedFileName, originalFileName, textFileName;
            String croppedFileId = null, originalFileId = null, textFileId = null;

            int textFilePos, croppedFilePos, originalFilePos;

            textFileName = getTextFileName(publicGuid, whichCard);
            croppedFileName = getCroppedFileName(publicGuid, whichCard);
            originalFileName = getOriginalFileName(publicGuid, whichCard);

            String[] filesToRemove = new String[3];
            filesToRemove[0] = textFileName;
            filesToRemove[1] = croppedFileName;
            filesToRemove[2] = originalFileName;

            Log.e("file names", "info: " + textFileName + ",crop: " + croppedFileName + ",original:" + originalFileName);

            textFilePos = getPositionInList(googleDriveFileHolders, textFileName);
            croppedFilePos = getPositionInList(googleDriveFileHolders, croppedFileName);
            originalFilePos = getPositionInList(googleDriveFileHolders, originalFileName);

            Log.e("file positions", "info: " + textFilePos + ",crop: " + croppedFilePos + ",original:" + originalFilePos);

            //if data is already present in the database.
            if (driveDocRepo.getDriveDocModelByPublicGuId(publicGuid) != null) {

                removeTheseFilesFromListAndPickNext(googleDriveFileHolders, filesToRemove, parentFolderId);

            } else {
                if (textFilePos > -1) {
                    textFileId = googleDriveFileHolders.get(textFilePos).id;
                }
                if (croppedFilePos > -1) {
                    croppedFileId = googleDriveFileHolders.get(croppedFilePos).id;
                }
                if (originalFilePos > -1) {
                    originalFileId = googleDriveFileHolders.get(originalFilePos).id;
                }

                DriveDocModel driveDocModel = new DriveDocModel();
                driveDocModel.setOriginalImageId(originalFileId);
                driveDocModel.setImagefileId(croppedFileId);
                driveDocModel.setTextfileId(textFileId);
                driveDocModel.setPublicGuid(publicGuid);
                driveDocModel.setFolderId(parentFolderId);
                driveDocModel.setFolderName(ScanRDriveOperations.ROOT_APP_FOLDER);
                driveDocModel.setWhichCard(Constants.document);

                if (croppedFileId != null) {

                    //crop file download
                    downloadImageFile(croppedFileId, croppedFileName, imagePath -> {
                        driveDocModel.setImagePath(imagePath);
                        downloadOriginalFileAndGetInfoText(driveDocModel, originalFileName, googleDriveFileHolders,
                                filesToRemove, parentFolderId);

                    });
                } else {
                    downloadOriginalFileAndGetInfoText(driveDocModel, originalFileName, googleDriveFileHolders,
                            filesToRemove, parentFolderId);
                }
            }
        } else {
            Log.e(TAG, "download completed");
            preferenceManagement.setSyncDriveToDb(true);
            Globalarea.isDbSyncInProgress = false;
        }
    }

    private void downloadOriginalFileAndGetInfoText(DriveDocModel driveDocModel, String originalFileName,
                                                    List<GoogleDriveFileHolder> googleDriveFileHolders,
                                                    String[] filesToRemove,
                                                    String parentFolderID) {

        if (driveDocModel.getOriginalImageId() != null) {

            downloadImageFile(driveDocModel.getOriginalImageId(), originalFileName, imagePath -> {
                driveDocModel.setOriginalImagePath(imagePath);
                getInfoText(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderID);
            });
        } else {
            getInfoText(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderID);
        }

    }

    private void getInfoText(DriveDocModel driveDocModel, List<GoogleDriveFileHolder> googleDriveFileHolders,
                             String[] filesToRemove,
                             String parentFolderId) {
        if (driveDocModel.getTextfileId() != null) {
            driveServiceHelper.readFile(driveDocModel.getTextfileId())
                    .addOnSuccessListener(nameContentPair -> {
                        Log.d("found the content", nameContentPair.second);
                        Log.d("found the name", nameContentPair.first);

                        String jsonText = nameContentPair.second;
                        driveDocModel.setJsonText(jsonText);
                        Log.e("json text", jsonText);

                        Gson gson = new Gson();
                        CardDetail cardDetail = gson.fromJson(jsonText, CardDetail.class);
                        if (cardDetail != null) {
                            Log.e("card detail", "setup");
                            cardDetail.setImage_url(driveDocModel.getImagePath());
                            driveDocModel.setFileName(cardDetail.fileName);
//                            driveDocModel.setPdfFilePath(cardDetail.pdfFilePath);
                            driveDocModel.setCardDetail(cardDetail);

                            if (cardDetail.getPdfFilePath() != null) {

                                File pdfFile = new File(cardDetail.getPdfFilePath());
                                String pdfName = pdfFile.getName();

                                Log.e("pdf file name", pdfName);

                                String mimeType = "application/pdf";

                                driveServiceHelper.searchFile(pdfName, mimeType)
                                        .addOnSuccessListener(googleDriveFileHolder -> {

                                            if (googleDriveFileHolder.getId() != null) {

                                                String pdfFileId = googleDriveFileHolder.getId();

//                                                File direct = Environment.getExternalStoragePublicDirectory(
//                                                        Environment.DIRECTORY_DOWNLOADS);
//                                                String directPath = direct.getPath() + "/Scanr/";
//                                                direct = new File(directPath);
//                                                if (!direct.exists()) {
//                                                    direct.mkdirs();
//                                                }
//                                                File pdfFileDownload = new File(direct, pdfName);

                                                driveDocModel.setPdfFileID(pdfFileId);

                                                driveServiceHelper.downloadFile(pdfFile, pdfFileId)
                                                        .addOnSuccessListener(aVoid -> {

                                                            driveDocModel.setPdfFilePath(pdfFile.getAbsolutePath());
                                                            saveInDb(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderId);

                                                        }).addOnFailureListener(e -> {
                                                    e.printStackTrace();
                                                    if (connectionDetector.isConnectingToInternet1()) {
                                                        saveInDb(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderId);
                                                    } else {
                                                        saveInDb(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderId);
                                                    }

                                                });

                                            } else {
                                                //todo: pdf path found but not found in the drive
                                                saveInDb(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderId);
                                            }

                                        }).addOnFailureListener(e -> {
                                    e.printStackTrace();

                                    if (connectionDetector.isConnectingToInternet1()) {
                                        //todo: error in finding pdf
                                        saveInDb(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderId);
                                    } else {
                                        saveInDb(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderId);
                                    }

                                });

                            } else {
                                saveInDb(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderId);
                            }

                        } else {
                            saveInDb(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderId);
                        }

                    }).addOnFailureListener(e -> {
                e.printStackTrace();
//                handleFailure();
                Globalarea.isDbSyncInProgress = false;
            });
        } else {
            saveInDb(driveDocModel, googleDriveFileHolders, filesToRemove, parentFolderId);
        }
    }

    private void saveInDb(DriveDocModel driveDocModel, List<GoogleDriveFileHolder> googleDriveFileHolders,
                          String[] filesToRemove, String parentFolderId) {

        driveDocModel.setSyncStatus(SyncStatus.synced.toString());
        driveDocRepo.addDriveDocInfo(driveDocModel);

        removeTheseFilesFromListAndPickNext(googleDriveFileHolders, filesToRemove, parentFolderId);

    }

    private void removeTheseFilesFromListAndPickNext(List<GoogleDriveFileHolder> googleDriveFileHolders,
                                                     String[] filesToRemove, String parentFolderId) {
//        Log.e("remove positions", "info: " + textFilePos + ",crop: " + croppedImageFilePos + ",original:" + originalImageFilePos);
        Log.e("size of list", String.valueOf(googleDriveFileHolders.size()));

        int oldSize = googleDriveFileHolders.size();
        Log.e("old size ", String.valueOf(oldSize));
//        Toast.makeText(context, "old size:" +oldSize, Toast.LENGTH_SHORT).show();

        for (String name : filesToRemove) {
            int pos = getPositionInList(googleDriveFileHolders, name);
            Log.e("removing from pos", String.valueOf(pos));
            if (pos > -1)
                googleDriveFileHolders.remove(pos);
        }

//        int croppedImageFilePos = getPositionInList(googleDriveFileHolders, croppedFileName);
//        if (croppedImageFilePos > -1)
//            googleDriveFileHolders.remove(croppedImageFilePos);
//
//        int originalImageFilePos = getPositionInList(googleDriveFileHolders, originalFileName);
//        if (originalImageFilePos > -1)
//            googleDriveFileHolders.remove(originalImageFilePos);

        int newSize = googleDriveFileHolders.size();
        Log.e("new Size", String.valueOf(googleDriveFileHolders.size()));
//        Toast.makeText(context, "new size:" +newSize, Toast.LENGTH_SHORT).show();

        if (oldSize > newSize) {
            for (GoogleDriveFileHolder googleDriveFileHolder : googleDriveFileHolders) {
                Log.e("flle name", googleDriveFileHolder.name);
            }

            //call for the next
            if (connectionDetector.isConnectingToInternet1())
                processFilesWithUnderScore(googleDriveFileHolders, parentFolderId);
            else
                Globalarea.isDbSyncInProgress = false;
        } else {
            Globalarea.isDriveSyncInProgress = false;
        }

    }

    private void downloadImageFile(String imageFileId, String imageFileName,
                                   OnSuccessfulDownloadFile onSuccessfulDownloadFile) {
        File file = getFile(imageFileName);

        if (file != null) {
            driveServiceHelper.downloadFile(file, imageFileId).
                    addOnSuccessListener(success -> {
                        if (onSuccessfulDownloadFile != null) {
                            onSuccessfulDownloadFile.onCompleteDownloadFile(file.getAbsolutePath());
                        }
                    }).addOnFailureListener(e -> {
                Log.i("image download failure", "yes");
                e.printStackTrace();
                //handleFailure();
                Globalarea.isDbSyncInProgress = false;

            });
        } else {
//            handleFailure();
            Globalarea.isDbSyncInProgress = false;
        }
    }

    private File getFile(String fileName) {
        File imageFile;
        File rootDirOfApp = LocalFilesAndFolder.getRootDirOfApp();

        try {
            if (rootDirOfApp != null) {
//                Date date = new Date();
//                imageFile = new File(dir.getAbsolutePath()

                imageFile = new File(rootDirOfApp.getAbsolutePath()
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

                return imageFile;

            } else {
                Log.e("root dir", "null");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface OnSuccessfulDownloadFile {
        void onCompleteDownloadFile(String imagePath);
    }
}
