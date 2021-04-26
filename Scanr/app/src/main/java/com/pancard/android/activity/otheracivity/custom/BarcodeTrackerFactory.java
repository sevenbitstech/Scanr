package com.pancard.android.activity.otheracivity.custom;


import com.google.android.gms.vision.MultiProcessor.Factory;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeTrackerFactory implements Factory<Barcode> {
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private BarcodeGraphicTracker.NewDetectionListener mDetectionListener;
    private int mTrackerColor;

    public BarcodeTrackerFactory(GraphicOverlay<BarcodeGraphic> barcodeGraphicOverlay, BarcodeGraphicTracker.NewDetectionListener listener, int trackerColor) {
        this.mGraphicOverlay = barcodeGraphicOverlay;
        this.mDetectionListener = listener;
        this.mTrackerColor = trackerColor;
    }

    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(this.mGraphicOverlay, this.mTrackerColor);
        BarcodeGraphicTracker tracker = new BarcodeGraphicTracker(this.mGraphicOverlay, graphic);
        if (this.mDetectionListener != null) {
            tracker.setListener(this.mDetectionListener);
        }

        return tracker;
    }
}
