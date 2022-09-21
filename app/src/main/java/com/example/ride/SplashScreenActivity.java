package com.example.ride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    private ViewPager screenPager;
    ViewPagerAdapter viewPagerAdapter;
    TabLayout tabLayout;
    Button nxtBtn;
    int position = 0;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();

        if(restorePrefData() && firebaseAuth.getCurrentUser()==null)
        {
            startActivity(new Intent(getApplicationContext(),CreateAccountActivity.class));
            finish();
        }

        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

        tabLayout = findViewById(R.id.tabLayout);
        nxtBtn = findViewById(R.id.confirmBtn);

        List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem("Lorem Ipsum is simply dummy text of the printing and typesetting industry."));
        mList.add(new ScreenItem("Lorem Ipsum is simply dummy text of the printing and typesetting industry."));
        mList.add(new ScreenItem("Lorem Ipsum is simply dummy text of the printing and typesetting industry."));

        screenPager = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this,mList);
        screenPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(screenPager);

        nxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                position = screenPager.getCurrentItem();
                if(position<mList.size())
                {
                    position++;
                    screenPager.setCurrentItem(position);
                }

                if(position == mList.size()-1)
                {
                   nxtBtn.setText("Get Started");
                }
                if(position == mList.size())
                {
                    savePrefsData();
                    startActivity(new Intent(SplashScreenActivity.this,CreateAccountActivity.class));
                    finish();
                }
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if(tab.getPosition() == mList.size()-1)
                {
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

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myprefs",MODE_PRIVATE);
        Boolean isIntroOpenBefore = pref.getBoolean("isIntroOpen",false);
        return isIntroOpenBefore;
    }

    private void savePrefsData() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("myprefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpen",true);
        editor.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null)
        {
            startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
            finish();
        }
    }
}