package com.wangliu.moodtravel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.wangliu.moodtravel.adapter.BusRouteAdapter;
import com.wangliu.moodtravel.adapter.PathPagerAdapter;
import com.wangliu.moodtravel.overlay.BusOverlay;
import com.wangliu.moodtravel.utils.AMapUtils;

import java.util.ArrayList;
import java.util.List;

public class BusRouteActivity extends AppCompatActivity {

    private MapView mapView;
    private AMap aMap;

    private NestedScrollView bottomSheet;
    private ViewPager mViewPager;
    private RecyclerView mRvBusDetails;
    private ImageView mIvBack;


    private BottomSheetBehavior behavior;
    private int sheetHeight;    //bottomSheet的高度
    private int maxHeight = 1100;     //最大高度

    private BusRouteResult mResult;
    private BusOverlay mBusOverlay;

    public static void startActivity(Context context, BusRouteResult result, int pos) {
        Intent intent = new Intent(context, BusRouteActivity.class);
        intent.putExtra("result", result);
        intent.putExtra("pos", pos);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route);
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        initMap();
        initView();

        initBottomSheet();
        initPage();

    }

    /**
     * 注册控件
     */
    private void initView() {
        bottomSheet = findViewById(R.id.bus_path_bottom_sheet);
        mViewPager = findViewById(R.id.bus_view_page);
        mRvBusDetails = findViewById(R.id.rv_bus_path);
        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(v -> this.finish());
    }

    /**
     * 初始化在bottomSheet里面的公交信息页面
     */
    private void initPage() {
        int pos = getIntent().getIntExtra("pos", 0);
        List<View> list = new ArrayList<>();
        for (BusPath path: mResult.getPaths()) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_bus_page,null);
            TextView title = view.findViewById(R.id.bus_path_title);
            TextView details = view.findViewById(R.id.bus_path_details);
            title.setText(AMapUtils.getBusPathTitle(path));
            details.setText(AMapUtils.getBusDetails(path));
            list.add(view);
        }
        PathPagerAdapter adapter = new PathPagerAdapter(list);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(pos);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                showBusRoute(mResult, mResult.getPaths().get(position));
                mRvBusDetails.setAdapter(new BusRouteAdapter(BusRouteActivity.this
                        , mResult.getPaths().get(position).getSteps()));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvBusDetails.setLayoutManager(manager);
        mRvBusDetails.setAdapter(new BusRouteAdapter(this
                , mResult.getPaths().get(pos).getSteps()));
    }

    private void initMap() {
        if (mapView != null) {
            aMap = mapView.getMap();
        }
        aMap.setTrafficEnabled(true);
        aMap.showIndoorMap(true);   //室内地图

        UiSettings settings = aMap.getUiSettings();
        settings.setMyLocationButtonEnabled(false); //隐藏定位按钮
        settings.setLogoBottomMargin(-50);  //隐藏log
        settings.setCompassEnabled(false);  //隐藏指南针
        settings.setZoomControlsEnabled(false); //隐藏缩放按钮

        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.local));
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        aMap.setMyLocationStyle(locationStyle);

        aMap.setOnMapLoadedListener(() -> {
            showBusRoute(mResult, mResult.getPaths().get(0));  //地图加载完成时，将公交的路线显示出来
        });
    }

    private void showBusRoute(BusRouteResult result, BusPath path) {
        aMap.clear();
        mBusOverlay = new BusOverlay(this, aMap, path, result.getStartPos(), result.getTargetPos());
        mBusOverlay.setIconVisible(true);  //隐藏marker
        mBusOverlay.removeFromMap();
        mBusOverlay.addToMap();
        mBusOverlay.moveCameraWithBottom(sheetHeight);
    }

    private void initBottomSheet() {
        sheetHeight = getResources().getDimensionPixelSize(R.dimen.sheet_height);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mBusOverlay.moveCameraWithBottom(behavior.getPeekHeight());
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
//                        Log.e("sheeet:", ""+bottomSheet.getHeight());
                        if (bottomSheet.getHeight() < maxHeight) {
                            sheetHeight = bottomSheet.getHeight();
                        }
                        mBusOverlay.moveCameraWithBottom(sheetHeight);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                Log.e("offset: ", mOffset+"");
            }
        });

        if (getIntent().hasExtra("result")) {
            mResult = getIntent().getParcelableExtra("result");
        } else {
            finish();
        }
    }



    //生命周期

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
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
