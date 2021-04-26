package com.pancard.android.activity.otheracivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.docscan.android.R;
import com.pancard.android.Scanner;


public class ContactUs extends AppCompatActivity {

    TextView versionCode;
    Toolbar toolbar;
    ImageView imgBack;
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        bindViews();
        initialise();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        imgBack = findViewById(R.id.img_back_button);
        tvTitle = findViewById(R.id.tv_title_text);
    }

    private void initialise() {
        tvTitle.setText(getString(R.string.str_help));
        setSupportActionBar(toolbar);

        imgBack.setOnClickListener(view -> onBackPressed());

        versionCode = findViewById(R.id.version_code);
        int versionnumber = Scanner.Current_Version_Code;
        String strVersion = getString(R.string.str_version) + " " + Scanner.Current_Version;

        if (versionnumber > 0) {
            strVersion = strVersion + " " + "(" + versionnumber + ")";
        }

        versionCode.setText(strVersion);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Intent intent = new Intent(ContactUs.this, HomeActivity.class);
//        startActivity(intent);
//        finish();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.more_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//
////            case R.id.about:
////                Intent intent = new Intent(this, ContactUs.class);
////                startActivity(intent);
////                finish();
////                return true;
//
//            case R.id.setting:
//                Intent intent2 = new Intent(this, SettingActivity.class);
//                startActivity(intent2);
//                finish();
//                return true;
//
//            case R.id.view_files:
//                Globalarea.openAllFilesMenu(this);
//                return true;
//        }
//        return true;
//    }
}
