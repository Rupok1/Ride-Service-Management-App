package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ride.HistoryRecyclerView.History;
import com.example.ride.HistoryRecyclerView.HistoryAdapter;
import com.example.ride.databinding.ActivityCustomerHistoryBinding;
import com.example.ride.databinding.ActivityCustomerPersonalInfoBinding;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerHistoryActivity extends MainActivity {

    ActivityCustomerHistoryBinding binding;
    private RecyclerView historyRecycler;
    private RecyclerView.Adapter historyAdapter;
    private RecyclerView.LayoutManager historyLayoutManager;
    String user,userID;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        historyRecycler = findViewById(R.id.historyRecycler);
        historyRecycler.setNestedScrollingEnabled(false);
        historyRecycler.setHasFixedSize(true);
        historyLayoutManager = new LinearLayoutManager(CustomerHistoryActivity.this);
        historyRecycler.setLayoutManager(historyLayoutManager);

        historyAdapter = new HistoryAdapter(getDataSetHistory(),CustomerHistoryActivity.this);
        historyRecycler.setAdapter(historyAdapter);

        mAuth = FirebaseAuth.getInstance();

        user = getIntent().getStringExtra("user");
        userID = mAuth.getCurrentUser().getUid();

        getUSerHistoryId();

    }

    private void getUSerHistoryId() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user).child(userID).child("history");

        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot history: snapshot.getChildren())
                    {
                        FetchRideInfo(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void FetchRideInfo(String rideKey) {

        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("History").child(rideKey);

        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                   String rideId = snapshot.getKey();
                   Long timeStamp = 0L;
                   for(DataSnapshot child: snapshot.getChildren())
                   {
                       if(child.getKey().equals("timestamp"))
                       {
                           timeStamp = Long.valueOf(child.getValue().toString());
                       }
                   }
                    History item = new History(rideId,getDate(timeStamp));
                    resultsHistory.add(item);
                    historyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private String getDate(long timestamp) {

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp * 1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

        return date;
    }

    private ArrayList resultsHistory = new ArrayList<History>();
    private ArrayList<History> getDataSetHistory() {


        return  resultsHistory;

    }
}