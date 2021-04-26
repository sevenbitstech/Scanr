package com.pancard.android.utility;

import android.content.Context;

import com.pancard.android.Scanner;

import java.io.File;

/**
 * Created by seven-bits-pc11 on 8/6/17.
 */
public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
        // Find the dir to save cached images

//        orchid.getInstance().getFilesDir().exists();
//
//        orchid.getInstance().getDir(".oji_test", Context.MODE_PRIVATE);

        if (Scanner.getInstance().getFilesDir().exists()) {
//            System.out.println("----oji_magazine_pages folder is created in device internal storage");
//            cacheDir = orchid.getInstance().getDir(".oji_magazine_pages", Context.MODE_PRIVATE);
            cacheDir = Scanner.getInstance().getCacheDir();
        }

//        else
//            cacheDir = context.getCacheDir();


//        if (android.os.Environment.getExternalStorageState().equals(
//                android.os.Environment.MEDIA_MOUNTED))
//            cacheDir = new File(
//                    android.os.Environment.getExternalStorageDirectory(),
//                    "JsonParseTutorialCache");
//        else
//            cacheDir = context.getCacheDir();
//

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        } else {
//            System.out.println("----oji_magazine_pages folder is exist in device");
        }

    }

    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode());
        // String filename = URLEncoder.encode(url);
        File f = new File(cacheDir, filename);
        return f;

    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}