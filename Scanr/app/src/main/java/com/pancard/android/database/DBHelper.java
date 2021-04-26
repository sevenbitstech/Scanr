package com.pancard.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.pancard.android.DriveOperations.DriveDocModel;

import java.sql.SQLException;

public class DBHelper extends OrmLiteSqliteOpenHelper {

    // Fields

    //    public static final String DB_NAME = "drive_doc_manager.db";
    private static final int DB_VERSION = 5;
    private RuntimeExceptionDao<DriveDocModel, Integer> mDriveDocModelDao = null;

    // Public methods

    public DBHelper(Context context, String dbName) {
        super(context, dbName, null, DB_VERSION);
        getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        try {

            // Create Table with given table name with columnName
            TableUtils.createTable(cs, DriveDocModel.class);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldVersion, int newVersion) {
        Log.e("Upgrading database", "Yes");

        Log.e("Old version", String.valueOf(oldVersion));
        Log.e("New version", String.valueOf(newVersion));
        if (oldVersion < 2) {
            try {
                RuntimeExceptionDao<DriveDocModel, Integer> mDriveDocModelDao = getDriveDocModelDao();
                mDriveDocModelDao.executeRaw("ALTER TABLE " + DriveDocModel.TABLE_NAME + " ADD COLUMN " + DriveDocModel.ORIGINAL_IMAGE_PATH + " STRING;");
                mDriveDocModelDao.executeRaw("ALTER TABLE " + DriveDocModel.TABLE_NAME + " ADD COLUMN " + DriveDocModel.PUBLIC_GUID + " STRING;");
                Log.e("DB Upgraded", "Successfully");
            } catch (Exception e) {
                Log.e("DB Upgrade Failure", e.getMessage());
            }
        }

        if (oldVersion < 3) {
            try {
                RuntimeExceptionDao<DriveDocModel, Integer> mDriveDocModelDao = getDriveDocModelDao();
                mDriveDocModelDao.executeRaw("ALTER TABLE " + DriveDocModel.TABLE_NAME + " ADD COLUMN " + DriveDocModel.ORIGINAL_IMAGE_ID + " STRING;");
                Log.e("DB Upgraded", "Successfully");
            } catch (Exception e) {
                Log.e("DB Upgrade Failure", e.getMessage());
            }
        }

        if (oldVersion < 4) {
            try {
                RuntimeExceptionDao<DriveDocModel, Integer> mDriveDocModelDao = getDriveDocModelDao();
                mDriveDocModelDao.executeRaw("ALTER TABLE " + DriveDocModel.TABLE_NAME + " ADD COLUMN " + DriveDocModel.FILE_NAME + " STRING;");
                mDriveDocModelDao.executeRaw("ALTER TABLE " + DriveDocModel.TABLE_NAME + " ADD COLUMN " + DriveDocModel.PDF_FILE_PATH + " STRING;");
                Log.e("DB Upgraded", "Successfully");
            } catch (Exception e) {
                Log.e("DB Upgrade Failure", e.getMessage());
            }
        }

        if (oldVersion < 5) {
            try {
                RuntimeExceptionDao<DriveDocModel, Integer> mDriveDocModelDao = getDriveDocModelDao();
                mDriveDocModelDao.executeRaw("ALTER TABLE " + DriveDocModel.TABLE_NAME + " ADD COLUMN " + DriveDocModel.PDF_FILE_ID + " STRING;");
                Log.e("DB Upgraded", "Successfully");
            } catch (Exception e) {
                Log.e("DB Upgrade Failure", e.getMessage());
            }
        }
    }

    public RuntimeExceptionDao<DriveDocModel, Integer> getDriveDocModelDao() {
        if (mDriveDocModelDao == null) {
            mDriveDocModelDao = getRuntimeExceptionDao(DriveDocModel.class);
        }
        return mDriveDocModelDao;
    }
}
