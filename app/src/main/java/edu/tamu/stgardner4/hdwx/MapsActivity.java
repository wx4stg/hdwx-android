package edu.tamu.stgardner4.hdwx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;

import edu.tamu.stgardner4.hdwx.databinding.ActivityMapsBinding;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Boolean isMapAttrLayoutExpanded = Boolean.FALSE;
    private final int preciseLocationCode = 1;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        LinearLayout mapAttrLayout = findViewById(R.id.mapAttrLayout);
        mapAttrLayout.setVisibility(View.INVISIBLE);
        ImageButton mapAttrBtn = findViewById(R.id.mapAttrBtn);
        mapAttrBtn.setOnClickListener(v -> {
            if (isMapAttrLayoutExpanded) {
                mapAttrLayout.animate().translationY(-1 * mapAttrLayout.getHeight());
                isMapAttrLayoutExpanded = Boolean.FALSE;
            } else {
                mapAttrLayout.setVisibility(View.VISIBLE);
                mapAttrLayout.animate().translationY(0);
                isMapAttrLayoutExpanded = Boolean.TRUE;
            }
        });
        MaterialButton streetBtn = findViewById(R.id.streetBtn);
        streetBtn.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mapAttrLayout.animate().translationY(-1 * mapAttrLayout.getHeight());
            isMapAttrLayoutExpanded = Boolean.FALSE;
        });
        MaterialButton satBtn = findViewById(R.id.satBtn);
        satBtn.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            mapAttrLayout.animate().translationY(-1 * mapAttrLayout.getHeight());
            isMapAttrLayoutExpanded = Boolean.FALSE;
        });
        MaterialButton terrBtn = findViewById(R.id.terrBtn);
        terrBtn.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mapAttrLayout.animate().translationY(-1 * mapAttrLayout.getHeight());
            isMapAttrLayoutExpanded = Boolean.FALSE;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                LinearLayout mapAttrLayout = findViewById(R.id.mapAttrLayout);
                mapAttrLayout.animate().translationY(-1 * mapAttrLayout.getHeight());
                isMapAttrLayoutExpanded = Boolean.FALSE;
            }
        });
        LatLng conusLatLon = new LatLng(39.8333333, -98.585522);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(conusLatLon, 3.0f));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, preciseLocationCode);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }
}