package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    private ViewPager screenPager;
    ViewPagerAdapter viewPagerAdapter;
    TabLayout tabLayout;
    Button nxtBtn;
    int position = 0;
    FirebaseAuth firebaseAuth;
    Animation btnAnim;
    FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
                    Dexter.withContext(getApplicationContext())
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            Toast.makeText(SplashScreenActivity.this, "Have to give location permission", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                            permissionToken.continuePermissionRequest();

                        }
                    }).check();

        if (restorePrefData() && firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }

        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

        tabLayout = findViewById(R.id.tabLayout);
        nxtBtn = findViewById(R.id.confirmBtn);
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.btn_anim);

        List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem("Ride", "Find affordable, fast and safe rides"));
        mList.add(new ScreenItem("Ride Anywhere", "you can use this app to look for nearby rides to reach\n your destination"));
        mList.add(new ScreenItem("Ride now", "Lorem Ipsum is simply dummy text of the printing and typesetting industry."));

        screenPager = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this, mList);
        screenPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(screenPager);

        nxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                position = screenPager.getCurrentItem();
                if (position < mList.size()) {
                    position++;
                    screenPager.setCurrentItem(position);
                }

                if (position == mList.size() - 1) {
                    loadLastScreen();

                }
                if (position == mList.size()) {
                    savePrefsData();
                    startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
                    finish();
                }
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == mList.size() - 1) {
                    nxtBtn.setText("Get Started");
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void loadLastScreen() {

        nxtBtn.setText("Get Started");
        nxtBtn.setAnimation(btnAnim);
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myprefs", MODE_PRIVATE);
        Boolean isIntroOpenBefore = pref.getBoolean("isIntroOpen", false);
        return isIntroOpenBefore;
    }

    private void savePrefsData() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("myprefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpen", true);
        editor.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(SplashScreenActivity.this,HomeActivity.class));
            finish();
        }
    }



}