package com.pancard.android.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ojimagazine-pc12 on 3/10/15.
 */
public class Utils {

//    private Context context;
//    private SharedPreferences preferences;
//    private SharedPreferences.Editor editor;

    public Utils() {
//        if (con != null) {
//            context = con;
//            preferences = PreferenceManager.getDefaultSharedPreferences(context);
//            editor = preferences.edit();
//
//            mSharedPreferences = getApplicationContext().getSharedPreferences(
//                    "MyPref", 0);
//        }
    }

    public static void Toast(Context context, String msg) {
        try {
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_HORIZONTAL);
            toast.show();
        } catch (Exception ex) {
        }
    }

    /***
     * This function is use to display toast msg
     * in alrertDialog box
     * Use when the toast msg is to long and user not
     * readble simple toast show time to small.
     *
     * @param context context
     * @param msg     dialog message for display
     */
    @SuppressWarnings("deprecation")
    public static void DisplayDialogForToastMsg(Context context, String msg) {
        try {
            final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setMessage(msg);
            alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, final int which) {
                    alertDialog.cancel();


                }
            });


            alertDialog.setCanceledOnTouchOutside(false);
            //alertDialog.setIcon(R.drawable.app_icon_blue);
            alertDialog.show();

        } catch (Exception ex) {

        }
    }

    public static void ToastByTime(Context context, String msg, int timeduration) {
        try {


            final Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_HORIZONTAL);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, timeduration);


        } catch (Exception ex) {

        }
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage, int width) {

        int targetWidth = width;
        int targetHeight = width;

        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);

        canvas.drawCircle(width / 2, width / 2, width / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2, ((float) targetHeight - 1) / 2, (Math.min(((float) targetWidth - 10), ((float) targetHeight - 10)) / 2), Path.Direction.CCW);
        canvas.clipPath(path);

        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }

    /**
     * for right side icon in action bar
     *
     * @param scaleBitmapImage
     * @param width
     * @return
     */
    public static Bitmap getActionBarIconRoundedShape(Bitmap scaleBitmapImage, int width) {

        int targetWidth = width;
        int targetHeight = width;

        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);

        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }


    public static String ExmessageFunctionStart(String functionName) {
        return AppendExMessage("", "Start " + functionName);
    }

    public static String ExmessageFunctionEnd(String functionName) {
        return AppendExMessage("", "End " + functionName);
    }

    public static String AppendExMessage(String oMessage, String message) {
        return oMessage + message + "/n";
    }

    //hides keyboard
    public static void hideKeyBoard(Activity activity, View view) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
//        View view = activity.getCurrentFocus();
//        If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

//    public void RemoveAllPrefrences() {
//
//        editor.clear().commit();
//    }
//
//    public void setPrefrences(String key, String value) {
//        editor.putString(key, value);
//        editor.commit();
//    }
//
//    public String getPreferences(String key) {
//        return preferences.getString(key, null);
//    }
//
//    public void setBooleanPrefrences(String key, boolean value) {
//        editor.putBoolean(key, value);
//        editor.commit();
//    }
//
//    public boolean getBooleanPreferences(String key) {
//        return preferences.getBoolean(key, false);
//    }
//
//    public void setIntPrefrences(String key, int value) {
//        editor.putInt(key, value);
//        editor.commit();
//    }
//
//    public Integer getIntPreferences(String key) {
//        return preferences.getInt(key, 0);
//    }
//
//    public void setLongPrefrences(String key, long value) {
//        editor.putLong(key, value);
//        editor.commit();
//    }
//
//    public long getLongPreferences(String key) {
//        return preferences.getLong(key, 0);
//    }

}