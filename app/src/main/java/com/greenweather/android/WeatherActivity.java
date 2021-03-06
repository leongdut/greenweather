package com.greenweather.android;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.greenweather.android.gson.Forecast;
import com.greenweather.android.gson.Weather;
import com.greenweather.android.service.AutoUpdateService;
import com.greenweather.android.util.HttpUtil;
import com.greenweather.android.util.ShareUtil;
import com.greenweather.android.util.Utility;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    //下拉更新
    public SwipeRefreshLayout swipeRefresh;
    //天气拖动页面
    private ScrollView weatherLayout;
    //同步中文字显示
    private Context context;

   // private Button navButton;
    //顶部城市名
    private TextView titleCity;
    //数据更新时间显示
    private TextView titleUpdateTime;
    //今天温度
    private TextView degreeText;
    //天气信息
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    //湿度质量
    private TextView aqiText;
    //空气质量
    private TextView pm25Text;
    //提示建议
    private TextView comfortText;
    //洗车建议
    private TextView carWashText;
    //运动建议
    private TextView sportText;
    //背景 图片
    private ImageView bingPicImg;
    private String mWeatherId;

    //向下弹出菜单
    private View popupMenuView;
    private PopupWindow popupMenu;
    //右上角菜单控件
    private ImageView menuImageView;
    //popupMenu菜单item
    private TextView changeCityTv, shareWeatherTv, settingTv,network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        /*
         *下面初始化各控件
         * */

        //弹出菜单View控件
        popupMenuView = getLayoutInflater().inflate(R.layout.popup_menu_layout, null);
        menuImageView = (ImageView) findViewById(R.id.menu_image);
        menuImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu = new PopupWindow(popupMenuView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                popupMenu.setAnimationStyle(R.style.popupInOutAnimation);
                popupMenu.setFocusable(true);
                popupMenu.setOutsideTouchable(true);
                ColorDrawable dw = new ColorDrawable(000000);
                popupMenu.setBackgroundDrawable(dw);
                popupMenu.showAtLocation(v, Gravity.TOP, 0, 60);

                setPopupMenuClick();
            }
        });
        //其他空间
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      //  navButton = (Button) findViewById(R.id.nav_button);
        context=this;

        //缓存
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

      /*  navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });*/
        //背景图片
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        //注册并获取和风天气key
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=c3dcdd6b5d69481183f5700b5f0a0fdd";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载背景图片
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度分析：" + weather.suggestion.comfort.info;
        String carWash = "洗车建议：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
    /**
     * 为弹出菜单设置item点击事件
     */
    private void setPopupMenuClick(){
        //关闭目录按键
        ImageView closeMenu = (ImageView) popupMenuView.findViewById(R.id.close_menu);
        //切换城市
        changeCityTv = (TextView) popupMenuView.findViewById(R.id.change_city);
        //分享功能
        shareWeatherTv = (TextView) popupMenuView.findViewById(R.id.share_weather);
        //关于作者
        settingTv = (TextView) popupMenuView.findViewById(R.id.setting);
        //访问天气网
        network=(TextView)popupMenuView.findViewById(R.id.network) ;

        //下面是一系列按钮监听实现
        closeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupMenu!= null && popupMenu.isShowing()){
                    popupMenu.dismiss();
                }
            }
        });
        changeCityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        shareWeatherTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupMenu != null){
                    popupMenu.dismiss();
                }
                ShareUtil.showShare(context);
            }
        });
        network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加点击响应事件
                Intent intent =new Intent(WeatherActivity.this,WebView1.class);
                //启动
                startActivity(intent);
            }
        });
        settingTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(WeatherActivity.this,about_me.class);
                startActivity(intent);
            }
        });
    }




}

