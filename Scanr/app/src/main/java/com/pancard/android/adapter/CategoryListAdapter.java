package com.pancard.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.docscan.android.R;
import com.pancard.android.DatabaseHandler;
import com.pancard.android.Globalarea;
import com.pancard.android.model.Category;
import com.pancard.android.newflow.activity.DocumentsListActivity;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.GlideApp;

import java.util.List;


public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.CategoryViewHolder> {

    private List<Category> mValues;
    private Context mContext;

    public CategoryListAdapter(Context context, List<Category> values) {

        mValues = values;
        mContext = context;

    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.category_item, parent, false);

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.setData(mValues.get(position), position);

    }

    private View.OnClickListener setOnCategoryClickListerner() {
        return new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Log.i("item click", "yes");
            }
        };
    }

    @Override
    public int getItemCount() {

        return mValues.size();
    }

    private void startActivity(String tag) {
        DatabaseHandler handler = new DatabaseHandler(mContext);
        if (handler.GetAllTableData(tag).size() > 0) {
//            if (checkLock()) {
//                callLockScreen(tag);
//            } else {
            Intent intent = new Intent(mContext, DocumentsListActivity.class);
            intent.putExtra("TAG_CAMERA", tag);
            mContext.startActivity(intent);
//                finish();
//            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.error_no_card), Toast.LENGTH_LONG).show();
        }
    }


    public interface DocumentCategoryClickListerner {
        void categoryOnClick(int categoryId);

    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Category item;
        private TextView textView;
        private ImageView imageView;
        private LinearLayout llDocCategory;
        private CardView cvCategoryDoc;
        private DocumentCategoryClickListerner mListener;

        private CategoryViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.grid_item_letter);
            imageView = v.findViewById(R.id.grid_item_label);
            llDocCategory = v.findViewById(R.id.ll_categoryitem);
            cvCategoryDoc = v.findViewById(R.id.cardViewCategoryDoc);
            v.setOnClickListener(this);
//            cvCategoryDoc.setOnClickListener(setOnCategoryClickListerner());
        }

        public void setData(final Category item, int position) {
            this.item = item;

            textView.setText(item.getName());
            GlideApp.with(mContext)
                    .load(item.getDrawableBackground())
                    .placeholder(R.drawable.ds_logo)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(imageView);


            llDocCategory.setBackground(item.getBackgroundLL());
            int screenhHeightDp = Globalarea.pxToDp(mContext, Globalarea.getSize(mContext));
            cvCategoryDoc.getLayoutParams().height = Globalarea.dpToPx(mContext, (screenhHeightDp - 100) / 5);
            Log.i("Dp Heigh", Integer.toString(cvCategoryDoc.getLayoutParams().height));
            int paddingLow = Globalarea.dpToPx(mContext, 10);
            int paddingHigh = Globalarea.dpToPx(mContext, 20);
            CardView.MarginLayoutParams cardViewMargin = (CardView.MarginLayoutParams) cvCategoryDoc.getLayoutParams();

            llDocCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("item click", item.getName());

                    switch (item.getName()) {
                        case "Licence":
                            startActivity(Constants.licence);
                            break;
                        case "Aadhar Card":
                            startActivity(Constants.adharcard);
                            break;
                        case "Credit Card":
                            startActivity(Constants.creditCard);
                            break;
                        case "Pancard":
                            startActivity(Constants.pancard);
                            break;
                        case "Business Card":
                            startActivity(Constants.businesscard);
                            break;
                        case "Passport":
                            startActivity(Constants.passport);
                            break;
                        case "Document":
                            startActivity(Constants.document);
                            break;
                    }
                }
            });

            if (position % 2 == 0) {

                cardViewMargin.setMargins(paddingHigh, paddingLow, paddingLow, paddingLow);
            } else {
                cardViewMargin.setMargins(paddingLow, paddingLow, paddingHigh, paddingLow);
            }

        }

        @Override
        public void onClick(View v) {

        }
    }
//    private boolean checkLock() {
//
//
//        if (Scanner.getInstance().getPreferences().getPin() != null) {
//            if (Scanner.getInstance().getPreferences().getPin().trim().length() == 4) {
//                Scanner.getInstance().getPreferences().setFirstOpen("unopen");
//                return true;
//            }
//            return false;
//        }
//        return false;
//    }
//    private void callLockScreen(String tag) {
//        Intent intent = new Intent(mContext, AppLockScrenn.class);
//        intent.putExtra(Constants.ActivityName, tag);
//        mContext.startActivity(intent);
////        finish();
//
//    }

}