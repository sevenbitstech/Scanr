package com.pancard.android.activity.newflow.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.docscan.android.R;
import com.pancard.android.Scanner;
import com.pancard.android.adapter.OnBoardingSliderAdapter;
import com.pancard.android.model.OnBoardingModel;

import java.util.ArrayList;
import java.util.List;

public class IntroductionSliderActivity extends AppCompatActivity {

    ViewPager viewPager;
    OnBoardingSliderAdapter onBoardingSliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction_slider);

        bindViews();
        initialize();
    }

    private void bindViews() {
        viewPager = findViewById(R.id.view_pager);
//        indicatorLayout = findViewById(R.id.indicator_layout);
    }

    private void initialize() {

        setStatusBarColour();

        onBoardingSliderAdapter = new OnBoardingSliderAdapter(this, getOnBoardingModelList(), pos -> {

            if (pos == onBoardingSliderAdapter.getCount() - 1) {

                Scanner.getInstance().getPreferences().setShowedIntroScreen(true);
                startLoginActivity();
            } else {
                viewPager.setCurrentItem(pos + 1, true);
            }
        });
        viewPager.setAdapter(onBoardingSliderAdapter);

//        setupIndicators();

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
//                enableView(pageIndicators.get(position));
            }
        });

        //2, 4, 9,12, 15 (11),

//        Transformer.TRANSFORM_CLASSES.get(12).getClazz().newInstance()
//        try {
        viewPager.setPageTransformer(true, new OnBoardingSliderAdapter.CustomTransformer1());
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        }

    }

    private void setStatusBarColour() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        }
    }

    private void startLoginActivity() {
//        FirebaseAuth.getInstance().signOut();
//        Globalarea.firebaseUser = null;
        Intent intent = new Intent(IntroductionSliderActivity.this, NewHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private List<OnBoardingModel> getOnBoardingModelList() {
        List<OnBoardingModel> onBoardingModelList = new ArrayList<>();
        onBoardingModelList.add(new OnBoardingModel(ContextCompat.getDrawable(this, R.drawable.walkthrough1),
                getString(R.string.str_title1), getString(R.string.new_intro_text_1)));
        onBoardingModelList.add(new OnBoardingModel(ContextCompat.getDrawable(this, R.drawable.walkthrough2),
                getString(R.string.str_title2), getString(R.string.new_intro_text_2)));
        onBoardingModelList.add(new OnBoardingModel(ContextCompat.getDrawable(this, R.drawable.pro_bg),
                getString(R.string.str_title3), getString(R.string.new_intro_text_3)));

        return onBoardingModelList;
    }

}
