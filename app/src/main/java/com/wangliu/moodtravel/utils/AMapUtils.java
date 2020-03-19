package com.wangliu.moodtravel.utils;

import android.annotation.SuppressLint;
import android.location.Location;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.enums.IconType;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteRailwayItem;
import com.wangliu.moodtravel.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AMapUtils {
    public static LatLng convertToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }


    public static LatLonPoint convertToLatLonPoint(LatLng latLng) {
        return new LatLonPoint(latLng.latitude, latLng.longitude);
    }

    public static List<LatLng> convertLatLngList(List<LatLonPoint> pointList) {
        List<LatLng> list = new ArrayList<>();
        for (LatLonPoint point : pointList) {
            LatLng temp = AMapUtils.convertToLatLng(point);
            list.add(temp);
        }
        return list;
    }


    @SuppressLint("SimpleDateFormat")
    public static String convertToTime(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(time);
            if (date != null) {
                return new SimpleDateFormat("HH:MM").format(date) + "更新";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "刚更新过了";
        }
        return "刚更新过了";
    }

    public static String convertToLengthStr(float length) {
        if (length < 1000) {
            return (int) length + "米";
        } else if (length < 10000) {
            length = (float) Math.round(length / 1000 * 100) / 100;
            return length + "公里";
        } else {
            return (int) (length / 1000) + "公里";
        }
    }

    public static String convertToTimeStr(long second) {
        if (second > 3600) {    //1h
            return second/3600 + "小时 " + second % 3600 / 60 + "分钟";
        }
        if (second >= 60) {
            return second / 60 + "分钟";
        }
        return second + "秒";
    }

    public static String getBusPathTitle(BusPath path) {
        if (path == null) return "";
        List<BusStep> steps = path.getSteps();
        if (steps == null) return "";
        StringBuilder builder = new StringBuilder();
        for (BusStep step: steps) {
            StringBuilder title = new StringBuilder();
            if (step.getBusLines().size() > 0) {
                for (RouteBusLineItem item: step.getBusLines()) {
                    if (item == null) continue;
                    title.append(formatLineName(item.getBusLineName()))
                            .append(" / ");
                }
                builder.append(title.substring(0, title.length() - 3))
                        .append(" > ");
            }
            if (step.getRailway() != null) {
                RouteRailwayItem item = step.getRailway();
                builder.append(item.getTrip())
                        .append("(")
                        .append(item.getDeparturestop().getName())
                        .append(" - ")
                        .append(item.getArrivalstop().getName())
                        .append(")").append(" > ");
            }
        }
        return builder.substring(0, builder.length()-3);
    }

    public static String getBusDetails(BusPath path) {
        if (path == null) return "";
        long sec = path.getDuration();
        String time = convertToTimeStr(sec);
        String distance = convertToLengthStr(path.getDistance());
        String distanceOfWalk = convertToLengthStr(path.getWalkDistance());

        return time + " | " + path.getCost() + "元 | " + distance + " | 步行 " + distanceOfWalk;
    }

    private static String formatLineName(String lineName) {
        if (lineName == null) return "";
        //利用正则匹配，将\和.替换掉
        return lineName.replaceAll("\\(.*?\\)", "");
    }

    /**
     * 驾车骑行公交的路线规划图片
     * @param stepAction    路线动作
     * @return
     */
    public static int getDriveIconID(String stepAction) {

        if (stepAction == null || stepAction.equals("")) {
            return R.drawable.dir3;
        }
        if ("左转".equals(stepAction)) {
            return R.drawable.dir2;
        }
        if ("右转".equals(stepAction)) {
            return R.drawable.dir1;
        }
        if ("向左前方行驶".equals(stepAction) || "靠左".equals(stepAction)) {
            return R.drawable.dir6;
        }
        if ("向右前方行驶".equals(stepAction) || "靠右".equals(stepAction)) {
            return R.drawable.dir5;
        }
        if ("向左后方行驶".equals(stepAction) || "左转调头".equals(stepAction)) {
            return R.drawable.dir7;
        }
        if ("向右后方行驶".equals(stepAction)) {
            return R.drawable.dir8;
        }
        if ("直行".equals(stepAction)) {
            return R.drawable.dir3;
        }
        if ("减速行驶".equals(stepAction)) {
            return R.drawable.dir4;
        }
        return R.drawable.dir3;
    }

    /**
     * 行走的路线规划图片
     * @param stepAction
     * @return
     */
    public static int getWalkIconID(String stepAction) {
        if (stepAction == null || stepAction.equals("")) {
            return R.drawable.dir13;
        }
        if ("左转".equals(stepAction)) {
            return R.drawable.dir2;
        }
        if ("右转".equals(stepAction)) {
            return R.drawable.dir1;
        }
        if ("向左前方".equals(stepAction) || "靠左".equals(stepAction)
                || stepAction.contains("向左前方")) {
            return R.drawable.dir6;
        }
        if ("向左后方".equals(stepAction)|| stepAction.contains("向左后方")) {
            return R.drawable.dir7;
        }
        if ("向右前方".equals(stepAction) || "靠右".equals(stepAction)
                || stepAction.contains("向右前方")) {
            return R.drawable.dir5;
        }
        if ("向右后方".equals(stepAction)|| stepAction.contains("向右后方")) {
            return R.drawable.dir8;
        }
        if ("直行".equals(stepAction)) {
            return R.drawable.dir3;
        }
        if ("通过人行横道".equals(stepAction)) {
            return R.drawable.dir9;
        }
        if ("通过过街天桥".equals(stepAction)) {
            return R.drawable.dir11;
        }
        if ("通过地下通道".equals(stepAction)) {
            return R.drawable.dir10;
        }
        return R.drawable.dir13;
    }

    public static String getRouteDetails(int iconType) {
        String result = null;
        switch (iconType) {
            case IconType.ARRIVED_DESTINATION:
                result = "到达目的地";
                break;
            case IconType.ARRIVED_SERVICE_AREA:
                result = "到达服务区";
                break;
            case IconType.ARRIVED_TOLLGATE:
                result = "到达收费站";
                break;
            case IconType.ARRIVED_TUNNEL:
                result = "到达隧道";
                break;
            case IconType.ARRIVED_WAYPOINT:
                result = "到达途径点";
                break;
            case IconType.BRIDGE:
                result = "通过桥";
                break;
            case IconType.CABLEWAY:
                result = "通过索道";
                break;
            case IconType.CHANNEL:
                result = "通过通道";
                break;
            case IconType.CROSSWALK:
                result = "通过人行横道";
                break;
            case IconType.CRUISE_ROUTE:
                result = "通过游船";
                break;
            case IconType.ENTER_BUILDING:
                result = "进入建筑物";
                break;
            case IconType.ENTER_ROUNDABOUT:
                result = "进入环岛图标";
                break;
            case IconType.FERRY:
                result = "通过轮渡";
                break;
            case IconType.LEAVE_BUILDING:
                result = "离开建筑物";
                break;
            case IconType.LADDER:
                result = "通过阶梯";
                break;
            case IconType.OUT_ROUNDABOUT:
                result = "驶出环岛";
                break;
            case IconType.LEFT_FRONT:
                result = "左前方";
                break;
            case IconType.LEFT_TURN_AROUND:
                result = "左转掉头";
                break;
            case IconType.LIFT:
                result = "通过直梯";
                break;
            case IconType.OVERPASS:
                result = "通过过街天桥";
                break;
            case IconType.PARK:
                result = "通过公园";
                break;
            case IconType.RIGHT:
                result = "右转";
                break;
            case IconType.RIGHT_BACK:
                result = "右后方";
                break;
            case IconType.RIGHT_FRONT:
                result = "右前方";
                break;
            case IconType.SIGHTSEEING_BUSLINE:
                result = "通过观光车";
                break;
            case IconType.SKY_CHANNEL:
                result = "通过空中通道";
                break;
            case IconType.SLIDEWAY:
                result = "通过滑道";
                break;
            case IconType.LEFT:
                result = "左转";
                break;
            case IconType.LEFT_BACK:
                result = "左后方";
                break;
            case IconType.SLOPE:
                result = "通过斜坡";
                break;
            case IconType.SPECIAL_CONTINUE:
                result = "顺行";
                break;
            case IconType.SQUARE:
                result = "通过广场";
                break;
            case IconType.STAIRCASE:
                result = "通过扶梯";
                break;
            case IconType.STRAIGHT:
                result = "直行";
                break;
            case IconType.SUBWAY:
                result = "通过地铁";
                break;
            case IconType.U_TURN_RIGHT:
                result = "右转掉头";
                break;
            case IconType.UNDERPASS:
                result = "通过地下通道";
                break;
            case IconType.WALK_ROAD:
                result = "通过行人道路";
                break;
        }
        return result;
    }
}
