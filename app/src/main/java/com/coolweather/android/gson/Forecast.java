package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Author: Hadeslock
 * Created on 2021/11/29 16:49
 * Email: hadeslock@126.com
 * Desc: GSON实体类
 */
public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {

        public String max;

        public String min;

    }

    public class More {

        @SerializedName("txt_d")
        public String info;

    }

}