package edu.tamu.stgardner4.hdwx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Fetch fetch;
    private Boolean isTracking = Boolean.FALSE;
    private final int preciseLocationCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LinearLayout mapAttrLayout = findViewById(R.id.mapAttrLayout);
        mapAttrLayout.setVisibility(View.INVISIBLE);
        Button mapAttrBtn = findViewById(R.id.mapAttrBtn);
        mapAttrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapAttrLayout.setVisibility(View.VISIBLE);
            }
        });
        Button streetBtn = findViewById(R.id.streetBtn);
        streetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mapAttrLayout.setVisibility(View.INVISIBLE);
            }
        });
        Button satBtn = findViewById(R.id.satBtn);
        satBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mapAttrLayout.setVisibility(View.INVISIBLE);
            }
        });
        Button terrBtn = findViewById(R.id.terrBtn);
        terrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                mapAttrLayout.setVisibility(View.INVISIBLE);
            }
        });
        MaterialButton locateBtn = findViewById(R.id.locateBtn);
        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking) {
                    isTracking = Boolean.FALSE;
                    locateBtn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.aggie_maroon));
                    locateBtn.setIconTintResource(R.color.white);
                } else {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast myToast = Toast.makeText(getApplicationContext(), "To follow your location, go to the settings app and give HDWX permission to view your location. This data is not stored by HDWX.", Toast.LENGTH_LONG);
                        myToast.show();
                    } else {
                        isTracking = Boolean.TRUE;
                        locateBtn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.white));
                        locateBtn.setIconTintResource(R.color.aggie_maroon);
                        LocationManager lm = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
                        Location lmlocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        LatLng userLocation = new LatLng(lmlocation.getLatitude(), lmlocation.getLongitude());
                        mMap.setMyLocationEnabled(true);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    if (isTracking) {
                                        Location lmlocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        LatLng userLocation = new LatLng(lmlocation.getLatitude(), lmlocation.getLongitude());
                                        mMap.setMyLocationEnabled(true);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                                    } else {
                                        lm.removeUpdates(this::onLocationChanged);
                                    }
                                }
                            }
                        });
                    }
                }
                mapAttrLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, preciseLocationCode);
        } else {
            LocationManager lm = (LocationManager)getSystemService(getApplicationContext().LOCATION_SERVICE);
            Location lmlocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng userLocation = new LatLng(lmlocation.getLatitude(), lmlocation.getLongitude());
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
        }

        FetchConfiguration config = new FetchConfiguration.Builder(this).setDownloadConcurrentLimit(10).build();
        fetch = Fetch.Impl.getInstance(config);
        File frame9File = new File(getFilesDir() + File.separator + "test.png");
        if (frame9File.exists()) {
            frame9File.delete();
        }
        final Request request = new Request("http://weather-dev.geos.tamu.edu/wx4stg/gisproducts/radar/regional/frame9.png", getFilesDir() + File.separator + "test.png");
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        FetchListener listener = new FetchListener() {
            @Override
            public void onCompleted(@NotNull Download download) {
                LatLng southWestLL = new LatLng(23.5, -110);
                LatLng northEastLL = new LatLng(37, -85);
                LatLngBounds bounds = new LatLngBounds(southWestLL, northEastLL);
                Bitmap imageBitmap = BitmapFactory.decodeFile(download.getFile());
                BitmapDescriptor img = BitmapDescriptorFactory.fromBitmap(imageBitmap);
                GroundOverlayOptions gndOverlay = new GroundOverlayOptions().image(img).positionFromBounds(bounds).transparency(0.25f);
                mMap.addGroundOverlay(gndOverlay);
            }
            @Override
            public void onAdded(@NotNull Download download) {}
            @Override
            public void onQueued(@NotNull Download download, boolean b) {}
            @Override
            public void onWaitingNetwork(@NotNull Download download) {}
            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                Log.w("HDWX-WARNING", "Download failed");
            }
            @Override
            public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {}
            @Override
            public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {}
            @Override
            public void onProgress(@NotNull Download download, long l, long l1) {}
            @Override
            public void onPaused(@NotNull Download download) {}
            @Override
            public void onResumed(@NotNull Download download) {}
            @Override
            public void onCancelled(@NotNull Download download) {}
            @Override
            public void onRemoved(@NotNull Download download) {}
            @Override
            public void onDeleted(@NotNull Download download) {}
        };
        fetch.addListener(listener);
        fetch.enqueue(request, updatedRequest -> {
        }, error -> {
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == preciseLocationCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationManager lm = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
                Location lmlocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng userLocation = new LatLng(lmlocation.getLatitude(), lmlocation.getLongitude());
                mMap.setMyLocationEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            }
        }
    }
}