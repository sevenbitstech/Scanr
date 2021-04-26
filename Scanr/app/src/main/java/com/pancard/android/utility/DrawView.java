package com.pancard.android.utility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.docscan.android.R;

public class DrawView extends View {
    boolean color;
    private Paint paint;
    private Path path;

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();

        paint.setStrokeWidth(10);

        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (color) {
            paint.setColor(getResources().getColor(R.color.red_light));
        } else {
            paint.setColor(getResources().getColor(R.color.salmon));
        }

//        paint.setAlpha(20);

        if (path != null) {
            canvas.drawPath(path, paint);
        }

    }

    public void setPath(Path path, boolean capture) {
        this.path = path;
        this.color = capture;
        setWillNotDraw(false);
        invalidate();
    }

    public void setColor(boolean color) {
        this.color = color;
        setWillNotDraw(false);
        invalidate();
    }
}
