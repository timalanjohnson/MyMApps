package com.timalanjohnson.mymapps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Globals
    private static final String TAG = "MapsActivityLog";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    // Widgets
    private EditText searchText;
    private RecyclerView searchRecycler;

    // Variables
    private GoogleMap mMap;
    private Boolean locationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private ArrayList<String> placeIDs = new ArrayList<>();
    private ArrayList<String> placePrimaryTexts = new ArrayList<>();
    private ArrayList<String> placeSecondaryTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        searchText = (EditText) findViewById(R.id.editTextSearch);

        Places.initialize(getApplicationContext(), getString(R.string.API_KEY)); // Initialize the Places SDK

        getLocationPermissions();

        init();
    }

    private void init(){
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){

                    // Suggests places from search and displays them
                    searchSuggestions();

                    // Execute search
                    // geoLocate();
                }
                return false;
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Autocomplete
                if(searchText.getText().toString().equals("")){
                    hideSearchRecycler();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady() called.");
        mMap = googleMap;

        if (locationPermissionGranted){
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.setTrafficEnabled(true);
        }
    }

    // Initialises the map
    private void initMap(){
        Log.d(TAG, "initMap() called.");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void searchSuggestions(){
        PlacesClient placesClient = Places.createClient(this);    // Create a new Places client instance

        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        String query = searchText.getText().toString();

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder().setCountry("za").setSessionToken(token).setQuery(query).build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {

            placeIDs.clear();
            placePrimaryTexts.clear();
            placeSecondaryTexts.clear();

            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                Log.i(TAG, prediction.getPlaceId());
                Log.i(TAG, prediction.getPrimaryText(null).toString());

                // Add IDs and Texts to ArrayLists
                placeIDs.add(prediction.getPlaceId());
                placePrimaryTexts.add(prediction.getPrimaryText(null).toString());
                placeSecondaryTexts.add(prediction.getSecondaryText(null).toString());
            }

            initSearchRecycler();

        }).addOnFailureListener((exception) -> {

            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }

        });
    }
    private void initSearchRecycler(){
        Log.d(TAG, "initRecyclerView: called. Reading ArrayLists");
        searchRecycler = findViewById(R.id.searchRecyclerView);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, placeIDs, placePrimaryTexts, placeSecondaryTexts);
        searchRecycler.setAdapter(adapter);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));
        displaySearchRecycler();

        // Listens for recycler selection and gets the Place ID of selected destination.
        adapter.SetOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                Log.d(TAG, "OnItemClick: called.");
                String id = adapter.placeIdList.get(position);
                Toast.makeText(MapsActivity.this, id, Toast.LENGTH_SHORT).show();

                // TODO
                // Find Destination by Place ID
                // Move camera to destination
                // Display navigate button
            }
        });
    }

    private void displaySearchRecycler(){
        Log.d(TAG, "displaySearchRecycler: called");
        searchRecycler.setVisibility(View.VISIBLE);
    }

    private void hideSearchRecycler(){
        Log.d(TAG, "hideSearchRecycler: called");
        searchRecycler.setVisibility(View.GONE);
    }

    // Moves camera to a location with a LatLng object
    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera() called.");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    // Get the current device location and moves camera to location
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation() called.");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(locationPermissionGranted){
                final Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Found location
                            Log.d(TAG, "Current location found.");
                            Location currentLocation = (Location) task.getResult();
                            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera(currentLatLng, DEFAULT_ZOOM);
                        } else {
                            // Unable to get current location
                            Log.d(TAG, "Current location is null.");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    // Checks if user has allowed location permissions.
    private void getLocationPermissions(){
        Log.d(TAG, "getLocationPermissions() called.");
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            initMap();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Requests location permissions from the user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() called.");
        locationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            locationPermissionGranted = false;
                            return;
                        }
                    }
                    locationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }
}
