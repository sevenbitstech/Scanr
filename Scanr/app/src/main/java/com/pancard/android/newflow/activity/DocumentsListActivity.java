package com.pancard.android.newflow.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.docscan.android.R;
import com.pancard.android.DatabaseHandler;
import com.pancard.android.Globalarea;
import com.pancard.android.activity.scanactivity.SpecificPage;
import com.pancard.android.model.SqliteDetail;
import com.pancard.android.utility.GlideApp;
import com.pancard.android.utility.PermissionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DocumentsListActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_CAMERA = 1;
    public static TextView tv_no_vlaue;
    TextView tvTitle;
    ArrayList<SqliteDetail> documents_list;
    ArrayList<SqliteDetail> deleteItem;
    //    RetriveandSetOneSection retriveandSetOneSection;
    String searchString;
    ProgressDialog dialog;
    String whichcard;
    AlertDialog alertDialog;
    SearchView searchView;
    DatabaseHandler handler;
    //    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            new FirebaseManagement(context).init(context);
//        }
//    };
    PermissionManager permissionManager;
    String[] permissions = {Manifest.permission.CAMERA};
    private LayoutInflater mInflater;
    private CardView mListView;
    private DocumentsListActivity.DocumentAdapter mAdapter;
    private RecyclerView recyclerViewDocs;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        permissionManager = new PermissionManager(this);
        mInflater = LayoutInflater.from(DocumentsListActivity.this);
        setContentView(R.layout.activity_main2);
        bindViews();
    }

    private void bindViews() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        handler = new DatabaseHandler(DocumentsListActivity.this);
        StrictMode.setThreadPolicy(policy);
        Globalarea.Card_type_activity = "Document";
        documents_list = new ArrayList<>();
        deleteItem = new ArrayList<>();
        recyclerViewDocs = findViewById(R.id.my_recycler_view);
        recyclerViewDocs.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewDocs.setLayoutManager(layoutManager);
        recyclerViewDocs.setItemAnimator(new DefaultItemAnimator());

//        retriveandSetOneSection = new RetriveandSetOneSection(this);

        dialog = new ProgressDialog(this);
        tvTitle = findViewById(R.id.tv_title);
        tv_no_vlaue = findViewById(R.id.no_value);
        mListView = findViewById(android.R.id.list);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String data = extras.getString("TAG_CAMERA");
            if (data != null) {
                whichcard = data;
                displayListItem();
            }
        }
//        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET"));
        registerForContextMenu(recyclerViewDocs);
    }

    private void displayListItem() {
        //  documents_list.clear();
//        documents_list = handler.getAllBusinessCard();

        documents_list = handler.GetAllTableData(whichcard);

        String strTitle = whichcard + " (" + documents_list.size() + ")";
        tvTitle.setText(strTitle);

        if (documents_list.size() >= 1) {
            Log.e("call loop 1: ", "1");
            mAdapter = new DocumentsListActivity.DocumentAdapter(documents_list);
            recyclerViewDocs.setAdapter(mAdapter);
            Collections.sort(documents_list, new Comparator<SqliteDetail>() {
                @Override
                public int compare(SqliteDetail lhs, SqliteDetail rhs) {
                    char lhsFirstLetter = TextUtils.isEmpty(lhs.getCard_name()) ? ' ' : lhs.getCard_name().charAt(0);
                    char rhsFirstLetter = TextUtils.isEmpty(rhs.getCard_name()) ? ' ' : rhs.getCard_name().charAt(0);
                    int firstLetterComparison = Character.toUpperCase(lhsFirstLetter) - Character.toUpperCase(rhsFirstLetter);
                    if (firstLetterComparison == 0)
                        return lhs.getCard_name().compareTo(rhs.getCard_name());
                    return firstLetterComparison;
                }
            });

//            mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.dark_theme));

//            mListView.setPinnedHeaderView(mInflater.inflate(R.layout.pinned_header_listview_side_header, mListView, false));
//            mListView.setAdapter(mAdapter);
//            mListView.setOnScrollListener(mAdapter);
//            mListView.setEnableHeaderTransparencyChanges(false);
        } else {
            Log.e("call loop 1: ", "2");

            tv_no_vlaue.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Globalarea.isInternetOn()) {
//            if (Globalarea.actionFire) {
//                Globalarea.actionFire = false;
//                new FirebaseManagement(this).init(this);
//            }
//        }
    }

    private class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.MyViewHolder> {

        private ArrayList<SqliteDetail> dataSet;

        public DocumentAdapter(ArrayList<SqliteDetail> data) {
            this.dataSet = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_item3, parent, false);

//            view.setOnClickListener(DocumentsListActivity.myOnClickListener);

            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

            TextView textViewDocName = holder.textViewDocName;
            TextView textViewTimespan = holder.textViewTimespan;
            ImageView imageViewDocIcon = holder.imageViewDocIcon;
            ImageView viewDoc = holder.imageViewDoc;
            RelativeLayout linearLayoutMain = holder.linearLayoutMain;
            boolean checked = false;

            textViewDocName.setText(dataSet.get(listPosition).getCard_name());
            textViewTimespan.setText(dataSet.get(listPosition).getScan_time());

            Bitmap bmp = BitmapFactory.decodeFile(dataSet.get(listPosition).getImage_url());
            GlideApp.with(getApplicationContext())
                    .load(bmp)
                    .placeholder(R.drawable.ds_logo)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(imageViewDocIcon);

            viewDoc.setOnClickListener(v -> {
                Globalarea.SpecificCard = handler.getSingleRow(whichcard, documents_list.get(listPosition).getScan_time());

                Intent intent = new Intent(DocumentsListActivity.this, SpecificPage.class);
                intent.putExtra("TAG_CAMERA", whichcard);
                intent.putExtra("WhictActivity", "ListActivity");
                startActivity(intent);
                finish();
            });

            linearLayoutMain.setOnLongClickListener(v -> {
                imageViewDocIcon.setImageDrawable(getResources().getDrawable(R.drawable.right));
                return true;
            });


        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

            TextView textViewDocName;
            TextView textViewTimespan;
            ImageView imageViewDocIcon;
            ImageView imageViewDoc;
            RelativeLayout linearLayoutMain;

            public MyViewHolder(View itemView) {
                super(itemView);
                this.textViewDocName = itemView.findViewById(R.id.listview_item_Pancardname);
                this.textViewTimespan = itemView.findViewById(R.id.timestamp);
                this.imageViewDocIcon = itemView.findViewById(R.id.listview_item_ImageView);
                this.imageViewDoc = itemView.findViewById(R.id.imageDocView);
                this.linearLayoutMain = itemView.findViewById(R.id.main_liner);
                itemView.setOnCreateContextMenuListener(this);
            }


            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
            }
        }

    }
}
