package com.pancard.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.docscan.android.R;
import com.google.gson.Gson;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.Globalarea;
import com.pancard.android.Utility;
import com.pancard.android.database.FileVersion;
import com.pancard.android.listener.AdapterDelegate;
import com.pancard.android.model.CardDetail;
import com.pancard.android.model.FilePopupModel;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.GlideApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StaggeredGridAdapter extends RecyclerView.Adapter<StaggeredGridAdapter.ViewHolder> implements Filterable {
    private boolean multiSelectionEnabled;
    private Context mContext;
    private SparseBooleanArray mSelectedItemsIds;
    private List<DriveDocModel> driveDocList;
    private AdapterDelegate adapterDelegate;
    //    private String[] popupItems;
    private OnPopupItemSelected onPopupItemSelected;
    private List<DriveDocModel> filteredDriveDocList;
    private NoFilesListener noFilesListener;

    public StaggeredGridAdapter(Context context, List<DriveDocModel> driveDocList, OnPopupItemSelected onPopupItemSelected) {
        mContext = context;
        this.driveDocList = driveDocList;
        mSelectedItemsIds = new SparseBooleanArray();
        this.onPopupItemSelected = onPopupItemSelected;
//        this.popupItems = popupItems;
        this.filteredDriveDocList = new ArrayList<>(driveDocList);
    }

    @NonNull
    @Override
    public StaggeredGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_view, parent, false);
        return new StaggeredGridAdapter.ViewHolder(view);
    }

    public void setAdapterDelegate(AdapterDelegate adapterDelegate) {
        this.adapterDelegate = adapterDelegate;
    }

    @Override
    public void onViewRecycled(@NonNull StaggeredGridAdapter.ViewHolder holder) {

    }

    public void notifyDataSetChangedWithFilter(String filterText, NoFilesListener noFilesListener) {
        List<DriveDocModel> driveDocModels = driveDocList;
        filteredDriveDocList.clear();
        filteredDriveDocList.addAll(driveDocModels);
        getFilter().filter(filterText);

        this.noFilesListener = noFilesListener;
    }

    @Override
    public void onBindViewHolder(@NonNull final StaggeredGridAdapter.ViewHolder holder, final int position) {

        DriveDocModel driveDocModel = filteredDriveDocList.get(position);


        try {
            Bitmap thumbnail;
            if (driveDocModel.getPdfFilePath() != null) {
                holder.imgMainImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                thumbnail = getScaledThumbnail(BitmapFactory.decodeFile(new File(driveDocModel.getImagePath()).getPath()));
            } else {
                holder.imgMainImage.setScaleType(ImageView.ScaleType.FIT_XY);
                thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(new File(driveDocModel.getImagePath()).getPath()), 200, 200);
            }

            if (driveDocModel.getImagePath() != null) {

                //todo: OOM: java.lang.OutOfMemoryError: Failed to allocate a 12384732 byte allocation with 3650088 free bytes and 3MB until OOM
                GlideApp.with(mContext)
//                    .load(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(new File(driveDocModel.getImagePath()).getPath()), 200, 200))
//                    .load(getThumbnail(driveDocModel.getImagePath()))
//                    .load(getScaledThumbnail(BitmapFactory.decodeFile(new File(driveDocModel.getImagePath()).getPath())))
                        .load(thumbnail)
                        .thumbnail(0.1f)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .error(R.drawable.ds_logo)
                        .placeholder(R.drawable.ds_logo)
//                    .centerCrop()
//                    .centerInside()
                        .into(holder.imgMainImage);
            } else {
                holder.imgMainImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ds_logo));
            }

        } catch (Exception e) {
            e.printStackTrace();
            holder.imgMainImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ds_logo));
        } catch (OutOfMemoryError outOfMemoryError) {
            outOfMemoryError.printStackTrace();
            holder.imgMainImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ds_logo));
        }

        if (driveDocModel.getFileName() != null) {
            holder.txtTitle.setText(driveDocModel.getFileName());
        } else {
            String displayString = driveDocModel.getWhichCard() + "_" + FileVersion.CROPPED.toString() + "_" + driveDocModel.getPublicGuid();
            if (driveDocModel.getJsonText() != null) {
                Gson gson = new Gson();
                CardDetail cardDetail = gson.fromJson(driveDocModel.getJsonText(), CardDetail.class);

                String displayName = cardDetail.get2WordOr15CharDisplayString();

                holder.txtTitle.setText(displayName.replace("\n", " "));

                String cardData = cardDetail.getCard_name();
                if (cardData.contains(Constants.KEY_OCR_OFF_SCAN)) {
                    holder.txtTitle.setText(displayString.replace("\n", ""));
                }

            } else {
//            String displayString = driveDocModel.getWhichCard()+"_"+ FileVersion.CROPPED.toString()+"_"+driveDocModel.getPublicGuid();
                holder.txtTitle.setText(displayString.replace("\n", ""));
            }

        }

        if (multiSelectionEnabled) {
            holder.imgCheckBox.setVisibility(View.VISIBLE);
        } else {
            holder.imgCheckBox.setVisibility(View.GONE);
        }

        //selected
        selectionUIChange(position, holder);


        holder.mView.setOnClickListener(view -> {
            if (multiSelectionEnabled) {
                toggleSelection(position, holder);
            } else {
                if (adapterDelegate != null) {
                    adapterDelegate.onClicked(view, driveDocModel, position);
                }
            }
        });

        holder.imgMore.setOnClickListener(view -> {
//                Toast.makeText(mContext, "more menu", Toast.LENGTH_SHORT).show();
            showListPopupWindow(position, driveDocModel, holder.imgMore, holder.txtTitle.getText().toString());
        });

        holder.mView.setOnLongClickListener(view -> {

            multiSelectionEnabled = true;
            selectItem(position, true, holder);
            notifyDataSetChanged();
            if (adapterDelegate != null)
                adapterDelegate.onLongClick(view, driveDocModel, position);

            return false;
        });

        holder.imgCheckBox.setOnClickListener(view -> toggleSelection(position, holder));

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString().trim();
                List<DriveDocModel> filteredFilesList = new ArrayList<>();
                List<DriveDocModel> driveDocModels = new ArrayList<>(driveDocList);

                if (charString.isEmpty()) {
                    filteredFilesList.addAll(driveDocModels);
                } else {
                    for (DriveDocModel driveDocModel1 : driveDocModels) {

                        String fileName = driveDocModel1.getFileName();
                        String displayString = driveDocModel1.getWhichCard() + "_" + FileVersion.CROPPED.toString() + "_" + driveDocModel1.getPublicGuid();

                        if (fileName != null) {
                            if (fileName.toLowerCase().contains(charString.toLowerCase())) {
                                filteredFilesList.add(driveDocModel1);
                            }
                        } else if (driveDocModel1.getJsonText() != null) {
                            CardDetail cardDetail = Globalarea.getCardDetailOfString(driveDocModel1.getJsonText());
                            String displayName = cardDetail.get2WordOr15CharDisplayString();

                            if (displayName != null && displayName.toLowerCase().contains(charString.toLowerCase())) {
                                filteredFilesList.add(driveDocModel1);
                            }

                        } else if (displayString.toLowerCase().contains(charString.toLowerCase())) {
                            filteredFilesList.add(driveDocModel1);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredFilesList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredDriveDocList = (List<DriveDocModel>) results.values;
                notifyDataSetChanged();

                if (noFilesListener != null) {
                    if (filteredDriveDocList == null || filteredDriveDocList.size() == 0) {
                        noFilesListener.onNoFilesListener(true);
                    } else
                        noFilesListener.onNoFilesListener(false);
                }

            }
        };
    }

    private void selectionUIChange(int position, ViewHolder holder) {
        if (mSelectedItemsIds.get(position)) {
            holder.imgCheckBox.setImageDrawable(mContext.getResources().getDrawable(R.drawable.checkbox_selected));
        } else {
            holder.imgCheckBox.setImageDrawable(mContext.getResources().getDrawable(R.drawable.itemselectcircle));
        }
    }

    private void showListPopupWindow(int position, DriveDocModel driveDocModel, View anchor, String fileName) {

//        UiUtil.hideKeyBoard(context, anchor);

        final ListPopupWindow listPopupWindow = new ListPopupWindow(mContext);
        listPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.spinner_item_bg));
        listPopupWindow.setModal(true);
        int width = Utility.dpToPx(mContext, 130);
//        int offset = (width / 2) + Utility.dpToPx(mContext, 20);
        listPopupWindow.setAnchorView(anchor);
//        listPopupWindow.setVerticalOffset(-offset);
        listPopupWindow.setWidth(width);

        List<FilePopupModel> filePopupModels = new ArrayList<>();
        filePopupModels.add(new FilePopupModel(mContext.getDrawable(R.drawable.pdf_47), "PDF"));
        filePopupModels.add(new FilePopupModel(mContext.getDrawable(R.drawable.share_white), "Share"));
        filePopupModels.add(new FilePopupModel(mContext.getDrawable(R.drawable.pdelete), "Delete"));
        FilesMenuAdapter filesMenuAdapter = new FilesMenuAdapter(mContext, filePopupModels);
        listPopupWindow.setAdapter(filesMenuAdapter);


        listPopupWindow.setOnItemClickListener((parent, view, pos, id) -> {
            listPopupWindow.dismiss();
            onPopupItemSelected.onPopupItemSelected(position, driveDocModel, pos, fileName);
        });
        listPopupWindow.show();
    }

    @Override
    public int getItemCount() {

        if (filteredDriveDocList != null) {
            return filteredDriveDocList.size();
        } else {
            return 0;
        }

    }

    /***
     * Methods required for do selections, remove selections, etc.
     */

    //Toggle selection methods
    public void toggleSelection(int position, ViewHolder holder) {
        selectItem(position, !mSelectedItemsIds.get(position), holder);
    }

    public void selectItem(int position, boolean value, ViewHolder holder) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        selectionUIChange(position, holder);
    }

    //Add selection to all Items
    public void selectAllItem() {
        mSelectedItemsIds.clear();
        for (int i = 0; i < driveDocList.size(); i++) {
            mSelectedItemsIds.put(i, true);
        }
        notifyDataSetChanged();
    }

    public void unselectAllItem() {
        mSelectedItemsIds.clear();
        multiSelectionEnabled = false;
        notifyDataSetChanged();
    }

    public boolean isSelectionEnabled() {
        return this.multiSelectionEnabled;
    }

    public void setSelectionEnabled(boolean multiSelectionEnabled) {
        this.multiSelectionEnabled = multiSelectionEnabled;
        notifyDataSetChanged();
    }

    public List<DriveDocModel> getAll() {
        return filteredDriveDocList;
    }

    public void addAll(ArrayList<DriveDocModel> productList) {

        if (driveDocList == null) {
            driveDocList = new ArrayList<>();
        } else {
            driveDocList.clear();
        }

        driveDocList.addAll(productList);

        if (mSelectedItemsIds == null) {
            mSelectedItemsIds = new SparseBooleanArray();
        } else {
            mSelectedItemsIds.clear();
        }

    }

//    //Put or delete selected position into SparseBooleanArray
//    public void setActionMode(boolean isActionMode) {
//        this.isActionMode = isActionMode;
//        notifyDataSetChanged();
//    }

    //Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public ArrayList<DriveDocModel> getSelectedItems() {

        Log.e("getting", "items");
        ArrayList<DriveDocModel> driveDocModels = new ArrayList<>();

        Log.e("selected item size", String.valueOf(mSelectedItemsIds.size()));

        for (int i = 0; i < mSelectedItemsIds.size(); i++) {

            int key = mSelectedItemsIds.keyAt(i);
            // get the object by the key.

            Log.e("i", String.valueOf(i));
            if (mSelectedItemsIds.get(key)) {
                driveDocModels.add(this.driveDocList.get(key));
            }
//                        && this.driveDocList.size() > i
//            if (mSelectedItemsIds.get(i)) {
//                Log.e("selected item id","true");
//                driveDocModels.add(this.driveDocList.get(mSelectedItemsIds.keyAt(i)));
//            }
            else {
                Log.e("why ", "it is going into else");
            }

        }

        return driveDocModels;

    }

    //Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public Bitmap getScaledThumbnail(Bitmap imageBitmap) {
        int THUMBNAIL_HEIGHT = 200;
        int THUMBNAIL_WIDTH = 200;

        float width = (float) imageBitmap.getWidth();
        float height = (float) imageBitmap.getHeight();
        float ratio = width / height;

        float newHeight = (float) THUMBNAIL_WIDTH / ratio;
        float newWidth = (float) THUMBNAIL_HEIGHT * ratio;
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int) THUMBNAIL_WIDTH, (int) newHeight, false);

//        int padding = (THUMBNAIL_WIDTH - imageBitmap.getWidth())/2;
//        imageView.setPadding(padding, 0, padding, 0);
//        imageView.setImageBitmap(imageBitmap);
        return imageBitmap;
    }

    public Bitmap getThumbnail(String imagePath) {
        File file = new File(imagePath); // the image file

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true; // obtain the size of the image, without loading it in memory
        BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions);

        // find the best scaling factor for the desired dimensions
        int desiredWidth = 200;
        int desiredHeight = 200;
        float widthScale = (float) bitmapOptions.outWidth / desiredWidth;
        float heightScale = (float) bitmapOptions.outHeight / desiredHeight;
        float scale = Math.min(widthScale, heightScale);

        int sampleSize = 1;
        while (sampleSize < scale) {
            sampleSize *= 2;
        }
        bitmapOptions.inSampleSize = sampleSize; // this value must be a power of 2,
        // this is why you can not have an image scaled as you would like
        bitmapOptions.inJustDecodeBounds = false; // now we want to load the image

        // Let's load just the part of the image necessary for creating the thumbnail, not the whole image

        return BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions);
    }

    public interface OnPopupItemSelected {
        void onPopupItemSelected(int position, DriveDocModel driveDocModel, int id, String fileName);
    }

    public interface NoFilesListener {
        void onNoFilesListener(boolean areZeroFiles);
    }

    public interface notifySelectionListener {
        void notifySelectedItems(List<DriveDocModel> driveDocModels);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imgMainImage;
        public final ImageView imgCheckBox;
        public final ImageView imgMore;
        public final TextView txtTitle;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imgMainImage = view.findViewById(R.id.img_item_doc);
            imgCheckBox = view.findViewById(R.id.imgCheckBox);
            txtTitle = view.findViewById(R.id.tv_item_doc_name);
            imgMore = view.findViewById(R.id.img_more);
        }
    }
}