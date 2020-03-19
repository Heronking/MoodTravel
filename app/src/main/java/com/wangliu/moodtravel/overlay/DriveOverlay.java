package com.wangliu.moodtravel.overlay;


import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.TMC;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.utils.AMapUtils;

import java.util.ArrayList;
import java.util.List;

public class DriveOverlay extends RouteOverlay {

    private PolylineOptions polylineOptions, colorPolylineOptions;
    private DrivePath drivePath;
    private List<TMC> tmcs; //交通信息

    private boolean isFullLine = true;  //交通拥堵

    /**
     * 构造函数
     * 创建步行路线图层
     *
     * @param context   当前activity
     * @param aMap      地图对象
     * @param drivePath 步行路线规划方案
     * @param start     起点
     * @param end       终点
     */
    public DriveOverlay(Context context, AMap aMap, DrivePath drivePath, LatLonPoint start, LatLonPoint end) {
        super(context);
        super.aMap = aMap;
        this.drivePath = drivePath;
        startLatLng = AMapUtils.convertToLatLng(start);
        endLatLng = AMapUtils.convertToLatLng(end);
    }


    public void setFullLine(boolean fullLine) {
        isFullLine = fullLine;
    }




    private int getColor(String traffic) {
//        Log.e("路况：", traffic);
        switch (traffic) {
            case "畅通":
                return R.color.colorGreen;
            case "拥堵":
                return R.color.colorRed;
            case "缓行":
                return R.color.colorLightYellow;
            case "严重拥堵":
                return R.color.colorJam;
            default:
                return R.color.colorSmallBlue;
        }
    }




    /**
     * 把路线添加到地图上
     */
    public void addToMap() {

        initPolyLine();

        if (aMap == null || drivePath == null) return;

        tmcs = new ArrayList<>();
        List<DriveStep> stepList = drivePath.getSteps();
//        polylineOptions.add(startLatLng);   //从起点开始

        for (DriveStep step : stepList) {
            List<LatLonPoint> points = step.getPolyline();
            tmcs.addAll(step.getTMCs());
            showMarker(step, AMapUtils.convertToLatLng(points.get(0)));

            for (LatLonPoint point : points) {
                polylineOptions.add(AMapUtils.convertToLatLng(point));
            }

        }
//        polylineOptions.add(endLatLng);
        if (startMarker != null) {
            startMarker.remove();
            startMarker = null;
        }
        if (endMarker != null) {
            endMarker.remove();
            endMarker = null;
        }
        addStartEndMarker();    //添加起点和终点
//        showWayPointMarkers();    //添加途径点

        if (isFullLine && tmcs.size() > 0) {
            colorRouteByTraffic();  //根据交通情况给画笔上色
            addPolyline(colorPolylineOptions);  //画出路线图
        } else {
            addPolyline(polylineOptions);
        }
    }

    /**
     * 根据交通拥堵情况上色
     */
    private void colorRouteByTraffic() {

        if (aMap == null || tmcs == null || tmcs.size() < 1) {
            return;
        }

        colorPolylineOptions = new PolylineOptions();
        colorPolylineOptions.width(getWidth());
        List<Integer> colorList = new ArrayList<>();
        colorPolylineOptions.add(AMapUtils.convertToLatLng(tmcs.get(0).getPolyline().get(0)));
        colorList.add(getColorForDrive());

        for (TMC trafficStatus : tmcs) {
            int color = getColor(trafficStatus.getStatus());
            List<LatLonPoint> mPolylineList = trafficStatus.getPolyline();
            for (LatLonPoint polyline : mPolylineList) {
                colorPolylineOptions.add(AMapUtils.convertToLatLng(polyline));
                colorList.add(color);
            }
        }
        colorList.add(getColorForDrive());
        colorPolylineOptions.colorValues(colorList);    //一一对应地上色
    }



    /**
     * 根据路线添加marker到地图上
     *
     * @param step
     * @param latLng
     */
    private void showMarker(DriveStep step, LatLng latLng) {
        addMarkerList(new MarkerOptions()
                .position(latLng)
                .title("方向：" + step.getAction() + "\n道路：" + step.getRoad())
                .snippet(step.getInstruction())
                .visible(isIconVisible)
                .anchor(0.5f, 1f)
                .icon(getDriveBitmap()));     //展示marker
    }

    /**
     * 初始化画笔
     */
    private void initPolyLine() {
        polylineOptions = null;
        colorPolylineOptions = null;
        polylineOptions = new PolylineOptions();
        polylineOptions.color(getColorForDrive()).width(getWidth());
    }
}
