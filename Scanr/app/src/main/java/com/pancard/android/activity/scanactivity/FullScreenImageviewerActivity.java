package com.pancard.android.activity.scanactivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.docscan.android.R;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.Globalarea;
import com.pancard.android.utility.Constants;

import ooo.oxo.library.widget.TouchImageView;

public class FullScreenImageviewerActivity extends AppCompatActivity {

    static String whichCard;
    Button btn_nxt;
    TouchImageView document;
    String activity = "";
    boolean displayImage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_imageviewer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");

            final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
            if (upArrow != null) {
                upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            }
            actionBar.setHomeAsUpIndicator(upArrow);

        }

        document = findViewById(R.id.fullscreenImage);
        btn_nxt = findViewById(R.id.btn_nxt);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String data = extras.getString("TAG_CAMERA");
            activity = extras.getString("activity");
            if (data != null) {
                whichCard = data;
                if (whichCard.equals(Constants.adharcard)) {
                    btn_nxt.setVisibility(View.VISIBLE);
                    document.setImageBitmap(Globalarea.firstDisplayImage);
                } else {
                    document.setImageBitmap(Globalarea.document_image);
                }
            }
        }

//        if (document != null && Globalarea.document_image != null)
//            document.setImageBitmap(Globalarea.document_image);

        if (Globalarea.adharCard_back_image != null && whichCard.equals(Constants.adharcard))
            document.setImageBitmap(Globalarea.adharCard_back_image);

        btn_nxt.setOnClickListener(v -> {
            if (displayImage) {
                document.setImageBitmap(Globalarea.secondDisplayImage);
                displayImage = false;
            } else {
                document.setImageBitmap(Globalarea.firstDisplayImage);
                displayImage = true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        if (activity.equals("specificactivity")) {
            intent = new Intent(FullScreenImageviewerActivity.this, SpecificPage.class);
            if (getIntent() != null) {
                DriveDocModel driveDocModel = (DriveDocModel) getIntent().getSerializableExtra(Constants.CARD_DATAT);
                intent.putExtra(Constants.CARD_DATAT, driveDocModel);
            }
        } else {
            intent = new Intent(FullScreenImageviewerActivity.this, CardScanActivity.class);
        }

        intent.putExtra("TAG_CAMERA", whichCard);
        startActivity(intent);
        finish();
    }
}
