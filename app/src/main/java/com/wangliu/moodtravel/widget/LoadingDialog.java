package com.wangliu.moodtravel.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wangliu.moodtravel.R;

import java.util.Objects;

import static android.graphics.Color.TRANSPARENT;


public class LoadingDialog extends Dialog {
    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.LoadingDialog);
    }
    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    public static class Builder{

        private String message;    //提示消息
        private Context context;    //上下文

        private boolean isShowMessage = true;   //设置是否显示消息
        private boolean isCancelable = false;   //设置是否点击框内消失
        private boolean isCancelableOutside = false;    //设置点击框外是否消失
        public Builder(Context context) {
            this.context = context;
        }
        /**
         * 是否显示message
         * @param isShowMessage
         * @return
         */
        public Builder showMessage(boolean isShowMessage) {
            this.isShowMessage = isShowMessage;
            return this;
        }

        /**
         * 是否点击框内消失
         * @param isCancelable
         * @return
         */
        public Builder setCanelable(boolean isCancelable) {
            this.isCancelable = isCancelable;
            return this;
        }

        /**
         * 是否点击框外消失
         * @param isCancelableOutside
         * @return
         */
        public Builder setCanelableOutside(boolean isCancelableOutside) {
            this.isCancelableOutside = isCancelableOutside;
            return this;
        }
        /**
         * 设置消息
         * @param message
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public LoadingDialog create() {

            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.layout_loading_dialog, null);
            LoadingDialog loadingDialog = new LoadingDialog(context);

            loadingDialog.setContentView(view);
            loadingDialog.setCanceledOnTouchOutside(isCancelableOutside);
            loadingDialog.setCancelable(isCancelable);
            TextView mTvMessage = view.findViewById(R.id.tv_message);
            if (!isShowMessage) {
                mTvMessage.setVisibility(View.GONE);
            } else {
                mTvMessage.setText(message);
            }
            return loadingDialog;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(TRANSPARENT));
    }
}
