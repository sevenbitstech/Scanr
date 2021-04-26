package com.pancard.android.utility;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * Created by seven-bits-pc11 on 5/7/17.
 */
public class FileOpration {

    public FileOpration() {

    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVedioFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static boolean isPDFFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        System.out.println("PDF file url : " + mimeType);
        return mimeType != null && mimeType.startsWith("application/pdf");
    }

    public String getPath(final Uri uri, Context context) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.e("path 1", "1");
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                Log.e("path 1", "2");

                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }

            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }

        }
        return null;
    }

    public String getMimeType(Uri uri, Context context) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public String getRealPath(Uri uri, Context context) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        String path = "";
        Cursor cursor = context.getContentResolver().query(uri,
                filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            path = cursor.getString(columnIndex);
            cursor.close();
//            Log.e("picturepath", path);
        }
        return path;
    }


    public Bitmap getImageThumbnail(String filePath, int size) {

        try {
            int THUMBNAIL_SIZE_HEIGHT = size;
            if (THUMBNAIL_SIZE_HEIGHT <= 0) {
                THUMBNAIL_SIZE_HEIGHT = 200;
            }

            FileInputStream fis = new FileInputStream(filePath);
            Bitmap imageBitmap = BitmapFactory.decodeStream(fis);

//            if(imageBitmap.getHeight() > 512){
//                THUMBNAIL_SIZE_HEIGHT = 512;
//            }else {
//                THUMBNAIL_SIZE_HEIGHT = 300;
//            }

            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE_HEIGHT, THUMBNAIL_SIZE_HEIGHT, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return imageBitmap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Bitmap getImageThumbnail20(String filePath) {

        try {
            int THUMBNAIL_SIZE_HEIGHT = 50;

            FileInputStream fis = new FileInputStream(filePath);
            Bitmap imageBitmap = BitmapFactory.decodeStream(fis);

//            if(imageBitmap.getHeight() > 512){
//                THUMBNAIL_SIZE_HEIGHT = 512;
//            }else {
//                THUMBNAIL_SIZE_HEIGHT = 300;
//            }

            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE_HEIGHT, THUMBNAIL_SIZE_HEIGHT, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return imageBitmap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

//    public Bitmap generateImageFromPdf(Uri pdfUri) {
//        int pageNumber = 0;
//        PdfiumCore pdfiumCore = new PdfiumCore(context);
//        try {
//            //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
//            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(pdfUri, "r");
//            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
//            pdfiumCore.openPage(pdfDocument, pageNumber);
//            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
//            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
//            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
//            pdfiumCore.closeDocument(pdfDocument); // important!
//            return bmp;
//        } catch (Exception e) {
//            //todo with exception
//            e.printStackTrace();
//            return null;
//        }
//    }


    public Intent getIntentForFileExplorer(String filename) {
        File file = new File(filename);
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW);
            File file1 = new File(file.getAbsolutePath());
            String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file1).toString());
            String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            myIntent.setDataAndType(Uri.fromFile(file1), mimetype);
            return myIntent;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public File CaptureImage(InputStream inputStream, String imagename) {
        /* MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), card, "Detected Image", ""); */
        try {

            File imageFile;
            File dir;
            dir = new File(Environment.getExternalStorageDirectory(), "Document Scanner");

            boolean success = true;
            if (!dir.exists()) {
                success = dir.mkdirs();
            }
            if (success) {
                imageFile = new File(dir.getAbsolutePath()
                        + File.separator
                        + imagename);

                imageFile.createNewFile();
            } else {
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            try {
                // instream is content got from httpentity.getContent()
                while ((len = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fout = new FileOutputStream(imageFile);
            fout.write(baos.toByteArray());
            fout.close();
//            baos.close();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN,
                    System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA,
                    imageFile.getAbsolutePath());


//            if (isImageFile(imagename) || isVedioFile(imagename))
//                MediaScannerConnection.scanFile(Kranti.getInstance(), new String[]{imageFile.getPath()}, new String[]{"image/jpeg"}, null);

            return imageFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}