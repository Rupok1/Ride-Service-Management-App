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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ride.databinding.ActivityCustomerMapBinding;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

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
    String destination,requestService;
    PlacesClient placesClient;
    private String Apikey = "AIzaSyDr3wY3Ek5Fm3snGqeT7sv8I1o3Y3_RJg0";
    LinearLayout driverInfo;
    TextView driverName,driverPhone,carType,price;
    ImageView driverImg;
    Button call_a_car;
    private RadioGroup radioGroup;
    private LatLng destinationLatLng;
    private RatingBar ratingBar;

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

        driverInfo = findViewById(R.id.driverInfo);
        driverName = findViewById(R.id.driverName);
        driverPhone = findViewById(R.id.driverPhone);
        driverImg = findViewById(R.id.driverImage);
        carType = findViewById(R.id.carType);
        call_a_car = findViewById(R.id.call_A_car);
        price = findViewById(R.id.price);

        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.check(R.id.rideX);

        ratingBar = findViewById(R.id.ratingBar);

        destinationLatLng = new LatLng(0.0,0.0);

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
                   endRide();

               }
               else
               {
                   int selectedId = radioGroup.getCheckedRadioButtonId();

                   RadioButton radioButton = findViewById(selectedId);

                   if(radioButton.getText() == null)
                   {
                       return;
                   }
                   requestService = radioButton.getText().toString();

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
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
                // TODO: Get info about the selected place.
            }


            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(CustomerMapActivity.this,""+status,Toast.LENGTH_SHORT).show();

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
                    DatabaseReference customerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists() && snapshot.getChildrenCount() > 0)
                            {
                                Map<String, Object> driverMap = (Map<String, Object>) snapshot.getValue();

                                if(driverFound)
                                {
                                    return;
                                }
                                if(driverMap.get("service").equals(requestService))
                                {
                                    driverFound = true;
                                    driverFoundId = snapshot.getKey();
                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map =new HashMap();
                                    map.put("customerRideId",customerId);
                                    Toast.makeText(CustomerMapActivity.this,""+destinationLatLng,Toast.LENGTH_SHORT).show();
                                    map.put("destinationLat",destinationLatLng.latitude);
                                    Toast.makeText(CustomerMapActivity.this,""+destinationLatLng.latitude,Toast.LENGTH_SHORT).show();
                                    map.put("destinationLng",destinationLatLng.longitude);
                                    map.put("destination",destination);
                                    driverRef.updateChildren(map);
                                    Toast.makeText(CustomerMapActivity.this, "Found driver", Toast.LENGTH_SHORT).show();
                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();
                                    //   call_a_car.setText("Looking for driver Location...");
                                    Toast.makeText(CustomerMapActivity.this, "Found Driver & Looking for driver Location...", Toast.LENGTH_SHORT).show();
                                }


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


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

    private void getDriverInfo() {
        driverInfo.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0)
                {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

                    if(map.get("name")!=null)
                    {
                        driverName.setText("Name: "+map.get("name").toString());
                    }
                    if(map.get("phone")!=null)
                    {
                        driverPhone.setText("Mobile: "+map.get("phone").toString());
                    }
                    if(map.get("cartype")!=null)
                    {
                        carType.setText("Car Type: "+map.get("cartype").toString());
                    }
                    if(map.get("profileImageUrl")!=null)
                    {
                        Glide.with(CustomerMapActivity.this)
                                .load(map.get("profileImageUrl").toString())
                                .into(driverImg);

                    }


                    int ratingSum = 0;
                    float ratingTotal = 0;
                    float ratingAvg = 0;
                    for(DataSnapshot child:snapshot.child("rating").getChildren())
                    {
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingTotal++;
                    }

                    if(ratingTotal != 0)
                    {
                        ratingAvg = ratingSum/ratingTotal;
                        ratingBar.setRating(ratingAvg);
                    }



                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private float distance = 0, dis = 0;
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


                    Location local3 = new Location("");
                    local3.setLatitude(destinationLatLng.latitude);
                    local3.setLongitude(destinationLatLng.longitude);

                    distance = local.distanceTo(local2);
                    dis = local.distanceTo(local3);
                    Toast.makeText(CustomerMapActivity.this, "Driver distance: "+String.valueOf(dis), Toast.LENGTH_SHORT).show();

                    float tPrice = (dis * 12);
                    price.setText("Price: "+tPrice + " tk");

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

    private DatabaseReference driveHasEndRef;
    private ValueEventListener driveHasEndRefListener;
    private void getHasRideEnded() {


        driveHasEndRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest").child("customerRideId");
        driveHasEndRefListener =  driveHasEndRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {

                }
                else
                {
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void endRide() {

        call_a_car.setText("Call A Car");
        requestBol = false;
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);
        driveHasEndRef.removeEventListener(driveHasEndRefListener);
        if(driverFoundId != null)
        {
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
            driverRef.removeValue();
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
            mMap.addMarker(null);
        }
        driverInfo.setVisibility(View.GONE);
        driverName.setText("");
        driverPhone.setText("");
        driverImg.setImageResource(R.drawable.undraw_male_avatar_323b);
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
        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(20.0f);
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