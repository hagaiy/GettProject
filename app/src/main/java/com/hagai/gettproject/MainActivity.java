package com.hagai.gettproject;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hagai.gettproject.comm.ApiInterface;
import com.hagai.gettproject.comm.PlacesResult;
import com.hagai.gettproject.comm.RetrofitClient;
import com.hagai.gettproject.comm.ReverseResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;

/**
 * Created by hagai on 8/13/2017.
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private final int RADIUS_IN_METERS = 1000;
    private final int SAMPALE_INTERVAL_IN_MILLIS = 1000;
    private final float PREFERED_ZOOM = 14.0f;
    //
    ApiInterface apiService;
    List<ReverseResult.Results> results;
    List<PlacesResult.Results> placesResults;
    List<MarkerModel> storeModels;
    //
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    //
    private SupportMapFragment mapFragment;
    //
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapterMarkers;
    private Button button;
    private Button button2;
    private TextView statusTextView;
    private LocationManager locationManager;
    //
    private Location mLastLocation;
    private String bestLocationProvider;
    private String latLngString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        statusTextView.setText(R.string.seeking_location);
        //
        button = (Button) findViewById(R.id.button);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make sure map is over populated with same markers
                if (storeModels == null)
                    findPlacesRequest();
                else if (storeModels.size() == 0)
                    findPlacesRequest();

            }
        });
        //
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //
        apiService = RetrofitClient.getClient().create(ApiInterface.class);
        //
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //
        requestPermissions();
    }


    @Override
    protected void onPause() {
        super.onPause();

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.clear();
            }
        });
        if (adapterMarkers != null)
            adapterMarkers.clearData();
        disableUI();

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerLocationManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        locationManager.removeUpdates(this);
        enableUI();
        mLastLocation = location;
        latLngString = new StringBuilder(location.getLatitude() + "," + location.getLongitude()).toString();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                reverseGeocodingRequest();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), PREFERED_ZOOM));
            }
        });
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                permissionsRejected.clear();
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }
                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKExit(getApplicationContext().getResources().getString(R.string.premissions_request),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    },
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MainActivity.this.finish();
                                        }
                                    }
                            );
                            return;
                        }
                    } else {
                        registerLocationManager();
                    }

                } else {
                    registerLocationManager();
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

    /*************************************************/
    //               private methods

    /*************************************************/

    private void enableUI() {
        statusTextView.setVisibility(View.GONE);
        button.setEnabled(true);
    }

    private void disableUI() {
        statusTextView.setVisibility(View.VISIBLE);
        button.setEnabled(false);
    }

    private void registerLocationManager() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        bestLocationProvider = String.valueOf(locationManager.getBestProvider(criteria, true));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(bestLocationProvider, SAMPALE_INTERVAL_IN_MILLIS, 0, this);
    }

    private void requestPermissions() {
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissions.add(INTERNET);
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                registerLocationManager();
            }
        } else {
            registerLocationManager();
        }
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private void showMessageOKExit(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener exitListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Exit", exitListener)
                .create()
                .show();
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private void reverseGeocodingRequest() {
        Call<ReverseResult.Root> call = apiService.reverseGeocoding(latLngString, RetrofitClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<ReverseResult.Root>() {
            @Override
            public void onResponse(Call<ReverseResult.Root> call, Response<ReverseResult.Root> response) {
                ReverseResult.Root root = response.body();
                if (response.isSuccessful()) {
                    if (root.status.equals("OK")) {
                        results = root.customA;
                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                LatLng me = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                googleMap.addMarker(new MarkerOptions().position(me).title(results.get(0).address));
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.no_matches_found), Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReverseResult.Root> call, Throwable t) {
                // Log error here since request failed
                call.cancel();
            }
        });
    }

    private void findPlacesRequest() {
        Call<PlacesResult.Root> call = apiService.findPlaces(latLngString, RADIUS_IN_METERS, RetrofitClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlacesResult.Root>() {
            @Override
            public void onResponse(Call<PlacesResult.Root> call, Response<PlacesResult.Root> response) {
                PlacesResult.Root root = response.body();
                if (response.isSuccessful()) {
                    if (root.status.equals("OK")) {
                        placesResults = root.customA;
                        storeModels = new ArrayList<>();
                        for (int i = 0; i < placesResults.size(); i++) {
                            PlacesResult.Results info = placesResults.get(i);
                            double lat = Double.valueOf(info.geometry.locationA.lat);
                            double lng = Double.valueOf(info.geometry.locationA.lng);
                            storeModels.add(new MarkerModel(info.name, info.vicinity, new LatLng(lat, lng)));
                        }
                        adapterMarkers = new RecyclerViewAdapter(placesResults, storeModels);
                        recyclerView.setAdapter(adapterMarkers);
                        addMarkers();
                    } else {
                        Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.no_matches_found), Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlacesResult.Root> call, Throwable t) {
                // Log error here since request failed
                call.cancel();
            }
        });
    }

    private void addMarkers() {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (storeModels != null)
                    for (MarkerModel place : storeModels) {
                        googleMap.addMarker(new MarkerOptions().position(place.latLng).title(place.name)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    }
            }
        });
    }
}



