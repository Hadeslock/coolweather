package com.coolweather.android.gson;

/**
 * Author: Hadeslock
 * Created on 2021/11/29 16:47
 * Email: hadeslock@126.com
 * Desc: GSON实体类
 */
public class AQI {

    public AQICity city;

    public class AQICity {

        public String aqi;

        public String pm25;

    }

}
