package com.pancard.android.core;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;

import com.pancard.android.liveedgedetection.view.ScanSurfaceView;
import com.pancard.android.utility.CameraPreview;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by elitebook on 2/9/17.
 */

public class OpenCVMethod {
    public double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    public MatOfPoint approxPolyDP(MatOfPoint curve, double epsilon, boolean closed) {
        MatOfPoint2f tempMat = new MatOfPoint2f();

        Imgproc.approxPolyDP(new MatOfPoint2f(curve.toArray()), tempMat, epsilon, closed);

        return new MatOfPoint(tempMat.toArray());
    }

    public void extractChannel(Mat source, Mat out, int channelNum) {
        List<Mat> sourceChannels = new ArrayList<Mat>();
        List<Mat> outChannel = new ArrayList<Mat>();

        Core.split(source, sourceChannels);

        outChannel.add(new Mat(sourceChannels.get(0).size(), sourceChannels.get(0).type()));

        Core.mixChannels(sourceChannels, outChannel, new MatOfInt(channelNum, 0));

        Core.merge(outChannel, out);
    }

    public List<Point> calculateCamreraPoint(List<Point> points, List<Point> ExtraCutPoint, Mat mRgba, Mat resultMat) {

        try {
            System.out.println("mRgb width and height  : " + mRgba.height());

            int max_height = mRgba.height();
            ExtraCutPoint.clear();
            int value = 0;

            Point point = points.get(0);
            Point cameraPoint = new Point(
                    (point.x * mRgba.width()) / resultMat.width(),
                    (point.y * mRgba.height()) / resultMat.height());

            if (cameraPoint.x - value > 0 && cameraPoint.y - value < max_height) {
                ExtraCutPoint.add(new Point(cameraPoint.x - value, cameraPoint.y - value));

            } else {
                ExtraCutPoint.add(cameraPoint);
            }

            Point point1 = points.get(1);
            Point cameraPoint1 = new Point(
                    (point1.x * mRgba.width()) / resultMat.width(),
                    (point1.y * mRgba.height()) / resultMat.height());

            if (cameraPoint1.x - value > 0 && cameraPoint1.y - value < max_height) {
                ExtraCutPoint.add(new Point(cameraPoint1.x - value, cameraPoint1.y + value));
            } else {
                ExtraCutPoint.add(cameraPoint1);
            }

            Point point2 = points.get(2);
            Point cameraPoint2 = new Point(
                    (point2.x * mRgba.width()) / resultMat.width(),
                    (point2.y * mRgba.height()) / resultMat.height());

            if (cameraPoint2.x - value > 0 && cameraPoint2.y - value < max_height) {
                ExtraCutPoint.add(new Point(cameraPoint2.x + value, cameraPoint2.y + value));

            } else {
                ExtraCutPoint.add(cameraPoint2);
            }

            Point point3 = points.get(3);
            Point cameraPoint3 = new Point(
                    (point3.x * mRgba.width()) / resultMat.width(),
                    (point3.y * mRgba.height()) / resultMat.height());

            if (cameraPoint3.x - value > 0 && cameraPoint3.y - value < max_height) {
                ExtraCutPoint.add(new Point(cameraPoint3.x + value, cameraPoint3.y - value));

            } else {
                ExtraCutPoint.add(cameraPoint3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ExtraCutPoint;

    }

    List<Point> orderRectCorners(List<Point> corners) {
        if (corners.size() == 4) {
            List<Point> ordCorners = orderPointsByRows(corners);

            if (ordCorners.get(0).x > ordCorners.get(1).x) { // swap points
                Point tmp = ordCorners.get(0);
                ordCorners.set(0, ordCorners.get(1));
                ordCorners.set(1, tmp);
            }

            if (ordCorners.get(2).x < ordCorners.get(3).x) { // swap points
                Point tmp = ordCorners.get(2);
                ordCorners.set(2, ordCorners.get(3));
                ordCorners.set(3, tmp);
            }
            return ordCorners;
        }
        return null;
    }

    List<Point> orderPointsByRows(List<Point> points) {
        Collections.sort(points, new Comparator<Point>() {
            public int compare(Point p1, Point p2) {
                if (p1.y < p2.y) return -1;
                if (p1.y > p2.y) return 1;
                return 0;
            }
        });
        return points;
    }

    public Bitmap warpDisplayImage(Mat inputMat, Mat detect_Mat, List<Point> ExtraCutPoint) {

        int resultWidth = detect_Mat.width();
        int resultHeight = detect_Mat.height();

        if (ExtraCutPoint.size() > 1) {
            Mat startM = Converters.vector_Point2f_to_Mat(orderRectCorners(ExtraCutPoint));

            Point ocvPOut4 = new Point(0, 0);
            Point ocvPOut1 = new Point(0, resultHeight);
            Point ocvPOut2 = new Point(resultWidth, resultHeight);
            Point ocvPOut3 = new Point(resultWidth, 0);

            if (inputMat.height() > inputMat.width()) {

                // int temp = resultWidth;
                // resultWidth = resultHeight;
                // resultHeight = temp;

                ocvPOut3 = new Point(0, 0);
                ocvPOut4 = new Point(0, resultHeight);
                ocvPOut1 = new Point(resultWidth, resultHeight);
                ocvPOut2 = new Point(resultWidth, 0);
            }

            Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

            List<Point> dest = new ArrayList<Point>();
            dest.add(ocvPOut3);
            dest.add(ocvPOut2);
            dest.add(ocvPOut1);
            dest.add(ocvPOut4);

//        dest.add(ocvPOut1);
//        dest.add(ocvPOut4);
//        dest.add(ocvPOut3);
//        dest.add(ocvPOut2);
            Mat endM = Converters.vector_Point2f_to_Mat(dest);

            Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

            Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight), Imgproc.INTER_CUBIC);
            Bitmap descBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(outputMat, descBitmap);
            return descBitmap;
        } else {
            Bitmap descBitmap = Bitmap.createBitmap(inputMat.cols(), inputMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(inputMat, descBitmap);
            return descBitmap;
        }
    }

    public boolean setFlashOn(boolean b, CameraPreview cameraPreview) {
        if (cameraPreview.mCamera != null) {

            if (cameraPreview != null) {
                Log.d("cameraPreview", "setFlashOn: " + b);
                try {
                    Camera.Parameters params = cameraPreview.mCamera.getParameters();
                    params.setFlashMode(b ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                    cameraPreview.mCamera.setParameters(params);

//                numManualTorchChange++;

                    return true;
                } catch (RuntimeException e) {
                    Log.w("cameraPreview.mCamera", "Could not set flash mode: " + e);
                }
            }
        }
        return false;
    }

    public boolean isFlashOn(CameraPreview cameraPreview) {
        if (cameraPreview.mCamera == null) {

            if (cameraPreview == null) {
                return false;
            }
            return false;
        }
        Camera.Parameters params = cameraPreview.mCamera.getParameters();
        return params.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH);
    }

    public boolean isFlashOn(ScanSurfaceView scanSurfaceView) {
        if (scanSurfaceView.camera == null) {

            if (scanSurfaceView == null) {
                return false;
            }
            return false;
        }
        Camera.Parameters params = scanSurfaceView.camera.getParameters();
        return params.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH);
    }

    public boolean setFlashOn(boolean b, ScanSurfaceView scanSurfaceView) {
        if (scanSurfaceView.camera != null) {

            if (scanSurfaceView != null) {
                Log.d("cameraPreview", "setFlashOn: " + b);
                try {
                    Camera.Parameters params = scanSurfaceView.camera.getParameters();
                    params.setFlashMode(b ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                    scanSurfaceView.camera.setParameters(params);

//                numManualTorchChange++;

                    return true;
                } catch (RuntimeException e) {
                    Log.w("cameraPreview.mCamera", "Could not set flash mode: " + e);
                }
            }
        }
        return false;
    }

}
