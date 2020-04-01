package com.wangliu.moodtravel.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RailwayStationItem;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.widget.BusPlan;

import java.util.ArrayList;
import java.util.List;

public class BusRouteAdapter extends RecyclerView.Adapter<BusRouteAdapter.ViewHolder> {

    private Context context;
    private List<BusPlan> busPlanList = new ArrayList<>();

    public BusRouteAdapter(Context context, List<BusStep> list) {
        this.context = context;
        busPlanList.clear();
        BusPlan start = new BusPlan(null);
        start.setStart(true);
        busPlanList.add(start);
        for (BusStep step: list) {
            if (step.getWalk() != null && step.getWalk().getDistance() > 0) {   //需要走路
                BusPlan walk = new BusPlan(step);
                walk.setWalk(true);
                busPlanList.add(walk);
            }
            if (step.getRailway() != null) {
                BusPlan railway = new BusPlan(step);
                railway.setRailway(true);
                busPlanList.add(railway);
            }
            if (step.getBusLines() != null && step.getBusLines().size() != 0) {  //地铁也是一种公交，这里用else if
                BusPlan bus = new BusPlan(step);
                bus.setBus(true);
                busPlanList.add(bus);
            }
            if (step.getTaxi() != null) {
                BusPlan taxi = new BusPlan(step);
                taxi.setTaxi(true);
                busPlanList.add(taxi);
            }
        }
        BusPlan end = new BusPlan(null);
        end.setEnd(true);
        busPlanList.add(end);
//        BusStep step = busPlanList.get(busPlanList.size()-2);
//        if (step.getRailway() != null) Log.e("railway: ", " " + step.getRailway().getName());
//        if (step.getBusLines() != null) Log.e("bus: ", " " + step.getBusLines().get(0).getBusLineName());
//        if (step.getTaxi() != null) Log.e("taxi: ", " " + step.getTaxi().getmSname());
//        if (step.getWalk() != null) Log.e("walk: ", " " + step.getWalk().getDistance());



    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(View.inflate(context, R.layout.item_bus_route_details, null));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusPlan item = busPlanList.get(position);
//        Log.e("item::::", "pos:"+position +" bus:"+item.isBus() + " walk:"+item.isWalk()+" taxi:"+item.isTaxi()+" railway:"+item.isRailway());
        if (position == 0 && item.isStart()) {    //出发栏
            holder.dirIcon.setImageResource(R.drawable.dir_start);
            holder.pathDetails.setText("出发");
            holder.connectLineBottom.setVisibility(View.VISIBLE);
            holder.connectLineTop.setVisibility(View.INVISIBLE);
            holder.divideLine.setVisibility(View.GONE);
            holder.stationNum.setVisibility(View.INVISIBLE);
            holder.expandImage.setVisibility(View.GONE);

//            holder.parent.setOnClickListener(v ->             ToastUtils.showMsg(context, "pos: " + position, 0));

        } else if (position == busPlanList.size() - 1 && item.isEnd()) {
            holder.dirIcon.setImageResource(R.drawable.dir_end);
            holder.pathDetails.setText("到达终点");
            holder.connectLineTop.setVisibility(View.VISIBLE);
            holder.connectLineBottom.setVisibility(View.INVISIBLE);
            holder.stationNum.setVisibility(View.INVISIBLE);
            holder.expandImage.setVisibility(View.INVISIBLE);

//            holder.parent.setOnClickListener(v ->             ToastUtils.showMsg(context, "pos: " + position, 0));

        } else {
            if (item.isWalk() && item.getWalk() != null && item.getWalk().getDistance() > 0) {
                holder.dirIcon.setImageResource(R.drawable.dir13);
                holder.connectLineTop.setVisibility(View.VISIBLE);
                holder.connectLineBottom.setVisibility(View.VISIBLE);
                holder.pathDetails.setText("步行" + (int) item.getWalk().getDistance() + "米");
                holder.stationNum.setVisibility(View.GONE);
                holder.expandImage.setVisibility(View.GONE);

//                holder.parent.setOnClickListener(v ->             ToastUtils.showMsg(context, "pos: " + position, 0));

            } else if (item.isRailway() && item.getRailway() != null) {
                holder.dirIcon.setImageResource(R.drawable.dir16);
                holder.connectLineBottom.setVisibility(View.VISIBLE);
                holder.connectLineTop.setVisibility(View.VISIBLE);
                holder.pathDetails.setText(item.getRailway().getName());
                holder.stationNum.setText(item.getRailway().getViastops().size());  //站点个数
                holder.stationNum.setVisibility(View.VISIBLE);

//                Log.e("Railway stationNum:", holder.stationNum.getText().toString());

                holder.expandImage.setVisibility(View.VISIBLE);

                holder.parent.setTag(position);
                onArrowClick click = new onArrowClick(holder, item);
                holder.parent.setOnClickListener(click);    //点击事件，展开站点信息

            } else if (item.isBus() && item.getBusLines() != null && item.getBusLines().size() > 0) {
                holder.dirIcon.setImageResource(R.drawable.dir14);
                holder.connectLineTop.setVisibility(View.VISIBLE);
                holder.connectLineBottom.setVisibility(View.VISIBLE);
                holder.pathDetails.setText(item.getBusLines().get(0).getBusLineName());
                holder.stationNum.setText(item.getBusLines().get(0).getPassStationNum() + 1 + "站"); //获取公交站数，本站也要算进去
//                Log.e("stationNum: ", holder.stationNum.getText().toString());

                holder.stationNum.setVisibility(View.VISIBLE);
                holder.expandImage.setVisibility(View.VISIBLE);

                holder.parent.setTag(position);
                onArrowClick click = new onArrowClick(holder, item);
                holder.parent.setOnClickListener(click);

            } else if (item.isTaxi() && item.getTaxi() != null) {
                holder.dirIcon.setImageResource(R.drawable.dir15);
                holder.connectLineBottom.setVisibility(View.VISIBLE);
                holder.connectLineTop.setVisibility(View.VISIBLE);
                holder.pathDetails.setText("打车到达终点");
                holder.stationNum.setVisibility(View.GONE);
                holder.expandImage.setVisibility(View.GONE);

//                holder.parent.setOnClickListener(v ->             ToastUtils.showMsg(context, "pos: " + position, 0));

            }
        }

    }

    @Override
    public int getItemCount() {
        return busPlanList.size();
    }

    class onArrowClick implements View.OnClickListener {

        private ViewHolder holder;
        private BusPlan item;

        onArrowClick(ViewHolder holder, BusPlan item) {
            this.holder = holder;
            this.item = item;
        }

        /**
         * 把每个地铁站点的信息展示出来
         * @param stationItem
         */
        @SuppressLint("SetTextI18n")
        private void showRailwayStation(RailwayStationItem stationItem) {
            LinearLayout layout = (LinearLayout) View.inflate(context, R.layout.item_railway_station_details, null);
            TextView textView = layout.findViewById(R.id.station);
            textView.setText(stationItem.getName() + " " + timeOfRailway(stationItem.getTime()));
//            Log.e("time:!!!!",  "!!!!!!!");
            holder.expandDetails.addView(layout);
        }

        String timeOfRailway(String time) {
            return time.substring(0, 2) + "：" + time.substring(2);
//            Log.e("time:", time);
//            return time;
        }

        private void showBusStation(BusStationItem stationItem) {
            LinearLayout layout = (LinearLayout) View.inflate(context, R.layout.item_bus_station_details, null);
            TextView textView = layout.findViewById(R.id.station_info);
            textView.setText(stationItem.getBusStationName());
            holder.expandDetails.addView(layout);
        }

        @Override
        public void onClick(View v) {
            int pos = Integer.parseInt(v.getTag().toString());
            item = busPlanList.get(pos);
//            Log.e("item::", item.isRailway() + " " + item.isBus());
//            ToastUtils.showMsg(context, "pos: " + pos, 0);
            if (item.isRailway()) {
                if (!holder.isExpand) {
                    holder.isExpand = true;
                    holder.expandImage.setImageResource(R.drawable.icon_expand_up);
                    showRailwayStation(item.getRailway().getDeparturestop());   //起始站
                    for (RailwayStationItem stationItem: item.getRailway().getViastops()) { //途经站点
                        showRailwayStation(stationItem);
                    }
                    showRailwayStation(item.getRailway().getArrivalstop()); //终点站
//                    Log.e("time:", item.getRailway().getViastops().get(0).getTime() + " 111111");
                } else {
                    holder.isExpand = false;
                    holder.expandImage.setImageResource(R.drawable.icon_expand_down);
                    holder.expandDetails.removeAllViews();
                    Log.e("time:", item.getRailway().getViastops().get(0).getTime() + " 111111");
                }
            } else if (item.isBus()) {
                if (!holder.isExpand) {
                    holder.isExpand = true;
//                    Log.e("stationNum: ", holder.stationNum.getText().toString());
                    holder.expandImage.setImageResource(R.drawable.icon_expand_up);
                    showBusStation(item.getBusLines().get(0).getDepartureBusStation());   //起始站
                    for (BusStationItem stationItem: item.getBusLines().get(0).getPassStations()) { //途经站点
                        showBusStation(stationItem);
                    }
                    showBusStation(item.getBusLines().get(0).getArrivalBusStation()); //终点站
                } else {
                    holder.isExpand = false;
                    holder.expandImage.setImageResource(R.drawable.icon_expand_down);
                    holder.expandDetails.removeAllViews();
                }
            }

        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout parent;
        LinearLayout expandDetails;
        TextView pathDetails;
        TextView stationNum;
        ImageView dirIcon;
        ImageView expandImage;
        View divideLine, connectLineTop, connectLineBottom;

        boolean isExpand = false;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.bus_item);
            expandDetails = itemView.findViewById(R.id.expand_details);
            pathDetails = itemView.findViewById(R.id.bus_path_details);
            stationNum = itemView.findViewById(R.id.station_num);
            dirIcon = itemView.findViewById(R.id.path_dir_icon);
            expandImage = itemView.findViewById(R.id.iv_expand);
            divideLine = itemView.findViewById(R.id.path_divide_line);
            connectLineTop = itemView.findViewById(R.id.path_connect_top);
            connectLineBottom = itemView.findViewById(R.id.path_connect_bottom);
        }
    }
}
