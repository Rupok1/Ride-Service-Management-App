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
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.internal.ApiKey;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMapsActivity extends MainActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {

    private GoogleMap mMap;
    private ActivityDriverMapsBinding binding;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    FirebaseAuth mAuth;
    private String customerId = "",destination;
    private LinearLayout customerInfo;
    private ImageView customerImage;
    private TextView cName,cPhone,customerDestination;
    private String Apikey = "AIzaSyDr3wY3Ek5Fm3snGqeT7sv8I1o3Y3_RJg0";
    Switch aSwitch;
    private Button rideStatus;
    private int status = 0;
    private LatLng destinationLatLng;
    private float rideDistance;
    private SupportMapFragment mapFragment;
    private DatabaseReference drf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        polylines = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mAuth = FirebaseAuth.getInstance();

        customerInfo = findViewById(R.id.customerInfo);
        customerImage = findViewById(R.id.customerImage);
        cName = findViewById(R.id.customerName);
        cPhone = findViewById(R.id.customerPhone);
        customerDestination = findViewById(R.id.destinationId);
        aSwitch = findViewById(R.id.switchID);
        rideStatus = findViewById(R.id.rideStatus);

        CalculateCost(mAuth.getCurrentUser().getUid());

        if(temp*.1>=1000)
        {
            aSwitch.setVisibility(View.GONE);
        }
        else
        {
            aSwitch.setVisibility(View.VISIBLE);
        }




        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                drf = FirebaseDatabase.getInstance().getReference("VerifyNidLicense");
                drf.child("Driver").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.getChildrenCount()>0)
                        {
                            Map<String, Object>map = (Map<String, Object>) snapshot.getValue();

                            if(map.get("verify").equals(true))
                            {
                                if(isChecked)
                                {
                                    connectDriver();
                                }
                                else
                                {
                                    disconnectDriver();
                                }
                            }
                            if(map.get("verify").equals(false))
                            {
                                Toast.makeText(DriverMapsActivity.this, "Please wait to verify your account\n Your account will be activate in 2 hours!!", Toast.LENGTH_SHORT).show();
                            }


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });


        rideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status){
                    case 1:
                        status = 2;
                        erasePolylines();
                        if(destinationLatLng.latitude !=0.0 && destinationLatLng.longitude != 0.0)
                        {
                            getRouteToMarker(destinationLatLng);
                        }
                        rideStatus.setText("drive completed");
                        break;
                    case 2:
                        recordRide();
                        endRide();
                        break;
                }
            }
        });
        
        getAssignedCustomer();


    }

    int ridePrice=0,temp=0;
    private void CalculateCost(String key) {
        DatabaseReference historyDatabase2 = FirebaseDatabase.getInstance().getReference().child("History").child(key);

        historyDatabase2.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {

                if(ds.exists())
                {
                    String distance = "";
                    ridePrice = 0;
                    if(ds.child("customerPaid").getValue()!= null && ds.child("driverPaidOut").getValue() == null)
                    {
                        if (ds.child("rideDistance").getValue() !=null) {
                            distance = ds.child("rideDistance").getValue().toString();
                            ridePrice = (int)((Double.valueOf(distance)/1000)*22);
                            temp += ridePrice;

                        }
                    }

                    // Toast.makeText(HistoryActivity.this,"HI: "+i,Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void endRide() {
        rideStatus.setText("Picked Customer");
        erasePolylines();

        if(mAuth.getCurrentUser() != null)
        {
            String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
            driverRef.removeValue();
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CustomerRequest");
        GeoFire geoFire = new GeoFire(reference);
        geoFire.removeLocation(customerId);
        customerId = "";
        rideDistance = 0;
        if(pickUpMarker != null)
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

    private void getAssignedCustomer() {
        String driverId = FirebaseAuth.getInstance().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    status = 1;
                    customerId = snapshot.getValue().toString();
                    getAssignedCustomerPickUpLocation();
                    getAssignedCustomerDestination();
                    getAssignedCustomerInfo();
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

    private void recordRide() {

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("History");
        String requestId = historyRef.push().getKey();

        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver",userId);
        map.put("customer",customerId);
        map.put("rating",0);
        map.put("destination",destination);
        map.put("location/from/lat",pickupLatLng.latitude);
        map.put("location/from/lng",pickupLatLng.longitude);
        map.put("location/to/lat",destinationLatLng.latitude);
        map.put("location/to/lng",destinationLatLng.longitude);
        map.put("rideDistance",rideDistance);
        map.put("timestamp",getCurrentTimeStamp());
        historyRef.child(requestId).updateChildren(map);

    }

    private Long getCurrentTimeStamp() {
        Long timeStamp = System.currentTimeMillis()/1000;
        return timeStamp;
    }

    private void getAssignedCustomerDestination() {
        String driverId = FirebaseAuth.getInstance().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    Map<String,Object>map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("destination") != null)
                    {
                        destination = map.get("destination").toString();
                        customerDestination.setText("Destination: "+destination);
                    }
                    else
                    {
                        customerDestination.setText("Destination: --");
                    }
                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;

                    if(map.get("destinationLat") != null)
                    {
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if(map.get("destinationLng") != null)
                    {
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat,destinationLng);
                    }


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
                        Glide.with(getApplicationContext())
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
    LatLng pickupLatLng;
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
                    pickupLatLng = new LatLng(locationLat,locationLng);

                  pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("PickUp Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.person_pin_circle)));
                  getRouteToMarker(pickupLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()), pickupLatLng)
                .key(Apikey)
                .build();
        routing.execute();
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


        if(getApplicationContext()!=null)
        {

            if(aSwitch.isChecked() &&  mAuth.getCurrentUser()!=null)
            {
                if(!customerId.equals(""))
                {
                    rideDistance += lastLocation.distanceTo(location);
                }
                lastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.setMinZoomPreference(6.0f);
                mMap.setMaxZoomPreference(20.0f);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17));



                String userId = mAuth.getCurrentUser().getUid();
                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriversWorking");

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Current_Location");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(mAuth.getCurrentUser().getUid(),new GeoLocation(location.getLatitude(),location.getLongitude()));

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


                if(mAuth.getCurrentUser()!= null)
                {
                    String userId = mAuth.getCurrentUser().getUid();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    geoFireAvailable.removeLocation(userId);
                }


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

    private void connectDriver()
    {
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

    private void disconnectDriver()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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