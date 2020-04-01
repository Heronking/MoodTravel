package com.wangliu.moodtravel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviTheme;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.Path;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.wangliu.moodtravel.adapter.BusResultAdapter;
import com.wangliu.moodtravel.adapter.DriveAdapter;
import com.wangliu.moodtravel.adapter.RideAdapter;
import com.wangliu.moodtravel.adapter.WalkAdapter;
import com.wangliu.moodtravel.overlay.DriveOverlay;
import com.wangliu.moodtravel.overlay.RideOverlay;
import com.wangliu.moodtravel.overlay.WalkOverLay;
import com.wangliu.moodtravel.utils.AMapUtils;
import com.wangliu.moodtravel.utils.Constants;
import com.wangliu.moodtravel.utils.ToastUtils;
import com.wangliu.moodtravel.widget.LoadingDialog;

import java.util.Objects;


public class RouteActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener, View.OnClickListener
        , RouteSearch.OnRouteSearchListener {

    private static final String TAB_WALK = "步行";
    private static final String TAB_RIDE = "骑行";
    private static final String TAB_BUS = "公交";
    private static final String TAB_DRIVE = "驾车";
    private static final int MODE_DRIVE = 3;
    private static final int MODE_BUS = 2;
    private static final int MODE_RIDE = 1;
    private static final int MODE_WALK = 0;

    private String mCity;

    private MapView mapView;    //地图视图
    private AMap mAMap;     //地图对象

    private LatLng startLatLng;
    private LatLng endLatLng;

    private RouteSearch search;
    private LoadingDialog loadingDialog;

    private TextView mTvStart;
    private TextView mTvEnd;

    private LinearLayout mLLPathDetails, mLLPathDetails1, mLLPathDetails2;
    private TextView mTvDis, mTvDis1, mTvDis2;
    private TextView mTvTime, mTvTime1, mTvTime2;

    private TextView mTvTips;
    private TextView mTvTrafficLight;
    private RecyclerView mRvPathDetails;
    private RecyclerView mRvBusResult;
    private ImageButton mIBtnSwap;

    private Button mBtnNavigation;
    private TextView mTvNavigation;

    private NestedScrollView mBottomSheet;
    private LinearLayout mLayoutSheetHead;

    private AppBarLayout appBarLayout;
    private TabLayout tabLayout;

    private BottomSheetBehavior behavior;

    private int changeTextFlag;

    private int routeMode = 0;

    private WalkRouteResult mWalkResult;
    private RideRouteResult mRideResult;

    private DriveRouteResult mDriveResult;


    /**
     * 重载方法startActivity
     * 用于跳转活动并传递数据
     *
     * @param context         上下文
     * @param current         当前经纬度
     * @param destination     目标经纬度
     * @param mCity           当前城市
     * @param destinationCity 目标城市
     */
    public static void startActivity(Context context, LatLng current
            , LatLng destination, String mCity, String currentName, String destinationCity) {
        Intent intent = new Intent(context, RouteActivity.class);

        intent.putExtra("currentCity", mCity);    //当前城市放进去
        if (current != null) {
            intent.putExtra("hasCurrent", true);
            intent.putExtra("current", current);    //当前经纬度
            intent.putExtra("currentName", currentName);
        } else {
            intent.putExtra("hasCurrent", false);
        }
        if (destination != null) {
            intent.putExtra("hasDestination", true);
            intent.putExtra("destinationCity", destinationCity);
            intent.putExtra("destination", destination);
        } else {
            intent.putExtra("hasDestination", false);
        }
        context.startActivity(intent);  //起飞
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        mapView = findViewById(R.id.route_map); //视图注册
        mapView.onCreate(savedInstanceState);   //创建地图


        initMap();
        intView();

        getIntentExtra();


    }

    /**
     * 获得intent中的数据
     * 设置好路线相关的数据
     */
    private void getIntentExtra() {
        Intent intent = getIntent();
        mCity = intent.getStringExtra("currentCity");
        if (intent.getBooleanExtra("hasCurrent", false)) {
            startLatLng = Objects.requireNonNull(intent.getParcelableExtra("current"));
            mTvStart.setText(intent.getStringExtra("currentName"));
        }

        if (intent.getBooleanExtra("hasDestination", false)) {
            endLatLng = Objects.requireNonNull(intent.getParcelableExtra("destination"));
            mTvEnd.setText(intent.getStringExtra("destinationCity"));
        }
        showTipsText();
    }

    /**
     * 初始化视图
     */
    private void intView() {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);   //不会变成半透明

        mTvStart = findViewById(R.id.tv_from);
        mTvEnd = findViewById(R.id.tv_to);

        mLLPathDetails = findViewById(R.id.layout_path);
        mLLPathDetails1 = findViewById(R.id.layout_path1);
        mLLPathDetails2 = findViewById(R.id.layout_path2);
        mTvDis = findViewById(R.id.path_dis);
        mTvDis1 = findViewById(R.id.path_dis1);
        mTvDis2 = findViewById(R.id.path_dis2);
        mTvTime = findViewById(R.id.path_time);
        mTvTime1 = findViewById(R.id.path_time1);
        mTvTime2 = findViewById(R.id.path_time2);

        mTvNavigation = findViewById(R.id.tv_start_navigation);
        mBtnNavigation = findViewById(R.id.btn_start_navigation);

        mTvTips = findViewById(R.id.tv_tips);
//        mTvNavigation = findViewById(R.id.tv_start_navigation);
        mTvTrafficLight = findViewById(R.id.path_trafficLight);
        mIBtnSwap = findViewById(R.id.btn_swap);
//        mBtnNavigation = findViewById(R.id.btn_start_navigation);

        mRvPathDetails = findViewById(R.id.rv_path_details);
        mRvBusResult = findViewById(R.id.rv_bus_result);

        mBottomSheet = findViewById(R.id.bs_route);
        mLayoutSheetHead = findViewById(R.id.layout_sheet_head);
        tabLayout = findViewById(R.id.tabs);
        Toolbar toolbar = findViewById(R.id.rt_toolbar);

        mLayoutSheetHead.measure(0, 0);

        showTipsText(); //显示提示信息


        mRvBusResult.setLayoutManager(
                new LinearLayoutManager(this)
        );  //recyclerView需要设置manager

        appBarLayout = findViewById(R.id.layout_app_bar);

        setSupportActionBar(toolbar);   //这里要导入androidx的包
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        tabLayout.addTab(tabLayout.newTab().setText(TAB_WALK));
        tabLayout.addTab(tabLayout.newTab().setText(TAB_RIDE));
        tabLayout.addTab(tabLayout.newTab().setText(TAB_BUS));
        tabLayout.addTab(tabLayout.newTab().setText(TAB_DRIVE));
        TabLayout.Tab tabFirst = tabLayout.getTabAt(0);
        if (tabFirst != null) {
            tabFirst.select();  //默认第一个被选中
        }

        registerListener();

    }

    private void showTipsText() {
        if (startLatLng == null && endLatLng == null) {
            mTvTips.setText("请输入起点及终点，开始路线规划");
        } else if (startLatLng == null) {
            mTvTips.setText("请输入起点");
        } else if (endLatLng == null) {
            mTvTips.setText("请输入终点");
        } else {
            mTvTips.setText("出错了，换个方法吧");
        }
        mapView.setVisibility(View.GONE);
        mTvTips.setVisibility(View.VISIBLE);
    }

    private void updateSheetHeight() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mBottomSheet.getLayoutParams();
        if (params != null) {
            params.height = point.y - 351;
//            Log.d("p:", point.y+" "+appBarLayout.getHeight());

            mBottomSheet.setLayoutParams(params);
        }
    }

    private void registerListener() {
        mTvStart.setOnClickListener(this);
        mTvEnd.setOnClickListener(this);
        mIBtnSwap.setOnClickListener(this);
        tabLayout.addOnTabSelectedListener(this);

        mLLPathDetails.setOnClickListener(this);
        mLLPathDetails1.setOnClickListener(this);
        mLLPathDetails2.setOnClickListener(this::onClick);
        mTvNavigation.setOnClickListener(this);
        mBtnNavigation.setOnClickListener(this);

        search = new RouteSearch(this);
        search.setRouteSearchListener(this);    //监听路径搜索结果

        updateSheetHeight();
        initBottomSheet();
    }

    private void initBottomSheet() {

        behavior = BottomSheetBehavior.from(mBottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        behavior.setPeekHeight(getSheetHeadHeight());

        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (appBarLayout.getVisibility() == View.VISIBLE
                        && mRvPathDetails.getVisibility() == View.VISIBLE) {
                    if (slideOffset > 0.5) {
                        mTvNavigation.setVisibility(View.GONE);
                        mBtnNavigation.setVisibility(View.VISIBLE);
//                        Log.e("appbar: ", appBarLayout.getHeight()+"");

                    } else {
                        mTvNavigation.setVisibility(View.VISIBLE);
                        mBtnNavigation.setVisibility(View.GONE);

                    }
                }
            }
        });
        mRvPathDetails.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * 初始化地图
     */
    private void initMap() {
        if (mAMap == null) {
            mAMap = mapView.getMap();
        }
        UiSettings settings = mAMap.getUiSettings();
        settings.setMyLocationButtonEnabled(false); //隐藏定位按钮
        settings.setLogoBottomMargin(-50);  //隐藏log
        settings.setCompassEnabled(false);  //隐藏指南针
        settings.setZoomControlsEnabled(false); //隐藏缩放按钮
//        settings.setScrollGesturesEnabled(false);   //禁用缩放手势

        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.local));
        mAMap.setMyLocationStyle(locationStyle);
    }

    /**
     * 放一个加载条
     */
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            LoadingDialog.Builder builder = new LoadingDialog.Builder(this);
            builder.setMessage("搜索中...").setCanelable(true).setCanelableOutside(true);
            loadingDialog = builder.create();
            loadingDialog.show();
            try {
                Thread.sleep(300);  //转300ms，五毛一条
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 让加载条消失
     */
    private void dismissLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }


    /**
     * 根据tab上点击获取的模式计算路线
     */
    private void calculateRoute() {
        showLoadingDialog();    //休息一下
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                AMapUtils.convertToLatLonPoint(startLatLng)
                , AMapUtils.convertToLatLonPoint(endLatLng)
        ); //fromAndTo包含路径规划的起点和终点
        switch (routeMode) {
            case MODE_WALK:
                RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo);
                search.calculateWalkRouteAsyn(walkRouteQuery);
                break;
            case MODE_RIDE:
                RouteSearch.RideRouteQuery rideRouteQuery = new RouteSearch.RideRouteQuery(fromAndTo);
                search.calculateRideRouteAsyn(rideRouteQuery);
                break;
            case MODE_BUS:
                //RouteSearch.BUS_DEFAULT表示公交查询模式
                // 第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算,1表示计算
                RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo,
                        RouteSearch.BUS_DEFAULT, mCity, 0
                );  //设置参数
//                Log.e("busssss:", fromAndTo + " " + mCity);
                search.calculateBusRouteAsyn(query);    //发起异步查询
                break;
            case MODE_DRIVE:
                //查询方案：多备选，时间最短，距离最短
                RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(fromAndTo
                        , RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SHORTEST
                        , null, null, "");
                search.calculateDriveRouteAsyn(driveRouteQuery);
                break;
            default:
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            dismissLoadingDialog();
            if (walkRouteResult != null && walkRouteResult.getPaths() != null) {

                if (walkRouteResult.getPaths().size() > 0) {
                    setRouteUI();

                    mWalkResult = walkRouteResult;
                    WalkPath path = mWalkResult.getPaths().get(0);   //第一个方案
                    if (path == null) {
                        return;
                    }

                    mRvPathDetails.setAdapter(new WalkAdapter(this, path.getSteps()));
                    mRvPathDetails.setVisibility(View.VISIBLE);

                    drawForWalk(walkRouteResult, path);

                    for (int j = 0; j < walkRouteResult.getPaths().size(); j++) {
                        setPathPlanUI(walkRouteResult.getPaths().get(j), j);
                    }

                    behavior.setPeekHeight(getSheetHeadHeight());

                } else if (walkRouteResult.getPaths() == null) {
                    Snackbar.make(mapView, "没找到路，换个别的", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(mapView, "没找到路，换个别的", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            showTipsText();
            if (i == AMapException.CODE_AMAP_OVER_DIRECTION_RANGE) {
                ToastUtils.showMsg(this, "距离太长了！", Snackbar.LENGTH_SHORT);
            } else {
                ToastUtils.showMsg(this, "出错了！errorCode：" + i, 0);
            }
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            dismissLoadingDialog();
            if (rideRouteResult != null && rideRouteResult.getPaths() != null) {
                if (rideRouteResult.getPaths().size() > 0) {
                    setRouteUI();
                    mRideResult = rideRouteResult;
                    RidePath path = mRideResult.getPaths().get(0);
                    mRvPathDetails.setAdapter(new RideAdapter(this, path.getSteps()));
                    mRvPathDetails.setVisibility(View.VISIBLE);

                    drawForRide(rideRouteResult, rideRouteResult.getPaths().get(0));

                    for (int j = 0; j < rideRouteResult.getPaths().size(); j++) {
                        setPathPlanUI(rideRouteResult.getPaths().get(j), j);
                    }
                    behavior.setPeekHeight(getSheetHeadHeight());
                } else if (rideRouteResult.getPaths() == null) {
                    Snackbar.make(mapView, "自行车不行啊", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(mapView, "要啥自行车", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            showTipsText();
            if (i == AMapException.CODE_AMAP_OVER_DIRECTION_RANGE) {
                ToastUtils.showMsg(this, "距离太长了！", Snackbar.LENGTH_SHORT);
            } else {
                ToastUtils.showMsg(this, "出错了！errorCode：" + i, 0);
            }        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            dismissLoadingDialog();
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    setRouteUI();
                    mDriveResult = driveRouteResult;
                    DrivePath path = driveRouteResult.getPaths().get(0);
                    mRvPathDetails.setAdapter(new DriveAdapter(this, path.getSteps()));
                    mRvPathDetails.setVisibility(View.VISIBLE);

                    mTvTrafficLight.setText(String.format("红绿灯 %s 个"
                            , path.getTotalTrafficlights() + ""));
                    mTvTrafficLight.setVisibility(View.VISIBLE);
                    drawForDrive(driveRouteResult, driveRouteResult.getPaths().get(0));

                    for (int j = 0; j < driveRouteResult.getPaths().size(); j++) {
                        setPathPlanUI(driveRouteResult.getPaths().get(j), j);
                    }
                    behavior.setPeekHeight(getSheetHeadHeight());
                } else if (driveRouteResult.getPaths() == null) {
                    Snackbar.make(mapView, "没找到路", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(mapView, "别开车了", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            showTipsText();
            if (i == AMapException.CODE_AMAP_OVER_DIRECTION_RANGE) {
                ToastUtils.showMsg(this, "距离太长了！", Snackbar.LENGTH_SHORT);
            } else {
                ToastUtils.showMsg(this, "出错了！errorCode：" + i, 0);
            }        }
    }

    /**
     * 搜索公交路线回调
     *
     * @param busRouteResult
     * @param i
     */
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        dismissLoadingDialog();
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (busRouteResult != null && busRouteResult.getPaths() != null) {
                if (busRouteResult.getPaths().size() > 0) {
                    mBottomSheet.setVisibility(View.GONE);
                    mRvBusResult.setAdapter(new BusResultAdapter(this, busRouteResult));
                    mapView.setVisibility(View.INVISIBLE);
                    mRvBusResult.setVisibility(View.VISIBLE);
                    mTvTips.setVisibility(View.GONE);
                } else if (busRouteResult.getPaths() == null) {
                    Snackbar.make(mapView, "没有公交车", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(mapView, "没公交车", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            showTipsText();
            if (i == AMapException.CODE_AMAP_OVER_DIRECTION_RANGE) {
                ToastUtils.showMsg(this, "距离太长了！", Snackbar.LENGTH_SHORT);
            } else {
                ToastUtils.showMsg(this, "出错了！errorCode：" + i, 0);
            }        }
    }

    private int getSheetHeadHeight() {
        mLayoutSheetHead.measure(0, 0);
        return mLayoutSheetHead.getMeasuredHeight();
    }

    private int getAppBarHeight() {
        return appBarLayout.getHeight();
    }

    /**
     * 改变路线方案的界面
     *
     * @param path 路线
     * @param j    方案序号
     */
    private void setPathPlanUI(Path path, int j) {
        String time = AMapUtils.convertToTimeStr(path.getDuration());
        String dis = AMapUtils.convertToLengthStr(path.getDistance());
        if (j == 0) {
            mTvTime.setText(time);
            mTvDis.setText(dis);
            mLLPathDetails.setVisibility(View.VISIBLE);
            mLLPathDetails1.setVisibility(View.GONE);
            mLLPathDetails2.setVisibility(View.GONE);
        } else if (j == 1) {
            mTvTime1.setText(time);
            mTvDis1.setText(dis);
            mLLPathDetails.setVisibility(View.VISIBLE);
            mLLPathDetails1.setVisibility(View.VISIBLE);
            mLLPathDetails2.setVisibility(View.GONE);
        } else if (j == 2) {
            mTvTime2.setText(time);
            mTvDis2.setText(dis);
            mLLPathDetails.setVisibility(View.VISIBLE);
            mLLPathDetails1.setVisibility(View.VISIBLE);
            mLLPathDetails2.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 改变一下显示路线的界面
     */
    private void setRouteUI() {

        if (mRvBusResult.getVisibility() == View.VISIBLE) {
            mRvBusResult.setVisibility(View.GONE);
        }

        mapView.setVisibility(View.VISIBLE);
        mTvNavigation.setVisibility(View.VISIBLE);
        mTvTrafficLight.setVisibility(View.GONE);
        mBottomSheet.setVisibility(View.VISIBLE);
        mTvTips.setVisibility(View.GONE);
    }


    private void drawForWalk(WalkRouteResult walkRouteResult, WalkPath path) {
        mAMap.clear();
        WalkOverLay overLay = new WalkOverLay(this, mAMap, path
                , walkRouteResult.getStartPos(), walkRouteResult.getTargetPos());

        overLay.removeFromMap();    //先清理一下
        overLay.setIconVisible(true);   //设置路线marker可见
        overLay.addToMap(); //画路线
        overLay.moveCameraWithPadding(getAppBarHeight(), getSheetHeadHeight());
    }

    private void drawForRide(RideRouteResult rideRouteResult, RidePath path) {
        mAMap.clear();
        RideOverlay overLay = new RideOverlay(this, mAMap, path
                , rideRouteResult.getStartPos(), rideRouteResult.getTargetPos());

        overLay.setIconVisible(true);   //设置路线marker可见
        overLay.removeFromMap();    //先清理一下
        overLay.addToMap(); //画路线
        overLay.moveCameraWithPadding(getAppBarHeight(), getSheetHeadHeight());
    }

    private void drawForDrive(DriveRouteResult driveRouteResult, DrivePath path) {
        mAMap.clear();
        DriveOverlay overLay = new DriveOverlay(this, mAMap, path
                , driveRouteResult.getStartPos(), driveRouteResult.getTargetPos());

        overLay.setIconVisible(false);   //设置路线marker可见
        overLay.setFullLine(true);
        overLay.removeFromMap();    //先清理一下
        overLay.addToMap(); //画路线
        overLay.moveCameraWithPadding(getAppBarHeight(), getSheetHeadHeight());
    }


    private void setStartAndEndText(LatLng latLng, String text) {
        switch (changeTextFlag) {
            case R.id.tv_from:
                startLatLng = latLng;
                mTvStart.setText(text);
                break;
            case R.id.tv_to:
                endLatLng = latLng;
                mTvEnd.setText(text);
        }
    }

    /**
     * 按返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                finish();
            }
        }
        return false;
    }

    /**
     * 跳转返回后的数据回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == Constants.REQUEST_ROUTE_ACTIVITY) && (resultCode == RESULT_OK)) {
            assert data != null;
            if (data.getIntExtra("resultType", 1)
                    == Constants.POITIP_RESULT) {   //数据类型是tip
                Tip tip = data.getParcelableExtra("result");    //拿出数据
                if (tip != null) {
                    updateCityName(tip);
                    setStartAndEndText(AMapUtils.convertToLatLng(tip.getPoint())
                            , tip.getName());
                }
                showTipsText();
            } else {
                PoiItem item = data.getParcelableExtra("result");
                assert item != null;
                mCity = item.getCityName();
                setStartAndEndText(AMapUtils.convertToLatLng(item.getLatLonPoint())
                        , item.getTitle());
                showTipsText();
            }
            if (startLatLng != null && endLatLng != null) {
                calculateRoute();
            }
        }
    }

    private void updateCityName(Tip tip) {
        PoiSearch poiSearch = new PoiSearch(this, null);
        poiSearch.searchPOIIdAsyn(tip.getPoiID());  //发送查询请求，搜索id
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
    }


    /**
     * 使用高德内置的导航组件进行导航
     */
    private void startNavigation() {
        AmapNaviType type = null;   //导航的类型
        if (routeMode == MODE_DRIVE) {
            type = AmapNaviType.DRIVER;
        } else if (routeMode == MODE_RIDE) {
            type = AmapNaviType.RIDE;
        } else if (routeMode == MODE_WALK) {
            type = AmapNaviType.WALK;
        }
        if (startLatLng != null && endLatLng != null && type != null) {
            if (startLatLng == endLatLng) {
                ToastUtils.showMsg(this, "你在原地还要导航啊？", 0);
            } else {
//                        GPSNavigationActivity.startActivity(this, startLatLng, mCity, endLatLng, mTvEnd.getText().toString(), type);
                Poi start = new Poi(mTvStart.getText().toString(), startLatLng, "");
                Poi end = new Poi(mTvEnd.getText().toString(), endLatLng, "");
                AmapNaviParams params = new AmapNaviParams(start, null, end, type, AmapPageType.NAVI);
                params.setTheme(AmapNaviTheme.WHITE);   //主题选择白色
                params.setUseInnerVoice(true);  //使用内置语音播报
                params.setMultipleRouteNaviMode(true);  //多路线，只支持驾车
                params.setNeedDestroyDriveManagerInstanceWhenNaviExit(true);    //退出导航后销毁导航实例

                //跳转到导航页面，直接开始导航
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, null);
            }
        }
    }

    /**
     * 点击事件！
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_from:
                PoiSearchActivity.startActivity(RouteActivity.this
                        , Constants.REQUEST_ROUTE_ACTIVITY, mCity);
                changeTextFlag = R.id.tv_from;

                break;
            case R.id.tv_to:
                PoiSearchActivity.startActivity(RouteActivity.this
                        , Constants.REQUEST_ROUTE_ACTIVITY, mCity);
                changeTextFlag = R.id.tv_to;

                break;
            case R.id.btn_swap:
                String temp = mTvStart.getText().toString();
                mTvStart.setText(mTvEnd.getText());
                mTvEnd.setText(temp);
                LatLng tempLatLng = startLatLng;
                startLatLng = endLatLng;
                endLatLng = tempLatLng;
                if (startLatLng != null && endLatLng != null) {
                    calculateRoute();
                }
                showTipsText();
                break;
            case R.id.btn_start_navigation:
            case R.id.tv_start_navigation:

                startNavigation();

                break;
            case R.id.layout_path:
                onPathPlanClick(0);
                break;
            case R.id.layout_path1:
                onPathPlanClick(1);
                break;
            case R.id.layout_path2:
                onPathPlanClick(2);
        }
    }

    /**
     * 方案选择事件处理
     * @param i
     */
    private void onPathPlanClick(int i) {
        switch (routeMode) {
            case MODE_WALK:
                if (mWalkResult != null) {
                    mRvPathDetails.setAdapter(
                            new WalkAdapter(this, mWalkResult.getPaths().get(i).getSteps()));
                    drawForWalk(mWalkResult, mWalkResult.getPaths().get(i));
                } else {
                    Log.e("空指针：", "起点：" + startLatLng + " 终点：" + endLatLng);
                }
                break;
            case MODE_RIDE:
                mRvPathDetails.setAdapter(
                        new RideAdapter(this, mRideResult.getPaths().get(i).getSteps()));
                drawForRide(mRideResult, mRideResult.getPaths().get(i));
                break;
            case MODE_DRIVE:
                mTvTrafficLight.setText(String.format("红绿灯 %s 个"
                        , mDriveResult.getPaths().get(i).getTotalTrafficlights() + ""));
                mTvTrafficLight.setVisibility(View.VISIBLE);
                mRvPathDetails.setAdapter(
                        new DriveAdapter(this, mDriveResult.getPaths().get(i).getSteps()));
                drawForDrive(mDriveResult, mDriveResult.getPaths().get(i));
        }
    }


    /**
     * 选中tab的回调
     *
     * @param tab
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getText() != null) {
            CharSequence text = tab.getText();
            if (TAB_WALK.contentEquals(text)) {
                routeMode = MODE_WALK;
            } else if (TAB_RIDE.contentEquals(text)) {
                routeMode = MODE_RIDE;
            } else if (TAB_BUS.contentEquals(text)) {
                routeMode = MODE_BUS;
            } else if (TAB_DRIVE.contentEquals(text)) {
                routeMode = MODE_DRIVE;
            }
            if (startLatLng != null && endLatLng != null) {
                calculateRoute();
            }

        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    /**
     * 按下返回键
     * 回到上一个活动并回调该活动的onActivityResult()
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        super.addContentView(view, params);
    }

    //生命周期重写

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
