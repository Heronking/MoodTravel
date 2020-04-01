package com.wangliu.moodtravel.users;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.adapter.LoginHistoryAdapter;
import com.wangliu.moodtravel.sqlite.AccountHistory;
import com.wangliu.moodtravel.sqlite.LoginHistorySQLiteHelper;
import com.wangliu.moodtravel.utils.Constants;
import com.wangliu.moodtravel.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.UpdateListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEtUsername;
    private RelativeLayout mRePsd;
    private EditText mEtPassword;
    private ImageView clearUsername;
    private ImageView clearPassword;
    private CheckBox expandUsername;
    private CheckBox showPassword;
    private TextView mTvForget;
    private TextView mTvRegister;
    private ImageView registerOfQQ;
    private ImageView registerOfWX;
    private ImageView registerEmail;
    private Button mBtnLogin;
    private LoginHistorySQLiteHelper helper;
    private PopupWindow window = null;

    public static void startActivity(AppCompatActivity activity, int requestCode) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //初始化Bmob
        Bmob.initialize(this, this.getString(R.string.Bmob_appkey));
        helper = new LoginHistorySQLiteHelper(this);

        initView();
    }

    private void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);   //改一下状态栏

        mEtUsername = findViewById(R.id.et_username);
        mRePsd = findViewById(R.id.password);
        mEtPassword = findViewById(R.id.et_password);
        clearUsername = findViewById(R.id.clean_username);
        clearPassword = findViewById(R.id.clean_password);
        expandUsername = findViewById(R.id.expand_user_list);
        showPassword = findViewById(R.id.show_password);
        mBtnLogin = findViewById(R.id.btn_login);
        mTvForget = findViewById(R.id.tv_forget);
        mTvRegister = findViewById(R.id.tv_register);
        registerOfQQ = findViewById(R.id.qq_login);
        registerOfWX = findViewById(R.id.wx_login);
        registerEmail = findViewById(R.id.email_login);

        mEtPassword.setLetterSpacing(0.1f);

        registerListener();

    }

    private void registerListener() {
        clearUsername.setOnClickListener(this);
        clearPassword.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);
        mTvForget.setOnClickListener(this);
        registerEmail.setOnClickListener(this);
        registerOfQQ.setOnClickListener(this);
        registerOfWX.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this::onClick);
        //显示或者隐藏密码
        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        expandUsername.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {    //通过setCheck改变
                expandChange(View.GONE);
                if (window != null && window.isShowing()) {
                    window.dismiss();
                }
                popupWindow();
            } else {
                if (window != null) {
                    window.dismiss();
                }
                expandChange(View.VISIBLE);
            }
        });
        //输入帐号的时候，密码栏的按钮隐藏。
        mEtUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                runOnUiThread(() -> {
                    clearPassword.setVisibility(View.GONE);
                    showPassword.setVisibility(View.GONE);
                });
            } else {
                runOnUiThread(() -> {
                    showPassword.setVisibility(View.VISIBLE);
                    clearPassword.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void expandChange(int v) {
        runOnUiThread(() -> {
            mTvForget.setVisibility(v);
            mTvRegister.setVisibility(v);
            mBtnLogin.setVisibility(v);
            mRePsd.setVisibility(v);
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clean_username:
                mEtUsername.setText("");
                break;
            case R.id.clean_password:
                mEtPassword.setText("");
                break;
            case R.id.btn_login:
                String un = mEtUsername.getText().toString().trim();
                String pwd = mEtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(un) || TextUtils.isEmpty(pwd)) {
                    ToastUtils.showMsg(this, "先输账号密码了再登录！", 0);
                    break;
                }
                BmobUser.loginByAccount(un, pwd, new LogInListener<User>() {
                    @Override
                    public void done(User user, BmobException e) {
                        if (user != null) {
                            Intent intent = new Intent();
//                            Log.i("login", "登录成功");
                            //插入记录到数据库
                            helper.insertData(un, pwd, helper.getWritableDatabase());
                            intent.putExtra("resultType", Constants.LOGIN_RESULT);
                            LoginActivity.this.setResult(AppCompatActivity.RESULT_OK, intent);
                            LoginActivity.this.finish();
                        } else {
                            ToastUtils.showMsg(LoginActivity.this, "登录失败！" + e.getMessage(), 1);
                            Log.e("login", "" + e.getMessage());

                            runOnUiThread(() -> mEtPassword.setText(""));
                        }
                    }
                });

                break;
            case R.id.tv_register:
                RegisterActivity.startActivity(this, Constants.REQUEST_LOGIN_ACTIVITY);
                break;
            case R.id.wx_login:
            case R.id.qq_login:
                ToastUtils.showMsg(this, "诶？还没做呢！", 0);
                break;
            case R.id.email_login:
                startActivity(new Intent(this, BindEmailActivity.class));
                break;
            case R.id.tv_forget:
                forgetPasswordDialog();


        }
    }

    private void forgetPasswordDialog() {
        @SuppressLint("InflateParams") View forgetView = LayoutInflater.from(this).inflate(R.layout.layout_forget_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(forgetView);
        TextInputLayout layout = forgetView.findViewById(R.id.tl);
        EditText text = forgetView.findViewById(R.id.email);

        //设置dialog
        builder.setNegativeButton("去绑定", (dialog, which) -> {
            startActivity(new Intent(this, BindEmailActivity.class));
            dialog.dismiss();
        }).setPositiveButton("重置密码", (dialog, which) -> {
            String input = text.getText().toString();
            if (BindEmailActivity.isEmail(input)) {
                //通过邮件重置密码
                BmobUser.resetPasswordByEmail(input, new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e != null) {
                            layout.setError("发送邮件失败" + e.getMessage());
                            layout.setErrorEnabled(true);
                        } else {
                            layout.setErrorEnabled(false);
                            layout.setError(null);
                            ToastUtils.showMsg(LoginActivity.this, "重置密码请求成功，请到" + input + "邮箱进行密码重置操作", 0);
                            dialog.dismiss();
                            //最后贴心的清空一下密码框
                            runOnUiThread(() -> mEtPassword.setText(""));
                        }
                    }
                });
            } else {
                layout.setError("邮箱格式错误！");
                layout.setErrorEnabled(true);
            }
        }).setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //找到本activity需要的数据
        if (requestCode == Constants.REQUEST_LOGIN_ACTIVITY && resultCode == RESULT_OK) {
            if (data == null) {
                Log.e("LoginActivity", "data为空！");
                return;
            }
            runOnUiThread(() -> {
                mEtUsername.setText(data.getStringExtra("username"));
                mEtPassword.setText(data.getStringExtra("password"));
            });
            ToastUtils.showMsg(this, "请点击登录~", 0);
        }
    }

    private void popupWindow() {
        String sql = "select * from " + helper.tableName;
        @SuppressLint("Recycle") Cursor cursor = helper.getReadableDatabase().rawQuery(sql, null);
        if (cursor == null || cursor.getCount() == 0) {
            //数据库中没有东西了就重置
            helper.initTable(helper.getWritableDatabase());
            ToastUtils.showMsg(this, "暂无记录", 0);
            return;
        }

        window = new PopupWindow(this);
        List<AccountHistory> historyList = new ArrayList<>();
        while (cursor.moveToNext()) {
            AccountHistory history = new AccountHistory(cursor.getInt(cursor.getColumnIndex("id"))
                    , cursor.getString(cursor.getColumnIndex("username"))
                    , cursor.getString(cursor.getColumnIndex("password")));
            historyList.add(history);
        }
        LoginHistoryAdapter adapter = new LoginHistoryAdapter(this, historyList, helper);
        ListView view = (ListView) View.inflate(this, R.layout.item_pop_up_window, null);
        view.setAdapter(adapter);

        view.setOnItemClickListener((parent, view1, position, id) -> {
            //点了一条记录，就收回这个popupWindow
            expandUsername.setChecked(false);
            //获取点击的信息，更新输入栏
            runOnUiThread(() -> {
                AccountHistory account = (AccountHistory) adapter.getItem(position);
                mEtUsername.setText(account.getAccount());
                mEtPassword.setText(account.getPassword());
            });
            //关闭window
            window.dismiss();
        });
        //在最后面加一个view，让最后一条记录下面也有分割线
        view.addFooterView(new TextView(this));

        initWindow(window, view);
    }

    /**
     * 设置一下window的相关属性
     *
     * @param window
     * @param view
     */
    private void initWindow(PopupWindow window, ListView view) {

        window.setContentView(view);
        window.setAnimationStyle(0);
        window.setWidth(mEtUsername.getWidth());
        window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        window.setOutsideTouchable(true);
        window.showAsDropDown(mEtUsername);
        window.setOnDismissListener(() -> {
            expandUsername.setChecked(false);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}


