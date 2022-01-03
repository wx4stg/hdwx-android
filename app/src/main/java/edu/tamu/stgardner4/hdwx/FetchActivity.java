package edu.tamu.stgardner4.hdwx;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FetchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch);
        FetchConfiguration config = new FetchConfiguration.Builder(this).setDownloadConcurrentLimit(10).build();
        Fetch fetch = Fetch.Impl.getInstance(config);
        File newAllProdPath = new File(getFilesDir() + File.separator + "all-products-new.json");
        if (newAllProdPath.exists()) {
            newAllProdPath.delete();
        }
        final Request request = new Request("http://weather-dev.geos.tamu.edu/wx4stg/api/all-products.php", getFilesDir() + File.separator + "all-products-new.json");
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        FetchListener listener = new FetchListener() {
            @Override
            public void onCompleted(@NotNull Download download) {
                File oldAllProdPath = new File(getFilesDir() + File.separator + "all-products-old.json");
                if (oldAllProdPath.exists()) {
                    oldAllProdPath.delete();
                }
                List<String> oldProductDescs = new ArrayList<>();
                File allProdPath = new File(getFilesDir() + File.separator + "all-products.json");
                if (allProdPath.exists()) {
                    allProdPath.renameTo(oldAllProdPath);
                    try {
                        String oldProductsJsonStr = new String(Files.readAllBytes(oldAllProdPath.toPath()));
                        JSONArray oldProductTypesArray = new JSONArray(oldProductsJsonStr);
                        for (int a = 0; a < oldProductTypesArray.length(); a++) {
                            JSONObject productType = oldProductTypesArray.getJSONObject(a);
                            JSONArray productsInProductType = productType.getJSONArray("products");
                            for (int b = 0; b < productsInProductType.length(); b++) {
                                JSONObject product = productsInProductType.getJSONObject(b);
                                oldProductDescs.add(product.getString("productDescription"));
                            }
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                newAllProdPath.renameTo(allProdPath);
                String productsJsonStr = null;
                List<String> newProductDescs = new ArrayList<>();
                try {
                    productsJsonStr = new String(Files.readAllBytes(allProdPath.toPath()));
                    JSONArray productTypesArray = new JSONArray(productsJsonStr);
                    for (int a = 0; a < productTypesArray.length(); a++) {
                        JSONObject productType = productTypesArray.getJSONObject(a);
                        JSONArray productsInProductType = productType.getJSONArray("products");
                        for (int b = 0; b < productsInProductType.length(); b++) {
                            JSONObject product = productsInProductType.getJSONObject(b);
                            newProductDescs.add(product.getString("productDescription"));
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                Log.d("HDWX-oldproducts", oldProductDescs.toString());
                Log.d("HDWX-newproducts", newProductDescs.toString());
                newProductDescs.removeAll(oldProductDescs);
                if (newProductDescs.size() > 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(FetchActivity.this, R.style.AlertDialogCustom).create();
                    alertDialog.setTitle("New Products Detected!");
                    alertDialog.setMessage(String.join(Objects.requireNonNull(System.getProperty("line.separator")), newProductDescs));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "DISMISS",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent mapsActInt = new Intent(FetchActivity.this, MapsActivity.class);
                                    startActivity(mapsActInt);
                                    finish();
                                }
                            });
                    alertDialog.show();
                } else {
                    Intent mapsActInt = new Intent(FetchActivity.this, MapsActivity.class);
                    startActivity(mapsActInt);
                    finish();
                }
            }
            @Override
            public void onAdded(@NotNull Download download) {
            }

            @Override
            public void onQueued(@NotNull Download download, boolean b) {
            }

            @Override
            public void onWaitingNetwork(@NotNull Download download) {
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                AlertDialog alertDialog = new AlertDialog.Builder(FetchActivity.this, R.style.AlertDialogCustom).create();
                alertDialog.setTitle("Fetching Product List Failed");
                alertDialog.setMessage("This probably means you don't have internet or HDWX is down. Products may not work as expected (or at all).");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "DISMISS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent mapsActInt = new Intent(FetchActivity.this, MapsActivity.class);
                                startActivity(mapsActInt);
                                finish();
                            }
                        });
                alertDialog.show();
            }

            @Override
            public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {
            }

            @Override
            public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {
            }

            @Override
            public void onProgress(@NotNull Download download, long l, long l1) {
            }

            @Override
            public void onPaused(@NotNull Download download) {
            }

            @Override
            public void onResumed(@NotNull Download download) {
            }

            @Override
            public void onCancelled(@NotNull Download download) {
            }

            @Override
            public void onRemoved(@NotNull Download download) {
            }

            @Override
            public void onDeleted(@NotNull Download download) {
            }
        };
        fetch.addListener(listener);
        fetch.enqueue(request, updatedRequest -> {
        }, error -> {
        });
    }
}