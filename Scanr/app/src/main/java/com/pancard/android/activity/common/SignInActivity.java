package com.pancard.android.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.pancard.android.Globalarea;
import com.pancard.android.activity.otheracivity.HomeActivity;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.PreferenceManagement;

import java.util.Arrays;


public class SignInActivity extends AppCompatActivity {

    final int GOOGLE_REQUEST_CODE = 101;

    CardView cardViewGoogle, cardViewFacebook, cardViewTwitter;
    CallbackManager mCallbackManager;
    PreferenceManagement preferences;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_screen);
        bindViews();
        initialise();
    }

    private void loginWithTwitter() {

        if (!new ConnectionDetector(this).isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.internetConnectionFail), Toast.LENGTH_SHORT).show();
            return;
        }

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
                            Toast.makeText(SignInActivity.this, getResources().getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
                            success();
                        })
                .addOnFailureListener(
                        e -> {
                            // Handle failure.
                            Toast.makeText(SignInActivity.this, getResources().getString(R.string.auth_failure), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Globalarea.firebaseUser != null) {
            Log.e("from login", "already logged in");
            moveToHome();
        }
    }

    private void bindViews() {
        cardViewGoogle = findViewById(R.id.cardViewGoogle);
        cardViewFacebook = findViewById(R.id.cardViewFB);
        cardViewTwitter = findViewById(R.id.cardViewTwitter);
    }

    private void initialise() {

        preferences = new PreferenceManagement(this);
        mAuth = FirebaseAuth.getInstance();

        //todo: You must pass your server's client ID to the requestIdToken method.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mCallbackManager = CallbackManager.Factory.create();

        cardViewGoogle.setOnClickListener(v -> loginWithGoogle());
        cardViewFacebook.setOnClickListener(v -> loginWithFacebook());
        cardViewTwitter.setOnClickListener(v -> loginWithTwitter());

    }

    private void moveToHome() {
//        Intent intent = new Intent(this, BottomBarActivity.class);
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginWithGoogle() {

        if (!new ConnectionDetector(this).isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.internetConnectionFail), Toast.LENGTH_SHORT).show();
            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    firebaseAuthWithGoogle(account);
//                    handleDrive(data);
                } else
                    Toast.makeText(this, "failed - null firebase account", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("Google sign in failed", e);
                Toast.makeText(this, "failed on activity result" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (mCallbackManager != null)
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("firebaseAuthWithGoogle:", acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("signInWithCredential", "success");
                        Globalarea.firebaseUser = mAuth.getCurrentUser();
                        getPreferences().setIsDriveConnected(true);
                        getPreferences().setShowedDriveDialog(true);
                        Toast.makeText(SignInActivity.this, getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
                        success();
//                            updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("signInWithCredential", "failure");

                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                            Toast.makeText(SignInActivity.this, "firebase auth with google" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loginWithFacebook() {

        if (!new ConnectionDetector(this).isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.internetConnectionFail), Toast.LENGTH_SHORT).show();
            return;
        }

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
                        Toast.makeText(SignInActivity.this, getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
                        success();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("signInWithCredential", "failure", task.getException());
                        Toast.makeText(SignInActivity.this, getString(R.string.auth_failure2),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void success() {
        moveToHome();
    }

    public PreferenceManagement getPreferences() {
        if (preferences != null)
            return preferences;
        else
            return new PreferenceManagement(this);
    }
}