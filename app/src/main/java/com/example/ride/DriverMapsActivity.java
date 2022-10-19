package com.example.ride;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.ride.databinding.ActivityDriverMapsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class DriverMapsActivity extends MainActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ActivityDriverMapsBinding binding;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    FirebaseAuth mAuth;
    private String customerId = "";
    private LinearLayout customerInfo;
    private ImageView customerImage;
    private TextView cName,cPhone,customerDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();

        customerInfo = findViewById(R.id.customerInfo);
        customerImage = findViewById(R.id.customerImage);
        cName = findViewById(R.id.customerName);
        cPhone = findViewById(R.id.customerPhone);
        customerDestination = findViewById(R.id.destinationId);
        
        getAssignedCustomer();


    }

    private void getAssignedCustomer() {
        String driverId = FirebaseAuth.getInstance().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    customerId = snapshot.getValue().toString();
                    getAssignedCustomerPickUpLocation();
                    getAssignedCustomerDestination();
                    getAssignedCustomerInfo();
                }
                else
                {
                    customerId = "";
                    if(pickUpMarker !=null)
                    {
                        pickUpMarker.remove();
                    }
                    if (assignedCustomerPickUpLocationRefListener != null)
                    {
                        assignedCustomerPickUpLocationRef.removeEventListener(assignedCustomerPickUpLocationRefListener);
                    }
                    customerInfo.setVisibility(View.GONE);
                    cName.setText("");
                    cPhone.setText("");
                    customerDestination.setText("Destination: --");
                    customerImage.setImageResource(R.drawable.undraw_male_avatar_323b);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getAssignedCustomerDestination() {
        String driverId = FirebaseAuth.getInstance().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("destination");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    String destination = snapshot.getValue().toString();
                    customerDestination.setText("Destination: "+destination);
                }
                else
                {
                    customerDestination.setText("Destination: ");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAssignedCustomerInfo() {
        customerInfo.setVisibility(View.VISIBLE);
       DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0)
                {
                    Map<String, Object>map = (Map<String, Object>) snapshot.getValue();

                    if(map.get("name")!=null)
                    {
                        cName.setText("Name: "+map.get("name").toString());
                    }
                    if(map.get("phone")!=null)
                    {
                        cPhone.setText("Mobile: "+map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl")!=null)
                    {
                        Glide.with(DriverMapsActivity.this)
                                .load(map.get("profileImageUrl").toString())
                                .into(customerImage);

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    Marker pickUpMarker;
   private DatabaseReference assignedCustomerPickUpLocationRef;
   private ValueEventListener assignedCustomerPickUpLocationRefListener;
    private void getAssignedCustomerPickUpLocation() {

        assignedCustomerPickUpLocationRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customerId).child("l");
        assignedCustomerPickUpLocationRefListener = assignedCustomerPickUpLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists() && !customerId.equals(""))
                {
                   List<Object>map = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    if(map.get(0) != null)
                    {
                        locationLat =Double.parseDouble(map.get(0).toString()) ;
                    }
                    if(map.get(1) != null)
                    {
                        locationLng = Double.parseDouble(map.get(1).toString()) ;
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);

                  pickUpMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("PickUp Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.person_pin_circle)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);


//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        if(getApplicationContext()!=null)
        {
            if(aSwitch.isChecked())
            {
                lastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17));


                String userId = mAuth.getCurrentUser().getUid();
                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriversWorking");
                GeoFire geoFireAvailable = new GeoFire(refAvailable);
                GeoFire geoFireWorking= new GeoFire(refWorking);
                switch (customerId)
                {
                    case "":
                        geoFireWorking.removeLocation(userId);
                        geoFireAvailable.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                        break;

                    default:
                        geoFireAvailable.removeLocation(userId);
                        geoFireWorking.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                        break;

                }

            }
            else
            {
                lastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

                String userId = mAuth.getCurrentUser().getUid();
                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                GeoFire geoFireAvailable = new GeoFire(refAvailable);
                geoFireAvailable.removeLocation(userId);

            }


        }

//        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked)
//                {
//                    String userId = mAuth.getCurrentUser().getUid();
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");
//                    GeoFire geoFire = new GeoFire(ref);
//                    geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
//                    String userId = mAuth.getCurrentUser().getUid();
//                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriversAvailable");
//                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriversWorking");
//                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
//                    GeoFire geoFireWorking= new GeoFire(refWorking);
//                    switch (customerId)
//                    {
//                        case "":
//                            geoFireWorking.removeLocation(userId);
//                            geoFireAvailable.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
//                            break;
//
//                        default:
//                            geoFireAvailable.removeLocation(userId);
//                            geoFireWorking.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
//                            break;
//
//                    }
//
//                }
//                else
//                {
//                    String userId = mAuth.getCurrentUser().getUid();
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");
//                    GeoFire geoFire = new GeoFire(ref);
//                    geoFire.removeLocation(userId);
//                }
//            }
//        });



    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, DriverMapsActivity.this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();
//        String userId = mAuth.getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.removeLocation(userId);
    }
}