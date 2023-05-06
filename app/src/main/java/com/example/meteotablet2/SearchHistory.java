package com.example.meteotablet2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchHistory extends AppCompatActivity {

    private ImageView home_button;
    private List<HistorySaver> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_history);

        loadSearchHistory();

        home_button = findViewById(R.id.home_button_search);
        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(historyList.size());

                Intent intent = new Intent(SearchHistory.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void loadSearchHistory(){
        Gson gson = new Gson();
        String json = getSharedPreferences("settings", Context.MODE_PRIVATE).getString("search_history", "");
        Type type = new TypeToken<List<HistorySaver>>() {}.getType();
        historyList = gson.fromJson(json, type);
    }
}