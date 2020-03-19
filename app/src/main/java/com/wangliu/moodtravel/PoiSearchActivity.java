package com.wangliu.moodtravel;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.wangliu.moodtravel.utils.ToastUtils;
import com.wangliu.moodtravel.widget.LoadingDialog;

import java.util.List;

public class PoiSearchActivity extends AppCompatActivity implements
        PoiSearch.OnPoiSearchListener, Inputtips.InputtipsListener,
        SearchView.OnQueryTextListener {

    private String mCity;   //当前所在城市区域

    private SearchView mSvSearch;
    private RecyclerView mRvSearchTips;
    private TextView mTvTips;



    private PoiTipsAdapter searchTipsAdapter;
    private List<Tip> tipList;  //poi提示信息集合

    private LoadingDialog loadingDialog;

    /**
     * 传递数据
     * 返回时将回调onActivityResult
     * @param appCompatActivity
     * @param requestCode
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

        initView();
        mRvSearchTips.setLayoutManager(new LinearLayoutManager(this));  //设置布局
    }

    /**
     * 初始化布局中的控件，注册监听
     */
//    @SuppressLint("ResourceAsColor")
    private void initView() {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mRvSearchTips = findViewById(R.id.rv_search);

        mSvSearch = findViewById(R.id.search_view);
        mSvSearch.setOnQueryTextListener(this); //搜索栏文本事件监听
        mSvSearch.setSubmitButtonEnabled(true); //提交按钮，点击开始搜索
        mSvSearch.onActionViewExpanded();   //进入搜索页面默认展开view，打开软键盘

//        int id = mSvSearch.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
//        EditText editText = mSvSearch.findViewById(androidx.appcompat.R.id.search_src_text);
//        editText.setTextColor(R.color.colorWhite);
//        editText.setHintTextColor(R.color.colorWhite);


        mTvTips = findViewById(R.id.tv_start_search);

        Toolbar mToolbar = findViewById(R.id.poi_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

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
    private void showLoadingDialog() {
        if (loadingDialog == null) {
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
                PoiItemAdapter poiItemAdapter = new PoiItemAdapter(poiItemList, this);
                mRvSearchTips.setAdapter(poiItemAdapter);
                poiItemAdapter.notifyDataSetChanged();  //刷新数据

                if (poiItemList.size() != 0) {  //搜索到了城市
                    mTvTips.setVisibility(View.GONE);   //去掉搜索提示
                } else {
                    mTvTips.setVisibility(View.VISIBLE);    //否则显示搜索提示
                }
            } else {    //搜索失败
                mTvTips.setVisibility(View.VISIBLE);    //显示搜索提示
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
        showLoadingDialog();    //显示加载框休息一下
        PoiSearch.Query query = new PoiSearch.Query(word, "", mCity);//si: 搜索类型  mCity：搜索的城市区域
        query.setPageSize(50);  //设置每页最多返回多少条poiItem
        query.setPageNum(0);    //设置查询页码

        PoiSearch search = new PoiSearch(this, query);
        search.setOnPoiSearchListener(this);
        search.searchPOIAsyn(); //发送查询请求

        mSvSearch.clearFocus(); //清除searchView焦点，收起软键盘

        return false;
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
            searchTipsAdapter = new PoiTipsAdapter(tipList, this);
            searchTipsAdapter.notifyDataSetChanged();
            mRvSearchTips.setAdapter(searchTipsAdapter);

            if (tipList.size() != 0) {  //有提示条目，就让搜索提示滚
                mTvTips.setVisibility(View.GONE);
            } else {
                mTvTips.setVisibility(View.VISIBLE);
            }
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
