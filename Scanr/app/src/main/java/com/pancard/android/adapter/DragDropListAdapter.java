/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pancard.android.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.docscan.android.R;
import com.google.gson.Gson;
import com.pancard.android.DriveOperations.DriveDocModel;
import com.pancard.android.database.FileVersion;
import com.pancard.android.dragdrophelper.ItemTouchHelperAdapter;
import com.pancard.android.dragdrophelper.ItemTouchHelperViewHolder;
import com.pancard.android.dragdrophelper.OnStartDragListener;
import com.pancard.android.model.CardDetail;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.GlideApp;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Simple RecyclerView.Adapter that implements {@link ItemTouchHelperAdapter} to respond to move and
 * dismiss events from a {@link androidx.recyclerview.widget.ItemTouchHelper}.
 *
 * @author Paul Burke (ipaulpro)
 */
public class DragDropListAdapter extends RecyclerView.Adapter<DragDropListAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

    public final List<DriveDocModel> driveDocModels;
    private final OnStartDragListener mDragStartListener;
    private Context context;
//    private ItemViewHolder holder;

    public DragDropListAdapter(Context context, List<DriveDocModel> driveDocModelList, OnStartDragListener dragStartListener) {
        this.context = context;
        this.mDragStartListener = dragStartListener;
        this.driveDocModels = driveDocModelList;
//        mItems.addAll(Arrays.asList(context.getResources().getStringArray(R.array.dummy_items)));
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new ItemViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
//        holder.textView.setText(driveDocModels.get(position).getFolderName());

        DriveDocModel driveDocModel = driveDocModels.get(position);

        String positionNumber = String.valueOf(position + 1);
        String fileName;

        if (driveDocModel.getFileName() != null) {
            fileName = driveDocModel.getFileName();
        } else {
            String displayString = driveDocModel.getWhichCard() + "_" + FileVersion.CROPPED.toString() + "_" + driveDocModel.getPublicGuid();
            if (driveDocModel.getJsonText() != null) {
                Gson gson = new Gson();
                CardDetail cardDetail = gson.fromJson(driveDocModel.getJsonText(), CardDetail.class);

                String displayName = cardDetail.get2WordOr15CharDisplayString();

                fileName = displayName.replace("\n", " ");

                String cardData = cardDetail.getCard_name();
                if (cardData.contains(Constants.KEY_OCR_OFF_SCAN)) {
                    fileName = displayString.replace("\n", "");
                }

            } else {
                fileName = displayString.replace("\n", "");
            }
        }

        String positionAndName = positionNumber + ". " + fileName;
        holder.imagePositionNumber.setText(positionAndName);

        if (driveDocModel.getImagePath() != null) {
            try {
                Bitmap bitmapFile = BitmapFactory.decodeFile(new File(driveDocModel.getImagePath()).getPath());
                if (bitmapFile != null) {
                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(bitmapFile, bitmapFile.getWidth(), bitmapFile.getHeight());

                    if (bitmap.getWidth() < holder.cardView.getWidth()) {
                        Log.e("true for", fileName);
                    } else {
                        Log.e("no less than card width", fileName);
                    }


                    GlideApp.with(context)
//                    .load(bitmapFile)
                            .load(bitmap)
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(holder.handleView);
                } else {
                    Log.e("bitmap is", "null");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError outOfMemoryError) {
                outOfMemoryError.printStackTrace();
            }

        }

        // Start a drag whenever the handle view it touched
        holder.draggableLayout.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mDragStartListener.onStartDrag(holder);
            }
            Log.e("Selected item", String.valueOf(position));
            return false;
        });
    }

    @Override
    public void onItemDismiss(int position) {
        driveDocModels.remove(position);
        notifyItemRemoved(position);
//        notifyItemChanged(position,1);
//        notifyDataSetChanged();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(driveDocModels, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);

        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return driveDocModels.size();
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        //        public final TextView textView;
        final ImageView handleView;
        final TextView imagePositionNumber;
        CardView cardView;
        ConstraintLayout draggableLayout;

        ItemViewHolder(View itemView) {
            super(itemView);
//            textView = (TextView) itemView.findViewById(R.id.text);
            handleView = itemView.findViewById(R.id.img_drag);
            cardView = itemView.findViewById(R.id.cardview);
            draggableLayout = itemView.findViewById(R.id.draggable_layout);
            imagePositionNumber = itemView.findViewById(R.id.image_position);
        }

        @Override
        public void onItemSelected() {
//            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
