package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Author: Hadeslock
 * Created on 2021/11/29 16:48
 * Email: hadeslock@126.com
 * Desc: GSON实体类
 */
public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;

    }

}