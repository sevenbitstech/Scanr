package com.pancard.android.utility;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PermissionManager {

    //    private static String[] allPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
//            Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private Context context;

    private androidx.appcompat.app.AlertDialog permissionDialog;
    private OnPermissionDialogCancel onPermissionDialogCancel;

    public PermissionManager(Context context) {
        this.context = context;
    }

//    public static String[] getAllPermissions() {
//        return allPermissions;
//    }

    //Todo:for activity please use this method to get callback of permission result in the activity.
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(String[] permissions, int requestCode) {

        if (context instanceof Activity) {
            ((Activity) context).requestPermissions(permissions, requestCode);
        }
    }

    //Todo:for fragment please use this method to get callback of permission result in the fragment itself.
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(Fragment fragment, String[] permissions, int requestCode) {
        if (fragment.isAdded())
            fragment.requestPermissions(permissions, requestCode);
    }

    public boolean hasPermissions(String[] permissions) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean shouldRequestPermission(Activity activity, String[] permissions) {
        boolean showRationale = true;

        //check if user has checked don't ask again in any of the permission
        for (String permission : permissions) {

            if (activity != null) {
                if (!activity.shouldShowRequestPermissionRationale(permission)) {
                    showRationale = false;
                    break;
                }
            } else {
                Log.e("null", "activity");
            }

        }

        return showRationale;
    }

    public void openSettingDialog(Activity activity, String message, OnPermissionDialogCancel onPermissionDialogCancel) {
        this.onPermissionDialogCancel = onPermissionDialogCancel;
        openSettingDialog(activity, message);
    }

    public void openSettingDialog(final Activity activity, String message) {

//        AlertDialog alertDialog;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    openSettings(activity);
                    dialog.dismiss();
                    if (onPermissionDialogCancel != null)
                        onPermissionDialogCancel.onPermissionDialogCancelled();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    if (onPermissionDialogCancel != null)
                        onPermissionDialogCancel.onPermissionDialogCancelled();
                })
                .setOnCancelListener(dialog -> {
                    dialog.dismiss();
                    if (onPermissionDialogCancel != null)
                        onPermissionDialogCancel.onPermissionDialogCancelled();
                });

        permissionDialog = builder.show();
    }

    public void showPermissionRequireDialog(final Activity activity, String message, OnPermissionRequireDialog onPermissionRequireDialog) {

//        AlertDialog alertDialog;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setCancelable(false);
        builder.setMessage(message)
                .setPositiveButton("Ok", (dialog, which) -> {
//                        openSettings(activity);
//                        permissionDialog.dismiss();
                    if (onPermissionRequireDialog != null)
                        onPermissionRequireDialog.onAcceptPermissionDialog();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
//                        permissionDialog.dismiss();
                    if (onPermissionRequireDialog != null)
                        onPermissionRequireDialog.onCancelPermissionDialog();
                })
                .setOnCancelListener(dialog -> {
//                        permissionDialog.dismiss();
                    if (onPermissionRequireDialog != null)
                        onPermissionRequireDialog.onCancelPermissionDialog();
                });

        permissionDialog = builder.show();
    }

    private void openSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, 500);
    }

    public boolean isPermissionDialogShowing() {
        return permissionDialog != null && permissionDialog.isShowing();
    }

    public boolean isPermissionsGranted(int[] grantResults) {
        boolean permissionGranted = true;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
                break;
            }
        }

        return permissionGranted;
    }

    public interface OnPermissionDialogCancel {
        void onPermissionDialogCancelled();
    }

    public interface OnPermissionRequireDialog {
        void onAcceptPermissionDialog();

        void onCancelPermissionDialog();
    }
}
