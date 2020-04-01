package com.wangliu.moodtravel.users;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.utils.Constants;
import com.wangliu.moodtravel.utils.ToastUtils;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView back;
    private TextView bar;
    private TextView title;
    private EditText username;
    private ImageView cleanUsername;
    private EditText password;
    private ImageView cleanPassword;
    private EditText passwordSecond;
    private ImageView cleanPasswordSecond;
    private TextInputLayout tlUsername;
    private TextInputLayout tlFirst;
    private TextInputLayout tlSecond;
    private Button mBtnOK;

    private boolean isUser = false;
    private boolean isPsd = false;

    private static boolean fromLogin;

    public static void startActivity(AppCompatActivity activity, int requestCode) {
        Intent intent = new Intent(activity, RegisterActivity.class);
        activity.startActivityForResult(intent, requestCode);

        fromLogin = requestCode != Constants.REQUEST_USER_CENTER_ACTIVITY;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Bmob.initialize(this, this.getString(R.string.Bmob_appkey));

        initView();
    }

    private void initView() {
        back = findViewById(R.id.back);
        bar = findViewById(R.id.title_bar);
        title = findViewById(R.id.title);
        username = findViewById(R.id.et_username);
        cleanUsername = findViewById(R.id.clean_username);
        password = findViewById(R.id.et_password);
        cleanPassword = findViewById(R.id.clean_password);
        passwordSecond = findViewById(R.id.et_password_second);
        cleanPasswordSecond = findViewById(R.id.clean_password_second);
        mBtnOK = findViewById(R.id.btn_ok);
        tlUsername = findViewById(R.id.tl_username);
        tlFirst = findViewById(R.id.tl_first);
        tlSecond = findViewById(R.id.tl_second);

        runOnUiThread(() -> {
            if (fromLogin) {
                bar.setText("用户注册");
                title.setText("欢迎注册！");
                tlUsername.setHint("想一个用户名吧！");
                tlFirst.setHint("输入密码！");
                tlSecond.setHint("确认密码");
            } else {
                bar.setText("修改密码（其实是注册页面改的）");
                title.setText("因为懒 ~ ");
                tlUsername.setHint("输入旧密码");
                tlFirst.setHint("输入新密码！");
                tlSecond.setHint("确认新密码");
            }
        });

        registerListener();
    }

    private void registerListener() {
        back.setOnClickListener(this);
        mBtnOK.setOnClickListener(this);
        cleanUsername.setOnClickListener(this);
        cleanPasswordSecond.setOnClickListener(this);
        cleanPassword.setOnClickListener(this::onClick);

        textInputListener();

    }

    private void textInputListener() {
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 9 || s.length() < 4) {
                    isUser = false;
                    tlUsername.setError("请输入4到9位数字做用户名~");
                } else {
                    isUser = true;
                    tlUsername.setError(null);
                    tlUsername.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (isUser) {
                    tlUsername.setError(null);
                    tlUsername.setErrorEnabled(false);
                }
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                tlSecond.setError(null);
                tlFirst.setError(null);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()< 6) {
                    isPsd = false;
                    tlFirst.setErrorEnabled(true);
                    tlFirst.setError("请输入6位以上密码");
                } else {
                    isPsd = true;
                    tlFirst.setError(null);
                    tlFirst.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isPsd) {
                    tlFirst.setError(null);
                    tlFirst.setErrorEnabled(false);
                }

            }
        });

    }

    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.btn_ok:
                if (fromLogin) {
                    registerOK();
                } else {
                    updatePassword();
                }
                break;
            case R.id.clean_username:
                username.setText("");
                break;
            case R.id.clean_password_second:
                passwordSecond.setText("");
                break;
            case R.id.clean_password:
                password.setText("");
        }
    }

    private void updatePassword() {
        String pwd0 = username.getText().toString();
        String newPwd = password.getText().toString();
        String newPwdTwice = passwordSecond.getText().toString();
        if (TextUtils.isEmpty(pwd0) || TextUtils.isEmpty(newPwdTwice) || TextUtils.isEmpty(newPwd)) {
            ToastUtils.showMsg(this, "先填完再确认可以吗？", 0);
        } else if (newPwd.equals(newPwdTwice)) {
            tlSecond.setError(null);
            BmobUser.updateCurrentUserPassword(pwd0, newPwd, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        Intent intent = new Intent();
                        RegisterActivity.this.setResult(AppCompatActivity.RESULT_OK, intent);
                        finish();
                    } else {
                        tlUsername.setError("修改失败" + e.getMessage());
                        tlUsername.setErrorEnabled(true);
                    }
                }
            });
        } else {
            tlSecond.setError("两次密码不一致!");
            tlSecond.setErrorEnabled(true);
        }
    }

    private void registerOK() {
        String name = username.getText().toString();
        String psd = password.getText().toString();
        String psdSecond = passwordSecond.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(psd) || TextUtils.isEmpty(psdSecond)) {
            ToastUtils.showMsg(this, "先填完再确认可以吗？", 0);
        } else {
            if (psd.equals(psdSecond)) {
                tlSecond.setError(null);
                if (!isUser || !isPsd) {
                    ToastUtils.showMsg(this, "按要求填写！", 0);
                } else {
                    //注册
                    registerUser(name, psd);
                }
            } else {
                tlSecond.setError("两次输入密码不一致");
                tlSecond.setErrorEnabled(true);
            }
        }
    }

    /**
     * 在Bmob数据库中注册用户
     *
     * @param name 用户名
     * @param psd  密码
     */
    private void registerUser(String name, String psd) {
        User user = new User();
        user.setUsername(name);
        user.setPassword(psd);
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e != null) {
                    tlUsername.setErrorEnabled(true);
                    tlUsername.setError("注册失败了！" + e.getMessage());
                    Log.e("register", "" + e.getMessage());
                } else {
                    //把错误信息去了
                    tlUsername.setError(null);
                    ToastUtils.showMsg(RegisterActivity.this, "注册成功", 0);
                    //把数据放到intent里面再传回去
                    Intent intent = new Intent();
                    intent.putExtra("username", name).putExtra("password", psd);
                    RegisterActivity.this.setResult(AppCompatActivity.RESULT_OK, intent);
                    RegisterActivity.this.finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
