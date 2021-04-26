package com.pancard.android.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.docscan.android.R;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.Globalarea;

import java.util.ArrayList;
import java.util.List;


public class ImageBitmapMerge {

    private Context context;
    private Activity activity;
    private List<DriveDocModel> driveDocModelList;
    private List<Bitmap> bitmaps;
    private float greatestWidth;

    public ImageBitmapMerge(Context context, Activity activity, List<DriveDocModel> driveDocModelList) {
        this.context = context;
        this.activity = activity;
        this.driveDocModelList = driveDocModelList;
    }

    public Bitmap startBitmapMerge() {
        if (activity != null) {
            LinearLayout templateView = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.template_image_combine, null);

            Bitmap bitmap = null;

            bitmaps = getBitmaps(driveDocModelList);

            if (bitmaps != null) {
                for (Bitmap bitmap1 : bitmaps) {
                    View view = createView(templateView, bitmap1, greatestWidth);
                    if (view != null)
                        bitmap = createBitmap(view);
                    else
                        return null;
                }
                return bitmap;
            } else {
                return null;
            }
        } else
            return null;
    }

    private List<Bitmap> getBitmaps(List<DriveDocModel> driveDocModelList) {
        List<Bitmap> bitmaps = new ArrayList<>();
        greatestWidth = 0;

        try {

            for (DriveDocModel driveDocModel : driveDocModelList) {
                if (driveDocModel.getImagePath() != null) {

                    BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//
//                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(driveDocModel.getImagePath())),null,options);

                    options.inSampleSize = 2;
                    Bitmap bitmap = BitmapFactory.decodeFile(driveDocModel.getImagePath(), options);
                    bitmaps.add(bitmap);

                    if (bitmap != null) {
                        float width = bitmap.getWidth();
                        if (width > greatestWidth) {
                            greatestWidth = width;
                        }
                    }

                } else {
//                    Bitmap bitmap = null;
                    bitmaps.add(null);
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } catch (OutOfMemoryError outOfMemoryError) {
            Toast.makeText(context, "Device on low memory", Toast.LENGTH_SHORT).show();
            outOfMemoryError.printStackTrace();
            return null;
        }
        return bitmaps;
    }

    private float findGreatestWidth(List<Bitmap> bitmaps) {
        float widthBiggest = 0;

        for (Bitmap bitmap : bitmaps) {
            if (bitmap != null) {
                float width = bitmap.getWidth();
                if (width > widthBiggest) {
                    widthBiggest = width;
                }
            }
        }

        return widthBiggest;
    }

    public Bitmap createBitmap(View view) {

        try {
            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);

            Drawable bgDrawable = view.getBackground();
            if (bgDrawable != null) {
                //has background drawable, then draw it on the canvas
                bgDrawable.draw(canvas);
            } else {
                //does not have background drawable, then draw white background on the canvas
                canvas.drawColor(Color.WHITE);
            }

            view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
            view.draw(canvas);

            Log.i("createBitmap", "yes");
            return bitmap;
        } catch (OutOfMemoryError outOfMemoryError) {
            Toast.makeText(context, "Device on low memory", Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    public View createView(LinearLayout view, Bitmap bitmap, float scaledWidth) {
        try {

//            ViewHolder holder = new ViewHolder(view);

            if (bitmap != null) {
                float aspect_ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                float newBitmapHeight = scaledWidth / aspect_ratio;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) scaledWidth, (int) newBitmapHeight, true);

                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, Globalarea.dpToPx(context, 10));
                imageView.setLayoutParams(lp);
                imageView.setImageBitmap(scaledBitmap);
                view.addView(imageView);

            } else {
                ImageView imageView = new ImageView(context);
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ds_logo));
                view.addView(imageView);
            }

            return view;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } catch (OutOfMemoryError outOfMemoryError) {
            Toast.makeText(context, "Device on low memory", Toast.LENGTH_SHORT).show();
            outOfMemoryError.printStackTrace();
            return null;
        }
    }
}
