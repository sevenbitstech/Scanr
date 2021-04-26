package com.pancard.android.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.docscan.android.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pancard.android.utility.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class TextDetailBottomSheet extends BottomSheetDialogFragment {

    private static OnUpdateDocument onUpdateDocument;
    private ImageView imgDownArrow;
    private EditText etDetails;
    private ImageView imgShare;
    private ImageView imgEdit;
    private ImageView imgSave, imgCancel;
    private String oldText;

    public TextDetailBottomSheet() {
        // Required empty public constructor
    }

    public static TextDetailBottomSheet newInstance(String text) {
        TextDetailBottomSheet textDetailBottomSheet = new TextDetailBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_BOTTOM_SHEET, text);
        textDetailBottomSheet.setArguments(bundle);

        return textDetailBottomSheet;

    }

    public void setEditListener(OnUpdateDocument onUpdateDocumentListner) {
        onUpdateDocument = onUpdateDocumentListner;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        bindViews(view);
        initialize();
        return view;
    }

    private void bindViews(View view) {
        etDetails = view.findViewById(R.id.tv_detail_text);
        imgDownArrow = view.findViewById(R.id.img_up_arrow);
        imgShare = view.findViewById(R.id.img_share);
        imgEdit = view.findViewById(R.id.img_edit);
        imgSave = view.findViewById(R.id.img_save);
        imgCancel = view.findViewById(R.id.img_cancel);
    }

    private void initialize() {

        setTextViewDetails();

        imgDownArrow.setOnClickListener(view -> dismiss());

        imgShare.setOnClickListener(view -> shareData());
        imgEdit.setOnClickListener(v -> {
            imgSave.setVisibility(View.VISIBLE);
            imgCancel.setVisibility(View.VISIBLE);
            etDetails.setEnabled(true);
        });

        imgSave.setOnClickListener(v -> {
            if (etDetails.getText().toString().length() > 0) {
                onUpdateDocument.onUpdateDocument(oldText, etDetails.getText().toString());
            } else {
                etDetails.setError("*Required");
            }

        });

        imgCancel.setOnClickListener(v -> {
            imgSave.setVisibility(View.GONE);
            imgCancel.setVisibility(View.GONE);
            etDetails.setEnabled(true);
            setTextViewDetails();
        });
    }

    private void setTextViewDetails() {
        if (getArguments() != null && getArguments().containsKey(Constants.KEY_BOTTOM_SHEET)) {
            String text = getArguments().getString(Constants.KEY_BOTTOM_SHEET);
            oldText = text;
            etDetails.setText(text);
        }
    }

    private void shareData() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, etDetails.getText().toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    public interface OnUpdateDocument {
        void onUpdateDocument(String oldText, String newText);
    }
}
