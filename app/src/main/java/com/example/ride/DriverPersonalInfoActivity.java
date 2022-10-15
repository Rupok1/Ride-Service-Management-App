package com.example.ride;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.ride.databinding.ActivityDriverPersonalInfoBinding;

public class DriverPersonalInfoActivity extends MainActivity {

    private ActivityDriverPersonalInfoBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverPersonalInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toast.makeText(this, "Driver Personal", Toast.LENGTH_SHORT).show();
    }
}