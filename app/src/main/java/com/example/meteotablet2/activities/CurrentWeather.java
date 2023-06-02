package com.example.meteotablet2.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.example.meteotablet2.R;
import com.example.meteotablet2.entities.SearchedData;
import com.example.meteotablet2.utils.AppDB;
import com.example.meteotablet2.utils.SearchDao;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class CurrentWeather extends AppCompatActivity {
    private ImageView update_button, tomorrowForecast, history_button;
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
            dew_point,
            percipitations;

    private SimpleDateFormat simpleDateFormat;

    private String latitude, longtitude;
    private JSONObject weather_report;

    private SharedPreferences mSettings;

    private NotificationManager nm;
    private SharedPreferences.Editor editor;
    private final int NOTIFICATION_CHANNEL_ID = 127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_activity);

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
                                simpleDateFormat = new SimpleDateFormat("yyyy dd MMM HH:mm");
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
        history_button = findViewById(R.id.history_button);

        humidity = findViewById(R.id.humidity);

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
        percipitations = findViewById(R.id.precipitation);

        if(checkInternet()){

            city.setOnClickListener(view -> startActivity(new Intent(CurrentWeather.this, YandexMap.class)));

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

                            String city_s = weather_report.getJSONObject("location").getString("name");
                            String temperature_s = weather_report.getJSONObject("current").getString("temp_c") + " °C";
                            String pressure_hgmm_s = String.valueOf((int) (weather_report.getJSONObject("current").getDouble("pressure_in") * 25.4));
                            String pressure_gpa_s = String.valueOf((int) weather_report.getJSONObject("current").getDouble("pressure_mb")) + " гПа";
                            String humidity_s = weather_report.getJSONObject("current").getString("humidity") + "%";
                            String uv_index_s = weather_report.getJSONObject("current").getString("uv");
                            String aqi = weather_report.getJSONObject("current").getJSONObject("air_quality").getString("us-epa-index");
                            String clouds_s = weather_report.getJSONObject("current").getString("cloud") + "%";
                            String wind_speed_s = String.valueOf((int) (weather_report.getJSONObject("current").getDouble("wind_kph") * 0.28)) + " м/с";
                            String wind_direction_s = weather_report.getJSONObject("current").getString("wind_degree") + "°";
                            String visiblity_S = weather_report.getJSONObject("current").getString("vis_km") + " км";
                            String dew_point_s = "0";
                            String percipitations_s = "0";

                            try {
                                dew_point_s = getDewPoint();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            try {
                                percipitations_s = getPercipitations();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            showWeatherData(city_s, temperature_s, pressure_hgmm_s, pressure_gpa_s,
                                    humidity_s, uv_index_s, aqi, clouds_s, wind_speed_s,
                                    wind_direction_s, visiblity_S, percipitations_s);

                            simpleDateFormat = new SimpleDateFormat("yyyy dd MMM HH:mm");
                            long millis =  System.currentTimeMillis();
                            String date = simpleDateFormat.format(millis);

                            SearchedData searchedData = new SearchedData(
                                    date, city_s, temperature_s, pressure_gpa_s, pressure_hgmm_s,
                                    humidity_s, uv_index_s, aqi, clouds_s, wind_speed_s,
                                    wind_direction_s, visiblity_S, dew_point_s, percipitations_s, "c");

                            AppDB appDB = Room.databaseBuilder(
                                            getApplicationContext(),
                                            AppDB.class,
                                            "searchedHistoryBase")
                                    .fallbackToDestructiveMigration()
                                    .allowMainThreadQueries()
                                    .build();

                            SearchDao searchDao = appDB.searchDao();
                            searchDao.insert(searchedData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        dew_point.setText(getDewPoint());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            tomorrowForecast.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CurrentWeather.this, ForecastWeather.class);
                    startActivity(intent);
                }
            });

        }else{
            Toast.makeText(this, "Ошибка подключения. Проверьте соединение.", Toast.LENGTH_LONG).show();
        }

        history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CurrentWeather.this, SearchedHistory.class);
                startActivity(intent);
            }
        });
    }

    protected void getWeatherAlert() throws Exception {
        if (mSettings.contains("latitude")) {
            latitude = mSettings.getString("latitude", "");
            longtitude = mSettings.getString("longtitude", "");
        }
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.weatherapi.com/v1/forecast.json").newBuilder();
        urlBuilder.addQueryParameter("key", "30c7a2c178344548800195018230404");
        urlBuilder.addQueryParameter("q", latitude + "," + longtitude);
        urlBuilder.addQueryParameter("days", "1");
        urlBuilder.addQueryParameter("alerts", "yes");

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .cacheControl(new CacheControl.Builder().maxStale(30, TimeUnit.DAYS).build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()){
                    Log.e("GET_WEATHER_ALERT", String.valueOf(response.code()));
                }else{
                    final String responseData = response.body().string();
                    try {
                        JSONObject data = new JSONObject(responseData);
                        JSONArray alerts = data.getJSONObject("alerts").getJSONArray("alert");
                        for (int i = 0; i < alerts.length(); i++){
                            JSONObject alert = new JSONObject(alerts.get(i).toString());
                            weatherNotififcation(alert.getString("event"), alert.getString("desc"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
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

    protected String getDewPoint() throws Exception {
        if (mSettings.contains("latitude")) {
            latitude = mSettings.getString("latitude", "");
            longtitude = mSettings.getString("longtitude", "");
        }

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("http://api.weatherapi.com/v1/forecast.json?key=30c7a2c178344548800195018230404&q=" + latitude + "," + longtitude + "&days=1");
        HttpResponse response;
        JSONObject data = null;
        String dew_point_s = "";
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                data = new JSONObject(EntityUtils.toString(entity));
                long millis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("HH");
                dew_point_s = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONArray("hour").getJSONObject(Integer.parseInt(sdf.format(millis)) - 1).getString("dewpoint_c") + " °C";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dew_point_s;
    }

    protected String getPercipitations() throws Exception {
        if (mSettings.contains("latitude")) {
            latitude = mSettings.getString("latitude", "");
            longtitude = mSettings.getString("longtitude", "");
        }

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("http://api.weatherapi.com/v1/forecast.json?key=30c7a2c178344548800195018230404&q=" + latitude + "," + longtitude + "&days=1");
        HttpResponse response;
        JSONObject data = null;
        String percipitations_s = "";
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                data = new JSONObject(EntityUtils.toString(entity));
                long millis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("HH");
                percipitations_s = data.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONArray("hour").getJSONObject(Integer.parseInt(sdf.format(millis)) - 1).getString("precip_mm") + " мм";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return percipitations_s;
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

    public void showWeatherData(String city, String temperature, String pressure_hgmm,
                                String pressure_gpa, String humidity, String uv_index,
                                String aqi, String clouds, String wind_speed,
                                String wind_direction, String visiblity, String percipitations){

        this.city.setText(city);
        this.temperature.setText(temperature);
        this.pressure_hgmm.setText(pressure_hgmm);
        this.humidity.setText(humidity);
        this.pressure_gpa.setText(pressure_gpa);
        this.uv_index.setText(uv_index);
        this.air_quality.setText(aqi);
        this.clouds.setText(clouds);
        this.wind_speed.setText(wind_speed);
        this.wind_direction.setText(wind_direction);
        this.visiblity.setText(visiblity);
        this.percipitations.setText(percipitations);

    }

    public boolean checkInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null &&
            connectivityManager.getActiveNetworkInfo().isConnected() &&
            connectivityManager.getActiveNetworkInfo().isAvailable()){
            return true;
        }else{
            return false;
        }
    }

}
