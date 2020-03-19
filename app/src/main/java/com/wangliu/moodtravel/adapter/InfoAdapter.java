package com.wangliu.moodtravel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;
import com.wangliu.moodtravel.R;

public class InfoAdapter implements AMap.InfoWindowAdapter {

    private Context context;

    public InfoAdapter(Context context) {
        this.context = context;
    }

    /**
     * 通过marker展示infoWindow
     *
     * @param marker
     * @return
     */
    @Override
    public View getInfoWindow(final Marker marker) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_info_window, null);

        //地点名、详细地址
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvAddress = view.findViewById(R.id.tv_address);

        tvTitle.setText(marker.getTitle());
        tvAddress.setText(marker.getSnippet());

        return view;
//        LinearLayout route = view.findViewById(R.id.ll_route);
//        route.setOnClickListener((View.OnClickListener) v -> {
//            ToastUtils.showMsg(context, "展示路线", Toast.LENGTH_SHORT);
//
//        });

    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }


}
