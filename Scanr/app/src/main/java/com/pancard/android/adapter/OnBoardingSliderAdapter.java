package com.pancard.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.docscan.android.R;
import com.pancard.android.model.OnBoardingModel;

import java.util.ArrayList;
import java.util.List;

public class OnBoardingSliderAdapter extends PagerAdapter {

    private OnButtonClick onButtonClick;
    private Context context;
    private List<View> pageIndicators;
    private List<OnBoardingModel> onBoardingModelList;

    public OnBoardingSliderAdapter(Context context, List<OnBoardingModel> onBoardingModelList, OnButtonClick onButtonClick) {
        this.context = context;
        this.onBoardingModelList = onBoardingModelList;
        this.onButtonClick = onButtonClick;
    }

    @Override
    public int getCount() {
        return onBoardingModelList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.slider_view, container, false);

        ImageView imageView = view.findViewById(R.id.intro_image);
        TextView tvDescription = view.findViewById(R.id.intro_desc);
//        LinearLayout indicatorLayout = view.findViewById(R.id.indicator_layout);
        Button button = view.findViewById(R.id.btn_next_slide);
        TextView tvBoardTitle = view.findViewById(R.id.intro_title);

        OnBoardingModel onBoardingModel = onBoardingModelList.get(position);

        imageView.setImageDrawable(onBoardingModel.getDrawable());
        tvDescription.setText(onBoardingModel.getDescription());

//        setupIndicators(indicatorLayout, position);

        if (onBoardingModel.getTitle() != null) {
            tvBoardTitle.setText(onBoardingModel.getTitle());
        }

        /*if (position == getCount() - 1) {
            button.setText(context.getString(R.string.str_walk_through_button_last));
        } else {
            button.setText(context.getString(R.string.str_walk_through_button1));
        } */

        button.setOnClickListener(v -> {
            if (onButtonClick != null) {
//                Toast.makeText(v.getContext(),"Checking execution",Toast.LENGTH_SHORT).show();
                onButtonClick.onButtonClick(position);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private void setupIndicators(LinearLayout indicatorLayout, int pos) {
        int count = getCount();
        pageIndicators = new ArrayList<>();
        indicatorLayout.removeAllViews();

        for (int i = 0; i < count; i++) {
            addViewToLayout(indicatorLayout);
        }

        enableView(pageIndicators.get(pos));
    }

    private void addViewToLayout(LinearLayout indicatorLayout) {

        float density = context.getResources().getDisplayMetrics().density;
        int margin = Math.round((float) 4 * density);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin, margin, margin, margin);

        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(layoutParams);
        imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.slider_indicator));

        indicatorLayout.addView(imageView);
        pageIndicators.add(imageView);

    }

    private void enableView(View view) {
        //first disable all the other views and enabling only one
        for (View v : pageIndicators) {
            v.setEnabled(false);
        }
        view.setEnabled(true);
    }

    public interface OnButtonClick {
        void onButtonClick(int pos);
    }

    public static class CustomTransformer1 implements ViewPager.PageTransformer {

        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View page, float position) {
            int pageWidth = page.getWidth();
            final float pageHeight = page.getHeight();
            final float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
//            final float scale = min(position < 0 ? 1f : Math.abs(1f - position), 0.5f);
            final float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            final float horzMargin = pageWidth * (1 - scaleFactor) / 2;

            if (position >= -1 || position <= 1) {
                ImageView imageView = page.findViewById(R.id.intro_image);
                imageView.setTranslationX(-position * 1.7f * pageWidth); //Half the normal speed

                TextView tvDescription = page.findViewById(R.id.intro_desc);
                tvDescription.setTranslationX(-position * 1.3f * pageWidth);

                TextView tvTitle = page.findViewById(R.id.intro_title);
                tvTitle.setTranslationX(-position * 1.2f * pageWidth);
            }

            //reffrence from the zoomoutslidetransformer class
            page.setPivotY(0.5f * pageHeight);
            page.setPivotX(0.5f * pageWidth);

            if (position < 0) {
                page.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                page.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            // Fade the page relative to its size.
//            page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        }
    }
}
