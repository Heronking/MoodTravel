package com.wangliu.moodtravel;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.wangliu.moodtravel.adapter.PoiItemAdapter;
import com.wangliu.moodtravel.adapter.PoiTipsAdapter;
import com.wangliu.moodtravel.sqlite.SearchHistorySQLiteHelper;
import com.wangliu.moodtravel.utils.ToastUtils;
import com.wangliu.moodtravel.widget.LoadingDialog;

import java.util.List;

import static com.wangliu.moodtravel.R.layout.item_search_history;

public class PoiSearchActivity extends AppCompatActivity implements
        PoiSearch.OnPoiSearchListener, Inputtips.InputtipsListener,
        SearchView.OnQueryTextListener {

    private String mCity;   //当前所在城市区域

    private SearchView mSvSearch;
    private RecyclerView mRvSearchTips;
    private LinearLayout mLLHistory;
    private ListView mLvRecord;
    private TextView mTvClean;
    private SearchHistorySQLiteHelper helper;

    private PoiTipsAdapter searchTipsAdapter;
    private List<Tip> tipList;  //poi提示信息集合

    private LoadingDialog loadingDialog;

    /**
     * 传递数据
     * 返回时将回调onActivityResult
     * @param appCompatActivity
     * @param requestCode   调用的请求码
     * @param mCity 把城市信息传过来
     */
    public static void startActivity(AppCompatActivity appCompatActivity, int requestCode, String mCity) {
        Intent intent = new Intent(appCompatActivity, PoiSearchActivity.class);
        intent.putExtra("mCity", mCity);
        appCompatActivity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_search);

        mCity = getIntent().getStringExtra("mCity");
        //初始化数据库
        helper = new SearchHistorySQLiteHelper(this, "history", null, 1);
//        Cursor cursor = helper.getReadableDatabase().rawQuery("select * from history", null);
//        while (cursor.moveToNext()) {
//            Log.e("data", cursor.getString(cursor.getColumnIndex("hno"))+" "+cursor.getString(cursor.getColumnIndex("record")));
//        }

        initView();

    }

    /**
     * 初始化布局中的控件，注册监听
     */
//    @SuppressLint("ResourceAsColor")
    private void initView() {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mRvSearchTips = findViewById(R.id.rv_search);
        mRvSearchTips.setLayoutManager(new LinearLayoutManager(this));  //设置布局
        mLvRecord = findViewById(R.id.record);
        mTvClean = findViewById(R.id.clean_username);
        mLLHistory = findViewById(R.id.ll_history);
        mSvSearch = findViewById(R.id.search_view);
        Toolbar mToolbar = findViewById(R.id.poi_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        if (helper.hasData(helper.getReadableDatabase())) {
            showRecord();
        }
        registerListener();
    }

    private void registerListener() {
        mSvSearch.setOnQueryTextListener(this); //搜索栏文本事件监听
        mSvSearch.setSubmitButtonEnabled(true); //提交按钮，点击开始搜索
        mSvSearch.onActionViewExpanded();   //进入搜索页面默认展开view，打开软键盘
        mSvSearch.setOnCloseListener(() -> {
            if (helper.hasData(helper.getReadableDatabase())) {
                showRecord();
            }
            return false;
        });

        mTvClean.setOnClickListener(v -> {
            new Thread(() -> {
                helper.delete(helper.getWritableDatabase());
                mLLHistory.setVisibility(View.GONE);
            }).start();
        });

        mLvRecord.setOnItemClickListener((parent, view, position, id) -> {
            TextView textView = view.findViewById(R.id.text);
            mSvSearch.setQuery(textView.getText(), false);
        });
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        super.addContentView(view, params);
    }

    /**
     * 输入文本后，直接提交开始搜索
     * 设置为public方便别处调用
     * @param s 输入的文本
     */
    public void setQuery(String s) {
        mSvSearch.setQuery(s, true);
    }

    /**
     * 放一个加载条
     */
    private void showLoadingDialog() throws InterruptedException {
        if (loadingDialog == null) {
            Thread.sleep(500);
            LoadingDialog.Builder builder = new LoadingDialog.Builder(this);
            builder.setMessage("搜索中...").setCanelable(true).setCanelableOutside(true);
            loadingDialog = builder.create();
            loadingDialog.show();
        }
    }

    /**
     * 自动消失
     */
    private void dismissLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }



    /**
     * 搜索地点结果回调事件，解析返回结果
     * @param poiResult poi搜索结果
     * @param i 值为CODE_AMAP_SUCCESS(1000)时代表搜索成功，其他为失败
     */
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        dismissLoadingDialog(); //先把加载框去掉
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            //解析poiResult获取poi信息
            if (poiResult != null && poiResult.getQuery() != null
                    && poiResult.getPois() != null) {

                //poi信息集合
                List<PoiItem> poiItemList = poiResult.getPois();
//                for (PoiItem p: poiItemList) {
//                    Log.e("数据：", p.getTitle()+" "+p.getSnippet()+"\n");
//                }
                PoiItemAdapter poiItemAdapter = new PoiItemAdapter(poiItemList, this, helper);
                mRvSearchTips.setAdapter(poiItemAdapter);
                poiItemAdapter.notifyDataSetChanged();  //刷新数据
            }
        } else {
            ToastUtils.showMsg(this, "搜索失败了，自己找原因\n错误码：" + i, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * 查询文本提交事件处理
     * @param word  要搜索的字符串
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String word) {
        try {
            showLoadingDialog();    //显示加载框休息一下
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PoiSearch.Query query = new PoiSearch.Query(word, "", mCity);//si: 搜索类型  mCity：搜索的城市区域
        query.setPageSize(50);  //设置每页最多返回多少条poiItem
        query.setPageNum(0);    //设置查询页码

        PoiSearch search = new PoiSearch(this, query);
        search.setOnPoiSearchListener(this);
        search.searchPOIAsyn(); //发送查询请求

        mSvSearch.clearFocus(); //清除searchView焦点，收起软键盘

        return false;
    }


    private void showRecord() {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select hno as _id, record from history order by hno desc", null);
        ListAdapter adapter = new SimpleCursorAdapter(this, item_search_history, cursor
                , new String[]{"record"}, new int[]{R.id.text}
                , CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mLvRecord.setAdapter(adapter);
        db.close();

        mLLHistory.setVisibility(View.VISIBLE);
    }


    /**
     * 文本框内文本改变时实时回调此方法更新搜索信息
     * @param newText
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText != null && !newText.equals("")) {   //文本框结果改变
            InputtipsQuery query = new InputtipsQuery(newText, mCity);
            Inputtips inputtips = new Inputtips(PoiSearchActivity.this.getApplicationContext(), query);

            inputtips.setInputtipsListener(this);   //根据输入内容回调监听获得搜索结果
            inputtips.requestInputtipsAsyn();

        } else {
            if (searchTipsAdapter != null && tipList != null) {
                tipList.clear();
                searchTipsAdapter.notifyDataSetChanged();   //刷新数据
            }
        }
        return false;
    }

    /**
     * 获取输入的关键词搜索结果
     * @param list
     * @param i
     */
    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            tipList = list;
            searchTipsAdapter = new PoiTipsAdapter(tipList, this, helper);
            searchTipsAdapter.notifyDataSetChanged();
            mRvSearchTips.setAdapter(searchTipsAdapter);

        } else {
            ToastUtils.showMsg(this, "i的值：" + i, Toast.LENGTH_SHORT);
        }
    }


    /**
     * toolbar菜单栏选中事件
     * @param item  事件对象
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {    //点击按钮返回跳转过来的activity
            finish();   //与startActivityForResult()相对应的方法，在这里调用之后，返回上一个activity，并回调onActivityResult()
        }
        return true;
    }


}
