package com.example.ride;

import static java.lang.Math.floor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ride.HistoryRecyclerView.History;
import com.example.ride.HistoryRecyclerView.HistoryAdapter;
import com.example.ride.databinding.ActivityHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HistoryActivity extends MainActivity {

    ActivityHistoryBinding binding;
    private RecyclerView historyRecycler;
    private RecyclerView.Adapter historyAdapter;
    private RecyclerView.LayoutManager historyLayoutManager;
    LinearLayout linearLayout;
    String user,userID;
    FirebaseAuth mAuth;
    private TextView mBalance,avaBalance,alreadyPayId;
    private int balance = 0,temp = 0;
    Button adminPay;
    private Boolean flag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        historyRecycler = findViewById(R.id.historyRecycler);
        historyRecycler.setNestedScrollingEnabled(false);
        historyRecycler.setHasFixedSize(true);
        historyLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        historyRecycler.setLayoutManager(historyLayoutManager);

        historyAdapter = new HistoryAdapter(getDataSetHistory(), HistoryActivity.this);
        historyRecycler.setAdapter(historyAdapter);

        mBalance = findViewById(R.id.balanceId);
        avaBalance = findViewById(R.id.avBalanceId);
        alreadyPayId = findViewById(R.id.alreadyPayId);
        linearLayout = findViewById(R.id.driver_admin);
        adminPay = findViewById(R.id.pay_to_Admin);
        mAuth = FirebaseAuth.getInstance();

        user = getIntent().getStringExtra("user");
        userID = mAuth.getCurrentUser().getUid();



        if (user.equals("Drivers"))
        {
            linearLayout.setVisibility(View.VISIBLE);
            adminPay.setVisibility(View.VISIBLE);
        }


        adminPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((int)(temp*.1) <1)
                {
                    Toast.makeText(HistoryActivity.this,"Minimum transaction 1 Tk !!",Toast.LENGTH_SHORT).show();
                }
                else {

                    Intent intent = new Intent(HistoryActivity.this, PaymentActivity.class);
                    intent.putExtra("rPrice", "" + (int)(temp*.1));
                    intent.putExtra("user", "Driver");
                    intent.putExtra("driverEmail", mAuth.getCurrentUser().getEmail());
                    flag = true;
                    startActivity(intent);
                }
            }
        });



        if(!flag)
        {
            getUSerHistoryId();
        }



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
                        if(user.equals("Drivers"))
                        {

                            CalculateCost(history.getKey());
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void CalculateCost(String key) {

        DatabaseReference historyDatabase2 = FirebaseDatabase.getInstance().getReference().child("History").child(key);

        historyDatabase2.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {

                if(ds.exists())
                {
                    String distance = "";
                    ridePrice = 0;

                        if(ds.child("customerPaid").getValue()!= null)
                        {
                            if (ds.child("rideDistance").getValue() !=null) {
                                distance = ds.child("rideDistance").getValue().toString();
                                Toast.makeText(HistoryActivity.this, ""+distance, Toast.LENGTH_SHORT).show();
                                ridePrice = (int)((Double.valueOf(distance)/1000)*22);
                                balance += ridePrice;

                            }
                        }
                        ridePrice = 0;
                        distance = "";
                        if(ds.child("driverPaidOut").getValue()!= null)
                        {
                            if (ds.child("rideDistance").getValue() !=null) {
                                distance = ds.child("rideDistance").getValue().toString();
                                ridePrice = (int)((Double.valueOf(distance)/1000)*22);
                                totalAdminPay += ridePrice;

                            }
                        }
                    ridePrice = 0;
                    distance = "";
                    if(ds.child("customerPaid").getValue()!= null && ds.child("driverPaidOut").getValue() == null)
                    {
                        if (ds.child("rideDistance").getValue() !=null) {
                            distance = ds.child("rideDistance").getValue().toString();
                            ridePrice = (int)((Double.valueOf(distance)/1000)*22);
                            temp += ridePrice;

                        }
                    }
                    Toast.makeText(HistoryActivity.this, "temp: "+temp+"balance: "+balance, Toast.LENGTH_SHORT).show();

                    mBalance.setText("Admin to Pay: "+(int)(temp*.1)+" tk");
                    avaBalance.setText("Earned: "+(int)(balance)+" tk");
                    alreadyPayId.setText("Paid: "+(int)(totalAdminPay*.1)+" tk");
                    // Toast.makeText(HistoryActivity.this,"HI: "+i,Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    int ridePrice = 0;
    int totalAdminPay = 0;
    private void FetchRideInfo(String rideKey) {

        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("History").child(rideKey);

        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {

                   String rideId = snapshot.getKey();
                   Long timeStamp = 0L;

                   if(snapshot.child("timestamp")!= null)
                   {
                       timeStamp = Long.valueOf(snapshot.child("timestamp").getValue().toString());
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