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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

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

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Boolean isTracking = Boolean.FALSE;
    private Boolean isMapAttrLayoutExpanded = Boolean.FALSE;
    private final int preciseLocationCode = 1;
    private Context context;

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
        //mapAttrLayout.animate().translationY(-1*mapAttrLayout.getHeight()).setDuration(1/10000);
        ImageButton mapAttrBtn = findViewById(R.id.mapAttrBtn);
        mapAttrBtn.setOnClickListener(v -> {
            if (isMapAttrLayoutExpanded) {
                mapAttrLayout.animate().translationY(-1*mapAttrLayout.getHeight());
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
            mapAttrLayout.animate().translationY(-1*mapAttrLayout.getHeight());
            isMapAttrLayoutExpanded = Boolean.FALSE;
        });
        MaterialButton satBtn = findViewById(R.id.satBtn);
        satBtn.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            mapAttrLayout.animate().translationY(-1*mapAttrLayout.getHeight());
            isMapAttrLayoutExpanded = Boolean.FALSE;
        });
        MaterialButton terrBtn = findViewById(R.id.terrBtn);
        terrBtn.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mapAttrLayout.animate().translationY(-1*mapAttrLayout.getHeight());
            isMapAttrLayoutExpanded = Boolean.FALSE;
        });
        ImageButton locateBtn = findViewById(R.id.locateBtn);
        locateBtn.setBackgroundColor(getResources().getColor(R.color.aggie_maroon));
        locateBtn.setColorFilter(Color.argb(255, 255, 255, 255));
        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking) {
                    isTracking = Boolean.FALSE;
                    ObjectAnimator bgColorFade = ObjectAnimator.ofObject(locateBtn, "backgroundColor", new ArgbEvaluator(), Color.argb(255,255,255,255), getResources().getColor(R.color.aggie_maroon));
                    ObjectAnimator fgColorFade = ObjectAnimator.ofObject(locateBtn, "colorFilter", new ArgbEvaluator(), getResources().getColor(R.color.aggie_maroon), Color.argb(255,255,255,255));
                    bgColorFade.start();
                    fgColorFade.start();
                } else {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast myToast = Toast.makeText(context, "To follow your location, go to the settings app and give HDWX permission to view your location. This data is not stored by HDWX.", Toast.LENGTH_LONG);
                        myToast.show();
                    } else {
                        isTracking = Boolean.TRUE;
                        ObjectAnimator bgColorFade = ObjectAnimator.ofObject(locateBtn, "backgroundColor", new ArgbEvaluator(), getResources().getColor(R.color.aggie_maroon), Color.argb(255,255,255,255));
                        ObjectAnimator fgColorFade = ObjectAnimator.ofObject(locateBtn, "colorFilter", new ArgbEvaluator(), Color.argb(255,255,255,255), getResources().getColor(R.color.aggie_maroon));
                        bgColorFade.start();
                        fgColorFade.start();
                        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                        Location lmlocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        LatLng userLocation = new LatLng(lmlocation.getLatitude(), lmlocation.getLongitude());
                        mMap.setMyLocationEnabled(true);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation));
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    if (isTracking) {
                                        Location lmlocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        LatLng userLocation = new LatLng(lmlocation.getLatitude(), lmlocation.getLongitude());
                                        mMap.setMyLocationEnabled(true);
                                        mMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation));
                                    } else {
                                        lm.removeUpdates(this);
                                    }
                                }
                            }
                        });
                    }
                }
                mapAttrLayout.animate().translationY(-1*mapAttrLayout.getHeight());
                isMapAttrLayoutExpanded = Boolean.FALSE;
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, preciseLocationCode);
        } else {
            LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
            Location lmlocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng userLocation = new LatLng(lmlocation.getLatitude(), lmlocation.getLongitude());
            mMap.setMyLocationEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 7.0f));
        }
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                LinearLayout mapAttrLayout = findViewById(R.id.mapAttrLayout);
                mapAttrLayout.animate().translationY(-1*mapAttrLayout.getHeight());
                isMapAttrLayoutExpanded = Boolean.FALSE;
                if (isTracking) {
                    isTracking = Boolean.FALSE;
                    ImageButton locateBtn = findViewById(R.id.locateBtn);
                    ObjectAnimator bgColorFade = ObjectAnimator.ofObject(locateBtn, "backgroundColor", new ArgbEvaluator(), Color.argb(255,255,255,255), getResources().getColor(R.color.aggie_maroon));
                    ObjectAnimator fgColorFade = ObjectAnimator.ofObject(locateBtn, "colorFilter", new ArgbEvaluator(), getResources().getColor(R.color.aggie_maroon), Color.argb(255,255,255,255));
                    bgColorFade.start();
                    fgColorFade.start();
                }

            }
        });
        FetchConfiguration config = new FetchConfiguration.Builder(this).setDownloadConcurrentLimit(10).build();
        Fetch fetch = Fetch.Impl.getInstance(config);
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
                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                Location lmlocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng userLocation = new LatLng(lmlocation.getLatitude(), lmlocation.getLongitude());
                mMap.setMyLocationEnabled(true);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 7.0f));
            }
        }
    }
}