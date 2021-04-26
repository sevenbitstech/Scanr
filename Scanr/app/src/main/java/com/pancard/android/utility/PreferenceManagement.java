package com.pancard.android.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Arrays;
import java.util.Base64;

public class PreferenceManagement {


    private static final String KEY_SIZE_DETAIL_INT = "SizeDetail";
    private static final String KEY_EMAIL_STRING = "EmailID";
    private static final String KEY_IS_PROACTIVE = "IsProActive";
    private static final String KEY_IS_SUBSCRIPTION_DIALOG = "KEY_IS_SUBSCRIPTION_DIALOG";
    private static final String KEY_UPGRADE_PRO_DIALOG_TIME = "UPGRADE_PRO_DIALOG_TIME";
    private static final String KEY_PURCHASE_TOKEN = "KEY_PURCHASE_TOKEN";
    private static final String KEY_FIRST_OPEN_STRING = "FirstOpen";

    //pin
    private static final String KEY_PIN_UPDATE_BOOL = "PINUpdate";
    private static final String KEY_PIN_UPDATE_FIREBASE_BOOL = "updetedInFirebase";
    private static final String KEY_ONLINE_PIN_BOOL = "OnlinePin";
    private static final String KEY_PIN_STRING = "PIN";
    private static final String KEY_SYNC_ONLINE_PIN_BOOL = "SynceOnlinePin";
    private static final String KEY_DRIVE_SCOPE_MIGRATION = "drive_scope_migration";

    private static final String KEY_SYNC_ONLINE_MESSAGE_BOOL = "SynceOnlineMessage";
    private static final String KEY_SYNC_SQLITE_BOOL = "SqliteSync";
    private static final String KEY_SYNC_DRIVE_TO_DB = "syncDriveToDB";

    //get list
    private static final String KEY_FIRST_GET_LIST_BOOL = "FirstGetList";
    private static final String KEY_DOCUMENT_GET_LIST_STRING = Constants.document + "Get List";
    private static final String KEY_BUSINESS_CARD_GET_LIST_STRING = Constants.businesscard + "Get List";
    private static final String KEY_PAN_CARD_GET_LIST_STRING = Constants.pancard + "Get List";
    private static final String KEY_PASSPORT_GET_LIST_STRING = Constants.passport + "Get List";
    private static final String KEY_LICENCE_GET_LIST_STRING = Constants.licence + "Get List";
    private static final String KEY_AADHAR_GET_LIST_STRING = Constants.adharcard + "Get List";
    private static final String KEY_CREDIT_CARD_GET_LIST_STRING = Constants.creditCard + "Get List";

    private static final String KEY_IS_DRIVE_CONNECTED = "IsDriveConnected";
    private static final String KEY_IS_SHOWED_DRIVE_DIALOG = "IsShowedDriveDialog";
    private static final String KEY_METADATA_DOC_DRIVE_ID = "metadata doc drive file id";

    //update
    private static final String KEY_LATEST_VERSION_NEVER_BOOL = "latest version never";
    private static final String KEY_LATEST_VERSION_STRING = "latest version";

    private static final String KEY_IS_DRIVE_SYNC_COMPLETE = "is drive sync complete";

    private static final String KEY_IS_INTRO_SHOWED = "is intro screen showed";

    private static final String KEY_IS_SHOWED_CAMERA_PERMISSION_DIALOG = "is camera permission dialog showed";
    private static final String KEY_IS_SHOWED_STORAGE_PERMISSION_DIALOG = "is storage permission dialog showed";
    private static final String KEY_IS_SHOWED_WRITE_STORAGE_PERMISSION_DIALOG = "is write storage permission dialog showed";

    private boolean isShowedCameraPermissionDialog = false;
    private boolean isShowedStoragePermissionDialog = false;

    private SharedPreferences preferences;

    public PreferenceManagement(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setStringPreference(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void setBooleanPreference(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void setIntPreference(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private void setLongPreference(String key, long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void setByteArrayPreference(String key, byte[] value) {

        SharedPreferences.Editor editor = preferences.edit();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String encodeToString = Base64.getEncoder().encodeToString(value);
            editor.putString(key, encodeToString);
        } else {
            editor.putString(key, Arrays.toString(value));
        }
        editor.apply();
    }

    private long getLongPreference(String key) {
        return preferences.getLong(key, 0);
    }

    private String getStringPreference(String key) {
        return preferences.getString(key, null);
    }

    private boolean getBooleanPreference(String key) {
        return preferences.getBoolean(key, false);
    }

    private int getIntPreference(String key) {
        return preferences.getInt(key, 0);
    }

    public byte[] getByteArrayPreference(String key) {

        String stringArray = preferences.getString(key, null);

        if (stringArray != null && stringArray.length() > 1) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                byte[] bytes = Base64.getDecoder().decode(stringArray);
                return bytes;
            } else {

//                byte[] bytes = stringArray.getBytes();
//                return bytes;
                String[] split = stringArray.substring(1, stringArray.length() - 1).split(", ");
                Log.e("split length", String.valueOf(split.length));
                byte[] array = new byte[split.length];

                if (array.length > 1) {
                    for (int i = 0; i < split.length; i++) {
                        array[i] = Byte.parseByte(split[i]);
                    }
                }
                return array;
            }
        }
        return null;
    }

    public void RemoveAllSharedPreference() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public int getSizeDetail() {
        return getIntPreference(KEY_SIZE_DETAIL_INT);
    }

    public void setSizeDetail(int sizeDetail) {
        setIntPreference(KEY_SIZE_DETAIL_INT, sizeDetail);
    }

    public long getUpgradeDialogTimeStamp() {
        return getLongPreference(KEY_UPGRADE_PRO_DIALOG_TIME);
    }

    public void setUpgradeDialogTimeStamp(long timeStamp) {
        setLongPreference(KEY_UPGRADE_PRO_DIALOG_TIME, timeStamp);
    }

    public boolean isLatestVersionNever() {
        return getBooleanPreference(KEY_LATEST_VERSION_NEVER_BOOL);
    }

    public void setLatestVersionNever(boolean latestVersionNever) {
        setBooleanPreference(KEY_LATEST_VERSION_NEVER_BOOL, latestVersionNever);
    }

    public String getLatestVersion() {
        return getStringPreference(KEY_LATEST_VERSION_STRING);
    }

    public void setLatestVersion(String latestVersion) {
        setStringPreference(KEY_LATEST_VERSION_STRING, latestVersion);
    }

    public boolean isPinUpdate() {
        return getBooleanPreference(KEY_PIN_UPDATE_BOOL);
    }

    public void setPinUpdate(boolean pinUpdate) {
        setBooleanPreference(KEY_PIN_UPDATE_BOOL, pinUpdate);
    }

    public boolean isPinUpdatedInFirebase() {
        return getBooleanPreference(KEY_PIN_UPDATE_FIREBASE_BOOL);
    }

    public void setPinUpdatedInFirebase(boolean pinUpdatedInFirebase) {
        setBooleanPreference(KEY_PIN_UPDATE_FIREBASE_BOOL, pinUpdatedInFirebase);
    }

    public boolean isOnlinePin() {
        return getBooleanPreference(KEY_ONLINE_PIN_BOOL);
    }

    public void setOnlinePin(boolean onlinePin) {
        setBooleanPreference(KEY_ONLINE_PIN_BOOL, onlinePin);
    }

    public String getPin() {
        return getStringPreference(KEY_PIN_STRING);
    }

    public void setPin(String pin) {
        setStringPreference(KEY_PIN_STRING, pin);
    }

    public boolean isSyncOnlinePin() {
        return getBooleanPreference(KEY_SYNC_ONLINE_PIN_BOOL);
    }

    public void setSyncOnlinePin(boolean syncOnlinePin) {
        setBooleanPreference(KEY_SYNC_ONLINE_PIN_BOOL, syncOnlinePin);
    }

    public boolean isSyncOnlineMessage() {
        return getBooleanPreference(KEY_SYNC_ONLINE_MESSAGE_BOOL);
    }

    public void setSyncOnlineMessage(boolean syncOnlineMessage) {
        setBooleanPreference(KEY_SYNC_ONLINE_MESSAGE_BOOL, syncOnlineMessage);
    }

    public boolean isSyncSqlite() {
        return getBooleanPreference(KEY_SYNC_SQLITE_BOOL);
    }

    public void setSyncSqlite(boolean syncSqlite) {
        setBooleanPreference(KEY_SYNC_SQLITE_BOOL, syncSqlite);
    }

    public boolean isSyncDriveToDb() {
        return getBooleanPreference(KEY_SYNC_DRIVE_TO_DB);
    }

    public void setSyncDriveToDb(boolean isSyncDriveToDb) {
        setBooleanPreference(KEY_SYNC_DRIVE_TO_DB, isSyncDriveToDb);
    }

    public boolean isMigrationDone() {
        return getBooleanPreference(KEY_DRIVE_SCOPE_MIGRATION);
    }

    public void setMigrationDone(boolean isMigrationDone) {
        setBooleanPreference(KEY_DRIVE_SCOPE_MIGRATION, isMigrationDone);
    }

    public boolean isFirstGetList() {
        return getBooleanPreference(KEY_FIRST_GET_LIST_BOOL);
    }

    public void setFirstGetList(boolean firstGetList) {
        setBooleanPreference(KEY_FIRST_GET_LIST_BOOL, firstGetList);
    }

    public String getBusinessCardGetList() {
        return getStringPreference(KEY_BUSINESS_CARD_GET_LIST_STRING);
    }

    public void setBusinessCardGetList(String businessCardGetList) {
        setStringPreference(KEY_BUSINESS_CARD_GET_LIST_STRING, businessCardGetList);
    }

    public String getDocumentGetList() {
        return getStringPreference(KEY_DOCUMENT_GET_LIST_STRING);
    }

    public void setDocumentGetList(String documentGetList) {
        setStringPreference(KEY_DOCUMENT_GET_LIST_STRING, documentGetList);
    }

    public String getAadharCardGetList() {
        return getStringPreference(KEY_AADHAR_GET_LIST_STRING);
    }

    public void setAadharCardGetList(String aadharCardGetList) {
        setStringPreference(KEY_AADHAR_GET_LIST_STRING, aadharCardGetList);
    }

    public String getPanCardGetList() {
        return getStringPreference(KEY_PAN_CARD_GET_LIST_STRING);
    }

    public void setPanCardGetList(String panCardGetList) {
        setStringPreference(KEY_PAN_CARD_GET_LIST_STRING, panCardGetList);
    }

    public String getPassportGetList() {
        return getStringPreference(KEY_PASSPORT_GET_LIST_STRING);
    }

    public void setPassportGetList(String passportGetList) {
        setStringPreference(KEY_PASSPORT_GET_LIST_STRING, passportGetList);
    }

    public String getCreditCardGetList() {
        return getStringPreference(KEY_CREDIT_CARD_GET_LIST_STRING);
    }

    public void setCreditCardGetList(String creditCardGetList) {
        setStringPreference(KEY_CREDIT_CARD_GET_LIST_STRING, creditCardGetList);
    }

    public String getLicenceGetList() {
        return getStringPreference(KEY_LICENCE_GET_LIST_STRING);
    }

    public void setLicenceGetList(String licenceGetList) {
        setStringPreference(KEY_LICENCE_GET_LIST_STRING, licenceGetList);
    }

    public String getEmail() {
        Log.e("get email", "from pref");
        return getStringPreference(KEY_EMAIL_STRING);
    }

    public void setEmail(String email) {
        Log.e("email set", "in pref");
        setStringPreference(KEY_EMAIL_STRING, email);
    }

    public boolean isProActive() {
        return getBooleanPreference(KEY_IS_PROACTIVE);
    }

    public void setisProActive(boolean isActive) {
        Log.e("settting pro activity", String.valueOf(isActive));
        setBooleanPreference(KEY_IS_PROACTIVE, isActive);
    }

    public String getPurchaseToken() {
        Log.e("get email", "from pref");
        return getStringPreference(KEY_PURCHASE_TOKEN);
    }

    public void setPurchaseToken(String isActive) {
        Log.e("email set", "in pref");
        setStringPreference(KEY_PURCHASE_TOKEN, isActive);
    }

    public boolean isExpiredDiloag() {
        Log.e("get email", "from pref");
        return getBooleanPreference(KEY_IS_SUBSCRIPTION_DIALOG);
    }

    public void setisExpiredDiloag(boolean isActive) {
        Log.e("email set", "in pref");
        setBooleanPreference(KEY_IS_SUBSCRIPTION_DIALOG, isActive);
    }


    public String getFirstOpen() {
        return getStringPreference(KEY_FIRST_OPEN_STRING);
    }

    public void setFirstOpen(String firstOpen) {
        setStringPreference(KEY_FIRST_OPEN_STRING, firstOpen);
    }

    public boolean isDriveConnected() {
        return getBooleanPreference(KEY_IS_DRIVE_CONNECTED);
    }

    public void setIsDriveConnected(boolean driveConnected) {
        setBooleanPreference(KEY_IS_DRIVE_CONNECTED, driveConnected);
    }

    public boolean isShowedDriveDialog() {
        return getBooleanPreference(KEY_IS_SHOWED_DRIVE_DIALOG);
    }

    public void setShowedDriveDialog(boolean showedDriveDialog) {
        setBooleanPreference(KEY_IS_SHOWED_DRIVE_DIALOG, showedDriveDialog);
    }

    public boolean isShowedIntroScreen() {
        return getBooleanPreference(KEY_IS_INTRO_SHOWED);
    }

    public void setShowedIntroScreen(boolean showedIntroScreen) {
        setBooleanPreference(KEY_IS_INTRO_SHOWED, showedIntroScreen);
    }

    public String getMetadataDocDriveId() {
        return getStringPreference(KEY_METADATA_DOC_DRIVE_ID);
    }

    public void setMetadataDocDriveId(String metadataDocDriveId) {
        setStringPreference(KEY_METADATA_DOC_DRIVE_ID, metadataDocDriveId);
    }

    public boolean isDriveSyncComplete() {
        return getBooleanPreference(KEY_IS_DRIVE_SYNC_COMPLETE);
    }

    public void setDriveSyncComplete(boolean showedDriveDialog) {
        setBooleanPreference(KEY_IS_DRIVE_SYNC_COMPLETE, showedDriveDialog);
    }

    public boolean isShowedCameraPermissionDialog() {
        return getBooleanPreference(KEY_IS_SHOWED_CAMERA_PERMISSION_DIALOG);
    }

    public void setShowedCameraPermissionDialog(boolean showedCameraPermissionDialog) {
        setBooleanPreference(KEY_IS_SHOWED_CAMERA_PERMISSION_DIALOG, showedCameraPermissionDialog);
    }

    public boolean isShowedStoragePermissionDialog() {
        return getBooleanPreference(KEY_IS_SHOWED_STORAGE_PERMISSION_DIALOG);
    }

    public void setShowedStoragePermissionDialog(boolean showedStoragePermissionDialog) {
        setBooleanPreference(KEY_IS_SHOWED_STORAGE_PERMISSION_DIALOG, showedStoragePermissionDialog);
    }

    public boolean isShowedWriteStoragePermissionDialog() {
        return getBooleanPreference(KEY_IS_SHOWED_WRITE_STORAGE_PERMISSION_DIALOG);
    }

    public void setShowedWriteStoragePermissionDialog(boolean showedWriteStoragePermissionDialog) {
        setBooleanPreference(KEY_IS_SHOWED_STORAGE_PERMISSION_DIALOG, showedWriteStoragePermissionDialog);
    }

    public void removeAllData() {
        setShowedDriveDialog(false);
        setIsDriveConnected(false);
        setAadharCardGetList(null);
        setBusinessCardGetList(null);
        setCreditCardGetList(null);
        setDocumentGetList(null);
        setEmail(null);
        setFirstGetList(false);
        setMetadataDocDriveId(null);
        setLicenceGetList(null);
        setOnlinePin(false);
        setPanCardGetList(null);
        setSyncSqlite(false);
        setPassportGetList(null);
        setSyncOnlineMessage(false);
        setPinUpdatedInFirebase(false);
        setPin(null);
        setPinUpdate(false);
        setDriveSyncComplete(false);
        setSyncDriveToDb(false);
        setisProActive(false);
        setPurchaseToken(null);
    }
}
