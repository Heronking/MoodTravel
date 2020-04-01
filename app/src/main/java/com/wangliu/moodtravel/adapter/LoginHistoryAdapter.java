package com.wangliu.moodtravel.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wangliu.moodtravel.R;
import com.wangliu.moodtravel.sqlite.AccountHistory;
import com.wangliu.moodtravel.sqlite.LoginHistorySQLiteHelper;

import java.util.List;

public class LoginHistoryAdapter extends BaseAdapter {
    private Context context;
    private List<AccountHistory> histories;
    LoginHistorySQLiteHelper helper;

    public LoginHistoryAdapter(Context context, List<AccountHistory> histories, LoginHistorySQLiteHelper helper) {
        this.histories = histories;
        this.context = context;
        this.helper = helper;
    }

    @Override
    public int getCount() {
        return histories.size() > 5 ? histories.size() - 5 : histories.size();
    }

    @Override
    public Object getItem(int position) {
        return histories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {  //这个view里面没有内容
            convertView = View.inflate(context, R.layout.item_login_history, null);
            holder = new ViewHolder();
            holder.account = convertView.findViewById(R.id.account);
            holder.clean = convertView.findViewById(R.id.clean);
            holder.clean.setOnClickListener(v -> {  //点击移除一条记录
                //把bean中的记录移除
                histories.remove(position);
                //把数据库中的记录移除
                helper.deleteData(position);
                notifyDataSetChanged(); //刷新
            });
            convertView.setTag(holder);
        } else {    //在tag中拿到holder
            holder = (ViewHolder) convertView.getTag();
        }
        AccountHistory history = (AccountHistory) getItem(position);
        holder.account.setText(history.getAccount());

        return convertView;
    }

    private static class ViewHolder {
        private TextView account;
        private ImageView clean;
    }
}
