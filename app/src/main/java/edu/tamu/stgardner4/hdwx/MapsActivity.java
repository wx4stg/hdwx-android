package edu.tamu.stgardner4.hdwx;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

import edu.tamu.stgardner4.hdwx.databinding.ActivityMapsBinding;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Boolean isTracking = Boolean.FALSE;
    private Boolean isMapAttrLayoutExpanded = Boolean.FALSE;
    private final int preciseLocationCode = 1;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageButton locateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        context = getApplicationContext();
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
        locateBtn = findViewById(R.id.locateBtn);
        locateBtn.setBackgroundColor(getResources().getColor(R.color.aggie_maroon));
        locateBtn.setColorFilter(Color.argb(255, 255, 255, 255));
        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking) {
                    stopTracking();
                } else {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast myToast = Toast.makeText(context, "To follow your location, go to the settings app and give HDWX permission to view your location. This data is not stored by HDWX.", Toast.LENGTH_LONG);
                        myToast.show();
                    } else {
                        startTracking();
                    }
                }
                mapAttrLayout.animate().translationY(-1 * mapAttrLayout.getHeight());
                isMapAttrLayoutExpanded = Boolean.FALSE;
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                LinearLayout mapAttrLayout = findViewById(R.id.mapAttrLayout);
                mapAttrLayout.animate().translationY(-1 * mapAttrLayout.getHeight());
                isMapAttrLayoutExpanded = Boolean.FALSE;
                if (isTracking) {
                    stopTracking();
                }

            }
        });
        LatLng conusLatLon = new LatLng(39.8333333,-98.585522);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(conusLatLon, 3.0f));
        updateMapCamera(3.0f);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == preciseLocationCode) {
            updateMapCamera(3.0f);
        }
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            if (isTracking) {
                updateMapCamera(9.0f);
            }
        }
    };

    private void startTracking() {
        isTracking = Boolean.TRUE;
        ObjectAnimator bgColorFade = ObjectAnimator.ofObject(locateBtn, "backgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.aggie_maroon), Color.argb(255, 255, 255, 255));
        ObjectAnimator fgColorFade = ObjectAnimator.ofObject(locateBtn, "colorFilter", new ArgbEvaluator(), Color.argb(255, 255, 255, 255), getResources().getColor(R.color.aggie_maroon));
        bgColorFade.start();
        fgColorFade.start();
        updateMapCamera(9.0f);
    }

    private void stopTracking() {
        isTracking = Boolean.FALSE;
        ObjectAnimator bgColorFade = ObjectAnimator.ofObject(locateBtn, "backgroundColor", new ArgbEvaluator(), Color.argb(255, 255, 255, 255), getResources().getColor(R.color.aggie_maroon));
        ObjectAnimator fgColorFade = ObjectAnimator.ofObject(locateBtn, "colorFilter", new ArgbEvaluator(), getResources().getColor(R.color.aggie_maroon), Color.argb(255, 255, 255, 255));
        bgColorFade.start();
        fgColorFade.start();
    }

    private void updateMapCamera(float zoomLvl) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, preciseLocationCode);
        } else {
            if (fusedLocationClient == null) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.requestLocationUpdates(LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY),
                        locationCallback,
                        Looper.getMainLooper());
            }
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location userLocation) {
                    if (userLocation != null) {
                        LatLng usrLatLon = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(usrLatLon, zoomLvl));
                    }
                }
            });
        }
    }
}