package com.pancard.android.database;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.pancard.android.DriveOperations.DriveDocModel;

import java.sql.SQLException;
import java.util.List;

public class DriveDocRepo {

    public final String DBName = "scanR";
    private RuntimeExceptionDao<DriveDocModel, Integer> dao;

    public DriveDocRepo(Context context, String dbName) {
        DBHelper dbHelper = new DBHelper(context, dbName);

        dao = dbHelper.getDriveDocModelDao();
    }

    public DriveDocRepo(Context context) {
        DBHelper dbHelper = new DBHelper(context, DBName);

        dao = dbHelper.getDriveDocModelDao();
    }

    public int addDriveDocInfo(DriveDocModel driveDocModel) {
//        if(driveDocModel.getPdfFilePath()!=null)
//            Log.e("pdf file path",driveDocModel.getPdfFilePath());
        try {
            dao.createOrUpdate(driveDocModel);
//                return driveDocModel.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    public List<DriveDocModel> getAllDriveDocs() {
        try {
            return dao.queryBuilder().where()
                    .ne(DriveDocModel.SYNC_STATUS, SyncStatus.deleted.toString())
                    .and().ne(DriveDocModel.SYNC_STATUS, SyncStatus.deletedDrive.toString())
                    .and().ne(DriveDocModel.SYNC_STATUS, SyncStatus.deletedDriveFailed.toString())
                    .and().ne(DriveDocModel.SYNC_STATUS, SyncStatus.deletedMetaData.toString()).query();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<DriveDocModel> getDocsFromCard(String whichCard) {
        try {
            QueryBuilder<DriveDocModel, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(DriveDocModel.WHICH_CARD, whichCard)
//                                .and().eq(DriveDocModel.SYNC_STATUS,SyncStatus.synced.toString());
                    .and().ne(DriveDocModel.SYNC_STATUS, SyncStatus.deleted.toString())
                    .and().ne(DriveDocModel.SYNC_STATUS, SyncStatus.deletedDrive.toString())
                    .and().ne(DriveDocModel.SYNC_STATUS, SyncStatus.deletedDriveFailed.toString())
                    .and().ne(DriveDocModel.SYNC_STATUS, SyncStatus.deletedMetaData.toString());
            return queryBuilder.query();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<DriveDocModel> getDocsBySyncStatus(String syncStatus) {
        try {
            QueryBuilder<DriveDocModel, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(DriveDocModel.SYNC_STATUS, syncStatus);
            return queryBuilder.query();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public DriveDocModel getDriveDocModel(int id) {
        try {
            return dao.queryForId(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    public DriveDocModel getDriveDocModelByFolderId(String folderId) {
        try {
            QueryBuilder<DriveDocModel, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(DriveDocModel.FOLDER_ID, folderId);
            return queryBuilder.queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public DriveDocModel getDriveDocModelByPublicGuId(String publicGuid) {
        try {
            QueryBuilder<DriveDocModel, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(DriveDocModel.PUBLIC_GUID, publicGuid);
            return queryBuilder.queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (StackOverflowError stackOverflowError) {
            stackOverflowError.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(stackOverflowError);
            return null;
        }
    }

    public DriveDocModel getDriveDocModelByFolderName(String folderName) {
        try {
            QueryBuilder<DriveDocModel, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(DriveDocModel.FOLDER_NAME, folderName);
            return queryBuilder.queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public DriveDocModel getDriveDocModelByStatus(String syncStatus) {
        try {
            QueryBuilder<DriveDocModel, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(DriveDocModel.SYNC_STATUS, syncStatus);
            return queryBuilder.queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (StackOverflowError stackOverflowError) {
            stackOverflowError.printStackTrace();
            return null;
        }
    }

    public int deleteById(DriveDocModel driveDocModel) {
        try {
            return dao.deleteById(driveDocModel.getId());
        } catch (Exception e) {
            return 0;
        }

    }

    public int updateSyncStatus(DriveDocModel driveDocModel, String syncStatus) {
        try {
            UpdateBuilder<DriveDocModel, Integer> updateBuilder = dao.updateBuilder();
// set the criteria like you would a QueryBuilder
            updateBuilder.where().eq(DriveDocModel.ID, driveDocModel.getId());
// update the value of your field(s)
            updateBuilder.updateColumnValue(DriveDocModel.SYNC_STATUS, syncStatus /* value */);
            return updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int updateDriveDoc(DriveDocModel driveDocModel) {
        try {

            return dao.update(driveDocModel);

//            UpdateBuilder<DriveDocModel, Integer> updateBuilder = dao.updateBuilder();
//// set the criteria like you would a QueryBuilder
//            updateBuilder.where().eq(DriveDocModel.ID, driveDocModel.getId());
//// update the value of your field(s)
//            updateBuilder.
//            updateBuilder.updateColumnValue(DriveDocModel.SYNC_STATUS , syncStatus /* value */);
//            return updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public List<DriveDocModel> getDriveDocModelsBySyncStatus(String syncStatus) {
        try {
            QueryBuilder<DriveDocModel, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(DriveDocModel.SYNC_STATUS, syncStatus);
            return queryBuilder.query();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int updateAllSyncedRecordsWhileUpgradingToNewScope() {
        Log.e("updating records", "yes");
        int rowsUpdated;
        try {
            UpdateBuilder<DriveDocModel, Integer> updateBuilder = dao.updateBuilder();
            // set the criteria like you would a QueryBuilder
            updateBuilder.where().eq(DriveDocModel.SYNC_STATUS, SyncStatus.synced);
            // update the value of your field(s)
            updateBuilder.updateColumnValue(DriveDocModel.SYNC_STATUS, SyncStatus.unsynced);
            rowsUpdated = updateBuilder.update();
        } catch (SQLException e) {
            e.printStackTrace();
            rowsUpdated = 0;
        }
        return rowsUpdated;
    }

//    public List<TableOfAddData> getLoginInfoList(String qrUniqueid) {
//
//        QueryBuilder<TableOfAddData, Integer> qb = dao.queryBuilder();
//        try {
//            qb.orderBy("qrUniqueID", true);
//            return dao.query(qb.prepare());
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    public TableOfAddData getQRUniqueIDRecoed(String qrUniqueid) {
//        try {
//            QueryBuilder<TableOfAddData, Integer> queryBuilder = dao.queryBuilder();
//            queryBuilder.where().eq("qrUniqueID", qrUniqueid);
//            Log.e("getting record",qrUniqueid);
//            return queryBuilder.queryForFirst();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    public List<TableOfAddData> getHistory() {
//        try {
//            QueryBuilder<TableOfAddData, Integer> queryBuilder = dao.queryBuilder();
////            queryBuilder.where().eq("isUnSynced", true);
//
//            return queryBuilder.query();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    public int setLoginInfoUnSynced(TableOfAddData loginInfo) {
//        try {
//            UpdateBuilder<TableOfAddData, Integer> updateBuilder = dao.updateBuilder();
//
//            updateBuilder.updateColumnValue("qrScanLink", loginInfo.getQrScanLink());
//            updateBuilder.updateColumnValue("downloadLink", loginInfo.getDownloadLink());
//            updateBuilder.updateColumnValue("metadataJson", loginInfo.getMetadataJson());
////            updateBuilder.updateColumnValue("isUnSynced", loginInfo.isUnSynced());
//            updateBuilder.where().eq("qrUniqueID", loginInfo.getQrUniqueID());
//            return updateBuilder.update();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }

}
