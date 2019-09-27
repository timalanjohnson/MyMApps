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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.timalanjohnson.mymapps.helpers.FetchURL;
import com.timalanjohnson.mymapps.helpers.TaskLoadedCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    // Globals
    private static final String TAG = "MapsActivityLog";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 12f;

    // Widgets
    private EditText searchText;
    private RecyclerView searchRecycler;
    private Button showRouteButton;

    // Variables
    private GoogleMap mMap;

    private Boolean locationPermissionGranted = false;

    private FusedLocationProviderClient mFusedLocationClient;

    private PlacesClient placesClient;

    private ArrayList<String> placeIDs = new ArrayList<>();
    private ArrayList<String> placePrimaryTexts = new ArrayList<>();
    private ArrayList<String> placeSecondaryTexts = new ArrayList<>();

    private String currentID;
    private double currentLat;
    private double currentLng;
    private LatLng currentLatLng;
    private String currentName;

    private String destinationID;
    private String destinationName;
    private double destinationLat;
    private double destinationLng;
    private LatLng destinationLatLng;

    private String travelMode = "driving";

    Polyline routePolyLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Places.initialize(getApplicationContext(), getString(R.string.API_KEY)); // Initialize the Places SDK

        searchText = findViewById(R.id.editTextSearch);
        showRouteButton = findViewById(R.id.showRouteButton);
        searchRecycler = findViewById(R.id.searchRecyclerView);
        placesClient = Places.createClient(this);    // Create a new Places client instance

        getLocationPermissions();

        setListeners();
    }

    private void setListeners(){
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

        showRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayRoute();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady() called.");
        mMap = googleMap;

        if (locationPermissionGranted){
            setDeviceLocation();
            mMap.setMyLocationEnabled(true);
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
        String query = searchText.getText().toString();

        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

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
            displaySearchRecycler();

        }).addOnFailureListener((exception) -> {

            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }

        });
    }

    private void initSearchRecycler(){
        Log.d(TAG, "initRecyclerView: called. Reading ArrayLists");
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, placeIDs, placePrimaryTexts, placeSecondaryTexts);
        searchRecycler.setAdapter(adapter);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));
        displaySearchRecycler();

        // Listens for recycler selection and gets the Place ID of selected destination.
        adapter.SetOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                Log.d(TAG, "OnItemClick: called.");
                destinationID = adapter.placeIdList.get(position);
                Log.d(TAG, "OnItemClick: destination id = " + destinationID);

                // Hide recycler
                hideSearchRecycler();

                // Find Destination by Place ID
                setDestinationById(destinationID);

                // Display navigate button
                showRouteButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displaySearchRecycler(){
        Log.d(TAG, "displaySearchRecycler: called");
        searchRecycler.setVisibility(View.VISIBLE);
    }

    private void hideSearchRecycler(){
        Log.d(TAG, "hideSearchRecycler: called");
        searchRecycler = findViewById(R.id.searchRecyclerView);
        searchRecycler.setVisibility(View.GONE);
    }

    private void setDestinationById(String placeID){
        Log.d(TAG, "getDestinationById: called.");

        // Specify fields to return
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME);

        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
           Place place = response.getPlace();
            Log.d(TAG, "getDestinationById: found place " + place.getName());

            destinationID = place.getId();
            destinationName = place.getName();
            destinationLatLng = place.getLatLng();

            Log.d(TAG, "getDestinationById: destinationLatLng:" + destinationLatLng.toString());

            // Clear markers
            mMap.clear();

            // Move to camera to destination
            moveCamera(destinationLatLng, DEFAULT_ZOOM, destinationName);

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });
    }

    private void calculateDirections(){
        Log.d(TAG, "calculateDirections: defining URL for Directions API call");

        /*
        https://maps.googleapis.com/maps/api/directions/json?
        origin=-33.9248685,18.4240553
        &destination=-34.0208739,18.3682641
        &mode=driving
        &key=AIzaSyB6sWXGSLQfiBuPQrcBaAe9dBNdKYvmgqs

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=-33.9248685,18.4240553&destination=-34.0208739,18.3682641&mode=driving&key=AIzaSyB6sWXGSLQfiBuPQrcBaAe9dBNdKYvmgqs";
         */

        String url = getUrl(currentLatLng, destinationLatLng, travelMode);

        Log.d(TAG, "calculateDirections: calling Directions API");
        new FetchURL(MapsActivity.this).execute(url, "driving");

    }

    @Override
    public void onTaskDone(Object... values) {

        Log.d(TAG, "onTaskDone: response from Directions API");
        if (routePolyLine != null){
            routePolyLine.remove();
        }
        Log.d(TAG, "onTaskDone: adding polyine to map");
        routePolyLine = mMap.addPolyline((PolylineOptions) values[0]);
        moveCamera(currentLatLng, 10f);
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.API_KEY);
        return url;
    }

    private void displayRoute(){
        Log.d(TAG, "displayRoute: called.");

        calculateDirections();

        // routePolyLine = mMap.addPolyline(new PolylineOptions().clickable(false).add(currentLatLng, destinationLatLng));
    }

    // Moves camera to a location with a LatLng object
    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera() called.");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    // Moves camera to a location with a LatLng object
    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera() called.");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
    }

    // Get the current device location and moves camera to location
    private void setDeviceLocation(){
        Log.d(TAG, "setDeviceLocation() called.");

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
                            currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            currentLat = currentLocation.getLatitude();
                            currentLng = currentLocation.getLongitude();
                            moveCamera(currentLatLng, DEFAULT_ZOOM);
                        } else {
                            // Unable to get current location
                            Log.d(TAG, "Current location is null.");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "setDeviceLocation: SecurityException: " + e.getMessage());
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
