package com.example.ride.HistoryRecyclerView;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ride.R;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideId;
    public HistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = itemView.findViewById(R.id.rideId);
    }

    @Override
    public void onClick(View v) {

    }
}
