package com.pancard.android.documentscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class MagnifierView extends View {
    private static int BORDER_WIDTH = 3;
    private static int SHADOW_WIDTH = 5;
    private static int PADDING;

    static {
        PADDING = BORDER_WIDTH + SHADOW_WIDTH;
    }

    private Bitmap bitmap;
    private Paint bitmapPaint;
    private Paint crosshairPaint;
    private Paint borderPaint;
    private Paint shadowPaint;
    private Path crosshairPath;
    private MagnifierView.RelativePoint currentFocusedPoint;

    public MagnifierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initializePaints();
    }

    public void onCornerFocus(float widthPercent, float heightPercent) {
        this.currentFocusedPoint = new MagnifierView.RelativePoint(widthPercent, heightPercent);
        this.invalidate();
    }

    public void onCornerUnfocus() {
        this.currentFocusedPoint = null;
        this.invalidate();
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private void initializePaints() {
        float density = this.getResources().getDisplayMetrics().density;
        this.bitmapPaint = new Paint();
        this.crosshairPaint = new Paint();
        this.crosshairPaint.setColor(-7829368);
        this.crosshairPaint.setStyle(Paint.Style.STROKE);
        this.crosshairPaint.setStrokeWidth(1.0F * density);
        this.borderPaint = new Paint();
        this.borderPaint.setAntiAlias(true);
        this.borderPaint.setColor(-1);
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.borderPaint.setStrokeWidth((float) BORDER_WIDTH * density);
        this.shadowPaint = new Paint();
        this.setLayerType(1, this.shadowPaint);
        this.shadowPaint.setShadowLayer((float) SHADOW_WIDTH * density, 0.0F, 1.0F * density, -16777216);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float density = this.getResources().getDisplayMetrics().density;
        int crossHairSize = (int) (30.0F * density);
        this.crosshairPath = new Path();
        this.crosshairPath.moveTo((float) ((w - crossHairSize) / 2), (float) (h / 2));
        this.crosshairPath.lineTo((float) ((w + crossHairSize) / 2), (float) (h / 2));
        this.crosshairPath.moveTo((float) (w / 2), (float) ((h - crossHairSize) / 2));
        this.crosshairPath.lineTo((float) (w / 2), (float) ((h + crossHairSize) / 2));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.currentFocusedPoint != null) {
            int magnifierWidth = this.getWidth();
            int magnifierHeight = this.getHeight();
            float density = this.getResources().getDisplayMetrics().density;
            canvas.drawCircle((float) (magnifierWidth / 2), (float) (magnifierHeight / 2), (float) magnifierWidth / 2.0F - (float) PADDING * density, this.shadowPaint);
            int x = (int) (this.currentFocusedPoint.getX() * (float) this.bitmap.getWidth());
            int y = (int) (this.currentFocusedPoint.getY() * (float) this.bitmap.getHeight());
            Bitmap croppedBitmap = this.getRoundedBitmap(this.bitmap, x, y, magnifierWidth - (int) ((float) PADDING * density * 2.0F), magnifierHeight - (int) ((float) PADDING * density * 2.0F));
            canvas.drawBitmap(croppedBitmap, (float) PADDING * density, (float) PADDING * density, this.bitmapPaint);
            canvas.drawPath(this.crosshairPath, this.crosshairPaint);
            canvas.drawCircle((float) (magnifierWidth / 2), (float) (magnifierHeight / 2), (float) magnifierWidth / 2.0F - (float) PADDING * density, this.borderPaint);
        }

    }

    private Bitmap getRoundedBitmap(Bitmap bitmap, int x, int y, int width, int height) {
        int size = Math.min(width, height);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        float radius = (float) (size / 2);
        canvas.drawCircle((float) (size / 2), (float) (size / 2), radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (float) (-x + size / 2), (float) (-y + size / 2), paint);
        return output;
    }

    private class RelativePoint {
        float x;
        float y;

        private RelativePoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private float getX() {
            return this.x;
        }

        private float getY() {
            return this.y;
        }
    }
}
