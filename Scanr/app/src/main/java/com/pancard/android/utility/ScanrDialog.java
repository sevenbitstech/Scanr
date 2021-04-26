package com.pancard.android.utility;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.docscan.android.R;

public class ScanrDialog extends Dialog {

    Context context;
    private ImageView imgClose;
    private Button primaryButton;
    private Button secondaryButton;
    private TextView tvTitle;
    private TextView tvSubTitle;

    public ScanrDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        setupDialog();
    }

    public ScanrDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        setupDialog();
    }

    protected ScanrDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
        setupDialog();
    }

    private void setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) {
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        }

        setContentView(R.layout.dialog_scanr_theme);
        bindViews();
        primaryButton.setVisibility(View.GONE);
        secondaryButton.setVisibility(View.GONE);
        imgClose.setOnClickListener(v -> dismiss());
    }

    private void bindViews() {
        imgClose = findViewById(R.id.img_close);
        primaryButton = findViewById(R.id.btn_primary);
        secondaryButton = findViewById(R.id.btn_secondary);
        tvTitle = findViewById(R.id.tv_title);
        tvSubTitle = findViewById(R.id.tv_sub_title);
    }

    public ScanrDialog setTitleText(String title) {
        tvTitle.setText(title);
        return this;
    }

    public ScanrDialog setTitleText(String title, int resColor) {
        tvTitle.setText(title);
        tvTitle.setTextColor(ContextCompat.getColor(context, resColor));
        return this;
    }

    public ScanrDialog setSubTitleText(String subTitle) {
        tvSubTitle.setText(subTitle);
        return this;
    }

    public ScanrDialog setSubTitleTextFormatted(String formattedSubTitle) {
        tvSubTitle.setText(Html.fromHtml(formattedSubTitle));
        return this;
    }

    public ScanrDialog setSubtitleJustified(boolean shouldJustify) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (shouldJustify)
                tvSubTitle.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

            else
                tvSubTitle.setJustificationMode(Layout.JUSTIFICATION_MODE_NONE);
        }
        return this;
    }

    public ScanrDialog setPrimaryButton(String btnText, View.OnClickListener onClickListener) {
        primaryButton.setVisibility(View.VISIBLE);
        primaryButton.setText(btnText);
        primaryButton.setOnClickListener(onClickListener);
        return this;
    }

    public ScanrDialog setSecondaryButton(String btnText, View.OnClickListener onClickListener) {
        secondaryButton.setVisibility(View.VISIBLE);
        secondaryButton.setText(btnText);
        secondaryButton.setOnClickListener(onClickListener);
        return this;
    }

    public ScanrDialog setOnCloseListener(View.OnClickListener onClickListener) {
        imgClose.setOnClickListener(onClickListener);
        return this;
    }

    public ScanrDialog removeClose(boolean shouldRemove) {
        if (shouldRemove)
            imgClose.setVisibility(View.GONE);
        else
            imgClose.setVisibility(View.VISIBLE);

        return this;
    }

    public ScanrDialog removeSubTitle(boolean shouldRemove) {
        if (shouldRemove)
            tvSubTitle.setVisibility(View.GONE);
        else
            tvSubTitle.setVisibility(View.VISIBLE);

        return this;
    }
}