package com.timalanjohnson.mymapps.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timalanjohnson.mymapps.R;
import com.timalanjohnson.mymapps.Trip;

import java.util.ArrayList;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {

    private Context context;
    ArrayList<Trip> trips;

    public HistoryRecyclerViewAdapter(Context context, ArrayList<Trip> trips) {
        this.context = context;
        this.trips = trips;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_trip_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.tripTime.setText(trips.get(position).getTime());
        holder.tripDestination.setText(trips.get(position).getDestination());
        holder.tripDistance.setText(trips.get(position).getDistance());

        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, holder.tripDestination.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder
    {
        RelativeLayout listItem;
        TextView tripTime, tripDestination, tripDistance;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            listItem = itemView.findViewById(R.id.trip_list_item);
            tripTime = itemView.findViewById(R.id.trip_time);
            tripDestination = itemView.findViewById(R.id.trip_destination);
            tripDistance = itemView.findViewById(R.id.trip_distance);
        }
        public void onClick(final int position)
        {
            tripDestination.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, position+" is clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
