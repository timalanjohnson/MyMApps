package com.timalanjohnson.mymapps;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseManager {

    private static final String TAG = "DatabaseManager";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uid = firebaseAuth.getUid();
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    public DatabaseManager() {

    }


    public void logTrip(String message){
        // Check if user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            try {
                database.child(uid).child("Trips").push().setValue(message);
            } catch (Exception e) {
                Log.d(TAG, "logTrip: " + e.getMessage());
            }
        }
    }

    public String getTripHistory(String uid){
        String tripHistory = "";

        return tripHistory;
    }
}
