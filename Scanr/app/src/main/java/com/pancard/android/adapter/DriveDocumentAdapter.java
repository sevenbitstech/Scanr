package com.pancard.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.docscan.android.R;
import com.pancard.android.listview_design.SearchablePinnedHeaderListViewAdapter;
import com.pancard.android.listview_design.StringArrayAlphabetIndexer;
import com.pancard.android.model.SqliteDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriveDocumentAdapter extends SearchablePinnedHeaderListViewAdapter<SqliteDetail> {

    private ArrayList<SqliteDetail> mContacts;
    private Context context;

    public DriveDocumentAdapter(Context context, final ArrayList<SqliteDetail> contacts, OnNoValues onNoValues) {
        super.setNoValuesCallback(onNoValues);
        this.context = context;
        setData(contacts);
    }

    @Override
    public CharSequence getSectionTitle(int sectionIndex) {
        return ((StringArrayAlphabetIndexer.AlphaBetSection) getSections()[sectionIndex]).getName();
    }

    public void setData(final ArrayList<SqliteDetail> contacts) {
        this.mContacts = contacts;
        final String[] generatedContactNames = generateContactNames(contacts);
        setSectionIndexer(new StringArrayAlphabetIndexer(generatedContactNames, true));
    }

    private String[] generateContactNames(final List<SqliteDetail> contacts) {
        final ArrayList<String> contactNames = new ArrayList<String>();
        if (contacts != null)
            for (final SqliteDetail contactEntity : contacts)
                contactNames.add(contactEntity.getCard_name());
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
            rootView.setTag(holder);
        } else {
            rootView = convertView;
            holder = (ViewHolder) rootView.getTag();
        }
        final SqliteDetail contact = getItem(position);
        final String displayName = contact.getCard_name();
        if (displayName.length() >= 1) {

            holder.friendName.setText(displayName);
        } else {
            holder.friendName.setText("UNKNOWN");
        }
        if (contact.getScan_time().trim().toLowerCase().substring(contact.getScan_time().trim().length() - 1).equals("m")) {
            holder.timestamp.setText(contact.getScan_time());
        } else {
            holder.timestamp.setText(contact.getScan_time().trim().substring(0, contact.getScan_time().trim().length() - 1));
        }
        holder.friendProfileCircularContactView.setImageResource(R.drawable.ds_logo);
        Bitmap bmp = BitmapFactory.decodeFile(contact.getImage_url());
        holder.friendProfileCircularContactView.setImageBitmap(bmp);
//           firebase_imageLoader.DisplayImage(contact.getImage_url(),holder.friendProfileCircularContactView,400,R.drawable.ds_logo);
//           holder.friendProfileCircularContactView.setImageBitmap(firebase_imageLoader.getBitmap(contact.getImage_url(),400));

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
    public boolean doFilter(final SqliteDetail item, final CharSequence constraint) {
        if (TextUtils.isEmpty(constraint))
            return true;
        final String displayName = item.getCard_name();
        return !TextUtils.isEmpty(displayName) && displayName.toLowerCase(Locale.getDefault())
                .contains(constraint.toString().toLowerCase(Locale.getDefault()));
    }

    @Override
    public ArrayList<SqliteDetail> getOriginalList() {
        return mContacts;
    }

    class ViewHolder {
        public ImageView friendProfileCircularContactView;
        TextView friendName, headerView, timestamp;
    }

}
