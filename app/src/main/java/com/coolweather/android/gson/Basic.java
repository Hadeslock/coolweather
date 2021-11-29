package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Author: Hadeslock
 * Created on 2021/11/29 16:46
 * Email: hadeslock@126.com
 * Desc: GSON实体类
 */
public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }

}
