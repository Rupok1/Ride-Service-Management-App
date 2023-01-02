package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.util.List;
import java.util.Locale;

public class CustomerManageActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DriverAvailable available;
    ArrayList<DriverAvailable>driverAvailables;
    ArrayList<DriverAvailable>driverUnpaidAvailables;
    DriverAvailableAdapter availableAdapter;
    CustomerAvailableAdapter customerAvailableAdapter;
    ArrayList<CustomerAvailable>customerAvailables;
    FirebaseAuth mAuth;
    SearchView searchView;
    String unpaid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_manage);

        recyclerView = findViewById(R.id.customerListId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchView);


        mAuth = FirebaseAuth.getInstance();

        String user = getIntent().getStringExtra("user");
        unpaid = getIntent().getStringExtra("unpaid");




        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filter(newText,user);

                return false;
            }
        });



        if(user.equals("Driver") && unpaid == null)
        {
            getDriverData();
        }
        else if(user.equals("Driver") && unpaid.equals("unpaid"))
        {
            getDriverData2();
        }
        else if(user.equals("Customer"))
        {
            getCustomerData();
        }



    }



    private void filter(String newText,String user) {

        if(user.equals("Driver") && unpaid == null)
        {
            ArrayList<DriverAvailable>driverAvailableList = new ArrayList<>();
            for(DriverAvailable ds: driverAvailables)
            {

                if(ds.getPhone().contains(newText) || ds.getName().toLowerCase().contains(newText.toLowerCase()))
                {
                    driverAvailableList.add(ds);

                }

            }

            availableAdapter.filterList(driverAvailableList);

        }
        else if(user.equals("Driver") && unpaid.equals("unpaid"))
        {
            ArrayList<DriverAvailable>driverAvailableList = new ArrayList<>();
            for(DriverAvailable ds: driverUnpaidAvailables)
            {

                if(ds.getPhone().contains(newText) || ds.getName().toLowerCase().contains(newText.toLowerCase()))
                {
                    driverAvailableList.add(ds);

                }

            }

            availableAdapter.filterList(driverAvailableList);

        }
        else if(user.equals("Customer"))
        {
            ArrayList<CustomerAvailable>customerList = new ArrayList<>();
            for(CustomerAvailable ds: customerAvailables)
            {

                if(ds.getPhone().contains(newText)|| ds.getName().toLowerCase().contains(newText.toLowerCase()))
                {
                    customerList.add(ds);

                }
            }

            customerAvailableAdapter.filterList(customerList);

        }


    }


    private void getCustomerData() {
        customerAvailables = new ArrayList<>();


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


                        customerAvailableAdapter = new CustomerAvailableAdapter(CustomerManageActivity.this, customerAvailables);
                        recyclerView.setAdapter(customerAvailableAdapter);
                        customerAvailableAdapter.notifyDataSetChanged();


                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private int p=0,q=0;

    private void getDriverData2() {

        driverUnpaidAvailables = new ArrayList<>();


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference("Users").child("Drivers");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot ds : snapshot.getChildren())
                    {
                        String key = ds.getKey();
                        x=0;
                        y=0;



                        DatabaseReference databaseReference1 = firebaseDatabase.getReference().child("History");

                        databaseReference1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    for(DataSnapshot ps: snapshot.getChildren())
                                    {
                                        if(ps.child("driver").getValue().toString().equals(key) && ps.child("customerPaid").getValue() != null && ps.child("driverPaidOut").getValue() == null)
                                        {

                                            if (ps.child("customerPaid").getValue() != null)
                                            {
                                                if (ps.child("rideDistance").getValue() !=null) {
                                                    String  distance = ps.child("rideDistance").getValue().toString();
                                                    int ridePrice = (int)(Double.valueOf(distance)/1000)*22;
                                                    p += ridePrice;

                                                }
                                            }
                                            if (ps.child("customerPaid").getValue() != null && ps.child("driverPaidOut").getValue() == null)
                                            {
                                                if (ps.child("rideDistance").getValue() !=null) {
                                                    String  distance = ps.child("rideDistance").getValue().toString();
                                                    int ridePrice = (int)(Double.valueOf(distance)/1000)*22;
                                                    q += ridePrice;
                                                }
                                            }


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




                                            available = new DriverAvailable(ds.child("name").getValue().toString(),ds.child("phone").getValue().toString(),ds.child("cartype").getValue().toString(),String.valueOf(ratingAvg).substring(0,3),ds.child("service").getValue().toString(),ds.child("profileImageUrl").getValue().toString(),String.valueOf(p),String.valueOf(q*.1));

                                            driverUnpaidAvailables.add(available);

                                            availableAdapter = new DriverAvailableAdapter(CustomerManageActivity.this,driverUnpaidAvailables);
                                            recyclerView.setAdapter(availableAdapter);
                                            availableAdapter.notifyDataSetChanged();



                                        }
                                        else {
                                            p=0;
                                            q=0;
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private int x=0,y=0;

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
                                String key = ds.getKey();
                                x=0;
                                y=0;



                                DatabaseReference databaseReference1 = firebaseDatabase.getReference().child("History");

                                databaseReference1.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists())
                                        {
                                            for(DataSnapshot ds: snapshot.getChildren())
                                            {
                                                if(ds.child("driver").getValue().toString().equals(key))
                                                {

                                                    if (ds.child("customerPaid").getValue() != null)
                                                    {
                                                        if (ds.child("rideDistance").getValue() !=null) {
                                                            String  distance = ds.child("rideDistance").getValue().toString();
                                                            int ridePrice = (int)(Double.valueOf(distance)/1000)*22;
                                                            x += ridePrice;
                                                        }
                                                    }
                                                    if (ds.child("customerPaid").getValue() != null && ds.child("driverPaidOut").getValue() == null)
                                                    {
                                                        if (ds.child("rideDistance").getValue() !=null) {
                                                            String  distance = ds.child("rideDistance").getValue().toString();
                                                            int ridePrice = (int)(Double.valueOf(distance)/1000)*22;
                                                            y += ridePrice;
                                                        }
                                                    }


                                                }
                                                else {
                                                    x=0;
                                                    y=0;
                                                }
                                            }
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


                                            available = new DriverAvailable(ds.child("name").getValue().toString(),ds.child("phone").getValue().toString(),ds.child("cartype").getValue().toString(),String.valueOf(ratingAvg).substring(0,3),ds.child("service").getValue().toString(),ds.child("profileImageUrl").getValue().toString(),""+x,""+(y*.1));

                                            driverAvailables.add(available);

                                            availableAdapter = new DriverAvailableAdapter(CustomerManageActivity.this,driverAvailables);
                                            recyclerView.setAdapter(availableAdapter);
                                            availableAdapter.notifyDataSetChanged();


                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });




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