package com.timalanjohnson.mymapps;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseManager {

    private static final String TAG = "DatabaseManager";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    public DatabaseManager() {

    }

    public void testMessage(String message){

        // Check if user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {

            String uid = firebaseAuth.getUid();

            Log.d(TAG, "testMessage: Got this far.");
            try {
                database.child(uid).push().setValue(message);
            } catch (Exception e) {
                Log.d(TAG, "testMessage: " + e.getMessage());
            }
        }

    }
}
