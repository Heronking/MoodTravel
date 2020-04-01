package com.wangliu.moodtravel.users;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.utils.ToastUtils;

import java.util.regex.Pattern;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class BindEmailActivity extends AppCompatActivity {

    private ImageView back;
    private Button ok;
    private EditText email;
    private TextInputLayout inputLayout;
    //邮箱正则表达式
    private static final String REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_email);
        Bmob.initialize(this, this.getString(R.string.Bmob_appkey));
        initView();

    }

    private void initView() {
        back = findViewById(R.id.back);
        ok = findViewById(R.id.btn_ok);
        email = findViewById(R.id.email);
        inputLayout = findViewById(R.id.tl);

        back.setOnClickListener(v -> finish());
        ok.setOnClickListener(v -> {
            String input = email.getText().toString();
            if (isEmail(input)) {
                emailVerify(input);
            }
            finish();
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                inputLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isEmail(s)) {
                    inputLayout.setError(null);
                    inputLayout.setErrorEnabled(false);
                } else {
                    inputLayout.setError("邮箱格式错误！");
                    inputLayout.setErrorEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 验证邮箱
     * @param input
     */
    private void emailVerify(String input) {
        User user = new User();
        user.setEmail(input);
        user.update(BmobUser.getCurrentUser(User.class).getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e != null) {
                    ToastUtils.showMsg(BindEmailActivity.this, "绑定失败 " + e.getMessage(), 0);
                    Log.e("Email", "" + e.getMessage());
                }
            }
        });

        BmobUser.requestEmailVerify(input, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    ToastUtils.showMsg(BindEmailActivity.this, "请求验证邮件成功，请到" + email + "邮箱中进行激活。", 1);
                } else {
                    ToastUtils.showMsg(BindEmailActivity.this, "请求邮箱验证失败！ " + e.getMessage(), 0);
                    Log.e("requestVerify", ""+e.getMessage());
                }
            }
        });
    }

    /**
     * 匹配邮箱
     * @param input
     * @return
     */
    public static boolean isEmail(CharSequence input) {
        return input != null && input.length() > 0
                && Pattern.matches(REGEX_EMAIL, input);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
