package com.timalanjohnson.mymapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.timalanjohnson.mymapps.adapters.HistoryRecyclerViewAdapter;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uid = firebaseAuth.getUid();
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference(uid).child("Trips");

    private RecyclerView historyRecycler;
    public ArrayList<Trip> trips;
    private HistoryRecyclerViewAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle("Trip History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        historyRecycler = findViewById(R.id.history_recycler);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trips = new ArrayList<Trip>();
                for (DataSnapshot dataSnapshotTrip : dataSnapshot.getChildren()) {
                    Trip trip = dataSnapshotTrip.getValue(Trip.class);
                    trips.add(trip);
                }
                historyAdapter = new HistoryRecyclerViewAdapter(HistoryActivity.this, trips);
                historyRecycler.setAdapter(historyAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HistoryActivity.this, "Error retrieving data from the database", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
