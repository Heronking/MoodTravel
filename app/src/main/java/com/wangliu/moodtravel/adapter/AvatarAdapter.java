package com.wangliu.moodtravel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.utils.AvatarUtils;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {

    private Context context;
    private OnItemClickListener listener;   //点击事件

    public AvatarAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_avatar_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.avatar.setImageResource(AvatarUtils.avatars.get(position+1));
        //响应外部传进来的点击事件
        holder.itemView.setOnClickListener(v -> {
            listener.onClick(position);
        });
    }


    @Override
    public int getItemCount() {
        return AvatarUtils.avatars.size()-1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }
}
