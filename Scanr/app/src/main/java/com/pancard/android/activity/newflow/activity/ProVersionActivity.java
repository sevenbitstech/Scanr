package com.pancard.android.activity.newflow.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.vending.billing.IInAppBillingService;
import com.docscan.android.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pancard.android.Scanner;
import com.pancard.android.utility.ConnectionDetector;

import java.util.ArrayList;
import java.util.List;

public class ProVersionActivity extends AppCompatActivity {
    BillingClient mBillingClient;
    TextView btn_pay;
    ImageView img_enter_pro;
    String responce = "";
    IInAppBillingService mService;
    ImageView imgBack;
    TextView tvLimitedVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_version);

        btn_pay = findViewById(R.id.btn_pay);
        img_enter_pro = findViewById(R.id.img_enter_pro);
        imgBack = findViewById(R.id.img_back_button);
        tvLimitedVersion = findViewById(R.id.tv_limited_version);

        imgBack.setOnClickListener(view -> onBackPressed());
        tvLimitedVersion.setOnClickListener(view -> onBackPressed());
        btn_pay.setOnClickListener(v -> loadAllSKUs());

        setupBillingClient();
    }

    private void setupBillingClient() {

        ServiceConnection mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);

            }
        };


        mBillingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (purchases != null && purchases.size() > 0) {
//                        Toast.makeText(getApplicationContext(),"Length : "+purchases.size(),Toast.LENGTH_LONG).show();
                        for (Purchase purchase : purchases) {
                            //When every a new purchase is made
                            acknowledgePurchase(purchase);
                        }
                    }

                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
//                    Log.i(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
                    Toast.makeText(getApplicationContext(), "Payment has been cancelled", Toast.LENGTH_LONG).show();
                    notSubscibe();
                } else {
//                    Log.w(TAG, "onPurchasesUpdated() got unknown resultCode: " + billingResult.getResponseCode());
                    Toast.makeText(getApplicationContext(), "Payment went unsuccessful. Please try again." + billingResult.getResponseCode(), Toast.LENGTH_LONG).show();
//                    onBackPressed();
                }
            }
        }).build();

        mBillingClient.startConnection(new BillingClientStateListener() {
//            @Override
//            public void onBillingSetupFinished(@BillingClient.BillingResponseCode int billingResponseCode) {
//                if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
//                    // The billing client is ready. You can query purchases here.
//                }
//            }

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
//                    loadAllSKUs();
//                    Toast.makeText(getApplicationContext(),"here onServiceConnected ",Toast.LENGTH_LONG).show();
                    try {
                        Purchase.PurchasesResult ownedItems = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS);
//                                    .getPurchases(3,getPackageName(), ITEM_TYPE_SUBS, Scanner.getInstance().getPreferences().getPurchaseToken());
                        if (ownedItems.getPurchasesList() != null && ownedItems.getPurchasesList().size() > 0) {
                            Purchase purchase = ownedItems.getPurchasesList().get(0);

//                                Toast.makeText(getApplicationContext()," 2 here onServiceConnected "+purchase.getOriginalJson(),Toast.LENGTH_LONG).show();


                            long days = purchase.getPurchaseTime() / (60 * 60 * 24 * 1000);
                            long currentDate = (System.currentTimeMillis() / (60 * 60 * 24 * 1000));

                            if ((currentDate - days) > 365) {
//                                    Toast.makeText(getApplicationContext(),"days : "+(currentDate -days),Toast.LENGTH_LONG).show();
                                if (!Scanner.getInstance().getPreferences().isExpiredDiloag()) {
                                    Scanner.getInstance().getPreferences().setisExpiredDiloag(true);
                                    displayErrorDialog(getResources().getString(R.string.expired_sub));
                                }
                                notSubscibe();
                            } else {

                                if (purchase.isAutoRenewing()) {
                                    if (Scanner.getInstance().getPreferences().getPurchaseToken() == null) {
                                        acknowledgePurchase(purchase);
                                        alreadySubscribe();
                                    } else {
//                                    Toast.makeText(NewHomeActivity.this, "the purchase token is not here", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Scanner.getInstance().getPreferences().setisProActive(false);
                                    Scanner.getInstance().getPreferences().setPurchaseToken(null);
                                    notSubscibe();
                                    btn_pay.setOnClickListener(view -> redirectToPlayStore("scanr_2020"));
                                }
                            }

//                                Toast.makeText(getApplicationContext()," 2 here onServiceConnected "+days,Toast.LENGTH_LONG).show();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("the reuslt is", "not okay");
//                        Toast.makeText(getApplicationContext()," 1 here onServiceConnected ",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                setupBillingClient();
            }
        });
    }

    private void redirectToPlayStore(String productId) {
        String link = "https://play.google.com/store/account/subscriptions";

//        String productId = ;

        String link2 = "https://play.google.com/store/account/subscriptions?" +
                "sku=" + productId +
                "&package=" + getPackageName();

        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(link2));
        startActivity(viewIntent);
    }

    private void acknowledgePurchase(Purchase purchase) {
        Scanner.getInstance().getPreferences().setisProActive(true);
        Scanner.getInstance().getPreferences().setPurchaseToken(purchase.getPurchaseToken());
        alreadySubscribe();
        Toast.makeText(getApplicationContext(), getString(R.string.str_thanks_for_subscription), Toast.LENGTH_LONG).show();
        onBackPressed();
//        Toast.makeText(this,"Result : "+String.valueOf(purchase.getPurchaseState()),Toast.LENGTH_LONG).show();
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            mBillingClient.acknowledgePurchase(params, billingResult -> {
//                    Toast.makeText(getApplicationContext(),"onAcknowledgePurchaseResponse : "+purchase.getPurchaseToken(),Toast.LENGTH_LONG).show();
                Scanner.getInstance().getPreferences().setPurchaseToken(purchase.getPurchaseToken());
            });
        }

    }

    private void loadAllSKUs() {

//        Toast.makeText(this, "loading skus", Toast.LENGTH_SHORT).show();
        ConnectionDetector connectionDetector = new ConnectionDetector(this);

        if (connectionDetector.isConnectingToInternet1()) {
            final List<String> skuList = new ArrayList<>();
            skuList.add("scanr_2020"); // SKU Id
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(skuList)
                    .setType(BillingClient.SkuType.SUBS)
                    .build();
            mBillingClient.querySkuDetailsAsync(params,
                    (billingResult, skuDetailsList) -> {
                        Log.e("responce code : ", billingResult.getDebugMessage());
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                            if (skuDetailsList.size() == 1) {
                                SkuDetails skuDetails = skuDetailsList.get(0);
                                BillingFlowParams.Builder builder = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetails);
                                BillingResult responseCode = mBillingClient.launchBillingFlow(ProVersionActivity.this, builder.build());
                                toastDisplay(responseCode);
                            }
                        }
                    });
        } else {
            Toast.makeText(this, getString(R.string.internetConnectionFail), Toast.LENGTH_SHORT).show();
        }
    }

    private void toastDisplay(BillingResult responseCode) {
//        Toast.makeText(this,"BillingResult : "+responseCode.getDebugMessage(),Toast.LENGTH_LONG).show();

    }

    public void alreadySubscribe() {
        btn_pay.setClickable(false);
        btn_pay.setBackgroundColor(getResources().getColor(R.color.selectedgray));
        img_enter_pro.setVisibility(View.VISIBLE);
        img_enter_pro.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_selected));
//        img_enter_pro.setOnClickListener(view -> Toast.makeText(ProVersionActivity.this, "You have already subscribed to the pro version", Toast.LENGTH_SHORT).show());
    }

    private void notSubscibe() {

        btn_pay.setClickable(true);
        btn_pay.setBackground(getResources().getDrawable(R.drawable.bordelinearborder));
        //todo: change background
        img_enter_pro.setVisibility(View.GONE);
//        img_enter_pro.setImageDrawable(getResources().getDrawable(R.drawable.circle_boarder));
    }


    public void displayErrorDialog(String message) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Renew",
                        (dialog, id) -> {
                            loadAllSKUs();
                        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }
}