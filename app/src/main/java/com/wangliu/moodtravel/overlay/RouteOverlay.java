package com.wangliu.moodtravel.overlay;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.adapter.InfoAdapter;

import java.util.ArrayList;
import java.util.List;

public class RouteOverlay {
    private Context context;
//    private Bitmap bitStart, bitEnd, bitBus, bitWalk, bitDrive;

    private List<Marker> markerList = new ArrayList<>();
    private List<Polyline> polylineList = new ArrayList<>();
    Marker startMarker;
    Marker endMarker;
    LatLng startLatLng;
    LatLng endLatLng;
    AMap aMap;
    boolean isIconVisible = true;

    RouteOverlay(Context context) {
        this.context = context;
    }

    /**
     * 去除marker
     * 重置图标
     */
    public void removeFromMap() {

        if (startMarker != null) startMarker.remove();
        if (endMarker != null) endMarker.remove();
        for (Marker marker : markerList) {
            marker.remove();
        }
        for (Polyline polyline : polylineList) {
            polyline.remove();
        }
        polylineList.clear();
        markerList.clear();
    }

    /**
     * 给起点marker换个图标
     *
     * @return 返回的是图片
     */
    BitmapDescriptor getStartBitmap() {
        return BitmapDescriptorFactory.fromResource(R.drawable.map_start);
    }

    /**
     * 终点marker的图标
     *
     * @return
     */
    BitmapDescriptor getEndBitmap() {
        return BitmapDescriptorFactory.fromResource(R.drawable.map_end);
    }

    /**
     * 步行marker图标
     *
     * @return
     */
    BitmapDescriptor getWalkBitmap() {
        return BitmapDescriptorFactory.fromResource(R.drawable.map_man);
    }

    /**
     * 驾车marker图标
     *
     * @return
     */
    BitmapDescriptor getDriveBitmap() {
        return BitmapDescriptorFactory.fromResource(R.drawable.map_car);
    }

    /**
     * 骑行marker图标
     *
     * @return
     */
    BitmapDescriptor getRideBitmap() {
        return BitmapDescriptorFactory.fromResource(R.drawable.map_ride);
    }

    /**
     * 公交站图标
     *
     * @return
     */
    BitmapDescriptor getBusBitmap() {
        return BitmapDescriptorFactory.fromResource(R.drawable.map_bus);
    }

    /**
     * 添加起点和终点marker
     */
    void addStartEndMarker() {
        startMarker = aMap.addMarker(new MarkerOptions()
                .position(startLatLng)
                .icon(getStartBitmap())
                .title("起点"));
        endMarker = aMap.addMarker(new MarkerOptions()
                .position(endLatLng)
                .icon(getEndBitmap())
                .title("终点"));
    }

    /**
     * 移动视图到涉及路线的区域
     * 默认padding 50dp
     * 可选择paddingTop和paddingBottom
     *
     * @param top    paddingTop
     * @param bottom paddingBottom
     */
    public void moveCameraWithPadding(int top, int bottom) {
        if (startLatLng != null) {
            if (aMap == null) {
                return;
            }
            try {
                aMap.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(getBounds()
                        , 50, 50, 50 + top, 50 + bottom));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 默认80，可加paddingBottom
     *
     * @param bottom paddingBottom
     */
    public void moveCameraWithBottom(int bottom) {
        if (startLatLng != null) {
            if (aMap == null) {
                return;
            }
            try {
//                Log.e("bound: ", getBounds().toString());
                aMap.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(getBounds()
                        , 80, 80, 80, 50 + bottom));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得路线涉及区域的范围
     *
     * @return 范围
     */
    protected LatLngBounds getBounds() {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        builder.include(new LatLng(startLatLng.latitude, startLatLng.longitude));
        builder.include(new LatLng(endLatLng.latitude, endLatLng.longitude));
        return builder.build();
    }

    /**
     * 设置每个marker是否可见
     *
     * @param visible
     */
    public void setIconVisible(boolean visible) {
        isIconVisible = visible;
        if (markerList != null && markerList.size() > 0) {
            for (Marker marker : markerList) {
                marker.setVisible(visible);
            }
        }
    }

    /**
     * 展示marker
     *
     * @param options
     */
    void addMarkerList(MarkerOptions options) {
        if (options == null) return;
        aMap.setInfoWindowAdapter(new InfoAdapter(context));
        Marker marker = aMap.addMarker(options);
        markerList.add(marker);
    }

    /**
     * 展示路线
     * @param options
     */
    void addPolyline(PolylineOptions options) {
        if (options == null)    {
            Log.e("空指针：", "polylineOptions为null");
            return;
        }
        Log.e("color: ", options.getColor() + "  -----" + getColorForWalk() + "");
        Polyline polyline = aMap.addPolyline(options);
        polylineList.add(polyline);
    }

    //路宽
    float getWidth() {
        return 25f;
    }

    //路线的颜色
    int getColorForWalk() {
        return R.color.colorWalk;
    }

    int getColorForBus() {
        return R.color.colorBusAndDrive;
    }

    int getColorForRide() {
        return R.color.colorBusAndDrive;
    }

    int getColorForDrive() {
        return R.color.colorBusAndDrive;
    }
}