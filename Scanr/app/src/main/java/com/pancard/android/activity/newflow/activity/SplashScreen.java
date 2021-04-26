package com.pancard.android.activity.newflow.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.database.DriveDocRepo;
import com.pancard.android.utility.ConnectionDetector;
import com.pancard.android.utility.PreferenceManagement;


public class SplashScreen extends AppCompatActivity {

    ImageView splashImageView;
    PreferenceManagement preferences;
    DriveDocRepo driveDocRepo;
    TextView textViewVersionCode;
    ConnectionDetector connectionDetector;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        textViewVersionCode = findViewById(R.id.version_code);
        String strVersion = "Version " + Scanner.Current_Version;
        textViewVersionCode.setText(strVersion);

        Log.e("creating", "splash");
        driveDocRepo = new DriveDocRepo(this);
        connectionDetector = new ConnectionDetector(this);
        preferences = Scanner.getInstance().getPreferences();
        splashImageView = findViewById(R.id.img_logo);

        auth = FirebaseAuth.getInstance();

        if (preferences.getFirstOpen() == null) {
            preferences.setFirstOpen("open");
        }

        performScopeMigration();

//        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
//        splashImageView.startAnimation(slide);

        Handler handler = new Handler();
        handler.postDelayed(() -> nextActivity(), 2000);

    }

    private void nextActivity() {

        Log.e("moving", "to next activity");

        if (auth.getCurrentUser() != null) {
            Globalarea.firebaseUser = auth.getCurrentUser();
        }

        Bundle extras = getIntent().getExtras();

        Intent mIntent = getIntent();
        String action = mIntent.getAction();
        String type = mIntent.getType();
        if (action != null && action.equals(Intent.ACTION_SEND) && type != null) {

            if (type.startsWith("image/")) {
                Uri mUri = mIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                Intent intent = new Intent(SplashScreen.this, NewHomeActivity.class);
                intent.putExtra("mUri", mUri.toString());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Unable to Share...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SplashScreen.this, NewHomeActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Intent intent;

            if (extras != null) {
                Log.e("skip to", "home");
                String Card_number = extras.getString("CardNumber");
                if (Card_number != null)
                    Log.e("card number", Card_number);
                byte[] CreditCardImage = extras.getByteArray("CreditCardImage");
                String CardType = extras.getString("CardType");
                if (CardType != null)
                    Log.e("card type", CardType);
                String CreditCardExpiryDate = extras.getString("CreditCardExpiryDate");
                if (CreditCardExpiryDate != null)
                    Log.e("expiry date", CreditCardExpiryDate);

                if (Card_number != null && CreditCardImage != null) {
                    intent = new Intent(SplashScreen.this, NewHomeActivity.class);
                    intent.putExtra("CardNumber", Card_number);
                    intent.putExtra("CreditCardImage", CreditCardImage);
                    intent.putExtra("CardType", CardType);
                    intent.putExtra("CreditCardExpiryDate", CreditCardExpiryDate);
                } else {
                    Log.e("lets", "login");

                    //todo: Replace with below intent for Singin Screen First
//                    intent = new Intent(SplashScreen.this, SignInActivity.class);

                    if (preferences.isShowedIntroScreen()) {
                        intent = new Intent(SplashScreen.this, NewHomeActivity.class);
                    } else {
                        intent = new Intent(SplashScreen.this, IntroductionSliderActivity.class);
                    }
                }
            } else {
                Log.e("lets", "login");

                //todo: Replace with below intent for Singin Screen First
//                intent = new Intent(SplashScreen.this, SignInActivity.class);
                if (preferences.isShowedIntroScreen()) {
                    intent = new Intent(SplashScreen.this, NewHomeActivity.class);
                } else {
                    intent = new Intent(SplashScreen.this, IntroductionSliderActivity.class);
                }
            }

            Log.e("starting new ", "activity");
            startActivity(intent);
            finish();
        }
    }

    private void performScopeMigration() {
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String currentVersion = packageInfo.versionName;
            String oldVersion = "5.0.20";

            if (isCurrentVersionNewer(oldVersion, currentVersion)) {
                Log.e("new version", "detected");

                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (!preferences.isMigrationDone() && account != null) {

                    if (connectionDetector.isConnectingToInternet1()) {
                        Log.e("migrations ", "started");
//                  Toast.makeText(this, "migrating", Toast.LENGTH_SHORT).show();

                        int updated = driveDocRepo.updateAllSyncedRecordsWhileUpgradingToNewScope();
                        Log.e("updated rows un synced", String.valueOf(updated));
//                                    Toast.makeText(this, updated + "rows", Toast.LENGTH_SHORT).show();
                        preferences.setMigrationDone(true);

                        GoogleSignInClient mGoogleSignInClient;

                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.google_client_id))
                                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                                .requestEmail()
                                .build();
                        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

                        mGoogleSignInClient.signOut().
                                addOnCompleteListener(task -> {
                                    preferences.setIsDriveConnected(false);
                                });
                    }

                } else {
                    preferences.setMigrationDone(true);
                }

            } else {
                Log.e("not newer", "version");
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private boolean isCurrentVersionNewer(String oldVersion, String currentVersion) {

        if (!oldVersion.contains(".") || !currentVersion.contains(".")) {
            return false;
        }

        String[] oldVparts = oldVersion.split("\\."); // escape .
        String[] newVparts = currentVersion.split("\\."); // escape .
//        String part1 = parts[0];
//        String part2 = parts[1];

        if (oldVparts.length < 2 || newVparts.length < 2) {
            return false;
        }

        int oldPart0 = Integer.parseInt(oldVparts[0]);
        int oldPart1 = Integer.parseInt(oldVparts[1]);
        int oldPart2 = Integer.parseInt(oldVparts[2]);

        int newPart0 = Integer.parseInt(newVparts[0]);
        int newPart1 = Integer.parseInt(newVparts[1]);
        int newPart2 = Integer.parseInt(newVparts[2]);

        if (oldPart0 < newPart0) {
//            if (!oldVparts[0].equals("0"))
            return true;
        } else if (oldPart0 == newPart0) {
            if (oldPart1 < newPart1) {
                return true;
            } else if (oldPart1 == newPart1) {
                return oldPart2 < newPart2;
            }
        }

        return false;
    }

}