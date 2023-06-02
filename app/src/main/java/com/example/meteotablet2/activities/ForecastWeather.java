package com.example.meteotablet2.activities;

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
import androidx.room.Room;

import com.example.meteotablet2.R;
import com.example.meteotablet2.entities.SearchedData;
import com.example.meteotablet2.utils.AppDB;
import com.example.meteotablet2.utils.SearchDao;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.ClientProtocolException;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class ForecastWeather extends AppCompatActivity {

    private ImageView update_button, currentForecast;
    private TextView city, time_date;
    private String city_name;

    private TextView
            temperature,
            humidity,
            uv_index,
            wind_speed,
            wind_direction,
            visiblity,
            precipitation,
            pressure_gpa,
            pressure_hgmm,
            clouds,
            dew_point;

    private SimpleDateFormat simpleDateFormat;

    private String latitude, longtitude;

    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forecast_activity);

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
                                simpleDateFormat = new SimpleDateFormat("yyyy dd MMM");
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

        city.setOnClickListener(view -> startActivity(new Intent(ForecastWeather.this, YandexMap.class)));

        temperature = findViewById(R.id.temperature);
        uv_index = findViewById(R.id.uv_index);
        wind_speed = findViewById(R.id.wind_speed);
        wind_direction = findViewById(R.id.wind_direction);
        humidity = findViewById(R.id.humidity);
        visiblity = findViewById(R.id.visibility);
        precipitation = findViewById(R.id.precipitation);
        pressure_gpa = findViewById(R.id.pressure_Gpa);
        pressure_hgmm = findViewById(R.id.pressure_mmhg);
        clouds = findViewById(R.id.clouds);
        dew_point = findViewById(R.id.dew_point);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_button.startAnimation(rotate);
                try {
                    JSONObject weather_report = getWeather();
                    if (weather_report != null){

                        String city_s = weather_report.getJSONObject("location").getString("name");
                        String temperature_S = weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("avgtemp_c") + " °C";
                        String humidity_S = weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("avghumidity") + "%";
                        String uv_index_s = weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("uv");
                        String wind_speed_S = String.valueOf((int) (weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getDouble("maxwind_kph") * 0.28)) + " м/с";
                        String visiblity_s = weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("avgvis_km") + " км";
                        String precipitations_S = weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONObject("day").getString("totalprecip_mm") + " мм";

                        JSONArray hours = weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getJSONArray("hour");

                        int val_pressure_Gpa = 0;
                        int val_pressure_hgmm = 0;
                        int val_clouds = 0;
                        int val_wind_direction = 0;
                        int val_dew_point = 0;

                        for (int i = 0; i < 24; i++){
                            JSONObject hour = new JSONObject(hours.get(i).toString());
                            val_pressure_Gpa += Integer.parseInt(hour.get("pressure_mb").toString());
                            val_pressure_hgmm += (int) (Double.parseDouble(hour.get("pressure_in").toString()) * 25.4);
                            val_clouds += Integer.parseInt(hour.get("cloud").toString());
                            val_wind_direction += Integer.parseInt(hour.get("wind_degree").toString());
                            val_dew_point += (int) (Double.parseDouble(hour.get("dewpoint_c").toString()));
                        }

                        val_pressure_Gpa = val_pressure_Gpa / 24;
                        val_pressure_hgmm = val_pressure_hgmm / 24;
                        val_clouds = val_clouds / 24;
                        val_wind_direction = val_wind_direction / 24;
                        val_dew_point = val_dew_point / 24;

                        city.setText(city_s);
                        temperature.setText(temperature_S);
                        humidity.setText(humidity_S);
                        uv_index.setText(uv_index_s);
                        wind_speed.setText(wind_speed_S);
                        visiblity.setText(visiblity_s);
                        precipitation.setText(precipitations_S);

                        pressure_gpa.setText(String.valueOf(val_pressure_Gpa) + " гПа");
                        pressure_hgmm.setText(String.valueOf(val_pressure_hgmm) + " мм");
                        clouds.setText(String.valueOf(val_clouds) + "%");
                        wind_direction.setText(String.valueOf(val_wind_direction) + "°");
                        dew_point.setText(String.valueOf(val_dew_point) + " °C");

                        simpleDateFormat = new SimpleDateFormat("yyyy dd MMM");
                        String date = simpleDateFormat.format(Long.parseLong(weather_report.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(1).getString("date_epoch")) * 1000);

                        SearchedData searchedData = new SearchedData(date, city_s, temperature_S,
                                String.valueOf(val_pressure_Gpa), String.valueOf(val_pressure_hgmm),
                                humidity_S, uv_index_s, "0", String.valueOf(val_clouds),
                                wind_speed_S, String.valueOf(val_wind_direction), visiblity_s,
                                String.valueOf(val_dew_point), precipitations_S, "f");

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
            }
        });

        currentForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForecastWeather.this, CurrentWeather.class);
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
