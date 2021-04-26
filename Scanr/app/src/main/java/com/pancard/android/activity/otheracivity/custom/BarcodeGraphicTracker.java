package com.pancard.android.activity.otheracivity.custom;

import com.google.android.gms.vision.Detector.Detections;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeGraphicTracker extends Tracker<Barcode> {
    private BarcodeGraphicTracker.NewDetectionListener mListener;
    private GraphicOverlay<BarcodeGraphic> mOverlay;
    private BarcodeGraphic mGraphic;

    BarcodeGraphicTracker(GraphicOverlay<BarcodeGraphic> overlay, BarcodeGraphic graphic) {
        this.mOverlay = overlay;
        this.mGraphic = graphic;
    }

    public void setListener(BarcodeGraphicTracker.NewDetectionListener mListener) {
        this.mListener = mListener;
    }

    public void onNewItem(int id, Barcode item) {
        this.mGraphic.setId(id);
        if (this.mListener != null) {
            this.mListener.onNewDetection(item);
        }

    }

    public void onUpdate(Detections<Barcode> detectionResults, Barcode item) {
        this.mOverlay.add(this.mGraphic);
        this.mGraphic.updateItem(item);
    }

    public void onMissing(Detections<Barcode> detectionResults) {
        this.mOverlay.remove(this.mGraphic);
    }

    public void onDone() {
        this.mOverlay.remove(this.mGraphic);
    }

    public interface NewDetectionListener {
        void onNewDetection(Barcode var1);
    }
}
