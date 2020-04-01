package com.wangliu.moodtravel.users;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lljjcoder.Interface.OnCityItemClickListener;
import com.lljjcoder.bean.CityBean;
import com.lljjcoder.bean.DistrictBean;
import com.lljjcoder.bean.ProvinceBean;
import com.lljjcoder.citywheel.CityConfig;
import com.lljjcoder.style.citypickerview.CityPickerView;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.adapter.AvatarAdapter;
import com.wangliu.moodtravel.utils.AvatarUtils;
import com.wangliu.moodtravel.utils.Constants;
import com.wangliu.moodtravel.utils.ToastUtils;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class UserCenterActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView back;
    private RelativeLayout changePassword;
    private RelativeLayout bindEmail;
    private RelativeLayout setNickName;
    private TextView nickname;
    private RelativeLayout setAvatar;
    private ImageView avatar;
    private RelativeLayout setAddress;
    private TextView address;
    private Button loginOut;

    private TextView emailInfo;

    private String ID;
    private String email;   //邮箱
    private Boolean emailVerify; //邮箱验证

    private CityPickerView cityPickerView;  //城市选择器
    private CityConfig.Builder builder; //城市选择器的配置

    public static void startActivity(AppCompatActivity activity, int requestCode) {
        Intent intent = new Intent(activity, UserCenterActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);
        Bmob.initialize(this, this.getString(R.string.Bmob_appkey));

        initCityPicker();
        initView();
        updateInfoByUser();

    }

    private void initCityPicker() {
        cityPickerView = new CityPickerView();
        new Thread(() -> {
            //预加载数据
            cityPickerView.init(UserCenterActivity.this);
        }).start();
    }

    /**
     * 根据用户更新个人信息
     */
    private void updateInfoByUser() {
        //user不会为null，在跳转之前已经判断了
        User user = BmobUser.getCurrentUser(User.class);
        builder = new CityConfig.Builder();

        ID = user.getObjectId();
        String name = user.getNickName();
        City home = user.getHome();

        email = user.getEmail();
        emailVerify = user.getEmailVerified();

        runOnUiThread(() -> {
            if (name != null) {
                nickname.setText(name);
            } else {
                nickname.setText(this.getString(R.string.hint_nickname));
            }
            if (home != null) {
                address.setText(home.getName());
                //配置默认的地址
                builder.province(home.province).city(home.city).district(home.area);
            } else {
                address.setText(this.getString(R.string.hint_address));
            }
            avatar.setImageResource(AvatarUtils.avatars.get(user.getAvatar()));
        });
        //把配置传到picker中
        CityConfig config = builder.build();
        cityPickerView.setConfig(config);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        back = findViewById(R.id.back);
        changePassword = findViewById(R.id.change_password);
        setNickName = findViewById(R.id.set_nickname);
        nickname = findViewById(R.id.nickname);
        setAddress = findViewById(R.id.set_address);
        address = findViewById(R.id.address);
        setAvatar = findViewById(R.id.set_avatar);
        avatar = findViewById(R.id.avatar);
        loginOut = findViewById(R.id.login_out);
        bindEmail = findViewById(R.id.bind_email);
        emailInfo = findViewById(R.id.email_info);

        registerListener();
    }

    private void registerListener() {
        back.setOnClickListener(this);
        setNickName.setOnClickListener(this);
        setAvatar.setOnClickListener(this);
        setAddress.setOnClickListener(this);
        bindEmail.setOnClickListener(this);
        changePassword.setOnClickListener(this);
        loginOut.setOnClickListener(this::onClick);

        //城市选择器的监听事件
        cityPickerView.setOnCityItemClickListener(new OnCityItemClickListener() {
            @Override
            public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {
                User user = new User();
                user.setHome(new City(province.getName(), city.getName(), district.getName()));
                // 更新地址
                new Thread(() -> user.update(ID, new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e != null) {
                            ToastUtils.showMsg(UserCenterActivity.this, "地址上传失败 " + e.getMessage(), 0);
                            Log.e("update_address", e.getMessage() + "");
                        }
                    }
                })).start();
                runOnUiThread(() -> {
                    address.setText(user.getHome().getName());
                });
            }

            @Override
            public void onCancel() {
                super.onCancel();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.set_avatar:
                showAvatarDialog();
                break;
            case R.id.set_address:
                cityPickerView.showCityPicker();
                break;
            case R.id.set_nickname:
                setNicknameByDialog();
                break;
            case R.id.bind_email:
                if (email == null) {
                    //跳转到绑定邮箱
                    startActivity(new Intent(this, BindEmailActivity.class));
                } else {
                    if (!emailVerify) {
                        BmobUser.requestEmailVerify(email, new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e != null) {
                                    Log.e("requestVerify", "" + e.getMessage());
                                    ToastUtils.showMsg(UserCenterActivity.this, "验证信息失败 " + e.getMessage(), 0);
                                } else {
                                    ToastUtils.showMsg(UserCenterActivity.this, "请求验证邮件成功，请到" + email + "邮箱中进行激活。", 1);
                                }
                            }
                        });
                    } else {
                        ToastUtils.showMsg(this, "你已经绑定了！很棒", 0);
                    }
                }
                break;
            case R.id.login_out:
                BmobUser.logOut();
                ToastUtils.showMsg(this, "已退出", 0);
                Intent intent = new Intent();
                intent.putExtra("resultType", Constants.LOGIN_OUT_RESULT);
                this.setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.change_password:
                RegisterActivity.startActivity(this, Constants.REQUEST_USER_CENTER_ACTIVITY);
        }
    }

    /**
     * 设置头像的dialog
     */
    private void showAvatarDialog() {
        @SuppressLint("InflateParams") View avatarView = LayoutInflater.from(this).inflate(R.layout.layout_avatar_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(avatarView);
        RecyclerView mRvAvatar = avatarView.findViewById(R.id.rv_avatar);
        mRvAvatar.setLayoutManager(new GridLayoutManager(this, 3));
        //设置dialog
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss();
        }).setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        //设置适配器，响应点击事件
        mRvAvatar.setAdapter(new AvatarAdapter(this, position -> {
            User user = new User();
            //+1是因为图片是从1开始的
            user.setAvatar(position + 1);
            new Thread(() -> user.update(ID, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e != null) {
                        ToastUtils.showMsg(UserCenterActivity.this, "头像信息上传失败 " + e.getMessage(), 0);
                        Log.e("avatar", "" + e.getMessage());
                    }
                }
            })).start();
            runOnUiThread(() -> {
                avatar.setImageResource(AvatarUtils.avatars.get(user.getAvatar()));
            });
            alertDialog.dismiss();
        }));
    }

    /**
     * 跳出一个dialog来设置昵称
     */
    private void setNicknameByDialog() {
        @SuppressLint("InflateParams") View nicknameView = LayoutInflater.from(this).inflate(R.layout.layout_nickname_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //自定义的dialog
        builder.setView(nicknameView);
        EditText text = nicknameView.findViewById(R.id.et_change_nickname);
        builder.setPositiveButton("确定", (dialog, which) -> {
            //点了确定就把昵称放到原来界面上，并更新用户信息
            String newText = text.getText().toString().trim();
            User user = new User();
            user.setNickName(newText);
            //更新用户信息
            new Thread(() -> user.update(ID, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e != null) {
                        ToastUtils.showMsg(UserCenterActivity.this, "信息上传失败" + e.getMessage(), 0);
                        Log.e("update_nickname", e.getMessage() + "");
                    }
                }
            })).start();
            runOnUiThread(() -> nickname.setText(newText)); })
                .setNegativeButton("取消", (dialog, which) -> dialog.cancel())
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_USER_CENTER_ACTIVITY) {
            if (data == null) {
                Log.e("LoginActivity", "data为空！");
                return;
            }
            if (resultCode == RESULT_OK) {
                ToastUtils.showMsg(this, "修改密码成功", 0);
            }
        } else {
            User user = BmobUser.getCurrentUser(User.class);
            runOnUiThread(() -> {
                if (user.getEmail() != null) {
                    if (!user.getEmailVerified()) {
                        emailInfo.setText("邮箱未激活");
                    } else {
                        emailInfo.setText(user.getEmail());
                    }
                } else {
                    emailInfo.setText(UserCenterActivity.this.getString(R.string.hint_bind_email));
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
