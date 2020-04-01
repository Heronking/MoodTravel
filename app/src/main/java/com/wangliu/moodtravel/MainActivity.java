package com.wangliu.moodtravel;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.wangliu.moodtravel.adapter.InfoAdapter;
import com.wangliu.moodtravel.users.LoginActivity;
import com.wangliu.moodtravel.users.User;
import com.wangliu.moodtravel.users.UserCenterActivity;
import com.wangliu.moodtravel.utils.AMapUtils;
import com.wangliu.moodtravel.utils.AvatarUtils;
import com.wangliu.moodtravel.utils.Constants;
import com.wangliu.moodtravel.utils.ToastUtils;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.wangliu.moodtravel.utils.WeatherUtils.weatherImage;


public class MainActivity extends HasPermissionsActivity implements
        AMapLocationListener
        , View.OnClickListener, AMap.OnPOIClickListener
        , GeocodeSearch.OnGeocodeSearchListener {

    private MapView mMapView;   //视图
    private AMap mAMap; //地图对象

    private AMapLocationClient client = null;   //定位对象

    private String mCity;
    private String mAddress;
    private LatLng mLatLng;

    private Marker marker;

    private DrawerLayout mDr;
    private FloatingActionButton mFBLocation;    //定位按钮
    private FloatingActionButton mFBNavigation;  //导航
    private FloatingActionButton mFBWeather;   //天气
    private Toolbar mTbSearch; //搜索栏
    private NestedScrollView mNsBottomSheet;    //底部信息栏
    private TextView mTvBottomSheetTitle;  //底部的名称
    private TextView mTvBottomSheetDis;    //离目标点的距离
    private TextView mTvTemperature;    //温度

    private NavigationView mNavigationView; //侧滑栏
    private RelativeLayout mReLogin;

    private User user;
    private CircleImageView mAvatar;
    private TextView mNickname;
    private TextView mMessage;


    private boolean isFirst = true;  //是否是第一次定位标志，定位成功后不再持续弹出数据或移动地图
    private boolean isFirstFailed = false;   //第一次定位是否失败
    private boolean isDataBack = false;   //是否有数据返回

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.map);  //地图控件引用
        mMapView.onCreate(savedInstanceState);  //实现地图的生命周期管理


        initMap();

        initView();

        initLocation();

        updateHeadUI();

    }

    /**
     * 更新用户UI
     */
    private void updateHeadUI() {
        if (user == null) {
            mMessage.setText("点击登录 ~ ");
            mNickname.setText("害没登录呢？");
            mAvatar.setImageResource(R.drawable.icon_unlogin);
        } else {
            mMessage.setText("个人中心");
            if (user.getNickName() == null) {
                mNickname.setText("昵称未设置");
            } else {
                mNickname.setText(user.getNickName());
            }
            if (user.getAvatar() != null) {
                mAvatar.setImageResource(AvatarUtils.avatars.get(user.getAvatar()));
            } else {
                mAvatar.setImageResource(R.drawable.icon_unlogin);
            }
        }
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        client = new AMapLocationClient(this);
        //定位设置
        AMapLocationClientOption option = new AMapLocationClientOption();   //定位参数
        client.setLocationListener(this::onLocationChanged);    //设置定位监听
        //配置定位参数

        //定位模式为高精度模式
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setOnceLocation(false);  //单次定位关闭
        option.setInterval(2000);   //连续定位，单位为ms
        option.setNeedAddress(true);    //返回地址信息
        option.setMockEnable(true);     //模拟位置结果，默认为true
        option.setHttpTimeOut(20000);   //设置超时时间，超时后定位停止
        option.setLocationCacheEnable(true);    //缓存开启

        /**  设置定位场景为出行  **/
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        if (client != null) {
            client.setLocationOption(option);
            client.stopLocation();
            client.startLocation();     //先停止再调用start保证场景模式生效
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);   //改一下状态栏
//        getWindow().setStatusBarColor(Color.TRANSPARENT);   //不会变成半透明

        mDr = findViewById(R.id.dr);

        mFBLocation = findViewById(R.id.fb_location);
        mFBNavigation = findViewById(R.id.fb_navigation);
        mFBWeather = findViewById(R.id.fb_weather);

        mTbSearch = findViewById(R.id.top_search);
        setSupportActionBar(mTbSearch);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);  //侧滑栏按钮
            actionBar.setDisplayShowTitleEnabled(false);    //隐藏应用名
            actionBar.setHomeAsUpIndicator(R.drawable.icon_menu);

        }

        mTvBottomSheetTitle = findViewById(R.id.tv_title);
        mTvBottomSheetDis = findViewById(R.id.tv_dis);
        mTvTemperature = findViewById(R.id.temperature);

        mNsBottomSheet = findViewById(R.id.ns_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(mNsBottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        //把header和menu加进来
        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.inflateHeaderView(R.layout.layout_nv_header);
        mNavigationView.inflateMenu(R.menu.menu_navigation);

        //获取顶部布局
        View view = mNavigationView.getHeaderView(0);
        mAvatar = view.findViewById(R.id.image_head);
        mNickname = view.findViewById(R.id.nickname);
        mReLogin = view.findViewById(R.id.rl_login);
        mMessage = view.findViewById(R.id.message);

        registerLayoutListener();

    }

    /**
     * 初始化地图
     */
    private void initMap() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();  //地图对象
        }
        //初始化key，和用户
        Bmob.initialize(this, this.getString(R.string.Bmob_appkey));
        user = BmobUser.getCurrentUser(User.class);

        mAMap.setTrafficEnabled(true);  //显示交通
        registerMapListener();
        initMyLocation();   //当前位置蓝点样式
        initUi();   //地图界面ui

        new Thread(() -> {  //这里有异步回调，放到线程里面
            searchWeather(mCity);    //实时天气
        }).start();
    }

    /**
     * 注册地图界面组件监听器
     */
    private void registerLayoutListener() {
        mFBLocation.setOnClickListener(this);
        mTbSearch.setOnClickListener(this);
        mFBWeather.setOnClickListener(this);
        mReLogin.setOnClickListener(this);
        mFBNavigation.setOnClickListener(this::onClick);

        mNavigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.satellite:
                    mAMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                    mDr.closeDrawers();
                    break;
                case R.id.night:
                    mAMap.setMapType(AMap.MAP_TYPE_NIGHT);
                    mDr.closeDrawers();
                    break;
                case R.id.common:
                    mAMap.setMapType(AMap.MAP_TYPE_NORMAL);
                    mDr.closeDrawers();
                    break;
            }
            return false;
        });
    }

    /**
     * 注册地图监听器
     */
    private void registerMapListener() {
        mAMap.setMyLocationEnabled(true);   //显示定位蓝点，并可触发定位
        mAMap.setOnPOIClickListener(this::onPOIClick);

    }


    /**
     * 初始化地图界面ui
     */
    private void initUi() {
        UiSettings settings = mAMap.getUiSettings();    //地图界面UI对象
        settings.setLogoBottomMargin(-50);  //将高德地图的logo放到界面外
        settings.setZoomControlsEnabled(false); //隐藏缩放按钮
        settings.setMyLocationButtonEnabled(false);  //隐藏定位按钮，默认是隐藏的
        settings.setCompassEnabled(false);   //指南针
        settings.setScaleControlsEnabled(false); //比例尺
        settings.setTiltGesturesEnabled(true);  //地图倾斜手势

    }


    /**
     * 设置定位模式
     */
    private void initMyLocation() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();    //初始化定位蓝点样式
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);  //连续定位、不会将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
        myLocationStyle.interval(2000); //连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.strokeColor(Color.TRANSPARENT)
                .radiusFillColor(Color.TRANSPARENT); //边框透明,填充透明
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromBitmap(BitmapFactory.decodeResource(
                        getResources(), R.drawable.local)));
        mAMap.setMyLocationStyle(myLocationStyle);
    }

    /**
     * 根据poi id搜索
     *
     * @param id poi的id
     */
    private void idSearch(String id) {
        PoiSearch poiSearch = new PoiSearch(this, null);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {
                //给id搜索结果处理一下
                if (i == AMapException.CODE_AMAP_SUCCESS) {
                    if (poiItem != null) {
                        mCity = poiItem.getCityName();  //更新一下自己的位置信息
                    }
                }
            }
        });
        poiSearch.searchPOIIdAsyn(id);  //发送查询请求，搜索id
    }

    /**
     * 在地图上加一个标记
     * 弹出目标距离信息
     *
     * @param latLng 点击位置的坐标
     * @param title  目标地点的标题
     */
    private void addMarker(final LatLng latLng, String title) {
        if (marker != null) {
            marker.remove();
        }
        getAddressByLatLng(latLng); //找地址
        mAMap.setInfoWindowAdapter(new InfoAdapter(this));  //声明信息窗体
        setMarkerInfo(latLng, title, null);   //给marker整点信息
        marker.showInfoWindow();    //展示窗体

        showBottomSheet(latLng, title);    //弹出屏幕底部的东西
    }

    /**
     * 给marker设置信息
     *
     * @param latLng  经纬度
     * @param title   标题
     * @param snippet 地址
     */
    private void setMarkerInfo(LatLng latLng, String title, String snippet) {
        marker = mAMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(false)
                .title(title)
                .snippet(snippet)
                .anchor(0.5f, 1.5f) //锚点偏移，v向左，v1向上
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_unfocused)));   //设置不可拖动
        if (snippet == null) {
            marker.setSnippet("逆地址编码中，没变化就是失败了。。");
        }
    }

    /**
     * 重载一下加 marker的方法
     *
     * @param latLng  目标经纬度
     * @param title   标题
     * @param snippet 具体地址
     */
    private void addMarker(LatLng latLng, String title, String snippet) {
        if (marker != null) {
            marker.remove();
        }
        mAMap.setInfoWindowAdapter(new
                InfoAdapter(this));
        setMarkerInfo(latLng, title, snippet);  //整点信息
        marker.showInfoWindow();    //显示窗体

        showBottomSheet(latLng, title);
    }

    /**
     * 展示屏幕下方的bottom sheet
     * 计算距离
     *
     * @param latLng 计算地址用的经纬度
     * @param title  当前marker的标题
     */
    private void showBottomSheet(LatLng latLng, String title) {
        BottomSheetBehavior behavior = BottomSheetBehavior.from(mNsBottomSheet);
        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {   //如果是折叠的
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);  //展开
        }

        mTvBottomSheetTitle.setText(title);

        if (mLatLng != null) {
            float dis = com.amap.api.maps.AMapUtils.calculateLineDistance(mLatLng, latLng);
            mTvBottomSheetDis.setText(String.format("%s"
                    , "距离" + AMapUtils.convertToLengthStr(dis)));
        } else {
            mTvBottomSheetDis.setText("计算失败，换个地儿再试试");
        }
    }

    /**
     * 查一下本地天气，然后更新图标和天气
     *
     * @param s 城市名
     */
    private void searchWeather(String s) {
        //天气预报查询
        WeatherSearchQuery query = new WeatherSearchQuery(s, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch search = new WeatherSearch(this);
        search.setQuery(query);
        search.setOnWeatherSearchListener(new WeatherSearch.OnWeatherSearchListener() {
            @Override
            public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
                if (i == AMapException.CODE_AMAP_SUCCESS) {
                    if (localWeatherLiveResult != null && localWeatherLiveResult.getLiveResult() != null) {
                        LocalWeatherLive live = localWeatherLiveResult.getLiveResult();
                        mTvTemperature.setText(live.getTemperature());
                        mFBWeather.setBackgroundResource(weatherImage.get(live.getWeather()));
                    } else {
                        mTvTemperature.setText("N/A");
                    }
                } else {
                    mTvTemperature.setText("N/A");
                }
            }

            @Override
            public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {
            }
        });
        search.searchWeatherAsyn(); //异步查询
    }


    /**
     * 按钮点击事件处理
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fb_location:
                if (mLatLng != null) {
                    //移动到定位点，有动画效果
                    mAMap.animateCamera(CameraUpdateFactory.newLatLng(mLatLng));
                    break;
                } else {    //可以选择使用Snackbar
                    ToastUtils.showMsg(this, "定位失败！", Toast.LENGTH_SHORT);
                }
                break;
            case R.id.top_search:
                //requestCode是MainActivity，回调时用来识别intent中的数据
                PoiSearchActivity.startActivity(this, Constants.REQUEST_MAIN_ACTIVITY, mCity);
                break;

            case R.id.fb_weather:
                WeatherActivity.startActivity(this, mCity, mAddress);
                break;
            case R.id.fb_navigation:
                if (marker != null) {
                    RouteActivity.startActivity(this, mLatLng
                            , marker.getPosition(), mCity, mAddress, marker.getTitle());
                } else {
                    RouteActivity.startActivity(this, mLatLng
                            , null, mCity, mAddress, null);
                }
                break;
            case R.id.rl_login:
                if (user == null) {
                    LoginActivity.startActivity(this, Constants.REQUEST_MAIN_ACTIVITY);
                } else {
                    UserCenterActivity.startActivity(this, Constants.REQUEST_MAIN_ACTIVITY);
                }
        }
    }


    /**
     * 地图点击事件监听处理
     *
     * @param poi
     */
    @Override
    public void onPOIClick(Poi poi) {
        if (marker != null && marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
        }

        addMarker(poi.getCoordinate(), poi.getName());
        mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(poi.getCoordinate().latitude
                        , poi.getCoordinate().longitude), 16
        ));

    }

    /**
     * 开始逆地址编码
     *
     * @param latLng 要转换的坐标
     */
    private void getAddressByLatLng(LatLng latLng) {
        final GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        final RegeocodeQuery query = new RegeocodeQuery(
                AMapUtils.convertToLatLonPoint(latLng), 500f,
                GeocodeSearch.AMAP
        );  //逆地址编码坐标点、查询范围、坐标类型
        geocodeSearch.setOnGeocodeSearchListener(this);

        geocodeSearch.getFromLocationAsyn(query);

    }

    /**
     * 逆地址编码异步回调结果
     *
     * @param regeocodeResult 回调的结果
     * @param i               为1000时表示成功
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        synchronized (this) {
            if (i == AMapException.CODE_AMAP_SUCCESS) {
                String address = regeocodeResult.getRegeocodeAddress().getFormatAddress();

                try {
                    Thread.sleep(250);  //休息250ms，五毛一条
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (address == null) {    //没有具体位置
                    marker.setSnippet("没找到，自己看着办吧");
                } else {
                    marker.setSnippet(address);
                }
                if (marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                    marker.showInfoWindow();
                }
            }
        }

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
    }

    /**
     * 回调数据处理
     *
     * @param requestCode 返回的activity标志码
     * @param resultCode  数据标志码
     * @param data        intent数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_MAIN_ACTIVITY && resultCode == RESULT_OK) {   //判断是否为此Activity的数据

            if (data == null) {
                Log.e("null null", requestCode + " " + resultCode);
                return;
            }
            if (Constants.POIITEM_RESULT == data.getIntExtra("resultType", 1)) {    //找一下关于poiItem的数据
                PoiItem poiItem = data.getParcelableExtra("result");
                if (poiItem != null) {
                    mCity = poiItem.getCityName();
                    addMarker(AMapUtils.convertToLatLng(poiItem.getLatLonPoint())
                            , poiItem.getTitle(), poiItem.getSnippet());
                    isDataBack = true;
                }
            }
            if (Constants.POITIP_RESULT == data.getIntExtra("resultType", 1)) {    //找一下关于poiItem的数据{
                Tip tip = data.getParcelableExtra("result");
                if (tip != null) {
                    idSearch(tip.getPoiID());
                    addMarker(AMapUtils.convertToLatLng(tip.getPoint())
                            , tip.getName(), tip.getAddress());
                    isDataBack = true;
                }
            }
            if (Constants.LOGIN_RESULT == data.getIntExtra("resultType", 1)) {
                user = BmobUser.getCurrentUser(User.class);
//                    Log.e("Login", "登录成功了");
                updateHeadUI();
            }
            if (Constants.LOGIN_OUT_RESULT == data.getIntExtra("resultType", 1)) {
                user = BmobUser.getCurrentUser(User.class);
//                    Log.e("Login_out", "退出登录了");
                updateHeadUI();
            }

        }
    }

    /**
     * 搜索栏左边的导航栏点击事件，展开导航栏
     * 在屏幕左边界向右侧滑也能打开
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDr.openDrawer(GravityCompat.START);
        }
        return true;
    }

    /**
     * 定位改变的回调
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation.getErrorCode() == 0) {
            mLatLng = AMapUtils.convertToLatLng(aMapLocation);
            mCity = aMapLocation.getCity();
            mAddress = aMapLocation.getPoiName();
            if (isFirst || isFirstFailed) {
                isFirst = false; //拖动地图时，不会再重新回到定位点
                isFirstFailed = false;
//                mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16f));
            }
        } else {
            if (isFirst) {  //定位失败了
                isFirst = false;
                isFirstFailed = true;   //下次再去尝试定位
                Snackbar.make(mMapView, "定位失败了, 错误码：" + aMapLocation.getErrorCode(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }


    //生命周期重写

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        if (isDataBack && marker != null) { //返回此页面时回调方法，如果有数据，移动地图中心
            mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16));
            isDataBack = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMapView != null) { //销毁地图
            mMapView.onDestroy();
        }
        client.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mMapView.onSaveInstanceState(outState);
    }
}

