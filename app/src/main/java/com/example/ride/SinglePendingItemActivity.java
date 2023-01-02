package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SinglePendingItemActivity extends AppCompatActivity {

    TextView name,email,phone,userId,dob,nidOrLicense;
    Button acceptBtn;
    ImageView img;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_pending_item);


        name = findViewById(R.id.penName);
        email = findViewById(R.id.penEmail);
        phone = findViewById(R.id.penPhone);
        userId = findViewById(R.id.penUserId);
        dob = findViewById(R.id.pDob);
        nidOrLicense = findViewById(R.id.nidOrLicense);
        img = findViewById(R.id.imageView);

        acceptBtn = findViewById(R.id.acceptId);

        String pName,pEmail,pPhone,pUserId,pDob,imgUrl,pNidOrLicense;

        pName = getIntent().getStringExtra("name");
        pEmail = getIntent().getStringExtra("email");
        pPhone = getIntent().getStringExtra("phone");
        pUserId = getIntent().getStringExtra("userId");
        pDob = getIntent().getStringExtra("dob");
        pNidOrLicense = getIntent().getStringExtra("nidOrLicense");
        imgUrl = getIntent().getStringExtra("imgUrl");
        type = getIntent().getStringExtra("type");

        name.setText("Name: "+pName);
        email.setText("Email: "+pEmail);
        phone.setText("Phone: "+pPhone);
        userId.setText("UserId: "+pUserId);
        dob.setText("Date of Birth: "+pDob);
        nidOrLicense.setText("Nid or License: "+pNidOrLicense);


        Glide.with(getApplicationContext())
                .load(imgUrl)
                .into(img);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("VerifyNidLicense").child(type).child(pUserId);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            Map userInfo = new HashMap();
                            userInfo.put("verify",true);
                            databaseReference.updateChildren(userInfo);

                            Intent intent = new Intent(SinglePendingItemActivity.this,PendingListActivity.class);
                            intent.putExtra("type",type);
                            startActivity(intent);
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SinglePendingItemActivity.this,PendingListActivity.class);
        intent.putExtra("type",type);
        startActivity(intent);
        finish();
    }
}