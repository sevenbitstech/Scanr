package com.pancard.android.documentscanner.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Magnifier;

import androidx.core.content.ContextCompat;

import com.docscan.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * An image view subclass which allows for selection of a portion of the image using a
 * convex quadrilateral
 */
@SuppressLint("AppCompatCustomView")
public class QuadrilateralSelectionImageView extends ImageView {

    boolean zooming;
    BitmapShader shader;
    Matrix matrix;
    Paint shaderPaint;
    private Paint mBackgroundPaint;
    private Paint mBorderPaint;
    private Paint mCirclePaint;
    private Path mSelectionPath;
    private Path mBackgroundPath;
    private PointF mUpperLeftPoint;
    private PointF mUpperRightPoint;
    private PointF mLowerLeftPoint;
    private PointF mLowerRightPoint;
    private PointF mLastTouchedPoint;
    private PointF zoomPos = new PointF();
    private QuadrilateralSelectionImageView polygonView;
    private Magnifier magnifier;

    public QuadrilateralSelectionImageView(Context context) {
        super(context);
        init(null, 0);
    }

    public QuadrilateralSelectionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public QuadrilateralSelectionImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        polygonView = this;
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0x80000000);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(7);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setStrokeWidth(2);

        mSelectionPath = new Path();
        mBackgroundPath = new Path();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            magnifier = new Magnifier(polygonView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                magnifier = new Magnifier.Builder(polygonView).setInitialZoom(2.0f)
                        .setElevation(20.0f)
                        .setClippingEnabled(true)
                        .setSize(500, 500)
//                         .setSourceBounds(10,10,10,10)
                        .setCornerRadius(250.0f).build();

            }

        }
//        polygonView.invalidate();
//        BitmapDrawable drawable = (BitmapDrawable) polygonView.getDrawable();
//        Bitmap bitmap = drawable.getBitmap();


        this.setDrawingCacheEnabled(true);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mUpperLeftPoint == null || mUpperRightPoint == null || mLowerRightPoint == null || mLowerLeftPoint == null) {
            setDefaultSelection();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mSelectionPath.reset();
        mSelectionPath.setFillType(Path.FillType.EVEN_ODD);
        mSelectionPath.moveTo(mUpperLeftPoint.x, mUpperLeftPoint.y);
        mSelectionPath.lineTo(mUpperRightPoint.x, mUpperRightPoint.y);
        mSelectionPath.lineTo(mLowerRightPoint.x, mLowerRightPoint.y);
        mSelectionPath.lineTo(mLowerLeftPoint.x, mLowerLeftPoint.y);
        mSelectionPath.close();

        mBackgroundPath.reset();
        mBackgroundPath.setFillType(Path.FillType.EVEN_ODD);
        mBackgroundPath.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        mBackgroundPath.addPath(mSelectionPath);

        canvas.drawPath(mBackgroundPath, mBackgroundPaint);
        canvas.drawPath(mSelectionPath, mBorderPaint);

        canvas.drawCircle(mUpperLeftPoint.x, mUpperLeftPoint.y, 20, mCirclePaint);
        canvas.drawCircle(mUpperRightPoint.x, mUpperRightPoint.y, 20, mCirclePaint);
        canvas.drawCircle(mLowerRightPoint.x, mLowerRightPoint.y, 20, mCirclePaint);
        canvas.drawCircle(mLowerLeftPoint.x, mLowerLeftPoint.y, 20, mCirclePaint);

//        if (zooming) {
//            buildDrawingCache();
//
//            shader = new BitmapShader(Globalarea.document_image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            matrix = new Matrix();
//            shaderPaint = new Paint();
//            shaderPaint.setShader(shader);
//
//            matrix.reset();
//            matrix.postScale(1.5f, 1.5f, mLastTouchedPoint.x, mLastTouchedPoint.y+800);
//            shaderPaint.getShader().setLocalMatrix(matrix);
//
//            canvas.drawCircle(200, 200, 200, shaderPaint);
//        }else {
//            buildDrawingCache();
//        }
    }

    private void drawMag(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && magnifier != null) {
//            magnifier.show(x - viewPosition[0], y - viewPosition[1]);
        }
        zooming = true;
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)MainActivity.magnifierView.getLayoutParams();
//        int xGravity = (double)x < 0.5D ? 5 : 3;
//        int yGravity = 48;
//        if (params.gravity != (xGravity | yGravity)) {
//            params.gravity = xGravity | yGravity;
//            MainActivity.magnifierView.setLayoutParams(params);
//            MainActivity.magnifierView.requestLayout();
//        }

//        MainActivity.magnifierView.onCornerFocus(x, y);
    }

    private void dismissMag() {
//        MainActivity.magnifierView.onCornerUnfocus();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && magnifier != null) {
            magnifier.dismiss();
        }
        zooming = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE: {
                boolean isConvex = false;
                PointF eventPoint = new PointF(event.getX(), event.getY());
                final int[] viewPosition = new int[2];
                polygonView.getLocationOnScreen(viewPosition);

                // Determine if the shape will still be convex when we apply the users next drag
                if (mLastTouchedPoint == mUpperLeftPoint) {
                    isConvex = isConvexQuadrilateral(eventPoint, mUpperRightPoint, mLowerRightPoint, mLowerLeftPoint);
                } else if (mLastTouchedPoint == mUpperRightPoint) {
                    isConvex = isConvexQuadrilateral(mUpperLeftPoint, eventPoint, mLowerRightPoint, mLowerLeftPoint);
                } else if (mLastTouchedPoint == mLowerRightPoint) {
                    isConvex = isConvexQuadrilateral(mUpperLeftPoint, mUpperRightPoint, eventPoint, mLowerLeftPoint);
                } else if (mLastTouchedPoint == mLowerLeftPoint) {
                    isConvex = isConvexQuadrilateral(mUpperLeftPoint, mUpperRightPoint, mLowerRightPoint, eventPoint);
                }

                if (isConvex && mLastTouchedPoint != null) {
                    mLastTouchedPoint.set(event.getX(), event.getY());

                }
//                this.quad.getPoints()[2 * this.currentCorner], this.quad.getPoints()[2 * this.currentCorner + 1]
                zoomPos.x = event.getX();
                zoomPos.y = event.getY();
                zooming = true;
//                drawMag(mLastTouchedPoint.x+50,mLastTouchedPoint.y+50);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && magnifier != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        magnifier.show(event.getRawX() - viewPosition[0],
                                event.getRawY() - viewPosition[1], 400, 400);
                    } else {
                        magnifier.show(event.getRawX() - viewPosition[0],
                                event.getRawY() - viewPosition[1]);

                    }
                }

                break;
            }
            case MotionEvent.ACTION_DOWN: {
                int p = 100;
                if (event.getX() < mUpperLeftPoint.x + p && event.getX() > mUpperLeftPoint.x - p &&
                        event.getY() < mUpperLeftPoint.y + p && event.getY() > mUpperLeftPoint.y - p) {
                    mLastTouchedPoint = mUpperLeftPoint;
                } else if (event.getX() < mUpperRightPoint.x + p && event.getX() > mUpperRightPoint.x - p &&
                        event.getY() < mUpperRightPoint.y + p && event.getY() > mUpperRightPoint.y - p) {
                    mLastTouchedPoint = mUpperRightPoint;
                } else if (event.getX() < mLowerRightPoint.x + p && event.getX() > mLowerRightPoint.x - p &&
                        event.getY() < mLowerRightPoint.y + p && event.getY() > mLowerRightPoint.y - p) {
                    mLastTouchedPoint = mLowerRightPoint;
                } else if (event.getX() < mLowerLeftPoint.x + p && event.getX() > mLowerLeftPoint.x - p &&
                        event.getY() < mLowerLeftPoint.y + p && event.getY() > mLowerLeftPoint.y - p) {
                    mLastTouchedPoint = mLowerLeftPoint;
                } else {
                    mLastTouchedPoint = null;
                }
                break;
            }
            case MotionEvent.ACTION_UP:
//                        zooming = false;
                dismissMag();
                break;
        }
        invalidate();
        return true;
    }

    /**
     * Translate the given point from view coordinates to image coordinates
     *
     * @param point The point to translate
     * @return The translated point
     */
    private PointF viewPointToImagePoint(PointF point) {
        Matrix matrix = new Matrix();
        getImageMatrix().invert(matrix);
        return mapPointToMatrix(point, matrix);
    }

    /**
     * Translate the given point from image coordinates to view coordinates
     *
     * @param imgPoint The point to translate
     * @return The translated point
     */
    private PointF imagePointToViewPoint(PointF imgPoint) {
        return mapPointToMatrix(imgPoint, getImageMatrix());
    }

    /**
     * Helper to map a given PointF to a given Matrix
     * <p>
     * NOTE: http://stackoverflow.com/questions/19958256/custom-imageview-imagematrix-mappoints-and-invert-inaccurate
     *
     * @param point  The point to map
     * @param matrix The matrix
     * @return The mapped point
     */
    private PointF mapPointToMatrix(PointF point, Matrix matrix) {
        float[] points = new float[]{point.x, point.y};
        matrix.mapPoints(points);
        if (points.length > 1) {
            return new PointF(points[0], points[1]);
        } else {
            return null;
        }
    }

    /**
     * Returns a list of points representing the quadrilateral.  The points are converted to represent
     * the location on the image itself, not the view.
     *
     * @return A list of points translated to map to the image
     */
    public List<PointF> getPoints() {
        List<PointF> list = new ArrayList<>();
        list.add(viewPointToImagePoint(mUpperLeftPoint));
        list.add(viewPointToImagePoint(mUpperRightPoint));
        list.add(viewPointToImagePoint(mLowerRightPoint));
        list.add(viewPointToImagePoint(mLowerLeftPoint));
        return list;
    }

    /**
     * Set the points in order to control where the selection will be drawn.  The points should
     * be represented in regards to the image, not the view.  This method will translate from image
     * coordinates to view coordinates.
     * <p>
     * NOTE: Calling this method will invalidate the view
     *
     * @param points A list of points. Passing null will set the selector to the default selection.
     */
    public void setPoints(List<PointF> points) {
        if (points != null) {
            mUpperLeftPoint = imagePointToViewPoint(points.get(0));
            mUpperRightPoint = imagePointToViewPoint(points.get(1));
            mLowerRightPoint = imagePointToViewPoint(points.get(2));
            mLowerLeftPoint = imagePointToViewPoint(points.get(3));
        } else {
            setDefaultSelection();
        }

        invalidate();
    }

    /**
     * Gets the coordinates representing a rectangles corners.
     * <p>
     * The order of the points is
     * 0------->1
     * ^        |
     * |        v
     * 3<-------2
     *
     * @param rect The rectangle
     * @return An array of 8 floats
     */
    private float[] getCornersFromRect(RectF rect) {
        return new float[]{
                rect.left, rect.top,
                rect.right, rect.top,
                rect.right, rect.bottom,
                rect.left, rect.bottom
        };
    }

    /**
     * Sets the points into a default state (A rectangle following the image view frame with
     * padding)
     */
    private void setDefaultSelection() {
        RectF rect = new RectF();

        float padding = 100;
        rect.right = getWidth() - padding;
        rect.bottom = getHeight() - padding;
        rect.top = padding;
        rect.left = padding;

        float pts[] = getCornersFromRect(rect);
        mUpperLeftPoint = new PointF(pts[0], pts[1]);
        mUpperRightPoint = new PointF(pts[2], pts[3]);
        mLowerRightPoint = new PointF(pts[4], pts[5]);
        mLowerLeftPoint = new PointF(pts[6], pts[7]);
    }

    /**
     * Determine if the given points are a convex quadrilateral.  This is used to prevent the
     * selection from being dragged into an invalid state.
     *
     * @param ul The upper left point
     * @param ur The upper right point
     * @param lr The lower right point
     * @param ll The lower left point
     * @return True is the quadrilateral is convex
     */
    private boolean isConvexQuadrilateral(PointF ul, PointF ur, PointF lr, PointF ll) {
        // http://stackoverflow.com/questions/9513107/find-if-4-points-form-a-quadrilateral

        PointF p = ll;
        PointF q = lr;
        PointF r = subtractPoints(ur, ll);
        PointF s = subtractPoints(ul, lr);

        double s_r_crossProduct = crossProduct(r, s);
        double t = crossProduct(subtractPoints(q, p), s) / s_r_crossProduct;
        double u = crossProduct(subtractPoints(q, p), r) / s_r_crossProduct;

        if (t < 0 || t > 1.0 || u < 0 || u > 1.0) {
            return false;
        } else {
            return true;
        }
    }

    private PointF subtractPoints(PointF p1, PointF p2) {
        return new PointF(p1.x - p2.x, p1.y - p2.y);
    }

    private float crossProduct(PointF v1, PointF v2) {
        return v1.x * v2.y - v1.y * v2.x;
    }

}