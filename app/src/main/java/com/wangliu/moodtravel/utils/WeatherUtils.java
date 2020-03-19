package com.wangliu.moodtravel.utils;

import com.wangliu.moodtravel.R;

import java.util.HashMap;

public class WeatherUtils {

    public static HashMap<String, Integer> weatherImage = new HashMap<>();

    static {
        weatherImage.put("晴", R.drawable.w1);
        weatherImage.put("多云",R.drawable.w2);
        weatherImage.put("阴",R.drawable.w3);
        weatherImage.put("雨", R.drawable.w5);
        weatherImage.put("小雨",R.drawable.w6);
        weatherImage.put("中雨",R.drawable.w5);
        weatherImage.put("大雨",R.drawable.w7);
        weatherImage.put("阵雨",R.drawable.w4);
        weatherImage.put("雷阵雨",R.drawable.w10);
        weatherImage.put("暴雨",R.drawable.w9);
        weatherImage.put("大暴雨",R.drawable.w11);
        weatherImage.put("特大暴雨",R.drawable.w12);
        weatherImage.put("冰雹",R.drawable.w8);
        weatherImage.put("雨夹雪",R.drawable.w13);
        weatherImage.put("阵雪",R.drawable.w14);
        weatherImage.put("小雪",R.drawable.w15);
        weatherImage.put("中雪",R.drawable.w16);
        weatherImage.put("大雪",R.drawable.w17);
        weatherImage.put("暴雪",R.drawable.w17);
        weatherImage.put("雾",R.drawable.w18);
        weatherImage.put("有风", R.drawable.w19);
        weatherImage.put("冻雨",R.drawable.w22);
        weatherImage.put("龙卷风",R.drawable.w21);
        weatherImage.put("轻雾",R.drawable.w18);
        weatherImage.put("霾",R.drawable.w20);
    }

}
