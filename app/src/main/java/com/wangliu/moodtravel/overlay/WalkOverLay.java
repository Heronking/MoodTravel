package com.wangliu.moodtravel.overlay;


import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkStep;
import com.wangliu.moodtravel.utils.AMapUtils;

import java.util.List;

public class WalkOverLay extends RouteOverlay {

    private PolylineOptions polylineOptions = null;
    private BitmapDescriptor walkDescriptor;
    private WalkPath walkPath;

    /**
     * 构造函数
     * 创建步行路线图层
     *
     * @param context  当前activity
     * @param aMap     地图对象
     * @param walkPath 步行路线规划方案
     * @param start    起点
     * @param end      终点
     */
    public WalkOverLay(Context context, AMap aMap, WalkPath walkPath, LatLonPoint start, LatLonPoint end) {
        super(context);
        super.aMap = aMap;
        this.walkPath = walkPath;
        startLatLng = AMapUtils.convertToLatLng(start);
        endLatLng = AMapUtils.convertToLatLng(end);
    }

    /**
     * 把路线添加到地图上
     */
    public void addToMap() {

        initPolyLine();

        List<WalkStep> stepList = walkPath.getSteps();

        for (WalkStep step : stepList) {
            LatLng latLng = AMapUtils.convertToLatLng(step.getPolyline().get(0));
            showMarker(step, latLng);
            polylineOptions.addAll(AMapUtils.convertLatLngList(step.getPolyline()));
        }

        addStartEndMarker();    //添加起点和终点
        addPolyline(polylineOptions);  //展示路线
    }

    /**
     * 根据路线添加marker到地图上
     * @param step
     * @param latLng
     */
    private void showMarker(WalkStep step, LatLng latLng) {
        addMarkerList(new MarkerOptions()
                .position(latLng)
                .title("方向：" + step.getAction() + "\n道路：" + step.getRoad())
                .snippet(step.getInstruction())
                .visible(isIconVisible)
                .anchor(0.5f, 1f)
                .icon(walkDescriptor));     //展示marker
    }

    /**
     * 初始化画笔
     */
    private void initPolyLine() {
        if (walkDescriptor == null) {
            walkDescriptor = getWalkBitmap();
        }
        polylineOptions = new PolylineOptions();
        polylineOptions.color(getColorForWalk()).width(getWidth());
    }
}
