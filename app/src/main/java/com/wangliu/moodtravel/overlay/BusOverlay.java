package com.wangliu.moodtravel.overlay;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RailwayStationItem;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteRailwayItem;
import com.amap.api.services.route.TaxiItem;
import com.amap.api.services.route.WalkStep;
import com.wangliu.moodtravel.utils.AMapUtils;

import java.util.ArrayList;
import java.util.List;

public class BusOverlay extends RouteOverlay {

    private BusPath path;
    private LatLng mLatLng;

    public BusOverlay(Context context, AMap aMap, BusPath path, LatLonPoint start, LatLonPoint end) {
        super(context);
        this.path = path;
        startLatLng = AMapUtils.convertToLatLng(start);
        endLatLng = AMapUtils.convertToLatLng(end);
        super.aMap = aMap;
    }

    /**
     * 在地图上画线
     */
    public void addToMap() {

        /**
         * 绘制节点和线
         * 细节情况较多
         * 两个step之间，用step和step1区分
         * 1.一个step内可能有步行和公交，然后有可能他们之间连接有断开
         * 2.step的公交和step1的步行，有可能连接有断开
         * 3.step和step1之间是公交换乘，且没有步行，需要把step的终点和step1的起点连起来
         * 4.公交最后一站和终点间有步行，加入步行线路，还会有一些步行marker
         * 5.公交最后一站和终点间无步行，之间连起来
         */
        List<BusStep> steps = path.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            BusStep step = steps.get(i);
            if (i < steps.size() - 1) { //不是最后一步
                BusStep step1 = steps.get(i + 1);   //这是当前步数的下一步
                if (step.getBusLines() != null && step.getBusLines().size() > 0) {

                    // 若步行之后再坐公交之间有断层，把步行的最后一个点和公交的第一个点连接起来
                    if (step.getWalk() != null) {
                        connectWalkToBus(step);
                    }
                    //  若公交之后再步行之间有断层，连接上一个公交的最后一点和下一个步行的第一点
                    if (step1.getWalk() != null
                            && step1.getWalk().getSteps().size() > 0) {
                        connectBusToWalk(step, step1);
                    }
                    //  若两个公交换乘时，中间不需要步行，连接两个公交点
                    if (step1.getWalk() == null && step1.getBusLines() != null && step1.getBusLines().size() > 0) {
                        connectBusToBus(step, step1);
                        connectBusToBus(step, step1);
                    }

                    //  从公交站走到火车站
                    if (step1.getRailway() != null) {
                        connectBusToRailway(step, step1);
                    }
                }   //这里和公交有关

                //  从火车站走出
                if (step1.getWalk() != null && step.getRailway() != null
                        && step1.getWalk().getSteps().size() > 0) {
                    connectRailwayToWalk(step, step1);
                }
                //  火车换乘
                if (step.getRailway() != null && step1.getRailway() != null) {
                    connectRailwayToRailway(step, step1);
                }

                if (step.getRailway() != null && step1.getTaxi() != null) {
                    connectRailwayToTaxi(step, step1);
                }
            }   //断层处理完了
            //  需要走路
            if (step.getWalk() != null && step.getWalk().getSteps().size() > 0) {
                addWalkSteps(step);
            } else if (step.getBusLines() == null && step.getBusLines().size() > 0
                    && step.getRailway() == null && step.getTaxi() == null) {
                addWalkPolyline(mLatLng, endLatLng);    //没有推荐路线就从当前位置走到终点去
            }
            // 坐公交车
            if (step.getBusLines() != null && step.getBusLines().size() > 0) {
                RouteBusLineItem item = step.getBusLines().get(0);
                addBusSteps(item.getPolyline());
                addMarker(item);
                if (i == steps.size() - 1) {    //下车以后走到终点去
                    addWalkPolyline(AMapUtils.convertToLatLng(getLastBusPoint(step)), endLatLng);
                }
            }

            if (step.getRailway() != null) {    //需要坐火车
                addRailwayStep(step.getRailway());
                addMarker(step.getRailway());
                if (i == steps.size() - 1) {    //下火车之后走到终点
                    addWalkPolyline(AMapUtils.convertToLatLng(step.getRailway().getArrivalstop().getLocation()), endLatLng);
                }
            }

            if (step.getTaxi() != null) {   //打车到目的地
                addTaxiStep(step.getTaxi());
                addMarker(step.getTaxi());
            }
        }
        addStartEndMarker();
    }

    /**
     * 此步是先走路再上车，如果步行的最后点和上车点之间有断层
     * 就把步行最后一点和公交上车点之间连接起来
     *
     * @param step 当前步
     */
    private void connectWalkToBus(BusStep step) {
        LatLonPoint lastWalkPoint = getLastWalkPoint(step);
        LatLonPoint firstBusPoint = getFirstBusPoint(step);

        if (lastWalkPoint != firstBusPoint) {   //如果有断层
            addWalkPolyline(lastWalkPoint, firstBusPoint);
        }
    }

    /**
     * 公交的最后一点，也就是下车后，和接下来需要走路的部分
     * 如果两者之间有断层，就把这两点之间连接起来
     *
     * @param step  当前步
     * @param step1 下一步
     */
    private void connectBusToWalk(BusStep step, BusStep step1) {
        LatLonPoint lastBusPoint = getLastBusPoint(step);   //这一步的最后一个公交点
        LatLonPoint firstWalkPoint = getFirstWalkPoint(step1); //下一步的第一个步行点

        if (lastBusPoint != firstWalkPoint) {
            addWalkPolyline((lastBusPoint), (firstWalkPoint));
        }
    }

    /**
     * 两个公交换乘时没有需要步行的路
     *
     * @param step
     * @param step1
     */
    private void connectBusToBus(BusStep step, BusStep step1) {
        LatLonPoint lastBusPoint = getLastBusPoint(step);
        LatLonPoint firstBusPoint = getFirstBusPoint(step1);
        if (firstBusPoint.getLatitude() - lastBusPoint.getLatitude() > 0.0001
                || firstBusPoint.getLongitude() - lastBusPoint.getLongitude() > 0.0001
        || lastBusPoint != firstBusPoint) {
            // double型数据比较，如果经纬度不相等
            addArrowPolyline(AMapUtils.convertToLatLng(lastBusPoint)
                    , AMapUtils.convertToLatLng(firstBusPoint));    //画个直线
        }
    }

    /**
     * 公交和地铁之间
     *
     * @param step
     * @param step1
     */
    private void connectBusToRailway(BusStep step, BusStep step1) {
        LatLonPoint lastBusPoint = getLastBusPoint(step);
        LatLonPoint firstRailwaypoint = step1.getRailway().getDeparturestop().getLocation();

        if (lastBusPoint != firstRailwaypoint) {    //公交站到地铁站要走过去
            addWalkPolyline(lastBusPoint, firstRailwaypoint);
        }
    }

    /**
     * 走出火车站
     *
     * @param step
     * @param step1
     */
    private void connectRailwayToWalk(BusStep step, BusStep step1) {
        LatLonPoint lastRailwayPoint = step.getRailway().getArrivalstop().getLocation();
        LatLonPoint firstWalkPoint = getFirstWalkPoint(step1);
        if (lastRailwayPoint != firstWalkPoint) {
            addWalkPolyline(lastRailwayPoint, firstWalkPoint);
        }
    }

    /**
     * 火车换乘，用走的
     *
     * @param step
     * @param step1
     */
    private void connectRailwayToRailway(BusStep step, BusStep step1) {
        LatLonPoint lastRailwayPoint = step.getRailway().getArrivalstop().getLocation();
        LatLonPoint firstRailwayPoint = step1.getRailway().getDeparturestop().getLocation();
        if (lastRailwayPoint != firstRailwayPoint) {
            addWalkPolyline(lastRailwayPoint, firstRailwayPoint);
        }
    }

    /**
     * 从地铁出来后需要打车
     *
     * @param step
     * @param step1
     */
    private void connectRailwayToTaxi(BusStep step, BusStep step1) {
        LatLonPoint lastRailwayPoint = step.getRailway().getArrivalstop().getLocation();
        LatLonPoint firstTaxiPoint = step1.getTaxi().getOrigin();
        if (lastRailwayPoint != firstTaxiPoint) {
            addWalkPolyline(lastRailwayPoint, firstTaxiPoint);
        }
    }


    private LatLonPoint getLastWalkPoint(BusStep step) {
        List<WalkStep> walkSteps = step.getWalk().getSteps();
        WalkStep walkStep = walkSteps.get(walkSteps.size() - 1);    //最后一步
        List<LatLonPoint> points = walkStep.getPolyline();  //这一步需要走的点集
        return points.get(points.size() - 1); //最后一个步行点
    }

    private LatLonPoint getFirstWalkPoint(BusStep step) {
        return step.getWalk().getSteps().get(0).getPolyline().get(0);
    }

    private LatLonPoint getFirstBusPoint(BusStep step) {
        return step.getBusLines().get(0).getPolyline().get(0);
    }

    private LatLonPoint getLastBusPoint(BusStep step) {
        List<LatLonPoint> points = step.getBusLines().get(0).getPolyline();
        return points.get(points.size() - 1); //最后一个公交点
    }

    /**
     * 画一个直线
     *
     * @param from
     * @param to
     */
    private void addArrowPolyline(LatLng from, LatLng to) {
        addPolyline(new PolylineOptions().add(from, to)
                .width(3)
                .color(getColorForBus())
                .width(getWidth()));
    }


    private void addWalkPolyline(LatLonPoint from, LatLonPoint to) {
        addWalkPolyline(AMapUtils.convertToLatLng(from), AMapUtils.convertToLatLng(to));
    }

    private void addWalkPolyline(LatLng from, LatLng to) {
        addPolyline(new PolylineOptions()
                .add(from, to)
                .width(getWidth())
                .color(getColorForWalk())
                .setDottedLine(true));
    }

    private void addPolylineByList(List<LatLng> latLngs) {
        addPolyline(new PolylineOptions().addAll(latLngs)
                .color(getColorForWalk()).width(getWidth()));
    }

    /**
     * 画出走路的路线
     *
     * @param step
     */
    private void addWalkSteps(BusStep step) {
        List<WalkStep> walkSteps = step.getWalk().getSteps();
        for (int i = 0; i < walkSteps.size(); i++) {
            WalkStep walkStep = walkSteps.get(i);
            if (i == 0) { //步行的第一步
                addMarker(AMapUtils.convertToLatLng(walkStep.getPolyline().get(0))
                        , walkStep.getRoad()
                        , convertToWalkSnippet(walkSteps)); //在地图上加一个marker
            }

            List<LatLng> walkPolylineList = AMapUtils.convertLatLngList(
                    walkStep.getPolyline()
            );
            mLatLng = walkPolylineList.get(walkPolylineList.size() - 1);  //最后一步的坐标
            addPolylineByList(walkPolylineList);

            if (i < walkSteps.size() - 1) { //不是最后一步
                LatLng lastLatLng = walkPolylineList.get(walkPolylineList.size() - 1);    //这一步的终点
                LatLng firstLatLng = AMapUtils.convertToLatLng(walkSteps.get(i + 1).getPolyline().get(0));     //  下一步的起点

                if (lastLatLng != firstLatLng) {
                    addWalkPolyline(lastLatLng, firstLatLng);   //有断层就连接起来
                }
            }
        }
    }

    /**
     * 画公交车的路线
     *
     * @param pointList
     */
    private void addBusSteps(List<LatLonPoint> pointList) {
        if (pointList.size() < 1) {
            return;
        }
        addPolyline(new PolylineOptions().width(getWidth()).color(getColorForBus())
                .addAll(AMapUtils.convertLatLngList(pointList)));
    }

    private void addRailwayStep(RouteRailwayItem item) {
        List<LatLng> latLngs = new ArrayList<>();
        List<RailwayStationItem> items = new ArrayList<>();
        items.add(item.getDeparturestop()); //添加起始站点
        items.addAll(item.getViastops());   //添加途经站点
        items.add(item.getArrivalstop());    //添加到达站点
        for (RailwayStationItem stationItem : items) {
            latLngs.add(AMapUtils.convertToLatLng(stationItem.getLocation()));
        }
        addPolylineByList(latLngs);
    }

    private void addTaxiStep(TaxiItem item) {
        addPolyline(new PolylineOptions().width(getWidth())
                .color(getColorForDrive())
                .add(AMapUtils.convertToLatLng(item.getOrigin()))
                .add(AMapUtils.convertToLatLng(item.getDestination())));
    }


    /**
     * 添加步行的marker
     *
     * @param latLng  坐标
     * @param title
     * @param snippet
     */
    private void addMarker(LatLng latLng, String title, String snippet) {
        addMarkerList(new MarkerOptions().position(latLng)
                .title(title).snippet(snippet)
                .anchor(0.5f, 1f)
                .visible(isIconVisible)
                .icon(getWalkBitmap()));
    }

    private void addMarker(RouteBusLineItem item) {
        addMarkerList(new MarkerOptions().position(AMapUtils.convertToLatLng(item.getDepartureBusStation().getLatLonPoint()))
                .title(item.getBusLineName())
                .snippet(convertToBusSnippet(item))
                .icon(getBusBitmap())
                .anchor(0.5f, 1f)
                .visible(isIconVisible));

    }

    private void addMarker(RouteRailwayItem item) {
        addMarkerList(new MarkerOptions().position(AMapUtils.convertToLatLng(item.getDeparturestop().getLocation()))
                .title(item.getDeparturestop().getName() + "上车")
                .snippet(item.getName())
                .anchor(0.5f, 1f)
                .icon(getBusBitmap())
                .visible(isIconVisible));   //添加一个起点marker

        addMarkerList(new MarkerOptions().position(AMapUtils.convertToLatLng(item.getArrivalstop().getLocation()))
                .title(item.getArrivalstop().getName() + "下车")
                .snippet(item.getName())
                .anchor(0.5f, 1f)
                .icon(getBusBitmap())
                .visible(isIconVisible));
    }

    private void addMarker(TaxiItem item) {
        addMarkerList(new MarkerOptions().position(AMapUtils.convertToLatLng(item.getOrigin()))
                .title(item.getmSname() + "打车")
                .snippet("到终点").icon(getDriveBitmap())
                .visible(isIconVisible).anchor(0.5f, 1f));
    }

    private String convertToWalkSnippet(List<WalkStep> steps) {
        float distance = 0;
        for (WalkStep step : steps) {
            distance += step.getDistance();
        }

        return "步行 " + (int) distance + " 米";
    }

    private String convertToBusSnippet(RouteBusLineItem item) {
        return "(" + item.getDepartureBusStation().getBusStationName()
                + "-->" + item.getArrivalBusStation().getBusStationName()
                + ") 经过" + (item.getPassStationNum() + 1) + "站";
    }
}
