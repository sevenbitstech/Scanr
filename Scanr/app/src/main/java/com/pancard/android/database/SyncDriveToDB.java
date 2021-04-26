package com.pancard.android.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.DriveOperations.DriveServiceHelper;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Scanner;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.PreferenceManagement;

import java.util.List;

public class SyncDriveToDB {

    public static void startSyncDriveToDb(Context context, DriveServiceHelper driveServiceHelper, String userId) {
        ConnectionDetector connectionDetector = new ConnectionDetector(context);
        PreferenceManagement preferences = Scanner.getInstance().getPreferences();

        if (connectionDetector.isConnectingToInternet()) {

            if (preferences.isDriveConnected()) {

                ScanRDriveOperations.getDocumentsDetails(driveServiceHelper,
                        new ScanRDriveOperations.OnCompleteDriveQueries() {
                            @Override
                            public void onSuccessfulDriveQuery(List<DriveDocModel> driveDocModelList) {
                                if (driveDocModelList != null && driveDocModelList.size() > 0) {
                                    Log.i("drivedocmodel size", String.valueOf(driveDocModelList.size()));
                                    addInDatabase(context, driveDocModelList, userId);
                                }
                            }

                            @Override
                            public void onFailure(Exception e, String message) {
                                Log.i("error getting metadata", message);
                            }
                        });


            }
        }
    }

    private static void addInDatabase(Context context, List<DriveDocModel> driveDocModelList, String userId) {

        //todo: Uncomment below code for separate DB with Firebase userid
//        DriveDocRepo driveDocRepo = new DriveDocRepo(context, userId);

        DriveDocRepo driveDocRepo = new DriveDocRepo(context);

        Log.e("docs to be added in db", String.valueOf(driveDocModelList.size()));

        int addedCounter = 0;
        for (DriveDocModel driveDocModel : driveDocModelList) {

//            Log.e("counter", String.valueOf(addedCounter));
//            Log.e("which card",driveDocModel.whichCard);
//            Log.e("folder id",driveDocModel.getFolderId());
//            Log.e("folder name",driveDocModel.getFolderName());
//            Log.e("image file id",driveDocModel.getImagefileId());
//            Log.e("image path",driveDocModel.getImagePath());
//            Log.e("image string",driveDocModel.getImageString());
//            Log.e("json text",driveDocModel.getJsonText());

//            Log.e("text file id",driveDocModel.getTextfileId());

            driveDocModel.setSyncStatus(SyncStatus.synced.toString());
//            Log.e("sync status",driveDocModel.getSyncStatus());
            addedCounter = addedCounter + driveDocRepo.addDriveDocInfo(driveDocModel);
        }

        Log.e("added counter", String.valueOf(addedCounter));
        if (addedCounter == driveDocModelList.size()) {
            Log.e("sync", "done");
            Log.e("added in ", "database");
            Scanner.getInstance().getPreferences().setSyncDriveToDb(true);
            Toast.makeText(context, "Sync done.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("sync not done", "try again");
//            Scanner.getInstance().getPreferences().setSyncDriveToDb(true);
//            Toast.makeText(HomeActivity.this, "Error storing document.", Toast.LENGTH_SHORT).show();
        }
    }

}
