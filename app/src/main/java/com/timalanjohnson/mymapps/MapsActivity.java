package com.timalanjohnson.mymapps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.timalanjohnson.mymapps.adapters.SearchRecyclerViewAdapter;
import com.timalanjohnson.mymapps.helpers.FetchURL;
import com.timalanjohnson.mymapps.helpers.TaskLoadedCallback;
import com.timalanjohnson.mymapps.helpers.TripData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private Button commenceTripButton;
    private Button settingsButton;
    private Button logoutButton;
    private Button historyButton;
    private View tripInfoLayout;
    private TextView durationTextView;
    private TextView distanceTextView;

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

    private String travelMode;

    Polyline routePolyLine;
    private FirebaseAuth firebaseAuth;

    private DatabaseManager dbm = new DatabaseManager();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Places.initialize(getApplicationContext(), getString(R.string.API_KEY)); // Initialize the Places SDK

        searchText = findViewById(R.id.editTextSearch);
        showRouteButton = findViewById(R.id.showRouteButton);
        durationTextView = findViewById(R.id.durationTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        commenceTripButton = findViewById(R.id.commenceTripButton);
        logoutButton = findViewById(R.id.buttonLogout);
        historyButton = findViewById(R.id.buttonHistory);
        settingsButton = findViewById(R.id.buttonSettings);
        searchRecycler = findViewById(R.id.searchRecyclerView);
        tripInfoLayout = findViewById(R.id.tripInfoLayout);
        placesClient = Places.createClient(this);    // Create a new Places client instance

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.getCurrentUser();

        dbm.initUserPreferences();

        Log.d(TAG, "onCreate: " + UserPreferences.travelMode);

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

                hideTripInfo();

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

        commenceTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commenceTrip();
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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
        SearchRecyclerViewAdapter adapter = new SearchRecyclerViewAdapter(this, placeIDs, placePrimaryTexts, placeSecondaryTexts);
        searchRecycler.setAdapter(adapter);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));
        displaySearchRecycler();

        // Listens for recycler selection and gets the Place ID of selected destination.
        adapter.SetOnItemClickListener(new SearchRecyclerViewAdapter.OnItemClickListener() {
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
        travelMode = UserPreferences.travelMode;
        String url = getUrl(currentLatLng, destinationLatLng, travelMode); // Build the API call
        Log.d(TAG, "calculateDirections: calling Directions API");
        new FetchURL(MapsActivity.this).execute(url, travelMode);
    }

    @Override
    public void onTaskDone(Object... values) {
        Log.d(TAG, "onTaskDone: response from Directions API");
        // Removing any existing lines
        if (routePolyLine != null){
            routePolyLine.remove();
        }

        // Adding route visualisation
        Log.d(TAG, "onTaskDone: adding polyline to map");
        routePolyLine = mMap.addPolyline((PolylineOptions) values[0]);

        // UI
        progressDialog.dismiss();
        tripInfoLayout.setVisibility(View.VISIBLE);
        distanceTextView.setText("Distance: " + TripData.distance);
        durationTextView.setText("Estimated Duration: " + TripData.duration);
        moveCamera(currentLatLng, 11f);
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
        progressDialog.show();
        calculateDirections();
    }
    
    private void commenceTrip(){
        Log.d(TAG, "commenceTrip: ");

        SimpleDateFormat time = new SimpleDateFormat("'On ' yyyy.MM.dd 'at' HH:mm");

        // Finalising trip details for logging to the database
        TripData.origin = currentLatLng.toString();
        TripData.destination = destinationName;
        TripData.time = time.format(new Date());
        TripData.travelMode = travelMode;

        // Logging trip to the database
        Trip log = new Trip(TripData.origin, TripData.destination, TripData.travelMode, TripData.distance, TripData.duration, TripData.time);
        dbm.logTrip(log);

        // UI
        showRouteButton.setVisibility(View.GONE);
        tripInfoLayout.setVisibility(View.GONE);
        moveCamera(currentLatLng, 18f);
    }

    private void hideTripInfo() {
        tripInfoLayout.setVisibility(View.GONE);
    }

    private void displaySearchRecycler(){
        searchRecycler.setVisibility(View.VISIBLE);
    }

    private void hideSearchRecycler(){
        searchRecycler.setVisibility(View.GONE);
    }

    // Moves camera to a location with a LatLng object
    private void moveCameraNoAnimation(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera() called.");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    // Moves camera to a location with a LatLng object
    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera() called.");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    // Moves camera to a location with a LatLng object
    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera() called.");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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
                            moveCameraNoAnimation(currentLatLng, DEFAULT_ZOOM);
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
