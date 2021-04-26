package com.pancard.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.docscan.android.R;
import com.pancard.android.model.FilePopupModel;

import java.util.List;

public class FilesMenuAdapter extends BaseAdapter {

    Context context;
    List<FilePopupModel> filePopupModelList;

    public FilesMenuAdapter(Context context, List<FilePopupModel> filePopupModelList) {
        this.context = context;
        this.filePopupModelList = filePopupModelList;
    }

    @Override
    public int getCount() {
        return filePopupModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return filePopupModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
//        View textview;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view == null) {
            view = inflater.inflate(R.layout.popup_item, null);
            TextView textView = view.findViewById(R.id.text1);
            ImageView imageView = view.findViewById(R.id.img_option);

            FilePopupModel filePopupModel = filePopupModelList.get(position);

            if (textView != null) {
//                textView.setCompoundDrawablesWithIntrinsicBounds(filePopupModel.iconRes,null,null,null);
                textView.setText(filePopupModel.category);
            }
            if (imageView != null) {
                imageView.setImageDrawable(filePopupModel.iconRes);
            }
        }

        return view;
    }
}
