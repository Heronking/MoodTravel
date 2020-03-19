package com.wangliu.moodtravel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.route.RideStep;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.utils.AMapUtils;

import java.util.ArrayList;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.ViewHolder> {

    private Context context;
    private List<RideStep> list = new ArrayList<>();

    public RideAdapter(Context context, List<RideStep> list) {
        this.context = context;
        this.list.add(new RideStep());
        this.list.addAll(list);
        this.list.add(new RideStep());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView divideLine;
        ImageView directIcon;
        TextView  pathDetail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            directIcon = itemView.findViewById(R.id.path_dir_icon);
            divideLine = itemView.findViewById(R.id.path_divide_line);
            pathDetail = itemView.findViewById(R.id.path_detail);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RideAdapter.ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_path, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideStep step = this.list.get(position);
        if (position == 0) {
            holder.divideLine.setVisibility(View.GONE);
            holder.directIcon.setImageResource(R.drawable.dir_start);
            holder.pathDetail.setText("出发");
        } else if (position == list.size() - 1) {
            holder.divideLine.setVisibility(View.VISIBLE);
            holder.directIcon.setImageResource(R.drawable.dir_end);
            holder.pathDetail.setText("到达终点");
        } else {
            holder.divideLine.setVisibility(View.VISIBLE);
            holder.directIcon.setImageResource(
                    AMapUtils.getDriveIconID(step.getAction()));
            holder.pathDetail.setText(step.getInstruction());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
