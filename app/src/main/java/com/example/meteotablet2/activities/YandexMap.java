package com.example.meteotablet2.activities;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meteotablet2.R;
import com.example.meteotablet2.utils.MapKitInitializator;
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

public class YandexMap extends AppCompatActivity implements Session.SearchListener, CameraListener {
    private MapView mapView;
    private EditText searchEdit;
    private SearchManager searchManager;
    private Session searchSession;
    private final String MAPKIT_API_KEY = "97c8d429-6d0a-409a-867e-38349c56c9e5";
    private Button saveButton;
    private Point cameraPos;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private void submitQuery(String query) {
        searchSession = searchManager.submit(
                query,
                VisibleRegionUtils.toPolygon(mapView.getMap().getVisibleRegion()),
                new SearchOptions(),
                this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MapKitInitializator mapKitInitializator = new MapKitInitializator(MAPKIT_API_KEY, this);
        mapKitInitializator.setInitialized();

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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cameraPos = mapView.getMap().getCameraPosition().getTarget();
                String lat = String.valueOf(cameraPos.getLatitude());
                String lon = String.valueOf(cameraPos.getLongitude());

                editor = prefs.edit();
                editor.putString("latitude", lat).apply();
                editor.putString("longtitude", lon).apply();

                Intent intent = new Intent(YandexMap.this, CurrentWeather.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
}
