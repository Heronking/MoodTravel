package com.wangliu.moodtravel.utils;

import android.content.Context;
import android.widget.Toast;
/*
可以解决多次点击按钮时，重复弹出多个Toast的问题
 */
public class ToastUtils {
    public static Toast mToast;
    public static void showMsg(Context context, String msg, int time){
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, msg, time);
        mToast.show();
    }

}
