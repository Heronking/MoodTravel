package com.wangliu.moodtravel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.wangliu.moodtravel.utils.Constants;


@SuppressLint("Registered")
public class HasPermissionsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private boolean isGranted = false;  //是否已经全部授权
    protected String[] perms = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    /**
     * 申请权限
     */
    private void startRequest() {
        if (Build.VERSION.SDK_INT >= 23
                && getApplicationInfo().targetSdkVersion >= 23) {   //安卓6.0以上，动态申请权限
            initPermissions();
        }
    }

    private void initPermissions() {
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
        }
        if (!isGranted) {
            ActivityCompat.requestPermissions(this, perms, Constants.MY_PERMISSION_CODE);
            isGranted = true;
        }
    }

    /**
     * 请求权限后，回调此方法
     *
     * @param requestCode  权限请求码
     * @param permissions  权限数组
     * @param grantResults 弹出对话框后的点击结果数组，每个元素对丁每个权限，0为允许，-1为禁止
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasRejection = false;   //是否有禁止的权限
        if (requestCode == Constants.MY_PERMISSION_CODE) {
            for (int g : grantResults) {
                if (g == -1) {
                    hasRejection = true;
                    isGranted = false;
                    break;
                }
            }
            if (hasRejection) {
                showDialog();
                isGranted = true;
            }

        }
    }

    private void showDialog() {
        AlertDialog.Builder mPermsDialog = new AlertDialog.Builder(this);
        mPermsDialog.setTitle("提示");
        mPermsDialog.setMessage("当前应用缺少必要权限！")
                .setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:"+getPackageName()));
                        startActivity(intent);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();   //关闭页面
                    }
                }).setCancelable(false).create();
        mPermsDialog.show();
    }

    /**
     * 如果按下返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isGranted)
            startRequest();
    }
}
