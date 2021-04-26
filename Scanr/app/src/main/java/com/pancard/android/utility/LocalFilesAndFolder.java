package com.pancard.android.utility;

import android.util.Log;

import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Scanner;

import java.io.File;

public class LocalFilesAndFolder {

    public static File getRootDirOfApp() {
//        String root = Environment.getExternalStorageDirectory().toString();

        File cacheDir = Scanner.getInstance().getCacheDir();
        Log.e("Image Pathe : ", cacheDir.getAbsolutePath());
        if (!cacheDir.exists()) {
            boolean b = cacheDir.mkdirs();
            if (!b)
                return null;
        }

        File myDir = new File(cacheDir + "/" + ScanRDriveOperations.ROOT_APP_FOLDER);

        if (!myDir.exists()) {
            boolean b = myDir.mkdirs();
            Log.e("new folder created", String.valueOf(b));
            if (!b)
                return null;
        }

        return myDir;
    }

    public static File getUserDirectory(String userId) {
        File userDir = new File(getRootDirOfApp(), userId);

        if (!userDir.exists()) {
            boolean b = userDir.mkdirs();
            Log.e("new folder created", String.valueOf(b));
            if (!b)
                return null;
        }

        return userDir;
    }

    public static File getSubDir(String subDirFolderName, String userId) {
        File subDir = new File(getRootDirOfApp(), subDirFolderName);
//        File subDir = new File(getUserDirectory(userId), subDirFolderName);

        if (!subDir.exists()) {
            boolean b = subDir.mkdirs();
            Log.e("new folder created", String.valueOf(b));
            if (!b)
                return null;
        }

        return subDir;
    }

}
