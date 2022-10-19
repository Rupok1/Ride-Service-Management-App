package com.example.ride;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ride.databinding.ActivityCustomerMapBinding;
import com.example.ride.databinding.ActivityDriverMapsBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CustomerMapActivity extends MainActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ActivityCustomerMapBinding binding;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    FirebaseAuth mAuth;
    private LatLng pickUpLocation;
    private Boolean requestBol = false;
    private Marker pickUpMarker;
    String destination;
    PlacesClient placesClient;
    private String Apikey = "AIzaSyAYFABukIvSUz_P6JE8LyuUgjy8dgZdPQU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();


        if(!Places.isInitialized())
        {
            Places.initialize(getApplicationContext(), Apikey); placesClient = Places.createClient(CustomerMapActivity.this);
        }




        call_a_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");

                if(requestBol)
               {
                   call_a_car.setText("Call A Car");
                   requestBol = false;
                   geoQuery.removeAllListeners();
                   driverLocationRef.removeEventListener(driverLocationRefListener);

                   if(driverFoundId != null)
                   {
                       DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
                       driverRef.setValue(true);
                       driverFoundId = null;
                   }
                   driverFound = false;
                   radius = 1;
                   String userId = mAuth.getCurrentUser().getUid();
                   DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CustomerRequest");
                   GeoFire geoFire = new GeoFire(reference);
                   geoFire.removeLocation(userId);
                   if(pickUpMarker != null && driverMarker!=null)
                   {
                       pickUpMarker.remove();
                       driverMarker.remove();
                       driverMarker.setIcon(null);
                   }
               }
               else
               {
                   requestBol = true;
                   call_a_car.setText("Cancel Request");
                   String userId = mAuth.getCurrentUser().getUid();


                   DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CustomerRequest");
                   GeoFire geoFire = new GeoFire(reference);
                   geoFire.setLocation(userId,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));

                   pickUpLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                 //  pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick Me Up"));
                   //    call_a_car.setText("Getting Your Driver...");
                   Toast.makeText(CustomerMapActivity.this, "Getting Your Driver...", Toast.LENGTH_SHORT).show();
                   getClosestDriver();
               }


            }
        });


        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS);
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(-33.880490,151.184363),
                new LatLng(-33.880490,151.184363)
        ));
        autocompleteFragment.setCountry("BD");
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destination = place.getName().toString();
                // TODO: Get info about the selected place.
            }


            @Override
            public void onError(@NonNull Status status) {

            }
        });




    }


    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundId;
    GeoQuery geoQuery;
    private void getClosestDriver() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        GeoFire geoFire = new GeoFire(databaseReference);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude,pickUpLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!driverFound && requestBol)
                {
                    driverFound = true;
                    driverFoundId = key;
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map =new HashMap();
                    map.put("customerRideId",customerId);
                    map.put("destination",destination);
                    driverRef.updateChildren(map);
                    Toast.makeText(CustomerMapActivity.this, "Found driver", Toast.LENGTH_SHORT).show();
                    getDriverLocation();
                 //   call_a_car.setText("Looking for driver Location...");
                    Toast.makeText(CustomerMapActivity.this, "Found Driver & Looking for driver Location...", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
                if(radius>8587)
                {
                    Toast.makeText(CustomerMapActivity.this,"Not found ",Toast.LENGTH_SHORT).show();
                    radius = 1;
                    Toast.makeText(CustomerMapActivity.this,"Searching again..",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private Marker driverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation() {

        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversWorking").child(driverFoundId).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    List<Object>map = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                  //  call_a_car.setText("Driver Location...");
                    if(map.get(0) != null)
                    {
                        locationLat = Double.parseDouble(map.get(0).toString()) ;
                    }
                    if(map.get(1) != null)
                    {
                        locationLng = Double.parseDouble(map.get(1).toString()) ;
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(driverMarker != null)
                    {
                        driverMarker.remove();
                    }
                    Location local = new Location("");
                    local.setLatitude(pickUpLocation.latitude);
                    local.setLongitude(pickUpLocation.longitude);


                    Location local2 = new Location("");
                    local2.setLatitude(driverLatLng.latitude);
                    local2.setLongitude(driverLatLng.longitude);

                    float distance = local.distanceTo(local2);
                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.transport_taxi)));
                    if(distance<100)
                    {

                        Toast.makeText(CustomerMapActivity.this, "Driver is here", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(CustomerMapActivity.this, "Driver distance: "+String.valueOf(distance), Toast.LENGTH_SHORT).show();
                    }


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

        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

//        String userId = mAuth.getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Customer");
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));



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
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, CustomerMapActivity.this);
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
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Customer");
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.removeLocation(userId);
    }
}