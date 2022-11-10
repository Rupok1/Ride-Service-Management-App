package com.example.ride.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ride.CustomerPersonalInfoActivity;
import com.example.ride.Model.DriverAvailable;
import com.example.ride.R;
import com.example.ride.User;
import com.example.ride.User2;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class DriverAvailableAdapter extends RecyclerView.Adapter<DriverAvailableAdapter.ViewHolder> {

    Context context;
    ArrayList<DriverAvailable>userArrayList;

    public DriverAvailableAdapter(Context context, ArrayList<DriverAvailable> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public DriverAvailableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.driver_available_item,parent,false);


        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverAvailableAdapter.ViewHolder holder, int position) {

        DriverAvailable user = userArrayList.get(position);

        holder.name.setText("Name: "+user.getName());
        holder.carType.setText("Car Type: "+user.getCartype());
        holder.phone.setText("Phone: "+user.getPhone());
        holder.rating.setText("Rating: "+user.getRating());
        holder.service.setText("Service: "+user.getSerivce());
        String profileImgUrl = user.getProfileImageUrl();
        Glide.with(context)
                .load(profileImgUrl)
                .into(holder.driverImg);

    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,carType,phone,rating,service,cost;
        ImageView driverImg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.nameId);
            carType = itemView.findViewById(R.id.carTypeId);
            phone = itemView.findViewById(R.id.phoneId);
            rating = itemView.findViewById(R.id.ratingId);
            service = itemView.findViewById(R.id.serviceId);
            driverImg = itemView.findViewById(R.id.driverImg);


        }
    }

}
