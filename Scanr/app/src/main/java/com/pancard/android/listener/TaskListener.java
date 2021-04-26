package com.pancard.android.listener;


import com.pancard.android.model.CardDetail;

/**
 * Created by seven-bits-pc11 on 24/10/15.
 */
public interface TaskListener {

    void onTaskFinished(String Token);

    void onTaskFinished(CardDetail Token);

    void onTaskError(String Token);

    void onTaskError(String Token, String errormessage);

    void onTaskFinished(String Token, String taskResponse);

}
