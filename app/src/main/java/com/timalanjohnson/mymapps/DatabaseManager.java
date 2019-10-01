package com.timalanjohnson.mymapps;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseManager {

    private static final String TAG = "DatabaseManager";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uid = firebaseAuth.getUid();
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference(uid);

    public DatabaseManager() {

    }

    public void logTrip(Trip log){
        // Check if user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            try {
                database.child("Trips").push().setValue(log);
                Log.d(TAG, "logTrip: logged trip to database.");
            } catch (Exception e) {
                Log.d(TAG, "logTrip: " + e.getMessage());
            }
        }
    }

    public void setUserPreferences(UserPreferences preferences){
        // Check if user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            try {
                database.child("Preferences").setValue(preferences);
            } catch (Exception e) {
                Log.d(TAG, "logTrip: " + e.getMessage());
            }
        }
    }
}
