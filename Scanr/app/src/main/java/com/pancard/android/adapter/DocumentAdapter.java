package com.pancard.android.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.docscan.android.R;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.Scanner;
import com.pancard.android.database.SyncStatus;
import com.pancard.android.listview_design.SearchablePinnedHeaderListViewAdapter;
import com.pancard.android.listview_design.StringArrayAlphabetIndexer;
import com.pancard.android.utility.GlideApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DocumentAdapter extends SearchablePinnedHeaderListViewAdapter<DriveDocModel> {

    private ArrayList<DriveDocModel> driveDocModels;
    private Context context;
//        Firebase_ImageLoader firebase_imageLoader = new Firebase_ImageLoader(ListActivity.this);

    public DocumentAdapter(Context context, final ArrayList<DriveDocModel> driveDocModels, OnNoValues onNoValues) {
        this.context = context;
        super.setNoValuesCallback(onNoValues);
        setData(driveDocModels);
    }

    @Override
    public CharSequence getSectionTitle(int sectionIndex) {
        return ((StringArrayAlphabetIndexer.AlphaBetSection) getSections()[sectionIndex]).getName();
    }

    public void setData(final ArrayList<DriveDocModel> driveDocModels) {
        this.driveDocModels = driveDocModels;
        final String[] generatedContactNames = generateContactNames(driveDocModels);
        setSectionIndexer(new StringArrayAlphabetIndexer(generatedContactNames, true));
    }

    private String[] generateContactNames(final List<DriveDocModel> driveDocModels) {
        final ArrayList<String> contactNames = new ArrayList<String>();
        if (driveDocModels != null)
            for (final DriveDocModel driveDocModelEntity : driveDocModels)
                contactNames.add(driveDocModelEntity.getFolderName());
        return contactNames.toArray(new String[contactNames.size()]);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        final View rootView;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(context);
            rootView = mInflater.inflate(R.layout.listview_item, parent, false);
            holder.friendProfileCircularContactView = rootView
                    .findViewById(R.id.listview_item_ImageView);

            holder.friendName = rootView
                    .findViewById(R.id.listview_item_Pancardname);
            holder.headerView = rootView.findViewById(R.id.header_text);
            holder.timestamp = rootView.findViewById(R.id.timestamp);
            holder.statusImageView = rootView.findViewById(R.id.img_status);
            rootView.setTag(holder);
        } else {
            rootView = convertView;
            holder = (ViewHolder) rootView.getTag();
        }
        final DriveDocModel driveDocModel = getItem(position);
        String displayName = driveDocModel.getCardDetail().getCard_name();
        String name = driveDocModel.getCardDetail().getCard_name();

        String[] words = displayName.split(" ");

        String twoWords = "";
        if (words.length > 2) {
            twoWords = words[0] + " " + words[1];
            int totalCharSize = twoWords.length();

            displayName = twoWords.substring(0, Math.min(twoWords.length() - 1, 15));

        } else {
            displayName = displayName.substring(0, Math.min(displayName.length() - 1, 15));
        }

        if (displayName.length() > 0) {
            if (name.length() > 15)
                displayName = displayName + "...";

            holder.friendName.setText(displayName);
        } else {
            holder.friendName.setText("UNKNOWN");
        }

        holder.timestamp.setText("");
        if (driveDocModel.getCardDetail() != null && driveDocModel.getCardDetail().getScan_time() != null) {
            String scanTime = driveDocModel.getCardDetail().getScan_time();
            if (scanTime.trim().toLowerCase().substring(scanTime.trim().length() - 1).equals("m")) {
                holder.timestamp.setText(scanTime);
            } else {
                holder.timestamp.setText(scanTime.trim().substring(0, scanTime.trim().length() - 1));
            }
        }

        //todo: set image
        holder.friendProfileCircularContactView.setImageResource(R.drawable.ds_logo);

        if (driveDocModel.getImagePath() != null) {

            GlideApp.with(context)
                    .load(new File(driveDocModel.getImagePath()))
                    .placeholder(R.drawable.ds_logo)
                    .into(holder.friendProfileCircularContactView);

//            byte[] decodedString = Base64.decode(driveDocModel.getImageString(), Base64.DEFAULT);
//            byte[] decodedString = driveDocModel.getImageString().getBytes();
//
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//            holder.friendProfileCircularContactView.setImageBitmap(decodedByte);
        }

        if (driveDocModel.getSyncStatus() != null) {
            String status = driveDocModel.getSyncStatus();

            if (!Scanner.getInstance().getPreferences().isDriveConnected()) {
                holder.statusImageView.setImageDrawable(context.getDrawable(R.drawable.no_sync_24));
            } else {
                if (status.equals(SyncStatus.synced.toString())) {
                    holder.statusImageView.setImageDrawable(context.getDrawable(R.drawable.cloud_sync_24));
                } else if (status.equals(SyncStatus.unsynced.toString())) {
                    holder.statusImageView.setImageDrawable(context.getDrawable(R.drawable.sync_pending));
                } else {
                    holder.statusImageView.setImageDrawable(context.getDrawable(R.drawable.sync_pending));
                    Log.e("wrong status", status);
                }
            }


        } else
            holder.statusImageView.setVisibility(View.GONE);

//        Bitmap bmp = BitmapFactory.decodeFile(driveDocModel.getImage_url());
//        holder.friendProfileCircularContactView.setImageBitmap(bmp);

//           firebase_imageLoader.DisplayImage(driveDocModel.getImage_url(),holder.friendProfileCircularContactView,400,R.drawable.ds_logo);


        //todo: to highlight the searched text
//        if (searchString != null) {
//            String panCardHolderName = displayName.toLowerCase(Locale.getDefault());
//            searchString = searchString.toLowerCase();
//            if (panCardHolderName.contains(searchString.toLowerCase())) {
//                int startPos = panCardHolderName.indexOf(searchString);
//                int endPos = startPos + searchString.length();
//
//                Spannable spanText = Spannable.Factory.getInstance().newSpannable(holder.friendName.getText()); // <- EDITED: Use the original string, as `country` has been converted to lowercase.
//                spanText.setSpan(new ForegroundColorSpan(Color.BLUE), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                holder.friendName.setText(spanText, TextView.BufferType.SPANNABLE);
//            }
//        }

        bindSectionHeader(holder.headerView, null, position);
        return rootView;
    }

    @Override
    public boolean doFilter(final DriveDocModel item, final CharSequence constraint) {
        if (TextUtils.isEmpty(constraint))
            return true;
        final String displayName = item.getFolderName();
        return !TextUtils.isEmpty(displayName) && displayName.toLowerCase(Locale.getDefault())
                .contains(constraint.toString().toLowerCase(Locale.getDefault()));
    }

    @Override
    public ArrayList<DriveDocModel> getOriginalList() {
        return driveDocModels;
    }

    class ViewHolder {
        ImageView friendProfileCircularContactView, statusImageView;
        TextView friendName, headerView, timestamp;
    }

}
