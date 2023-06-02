package com.example.meteotablet2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.meteotablet2.R;
import com.example.meteotablet2.entities.SearchedData;
import com.example.meteotablet2.utils.AppDB;
import com.example.meteotablet2.utils.SearchDao;

public class CurrentCardActivity extends AppCompatActivity {

    private AppDB appDB;
    private ImageView back_button;
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
            precipitations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_card);

        Intent intent = getIntent();

        time_date = findViewById(R.id.time_date);

        city = findViewById(R.id.city);

        back_button = findViewById(R.id.back_button_current);

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
        precipitations = findViewById(R.id.precipitation);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CurrentCardActivity.this, SearchedHistory.class);
                startActivity(intent);
            }
        });

        appDB = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDB.class,
                        "searchedHistoryBase")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        SearchDao searchDao = appDB.searchDao();
        String extra = intent.getStringExtra("id");

        SearchedData searchedData = searchDao.getDataById(Integer.parseInt(extra));
        showWeatherData(searchedData.date, searchedData.city, searchedData.temperature, searchedData.pressure_Hgmm,
                        searchedData.pressure_gPa, searchedData.humidity, searchedData.uv_index,
                        searchedData.aqi, searchedData.clouds, searchedData.wind_speed,
                        searchedData.wind_direction, searchedData.visiblity, searchedData.dew_point, searchedData.precipitations);
    }

    public void showWeatherData(String date, String city, String temperature, String pressure_hgmm,
                                String pressure_gpa, String humidity, String uv_index,
                                String aqi, String clouds, String wind_speed,
                                String wind_direction, String visiblity, String dew_point, String percipitations){

        this.time_date.setText(date);
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
        this.time_date.setText(date);
        this.dew_point.setText(dew_point);
        this.precipitations.setText(percipitations);

    }
}
