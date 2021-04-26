package com.pancard.android.asyntask;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.docscan.android.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pancard.android.DatabaseHandler;
import com.pancard.android.Globalarea;
import com.pancard.android.OnlineOffilneDataManagement;
import com.pancard.android.Scanner;
import com.pancard.android.model.CardDetail;
import com.pancard.android.model.SizeDetail;
import com.pancard.android.model.SqliteDetail;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PreferenceManagement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by seven-bits-pc11 on 3/6/17.
 */
public class SqliteDataSync extends AsyncTask<String, String, Boolean> {
    OnlineOffilneDataManagement handler;
    String message = "Oops Something went wrong!!";
    private Context context;
    private DatabaseReference databaseReference;
    private StorageReference storageRef;

    public SqliteDataSync(Context context) {
        this.context = context;
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Log.e("sync data : ", "1");
        try {
            syncData();
            return true;
        } catch (Exception e) {
            message = "Oops Something went wrong!!";
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

//        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public void syncData() {
        Log.e("sync data : ", "2");

        handler = new OnlineOffilneDataManagement(context);

        ArrayList<SqliteDetail> documentDetail = handler.getAllDocument();
        ArrayList<SqliteDetail> pancardDetail = handler.getAllDatas();
        ArrayList<SqliteDetail> licenceDetail = handler.getAllLicence();
        ArrayList<SqliteDetail> passportDetail = handler.getAllPassport();

        if (documentDetail.size() < 1 && pancardDetail.size() < 1 && licenceDetail.size() < 1 && passportDetail.size() < 1) {
            //fixme: in pref this is being saved but not getting fetched
            Scanner.getInstance().getPreferences().setSyncSqlite(true);
            return;
        }

        if (documentDetail.size() > 0) {
            for (int i = 0; i < documentDetail.size(); i++) {
                File file = CaptureImage(documentDetail.get(i).getImage_Bitmap(), Constants.businesscard);

                sendcard(new CardDetail(documentDetail.get(i).getCard_name(), documentDetail.get(i).getScan_time(),
                        file.getAbsolutePath(), file.length() / 1024), Constants.businesscard, documentDetail.get(i).getId());

            }
        }
        if (pancardDetail.size() > 0) {

            for (int i = 0; i < pancardDetail.size(); i++) {

                File file = CaptureImage(pancardDetail.get(i).getImage_Bitmap(), Constants.pancard);
                sendcard(new CardDetail(pancardDetail.get(i).getCard_name(), pancardDetail.get(i).getDate_of_birth(), pancardDetail.get(i).getCard_unique_no(),
                                pancardDetail.get(i).getScan_time(), file.getAbsolutePath(), file.length() / 1024),
                        Constants.pancard, pancardDetail.get(i).getId());
            }
        }
        if (licenceDetail.size() > 0) {
            for (int i = 0; i < licenceDetail.size(); i++) {
                File file = CaptureImage(licenceDetail.get(i).getImage_Bitmap(), Constants.licence);

                sendcard(new CardDetail(licenceDetail.get(i).getCard_name(), licenceDetail.get(i).getCard_unique_no(),
                                licenceDetail.get(i).getDate_of_birth(), licenceDetail.get(i).getIssue_date(), licenceDetail.get(i).getTill_date(),
                                licenceDetail.get(i).getBirth_place(),
                                licenceDetail.get(i).getIssue_address(), file.getAbsolutePath(),
                                licenceDetail.get(i).getScan_time(), file.length() / 1024),
                        Constants.licence, licenceDetail.get(i).getId());
            }
        }
        if (passportDetail.size() > 0) {

            for (int i = 0; i < passportDetail.size(); i++) {
                File file = CaptureImage(passportDetail.get(i).getImage_Bitmap(), Constants.passport);

                sendcard(new CardDetail(passportDetail.get(i).getCard_name(), passportDetail.get(i).getCard_unique_no(),
                                passportDetail.get(i).getDate_of_birth(), passportDetail.get(i).getIssue_date(), passportDetail.get(i).getTill_date(),
                                passportDetail.get(i).getBirth_place(),
                                passportDetail.get(i).getIssue_address(), file.getAbsolutePath(),
                                passportDetail.get(i).getScan_time(), file.length() / 1024),
                        Constants.passport, passportDetail.get(i).getId());
            }
        }

    }

    public void sendcard(final CardDetail cardDetail, String tag, int id) {
        Log.e("sync data : ", tag);
        DatabaseHandler handler1 = new DatabaseHandler(context);
        cardDetail.setScan_time(cardDetail.getScan_time() + id);

        PreferenceManagement preferences = Scanner.getInstance().getPreferences();

        preferences.setSizeDetail((int) (preferences.getSizeDetail() + cardDetail.getImage_size()));
        if (preferences.getSizeDetail() < 20000) {
            handler1.sqliteInsertData(cardDetail, tag, "false");
        }

        if (tag.equals(Constants.businesscard)) {
            handler.deletedocument(String.valueOf(id));
        } else if (tag.equals(Constants.pancard)) {
            handler.deletepancard(String.valueOf(id));
        } else if (tag.equals(Constants.licence)) {
            handler.deletelicence(String.valueOf(id));
        } else if (tag.equals(Constants.passport)) {
            handler.deletepassport(String.valueOf(id));
        }
    }

    public void sendcardInfo(final CardDetail cardDetail, String tag, Bitmap bitmap, final int id) {

        //  if(isInternetOn()){
        final String sqliteTag = tag;
        tag = "-" + tag;
        final String finalTag = tag;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        final long size = data.length / 1024L;

        if (spaceManagement(size)) {

            if (data != null) {
                cardDetail.setScan_time(cardDetail.getScan_time() + id);
                final StorageReference childRef = storageRef.child(Globalarea.firebaseUser.getUid())
                        .child(Globalarea.firebaseUser.getUid() + finalTag)
                        .child(cardDetail.getScan_time());

                UploadTask uploadTask = childRef.putBytes(data);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        childRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
//                                    Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();

                                    String generatedFilePath = task.getResult().toString();
                                    cardDetail.setImage_url(generatedFilePath);
                                    cardDetail.setImage_size(size);

                                    databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + finalTag)) {
                                                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + finalTag)) {
                                                            Log.e("Sqlite", "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + finalTag + " exists");
                                                            databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag).
                                                                    child(cardDetail.getScan_time())
                                                                    .setValue(cardDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        System.out.println("network error :=== " + task.getException());

                                                                    } else {
                                                                        if (sqliteTag.equals(Constants.businesscard)) {
//                                                                handler.deletedocument(String.valueOf(id));
                                                                        } else if (sqliteTag.equals(Constants.pancard)) {
//                                                                handler.deletepancard(String.valueOf(id));
                                                                        } else if (sqliteTag.equals(Constants.licence)) {
//                                                                handler.deletelicence(String.valueOf(id));
                                                                        } else if (sqliteTag.equals(Constants.passport)) {
//                                                                handler.deletepassport(String.valueOf(id));
                                                                        }
                                                                        Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.availableSpace - size);
                                                                        Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.usedSpace + size);
                                                                        sendSizeDetail();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    } else {

                                                        databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                                                                .child(cardDetail.getScan_time())
                                                                .setValue(cardDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (!task.isSuccessful()) {

                                                                    System.out.println("network error :=== " + task.getException());

                                                                } else {
                                                                    if (sqliteTag.equals(Constants.businesscard)) {
//                                                            handler.deletedocument(String.valueOf(id));
                                                                    } else if (sqliteTag.equals(Constants.pancard)) {
//                                                            handler.deletepancard(String.valueOf(id));
                                                                    } else if (sqliteTag.equals(Constants.licence)) {
//                                                            handler.deletelicence(String.valueOf(id));
                                                                    } else if (sqliteTag.equals(Constants.passport)) {
//                                                            handler.deletepassport(String.valueOf(id));
                                                                    }
                                                                    Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.availableSpace - size);
                                                                    Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.usedSpace + size);
                                                                    sendSizeDetail();
                                                                }
                                                            }
                                                        });

                                                    }
                                                    // send push notification to the receiver
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
//                mOnSendMessageListener.onSendMessageFailure("Unable to send message: " + databaseError.getMessage());
                                                    System.out.println("error : " + databaseError.getMessage());
                                                }
                                            });
                                }
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }

        } else {
            //    taskListener.onTaskError(Constants.UploadError);
        }
        //   }else{
        //   taskListener.onTaskError(Constants.InternetConnectionFail);

        // }
    }

    private boolean spaceManagement(long imageSize) {

        //            Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.availableSpace - imageSize);
//            Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.usedSpace + imageSize);
        return Globalarea.sizeDetail.availableSpace > imageSize;
    }

    public void sendSizeDetail() {


        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-sizeDetail")) {
                            Log.e("Sqlite", "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-sizeDetail" + " exists");
                            databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + "-sizeDetail")
                                    .child("info")
                                    .setValue(Globalarea.sizeDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
//                                        taskListener.onTaskFinished("Notset");

                                        getSpaceList();
                                    } else {
//                                        taskListener.onTaskFinished("set");
                                    }
                                }
                            });


                        }
                        // send push notification to the receiver
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                mOnSendMessageListener.onSendMessageFailure("Unable to send message: " + databaseError.getMessage());
                        System.out.println("error : " + databaseError.getMessage());
                    }
                });
    }

    private void getSpaceList() {
        //  if(isInternetOn()) {

        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-sizeDetail")) {
                            Log.e("sizeDetail", "getMessageFromFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-sizeDetail" + " exists");
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child(Globalarea.firebaseUser.getUid())
                                    .child(Globalarea.firebaseUser.getUid() + "-sizeDetail")
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                            SizeDetail businessCard = dataSnapshot.getValue(SizeDetail.class);
                                            Globalarea.sizeDetail = businessCard;

                                        }

                                        @Override
                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        // }else{
        // taskListener.onTaskError(Constants.InternetConnectionFail);

        // }
    }

    public File CaptureImage(Bitmap bitmap, String tag) {
        /* MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), card, "Detected Image", ""); */
        try {

            File imageFile;
            File dir;
            dir = new File(context.getExternalCacheDir() + "/" + context.getResources().getString(R.string.app_name), tag);
//            File file = new File(this.getExternalCacheDir(), "image.png");
            boolean success = true;
            if (!dir.exists()) {
                success = dir.mkdirs();
            }
            if (success) {
                Date date = new Date();
                imageFile = new File(dir.getAbsolutePath()
                        + File.separator
                        + new java.sql.Timestamp(date.getTime()).toString()
                        + "Image.jpg");

                imageFile.createNewFile();
            } else {
                return null;
            }
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

            // save image into gallery
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

            FileOutputStream fout = new FileOutputStream(imageFile);
            fout.write(ostream.toByteArray());
            fout.close();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN,
                    System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA,
                    imageFile.getAbsolutePath());
            return imageFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
