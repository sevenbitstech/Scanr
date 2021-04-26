package com.pancard.android.activity.otheracivity.custom;


import android.content.Context;
import android.media.SoundPool;

import com.docscan.android.R;

import java.util.HashMap;

public class SoundPoolPlayer {
    private SoundPool mShortPlayer = null;
    private HashMap mSounds = new HashMap();

    public SoundPoolPlayer(Context pContext) {
        this.mShortPlayer = new SoundPool(4, 3, 0);
        this.mSounds.put(R.raw.bleep, this.mShortPlayer.load(pContext, R.raw.bleep, 1));
    }

    public void playShortResource(int piResource) {
        int iSoundId = (Integer) this.mSounds.get(piResource);
        this.mShortPlayer.play(iSoundId, 0.99F, 0.99F, 0, 0, 1.0F);
    }

    public void release() {
        this.mShortPlayer.release();
    }
}
