package com.pancard.android.firebase;

import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;

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
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.asyntask.FirebaseDownloaderTask;
import com.pancard.android.listener.TaskListener;
import com.pancard.android.model.CardDetail;
import com.pancard.android.model.SecurityStatus;
import com.pancard.android.model.SizeDetail;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PreferenceManagement;

import java.io.ByteArrayOutputStream;

/**
 * Created by seven-bits-pc11 on 12/5/17.
 */
public class RetriveandSetOneSection {
    PreferenceManagement preferences;
    private String TAG = "Firebase retrive date";
    private TaskListener taskListener;
    private StorageReference storageRef;
    private DatabaseReference databaseReference;

    public RetriveandSetOneSection(TaskListener taskListener) {
        this.taskListener = taskListener;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        storageRef = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        StrictMode.setThreadPolicy(policy);

        preferences = Scanner.getInstance().getPreferences();
    }

    //Space Management

    public boolean spaceManagement(long imageSize) {

        if (Globalarea.sizeDetail.availableSpace > imageSize) {
//            Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.availableSpace - imageSize);
//            Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.usedSpace + imageSize);
            return true;
        } else {
            return false;
        }
    }

    public void sendSizeDetail() {
        if (isInternetOn()) {

            databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-sizeDetail")) {
                                Log.e(TAG, "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-sizeDetail" + " exists");
                                databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + "-sizeDetail")
                                        .child("info")
                                        .setValue(Globalarea.sizeDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
//                                        taskListener.onTaskFinished("Notset");
                                            //getSpaceList();
                                        } else {
                                            // getSpaceList();
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
        } else {
            taskListener.onTaskError(Constants.InternetConnectionFail);
        }
    }

    private void getSpaceList() {
        if (isInternetOn()) {

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
                                                //  taskListener.onTaskFinished("Notset");
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
        } else {
            taskListener.onTaskError(Constants.InternetConnectionFail);

        }
    }

    public void sendcardInfo(final CardDetail cardDetail, final String tag1, final Bitmap bitmap) {

        try {
            if (isInternetOn()) {

                final String tag = "-" + tag1;
                final String finalTag = tag;

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                final long size = data.length / 1024L;

                if (spaceManagement(size)) {

                    if (data != null) {

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
                                        if (task.isSuccessful()) {
//                                                          Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();

                                            if (task.getResult() != null) {
                                                String generatedFilePath = task.getResult().toString();
                                                cardDetail.setImage_url(generatedFilePath);
                                                cardDetail.setImage_size(size);

//                                            new DownloadImage(generatedFilePath).execute();

                                                databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + finalTag)) {
                                                                    Log.e(TAG, "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + finalTag + " exists");
                                                                    databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag).child(cardDetail.getScan_time())
                                                                            .setValue(cardDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (!task.isSuccessful()) {
                                                                                System.out.println("network error :=== " + task.getException());
                                                                                taskListener.onTaskFinished(Constants.SomethingWentWrong);
                                                                            } else {
                                                                                Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.availableSpace - size);
                                                                                Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.usedSpace + size);
                                                                                sendSizeDetail();

                                                                                taskListener.onTaskFinished("set");
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
                                                                            if (!task.isSuccessful()) {
                                                                                taskListener.onTaskFinished(Constants.SomethingWentWrong);
                                                                                System.out.println("network error :=== " + task.getException());

                                                                            } else {
                                                                                Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.availableSpace - size);
                                                                                Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.usedSpace + size);
                                                                                sendSizeDetail();
                                                                                taskListener.onTaskFinished("set");
                                                                            }

                                                                        }


                                                                    });

                                                                }
                                                                // send push notification to the receiver
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
//
                                                                System.out.println("error : " + databaseError.getMessage());
                                                                taskListener.onTaskFinished(Constants.SomethingWentWrong);
                                                            }
                                                        });

                                            } else {

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

                    }

                } else {
                    taskListener.onTaskError(Constants.UploadError);
                }
            } else {
                taskListener.onTaskError(Constants.InternetConnectionFail);

            }
        } catch (Exception e) {
            e.printStackTrace();
            taskListener.onTaskError(Constants.SomethingWentWrong);
        }

    }

    public void getCardDetailList(final String tag1) {
        if (isInternetOn()) {
            Globalarea.cardDetail.clear();
            //  Globalarea.tempArrayList.clear();

            String tag = "-" + tag1;

            final String finalTag = tag;
            databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + finalTag)) {
                                Log.e(TAG, "getMessageFromFirebaseUser: " + Globalarea.firebaseUser.getUid() +
                                        finalTag + " exists");
                                FirebaseDatabase.getInstance()
                                        .getReference()
                                        .child(Globalarea.firebaseUser.getUid())
                                        .child(Globalarea.firebaseUser.getUid() + finalTag)
                                        .addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                                CardDetail businessCard = dataSnapshot.getValue(CardDetail.class);
                                                Globalarea.cardDetail.add(businessCard);
                                            }

                                            @Override
                                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                                            System.out.println("network error :===1 "+task.getException());

                                            }

                                            @Override
                                            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                                            System.out.println("network error :===2 "+task.getException());

                                            }

                                            @Override
                                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                                            System.out.println("network error :===3 "+task.getException());

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                System.out.println("network error :===4 " + databaseError.getMessage());

                                            }
                                        });
                            } else {
                                Log.e(TAG, "getMessageFromFirebaseUser 3: no such room available");

                                taskListener.onTaskFinished("NoDataAvailable");
                            }

                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    new FirebaseDownloaderTask(taskListener, Globalarea.cardDetail).execute();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    System.out.println("network error :===5 " + databaseError.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        } else {
            taskListener.onTaskError(Constants.InternetConnectionFail);
        }
    }

    public void deleteCardData(final String userId, String tag, final long avilable, final long used) {
        if (isInternetOn()) {

            tag = "-" + tag;
            final String finalTag = tag;
            databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                    .child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        storageRef.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                                .child(userId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Globalarea.sizeDetail.setUsedSpace(used);
                                Globalarea.sizeDetail.setAvailableSpace(avilable);
//
                                sendSizeDetail();
                                taskListener.onTaskFinished("Delete");
                            }
                        });
                    } else {
                        System.out.println("network error :===6 " + task.getException());

                    }
                }
            });
        } else {
            taskListener.onTaskError(Constants.InternetConnectionFail);

        }
    }

    public void deletemultiplerow(final String userid, String tag, final long image_size) {
        if (isInternetOn()) {

            tag = "-" + tag;
            final String finalTag = tag;
            databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                    .child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                        storageRef.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                                .child(userid).delete();

                        Globalarea.sizeDetail.setUsedSpace(Globalarea.sizeDetail.getUsedSpace() - image_size);
                        Globalarea.sizeDetail.setAvailableSpace(Globalarea.sizeDetail.getAvailableSpace() + image_size);
                        sendSizeDetail();
                    }
                }
            });
        } else {
            taskListener.onTaskError(Constants.InternetConnectionFail);

        }
    }

    public void editCardvalue(final CardDetail businessCard, String tag) {
        if (isInternetOn()) {

            tag = "-" + tag;
            final String finalTag = tag;
            databaseReference.child(Globalarea.firebaseUser.getUid()).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + finalTag)) {
                        Log.e(TAG, "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + finalTag + " exists");
                        databaseReference.child(Globalarea.firebaseUser.getUid()).child(Globalarea.firebaseUser.getUid() + finalTag)
                                .child(businessCard.getScan_time())
                                .setValue(businessCard).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    taskListener.onTaskFinished("Notset");
                                    System.out.println("network error :===7 " + task.getException());

                                } else {
                                    taskListener.onTaskFinished("Update");
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("network error :===8 " + databaseError.getMessage());

                }
            });
        } else {
            taskListener.onTaskError(Constants.InternetConnectionFail);

        }
    }

    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) Scanner.getInstance().getSystemService(Scanner.getInstance().CONNECTIVITY_SERVICE);

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


//    private void UserLogin() {
//
//        final String email = getEmailId();
//
//        if (TextUtils.isEmpty(email)) {
//            displayAlert("Please Configure your gmail account!!!");
//
//            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        auth.createUserWithEmailAndPassword(email.trim(), "12345678")
//                .addOnCompleteListener(SplashScreen.this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (!task.isSuccessful()) {
//                            Login(email);
//                        } else {
//                            Globalarea.firebaseUser = task.getResult().getUser();
//                            Constants.ARG_UID = task.getResult().getUser().getUid();
//
//                            sendSizeDetailInfo();
//                        }
//                    }
//                });
//    }
//
//    public void sendSizeDetailInfo() {
//
//        final SizeDetail sizeDetail = new SizeDetail(20000L, 20000L, 0L);
//
//        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (!dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-sizeDetail")) {
//                            Log.e("Send", "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-sizeDetail" + " exists");
//                            databaseReference.child(Globalarea.firebaseUser.getUid())
//                                    .child(Globalarea.firebaseUser.getUid() + "-sizeDetail").child("info")
//                                    .setValue(sizeDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (!task.isSuccessful()) {
//                                        displayAlert("Oops something went wrong!!!");
////                                taskListener.onTaskFinished("Notset");
//                                    } else {
////                                taskListener.onTaskFinished("Update");
//                                        getDocumentList();
//                                    }
//                                }
//                            });
//                        } else {
////                            startActivity(new Intent(SplashScreen.this, HomeActivity.class));
////                            finish();
//                            getDocumentList();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//    }
//
//    public void getDocumentList() {
//
//        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-sizeDetail")) {
//                            Log.e("sizeDetail", "getMessageFromFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-sizeDetail" + " exists");
//                            FirebaseDatabase.getInstance()
//                                    .getReference()
//                                    .child(Globalarea.firebaseUser.getUid())
//                                    .child(Globalarea.firebaseUser.getUid() + "-sizeDetail").addChildEventListener(new ChildEventListener() {
//                                @Override
//                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                                    SizeDetail businessCard = dataSnapshot.getValue(SizeDetail.class);
//                                    Globalarea.sizeDetail = businessCard;
//
//                                }
//
//                                @Override
//                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                                }
//
//                                @Override
//                                public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                                }
//
//                                @Override
//                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//                                }
//                            });
//                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    sendSecurityStatus();
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }
//
//    public void sendSecurityStatus() {
//
//        final SecurityStatus securityStatus = new SecurityStatus(false,"0000");
//
//        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (!dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-SecurityStatus")) {
//                            Log.e("Send", "sendMessageToFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-SecurityStatus" + " exists");
//                            databaseReference.child(Globalarea.firebaseUser.getUid())
//                                    .child(Globalarea.firebaseUser.getUid() + "-SecurityStatus").child("info")
//                                    .setValue(securityStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (!task.isSuccessful()) {
//                                        displayAlert("Oops something went wrong!!!");
//                                    } else {
//                                        getSecurityStatus();
//                                    }
//                                }
//                            });
//                        } else {
//                            getSecurityStatus();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }
//
//    public void getSecurityStatus() {
//
//        databaseReference.child(Globalarea.firebaseUser.getUid()).getRef()
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                        if (dataSnapshot.hasChild(Globalarea.firebaseUser.getUid() + "-SecurityStatus")) {
//                            Log.e("SecurityStatus", "getMessageFromFirebaseUser: " + Globalarea.firebaseUser.getUid() + "-SecurityStatus" + " exists");
//                            FirebaseDatabase.getInstance()
//                                    .getReference()
//                                    .child(Globalarea.firebaseUser.getUid())
//                                    .child(Globalarea.firebaseUser.getUid() + "-SecurityStatus").addChildEventListener(new ChildEventListener() {
//                                @Override
//                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                                    SecurityStatus businessCard = dataSnapshot.getValue(SecurityStatus.class);
//                                    Globalarea.securityStatus = businessCard;
//
//                                }
//
//                                @Override
//                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                                }
//
//                                @Override
//                                public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                                }
//
//                                @Override
//                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//                                }
//                            });
//                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    if (Globalarea.securityStatus.isStatus())
//                                        Scanner.getInstance().getUtils().setPrefrences(Constants.pin, Globalarea.securityStatus.getPin());
//                                    else
//                                        Scanner.getInstance().getUtils().setPrefrences(Constants.pin, "");
//
//                                    if (!Scanner.getInstance().getUtils().getBooleanPreferences("SqliteSync")) {
//
//                                        SqliteDataSync sqliteDataSync = new SqliteDataSync(SplashScreen.this);
//                                        sqliteDataSync.syncData();
//                                    }else{
//                                        //      Toast.makeText(SplashScreen.this,"No data available..",Toast.LENGTH_LONG).show();
//                                    }
//
//
//                                    startActivity(new Intent(SplashScreen.this, HomeActivity.class));
//                                    finish();
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }
//
//    private void Login(String emailID) {
//        String email = emailID;
//        final String password = "12345678";
//
//        //authenticate user
//        auth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(SplashScreen.this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (!task.isSuccessful()) {
//                            // there was an error
//                            Toast.makeText(SplashScreen.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
//                            displayAlert("Oops something went wrong!!!");
//
//                        } else {
//                            Globalarea.firebaseUser = task.getResult().getUser();
//
//                            Constants.ARG_UID = task.getResult().getUser().getUid();
//                            sendSizeDetailInfo();
//                        }
//                    }
//                });
//    }
}
