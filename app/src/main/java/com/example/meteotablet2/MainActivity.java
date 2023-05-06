package com.example.meteotablet2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private ImageView update_button, tomorrowForecast, airports_data, history_button;
    private TextView city, time_date;

    private TextView
            temperature,
            pressure_hgmm,
            humidity,
            pressure_gpa,
            uv_index,
            air_quality,
            clouds,
            wind_speed,
            wind_direction,
            visiblity,
            dew_point;

    private SimpleDateFormat simpleDateFormat;

    private String latitude, longtitude;

    private SharedPreferences mSettings;

    private NotificationManager nm;
    private final int NOTIFICATION_CHANNEL_ID = 127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        final Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);

        mSettings = getSharedPreferences("settings", Context.MODE_PRIVATE);

        time_date = findViewById(R.id.time_date);

        Thread date = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                long millis = System.currentTimeMillis();
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
        tomorrowForecast = findViewById(R.id.tomorrowForecast);
        airports_data = findViewById(R.id.airports_data);
        history_button = findViewById(R.id.history_button);

        humidity = findViewById(R.id.humidity);

        city.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, YandexMap.class)));

        temperature = findViewById(R.id.temperature);
        pressure_hgmm = findViewById(R.id.pressure_hgmm);
        pressure_gpa = findViewById(R.id.pressure_gpa);
        uv_index = findViewById(R.id.uv_index);
        air_quality = findViewById(R.id.epa_index);
        clouds = findViewById(R.id.clouds);
        wind_speed = findViewById(R.id.wind_speed);
        visiblity = findViewById(R.id.visiblity);
        wind_direction = findViewById(R.id.wind_direction);
        dew_point = findViewById(R.id.dew_point);

        Thread weatherNotification = new Thread() {
            @Override
            public void run(){
                try {
                    getWeatherAlert();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        weatherNotification.start();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_button.startAnimation(rotate);
                try {
                    JSONObject weather_report = getWeather();
                    if (weather_report != null) {
                        city.
                                setText(weather_report.getJSONObject("location").getString("name"));
                        temperature.
                                setText(weather_report.getJSONObject("current").getString("temp_c") + "°C");
                        pressure_hgmm.
                                setText(String.valueOf((int) (weather_report.getJSONObject("current").getDouble("pressure_in") * 25.4)));
                        humidity.
                                setText(weather_report.getJSONObject("current").getString("humidity") + "%");
                        pressure_gpa.
                                setText(String.valueOf((int) weather_report.getJSONObject("current").getDouble("pressure_mb")));
                        uv_index.
                                setText(weather_report.getJSONObject("current").getString("uv"));
                        air_quality.
                                setText(weather_report.getJSONObject("current").getJSONObject("air_quality").getString("us-epa-index"));
                        clouds.
                                setText(weather_report.getJSONObject("current").getString("cloud") + "%");
                        wind_speed.
                                setText(String.valueOf((int) (weather_report.getJSONObject("current").getDouble("wind_kph") * 0.28)) + " m/s");
                        wind_direction.
                                setText(weather_report.getJSONObject("current").getString("wind_degree") + "°");
                        visiblity.
                                setText(weather_report.getJSONObject("current").getString("vis_km") + " km");
                        // dew_point.
                        // setText(weather_report.getJSONObject("current").getString("wind_degree") + "°C");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        tomorrowForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TomorrowWeather.class);
                startActivity(intent);
            }
        });

        airports_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AirportData.class);
                startActivity(intent);
            }
        });

        history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchHistory.class);
                startActivity(intent);
            }
        });
    }

    protected void getWeatherAlert() throws Exception {
        if (mSettings.contains("latitude")) {
            latitude = mSettings.getString("latitude", "");
            longtitude = mSettings.getString("longtitude", "");
        }
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("http://api.weatherapi.com/v1/forecast.json?key=30c7a2c178344548800195018230404&q=" + latitude + "," + longtitude + "&days=2&alerts=yes");
        HttpResponse response;
        JSONObject data = null;
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                System.out.println(EntityUtils.toString(entity));
                data = new JSONObject(EntityUtils.toString(entity));
                System.out.println(data.getJSONObject("alerts").getJSONArray("alert").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected JSONObject getWeather() throws Exception {
        if (mSettings.contains("latitude")) {
            latitude = mSettings.getString("latitude", "");
            longtitude = mSettings.getString("longtitude", "");
        }
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("http://api.weatherapi.com/v1/current.json?key=30c7a2c178344548800195018230404&q=" + latitude + "," + longtitude + "&aqi=yes");
        HttpResponse response;
        JSONObject data = null;
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                data = new JSONObject(EntityUtils.toString(entity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public void weatherNotififcation(String title, String text) {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            channel = new NotificationChannel(
                    "WEATHER_NOTIFICATIONS",
                    "WEATHER NOTIFICATIONS",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, "WEATHER_NOTIFICATIONS")
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.history)
                    .build();
            notificationManager.notify(NOTIFICATION_CHANNEL_ID, notification);
        }
    }
}
