package com.pancard.android.activity.otheracivity.custom;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeGraphic extends GraphicOverlay.Graphic {
    private static int mCurrentColorIndex = 0;
    private int mId;
    private Paint mRectPaint;
    private Paint mTextPaint;
    private volatile Barcode mBarcode;
    private int mStrokeWidth = 24;
    private int mCornerWidth = 64;
    private int mCorderPadding;

    public BarcodeGraphic(GraphicOverlay overlay, int trackerColor) {
        super(overlay);
        this.mCorderPadding = this.mStrokeWidth / 2;
        this.mRectPaint = new Paint();
        this.mRectPaint.setColor(trackerColor);
        this.mRectPaint.setStyle(Style.STROKE);
        this.mRectPaint.setStrokeWidth((float) this.mStrokeWidth);
        this.mTextPaint = new Paint();
        this.mTextPaint.setColor(trackerColor);
        this.mTextPaint.setFakeBoldText(true);
        this.mTextPaint.setTextSize(46.0F);
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public Barcode getBarcode() {
        return this.mBarcode;
    }

    void updateItem(Barcode barcode) {
        this.mBarcode = barcode;
        this.postInvalidate();
    }

    public void draw(Canvas canvas) {
        Barcode barcode = this.mBarcode;
        if (barcode != null) {
            RectF rect = new RectF(barcode.getBoundingBox());
            rect.left = this.translateX(rect.left);
            rect.top = this.translateY(rect.top);
            rect.right = this.translateX(rect.right);
            rect.bottom = this.translateY(rect.bottom);
            canvas.drawLine(rect.left - (float) this.mCorderPadding, rect.top, rect.left + (float) this.mCornerWidth, rect.top, this.mRectPaint);
            canvas.drawLine(rect.left, rect.top, rect.left, rect.top + (float) this.mCornerWidth, this.mRectPaint);
            canvas.drawLine(rect.left, rect.bottom, rect.left, rect.bottom - (float) this.mCornerWidth, this.mRectPaint);
            canvas.drawLine(rect.left - (float) this.mCorderPadding, rect.bottom, rect.left + (float) this.mCornerWidth, rect.bottom, this.mRectPaint);
            canvas.drawLine(rect.right + (float) this.mCorderPadding, rect.top, rect.right - (float) this.mCornerWidth, rect.top, this.mRectPaint);
            canvas.drawLine(rect.right, rect.top, rect.right, rect.top + (float) this.mCornerWidth, this.mRectPaint);
            canvas.drawLine(rect.right + (float) this.mCorderPadding, rect.bottom, rect.right - (float) this.mCornerWidth, rect.bottom, this.mRectPaint);
            canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - (float) this.mCornerWidth, this.mRectPaint);
            canvas.drawText(barcode.displayValue, rect.left, rect.bottom + 100.0F, this.mTextPaint);
        }
    }
}
