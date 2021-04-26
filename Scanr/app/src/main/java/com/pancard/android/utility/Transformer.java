package com.pancard.android.utility;

import com.pancard.android.animation.AccordionTransformer;
import com.pancard.android.animation.BackgroundToForegroundTransformer;
import com.pancard.android.animation.CubeInTransformer;
import com.pancard.android.animation.CubeOutTransformer;
import com.pancard.android.animation.DefaultTransformer;
import com.pancard.android.animation.DepthPageTransformer;
import com.pancard.android.animation.FlipHorizontalTransformer;
import com.pancard.android.animation.FlipVerticalTransformer;
import com.pancard.android.animation.ForegroundToBackgroundTransformer;
import com.pancard.android.animation.RotateDownTransformer;
import com.pancard.android.animation.RotateUpTransformer;
import com.pancard.android.animation.ScaleInOutTransformer;
import com.pancard.android.animation.StackTransformer;
import com.pancard.android.animation.TabletTransformer;
import com.pancard.android.animation.ZoomInTransformer;
import com.pancard.android.animation.ZoomOutSlideTransformer;
import com.pancard.android.animation.ZoomOutTranformer;
import com.pancard.android.model.TransformerItem;

import java.util.ArrayList;

public class Transformer {

    public static final ArrayList<TransformerItem> TRANSFORM_CLASSES;

    static {

        TRANSFORM_CLASSES = new ArrayList<>();

        TRANSFORM_CLASSES.add(new TransformerItem(DefaultTransformer.class)); //0

        TRANSFORM_CLASSES.add(new TransformerItem(AccordionTransformer.class)); //1

        TRANSFORM_CLASSES.add(new TransformerItem(BackgroundToForegroundTransformer.class)); //2

        TRANSFORM_CLASSES.add(new TransformerItem(CubeInTransformer.class)); //3

        TRANSFORM_CLASSES.add(new TransformerItem(CubeOutTransformer.class)); //4

        TRANSFORM_CLASSES.add(new TransformerItem(DepthPageTransformer.class)); //5

        TRANSFORM_CLASSES.add(new TransformerItem(FlipHorizontalTransformer.class)); //6

        TRANSFORM_CLASSES.add(new TransformerItem(FlipVerticalTransformer.class)); //7

        TRANSFORM_CLASSES.add(new TransformerItem(ForegroundToBackgroundTransformer.class)); //8

        TRANSFORM_CLASSES.add(new TransformerItem(RotateDownTransformer.class)); //9

        TRANSFORM_CLASSES.add(new TransformerItem(RotateUpTransformer.class)); //10

        TRANSFORM_CLASSES.add(new TransformerItem(ScaleInOutTransformer.class)); //11

        TRANSFORM_CLASSES.add(new TransformerItem(StackTransformer.class)); //12

        TRANSFORM_CLASSES.add(new TransformerItem(TabletTransformer.class)); //13

        TRANSFORM_CLASSES.add(new TransformerItem(ZoomInTransformer.class)); //14

        TRANSFORM_CLASSES.add(new TransformerItem(ZoomOutSlideTransformer.class)); //15

        TRANSFORM_CLASSES.add(new TransformerItem(ZoomOutTranformer.class)); //16

    }

}
