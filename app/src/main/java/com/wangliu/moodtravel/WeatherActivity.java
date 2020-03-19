package com.wangliu.moodtravel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Tip;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.wangliu.moodtravel.utils.AMapUtils;
import com.wangliu.moodtravel.utils.Constants;
import com.wangliu.moodtravel.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.wangliu.moodtravel.utils.WeatherUtils.weatherImage;

public class WeatherActivity extends AppCompatActivity implements WeatherSearch.OnWeatherSearchListener {

    private String mCity;
    private String mAddress;

    private LinearLayout mBackground;
    private LinearLayout mWeatherDetails;
    private ImageView mIvBack;
    private TextView mTvCityName;
    private Button mBtnChange;
    private ImageView mIvWeather;
    private TextView mTvTemperature;
    private TextView mTvTime;
    private TextView mTvInfo;
    private TextView mTvWind;
    private TextView mTvHumidity;
    List<View> views = new ArrayList<>();   //预报列表放在这里面


    public static void startActivity(Context context, String mCity, String address) {
        Intent intent = new Intent(context, WeatherActivity.class);
        intent.putExtra("city", mCity);
        intent.putExtra("address", address);
//        Log.e("address", address);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initView();

        getExtras();
//        Log.e("address", mAddress);
    }

    private void getExtras() {
        Intent intent = getIntent();
        mCity = intent.getStringExtra("city");
        mAddress = intent.getStringExtra("address");
        if (mCity != null && mAddress != null) {
            mTvCityName.setText(mAddress);
        } else {
            mTvCityName.setText("城市定位错误，你重来吧");
            mTvTemperature.setText("N/A");
        }
//        mIvWeather.setImageResource(weatherImage.get("雨"));
        if (mCity != null) {
            threadForSearch(mCity); //弄个线程去查询天气以及更新ui
        } else {
            mTvTemperature.setText("定位失败了~");
            mTvTime.setVisibility(View.GONE);
            mTvWind.setVisibility(View.GONE);
            mTvHumidity.setVisibility(View.GONE);
        }
    }

    /**
     * 根据城市名或者地区编码来查询天气
     * @param cityOrCode
     */
    private void threadForSearch(String cityOrCode) {
        new Thread(() -> {  //这里有异步回调，放到线程里面
            searchWeather(cityOrCode, WeatherSearchQuery.WEATHER_TYPE_LIVE);    //实时天气
        }).start();
        new Thread(() -> {
            searchWeather(cityOrCode, WeatherSearchQuery.WEATHER_TYPE_FORECAST);    //预报天气
        }).start();
    }

    private void searchWeather(String cityOrCode, int i) {
        //天气预报查询
        WeatherSearchQuery query = new WeatherSearchQuery(cityOrCode, i);
        WeatherSearch search = new WeatherSearch(this);
        search.setQuery(query);
        search.setOnWeatherSearchListener(this);
        search.searchWeatherAsyn(); //异步查询
    }


    private void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);   //改一下状态栏

//        Log.e("address", mAddress);

        mBackground = findViewById(R.id.background);
        mWeatherDetails = findViewById(R.id.layout_weather_details);
        mIvBack = findViewById(R.id.btn_back);
        mBtnChange = findViewById(R.id.btn_change);
        mIvWeather = findViewById(R.id.iv_weather);
        mTvCityName = findViewById(R.id.weather_city);
        mTvTime = findViewById(R.id.time);
        mTvInfo = findViewById(R.id.info);
        mTvWind = findViewById(R.id.wind);
        mTvTemperature = findViewById(R.id.temperature);
        mTvHumidity = findViewById(R.id.humidity);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);  //现在是几点
        if (hour > 18 || hour < 6) {    //下午6点到上午6点是晚上
            mTvTemperature.setTextColor(Color.WHITE);
            mBackground.setBackgroundResource(R.drawable.bg_weather_night);
        } else {
            mTvTemperature.setTextColor(Color.BLACK);
            mBackground.setBackgroundResource(R.drawable.bg_weather_day);
        }

        mIvBack.setOnClickListener(v -> this.finish());   //返回键监听
        mBtnChange.setOnClickListener(v ->
                PoiSearchActivity.startActivity(this, Constants.REQUEST_WEATHER_ACTIVITY, mCity));  //去搜索

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (localWeatherLiveResult != null && localWeatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive live = localWeatherLiveResult.getLiveResult();
                mTvTemperature.setText(live.getTemperature() + "℃");
                mTvWind.setText(live.getWindDirection() + "风 " + live.getWindPower() + "级");
                mTvHumidity.setText("湿度：" + live.getHumidity() + "%");
                mTvTime.setText(AMapUtils.convertToTime(live.getReportTime()));
                String info = live.getWeather();
                if (weatherImage.containsKey(info)) {
                    mTvInfo.setText(info);
                    mIvWeather.setImageResource(weatherImage.get(info));
                } else {
                    mIvWeather.setImageResource(R.drawable.w0);
                    mTvInfo.setText(info);
                    ToastUtils.showMsg(this, "我没整"+ info + "的图hhhhhhh", 1);
                }
            } else {
                mTvTemperature.setText("没找到天气信息~");
                mTvTime.setVisibility(View.GONE);
                mTvWind.setVisibility(View.GONE);
                mTvHumidity.setVisibility(View.GONE);
            }
        } else {
            mTvTemperature.setText("出错！！找一下原因");
            ToastUtils.showMsg(this, "错误码：" + i, 0);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (localWeatherForecastResult != null
                    && localWeatherForecastResult.getForecastResult() != null
                    && localWeatherForecastResult.getForecastResult().getWeatherForecast() != null
                    && localWeatherForecastResult.getForecastResult().getWeatherForecast().size() > 0) {

                if (views.size() > 0) { //把预报列表清空一下
                    for (View v: views) {
                        mWeatherDetails.removeView(v);
                    }
                }

                for (LocalDayWeatherForecast forecast: localWeatherForecastResult.getForecastResult().getWeatherForecast()) {
                    View view = LayoutInflater.from(this).inflate(R.layout.item_weather_forecast, mWeatherDetails, false);
                    TextView date = view.findViewById(R.id.date);
                    date.setText(forecast.getDate());
                    TextView week = view.findViewById(R.id.week);
                    week.setText(convertToWeek(forecast.getWeek()));
                    TextView min = view.findViewById(R.id.min);
                    min.setText(forecast.getNightTemp() + "℃");
                    TextView max = view.findViewById(R.id.max);
                    max.setText(forecast.getDayTemp() + "℃");
                    TextView info = view.findViewById(R.id.weather);
                    info.setText(forecast.getDayWeather());
                    views.add(view);
                    mWeatherDetails.addView(view);
                }
            } else {
                ToastUtils.showMsg(this, "是不是哪里出问题了？", 0);
            }
        } else {
            mTvTemperature.setText("出错！！找一下原因");
            ToastUtils.showMsg(this, "错误码：" + i, 0);
        }
    }

    private String convertToWeek(String s) {
        switch (s) {
            case "1":
                return "星期一";
            case "2":
                return "星期二";
            case "3":
                return "星期三";
            case "4":
                return "星期四";
            case "5":
                return "星期五";
            case "6":
                return "星期六";
            default:
                return "星期天";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_WEATHER_ACTIVITY) {
            if (resultCode == RESULT_OK && data != null) {
                if (data.getIntExtra("resultType", 1) == Constants.POIITEM_RESULT) {
                    PoiItem item = data.getParcelableExtra("result");
                    if (item != null) {
                        mAddress = item.getAdName();
                        mTvCityName.setText(mAddress);
                        threadForSearch(item.getAdCode());
                    }
                } else if (data.getIntExtra("resultType", 1) == Constants.POITIP_RESULT) {
                    Tip tip = data.getParcelableExtra("result");
                    if (tip != null) {
//                        idSearch(tip);
                        mAddress = tip.getName();
                        mTvCityName.setText(mAddress);
                        threadForSearch(tip.getAdcode());   //重新查询
                    }
                }
            }
        }
    }

}
