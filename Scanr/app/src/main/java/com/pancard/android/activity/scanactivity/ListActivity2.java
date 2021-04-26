package com.pancard.android.activity.scanactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.docscan.android.R;
import com.pancard.android.DatabaseHandler;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.otheracivity.CommonScan;
import com.pancard.android.activity.otheracivity.DocumentScan;
import com.pancard.android.activity.otheracivity.HomeActivity;
import com.pancard.android.activity.otheracivity.QRCodeScanner;
import com.pancard.android.listener.TaskListener;
import com.pancard.android.listview_design.PinnedHeaderListView;
import com.pancard.android.listview_design.SearchablePinnedHeaderListViewAdapter;
import com.pancard.android.listview_design.StringArrayAlphabetIndexer;
import com.pancard.android.model.CardDetail;
import com.pancard.android.model.SqliteDetail;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PermissionManager;
import com.pancard.android.utility.PreferenceManagement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ListActivity2 extends AppCompatActivity implements TaskListener {
    private final static int REQUEST_CODE_CAMERA = 1;
    public TextView tv_no_vlaue;
    ActionBar actionBar;
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
    private PinnedHeaderListView mListView;
    private DocumentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionManager = new PermissionManager(this);
        mInflater = LayoutInflater.from(ListActivity2.this);
        setContentView(R.layout.activity_main2);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        handler = new DatabaseHandler(ListActivity2.this);
        StrictMode.setThreadPolicy(policy);
        Globalarea.Card_type_activity = "Document";
        documents_list = new ArrayList<>();
        deleteItem = new ArrayList<>();

//        retriveandSetOneSection = new RetriveandSetOneSection(this);
        dialog = new ProgressDialog(this);
        actionBar = getSupportActionBar();
        tv_no_vlaue = findViewById(R.id.no_value);
        mListView = findViewById(android.R.id.list);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //               Globalarea.positionBitmap = newlist.getBitmap();
                Globalarea.SpecificCard = handler.getSingleRow(whichcard, mAdapter.getItem(position).getScan_time());

                Intent intent = new Intent(ListActivity2.this, SpecificPage.class);
                intent.putExtra("TAG_CAMERA", whichcard);
                intent.putExtra("WhictActivity", "ListActivity2");
                startActivity(intent);
                finish();
            }
        });

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                mode.setTitle(mListView.getCheckedItemCount() + " items selected");

                if (checked) {
                    deleteItem.add(mAdapter.getItem(position));
                } else {
                    deleteItem.remove(mAdapter.getItem(position));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_all: {
                        dialog_open(mode);
                        return true;
                    }
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

                deleteItem.clear();
            }
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String data = extras.getString("TAG_CAMERA");
            if (data != null) {
                whichcard = data;
                displayListItem();
            }
        }
//        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET"));
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

    public void dialog_open(final ActionMode mode) {
        if (searchView != null) {
            if (!searchView.isIconified()) {
                searchView.setIconified(true);
            }
        }
        new AlertDialog.Builder(ListActivity2.this)
                .setMessage("Are you sure you want to Delete " + deleteItem.size() + " records?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (deleteItem.size() > 0) {
                                    Globalarea.actionFire = true;

                                    for (int i = 0; i < deleteItem.size(); i++) {

                                        PreferenceManagement preferences = Scanner.getInstance().getPreferences();
                                        preferences.setSizeDetail(preferences.getSizeDetail() - deleteItem.get(i).getImage_size());

//                                            SqliteDetail sqliteDetail = handler.getSingleRow(whichcard,)
                                        if (deleteItem.get(i).getStatus().equals("true") || deleteItem.get(i).getStatus().equals("update")) {
                                            handler.sqliteInsertData(new CardDetail(whichcard, deleteItem.get(i).getScan_time(), deleteItem.get(i).getImage_size()), Constants.delete, null);
                                        }
                                        File file = new File(deleteItem.get(i).getImage_url());
                                        if (file.exists()) {
                                            file.delete();
                                        }
                                        handler.deletRowData(whichcard, deleteItem.get(i).getScan_time());
//                                        retriveandSetOneSection.deletemultiplerow(deleteItem.get(i),whichcard,deletesize.get(i));
                                    }
                                    deleteItem.clear();
                                    callRetrivedatabase(whichcard);
                                    mode.finish();


                                }
                            }
                        }).setNegativeButton("No", null).show();
    }

    private void callRetrivedatabase(String tag) {
//        if (Globalarea.isInternetOn()) {
//            if (Globalarea.firebaseUser != null) {
//                Globalarea.firebaseUser.getUid();
//                Globalarea.actionFire = false;
//
//                new FirebaseManagement(this).deleteDocument(this);
//            } else {
//                Globalarea.actionFire = true;
//            }
//        } else {
//            Globalarea.actionFire = true;
//        }
//        dialog.setMessage("Please Wait...");
//        dialog.setCancelable(false);
        displayListItem();
//        dialog.show();
//        handler.GetAllTableData(whichcard);
//        retriveandSetOneSection.getCardDetailList(tag);
    }

    private void displayListItem() {
        //  documents_list.clear();
//        documents_list = handler.getAllBusinessCard();

        documents_list = handler.GetAllTableData(whichcard);
        actionBar.setTitle(whichcard + " (" + documents_list.size() + ")");

        if (documents_list.size() >= 1) {
            Log.e("call loop 1: ", "1");
            mAdapter = new DocumentAdapter(documents_list, size -> {
                if (size > 0) {
                    tv_no_vlaue.setVisibility(View.GONE);
                } else {
                    tv_no_vlaue.setVisibility(View.VISIBLE);
                }
            });
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

            mAdapter.setPinnedHeaderTextColor(getResources().getColor(R.color.dark_theme));
            mListView.setPinnedHeaderView(mInflater.inflate(R.layout.pinned_header_listview_side_header, mListView, false));
            mListView.setAdapter(mAdapter);
            mListView.setOnScrollListener(mAdapter);
            mListView.setEnableHeaderTransparencyChanges(false);
        } else {
            Log.e("call loop 1: ", "2");

            tv_no_vlaue.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        MenuItem search = menu.findItem(R.id.menuItem_search);
        SearchView searchView = (SearchView) search.getActionView();

//        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menuItem_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                searchString = newText;
                performSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_Camera:
                cameraopen();
                return true;
            case R.id.menuItem_Refresh:
                if (searchView != null) {
                    if (searchView.isIconified()) {
                        displayListItem();
                    }
                } else {
                    displayListItem();

                }
                return true;
        }
        return true;
    }

    public void performSearch(final String queryText) {
        if (mAdapter != null) {
            mAdapter.getFilter().filter(queryText);
            mAdapter.setHeaderViewVisible(TextUtils.isEmpty(queryText));
        }
    }

    @Override
    public void onTaskFinished(String Token) {
        try {
            dialog.dismiss();
            if (Token != null) {
                if (Token.equals("GetAllData")) {
                    displayListItem();
                    deleteItem.clear();

                } else if (Token.equals("NoDataAvailable")) {
                    Globalarea.cardDetail.clear();
                    displayListItem();
                } else {

                    new AlertDialog.Builder(ListActivity2.this)
                            .setMessage(Token)
                            .setTitle("Error")
                            .setCancelable(false).setNegativeButton("Finish", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ListActivity2.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).show();
                }
            } else {

                new AlertDialog.Builder(ListActivity2.this)
                        .setMessage("Oops Something went wrong..")
                        .setTitle("Error")
                        .setCancelable(false).setNegativeButton("Finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ListActivity2.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).show();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onTaskFinished(CardDetail Token) {

    }

    @Override
    public void onTaskError(String Token) {
        try {
            dialog.dismiss();
            if (Token.equals(Constants.UploadError)) {
                dialog_open(getResources().getString(R.string.UploadError));
            } else if (Token.equals(Constants.InternetConnectionFail)) {
                dialog_open(getResources().getString(R.string.internetConnectionFail));
            } else if (Token.equals(Constants.SomethingWentWrong)) {
                dialog_open(getResources().getString(R.string.SomethingWentWrong));
            }
        } catch (Exception e) {

        }
    }

    public void dialog_open(String message) {
        new android.app.AlertDialog.Builder(ListActivity2.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                System.out.println(" User is Agree to Delete the File ");
                                Intent intent = new Intent(ListActivity2.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).show();
    }

    @Override
    public void onTaskError(String Token, String errormessage) {

    }

    @Override
    public void onTaskFinished(String Token, String taskResponse) {

    }


    // Adpter class

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(ListActivity2.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void cameraopen() {
        if (whichcard.equals(Constants.businesscard)) {
            activityStart(whichcard);
        } else if (whichcard.equals(Constants.document)) {
            if (permissionManager.hasPermissions(permissions)) {
                openDocumentScan();
            } else {
                permissionManager.requestPermissions(permissions, REQUEST_CODE_CAMERA);
            }
        } else if (whichcard.equals(Constants.pancard)) {
            activityStart(whichcard);

        } else if (whichcard.equals(Constants.licence)) {
            openDialog();
        } else if (whichcard.equals(Constants.passport)) {
            activityStart(whichcard);
        } else if (whichcard.equals(Constants.adharcard)) {
//            Intent intent = new Intent(this, QRCodeScanner.class);
            Intent intent = new Intent(this, QRCodeScanner.class);
            intent.putExtra("TAG_CAMERA", Constants.adharcard);
            startActivity(intent);
            finish();
        } else if (whichcard.equals(Constants.creditCard)) {
            try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.creditcard.android");
                if (launchIntent != null) {
                    launchIntent.putExtra("io.card.payment.requireExpiry", true); // default: false
                    launchIntent.putExtra("io.card.payment.requireCVV", true); // default: false
                    launchIntent.putExtra("io.card.payment.requirePostalCode", true); // default: false
                    startActivity(launchIntent);//null pointer check in case package name was not found
                    finish();
                } else {
                    new android.app.AlertDialog.Builder(ListActivity2.this)
                            .setTitle("Hint")
                            .setMessage("For the credit card scan you have to download Credit Card Scanner Application from play store..")
                            .setPositiveButton("Install",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.creditcard.android")));
                                            dialog.dismiss();
                                        }
                                    }).setNegativeButton("Cancel", null).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_CAMERA) {

            if (grantResults.length > 0) {
                boolean permissionGranted = true;

                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }

                if (permissionGranted) {
                    openDocumentScan();
                } else
                    requestDialog();
            } else {
                requestDialog();
            }
        }

    }

    private void requestDialog() {
        new AlertDialog.Builder(ListActivity2.this)
                .setMessage("Document Scanner needs permission to Access camera. Do you want to grant it?")
                .setTitle("Error")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (permissionManager.shouldRequestPermission(ListActivity2.this, permissions)) {
                            permissionManager.requestPermissions(permissions, REQUEST_CODE_CAMERA);
                        } else {
                            permissionManager.openSettingDialog(ListActivity2.this, "Please grant permissions to access the camera");
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void openDocumentScan() {
        Intent intent = new Intent(ListActivity2.this, DocumentScan.class);
        startActivity(intent);
        finish();
    }

    private void activityStart(String tag) {
        Intent intent = new Intent(this, CommonScan.class)
                .putExtra(CommonScan.SCANNER_TYPE, tag);
        startActivity(intent);
        finish();
    }

    public void openDialog() {

        LayoutInflater li = LayoutInflater.from(ListActivity2.this);
        View promptsView = li.inflate(R.layout.prompt_dialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ListActivity2.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });

        // create alert dialog
        alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public void onFormatLicenseScan_1(View pressed) {
        activityStart("licence_1");
    }

    public void onFormatLicenseScan_2(View pressed) {
        activityStart("licence_2");
    }

    public void onFormatLicenseScan_3(View pressed) {
        activityStart("licence_3");
    }

    private static class ViewHolder {
        public ImageView friendProfileCircularContactView;
        TextView friendName, headerView;
        Button timestamp;
    }

    private class DocumentAdapter extends SearchablePinnedHeaderListViewAdapter<SqliteDetail> {

        private ArrayList<SqliteDetail> mContacts;
//        Firebase_ImageLoader firebase_imageLoader = new Firebase_ImageLoader(ListActivity2.this);

        public DocumentAdapter(final ArrayList<SqliteDetail> contacts, OnNoValues onNoValues) {

            setData(contacts);
            super.setNoValuesCallback(onNoValues);

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
                rootView = mInflater.inflate(R.layout.activity_documents_list, parent, false);
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

            if (searchString != null) {
                String pancard_holder_name = displayName.toLowerCase(Locale.getDefault());
                searchString = searchString.toLowerCase();
                if (pancard_holder_name.contains(searchString.toLowerCase())) {
                    int startPos = pancard_holder_name.indexOf(searchString);
                    int endPos = startPos + searchString.length();

                    Spannable spanText = Spannable.Factory.getInstance().newSpannable(holder.friendName.getText()); // <- EDITED: Use the original string, as `country` has been converted to lowercase.
                    spanText.setSpan(new ForegroundColorSpan(Color.BLUE), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    holder.friendName.setText(spanText, TextView.BufferType.SPANNABLE);
                }
            }

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

    }

}
