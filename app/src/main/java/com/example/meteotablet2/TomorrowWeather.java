package com.example.meteotablet2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.ClientProtocolException;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class TomorrowWeather extends AppCompatActivity {

    private ImageView update_button, currentForecast;
    private TextView city, time_date;
    private String city_name;

    private TextView
            temperature,
            humidity,
            uv_index,
            wind_speed,
            visiblity,
            precipitation;

    private SimpleDateFormat simpleDateFormat;

    private String latitude, longtitude;

    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tomorrow_forecast);

        final Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);

        mSettings = getSharedPreferences("settings", Context.MODE_PRIVATE);

        time_date = findViewById(R.id.time_date);

        Thread date = new Thread(){
            @Override
            public void run(){
                try{
                    while (!isInterrupted()){
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                long millis = System.currentTimeMillis() + 86400000;
                                simpleDateFormat = new SimpleDateFormat("dd MMM hh:mm");
                                time_date.setText(simpleDateFormat.format(millis));
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        date.start();

        city = findViewById(R.id.city);

        update_button = findViewById(R.id.update_button);
        currentForecast = findViewById(R.id.currentForecast);

        city.setOnClickListener(view -> startActivity(new Intent(TomorrowWeather.this, YandexMap.class)));

        temperature = findViewById(R.id.temperature);
        uv_index = findViewById(R.id.uv_index);
        wind_speed = findViewById(R.id.wind_speed);
        humidity = findViewById(R.id.humidity);
        visiblity = findViewById(R.id.visibility);
        precipitation = findViewById(R.id.precipitation);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_button.startAnimation(rotate);
                try {
                    JSONObject weather_report = getWeather();
                    if (weather_report != null){
                        city.
                                setText(weather_report.getJSONObject("location").getString("name"));
                        temperature.
                                setText(weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("avgtemp_c") + "°C");
                        humidity.
                                setText(weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("avghumidity") + "%");
                        uv_index.
                                setText(weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("uv"));
                        wind_speed.
                                setText(String.valueOf((int) (weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getDouble("maxwind_kph") * 0.28)));
                        visiblity.
                                setText(weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("avgvis_km"));
                        precipitation.
                                setText(weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("totalprecip_mm"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        currentForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TomorrowWeather.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    protected JSONObject getWeather() throws Exception {;
        if(mSettings.contains("latitude")) {
            latitude = mSettings.getString("latitude", "");
            longtitude = mSettings.getString("longtitude", "");
        }
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("http://api.weatherapi.com/v1/forecast.json?key=30c7a2c178344548800195018230404&q=" + latitude + "," + longtitude + "&days=2");
        HttpResponse response;
        JSONObject data = null;
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null){
                data = new JSONObject(EntityUtils.toString(entity));
            }
        } catch (ClientProtocolException e){
            e.printStackTrace();
        }
        return data;
    }
}
