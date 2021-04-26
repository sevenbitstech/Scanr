package com.pancard.android.activity.scanactivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.docscan.android.R;
import com.google.android.material.snackbar.Snackbar;
import com.pancard.android.Globalarea;
import com.pancard.android.activity.otheracivity.HomeActivity;

public class QRCodeResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_result);

        TextView tvQrCode = findViewById(R.id.tv_qrcode);
        if (Globalarea.adharCardText != null) {
            tvQrCode.setText(Globalarea.adharCardText);
        } else {
            tvQrCode.setText(getString(R.string.error_invalid_qr));
            tvQrCode.setTextColor(Color.RED);

        }
    }

    public void OnCancel(View view) {
        nextActivity();
    }


    public void OnShare(View view) {
        try {
            if (Globalarea.adharCardText != null) {
                String shareBody = Globalarea.adharCardText;
                String subject = "Barcode or QR code Result";

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");

                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share Via"));
            } else {
                Snackbar.make(view, getString(R.string.no_content_share), Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        nextActivity();
    }

    private void nextActivity() {
        Intent intent = new Intent(QRCodeResultActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
