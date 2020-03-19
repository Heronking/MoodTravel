package com.wangliu.moodtravel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.wangliu.moodtravel.BusRouteActivity;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.utils.AMapUtils;

import java.util.List;

public class BusResultAdapter extends RecyclerView.Adapter<BusResultAdapter.ViewHolder> {

    private Context context;
    private List<BusPath> pathList;
    private BusRouteResult result;

    public BusResultAdapter(Context context, BusRouteResult result) {
        this.context = context;
        this.pathList = result.getPaths();
        this.result = result;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_bus_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusPath path = pathList.get(position);
        holder.title.setText(AMapUtils.getBusPathTitle(path));
        holder.details.setText(AMapUtils.getBusDetails(path));
        holder.itemView.setOnClickListener(v -> BusRouteActivity.startActivity(context, result, position));
    }


    @Override
    public int getItemCount() {
        return pathList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView details;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.bus_path_title);
            details = itemView.findViewById(R.id.bus_details);
        }
    }
}
