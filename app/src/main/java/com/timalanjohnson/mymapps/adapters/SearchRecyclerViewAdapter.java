package com.timalanjohnson.mymapps.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timalanjohnson.mymapps.R;

import java.util.ArrayList;

public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapterLog";

    private Context context;
    public ArrayList<String> placeIdList = new ArrayList<>();
    private ArrayList<String> primaryTextList = new ArrayList<>();
    private ArrayList<String> secondaryTextList = new ArrayList<>();

    private OnItemClickListener itemListener;

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    public void SetOnItemClickListener(OnItemClickListener listener){
        itemListener = listener;
    }

    public SearchRecyclerViewAdapter(Context context, ArrayList<String> placeIdList, ArrayList<String> primaryTextList, ArrayList<String> secondaryTextList) {
        this.context = context;
        this.placeIdList = placeIdList;
        this.primaryTextList = primaryTextList;
        this.secondaryTextList = secondaryTextList;
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
                //Toast.makeText(context, holder.primaryText.getText().toString(), Toast.LENGTH_SHORT).show();

                if (itemListener != null) {
                    itemListener.OnItemClick(position);
                    Log.d(TAG, "itemListener" + holder.placeId);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return primaryTextList.size();
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

}
