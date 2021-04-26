package com.pancard.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.docscan.android.R;
import com.pancard.android.model.Category;

import java.util.List;

public class GridAdapter extends BaseAdapter {

    List<Category> categoryList;
    //    private final String[] menu;
//    private final int[] Imageid;
    LayoutInflater inflater;
    private Context mContext;

    public GridAdapter(Context context, List<Category> categoryList) {
        mContext = context;
        this.categoryList = categoryList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Object getItem(int i) {
        return categoryList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view == null) {
            Category category = categoryList.get(position);

            grid = new View(mContext);

            grid = inflater.inflate(R.layout.category_item, null);

//            LinearLayout gridLinearLayout = grid.findViewById(R.id.layout_item);
            LinearLayout gridLinearLayout = getLinearGrid(category, grid);


            if (gridLinearLayout != null) {
                grid = gridLinearLayout;
            }
            if (position == 6) {
                grid.setVisibility(View.GONE);
            }

        } else {
            grid = view;
        }

        return grid;
    }

    private LinearLayout getLinearGrid(Category category, View view) {
        ImageView imageView = null;
        TextView textView = new TextView(mContext);
        LinearLayout newLinearGrid = new LinearLayout(mContext);
//        LinearLayout newLinearGrid = view.findViewById(R.id.layout_item);

        if (newLinearGrid != null && category != null) {
            Log.i("islayout get", "yes");
            imageView = (ImageView) newLinearGrid.getChildAt(0);
            textView = (TextView) newLinearGrid.getChildAt(1);

            textView.setText(category.getName());
            imageView.setImageDrawable(category.getDrawableBackground());

//            GlideApp.with(mContext)
//                    .load(category.getDrawableBackground())
//                    .placeholder(R.drawable.ds_logo)
//                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
//                    .into(imageView);
        }
        return newLinearGrid;
    }
}
