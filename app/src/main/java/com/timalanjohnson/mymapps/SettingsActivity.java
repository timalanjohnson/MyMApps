package com.timalanjohnson.mymapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private RadioButton radioDriving;
    private RadioButton radioWalking;
    private RadioButton radioTransit;
    private RadioButton radioMetric;
    private RadioButton radioImperial;
    private Button buttonSettingsUpdate;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseManager dbm = new DatabaseManager();
    private UserPreferences preferences = new UserPreferences();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        radioDriving = findViewById(R.id.radioButtonDriving);
        radioWalking = findViewById(R.id.radioButtonWalking);
        radioTransit = findViewById(R.id.radioButtonTransit);
        radioMetric = findViewById(R.id.radioButtonMetric);
        radioImperial = findViewById(R.id.radioButtonImperial);
        buttonSettingsUpdate = findViewById(R.id.buttonSettingsUpdate);

        String mode = preferences.getTravelMode();
        String units = preferences.getUnits();

        try {
            if (mode.equals("driving")){
                radioDriving.setChecked(true);
            }

            if (mode.equals("walking")){
                radioWalking.setChecked(true);
            }

            if (mode.equals("transit")){
                radioTransit.setChecked(true);
            }

            if (units.equals("metric")) {
                radioMetric.setChecked(true);
            } else {
                radioImperial.setChecked(true);
            }
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e.getMessage());
        }

    }

    public void checkRadioButtons(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButtonDriving:
                if (checked)
                    preferences.setTravelMode("driving");
                    break;
            case R.id.radioButtonWalking:
                if (checked)
                    preferences.setTravelMode("walking");
                    break;
            case R.id.radioButtonTransit:
                if (checked)
                    preferences.setTravelMode("transit");
                break;
            case R.id.radioButtonMetric:
                if (checked)
                    preferences.setUnits("metric");
                break;
            case R.id.radioButtonImperial:
                if (checked)
                    preferences.setUnits("imperial");
                    UserPreferences.units = "imperial";
                break;
        }

    }

    public void updateSettings(View view) {
        dbm.setUserPreferences(preferences);
        Toast.makeText(this, "Settings updated.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "updateSettings: " + UserPreferences.travelMode + UserPreferences.units);
    }
}
