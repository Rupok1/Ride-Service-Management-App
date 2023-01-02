package com.example.ride;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
    SupportMapFragment mapFragment;
    DatabaseReference drf;

    AutocompleteSupportFragment autocompleteFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
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

                drf = FirebaseDatabase.getInstance().getReference();
                drf.child("VerifyNidLicense").child("Passenger").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.getChildrenCount()>0)
                        {
                            Map<String, Object>map = (Map<String, Object>) snapshot.getValue();

                            if(map.get("verify").equals(true))
                            {
                                if(requestBol)
                                {
                                    endRide();

                                }
                                else
                                {

                                    if(destination != null)
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



                                        pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick Me Up"));

                                        Toast.makeText(CustomerMapActivity.this, "Getting Your Driver...", Toast.LENGTH_SHORT).show();

                                        getClosestDriver();

                                    }
                                    else
                                    {
                                        Toast.makeText(CustomerMapActivity.this,"Please select a destination",Toast.LENGTH_SHORT).show();
                                    }


                                }


                            }
                            if(map.get("verify").equals(false))
                            {
                                Toast.makeText(CustomerMapActivity.this, "Please wait to verify your account\n Your account will be activate in 2 hours!!\n", Toast.LENGTH_SHORT).show();

                            }


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }
        });

//        searchLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//
//                String location = searchLocation.getQuery().toString();
//                List<Address>addressList = null;
//
//                if(location!=null || !location.equals(""))
//                {
//                    Geocoder geocoder = new Geocoder(CustomerMapActivity.this);
//                    try {
//                        addressList = geocoder.getFromLocationName(location,1);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    Address address = addressList.get(0);
//                    destinationLatLng = new LatLng(address.getLatitude(),address.getLongitude());
//
//                    destinationMarker =  mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination"));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng,17));
////                    addressList.clear();
////                    m.remove();
//                }
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });




        // Initialize the AutocompleteSupportFragment.
         autocompleteFragment = (AutocompleteSupportFragment)
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
    String email = null,msg,subj;

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

                                    Map<String, Object>nmap = (Map<String, Object>) snapshot.getValue();





                                    if(nmap.get("email")!=null)
                                    {
                                        email = nmap.get("email").toString();

                                    }
                                    msg = "You have customer request from Ride App. Please check";
                                    subj = "Got a Customer";

                                    sendEmailToDriver(email,msg,subj);





//                                    DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
//                                    dRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                            Toast.makeText(CustomerMapActivity.this, "Hi-2:"+snapshot, Toast.LENGTH_SHORT).show();
//                                            if(snapshot.exists() && snapshot.getChildrenCount()>0)
//                                            {
//
//                                                Map<String, Object>map = (Map<String, Object>) snapshot.getValue();
//
//                                                String email = null,msg,subj;
//
//                                                if(map.get("phone")!=null)
//                                                {
//                                                    email = map.get("phone").toString();
//
//                                                }
//                                                Toast.makeText(CustomerMapActivity.this, "Hi: "+email, Toast.LENGTH_SHORT).show();
//                                                msg = "You have customer request from Ride App. Please check";
//                                                subj = "Got a Customer notification";
////                                                try {
////
////                                                        SmsManager smsManager = SmsManager.getDefault();
////                                                        smsManager.sendTextMessage(no,null,msg,null,null);
////                                                        Toast.makeText(CustomerMapActivity.this,"Driver get your request successfully"+no,Toast.LENGTH_SHORT).show();
////
////                                                }catch (Exception e)
////                                                {
////                                                    Toast.makeText(CustomerMapActivity.this,"Error: "+e,Toast.LENGTH_SHORT).show();
////
////                                                }
////
//
//                                               sendEmail(email,msg,subj);
//
//
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError error) {
//
//                                        }
//                                    });


                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map =new HashMap();
                                    map.put("customerRideId",customerId);
                                    map.put("destinationLat",destinationLatLng.latitude);
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

                if(!requestBol)
                {
                    return;
                }
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

    private void sendEmailToDriver(String email, String msg, String subj) {

        String username = "rideapp1807000306@gmail.com";
        String password = "fyxeyupawzzdggms";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth","true");
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.smtp.host","smtp.gmail.com");
        properties.put("mail.smtp.port","587");

        Session session = Session.getInstance(properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return  new PasswordAuthentication(username,password);
                    }
                });

        Thread gfgThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(username));
                    message.setRecipient(Message.RecipientType.TO,new InternetAddress(email));
                    message.setSubject(subj);
                    message.setText(msg);
                    Transport.send(message);
                }catch (MessagingException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        gfgThread.start();
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

                    float tPrice = (dis/1000) * 22;
                    price.setText("Price: "+tPrice + " tk");

                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver"));
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

        autocompleteFragment.setText("");
        destination = null;
        call_a_car.setText("Call A Car");
        requestBol = false;
        geoQuery.removeAllListeners();
        if(driverLocationRef != null)
        {
            driverLocationRef.removeEventListener(driverLocationRefListener);
        }
       if(driveHasEndRef != null)
       {
           driveHasEndRef.removeEventListener(driveHasEndRefListener);
       }

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
        if(pickUpMarker != null)
        {
            pickUpMarker.remove();
        }
        if(driverMarker != null)
        {
            driverMarker.remove();
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

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        View zoomControls = mapFragment.getView().findViewById((int)0x1);

        if (zoomControls != null && zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            RelativeLayout.LayoutParams params_zoom = (RelativeLayout.LayoutParams) zoomControls.getLayoutParams();

            // Align it to - parent top|left
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            // Update margins, set to 10dp
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120,
                    getResources().getDisplayMetrics());
            params_zoom.setMargins(0, 0, 0, margin);

        }
        View navigation_control = mapFragment.getView().findViewById((int)0x2);

        if (navigation_control != null && navigation_control.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            RelativeLayout.LayoutParams params_zoom = (RelativeLayout.LayoutParams) navigation_control.getLayoutParams();

            // Align it to - parent top|left
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            // Update margins, set to 10dp
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                    getResources().getDisplayMetrics());
            params_zoom.setMargins(0,0 , 0,margin);

        }

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

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Current_Location");
        GeoFire geoFire = new GeoFire(ref);
        if(mAuth.getCurrentUser() != null)
        {
            geoFire.setLocation(mAuth.getCurrentUser().getUid(),new GeoLocation(location.getLatitude(),location.getLongitude()));
        }



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