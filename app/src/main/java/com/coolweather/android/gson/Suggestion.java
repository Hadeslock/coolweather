package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Author: Hadeslock
 * Created on 2021/11/29 16:48
 * Email: hadeslock@126.com
 * Desc: GSON实体类
 */
public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort {

        @SerializedName("txt")
        public String info;

    }

    public class CarWash {

        @SerializedName("txt")
        public String info;

    }

    public class Sport {

        @SerializedName("txt")
        public String info;

    }

}

