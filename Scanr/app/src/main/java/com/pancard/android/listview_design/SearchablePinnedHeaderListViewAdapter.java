package com.pancard.android.listview_design;

import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;

import com.pancard.android.Globalarea;

import java.util.ArrayList;


/**
 * This gives the ability of searching in a pinnedHead list view
 */
public abstract class SearchablePinnedHeaderListViewAdapter<T> extends IndexedPinnedHeaderListViewAdapter implements
        Filterable {
    private final Filter mFilter;
    private ArrayList<T> mFilterListCopy;
    private OnNoValues onNoValues;

    public SearchablePinnedHeaderListViewAdapter() {
        mFilter = new Filter() {
            CharSequence lastConstraint = null;

            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                if (constraint == null || constraint.length() == 0)
                    return null;
                final ArrayList<T> newFilterArray = new ArrayList<T>();
                final FilterResults results = new FilterResults();
                for (final T item : getOriginalList())
                    if (doFilter(item, constraint))
                        newFilterArray.add(item);
                results.values = newFilterArray;
                results.count = newFilterArray.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(final CharSequence constraint, final FilterResults results) {
                mFilterListCopy = results == null ? null : (ArrayList<T>) results.values;
                final boolean needRefresh = !TextUtils.equals(constraint, lastConstraint);
                lastConstraint = constraint;

                if (Globalarea.Card_type_activity.equals("Document")) {

                    if (mFilterListCopy != null) {

                        if (onNoValues != null) {
                            onNoValues.onNoValues(mFilterListCopy.size());
                        }
                    }

                    if (constraint == null || constraint.length() == 0) {
                        if (onNoValues != null) {
                            onNoValues.onNoValues(getOriginalList().size());
                        }

                    }
                }

                if (needRefresh) {
                    notifyDataSetChanged();
                }
            }
        };
    }

    protected void setNoValuesCallback(OnNoValues onNoValues) {
        this.onNoValues = onNoValues;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    /**
     * returns true iff the item can "pass" the filtering process and should be shown
     */
    public abstract boolean doFilter(T item, CharSequence constraint);

    public abstract ArrayList<T> getOriginalList();

    @Override
    public T getItem(final int position) {
        if (position < 0)
            return null;
        final ArrayList<T> listCopy = getFilterListCopy();
        if (listCopy != null) {
            if (position < listCopy.size())
                return listCopy.get(position);
            else
                return null;
        } else {
            final ArrayList<T> originalList = getOriginalList();
            if (position < originalList.size())
                return originalList.get(position);
            else
                return null;
        }
    }

    @Override
    public int getCount() {
        final ArrayList<T> listCopy = getFilterListCopy();
        if (listCopy != null)
            return listCopy.size();
        else
            return getOriginalList().size();
    }

    private ArrayList<T> getFilterListCopy() {
        return mFilterListCopy;
    }

    public interface OnNoValues {
        void onNoValues(int size);
    }

}
