package com.example.meteotablet2.utils;

import android.app.Application;
import android.content.Context;

import com.yandex.mapkit.MapKitFactory;

public class MapKitInitializator extends Application {

    static private boolean initialized = false;
    private String apiKey;
    private Context context;

    public MapKitInitializator(String apiKey, Context context){
        this.apiKey = apiKey;
        this.context = context;
    }

    public void setInitialized(){
        try{
            if(!initialized){
                MapKitFactory.setApiKey(this.apiKey);
                MapKitFactory.initialize(this.context);
                initialized = true;
            }
        }catch (Exception e){
            throw e;
        }
    }

}
