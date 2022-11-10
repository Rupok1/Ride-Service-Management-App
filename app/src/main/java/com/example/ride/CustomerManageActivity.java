package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.ride.Adapter.CustomerAvailableAdapter;
import com.example.ride.Adapter.DriverAvailableAdapter;
import com.example.ride.Model.CustomerAvailable;
import com.example.ride.Model.DriverAvailable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CustomerManageActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DriverAvailable available;
    ArrayList<DriverAvailable>driverAvailables;
    DriverAvailableAdapter availableAdapter;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_manage);

        recyclerView = findViewById(R.id.customerListId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();

        String user = getIntent().getStringExtra("user");

        if(user.equals("Driver"))
        {
            getDriverData();

        }
        else if(user.equals("Customer"))
        {
            getCustomerData();
        }



    }

    private void getCustomerData() {
        ArrayList<CustomerAvailable>customerAvailables = new ArrayList<>();


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference("Users").child("Customers");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot ds : snapshot.getChildren())
                    {


                        CustomerAvailable available = new CustomerAvailable(ds.child("name").getValue().toString(),ds.child("phone").getValue().toString(),ds.child("profileImageUrl").getValue().toString());
                        customerAvailables.add(available);

                        CustomerAvailableAdapter availableAdapter = new CustomerAvailableAdapter(CustomerManageActivity.this,customerAvailables, new CustomerAvailableAdapter.ItemClickListener(){
                            @Override
                            public void onItemClick(CustomerAvailable user) {



                            }
                        });
                        recyclerView.setAdapter(availableAdapter);
                        availableAdapter.notifyDataSetChanged();


                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void getDriverData() {

        driverAvailables = new ArrayList<>();


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference("Users").child("Drivers");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot ds : snapshot.getChildren())
                    {

                        int ratingSum = 0;
                        float ratingTotal = 0;
                        float ratingAvg = 0;
                        if(ds.child("rating")!=null)
                        {
                            for(DataSnapshot ds2:ds.child("rating").getChildren())
                            {
                                ratingSum = ratingSum + Integer.valueOf(ds2.getValue().toString());
                                ratingTotal++;
                            }

                            if(ratingTotal != 0)
                            {
                                ratingAvg = ratingSum/ratingTotal;
                            }

                        }

                        if(ds.child("profileImageUrl") == null)
                        {

                        }


                        available = new DriverAvailable(ds.child("name").getValue().toString(),ds.child("phone").getValue().toString(),ds.child("cartype").getValue().toString(),String.valueOf(ratingAvg).substring(0,3),ds.child("service").getValue().toString(),ds.child("profileImageUrl").getValue().toString());

                        Toast.makeText(CustomerManageActivity.this,ds.child("cartype").getValue().toString(),Toast.LENGTH_SHORT).show();
                        driverAvailables.add(available);

                        availableAdapter = new DriverAvailableAdapter(CustomerManageActivity.this,driverAvailables);
                        recyclerView.setAdapter(availableAdapter);
                        availableAdapter.notifyDataSetChanged();


                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

//    private void getDriverData() {
//        fstore.collection("users").orderBy("name", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                for(DocumentChange dc : value.getDocumentChanges())
//                {
//                    User user = dc.getDocument().toObject(User.class);
//                    User2 user2 = new User2(user.getName(),user.getEmail(),user.getPhone(),user.getType(),"25tk");
//
//                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//
//
//
//                    //  Toast.makeText(CustomerManageActivity.this,user.getPhone(),Toast.LENGTH_SHORT).show();
//                    if(user2.type.equals("Driver"))
//                    {
//                        userArrayList.add(user2);
//                    }
//                    customerManageAdapter.notifyDataSetChanged();
//
//                }
//
//            }
//        });
//
//    }

//    private void getData() {
//        fstore.collection("users").orderBy("name", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                for(DocumentChange dc : value.getDocumentChanges())
//                {
//                    User user = dc.getDocument().toObject(User.class);
//                    User2 user2 = new User2(user.getName(),user.getEmail(),user.getPhone(),user.getType(),"25tk");
//
//                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//
//
//
//                  //  Toast.makeText(CustomerManageActivity.this,user.getPhone(),Toast.LENGTH_SHORT).show();
//                    if(user2.type.equals("Traveller"))
//                    {
//                       userArrayList.add(user2);
//                    }
//                    customerManageAdapter.notifyDataSetChanged();
//
//                }
//
//            }
//        });
//
//    }
}