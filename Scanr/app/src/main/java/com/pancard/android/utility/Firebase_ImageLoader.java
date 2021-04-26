package com.pancard.android.utility;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import com.docscan.android.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by seven-bits-pc11 on 8/6/17.
 */
public class Firebase_ImageLoader {

    Context context;
    int stub_id = R.drawable.ds_logo;
    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    ExecutorService executorService;

    // Handler to display images in UI thread
    Handler handler = new Handler();
    private Map<ImageView, String> imageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());

    public Firebase_ImageLoader(Context context) {
        this.context = context;
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(3);
    }

//    public void DisplayImage(String url, SimpleDraweeView imageView, int Size, int DefaultIcone) {
//        imageViews.put(imageView, url);
//        Bitmap bitmap = memoryCache.get(url);
//        if (bitmap != null)
//            imageView.setImageBitmap(bitmap);
//        else {
//            queuePhoto(url, imageView, Size);
//            imageView.setImageResource(DefaultIcone);
//        }
//    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 3;
            final int halfWidth = width / 3;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void DisplayImage(String url, ImageView imageView, int Size, int DefaultIcone) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(url, imageView, Size);
            imageView.setImageResource(DefaultIcone);
        }
    }

    private void queuePhoto(String url, ImageView imageView, int Size) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p, Size));
    }

    public void downloadImages(String url, int Size) {
        try {

            File f = fileCache.getFile(url);
            Bitmap b = decodeFile(f, Size);

            if (b == null) {
                System.out.println("Download Image from firebase  : ");

                /*****************************
                 * Firebase image download
                 *****************************/

                URL imageUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(f);
                Utils.CopyStream(is, os);
                os.close();
            }

        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
        }
    }

    //fixme: the url is null sometimes
    public Bitmap getBitmap(String url, int Size) {

        File f = fileCache.getFile(url);

        Bitmap b = decodeFile(f, Size);
        if (b != null)
            return b;

        // Download Images from the Internet
        try {
            Bitmap bitmap = null;

            /*****************************
             * Firebase image download
             *****************************/
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f, Size);

            return bitmap;

        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }

    // Decodes image and scales it to reduce memory consumption
    public Bitmap decodeFile(File file, int Size) {
        try {
            // Decode image size

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(file);
            BitmapFactory.decodeStream(stream1, null, options);
            stream1.close();

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, Size, Size);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            FileInputStream stream2 = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, options);
            stream2.close();

            return bitmap;

        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

    // Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        int Size;

        PhotosLoader(PhotoToLoad photoToLoad, int Size) {
            this.photoToLoad = photoToLoad;
            this.Size = Size;
        }

        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;
                Bitmap bmp = getBitmap(photoToLoad.url, Size);
                memoryCache.put(photoToLoad.url, bmp);

                if (imageViewReused(photoToLoad))
                    return;
                BitmapDisplayer bd;
                bd = new BitmapDisplayer(bmp, photoToLoad);

                handler.post(bd);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {

            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }
}
