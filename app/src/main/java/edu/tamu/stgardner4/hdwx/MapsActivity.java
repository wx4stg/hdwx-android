package edu.tamu.stgardner4.hdwx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        FetchConfiguration config = new FetchConfiguration.Builder(this).setDownloadConcurrentLimit(10).build();
        fetch = Fetch.Impl.getInstance(config);
        File frame9File = new File(getFilesDir() + File.separator + "test.png");
        if (frame9File.exists()) {
            Log.d("HDWX-DEBUG", "Removing file...");
            frame9File.delete();
        }
        final Request request =  new Request("http://weather-dev.geos.tamu.edu/wx4stg/test.png", getFilesDir() + File.separator + "test.png");
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        FetchListener listener = new FetchListener() {
            @Override
            public void onAdded(@NotNull Download download) {
                Log.d("HDWX-DEBUG", "Successfully added.");
            }

            @Override
            public void onQueued(@NotNull Download download, boolean b) {
                Log.d("HDWX-DEBUG", "Successfully queued.");
            }

            @Override
            public void onWaitingNetwork(@NotNull Download download) {
                Log.d("HDWX-DEBUG", "Waiting network.");
            }

            @Override
            public void onCompleted(@NotNull Download download) {
                Log.d("HDWX-DEBUG", "Download complete!");
                LatLng southWestLL = new LatLng(23.5,-110);
                LatLng northEastLL = new LatLng(37,-85);
                LatLngBounds bounds = new LatLngBounds(southWestLL, northEastLL);
                Bitmap imageBitmap = BitmapFactory.decodeFile(download.getFile());
                BitmapDescriptor img = BitmapDescriptorFactory.fromBitmap(imageBitmap);
                GroundOverlayOptions gndOverlay = new GroundOverlayOptions().image(img).positionFromBounds(bounds);
                mMap.addGroundOverlay(gndOverlay);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(northEastLL));
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                Log.d("HDWX-DEBUG", throwable.toString());
            }

            @Override
            public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {
                Log.d("HDWX-DEBUG", "Block updated.");
            }

            @Override
            public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {
                Log.d("HDWX-DEBUG", "Successfully started.");
            }

            @Override
            public void onProgress(@NotNull Download download, long l, long l1) {
                Log.d("HDWX-DEBUG", "Progress" + l + l1);
            }

            @Override
            public void onPaused(@NotNull Download download) {
                Log.d("HDWX-DEBUG", "Paused.");
            }

            @Override
            public void onResumed(@NotNull Download download) {
                Log.d("HDWX-DEBUG", "Resumed.");
            }

            @Override
            public void onCancelled(@NotNull Download download) {
                Log.d("HDWX-DEBUG", "Cancelled.");
            }

            @Override
            public void onRemoved(@NotNull Download download) {
                Log.d("HDWX-DEBUG", "Removed.");
            }

            @Override
            public void onDeleted(@NotNull Download download) {
                Log.d("HDWX-DEBUG", "Deleted.");
            }
        };
        fetch.addListener(listener);
        fetch.enqueue(request, updatedRequest -> {
            Log.d("HDWX-DEBUG", "Successfully enqueued.");
        }, error -> {
            Log.d("HDWX-DEBUG", error.getThrowable().toString());
        });
    }
}