package com.pancard.android.newflow.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.docscan.android.R;
import com.pancard.android.DatabaseHandler;
import com.pancard.android.Scanner;
import com.pancard.android.adapter.CategoryListAdapter;
import com.pancard.android.model.Category;
import com.pancard.android.newflow.activity.DocumentsListActivity;
import com.pancard.android.utility.PreferenceManagement;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListDocTypeFragment extends Fragment {

    //    private Context context;
    //    Toolbar toolbar;
    private RecyclerView rvCategoryList;
    //    private GridView gridView;
    private List<Category> categoryList;
    private PreferenceManagement preferences;

    public ListDocTypeFragment() {

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.doctypes_fragment, container, false);

        preferences = Scanner.getInstance().getPreferences();

//        gridView = view.findViewById(R.id.grid_docs);

        categoryList = getCategoryList();

        bindView(view);


        CategoryListAdapter categoryListAdapter = new CategoryListAdapter(getActivity(), categoryList);
        rvCategoryList.setAdapter(categoryListAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false);
        rvCategoryList.setLayoutManager(gridLayoutManager);

//        AutoFitGridLayoutManager autoFitGridLayoutManager = new AutoFitGridLayoutManager(context,400);
//        rvCategoryList.setLayoutManager(autoFitGridLayoutManager);

//        GridAdapter adapter = new GridAdapter(getActivity(), categoryList);
//        gridView.setAdapter(adapter);
        return view;
    }

    private void bindView(View currentView) {

        rvCategoryList = currentView.findViewById(R.id.recyclerView);

//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.i("clicked item", Integer.toString(position));
//
////                Intent intent = new Intent(getActivity(),DocumentsListActivity.class);
////                context.startActivity(intent);
//
//                if(position != 6) {
//                    switch (position)
//                    {
//                        case 0:
//                            startActivity(Constants.licence);
//                            break;
//                        case 1:
//                            startActivity(Constants.adharcard);
//                            break;
//                        case 2:
//                            startActivity(Constants.creditCardcard);
//                            break;
//                        case 3:
//                            startActivity(Constants.pancard);
//                            break;
//                        case 4:
//                            startActivity(Constants.businesscard);
//                            break;
//                        case 5:
//                            startActivity(Constants.passport);
//                            break;
//                        case 7:
//                            startActivity(Constants.document);
//                            break;
//                    }
//                }
//
//
//            }
//        });
//        toolbar = getActivity().findViewById(R.id.toolbar);
    }

    private ArrayList<Category> getCategoryList() {

        if (getActivity() != null) {
            ArrayList<Category> categories = new ArrayList<>();

            categories.add(new Category("Licence", ContextCompat.getDrawable(getActivity(), R.drawable.card_license_2), ContextCompat.getDrawable(getActivity(), R.drawable.drawable_category_license)));
            categories.add(new Category("Aadhar Card", ContextCompat.getDrawable(getActivity(), R.drawable.card_aadhar3), ContextCompat.getDrawable(getActivity(), R.drawable.drawable_category_aadhar)));
            categories.add(new Category("Credit Card", ContextCompat.getDrawable(getActivity(), R.drawable.card_credit), ContextCompat.getDrawable(getActivity(), R.drawable.drawable_category_pancard)));
            categories.add(new Category("Pancard", ContextCompat.getDrawable(getActivity(), R.drawable.card_pan_2), ContextCompat.getDrawable(getActivity(), R.drawable.drawable_category_pancard)));
            categories.add(new Category("Business Card", ContextCompat.getDrawable(getActivity(), R.drawable.card_business), ContextCompat.getDrawable(getActivity(), R.drawable.drawable_category_aadhar)));
            categories.add(new Category("Passport", ContextCompat.getDrawable(getActivity(), R.drawable.card_passport), ContextCompat.getDrawable(getActivity(), R.drawable.drawable_category_pancard)));
            categories.add(new Category("Document", ContextCompat.getDrawable(getActivity(), R.drawable.card_document_2), ContextCompat.getDrawable(getActivity(), R.drawable.drawable_category_license)));


            return categories;
        } else
            return null;
    }

    private void startActivity(String tag) {
        DatabaseHandler handler = new DatabaseHandler(getActivity());
        if (handler.GetAllTableData(tag).size() > 0) {
//            if (checkLock()) {
//                callLockScreen(tag);
//            } else {
            Intent intent = new Intent(getActivity(), DocumentsListActivity.class);
            intent.putExtra("TAG_CAMERA", tag);
            startActivity(intent);
//                finish();
//            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_no_card), Toast.LENGTH_LONG).show();
        }
    }

//    private boolean checkLock() {
//        if (preferences.getPin() != null && preferences.getPin().trim().length() == 4) {
//            preferences.setFirstOpen("unopen");
//            return true;
//        }
//        return false;
//    }

//    private void callLockScreen(String tag) {
//        Intent intent = new Intent(getActivity(), AppLockScrenn.class);
//        intent.putExtra(Constants.ActivityName, tag);
//        startActivity(intent);
////        finish();
//
//    }
}
