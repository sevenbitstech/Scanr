package com.pancard.android.activity.newflow.activity.inappbilling;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pancard.android.Scanner;
import com.pancard.android.activity.newflow.activity.inappbilling.util.IabHelper;
import com.pancard.android.activity.newflow.activity.inappbilling.util.IabResult;
import com.pancard.android.activity.newflow.activity.inappbilling.util.Inventory;
import com.pancard.android.activity.newflow.activity.inappbilling.util.Purchase;


public class InAppBillingActivity extends AppCompatActivity {

    static final String ITEM_SKU = "scanr_2020";
    private static final String TAG =
            "InAppBilling";
    IabHelper mHelper;
    String responce = "";
    //    private Button clickButton;
//    private Button buyButton;
    TextView buyButton;
    ImageView img_back_button;
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {
                    Toast.makeText(InAppBillingActivity.this, "on consume finished", Toast.LENGTH_SHORT).show();
                    Log.e("Click", result.getMessage());
                    responce = "onConsumeFinished";
                    if (purchase.getOrderId() != null) {
                        responce += "\n getOrderId  : " + purchase.getOrderId();
                    }
                    if (purchase.getDeveloperPayload() != null) {
                        responce += "\n getDeveloperPayload  : " + purchase.getDeveloperPayload();
                    }
                    if (purchase.getItemType() != null) {
                        responce += "\n getItemType  : " + purchase.getItemType();
                    }
                    if (purchase.getOriginalJson() != null) {
                        responce += "\n getOriginalJson  : " + purchase.getOriginalJson();
                    }
                    if (purchase.getSignature() != null) {
                        responce += "\n getSignature  : " + purchase.getSignature();
                    }
                    if (purchase.getSku() != null) {
                        responce += "\n getSku  : " + purchase.getSku();
                    }
                    if (purchase.getToken() != null) {
                        responce += "\n getToken  : " + purchase.getToken();
                    }
                    if (result.getMessage() != null) {
                        responce += "\n getMessage  : " + result.getMessage();
                    }
//                    if (result.getResponse() != null) {
                    responce += "\n getResponse  : " + result.getResponse();
//                    }
                    if (result.isSuccess()) {

                        Toast.makeText(InAppBillingActivity.this, "successfully subscribed", Toast.LENGTH_SHORT).show();
                        Scanner.getInstance().getPreferences().setisProActive(true);
                        displayDialog(responce);

//                        clickButton.setEnabled(true);
//                        String message = result.

                    } else {
                        // handle error
                        //todo: dialog display
                        displayErrorDialog("Error in subscribing to the pro version of the app");
                    }
                }
            };
    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase) {
            Log.e("Click", result.getMessage());
            Log.e("Click", result.getMessage());
            responce = "OnIabPurchaseFinishedListener";
            if (purchase.getOrderId() != null) {
                responce += "\n getOrderId  : " + purchase.getOrderId();
            }
            if (purchase.getDeveloperPayload() != null) {
                responce += "\n getDeveloperPayload  : " + purchase.getDeveloperPayload();
            }
            if (purchase.getItemType() != null) {
                responce += "\n getItemType  : " + purchase.getItemType();
            }
            if (purchase.getOriginalJson() != null) {
                responce += "\n getOriginalJson  : " + purchase.getOriginalJson();
            }
            if (purchase.getSignature() != null) {
                responce += "\n getSignature  : " + purchase.getSignature();
            }
            if (purchase.getSku() != null) {
                responce += "\n getSku  : " + purchase.getSku();
            }
            if (purchase.getToken() != null) {
                responce += "\n getToken  : " + purchase.getToken();
            }
            if (result.getMessage() != null) {
                responce += "\n getMessage  : " + result.getMessage();
            }
//                    if (result.getResponse() != null) {
            responce += "\n getResponse  : " + result.getResponse();
            displayDialog(responce);
//                    }
//            Log.e("getOrderId Finished : ", purchase.getOrderId());
//            Log.e("getDeveloperPayload  : ", purchase.getDeveloperPayload());
//            Log.e("getItemType detail : ", purchase.getItemType());
//            Log.e("getOriginalJson : ", purchase.getOriginalJson());
//            Log.e("getSignature detail : ", purchase.getSignature());
//            Log.e("getSku detail : ", purchase.getSku());
////                        Log.e("Purchase detail : ", purchase.getPurchaseState());
//            Log.e("getToken detail : ", purchase.getToken());
//            Log.e("getMessage detail : ", result.getMessage());
//            Log.e("getResponse detail : ", String.valueOf(result.getResponse()));
//            Log.e("getOrderId detail : ", purchase.getOrderId());


            if (result.isFailure()) {
                // Handle error
                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
                buyButton.setEnabled(false);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_version);
        buyButton = findViewById(R.id.btn_pay);
        img_back_button = findViewById(R.id.img_back_button);

        img_back_button.setOnClickListener(v -> {
            Log.e("pressing", "back");
            onBackPressed();
        });

        buyButton.setOnClickListener(v -> buyClick());

        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoz9iKKIgHUcZ9fuZ36Nkch29wBLLRan73lYht5z7vX8pRLPzaY+xRFBcLmJHCcxi24mEc5P0WhtavbEWPHk37kvDA8bUmLFlhLzttlnC36ATKAEZ3zqi0zJdykXcq9UZFptozXKhOcGrYqXc5QgkErVA6v2nDdFWv4c09QSbEfbmiW0VA2vD8sxwgttSLcpPJwkmjA2CKeomdig4w2e9IElz4JqDSl7xa1kFERQVINYmm0hXwgFiSYgjLXgXTdbP0oDnCauZMoiCrnv0COLrk9RccbOwsRIlVVvKZ/LkVt/0dya7eFK95FQ2qQ6lHT8J7R92IClIpFMW/dGB2s07gwIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Toast.makeText(InAppBillingActivity.this, "helper failure", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "In-app Billing setup failed: " +
                            result);
                } else {
                    Toast.makeText(InAppBillingActivity.this, "helper success", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "In-app Billing is set up OK");
                }
            }
        });


    }

    public void buttonClicked(View view) {
        buyButton.setEnabled(true);
    }

    public void buyClick() {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "mypurchasetoken");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    public void displayDialog(String responce) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(responce)
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, id) -> {

                        }).setNegativeButton("No", null).show();
    }

    public void displayErrorDialog(String message) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Try Again",
                        (dialog, id) -> {
                            buyClick();
                        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

}
