package com.example.meteotablet2.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SearchedData {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "city")
    public String city;

    @ColumnInfo(name = "temperature")
    public String temperature;

    @ColumnInfo(name = "pressure_gPa")
    public String pressure_gPa;

    @ColumnInfo(name = "pressure_Hgmm")
    public String pressure_Hgmm;

    @ColumnInfo(name = "humidity")
    public String humidity;

    @ColumnInfo(name = "uv_index")
    public String uv_index;

    @ColumnInfo(name = "aqi")
    public String aqi;

    @ColumnInfo(name = "clouds")
    public String clouds;

    @ColumnInfo(name = "wind_speed")
    public String wind_speed;

    @ColumnInfo(name = "wind_direction")
    public String wind_direction;

    @ColumnInfo(name = "visiblity")
    public String visiblity;

    @ColumnInfo(name = "dew_point")
    public String dew_point;

    @ColumnInfo(name = "precipitataions")
    public String precipitations;

    @ColumnInfo(name = "window")
    public String window;

    public SearchedData(String date, String city, String temperature,
                        String pressure_gPa, String pressure_Hgmm, String humidity,
                        String uv_index, String aqi, String clouds,
                        String wind_speed, String wind_direction, String visiblity,
                        String dew_point, String precipitations, String window){

        this.date = date;
        this.city = city;
        this.temperature = temperature;
        this.pressure_gPa = pressure_gPa;
        this.pressure_Hgmm = pressure_Hgmm;
        this.humidity = humidity;
        this.uv_index = uv_index;
        this.aqi = aqi;
        this.clouds = clouds;
        this.wind_speed = wind_speed;
        this.wind_direction = wind_direction;
        this.visiblity = visiblity;
        this.dew_point = dew_point;
        this.precipitations = precipitations;
        this.window = window;

    }

}
