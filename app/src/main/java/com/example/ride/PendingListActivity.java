package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ride.Adapter.PendingAdapter;
import com.example.ride.Model.PendingItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PendingListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView textView;
    PendingAdapter pendingAdapter;
    DatabaseReference database;
    List<PendingItem>pendingItemList;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_list);

        recyclerView = findViewById(R.id.pendingRecyclerView);
        textView = findViewById(R.id.pendingStatus);
        type = getIntent().getStringExtra("type");


        if(type.equals("Passenger"))
        {
            textView.setText("Pending Passenger List");
        }
        if(type.equals("Driver"))
        {
            textView.setText("Pending Driver List");
        }

        database = FirebaseDatabase.getInstance().getReference("VerifyNidLicense").child(type);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pendingItemList = new ArrayList<>();

        final String[] dob = {""};
        final String[] nidOrLicense = { "" };
        final String[] imgUrl = { "" };


        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists() && snapshot.getChildrenCount()>0)
                {


                    for(DataSnapshot ds : snapshot.getChildren())
                    {
                        String key = ds.getKey();

                        if(ds.child("dob").exists())
                        {
                            dob[0] = ds.child("dob").getValue().toString();
                        }
                        if(ds.child("nid").exists())
                        {
                            nidOrLicense[0] = ds.child("nid").getValue().toString();
                        }

                        if(ds.child("verify").getValue().toString() == "false")
                        {

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            if(type.equals("Passenger"))
                            {
                                if(ds.child("nidImg").exists())
                                {
                                    imgUrl[0] = ds.child("nidImg").getValue().toString();
                                }
                                databaseReference.child("Customers").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if(snapshot.exists() && snapshot.getChildrenCount()>0)
                                        {
                                            for(DataSnapshot ds1: snapshot.getChildren())
                                            {
                                                String key2 = ds1.getKey();
                                                if(key2.equals(key))
                                                {
                                                    PendingItem item = new PendingItem(ds1.child("name").getValue().toString(),ds1.child("email").getValue().toString(),ds1.child("phone").getValue().toString(),key);
                                                    pendingItemList.add(item);
                                                    pendingAdapter.notifyDataSetChanged();
                                                    break;
                                                }
                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }
                            if(type.equals("Driver"))
                            {
                                if(ds.child("licenseImg").exists())
                                {
                                    imgUrl[0] = ds.child("licenseImg").getValue().toString();
                                }
                                databaseReference.child("Drivers").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if(snapshot.exists() && snapshot.getChildrenCount()>0)
                                        {
                                            for(DataSnapshot ds1: snapshot.getChildren())
                                            {
                                                String key2 = ds1.getKey();
                                                if(key2.equals(key))
                                                {
                                                    PendingItem item = new PendingItem(ds1.child("name").getValue().toString(),ds1.child("email").getValue().toString(),ds1.child("phone").getValue().toString(),key);
                                                    pendingItemList.add(item);
                                                    pendingAdapter.notifyDataSetChanged();
                                                    break;
                                                }
                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }
                        }


                    }
                    pendingAdapter = new PendingAdapter(PendingListActivity.this, pendingItemList, new PendingAdapter.ItemClickListener() {
                        @Override
                        public void onItemCLick(PendingItem item) {

                            Intent intent = new Intent(PendingListActivity.this,SinglePendingItemActivity.class);

                            intent.putExtra("name",item.getName());
                            intent.putExtra("email",item.getEmail());
                            intent.putExtra("phone",item.getPhone());
                            intent.putExtra("userId",item.getUserId());
                            intent.putExtra("dob", dob[0]);
                            intent.putExtra("nidOrLicense", nidOrLicense[0]);
                            intent.putExtra("imgUrl", imgUrl[0]);
                            intent.putExtra("type",type);
                            startActivity(intent);


                        }
                    });
                    recyclerView.setAdapter(pendingAdapter);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PendingListActivity.this,AdminHomeActivity.class));
        finish();
    }
}