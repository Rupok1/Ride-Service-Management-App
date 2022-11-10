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
import com.example.ride.Model.CustomerAvailable;
import com.example.ride.Model.DriverAvailable;
import com.example.ride.R;
import com.example.ride.User;
import com.example.ride.User2;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class CustomerAvailableAdapter extends RecyclerView.Adapter<CustomerAvailableAdapter.ViewHolder> {

    Context context;
    ArrayList<CustomerAvailable>userArrayList;
    private ItemClickListener itemClickListener;

    public CustomerAvailableAdapter(Context context, ArrayList<CustomerAvailable> userArrayList,ItemClickListener itemClickListener) {
        this.context = context;
        this.userArrayList = userArrayList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public CustomerAvailableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.customer_available,parent,false);


        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerAvailableAdapter.ViewHolder holder, int position) {

        CustomerAvailable user = userArrayList.get(position);

        holder.name.setText("Name: "+user.getName());
        holder.phone.setText("Phone: "+user.getPhone());
        String profileImgUrl = user.getProfileImageUrl();
        Glide.with(context)
                .load(profileImgUrl)
                .into(holder.customerImg);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              itemClickListener.onItemClick(userArrayList.get(position));
            }
        });

    }
    public interface ItemClickListener{
        void onItemClick(CustomerAvailable user);
    }
    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,carType,phone,rating,service,cost;
        ImageView customerImg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.nameId);
            phone = itemView.findViewById(R.id.phoneId);
            customerImg = itemView.findViewById(R.id.driverImg);


        }
    }

}
