package com.wangliu.moodtravel.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Tip;
import com.wangliu.moodtravel.PoiSearchActivity;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.utils.Constants;

import java.util.List;

public class PoiTipsAdapter extends RecyclerView.Adapter<PoiTipsAdapter.ViewHolder>{

    private List<Tip> tipList;
    private PoiSearchActivity activity;

    public PoiTipsAdapter(List<Tip> tipList, PoiSearchActivity activity) {
        this.tipList = tipList;
        this.activity = activity;
    }

    /**
     * 点击提示条目返回数据
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poi_tips, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.tipsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Tip tip = tipList.get(position);
                if (tip.getPoiID() != null && tip.getPoint() != null) {
                    Intent intent = new Intent();
                    intent.putExtra("resultType", Constants.POITIP_RESULT)
                            .putExtra("result", tip);
                    activity.setResult(AppCompatActivity.RESULT_OK, intent);
                    activity.finish();  //返回上一个界面
                } else {    //若点击的提示条目poi信息为空，直接搜索该条目
                    activity.setQuery(tip.getName());
                }
            }
        });
        return holder;
    }

    /**
     * 把得到的提示条目信息显示到TextView上
     *
     * @param holder
     * @param position
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tip tip = tipList.get(position);
        if (tip.getPoint() == null && tip.getPoiID() == null) {
            holder.tipsName.setText(tip.getName() + tip.getDistrict());
            holder.tipsAddress.setVisibility(View.GONE);
        } else {
            holder.tipsName.setText(tip.getName());
            holder.tipsAddress.setText(tip.getAddress());
        }
    }

    /**
     * 条目数量是集合的大小
     * @return
     */
    @Override
    public int getItemCount() {
        return tipList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View tipsView;
        TextView tipsName;
        TextView tipsAddress;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tipsView = itemView;
            tipsName = itemView.findViewById(R.id.poi_name);
            tipsAddress = itemView.findViewById(R.id.poi_address);
        }
    }
}
