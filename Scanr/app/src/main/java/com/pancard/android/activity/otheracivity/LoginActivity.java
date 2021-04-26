package com.pancard.android.activity.otheracivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.pancard.android.DriveOperations.ScanRDriveOperations;
import com.pancard.android.Globalarea;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    final int GOOGLE_REQUEST_CODE = 101;
    FloatingActionButton fabGoogle, fabFacebook, fabTwitter;
    Button btnSignIn;
    TextView tvSkip, tvForgotPassword;
    EditText etEmail, etPassword;
    CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        bindViews();
        initialise();

        tvSkip.setOnClickListener(v -> moveToHome());

        fabGoogle.setOnClickListener(v -> loginWithGoogle());

        fabFacebook.setOnClickListener(v -> loginWithFacebook());

        fabTwitter.setOnClickListener(v -> loginWithTwitter());
    }

    private void loginWithTwitter() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");

        mAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(
                        authResult -> {
                            // User is signed in.
                            // IdP data available in
                            // authResult.getAdditionalUserInfo().getProfile().
                            // The OAuth access token can also be retrieved:
                            // authResult.getCredential().getAccessToken().
                            // The OAuth secret can be retrieved by calling:
                            // authResult.getCredential().getSecret().
                            Globalarea.firebaseUser = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
                            success();
                        })
                .addOnFailureListener(
                        e -> {
                            // Handle failure.
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.auth_failure), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
    }

    @Override
    protected void onStart() {
        super.onStart();

//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            Log.e("already ", "logged in");
//            moveToHome();
//        }

        if (Globalarea.firebaseUser != null) {
            Log.e("from login", "already logged in");
            moveToHome();
        }
    }

    private void bindViews() {
        fabFacebook = findViewById(R.id.fab_facebook);
        fabGoogle = findViewById(R.id.fab_google);
        fabTwitter = findViewById(R.id.fab_twitter);
        btnSignIn = findViewById(R.id.btn_Sign_in);
        tvSkip = findViewById(R.id.tv_skip);
        tvForgotPassword = findViewById(R.id.forgot_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
    }

    private void initialise() {
        mAuth = FirebaseAuth.getInstance();


        //todo: uncomment below line to unable guest login and remove moveToHome() line
//        guestLogin();
//        moveToHome();

        //todo: You must pass your server's client ID to the requestIdToken method.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mCallbackManager = CallbackManager.Factory.create();

        btnSignIn.setOnClickListener(v -> {

            String strEmail = etEmail.getText().toString().trim();
            String strPass = etPassword.getText().toString().trim();

            if (strEmail.equals("")) {
                Toast.makeText(LoginActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
            } else if (strPass.trim().equals("")) {
                Toast.makeText(LoginActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
            } else {
                loginWithEmailPass(strEmail, strPass);
            }
        });
    }

    private void loginWithEmailPass(String strEmail, String strPass) {
        mAuth.signInWithEmailAndPassword(strEmail, strPass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Signin Successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Signin failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void guestLogin() {
//
//        if (Scanner.getInstance().getConnectionDetactor().isConnectingToInternet()) {
//            FirebaseUser currentUser = mAuth.getCurrentUser();
//
//            if (currentUser != null) {
//                Globalarea.firebaseUser = currentUser;
//                moveToHome();
//            } else {
//                ProgressDialog progressDialog = new ProgressDialog(this); // this = YourActivity
//                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                progressDialog.setTitle(getResources().getString(R.string.loading));
//                progressDialog.setMessage(getResources().getString(R.string.loading_msg));
//                progressDialog.setIndeterminate(true);
//                progressDialog.setCanceledOnTouchOutside(false);
//
//                mAuth.signInAnonymously()
//                        .addOnCompleteListener(this, task -> {
//                            if (task.isSuccessful()) {
//                                // Sign in success, update UI with the signed-in user's information
//                                Log.e("sign in", "signInAnonymously:success");
//                                Globalarea.firebaseUser = mAuth.getCurrentUser();
//                                progressDialog.dismiss();
//                                moveToHome();
//                            } else {
//                                // If sign in fails, display a message to the user.
//                                Log.w("sign in activity", "signInAnonymously:failure", task.getException());
//                                progressDialog.dismiss();
//                                Toast.makeText(LoginActivity.this, "Authentication failed.",
//                                        Toast.LENGTH_SHORT).show();
//
//                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                                builder.setCancelable(false);
//                                builder.setTitle("Error");
//                                builder.setMessage("Can not let you login. Please try again later");
//                                builder.setPositiveButton("OK", (dialog, which) -> {
//                                    dialog.dismiss();
//                                    onBackPressed();
//                                });
//                                builder.show();
//                            }
//                        });
//            }
//        } else {
//            Log.e("no ", "connection");
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setCancelable(false);
//            builder.setTitle("Error");
//            builder.setMessage(getString(R.string.internetConnectionFail));
//            builder.setPositiveButton("OK", (dialog, which) -> {
//                dialog.dismiss();
//                onBackPressed();
//            });
//            builder.show();
//        }
//    }

    private void moveToHome() {
//        Intent intent = new Intent(this, BottomBarActivity.class);
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginWithGoogle() {

        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = ScanRDriveOperations.getGoogleSignInIntent(this);
            startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);
        });

//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_REQUEST_CODE) {

            ScanRDriveOperations.handleGoogleSignInActivityResult(this, data, new ScanRDriveOperations.OnCompleteGoogleSignIn() {
                @Override
                public void onSuccessfulGoogleSignIn(GoogleSignInAccount account) {
                    firebaseAuthWithGoogle(account);
                }

                @Override
                public void onFailure(Exception e, String message) {
                    if (e != null && e.getMessage() != null)
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (mCallbackManager != null)
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

//    private void handleDrive(Intent result) {
//        // Build a new authorized API client service.
//        final NetHttpTransport HTTP_TRANSPORT;
//        try {
//            HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
//            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                    .setApplicationName(APPLICATION_NAME)
//                    .build();
//
//
//            // Print the names and IDs for up to 10 files.
//            FileList result2 = service.files().list()
//                    .setPageSize(10)
//                    .setFields("nextPageToken, files(id, name)")
//                    .execute();
//            List<File> files = result2.getFiles();
//            if (files == null || files.isEmpty()) {
//                System.out.println("No files found.");
//            } else {
//                System.out.println("Files:");
//                for (File file : files) {
//                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("firebaseAuthWithGoogle:", acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("signInWithCredential", "success");
                        Globalarea.firebaseUser = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
                        success();
//                            updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("signInWithCredential", "failure");

                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                            Toast.makeText(LoginActivity.this, "firebase auth with google" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }


//                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failure2), Toast.LENGTH_SHORT).show();

//                            updateUI(null);
                    }
                });
    }

    private void loginWithFacebook() {
        LoginManager.getInstance().logOut();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));

        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // login successful
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // login cancelled
                        Log.d("facebook", "onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // login error
                        Log.d("facebook", "on error");
                        exception.printStackTrace();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("FacebookAccessToken", String.valueOf(token));

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("signInWithCredential", "success");
                        Globalarea.firebaseUser = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
                        success();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("signInWithCredential", "failure", task.getException());
                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failure2),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void success() {
        moveToHome();
    }
}