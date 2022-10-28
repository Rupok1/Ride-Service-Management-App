package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private String rideId,userId,customerId,driverId,userOrDriver;
    private TextView rideLocation,rideDistance,rideDate,userName,userPhone;
    private ImageView userImg;
    DatabaseReference historyRideRef;
    private LatLng destinationLatLng,pickUpLatLng;
    private String Apikey = "AIzaSyDr3wY3Ek5Fm3snGqeT7sv8I1o3Y3_RJg0";
    private RatingBar ratingBar;
    private String distance;
    private Double ridePrice;
    private Button pay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        polylines = new ArrayList<>();
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        rideId = getIntent().getExtras().getString("rideId");

        rideLocation = findViewById(R.id.rideLocation);
        rideDistance = findViewById(R.id.rideDistance);
        rideDate = findViewById(R.id.rideDate);
        rideLocation = findViewById(R.id.rideLocation);

        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        userImg = findViewById(R.id.userImg);

        ratingBar = findViewById(R.id.ratingBar);
        pay = findViewById(R.id.pay);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyRideRef = FirebaseDatabase.getInstance().getReference().child("History").child(rideId);

        getRideInfo();

    }

    private void getRideInfo() {
        historyRideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot child:snapshot.getChildren())
                    {
                        if(child.getKey().equals("customer"))
                        {
                            customerId = child.getValue().toString();
                            if(!customerId.equals(userId))
                            {
                                userOrDriver = "Drivers";
                                getUserInfo("Customers",customerId);

                            }
                        }
                        if(child.getKey().equals("rideDistance"))
                        {
                            distance = child.getValue().toString();
                            rideDistance.setText(distance.substring(0,Math.min(distance.length(),5))+ " km");
                            ridePrice = Double.valueOf(distance) * 12;
                        }
                        if(child.getKey().equals("driver"))
                        {
                            driverId = child.getValue().toString();
                            if(!driverId.equals(userId))
                            {
                                userOrDriver = "Customers";
                                getUserInfo("Drivers",driverId);
                                displayCustomerRelatedInfo(ridePrice);

                            }
                        }
                        if(child.getKey().equals("timestamp"))
                        {
                          rideDate.setText("Ride Date: "+getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if(child.getKey().equals("rating"))
                        {
                            ratingBar.setRating(Integer.valueOf(child.getValue().toString()));
                        }
                        if(child.getKey().equals("destination"))
                        {
                            rideLocation.setText("Destination: "+child.getValue().toString());
                        }
                        if(child.getKey().equals("location"))
                        {
                            pickUpLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()),Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()),Double.valueOf(child.child("to").child("lng").getValue().toString()));

                            if(destinationLatLng != new LatLng(0,0))
                            {
                                getRouteToMarker();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayCustomerRelatedInfo(Double rPrice) {
        ratingBar.setVisibility(View.VISIBLE);
        pay.setVisibility(View.VISIBLE);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                historyRideRef.child("rating").setValue(rating);
                DatabaseReference mDriverRatingRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("rating");
                mDriverRatingRef.child(rideId).setValue(rating);
            }
        });
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistorySingleActivity.this,PaymentActivity.class);
                intent.putExtra("price",rPrice);
                startActivity(intent);
            }
        });
    }

    private void getUserInfo(String userOrDriver, String userId) {

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userOrDriver).child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    Map<String, Object> map = ( Map<String, Object>)snapshot.getValue();

                    if(map.get("name")!=null)
                    {
                        userName.setText("Name: "+map.get("name").toString());
                    }
                    if(map.get("phone")!=null)
                    {
                        userPhone.setText("Phone: "+map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl")!=null)
                    {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(userImg);
                    }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickUpLatLng,destinationLatLng)
                .key(Apikey)
                .build();
        routing.execute();
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.gradient_end_color};

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickUpLatLng);
        builder.include(destinationLatLng);

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int)(width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding);

        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(pickUpLatLng).title("pickUp location").icon(BitmapDescriptorFactory.fromResource(R.drawable.person_pin_circle)));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("destination"));

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines()
    {
        for(Polyline line: polylines)
        {
            line.remove();
        }
        polylines.clear();
    }
}