package com.example.ride;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    Button letsGo;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fstore;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        letsGo = findViewById(R.id.letsGoBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        letsGo.setOnClickListener(new View.OnClickListener() {
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


                    if (documentSnapshot.getString("type").equals("Passenger")) {
                        dialog.dismiss();
                        startActivity(new Intent(HomeActivity.this,CustomerMapActivity.class));
                        finish();
                    } else if (documentSnapshot.getString("type").equals("Driver")) {
                        dialog.dismiss();
                        startActivity(new Intent(HomeActivity.this, DriverMapsActivity.class));
                        finish();

                    }
                    else if (documentSnapshot.getString("type").equals("admin")) {
                        dialog.dismiss();
                        startActivity(new Intent(HomeActivity.this, AdminHomeActivity.class));
                        finish();
                    }
                }
            }
        });

    }
}