package com.pancard.android.firebase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.docscan.android.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pancard.android.DatabaseHandler;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.common.SignInActivity;
import com.pancard.android.asyntask.DownloadImage;
import com.pancard.android.model.CardDetail;
import com.pancard.android.model.SecurityStatus;
import com.pancard.android.model.SizeDetail;
import com.pancard.android.model.SqliteDetail;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PreferenceManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by seven-bits-pc11 on 12/5/17.
 */
public class FirebaseManagement {
    //    Context context;
    private DatabaseHandler handler;
    //    int listOfCard = 0;
    private int insertedCard = 0;
    private String TAG = "Firebase retrieve data";
    private StorageReference storageRef;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private PreferenceManagement preferences;

    public FirebaseManagement(Context context) {
//        this.context = context;
        auth = FirebaseAuth.getInstance();

        storageRef = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        handler = new DatabaseHandler(context);
        preferences = Scanner.getInstance().getPreferences();

    }

    public void init(Context context) {
        Log.e("init", "yes");
        if (Globalarea.firebaseUser != null) {

//            if (!Scanner.getInstance().getUtils().getBooleanPreferences("UploadedInSqlite")) {
            callGetDataFromFirebase(context);
//            }
            getSpaceList(context);
        } else {
            UserLogin(context);
        }
    }

    public void callGetDataFromFirebase(Context context) {

        ArrayList<CardDetail> document = new ArrayList<>();
        ArrayList<CardDetail> businesscard = new ArrayList<>();
        ArrayList<CardDetail> pancard = new ArrayList<>();
        ArrayList<CardDetail> passport = new ArrayList<>();
        ArrayList<CardDetail> licence = new ArrayList<>();
        ArrayList<CardDetail> adharcard = new ArrayList<>();
        ArrayList<CardDetail> creditcardcard = new ArrayList<>();
        if (!preferences.isFirstGetList()) {
            preferences.setFirstGetList(true);
            preferences.setDocumentGetList("1");
            preferences.setBusinessCardGetList("1");
            preferences.setPanCardGetList("1");
            preferences.setPassportGetList("1");
            preferences.setLicenceGetList("1");
            preferences.setAadharCardGetList("1");
            preferences.setCreditCardGetList("1");
            getCardDetailList(context, Constants.document, document);
            getCardDetailList(context, Constants.businesscard, businesscard);
            getCardDetailList(context, Constants.pancard, pancard);
            getCardDetailList(context, Constants.passport, passport);
            getCardDetailList(context, Constants.licence, licence);
            getCardDetailList(context, Constants.adharcard, adharcard);
            getCardDetailList(context, Constants.creditCard, creditcardcard);
        } else {
            Constants.initArrayTag();
            int i = Integer.parseInt(preferences.getDocumentGetList());
            if (i > 0) {
                getCardDetailList(context, Constants.document, document);
            }
            int j = Integer.parseInt(preferences.getBusinessCardGetList());
            if (j > 0) {
                getCardDetailList(context, Constants.businesscard, businesscard);
            }
            int k = Integer.parseInt(preferences.getPanCardGetList());
            if (k > 0) {
                getCardDetailList(context, Constants.pancard, pancard);
            }
            int l = Integer.parseInt(preferences.getPassportGetList());
            if (l > 0) {
                getCardDetailList(context, Constants.passport, passport);
            }
            if (preferences.getAadharCardGetList() == null) {
                preferences.setAadharCardGetList("1");
            }
            if (preferences.getCreditCardGetList() == null) {
                preferences.setCreditCardGetList("1");
            }
            int o = Integer.parseInt(preferences.getAadharCardGetList());
            if (o > 0) {
                getCardDetailList(context, Constants.adharcard, adharcard);
            }
            int n = Integer.parseInt(preferences.getCreditCardGetList());
            if (n > 0) {
                getCardDetailList(context, Constants.creditCard, adharcard);
            }
            int m = Integer.parseInt(preferences.getLicenceGetList());
            if (m > 0) {
                getCardDetailList(context, Constants.licence, licence);
            }
        }
    }

    public void UserLogin(Context context) {

        Log.e("user Login", "login");

        if (auth.getCurrentUser() != null) {
            Log.e("already", "logged in");
            setUser(context, auth.getCurrentUser());
        } else {
            Log.e("login ", "guest login");
            guestLogin(context);
        }
    }

    private void guestLogin(final Context context) {
//        final String email = preferences.getEmail();

//        if (TextUtils.isEmpty(email)) {
////            displayAlert("Please Configure your gmail account!!!");
//            return;
//        }

        Intent intent = new Intent(context, SignInActivity.class);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
            ((Activity) context).finishAffinity();
        }

//        auth.createUserWithEmailAndPassword(email.trim(), "12345678")
//                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (!task.isSuccessful()) {
//                            Login(context, email);
//                        } else {
//                            if (task.getResult() != null)
//                                setUser(context, task.getResult().getUser());
//                            else
//                                Toast.makeText(context, "Something went wrong! Please try again!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }

    private void setUser(Context context, @NotNull FirebaseUser user) {
        Globalarea.firebaseUser = user;
        Constants.ARG_UID = user.getUid();
        Log.e("Firebase User Id : ", Constants.ARG_UID);
//                            if (!Scanner.getInstance().getUtils().getBooleanPreferences("UploadedInSqlite")) {
        callGetDataFromFirebase(context);
//                            }
        getSpaceList(context);
    }

//    private void Login(final Context context, String emailID) {
//
//        final String password = "12345678";
//
//        //authenticate user
//        auth.signInWithEmailAndPassword(emailID, password)
//                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//
//                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
//                            Globalarea.firebaseUser = task.getResult().getUser();
//                            Constants.ARG_UID = task.getResult().getUser().getUid();
////                            if (!Scanner.getInstance().getUtils().getBooleanPreferences("UploadedInSqlite")) {
//                            callGetDataFromFirebase(context);
////                            }
//                            getSpaceList(context);
//                        } else {
//                            if (task.getException() != null) {
//                                task.getException().printStackTrace();
//                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//                });
//    }

    public void deleteDocument(Context context) {
        ArrayList<SqliteDetail> deleteData = handler.GetAllTableData(Constants.delete);
        Log.e("add value", String.valueOf(deleteData.size()));
        for (int i = 0; i < deleteData.size(); i++) {

            deletemultiplerow(context, deleteData.get(i).getScan_time(),
                    deleteData.get(i).getWhichcard(), deleteData.get(i).getImage_size(),
                    deleteData.get(i).getId());
        }
    }

    public void syncData(Context context, String status) {

        handler = new DatabaseHandler(context);
        ArrayList<CardDetail> documentDetail = handler.getFalseValue(Constants.document, status);
        ArrayList<CardDetail> businessCardDetail = handler.getFalseValue(Constants.businesscard, status);
        ArrayList<CardDetail> pancardDetail = handler.getFalseValue(Constants.pancard, status);
        ArrayList<CardDetail> licenceDetail = handler.getFalseValue(Constants.licence, status);
        ArrayList<CardDetail> passportDetail = handler.getFalseValue(Constants.passport, status);
        ArrayList<CardDetail> adharcardDetail = handler.getFalseValue(Constants.adharcard, status);
        ArrayList<CardDetail> creditcardDetail = handler.getFalseValue(Constants.creditCard, status);

        SendDataToFireBase(context, documentDetail, Constants.document, status);
        SendDataToFireBase(context, businessCardDetail, Constants.businesscard, status);
        SendDataToFireBase(context, pancardDetail, Constants.pancard, status);
        SendDataToFireBase(context, licenceDetail, Constants.licence, status);
        SendDataToFireBase(context, passportDetail, Constants.passport, status);
        SendDataToFireBase(context, adharcardDetail, Constants.adharcard, status);
        SendDataToFireBase(context, creditcardDetail, Constants.creditCard, status);

    }

    private void SendDataToFireBase(Context context, ArrayList<CardDetail> cardDetails, String tag, String status) {
        if (cardDetails.size() > 0) {
            for (int i = 0; i < cardDetails.size(); i++) {

                if (status.equals("false")) {
                    if (spaceManagement(cardDetails.get(i).getImage_size())) {
                        sendcardInfo(context, cardDetails.get(i), tag, status);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.UploadError), Toast.LENGTH_LONG).show();
//                        new android.app.AlertDialog.Builder(context)
//                                .setMessage(context.getResources().getString(R.string.UploadError))
//                                .setCancelable(true)
//                                .show();
                    }
                } else if (status.equals("update")) {
                    sendcardInfo(context, cardDetails.get(i), tag, status);
                }

            }

        }
    }

    public void sendcardInfoDirect(Context context, CardDetail cardDetails, String tag, String status) {

        if (status.equals("false")) {
            if (spaceManagement(cardDetails.getImage_size())) {
                sendcardInfo(context, cardDetails, tag, status);
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.UploadError), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void sendcardInfo(final Context context, final CardDetail cardDetail, final String tag1, final String status) {

        try {
            insertedCard += 1;
            final String finalTag = "-" + tag1;
            Log.e(TAG, "sendMessageToFirebaseUser: success");
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] data = baos.toByteArray();
//            final long size = data.length / 1024L;

            InputStream stream = new FileInputStream(new File(cardDetail.getImage_url()));
//
//            uploadTask = mountainsRef.putStream(stream);

//            if (spaceManagement(cardDetail.getImage_size())) {

            final StorageReference childRef = storageRef.child(Globalarea.firebaseUser.getUid())
                    .child(Globalarea.firebaseUser.getUid() + finalTag)
                    .child(cardDetail.getScan_time());

            UploadTask uploadTask = childRef.putStream(stream);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    childRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {

//                                    Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();

                                    String generatedFilePath = task.getResult().toString();
                                    cardDetail.setImage_url(generatedFilePath);
//                            cardDetail.setImage_size(size);

                                    if (tag1.equals(Constants.adharcard)) {

                                        InputStream stream;
                                        try {
                                            stream = new FileInputStream(new File(cardDetail.getIssue_date()));

                                            final StorageReference childRef = storageRef.child(Globalarea.firebaseUser.getUid())
                                                    .child(Globalarea.firebaseUser.getUid() + finalTag)
                                                    .child(cardDetail.getScan_time() + "1");

                                            UploadTask uploadTask = childRef.putStream(stream);

                                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                    childRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            if (task.isSuccessful() && task.getResult() != null) {
//                                                            Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();

                                                                String generatedFilePath = task.getResult().toString();
                                                                cardDetail.setIssue_date(generatedFilePath);

                                                                saveToFirebase(context, cardDetail, finalTag, status, tag1);
                                                            }
                                                        }
                                                    });

                                                }
                                            });
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        saveToFirebase(context, cardDetail, finalTag, status, tag1);
                                    }
                                }
                            } else {

                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //   taskListener.onTaskFinished("Notset");
                }
            });


//            } else {
//
//                    new android.app.AlertDialog.Builder(context)
//                            .setMessage(context.getResources().getString(R.string.UploadError))
//                            .setCancelable(true)
//                            .show();
//
////                    taskListener.onTaskError(Constants.UploadError);
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveToFirebase(final Context context, final CardDetail cardDetail, final String finalTag,
                                final String status, final String tag1) {
        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + finalTag)) {
                            Log.e(TAG, "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + finalTag + " exists");
                            databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag).child(cardDetail.getScan_time())
                                    .setValue(cardDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        if (status.equals("false")) {
                                            Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.availableSpace - cardDetail.getImage_size());
                                            Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.usedSpace + cardDetail.getImage_size());
                                            sendSizeDetail(context);
                                        }
                                        cardDetail.setImage_size((int) cardDetail.getImage_size());
                                        handler.updateSqliteRow(cardDetail, tag1, "true");
//                                                            if (listOfCard == insertedCard) {
//                                                                syncData("false");
//                                                                syncData("update");
//                                                            }
                                    }
                                }
                            });
                        } else {
                            Log.e(TAG, "sendMessageToFirebaseUser: success");
                            databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                                    .child(cardDetail.getScan_time())
                                    .setValue(cardDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (status.equals("false")) {
                                            Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.availableSpace - cardDetail.getImage_size());
                                            Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.usedSpace + cardDetail.getImage_size());
                                            sendSizeDetail(context);
                                        }

                                        cardDetail.setImage_size((int) cardDetail.getImage_size());

                                        handler.updateSqliteRow(cardDetail, tag1, "true");
//                                                            if (listOfCard == insertedCard) {
//                                                                syncData("false");
//                                                                syncData("update");
//                                                            }
//                                                                taskListener.onTaskFinished("set");
                                    }

                                }


                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }


    public void getSpaceList(final Context context) {

        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-sizeDetail")) {
                            Log.e("sizeDetail", "getMessageFromFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-sizeDetail" + " exists");
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child(Globalarea.firebaseUser.getUid())
                                    .child(Globalarea.firebaseUser.getUid() + "-sizeDetail")
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                                            Globalarea.sizeDetail = dataSnapshot.getValue(SizeDetail.class);

                                        }

                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            //  taskListener.onTaskFinished("Notset");
                                        }

                                    });
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (preferences.isOnlinePin()) {

                                        if (!preferences.isPinUpdatedInFirebase()) {
                                            if (preferences.isPinUpdate()) {
                                                sendSecurityStatus(true, preferences.getPin());
                                            } else {
                                                sendSecurityStatus(false, "0000");
                                            }
                                        }
                                    } else {
                                        getSecurityStatus();
                                    }
                                    deleteDocument(context);
                                    syncData(context, "false");
                                    syncData(context, "update");

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            sendSizeDetail(context);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
    //Space Management

    public boolean spaceManagement(long imageSize) {

        return Globalarea.sizeDetail.availableSpace > imageSize;
    }


    public void sendSizeDetail(final Context context) {
        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-sizeDetail")) {
                            Log.e(TAG, "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-sizeDetail" + " exists");
                            databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + "-sizeDetail")
                                    .child("info")
                                    .setValue(Globalarea.sizeDetail);
                        } else {
                            SizeDetail sizeDetail = new SizeDetail(Globalarea.sizeDetail.getTotalSpace(), Globalarea.sizeDetail.getAvailableSpace(), Globalarea.sizeDetail.getUsedSpace());
                            databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + "-sizeDetail")
                                    .child("info")
                                    .setValue(sizeDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                    } else {
                                        getSpaceList(context);
                                    }
                                }
                            });
                        }
                        // send push notification to the receiver
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                mOnSendMessageListener.onSendMessageFailure("Unable to send message: " + databaseError.getMessage());
                        System.out.println("error : " + databaseError.getMessage());
                    }
                });

    }


    public void getCardDetailList(final Context context, final String tag1, final ArrayList<CardDetail> cardDetails) {

        Globalarea.cardDetail.clear();

        final String finalTag = "-" + tag1;
        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + finalTag)) {
                            Log.e(TAG, "getMessageFromFirebaseUser: " + Globalarea.firebaseUser.getUid() +
                                    finalTag + " exists");
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child(Globalarea.firebaseUser.getUid())
                                    .child(Globalarea.firebaseUser.getUid() + finalTag)
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                                            CardDetail businessCard = dataSnapshot.getValue(CardDetail.class);
                                            cardDetails.add(businessCard);
                                        }

                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
//                                            System.out.println("network error :===1 "+task.getException());

                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                                            System.out.println("network error :===2 "+task.getException());

                                        }

                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
//                                            System.out.println("network error :===3 "+task.getException());

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            System.out.println("network error :===4 " + databaseError.getMessage());

                                        }
                                    });
                        } else {
                            Log.e(TAG, "getMessageFromFirebaseUser 3: no such room available");
                        }

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.e("List of cards " + tag1, String.valueOf(cardDetails.size()));
                                preferences.setStringPreference(tag1 + "Get List", String.valueOf(cardDetails.size()));
                                if (cardDetails.size() > 0) {
                                    new DownloadImage(context, tag1, cardDetails).execute();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                System.out.println("network error :===5 " + databaseError.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void deletemultiplerow(final Context context, final String userid, String tag, final long image_size, final int id) {
        handler.deletRowData(Constants.delete, userid);
        final String finalTag1 = tag;
        tag = "-" + tag;
        final String finalTag = tag;

        databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                .child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                    storageRef.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                            .child(userid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                if (finalTag1.equals(Constants.adharcard)) {
                                    storageRef.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                                            .child(userid + "1").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.getUsedSpace() - image_size);
                                            Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.getAvailableSpace() + image_size);

                                            sendSizeDetail(context);
                                        }
                                    });
                                }

                            }
                        }
                    });
                }
            }
        });

    }

    public void sendSecurityStatus(final boolean status, final String pin) {

        final SecurityStatus securityStatus = new SecurityStatus(status, pin);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-SecurityStatus")) {
                            Log.e("Send", "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-SecurityStatus" + " exists");
                            databaseReference.child(Globalarea.firebaseUser.getUid())
                                    .child(Globalarea.firebaseUser.getUid() + "-SecurityStatus").child("info")
                                    .setValue(securityStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                    } else {
                                        if (status) {
                                            preferences.setPin(pin);
                                        } else {
                                            preferences.setPin("");
                                        }
                                        preferences.setPinUpdatedInFirebase(true);
                                    }
                                }
                            });
                        } else {
                            databaseReference.child(Globalarea.firebaseUser.getUid())
                                    .child(Globalarea.firebaseUser.getUid() + "-SecurityStatus").child("info")
                                    .setValue(securityStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {

                                    } else {
                                        if (status) {
                                            preferences.setPin(pin);
                                        } else {
                                            preferences.setPin("");
                                        }
                                        preferences.setPinUpdatedInFirebase(true);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void getSecurityStatus() {

        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-SecurityStatus")) {
                            Log.e("SecurityStatus", "getMessageFromFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-SecurityStatus" + " exists");
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child(Globalarea.firebaseUser.getUid())
                                    .child(Globalarea.firebaseUser.getUid() + "-SecurityStatus").addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                                    Globalarea.securityStatus = dataSnapshot.getValue(SecurityStatus.class);

                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    preferences.setPinUpdatedInFirebase(true);
                                    preferences.setOnlinePin(true);

                                    if (Globalarea.securityStatus.isStatus()) {
                                        preferences.setPinUpdate(true);
                                        preferences.setPin(Globalarea.securityStatus.getPin());
                                    } else {
                                        preferences.setPinUpdate(false);
                                        preferences.setPin("");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            preferences.setOnlinePin(true);
                            if (checkLock()) {
                                preferences.setPinUpdate(true);
                                sendSecurityStatus(true, preferences.getPin());
                            } else {
                                preferences.setPinUpdate(false);
                                preferences.setPin("");
                                sendSecurityStatus(false, "0000");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private boolean checkLock() {
        if (preferences.getPin() != null) {
            return preferences.getPin().trim().length() == 4;
        }
        return false;
    }

    //
    public void editCardvalue(final CardDetail businessCard, String tag) {

        tag = "-" + tag;
        final String finalTag = tag;
        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + finalTag)) {
                    Log.e(TAG, "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + finalTag + " exists");
                    databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                            .child(businessCard.getScan_time())
                            .setValue(businessCard);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("network error :===8 " + databaseError.getMessage());

            }
        });
    }

    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) Scanner.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return false;
        }
        return false;
    }
}
