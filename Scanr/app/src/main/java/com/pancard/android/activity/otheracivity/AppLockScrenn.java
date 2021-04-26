package com.pancard.android.activity.otheracivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.docscan.android.R;
import com.pancard.android.Globalarea;
import com.pancard.android.Scanner;
import com.pancard.android.activity.scanactivity.ListActivity;
import com.pancard.android.utility.Constants;
import com.pancard.android.utility.PreferenceManagement;

/**
 * This activity holds view with a custom 4-digit PIN EditText.
 */
public class AppLockScrenn extends Activity implements View.OnFocusChangeListener, View.OnKeyListener, TextWatcher {
    Button btn_skip, btn_savepin;
    TextView typmessage;
    String type_pin = "";
    String compair_pin = "";
    PreferenceManagement preferences;
    private EditText mPinFirstDigitEditText;
    private EditText mPinSecondDigitEditText;
    private EditText mPinThirdDigitEditText;
    private EditText mPinForthDigitEditText;
    private EditText mPinHiddenEditText;

    /**
     * Sets focus on a specific EditText field.
     *
     * @param editText EditText to set focus on
     */
    public static void setFocus(EditText editText) {
        if (editText == null)
            return;

        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /**
     * Hides soft keyboard.
     *
     * @param editText EditText which has focus
     */
    public void hideSoftKeyboard(EditText editText) {
        if (editText == null)
            return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        if (btn_savepin.getVisibility() == View.GONE)
            chnagePIN();
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * Initialize EditText fields.
     */
    private void init() {
        preferences = Scanner.getInstance().getPreferences();
        mPinFirstDigitEditText = findViewById(R.id.pin_first_edittext);
        mPinSecondDigitEditText = findViewById(R.id.pin_second_edittext);
        mPinThirdDigitEditText = findViewById(R.id.pin_third_edittext);
        mPinForthDigitEditText = findViewById(R.id.pin_forth_edittext);
        mPinHiddenEditText = findViewById(R.id.pin_hidden_edittext);
        btn_skip = findViewById(R.id.btn_skip);
        btn_savepin = findViewById(R.id.btn_savepin);
        typmessage = findViewById(R.id.typemessage);

        if (preferences.getPin() != null) {
            if (preferences.getPin().trim().length() == 4) {
                btn_savepin.setVisibility(View.GONE);
                typmessage.setText(R.string.enter_pin);
            }
        }

        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppLockScrenn.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_savepin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type_pin.trim() != null && type_pin.trim().length() == 4) {
                    if (btn_savepin.getText().toString().trim().toLowerCase().equals("Continue".toLowerCase())) {
                        compair_pin = type_pin;
                        btn_savepin.setText(R.string.save);
                        typmessage.setText(R.string.reenter_pin);

                        mPinFirstDigitEditText.getText().clear();
                        mPinSecondDigitEditText.getText().clear();
                        mPinThirdDigitEditText.getText().clear();
                        mPinForthDigitEditText.getText().clear();

                        if (mPinHiddenEditText.length() > 0)
                            mPinHiddenEditText.getText().clear();
                        showKeyboard();

                    } else if (btn_savepin.getText().toString().trim().toLowerCase().equals("Save".toLowerCase())) {

                        if (compair_pin.trim().equals(type_pin.trim())) {
//                            sendSecurityStatus(true, type_pin);
                            Globalarea.actionFire = true;

                            preferences.setPinUpdate(true);
                            preferences.setPinUpdatedInFirebase(false);
                            preferences.setPin(type_pin);

                            if (!isInternetOn()) {
                                if (!preferences.isSyncOnlinePin()) {
                                    preferences.setSyncOnlinePin(true);
                                    new android.app.AlertDialog.Builder(AppLockScrenn.this)
                                            .setTitle(getResources().getString(R.string.hint))
                                            .setMessage(getResources().getString(R.string.SyncMessage))
                                            .setCancelable(false)
                                            .setPositiveButton("Ok",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog,
                                                                            int id) {
                                                            startActivity();
                                                        }
                                                    }).show();
                                } else {
                                    startActivity();
                                }
                            } else {
                                startActivity();
                            }
                        } else {
                            typmessage.setTextColor(Color.RED);
                            typmessage.setText(R.string.error_pin);
                            showKeyboard();
                            Vibrator v1 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v1.vibrate(400);

                        }
                    }
                }
            }
        });
    }

    public boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) Scanner.getInstance().getSystemService(CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {


            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {


            return false;
        }
        return false;
    }

    private void displayAlert(String message) {
        new AlertDialog.Builder(AppLockScrenn.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.try_again),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                startActivity(new Intent(AppLockScrenn.this, HomeActivity.class));

                                finish();
                            }
                        }).show();
    }

    private void startActivity() {
        Intent scantype = this.getIntent();
        if (scantype.getStringExtra(Constants.ActivityName).equals(Constants.pancard)) {
            callActivity(Constants.pancard);

        } else if (scantype.getStringExtra(Constants.ActivityName).equals(Constants.passport)) {
            callActivity(Constants.passport);

        } else if (scantype.getStringExtra(Constants.ActivityName).equals(Constants.adharcard)) {
            callActivity(Constants.adharcard);

        } else if (scantype.getStringExtra(Constants.ActivityName).equals(Constants.licence)) {
            callActivity(Constants.licence);

        } else if (scantype.getStringExtra(Constants.ActivityName).equals(Constants.businesscard)) {
            callActivity(Constants.businesscard);

        } else if (scantype.getStringExtra(Constants.ActivityName).equals(Constants.document)) {
            callActivity(Constants.document);

        } else {
            Intent intent = new Intent(AppLockScrenn.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void callActivity(String tag) {
        Intent intent = new Intent(AppLockScrenn.this, ListActivity.class);
        intent.putExtra("TAG_CAMERA", tag);
        startActivity(intent);
        finish();
    }

    private void chnagePIN() {
        Intent scantype = this.getIntent();
        if (scantype.getStringExtra(Constants.ActivityName).equals("ChangePIN")) {

            if (preferences.getPin() != null) {
                if (preferences.getPin().trim().length() == 4) {
                    if (preferences.getPin().trim().equals(type_pin.trim())) {
                        btn_savepin.setVisibility(View.VISIBLE);
                        typmessage.setText("Enter New PIN");
                        mPinFirstDigitEditText.getText().clear();
                        mPinSecondDigitEditText.getText().clear();
                        mPinThirdDigitEditText.getText().clear();
                        mPinForthDigitEditText.getText().clear();

//                        if (mPinHiddenEditText.length() > 0)
                        mPinHiddenEditText.setText("");
                        showKeyboard();

                    } else {
                        typmessage.setTextColor(Color.RED);
                        typmessage.setText("Wrong PIN!!");
                        showKeyboard();

                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(400);

                    }
                }
            }
        } else if (scantype.getStringExtra(Constants.ActivityName).equals("RemovePIN")) {
            if (preferences.getPin() != null) {
                if (preferences.getPin().trim().length() == 4) {
                    if (preferences.getPin().trim().equals(type_pin.trim())) {
                        Globalarea.actionFire = true;
                        preferences.setPin("");
                        preferences.setPinUpdate(false);
                        preferences.setPinUpdatedInFirebase(false);

                        startActivity();
//                        sendSecurityStatus(false, "0000");

                    } else {
                        typmessage.setTextColor(Color.RED);
                        typmessage.setText(getResources().getString(R.string.error_wrong_pin));
                        showKeyboard();

                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(400);

                    }
                }
            }
        } else {
            if (preferences.getPin() != null) {
                if (preferences.getPin().trim().length() == 4) {
                    if (preferences.getPin().trim().equals(type_pin.trim())) {
                        startActivity();
                    } else {
                        typmessage.setTextColor(Color.RED);
                        typmessage.setText(getResources().getString(R.string.error_wrong_pin));
                        showKeyboard();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(400);

                    }
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MainLayout(this, null));
        init();
        setPINListeners();


    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        final int id = v.getId();
        switch (id) {
            case R.id.pin_first_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_second_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_third_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_forth_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            final int id = v.getId();
            switch (id) {
                case R.id.pin_hidden_edittext:
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        if (mPinHiddenEditText.getText().length() == 4)
                            mPinForthDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 3)
                            mPinThirdDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 2)
                            mPinSecondDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 1)
                            mPinFirstDigitEditText.setText("");

                        if (mPinHiddenEditText.length() > 0)
                            mPinHiddenEditText.setText(mPinHiddenEditText.getText().subSequence(0, mPinHiddenEditText.length() - 1));

                        return true;
                    }

                    break;

                default:
                    return false;
            }
        }

        return false;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setDefaultPinBackground(mPinFirstDigitEditText);
        setDefaultPinBackground(mPinSecondDigitEditText);
        setDefaultPinBackground(mPinThirdDigitEditText);
        setDefaultPinBackground(mPinForthDigitEditText);
        typmessage.setTextColor(Color.BLACK);

        if (s.length() == 0) {
            setFocusedPinBackground(mPinFirstDigitEditText);
            mPinFirstDigitEditText.setText("");
            type_pin = s.toString();

        } else if (s.length() == 1) {
            setFocusedPinBackground(mPinSecondDigitEditText);
            mPinFirstDigitEditText.setText(s.charAt(0) + "");
            mPinSecondDigitEditText.setText("");
            mPinThirdDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
            type_pin = s.toString();

        } else if (s.length() == 2) {
            setFocusedPinBackground(mPinThirdDigitEditText);
            mPinSecondDigitEditText.setText(s.charAt(1) + "");
            mPinThirdDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
            type_pin = s.toString();

        } else if (s.length() == 3) {
            setFocusedPinBackground(mPinForthDigitEditText);
            mPinThirdDigitEditText.setText(s.charAt(2) + "");
            mPinForthDigitEditText.setText("");
            type_pin = s.toString();

        } else if (s.length() == 4) {
            setFocusedPinBackground(mPinForthDigitEditText);
            mPinForthDigitEditText.setText(s.charAt(3) + "");
            type_pin = s.toString();

            hideSoftKeyboard(mPinForthDigitEditText);
        }
    }

    /**
     * Sets default PIN background.
     *
     * @param editText edit text to change
     */
    private void setDefaultPinBackground(EditText editText) {
//        setViewBackground(editText, getResources().getDrawable(R.drawable.textfield_default_holo_light));
    }

    /**
     * Sets focused PIN field background.
     *
     * @param editText edit text to change
     */
    private void setFocusedPinBackground(EditText editText) {
//        setViewBackground(editText, getResources().getDrawable(R.drawable.textfield_focused_holo_light));
    }

    /**
     * Sets listeners for EditText fields.
     */
    private void setPINListeners() {
        mPinHiddenEditText.addTextChangedListener(this);

        mPinFirstDigitEditText.setOnFocusChangeListener(this);
        mPinSecondDigitEditText.setOnFocusChangeListener(this);
        mPinThirdDigitEditText.setOnFocusChangeListener(this);
        mPinForthDigitEditText.setOnFocusChangeListener(this);


        mPinFirstDigitEditText.setOnKeyListener(this);
        mPinSecondDigitEditText.setOnKeyListener(this);
        mPinThirdDigitEditText.setOnKeyListener(this);
        mPinForthDigitEditText.setOnKeyListener(this);

        mPinHiddenEditText.setOnKeyListener(this);
    }

    /**
     * Sets background of the view.
     * This method varies in implementation depending on Android SDK version.
     *
     * @param view       View to which set background
     * @param background Background to set to view
     */
    @SuppressWarnings("deprecation")
    public void setViewBackground(View view, Drawable background) {
        if (view == null || background == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    /**
     * Shows soft keyboard.
     *
     * @param editText EditText which has focus
     */
    public void showSoftKeyboard(EditText editText) {
        if (editText == null)
            return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AppLockScrenn.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Custom LinearLayout with overridden onMeasure() method
     * for handling software keyboard show and hide events.
     */
    public class MainLayout extends LinearLayout {

        public MainLayout(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.activity_app_lock_screnn, this);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int proposedHeight = MeasureSpec.getSize(heightMeasureSpec);
            final int actualHeight = getHeight();

            Log.d("TAG", "proposed: " + proposedHeight + ", actual: " + actualHeight);

            if (actualHeight >= proposedHeight) {
                // Keyboard is shown
                if (mPinHiddenEditText.length() == 0)
                    setFocusedPinBackground(mPinFirstDigitEditText);
                else
                    setDefaultPinBackground(mPinFirstDigitEditText);
            }

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
