package com.example.ride;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.transition.Slide;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {


    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
//    Switch aSwitch;
//    Button call_a_car;
    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_main,null);

        FrameLayout container = drawerLayout.findViewById(R.id.activity_container);
        container.addView(view);
        super.setContentView(drawerLayout);

        NavigationView navigationView = drawerLayout.findViewById(R.id.nav_view);
//        aSwitch = findViewById(R.id.switchID);
//        call_a_car = findViewById(R.id.call_A_car);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();


        String userId = mAuth.getCurrentUser().getEmail();
        Toast.makeText(this, "" + userId, Toast.LENGTH_SHORT).show();
        DocumentReference documentReference = fstore.collection("users").document(userId);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {


                    TextView name = navigationView.findViewById(R.id.username);
                    TextView type = navigationView.findViewById(R.id.prof_type);
                    TextView email = navigationView.findViewById(R.id.email);
                    CircleImageView image = navigationView.findViewById(R.id.nav_image);

                    name.setText(documentSnapshot.getString("name"));
                    type.setText(documentSnapshot.getString("type"));
                    email.setText(documentSnapshot.getString("email"));


                    if(documentSnapshot.getString("type").equals("Traveller"))
                    {
                        loadImage(image);
//                       call_a_car.setVisibility(View.VISIBLE);

                        Toast.makeText(MainActivity.this, "Traveller", Toast.LENGTH_SHORT).show();

                        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                            @Override
                            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                                drawerLayout.closeDrawer(GravityCompat.START);
                                switch (item.getItemId())
                                {
                                    case R.id.menu_home:
                                        startActivity(new Intent(MainActivity.this,CustomerMapActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                        overridePendingTransition(0,0);
                                        break;
                                    case R.id.per_info:
                                        Intent intent = new Intent(MainActivity.this,CustomerPersonalInfoActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        intent.putExtra("mobile",documentSnapshot.getString("phone"));
                                        startActivity(intent);
                                        overridePendingTransition(0,0);
                                        break;
                                    case R.id.history:
                                        Intent intent2 = new Intent(MainActivity.this,CustomerHistoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        intent2.putExtra("user","Customers");
                                        startActivity(intent2);
                                        overridePendingTransition(0,0);
                                        break;
                                    case R.id.logout:
                                        String userId = mAuth.getCurrentUser().getUid();
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                                        GeoFire geoFire = new GeoFire(ref);
                                        geoFire.removeLocation(userId);

                                        mAuth.signOut();
                                        startActivity(new Intent(MainActivity.this,SignInActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                        overridePendingTransition(0,0);
                                        finish();
                                        break;

                                }
                                return false;
                            }
                        });
                    }
                    else if(documentSnapshot.getString("type").equals("Driver"))
                    {
                        loadImageForDriver(image);
//                        aSwitch.setVisibility(View.VISIBLE);
//                        call_a_car.setVisibility(View.GONE);
                      //  Toast.makeText(MainActivity.this, "Driver", Toast.LENGTH_SHORT).show();
                        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                            @Override
                            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                                drawerLayout.closeDrawer(GravityCompat.START);
                                switch (item.getItemId())
                                {
                                    case R.id.menu_home:

                                        startActivity(new Intent(MainActivity.this,DriverMapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                        overridePendingTransition(0,0);
                                        break;
                                    case R.id.per_info:
                                        startActivity(new Intent(MainActivity.this,DriverProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                        overridePendingTransition(0,0);
                                        break;

                                    case R.id.logout:
                                        String userId = mAuth.getCurrentUser().getUid();
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                                        GeoFire geoFire = new GeoFire(ref);
                                        geoFire.removeLocation(userId);

                                        mAuth.signOut();
                                        startActivity(new Intent(MainActivity.this,SignInActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                        overridePendingTransition(0,0);
                                        finish();
                                        break;


                                }
                                return false;
                            }
                        });
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    private void loadImageForDriver(CircleImageView image)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(mAuth.getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if(snapshot.exists())
                {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("profileImageUrl")!=null)
                    {
                        String profileImgUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplicationContext())
                                .load(profileImgUrl)
                                .into(image);

                    }

                }
                else
                {
                    return;
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadImage(CircleImageView image)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(mAuth.getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if(snapshot.exists())
                {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("profileImageUrl")!=null)
                    {
                        String profileImgUrl = map.get("profileImageUrl").toString();
                        Glide.with(MainActivity.this)
                                .load(profileImgUrl)
                                .into(image);

                    }

                }
                else
                {
                    return;
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
//         navigationView = findViewById(R.id.nav_menu);
//         drawerLayout = findViewById(R.id.drawer);
//
//        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
//        FrameLayout container = drawerLayout.findViewById(R.id.activity_container);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//
//
//
//
//        mAuth = FirebaseAuth.getInstance();
//        fstore = FirebaseFirestore.getInstance();
//
//        String userId = mAuth.getCurrentUser().getEmail();
//        Toast.makeText(this, "" + userId, Toast.LENGTH_SHORT).show();
//        DocumentReference documentReference = fstore.collection("users").document(userId);
//
//
//        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                if (documentSnapshot.exists()) {
//
//
//                    TextView name = navigationView.findViewById(R.id.username);
//                    TextView type = navigationView.findViewById(R.id.prof_type);
//                    TextView email = navigationView.findViewById(R.id.email);
//
//                    name.setText(documentSnapshot.getString("name"));
//                    type.setText(documentSnapshot.getString("type"));
//                    email.setText(documentSnapshot.getString("email"));
//                    //Toast.makeText(MainActivity.this, ""+documentSnapshot.getString("name"), Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//
//        smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
//        client = LocationServices.getFusedLocationProviderClient(this);
//
//        Dexter.withContext(getApplicationContext())
//                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                .withListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
//
//                        getMyLocation();
//
//                    }
//
//                    @Override
//                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
//
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
//
//                        permissionToken.continuePermissionRequest();
//
//                    }
//                }).check();
//
//
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//
//                switch (item.getItemId()) {
//                    case R.id.menu_home:
//                        startActivity(new Intent(MainActivity.this,DriverMapsActivity.class));
//                        drawerLayout.closeDrawer(GravityCompat.START);
//                        break;
//                    case R.id.history:
//                        Toast.makeText(MainActivity.this, "history", Toast.LENGTH_SHORT).show();
//                        drawerLayout.closeDrawer(GravityCompat.START);
//                        break;
//                    case R.id.payment:
//                        Toast.makeText(MainActivity.this, "payment", Toast.LENGTH_SHORT).show();
//                        drawerLayout.closeDrawer(GravityCompat.START);
//                        break;
//                    case R.id.per_info:
//                        Toast.makeText(MainActivity.this, "personal info", Toast.LENGTH_SHORT).show();
//                        drawerLayout.closeDrawer(GravityCompat.START);
//                        break;
//                    case R.id.logout:
//
//                        mAuth.signOut();
//                        startActivity(new Intent(MainActivity.this,SignInActivity.class));
//                        finish();
//                       // drawerLayout.closeDrawer(GravityCompat.START);
//                        break;
//                }
//
//                return true;
//            }
//        });
//
//
//    }

//    private void getMyLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Task<Location> task = client.getLastLocation();
//        task.addOnSuccessListener(new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//
//                smf.getMapAsync(new OnMapReadyCallback() {
//                    @Override
//                    public void onMapReady(GoogleMap googleMap) {
//                        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
//
//                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("My Location");
//                        googleMap.addMarker(markerOptions);
//                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
//                    }
//                });
//
//            }
//        });
//
//    }


}