package com.timalanjohnson.mymapps;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    public void initUserPreferences(){

        database.child("Preferences").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserPreferences.travelMode = dataSnapshot.child("travelMode").getValue(String.class);
                UserPreferences.units = dataSnapshot.child("units").getValue(String.class);
                Log.d(TAG, "settings: " + UserPreferences.travelMode + UserPreferences.units);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "settings: " + databaseError.getMessage());
            }
        });
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
