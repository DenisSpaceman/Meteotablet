package com.example.meteotablet2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Session;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class YandexMap extends AppCompatActivity implements Session.SearchListener, CameraListener {
    private MapView mapView;
    private EditText searchEdit;
    private SearchManager searchManager;
    private Session searchSession;
    private final String MAPKIT_API_KEY = "97c8d429-6d0a-409a-867e-38349c56c9e5";
    private Button saveButton;
    private Point cameraPos;
    private String latitude, longtitude;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private List<HistorySaver> historyList = new ArrayList<>();
    private SimpleDateFormat simpleDateFormat;

    private void submitQuery(String query) {
        searchSession = searchManager.submit(
                query,
                VisibleRegionUtils.toPolygon(mapView.getMap().getVisibleRegion()),
                new SearchOptions(),
                this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        SearchFactory.initialize(this);

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);

        setContentView(R.layout.yandex_map);
        super.onCreate(savedInstanceState);

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        mapView = findViewById(R.id.mapview);
        mapView.getMap().addCameraListener(this);

        saveButton = findViewById(R.id.saveButton);

        searchEdit = findViewById(R.id.search_edit);
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    submitQuery(searchEdit.getText().toString());
                }
                return false;
            }
        });

        submitQuery(searchEdit.getText().toString());

        loadSearchHistory();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraPos = mapView.getMap().getCameraPosition().getTarget();
                String lat = String.valueOf(cameraPos.getLatitude());
                String lon = String.valueOf(cameraPos.getLongitude());

                editor = prefs.edit();
                editor.putString("latitude", lat).apply();
                editor.putString("longtitude", lon).apply();

                long millis = System.currentTimeMillis();
                simpleDateFormat = new SimpleDateFormat("dd MMM hh:mm");

                HistorySaver row = new HistorySaver(searchEdit.getText().toString(), simpleDateFormat.format(millis).toString());
                historyList.add(row);

                saveHistory();

                Intent intent = new Intent(YandexMap.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public void onSearchResponse(Response response) {
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        mapObjects.clear();

        for (GeoObjectCollection.Item searchResult : response.getCollection().getChildren()) {
            Point resultLocation = searchResult.getObj().getGeometry().get(0).getPoint();
            if (resultLocation != null) {
                mapObjects.addPlacemark(
                        resultLocation,
                        ImageProvider.fromResource(this, R.drawable.search_result));
            }
        }
    }

    @Override
    public void onSearchError(Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraPositionChanged(
            Map map,
            CameraPosition cameraPosition,
            CameraUpdateReason cameraUpdateReason,
            boolean finished) {
        if (finished) {
            submitQuery(searchEdit.getText().toString());
        }
    }

    public void saveHistory(){
        Gson gson = new Gson();
        String json = gson.toJson(historyList);
        editor.putString("search_history", json);
        editor.apply();
    }

    public void loadSearchHistory(){
        Gson gson = new Gson();
        String json = getSharedPreferences("settings", Context.MODE_PRIVATE).getString("search_history", "");
        Type type = new TypeToken<List<HistorySaver>>() {}.getType();
        historyList = gson.fromJson(json, type);
    }
}
