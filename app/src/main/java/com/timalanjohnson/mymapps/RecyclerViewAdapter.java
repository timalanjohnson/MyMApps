package com.timalanjohnson.mymapps;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapterLog";

    private Context context;
    private ArrayList<String> placeIdList = new ArrayList<>();
    private ArrayList<String> primaryTextList = new ArrayList<>();
    private ArrayList<String> secondaryTextList = new ArrayList<>();

    public RecyclerViewAdapter(Context context, ArrayList<String> placeIdList, ArrayList<String> primaryTextList, ArrayList<String> secondaryTextList) {
        this.context = context;
        this.placeIdList = placeIdList;
        this.primaryTextList = primaryTextList;
        this.secondaryTextList = secondaryTextList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout listItem;
        TextView primaryText;
        TextView secondaryText;
        String placeId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            listItem = itemView.findViewById(R.id.locationListItem);
            primaryText = itemView.findViewById(R.id.locationPrimaryText);
            secondaryText = itemView.findViewById(R.id.locationSecondaryText);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_location_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder(): called.");

        holder.primaryText.setText(primaryTextList.get(position));
        holder.secondaryText.setText(secondaryTextList.get(position));
        holder.placeId = placeIdList.get(position);

        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Location selected by user: " + holder.primaryText.getText().toString());
                Toast.makeText(context, holder.placeId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return primaryTextList.size();
    }

}
