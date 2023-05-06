package com.example.meteotablet2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AirportData extends AppCompatActivity {

    private EditText airport_code;
    private Button get_button;
    private ImageView home_button;
    private TextView metar, taf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airports_data);

        airport_code = findViewById(R.id.airport_code);
        get_button = findViewById(R.id.get_button);

        home_button = findViewById(R.id.home_button_search);

        metar = findViewById(R.id.metar);
        taf = findViewById(R.id.taf);

        get_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    try {
                        JSONObject data = getData(airport_code.getText().toString());
                        System.out.println(data.toString());
                        if (data.has("status") == false){
                            metar.setText(data.getString("metar"));
                            taf.setText(data.getString("taf"));
                        }else{
                            metar.setText("No data. Try again!");
                            taf.setText("No data. Try again!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AirportData.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    protected JSONObject getData(String Acode) throws Exception {;
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("https://metartaf.ru/" + Acode + ".json");
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
        System.out.println("Data:");
        System.out.println(data);
        return data;
    }
}