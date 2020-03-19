package com.wangliu.moodtravel.overlay;


import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideStep;
import com.wangliu.moodtravel.utils.AMapUtils;

import java.util.List;

public class RideOverlay extends RouteOverlay {

    private PolylineOptions polylineOptions;
    private BitmapDescriptor rideDescriptor;
    private RidePath ridePath;

    /**
     * 构造函数
     * 创建步行路线图层
     *
     * @param context  当前activity
     * @param aMap     地图对象
     * @param ridePath 步行路线规划方案
     * @param start    起点
     * @param end      终点
     */
    public RideOverlay(Context context, AMap aMap, RidePath ridePath, LatLonPoint start, LatLonPoint end) {
        super(context);
        super.aMap = aMap;
        this.ridePath = ridePath;
        startLatLng = AMapUtils.convertToLatLng(start);
        endLatLng = AMapUtils.convertToLatLng(end);
    }

    /**
     * 把路线添加到地图上
     */
    public void addToMap() {

        initPolyLine();

        List<RideStep> stepList = ridePath.getSteps();

        for (RideStep step : stepList) {
            LatLng latLng = AMapUtils.convertToLatLng(step.getPolyline().get(0));
            showMarker(step, latLng);
            polylineOptions.addAll(AMapUtils.convertLatLngList(step.getPolyline()));
        }

        addPolyline(polylineOptions);  //展示路线
    }

    /**
     * 根据路线添加marker到地图上
     * @param step
     * @param latLng
     */
    private void showMarker(RideStep step, LatLng latLng) {
        addMarkerList(new MarkerOptions()
                .position(latLng)
                .title("方向：" + step.getAction() + "\n道路：" + step.getRoad())
                .snippet(step.getInstruction())
                .visible(isIconVisible)
                .anchor(0.5f, 1f)
                .icon(rideDescriptor));     //展示marker
    }

    /**
     * 初始化画笔
     */
    private void initPolyLine() {
        if (rideDescriptor == null) {
            rideDescriptor = getRideBitmap();
        }
        polylineOptions = null;
        polylineOptions = new PolylineOptions();
        polylineOptions.color(getColorForRide()).width(getWidth());
    }
}
