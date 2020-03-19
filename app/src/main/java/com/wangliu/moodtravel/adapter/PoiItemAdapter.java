package com.wangliu.moodtravel.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.PoiItem;
import com.wangliu.moodtravel.PoiSearchActivity;
import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.utils.Constants;

import java.util.List;

public class PoiItemAdapter extends RecyclerView.Adapter<PoiItemAdapter.ViewHolder> {

    private List<PoiItem> poiItemList;  //poi事件集合
    private PoiSearchActivity activity;

    public PoiItemAdapter(List<PoiItem> poiItemList, PoiSearchActivity activity) {
        this.poiItemList = poiItemList;
        this.activity = activity;
    }

    /**
     * 点击下拉条目返回数据
     * 把数据放到intent传到其他活动中
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poi_tips,parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.poiView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                PoiItem poi = poiItemList.get(position);
                Intent intent = new Intent();   //把点击的数据放到intent里面
                intent.putExtra("resultType", Constants.POIITEM_RESULT)
                        .putExtra("result", poi);
                activity.setResult(AppCompatActivity.RESULT_OK, intent);
                activity.finish();  //点了以后返回上一个跳转过来的界面
            }
        });
        return holder;
    }

    /**
     * 绑定数据
     * 设置城市名以及地址
     * @param holder    子项holder
     * @param position  每个子项的下标
     */
    @Override
    public void onBindViewHolder(@NonNull PoiItemAdapter.ViewHolder holder, int position) {
        PoiItem poiItem = poiItemList.get(position);
        holder.poiName.setText(poiItem.getTitle());
        holder.poiAddress.setText(poiItem.getSnippet());
    }

    /**
     * 条目数量即返回到的所有数据
     * @return
     */
    @Override
    public int getItemCount() {
        return poiItemList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        View poiView;   //视图
        TextView poiName;   //加载的城市名
        TextView poiAddress;    //加载的地址

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            poiView = itemView;
            poiName = itemView.findViewById(R.id.poi_name);
            poiAddress = itemView.findViewById(R.id.poi_address);
        }
    }
}
