package com.example.ride;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class HomeActivity extends AppCompatActivity {

    Button driver;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        driver = findViewById(R.id.driverBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(HomeActivity.this);

                dialog.setContentView(R.layout.loader);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loader_dialog));
                }
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(false);
                dialog.show();
                check_user();
            }
        });



    }
        private void check_user() {

        DocumentReference documentReference = fstore.collection("users").document(firebaseAuth.getCurrentUser().getEmail());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {


                    if (documentSnapshot.getString("type").equals("Traveller")) {
                        dialog.dismiss();
                        startActivity(new Intent(HomeActivity.this,CustomerMapActivity.class));
                        finish();
                    } else if (documentSnapshot.getString("type").equals("Driver")) {
                        dialog.dismiss();
                        startActivity(new Intent(HomeActivity.this, DriverMapsActivity.class));
                        finish();

                    }
                }
            }
        });

    }
}