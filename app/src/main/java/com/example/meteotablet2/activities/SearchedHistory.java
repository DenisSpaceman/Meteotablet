package com.example.meteotablet2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.meteotablet2.R;
import com.example.meteotablet2.entities.SearchedData;
import com.example.meteotablet2.utils.AppDB;
import com.example.meteotablet2.utils.RecyclerAdapter;
import com.example.meteotablet2.utils.RecyclerInterface;
import com.example.meteotablet2.utils.SearchDao;

import java.util.List;
import java.util.Objects;

public class SearchedHistory extends AppCompatActivity implements RecyclerInterface {

    private ImageView home_button, clear_db_button;
    private List<SearchedData> searchedDataList;
    private RecyclerView listSearch;
    private String nameDB = "searchedHistoryBase";
    private AppDB appDB;
    private SearchDao searchDao;
    private RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_history);

        home_button = findViewById(R.id.home_button_search);
        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchedHistory.this, CurrentWeather.class);
                startActivity(intent);
            }
        });

        listSearch = findViewById(R.id.searchView);
        appDB = Room.databaseBuilder(
                getApplicationContext(),
                        AppDB.class,
                        nameDB)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        searchDao = appDB.searchDao();

        searchedDataList = searchDao.getAll();

        listSearch.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter = new RecyclerAdapter(this, searchedDataList, this);
        listSearch.setAdapter(recyclerAdapter);

        clear_db_button = findViewById(R.id.remove_data_button);
        clear_db_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDao.deleteAll();
                recyclerAdapter.clearData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            searchedDataList.clear();
            searchedDataList.addAll(searchDao.getAll());
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(int postition) {
        if (Objects.equals(searchedDataList.get(postition).window, "c")) {
            Intent intent = new Intent(SearchedHistory.this, CurrentCardActivity.class);
            intent.putExtra("id", String.valueOf(searchedDataList.get(postition).id));
            startActivity(intent);
        }
        if (Objects.equals(searchedDataList.get(postition).window, "f")) {
            Intent intent = new Intent(SearchedHistory.this, ForecastCardActivity.class);
            intent.putExtra("id", String.valueOf(searchedDataList.get(postition).id));
            startActivity(intent);
        }
    }
}