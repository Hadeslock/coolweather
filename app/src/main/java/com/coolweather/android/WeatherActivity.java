package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {


    //天气信息外框架
    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefresh;// 下拉刷新
    private ScrollView weatherLayout;// 信息流
    //当前天气
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    //预报信息
    private LinearLayout forecastLayout;
    //空气质量
    private TextView aqiText;
    private TextView pm25Text;
    //生活建议
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;// 背景图片
    private Button navButton;// 左侧导航提示按钮

    private String mWeatherId;// 当前城市对应的天气id，当前已显示天气，刷新时使用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏显示
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);// 设置状态栏透明
        setContentView(R.layout.activity_weather);

        // 初始化各控件
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        bingPicImg = findViewById(R.id.bing_pic_img);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);

        //查询数据库
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            assert weather != null;
            mWeatherId = weather.basic.weatherId;// 获取之前的天气id
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        //下拉刷新
        swipeRefresh.setColorSchemeResources(R.color.purple_500);
        swipeRefresh.setOnRefreshListener(() -> requestWeather(mWeatherId));

        //设置背景图片
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

        //左侧导航栏点击事件
        navButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    /*
     * 根据天气id请求城市天气信息
     * @author Hadeslock
     * @time 2021/11/29 17:31
     */
    public void requestWeather(final String weatherId) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        //发送请求
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                //解析json数据
                final String responseText = Objects.requireNonNull(response.body()).string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                //切换到主线程继续
                runOnUiThread(() -> {
                    if (weather != null && "ok".equals(weather.status)) {
                        //请求天气成功了，将返回的数据缓存到SharedPreferences
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                        //更新weatherId
                        mWeatherId = weather.basic.weatherId;
                        //进行内容显示
                        showWeatherInfo(weather);
                    } else {
                        //简单提示
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                    //刷新事件结束，并隐藏刷新进度条
                    swipeRefresh.setRefreshing(false);
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                            Toast.LENGTH_SHORT).show();
                    //刷新事件结束，并隐藏刷新进度条
                    swipeRefresh.setRefreshing(false);
                });
            }
        });

        // 每次请求天气信息的时候同时刷新背景图片
        loadBingPic();
    }

    /*
     * 处理并展示Weather实体类中的数据
     * @author Hadeslock
     * @time 2021/11/29 17:40
     */
    private void showWeatherInfo(Weather weather) {
        //基本信息和当前天气
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //未来几天的天气
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this)
                                .inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        //天气质量
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        //生活建议
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);
        //开启后台服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /*
     * 加载必应每日一图
     * @author Hadeslock
     * @time 2021/11/29 18:35
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        //发送请求
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                final String bingPic = Objects.requireNonNull(response.body()).string();
                //将图片缓存到数据库
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                //刷新背景图片
                runOnUiThread(() -> Glide.with(WeatherActivity.this)
                        .load(bingPic).into(bingPicImg));
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });
    }

}