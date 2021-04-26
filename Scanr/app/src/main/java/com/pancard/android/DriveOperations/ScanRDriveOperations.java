package com.pancard.android.DriveOperations;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.docscan.android.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.model.CardDetail;
import com.pancard.android.utility.LocalFilesAndFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScanRDriveOperations {

    public static final String ROOT_APP_FOLDER = "ScanR - Document Scanner";

    public static final String IMAGE_FILE_NAME = "image.jpeg";
    public static final String JSON_FILE_NAME = "JSON details";
    public static final String METADATA_FILE_NAME = "metadata";
    public static final int NO_OF_FILES_IN_FOLDER = 2;
    public static final String DELETE_OPERATION = "delete";
    public static final String ADD_OPERATION = "add";
    public static final String GET_OPERATION = "get";
    public static int driveDocQueryTotal = 0;
    public static int metadataQueryTotal = 0;
    public static int driveDeleteDocTotal = 0;
    public static int driveDocQueryCounter = 0;

//    public static String AADHAR_CARD_FOLDER = "Aadhar Card";
//    public static String BUSINESS_CARD_FOLDER = "Business Card";
//    public static String CREDIT_CARD_FOLDER = "Credit Card";
//    public static String DOCUMENT_FOLDER = "Documents";
//    public static String DRIVING_LICENCE_FOLDER = "Driving Licence";
//    public static String PAN_CARD_FOLDER = "Pan Card";
//    public static String PASSPORT_FOLDER = "Passport";

    public static void createRootFolder(DriveServiceHelper driveServiceHelper,
                                        OnCompleteDriveOperations onCompleteDriveOperations) {
        driveServiceHelper.createFolder(ROOT_APP_FOLDER, null)
                .addOnSuccessListener(googleDriveFileHolder -> {
                    Log.e("successfully created", "root folder");

//                    createRootCommonDoc(driveServiceHelper, googleDriveFileHolder.getId(), null);
                    if (onCompleteDriveOperations != null)
                        onCompleteDriveOperations.onSuccess(googleDriveFileHolder.getId());

                })
                .addOnFailureListener(e -> {
                    Log.e("failed to create", "root folder");
                    String failureMessage = "Could not create folder " + ROOT_APP_FOLDER + ". Please try again.";
                    if (onCompleteDriveOperations != null)
                        onCompleteDriveOperations.onFailure(e, failureMessage);
                });
    }

    public static void createRootCommonDoc(DriveServiceHelper driveServiceHelper, String parentFolderId,
                                           OnCompleteDriveOperations onCompleteDriveOperations) {

        driveServiceHelper.createTextFile(METADATA_FILE_NAME, "", parentFolderId)
                .addOnSuccessListener(googleDriveFileHolder -> {
                    Log.e("metadata ", "created");
                    Scanner.getInstance().getPreferences().setMetadataDocDriveId(googleDriveFileHolder.getId());

                    if (onCompleteDriveOperations != null)
                        onCompleteDriveOperations.onSuccess(googleDriveFileHolder.getId());
                }).addOnFailureListener(e -> {
            Log.e("failed to create", "root metadata file");
            String failureMessage = "Could not create file " + METADATA_FILE_NAME + ". Please try again.";
            if (onCompleteDriveOperations != null)
                onCompleteDriveOperations.onFailure(e, failureMessage);
        });
    }


    public static void getRootCommonDocFromDrive(DriveServiceHelper driveServiceHelper, OnCompleteDriveOperations onCompleteDriveOperations) {
        driveServiceHelper.searchFile(METADATA_FILE_NAME, "text/plain")
                .addOnSuccessListener(googleDriveFileHolder -> {

                    if (googleDriveFileHolder.getId() != null) {
                        Scanner.getInstance().getPreferences().setMetadataDocDriveId(googleDriveFileHolder.getId());

                        if (onCompleteDriveOperations != null)
                            onCompleteDriveOperations.onSuccess(googleDriveFileHolder.getId());
                    } else {
                        Log.e("creating metadata", "root file");

                        getRootFolderIdOrCreate(driveServiceHelper, new OnCompleteDriveOperations() {
                            @Override
                            public void onSuccess(String id) {
                                createRootCommonDoc(driveServiceHelper, id, onCompleteDriveOperations);
                            }

                            @Override
                            public void onFailure(Exception e, String message) {
                                Log.e("failed to", " create the root folder");
                                String failureMessage = "Could not find file " + METADATA_FILE_NAME + ". Please try again.";
                                if (onCompleteDriveOperations != null)
                                    onCompleteDriveOperations.onFailure(e, failureMessage);
                            }
                        });

                    }

                }).addOnFailureListener(e -> {
            Log.e("failed to create", "root metadata file");
            String failureMessage = "Could not find file " + METADATA_FILE_NAME + ". Please try again.";
            if (onCompleteDriveOperations != null)
                onCompleteDriveOperations.onFailure(e, failureMessage);
        });
    }

    public static void createFolder(String folderName, String folderId, DriveServiceHelper driveServiceHelper,
                                    OnCompleteDriveOperations onCompleteDriveOperations) {
        driveServiceHelper.createFolder(folderName, folderId)
                .addOnSuccessListener(googleDriveFileHolder -> {
                    Log.e("created desired folder", " name" + folderName + "id" + folderId);
                    if (onCompleteDriveOperations != null)
                        onCompleteDriveOperations.onSuccess(googleDriveFileHolder.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("failed to create", "desired folder" + folderName);
                    String failureMessage = "Could not create folder " + folderName + ". Please try again.";
                    if (onCompleteDriveOperations != null)
                        onCompleteDriveOperations.onFailure(e, failureMessage);
                });
    }

    public static void getRootFolderIdOrCreate(DriveServiceHelper driveServiceHelper, OnCompleteDriveOperations onCompleteDriveOperations) {
        driveServiceHelper.searchFolder(ROOT_APP_FOLDER)
                .addOnSuccessListener(googleDriveFileHolder -> {

                    if (googleDriveFileHolder.getId() != null) {
                        String folderId = googleDriveFileHolder.getId();
                        Log.e("found", "root folder" + folderId);
                        if (onCompleteDriveOperations != null)
                            onCompleteDriveOperations.onSuccess(googleDriveFileHolder.getId());
                    } else {
                        Log.e("creating", "root folder");
                        createRootFolder(driveServiceHelper, onCompleteDriveOperations);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    if (onCompleteDriveOperations != null)
                        onCompleteDriveOperations.onFailure(e, "Issue in accessing the folder");
                });

    }

    public static void getCategoryFolderAndCreateFolder(String categoryFolderName, String subFolderName,
                                                        DriveServiceHelper driveServiceHelper,
                                                        OnCompleteDriveOperations onCompleteDriveOperations) {

        ScanRDriveOperations.getFolderIdOrCreate(categoryFolderName, driveServiceHelper,
                new ScanRDriveOperations.OnCompleteDriveOperations() {
                    @Override
                    public void onSuccess(String folderId) {

                        driveServiceHelper.searchFolder(subFolderName)
                                .addOnSuccessListener(googleDriveFileHolder -> {
                                    //returning the folder id

                                    if (googleDriveFileHolder.getId() != null) {
                                        Log.e("found desired folder", " name: " + subFolderName + " id: " + folderId);
                                        if (onCompleteDriveOperations != null)
                                            onCompleteDriveOperations.onSuccess(googleDriveFileHolder.getId());
                                    } else {
                                        Log.e("creating", "desired folder" + subFolderName);
                                        createFolder(subFolderName, folderId, driveServiceHelper, onCompleteDriveOperations);
                                    }

                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    if (onCompleteDriveOperations != null)
                                        onCompleteDriveOperations.onFailure(e, "Issue in accessing the folder");
                                });

                        Log.e("folder id", "is: " + folderId);


                    }

                    @Override
                    public void onFailure(Exception e, String message) {

                        if (onCompleteDriveOperations != null)
                            onCompleteDriveOperations.onFailure(e, message);

                    }
                });
    }

    public static void getFolderIdOrCreate(String folderName,
                                           DriveServiceHelper driveServiceHelper,
                                           OnCompleteDriveOperations onCompleteDriveOperations) {

        //first getting the root folder/ creating root folder
        ScanRDriveOperations.getRootFolderIdOrCreate(driveServiceHelper, new ScanRDriveOperations.OnCompleteDriveOperations() {
            @Override
            public void onSuccess(String folderId) {

                //search for the desired folder
                driveServiceHelper.getCategotyFolder(folderName, folderId)
                        .addOnSuccessListener(googleDriveFileHolder -> {
                            //returning the folder id

                            if (googleDriveFileHolder.getId() != null) {
                                Log.e("found desired folder", " name: " + folderName + " id: " + folderId);
                                if (onCompleteDriveOperations != null)
                                    onCompleteDriveOperations.onSuccess(googleDriveFileHolder.getId());
                            } else {
                                Log.e("creating", "desired folder" + folderName);
                                createFolder(folderName, folderId, driveServiceHelper, onCompleteDriveOperations);
                            }


                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            if (onCompleteDriveOperations != null)
                                onCompleteDriveOperations.onFailure(e, "Issue in accessing the folder");
                        });
            }

            @Override
            public void onFailure(Exception e, String message) {

                Log.e("root folder not found", "failed to create it.");
                if (onCompleteDriveOperations != null)
                    onCompleteDriveOperations.onFailure(e, message);
            }
        });

    }

    public static void uploadAndRenameImageFile(File file, String mimeType, String folderId,
                                                DriveServiceHelper driveServiceHelper,
                                                OnCompleteDriveOperations onCompleteDriveOperations) {
        driveServiceHelper.uploadFile(file, mimeType, folderId).addOnSuccessListener(googleDriveFileHolder -> {
            Log.e("successfully uploaded", "file");

            String fileId = googleDriveFileHolder.getId();
            Log.e("old name", googleDriveFileHolder.getName());
            driveServiceHelper.renameFile(fileId, IMAGE_FILE_NAME)
                    .addOnSuccessListener(aVoid -> {
                        if (onCompleteDriveOperations != null)
                            onCompleteDriveOperations.onSuccess(fileId);
                    }).addOnFailureListener(e -> {
                if (onCompleteDriveOperations != null) {
                    onCompleteDriveOperations.onFailure(e, e.getMessage());
                }

            });

        }).addOnFailureListener(e -> {
            if (onCompleteDriveOperations != null)
                onCompleteDriveOperations.onFailure(e, e.getMessage());
        });
    }

    public static void getListOfDocsFromFolder(String folderName, DriveServiceHelper driveServiceHelper,
                                               OnCompleteDriveQueries onCompleteDriveQueries) {

        driveServiceHelper.searchFolder(folderName)
                .addOnSuccessListener(googleDriveFileHolder -> {

                    if (googleDriveFileHolder.getId() != null) {
                        String folderId = googleDriveFileHolder.getId();
                        Log.e("found", "searched folder" + folderId);
                        queryFiles(folderName, folderId, driveServiceHelper, onCompleteDriveQueries);

                    } else {
                        String message = "No folder found";
                        if (onCompleteDriveQueries != null)
                            onCompleteDriveQueries.onFailure(new Exception(message), message);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    if (onCompleteDriveQueries != null)
                        onCompleteDriveQueries.onFailure(e, "Issue in accessing the folder");
                });

    }

    private static void queryFiles(String folderName, String folderId, DriveServiceHelper driveServiceHelper,
                                   OnCompleteDriveQueries onCompleteDriveQueries) {

        driveServiceHelper.queryFiles(folderId)
                .addOnSuccessListener(googleDriveFileHolders -> {

                    if (googleDriveFileHolders != null && googleDriveFileHolders.size() > 0) {
                        Log.e("found no. of files", String.valueOf(googleDriveFileHolders.size()));

                        driveDocQueryTotal = googleDriveFileHolders.size();
                        List<DriveDocModel> driveDocModels = new ArrayList<>();

                        for (GoogleDriveFileHolder googleDriveFileHolder : googleDriveFileHolders) {
                            driveDocModels.add(new DriveDocModel(googleDriveFileHolder));
                        }

                        downloadFiles(googleDriveFileHolders, driveServiceHelper, driveDocModels,
                                0, onCompleteDriveQueries);

//                        onCompleteDriveQueries.onSuccessfulDriveQuery(googleDriveFileHolders);
                    } else {
                        String message = "No files found under the document";
                        if (onCompleteDriveQueries != null)
                            onCompleteDriveQueries.onFailure(new Exception(message), message);
                    }

                }).addOnFailureListener(e -> {
            e.printStackTrace();
            if (onCompleteDriveQueries != null)
                onCompleteDriveQueries.onFailure(e, "Issue in accessing the files of the folder");
        });

    }

    public static void deleteFolder(List<DriveDocModel> driveDocModels, DriveServiceHelper driveServiceHelper, int currentCounter,
                                    OnCompleteDriveOperations completeDriveOperations) {

        String folderId = driveDocModels.get(currentCounter).getFolderId();

        int nextCounter = currentCounter + 1;

        driveServiceHelper.deleteFolderFile(folderId)
                .addOnSuccessListener((googleDriveFileHolder) -> {

                    driveDeleteDocTotal = driveDocModels.size();

                    if (nextCounter < driveDeleteDocTotal) {
                        deleteFolder(driveDocModels, driveServiceHelper, nextCounter, completeDriveOperations);
                    } else {
                        completeDriveOperations.onSuccess(Globalarea.getStringOfDriveDoc(driveDocModels.get(currentCounter)));
                    }

                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    if (completeDriveOperations != null)
                        completeDriveOperations.onFailure(e, "Issue in accessing the folder");
                });
    }

    public static void downloadFiles(List<GoogleDriveFileHolder> googleDriveFileHolders,
                                     DriveServiceHelper driveServiceHelper,
                                     List<DriveDocModel> driveDocModels, int currentCounter,
                                     OnCompleteDriveQueries onCompleteDriveQueries) {

        Log.e("current counter", String.valueOf(currentCounter));
        int nextCounter = currentCounter + 1;

        String folderId = googleDriveFileHolders.get(currentCounter).getId();

        driveServiceHelper.queryFiles(folderId).addOnSuccessListener(googleDriveFileHolderList -> {

            if (googleDriveFileHolderList.size() == NO_OF_FILES_IN_FOLDER) {

                String fileName1 = googleDriveFileHolderList.get(0).getName().toLowerCase();
                String fileName2 = googleDriveFileHolderList.get(1).getName().toLowerCase();

                String imageFileId = null;
                if (fileName1.equals(IMAGE_FILE_NAME) || fileName1.contains(IMAGE_FILE_NAME)) {
                    imageFileId = googleDriveFileHolderList.get(0).getId();
                } else if (fileName2.equals(IMAGE_FILE_NAME) || fileName2.contains(IMAGE_FILE_NAME)) {
                    imageFileId = googleDriveFileHolderList.get(1).getId();
                } else {
                    Log.i("Image is", "Null");
                    if (nextCounter < driveDocQueryTotal) {
                        downloadFiles(googleDriveFileHolders, driveServiceHelper, driveDocModels,
                                nextCounter, onCompleteDriveQueries);
                    } else {
                        if (onCompleteDriveQueries != null)
                            onCompleteDriveQueries.onSuccessfulDriveQuery(driveDocModels);
                    }
                    return;
                }

                String textFileId;
                if (fileName1.equals(JSON_FILE_NAME.toLowerCase()) || fileName1.contains(JSON_FILE_NAME)) {
                    textFileId = googleDriveFileHolderList.get(0).getId();
                } else if (fileName2.equals(JSON_FILE_NAME.toLowerCase()) || fileName2.contains(JSON_FILE_NAME.toLowerCase())) {
                    textFileId = googleDriveFileHolderList.get(1).getId();
                } else {

                    Log.e("file name 0", googleDriveFileHolderList.get(0).getName());
                    Log.e("file name 1", googleDriveFileHolderList.get(1).getName());
                    Log.e("no text file dound", "skipping to next");
                    downloadFiles(googleDriveFileHolders, driveServiceHelper, driveDocModels,
                            nextCounter, onCompleteDriveQueries);
//                    if (onCompleteDriveQueries != null)
//                        onCompleteDriveQueries.onFailure(new Exception("no text file found of item " + currentCounter), "We could not fetch the docs from drive");
                    return;
                }

                String finalImageFileId = imageFileId;

                DriveDocModel currentDriveDocModel = driveDocModels.get(currentCounter);
                currentDriveDocModel.setImagefileId(finalImageFileId);
                currentDriveDocModel.setTextfileId(textFileId);

                driveServiceHelper.readFile(textFileId).addOnSuccessListener(nameContentPair -> {
                    Log.d("found the content", nameContentPair.second);
                    Log.d("found the name", nameContentPair.first);

                    String jsonText = nameContentPair.second;
                    currentDriveDocModel.setJsonText(jsonText);

                    Gson gson = new Gson();
                    CardDetail cardDetail = gson.fromJson(jsonText, CardDetail.class);

                    currentDriveDocModel.setCardDetail(cardDetail);

                    try {

                        String tag = currentDriveDocModel.getWhichCard();
                        String name = currentDriveDocModel.getCardDetail().getCard_name();
                        String fileName = name.substring(0, Math.min(name.length() - 1, 10)) + System.currentTimeMillis();
                        if (tag == null)
                            tag = currentDriveDocModel.getCardDetail().whichcard;

                        File subDir = LocalFilesAndFolder.getSubDir(tag, FirebaseAuth.getInstance().getCurrentUser().getUid());

//                        File file = File.createTempFile(finalImageFileId, ".jpg");

                        File file = new File(subDir.getAbsolutePath()
                                + File.separator
                                + fileName + ".jpg");
                        file.createNewFile();

                        driveServiceHelper.downloadFile(file, finalImageFileId).
                                addOnSuccessListener(success -> {
                                    Log.i("download success", "yes");

                                    String filePath = file.getAbsolutePath();
                                    cardDetail.setImage_url(filePath);
                                    currentDriveDocModel.setImagePath(filePath);

                                    if (nextCounter < driveDocQueryTotal) {
                                        downloadFiles(googleDriveFileHolders, driveServiceHelper, driveDocModels,
                                                nextCounter, onCompleteDriveQueries);
                                    } else {
                                        if (onCompleteDriveQueries != null)
                                            onCompleteDriveQueries.onSuccessfulDriveQuery(driveDocModels);
                                    }

                                }).addOnFailureListener(e -> {
                            Log.i("download failure", "yes");

                            e.printStackTrace();

                            if (nextCounter < driveDocQueryTotal) {
                                downloadFiles(googleDriveFileHolders, driveServiceHelper, driveDocModels,
                                        nextCounter, onCompleteDriveQueries);
                            } else {
                                if (onCompleteDriveQueries != null)
                                    onCompleteDriveQueries.onSuccessfulDriveQuery(driveDocModels);
                            }

                        });

                    } catch (IOException e) {
                        e.printStackTrace();

                        if (nextCounter < driveDocQueryTotal) {
                            downloadFiles(googleDriveFileHolders, driveServiceHelper, driveDocModels,
                                    nextCounter, onCompleteDriveQueries);
                        } else {
                            if (onCompleteDriveQueries != null)
                                onCompleteDriveQueries.onSuccessfulDriveQuery(driveDocModels);
                        }
                    }
                }).addOnFailureListener(e -> {
                    Log.e("text file", "fetch failure");
                    if (onCompleteDriveQueries != null)
                        onCompleteDriveQueries.onFailure(new Exception("no text file found of item " + currentCounter), "We could not fetch the docs from drive");
                });

            } else {
                if (onCompleteDriveQueries != null)
                    onCompleteDriveQueries.onFailure(new Exception("more than 2 files found on the folder. something is wrong with it."), "We could not fetch the docs from drive");
            }


        }).addOnFailureListener(e -> {
            e.printStackTrace();
            if (onCompleteDriveQueries != null)
                onCompleteDriveQueries.onFailure(e, "We could not fetch the docs from drive");
        });

    }

    private static void queryFiles(String folderId, DriveServiceHelper driveServiceHelper,
                                   OnCompleteDriveQueries onCompleteDriveQueries) {

        driveServiceHelper.queryFiles(folderId)
                .addOnSuccessListener(googleDriveFileHolders -> {

                    if (googleDriveFileHolders != null && googleDriveFileHolders.size() > 0) {
                        Log.e("found no. of files", String.valueOf(googleDriveFileHolders.size()));

                        driveDocQueryTotal = googleDriveFileHolders.size();
                        List<DriveDocModel> driveDocModels = new ArrayList<>();

                        for (GoogleDriveFileHolder googleDriveFileHolder : googleDriveFileHolders) {
                            driveDocModels.add(new DriveDocModel(googleDriveFileHolder));
                        }

                        downloadFiles(googleDriveFileHolders, driveServiceHelper, driveDocModels,
                                0, onCompleteDriveQueries);

//                        onCompleteDriveQueries.onSuccessfulDriveQuery(googleDriveFileHolders);
                    } else {
                        String message = "No files found under the document";
                        if (onCompleteDriveQueries != null)
                            onCompleteDriveQueries.onFailure(new Exception(message), message);
                    }

                }).addOnFailureListener(e -> {
            e.printStackTrace();
            if (onCompleteDriveQueries != null)
                onCompleteDriveQueries.onFailure(e, "Issue in accessing the files of the folder");
        });

    }

    public static Intent getGoogleSignInIntent(Activity activity) {
        GoogleSignInClient mGoogleSignInClient;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.google_client_id))
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);


        return mGoogleSignInClient.getSignInIntent();
    }

    public static void handleGoogleSignInActivityResult(Activity activity, Intent data,
                                                        OnCompleteGoogleSignIn onCompleteGoogleSignIn) {

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);

            if (account != null) {
                if (onCompleteGoogleSignIn != null)
                    onCompleteGoogleSignIn.onSuccessfulGoogleSignIn(account);
            } else {
                String strMessage = "failed - null firebase account";
                if (onCompleteGoogleSignIn != null)
                    onCompleteGoogleSignIn.onFailure(new Exception(strMessage), strMessage);
            }

        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Log.w("Google sign in failed", e);
            Log.e("failed", " on activity result" + e.getMessage());
            Toast.makeText(activity, "Failed to sign in with google. Please try again", Toast.LENGTH_SHORT).show();
            // [START_EXCLUDE]
//                updateUI(null);
            // [END_EXCLUDE]
        }
    }

    public static void findMetadataDocForOperation(DriveServiceHelper driveServiceHelper,
                                                   OnCompleteMetaDataQueries onCompleteMetaDataQueries) {

        String metadataID = Scanner.getInstance().getPreferences().getMetadataDocDriveId();
        if (metadataID != null) {
            Log.i("First metadataid", metadataID);
            getMetaDataContent(driveServiceHelper, metadataID, onCompleteMetaDataQueries);
//            getContentFromMetadata(driveServiceHelper, metadataID, metadataModels, operationType, onCompleteDriveOperations);
        } else {
            getRootCommonDocFromDrive(driveServiceHelper, new OnCompleteDriveOperations() {
                @Override
                public void onSuccess(String metadataID) {
                    Log.e("second metadataid", metadataID);
                    Scanner.getInstance().getPreferences().setMetadataDocDriveId(metadataID);
                    getMetaDataContent(driveServiceHelper, metadataID, onCompleteMetaDataQueries);
//                    getContentFromMetadata(driveServiceHelper, metadataID, metadataModels, operationType, onCompleteDriveOperations);
                }

                @Override
                public void onFailure(Exception e, String message) {
                    e.printStackTrace();
                    if (onCompleteMetaDataQueries != null)
                        onCompleteMetaDataQueries.onFailure(e, "Issue in accessing the metadata file. Please try again");
                }
            });
        }
    }

    private static void getContentFromMetadata(DriveServiceHelper driveServiceHelper, String metadataID,
                                               List<MetadataModel> rawMetadataModelList, String operationType,
                                               OnCompleteDriveOperations onCompleteDriveOperations) {

        Log.e("reading from", "metadata content");
        driveServiceHelper.readFile(metadataID).addOnSuccessListener(nameContentPair -> {

            Log.e("on success content get", "finally");
            String jsonString = nameContentPair.second;

            List<MetadataModel> metadataModels;
            String finalJsonMetadata = "";
            if (jsonString != null && jsonString.trim().length() > 0) {

                Gson gson = new Gson();
                TypeToken<List<MetadataModel>> token = new TypeToken<List<MetadataModel>>() {
                };
                metadataModels = gson.fromJson(jsonString, token.getType());

                if (operationType.equals(ADD_OPERATION)) {
                    finalJsonMetadata = addDoc(metadataModels, rawMetadataModelList.get(0));
                    Log.e("final data", finalJsonMetadata);
                } else if (operationType.equals(DELETE_OPERATION)) {
                    finalJsonMetadata = deleteDoc(metadataModels, rawMetadataModelList);
                } else if (operationType.equals(GET_OPERATION)) {
                    finalJsonMetadata = jsonString;
                    if (onCompleteDriveOperations != null) {
                        onCompleteDriveOperations.onSuccess(finalJsonMetadata);
                    }
                }

            } else {
                if (operationType.equals(ADD_OPERATION)) {
                    metadataModels = new ArrayList<>();
//                    metadataModels.add(rawMetadataModelList.get(0));

                    finalJsonMetadata = addDoc(metadataModels, rawMetadataModelList.get(0));

                } else if (operationType.equals(DELETE_OPERATION)) {
                    if (onCompleteDriveOperations != null)
                        onCompleteDriveOperations.onFailure(new Exception("Can't delete the non existent document"), "Issue in deleting the doc file. Please try again");
                } else if (operationType.equals(GET_OPERATION)) {
                    if (onCompleteDriveOperations != null) {
                        onCompleteDriveOperations.onFailure(new Exception("Can't get the details from non existent document"), "Issue in getting the doc file. Please try again");
                    }
                }
            }

            if (!operationType.equals(GET_OPERATION)) {
                if (finalJsonMetadata == null) {
                    if (onCompleteDriveOperations != null)
                        onCompleteDriveOperations.onFailure(new Exception("Can't delete the non existent document"), "Issue in deleting the doc file. Please try again");
                } else {
                    Log.e("saving back", "to json file");
                    driveServiceHelper.saveFile(metadataID, METADATA_FILE_NAME, finalJsonMetadata)
                            .addOnSuccessListener(aVoid -> {
                                if (onCompleteDriveOperations != null)
                                    onCompleteDriveOperations.onSuccess("Success");
                            }).addOnFailureListener(e -> {
                        e.printStackTrace();
                        if (onCompleteDriveOperations != null)
                            onCompleteDriveOperations.onFailure(e, "no document found for deletion");
                    });
                }
            } else {
                if (onCompleteDriveOperations != null)
                    onCompleteDriveOperations.onFailure(new Exception("Can't get the non existent document"), "Issue in getting the doc file. Please try again");
            }


        }).addOnFailureListener(e -> {
            Log.e("on failure", "content get");
            e.printStackTrace();
            if (onCompleteDriveOperations != null)
                onCompleteDriveOperations.onFailure(e, "Issue in accessing the metadata file. Please try again");
        });
    }

    public static void getMetaDataContent(DriveServiceHelper driveServiceHelper, String metadataID,
                                          OnCompleteMetaDataQueries onCompleteMetaDataQueries) {


        Log.e("reading from", "metadata content");
        driveServiceHelper.readFile(metadataID).addOnSuccessListener(nameContentPair -> {

            Log.e("on success content get", "finally");
            String jsonString = nameContentPair.second;

            List<MetadataModel> metadataModels;

            if (jsonString != null && jsonString.trim().length() > 0) {
                Gson gson = new Gson();
                TypeToken<List<MetadataModel>> token = new TypeToken<List<MetadataModel>>() {
                };
                metadataModels = gson.fromJson(jsonString, token.getType());
            } else {
                Log.e("No data", "yes");
                metadataModels = new ArrayList<>();
            }

            if (onCompleteMetaDataQueries != null)
                onCompleteMetaDataQueries.onSuccessFullMetadataContent(metadataID, metadataModels);

        }).addOnFailureListener(e -> {
            Log.e("on failure", "content get");
            e.printStackTrace();
            if (onCompleteMetaDataQueries != null)
                onCompleteMetaDataQueries.onFailure(e, "Issue in accessing the metadata file. Please try again");
        });

    }

    public static void addDocInMetadata(DriveServiceHelper driveServiceHelper, String metadataFileId,
                                        List<MetadataModel> metadataModels,
                                        MetadataModel metadataModelToBeAdded,
                                        OnCompleteDriveOperations onCompleteDriveOperations) {

        String finalJsonMetadata = addDoc(metadataModels, metadataModelToBeAdded);
        updateMetaDataContent(driveServiceHelper, finalJsonMetadata, metadataFileId, onCompleteDriveOperations);

    }

    public static void deleteDocInMetadata(DriveServiceHelper driveServiceHelper, String metadataFileId,
                                           List<MetadataModel> metadataModels,
                                           List<MetadataModel> metadataModelsTobeDeleted,
                                           OnCompleteDriveOperations onCompleteDriveOperations) {
        String finalJsonMetadata = deleteDoc(metadataModels, metadataModelsTobeDeleted);

        updateMetaDataContent(driveServiceHelper, finalJsonMetadata, metadataFileId, onCompleteDriveOperations);
    }

    public static void updateMetaDataContent(DriveServiceHelper driveServiceHelper,
                                             String finalJsonMetadata, String metadataFileId,
                                             OnCompleteDriveOperations onCompleteDriveOperations) {
        if (finalJsonMetadata == null) {
            if (onCompleteDriveOperations != null)
                onCompleteDriveOperations.onFailure(new Exception("Can't delete the non existent document"), "Issue in deleting the doc file. Please try again");
        } else {
            Log.e("saving back", "to json file");
            driveServiceHelper.saveFile(metadataFileId, METADATA_FILE_NAME, finalJsonMetadata)
                    .addOnSuccessListener(aVoid -> {
                        if (onCompleteDriveOperations != null)
                            onCompleteDriveOperations.onSuccess(finalJsonMetadata);
                    }).addOnFailureListener(e -> {
                e.printStackTrace();
                if (onCompleteDriveOperations != null)
                    onCompleteDriveOperations.onFailure(e, "no document found for deletion");
            });
        }
    }

    private static String deleteDoc(List<MetadataModel> metadataModels,
                                    List<MetadataModel> metadataModelListTobeDeleted) {
        if (metadataModels != null && metadataModels.size() > 0) {
            try {
                Log.e("deleting items ", String.valueOf(metadataModelListTobeDeleted.size()));
                for (MetadataModel metadataModel : metadataModelListTobeDeleted) {

                    if (containsObjectInList(metadataModels, metadataModel)) {
                        Log.e("removing", "object");
                        //Todo:check if this is working or not.
                        metadataModels.remove(metadataModel);
                    } else
                        Log.e("not ", "containing");
                }

                Gson gson = new Gson();
                return gson.toJson(metadataModels);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean containsObjectInList(List<MetadataModel> metadataModelList, MetadataModel metadataModel) {

        for (MetadataModel metadataModel1 : metadataModelList) {
            if (metadataModel1.getFileDocFolderName().equals(metadataModel.getFileDocFolderName()))
                return true;
        }

        return false;
    }

    public static int getPosInList(List<MetadataModel> metadataModelList, MetadataModel metadataModel) {
        //todo: fix this issue filedocfolder name null.
        try {
            for (int i = 0; i < metadataModelList.size(); i++) {
                MetadataModel metadataModel1 = metadataModelList.get(i);
                if (metadataModel1.getFileDocFolderName().equals(metadataModel.getFileDocFolderName()))
                    return i;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static String addDoc(List<MetadataModel> metadataModels, MetadataModel metadataModel) {
        Log.e("adding into", "metadata doc");
        if (metadataModels != null && metadataModels.size() > 0) {
            metadataModels.add(metadataModel);
        } else {
            metadataModels = new ArrayList<>();
            metadataModels.add(metadataModel);
        }

        Gson gson = new Gson();
        return gson.toJson(metadataModels);
    }

//    public static void getDownloadDriveFile(String fileId, DriveServiceHelper
//            driveServiceHelper, OnCompleteDriveOperations onCompleteDriveOperations) {
//
//        driveServiceHelper.downloadFile(null, fileId, "application/vnd.google-apps.script+json").
//                addOnSuccessListener(success -> {
//                    Log.i("download success", "yes");
//                    onCompleteDriveOperations.onSuccess("DOWNLOADED");
//                }).addOnFailureListener(e -> {
//            Log.i("download failure", "yes");
//            onCompleteDriveOperations.onFailure(e, "FAILURE");
//        });
//    }

    public static void getDocumentsDetails(DriveServiceHelper mDriveServiceHelper, OnCompleteDriveQueries onCompleteDriveQueries) {

        ScanRDriveOperations.findMetadataDocForOperation(mDriveServiceHelper,
                new OnCompleteMetaDataQueries() {
                    @Override
                    public void onSuccessFullMetadataContent(String metadataFileId,
                                                             List<MetadataModel> metadataModels) {


                        if (metadataModels != null && metadataModels.size() > 0) {

                            List<DriveDocModel> driveDocModelList = new ArrayList<>();
                            for (MetadataModel metadataModel : metadataModels) {
                                driveDocModelList.add(new DriveDocModel());
                            }

                            Log.e("size of the syncing", String.valueOf(metadataModels.size()));
                            ScanRDriveOperations.readTextFileAndDownloadImage(metadataModels,
                                    driveDocModelList, mDriveServiceHelper, 0,
                                    onCompleteDriveQueries);

                        } else {
                            if (onCompleteDriveQueries != null)
                                onCompleteDriveQueries.onSuccessfulDriveQuery(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String message) {
                        if (onCompleteDriveQueries != null)
                            onCompleteDriveQueries.onFailure(e, message);
                    }
                });
    }

    public static void readTextFileAndDownloadImage(List<MetadataModel> metadataModelList,
                                                    List<DriveDocModel> driveDocModelLists,
                                                    DriveServiceHelper driveServiceHelper,
                                                    int currentCounter,
                                                    OnCompleteDriveQueries onCompleteDriveQueries) {

        Log.e("current counter", String.valueOf(currentCounter));
        MetadataModel currentMetadataModel = metadataModelList.get(currentCounter);
        DriveDocModel currentDriveDocModel = driveDocModelLists.get(currentCounter);

        currentDriveDocModel.setDriveDocInfo(currentMetadataModel);

        int nextCounter = currentCounter + 1;

        metadataQueryTotal = metadataModelList.size();

        String textFileId = currentMetadataModel.getJsonFileID();
        Log.e("textfile id", currentMetadataModel.getJsonFileID());

        if (textFileId == null) {
            Log.e("text file id", "is null");
            readTextFileAndDownloadImage(metadataModelList, driveDocModelLists, driveServiceHelper,
                    nextCounter, onCompleteDriveQueries);
            return;
        }

        Log.e("reading file", "json file");

        driveServiceHelper.readFile(textFileId).addOnSuccessListener(nameContentPair -> {
            Log.d("found the content", nameContentPair.second);
            Log.d("found the name", nameContentPair.first);

            String jsonText = nameContentPair.second;

            Gson gson = new Gson();
            CardDetail cardDetail = gson.fromJson(jsonText, CardDetail.class);
            currentDriveDocModel.setCardDetail(cardDetail);
            Log.e("card detail", "setup");
            currentDriveDocModel.setJsonText(jsonText);
            Log.e("json text", jsonText);

            String imageFileId = currentMetadataModel.getImageFileId();
            if (imageFileId == null) {
                Log.e("image file id", "null");
                readTextFileAndDownloadImage(metadataModelList, driveDocModelLists, driveServiceHelper,
                        nextCounter, onCompleteDriveQueries);
                return;
            }

            try {

                String tag = currentDriveDocModel.getWhichCard();
                String name = currentDriveDocModel.getCardDetail().getCard_name();
                String fileName = name.substring(0, Math.min(name.length() - 1, 10)) + System.currentTimeMillis();
                Log.e("file name", fileName);


                File subDir = LocalFilesAndFolder.getSubDir(tag, FirebaseAuth.getInstance().getCurrentUser().getUid());

//                        File file = File.createTempFile(finalImageFileId, ".jpg");

                File file = new File(subDir.getPath()
                        + File.separator
                        + fileName + ".jpg");
                boolean created = file.createNewFile();

                if (created)
                    Log.e("file is created", "succesfully at " + file.getPath());
                else
                    Log.e("file not created", "oops");

//                        File file = File.createTempFile(imageFileId, ".jpg");

                driveServiceHelper.downloadFile(file, imageFileId).
                        addOnSuccessListener(success -> {
                            if (file.getAbsolutePath() != null) {
                                Log.e("downloaded success at ", file.getAbsolutePath());
                                String filePath = file.getAbsolutePath();
                                cardDetail.setImage_url(filePath);

                                currentDriveDocModel.setImagePath(file.getAbsolutePath());

                                if (nextCounter < metadataQueryTotal) {
                                    readTextFileAndDownloadImage(metadataModelList, driveDocModelLists, driveServiceHelper,
                                            nextCounter, onCompleteDriveQueries);
                                } else {
                                    if (onCompleteDriveQueries != null)
                                        onCompleteDriveQueries.onSuccessfulDriveQuery(driveDocModelLists);
                                }
                            } else {
                                Log.e("filepath is", "null");
                            }

                        }).addOnFailureListener(e -> {
                    Log.i("image download failure", "yes");

                    e.printStackTrace();

                    if (nextCounter < metadataQueryTotal) {
                        Log.e("moving to next", String.valueOf(nextCounter));
                        readTextFileAndDownloadImage(metadataModelList, driveDocModelLists, driveServiceHelper,
                                nextCounter, onCompleteDriveQueries);
                    } else {
                        Log.e("returning ", "success");
                        if (onCompleteDriveQueries != null)
                            onCompleteDriveQueries.onSuccessfulDriveQuery(driveDocModelLists);
                    }

                });

            } catch (IOException e) {
                e.printStackTrace();

                if (nextCounter < metadataQueryTotal) {
                    readTextFileAndDownloadImage(metadataModelList, driveDocModelLists, driveServiceHelper,
                            nextCounter, onCompleteDriveQueries);
                } else {
                    if (onCompleteDriveQueries != null)
                        onCompleteDriveQueries.onSuccessfulDriveQuery(driveDocModelLists);
                }
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();

            Log.e("text file", "fetch failure");
            if (nextCounter < metadataQueryTotal) {
                readTextFileAndDownloadImage(metadataModelList, driveDocModelLists, driveServiceHelper,
                        nextCounter, onCompleteDriveQueries);
            } else {
                if (onCompleteDriveQueries != null)
                    onCompleteDriveQueries.onSuccessfulDriveQuery(driveDocModelLists);
            }
//                    if (onCompleteDriveQueries != null)
//                        onCompleteDriveQueries.onFailure(new Exception("no text file found of item " + currentCounter), "We could not fetch the docs from drive");
        });

    }

    public static void startDownloadingAllFiles(DriveServiceHelper driveServiceHelper,
                                                OnCompleteDriveFileQueries onCompleteDriveQueries) {

        Log.e("starting", "download");
        driveServiceHelper.searchFolder(ROOT_APP_FOLDER)
                .addOnSuccessListener(googleDriveFileHolder -> {

                    if (googleDriveFileHolder.getId() != null) {
                        String folderId = googleDriveFileHolder.getId();
                        Log.e("found", "root folder" + folderId);
                        getAllFilesFromRoot(driveServiceHelper, folderId, onCompleteDriveQueries);

                    } else {
                        String message = "No root folder";

                        if (onCompleteDriveQueries != null)
                            onCompleteDriveQueries.onFailure(new Exception(message), message);
                    }

                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    if (onCompleteDriveQueries != null)
                        onCompleteDriveQueries.onFailure(e, "No root folder");
                });
    }

    public static void getAllFilesFromRoot(DriveServiceHelper driveServiceHelper,
                                           String rootFolderId,
                                           OnCompleteDriveFileQueries onCompleteDriveFileQueries) {

        driveServiceHelper.queryFiles(rootFolderId)
                .addOnSuccessListener(googleDriveFileHolders -> {

                    if (googleDriveFileHolders != null && googleDriveFileHolders.size() > 0) {
                        Log.e("found no. of files", String.valueOf(googleDriveFileHolders.size()));

                        if (onCompleteDriveFileQueries != null)
                            onCompleteDriveFileQueries.onSuccessfulDriveFileQueries(googleDriveFileHolders, rootFolderId);

                    } else {
                        //todo: why it is going here.

                        //todo: no files
                        String message = "No files found under the document";
                        if (onCompleteDriveFileQueries != null)
                            onCompleteDriveFileQueries.onFailure(new Exception(message), message);
                    }

                }).addOnFailureListener(e -> {
            e.printStackTrace();
            String message = "Can not get files from root";
            if (onCompleteDriveFileQueries != null)
                onCompleteDriveFileQueries.onFailure(new Exception(message), message);
        });

    }

    public interface OnCompleteDriveOperations {
        void onSuccess(String message);

        void onFailure(Exception e, String message);
    }

    public interface OnCompleteDriveFileQueries {
        void onSuccessfulDriveFileQueries(List<GoogleDriveFileHolder> googleDriveFileHolders, String parentId);

        void onFailure(Exception e, String message);
    }

    public interface OnCompleteDriveQueries {
        void onSuccessfulDriveQuery(List<DriveDocModel> googleDriveFileHolderList);

        void onFailure(Exception e, String message);
    }

//    public interface OnCompleteDriveFileRead {
//
//        void onSuccessfulDriveFileRead(CardDetail jsonContent, Bitmap imageCard, String textFileId);
//
//        void onFailure(Exception e, String message);
//    }


    public interface OnCompleteMetaDataQueries {
        void onSuccessFullMetadataContent(String metadataFileId, List<MetadataModel> metadataModels);

        void onFailure(Exception e, String message);
    }

    public interface OnCompleteGoogleSignIn {
        void onSuccessfulGoogleSignIn(GoogleSignInAccount account);

        void onFailure(Exception e, String message);
    }

}

