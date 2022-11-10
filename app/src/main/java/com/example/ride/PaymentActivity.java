package com.example.ride;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    Button btn;
    TextView payText;
    String price;
    String userId,userEmail,userPhone;
    String rideId;
    DatabaseReference historyRideRef;
    int rPrice = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Checkout.preload(getApplicationContext());

        payText = findViewById(R.id.textView2);
        btn = findViewById(R.id.button);

        price = getIntent().getStringExtra("rPrice");
        Toast.makeText(PaymentActivity.this,""+price,Toast.LENGTH_SHORT).show();
        payText.setText(price+" TK");

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        rideId = getIntent().getExtras().getString("rideId");
        if(rideId != null)
        {
            historyRideRef = FirebaseDatabase.getInstance().getReference().child("History").child(rideId);
        }

        rPrice = Integer.parseInt(price);

        Toast.makeText(PaymentActivity.this,"PRice: "+rPrice,Toast.LENGTH_SHORT).show();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePayment();
            }
        });


    }

    private void makePayment() {

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_LoUwoBY9TkYgaw");

        checkout.setImage(R.drawable.payment_icon);

        final Activity activity = this;


        try {
            JSONObject options = new JSONObject();

            options.put("name", "Merchant Name");
         //   options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg");
           // options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            Toast.makeText(PaymentActivity.this,""+price,Toast.LENGTH_SHORT).show();
            options.put("amount", rPrice*100);//pass amount in currency subunits
            options.put("prefill.email", userEmail);
            options.put("prefill.contact",userPhone);
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);


        } catch(Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }
    }


    @Override
    public void onPaymentSuccess(String s)
    {

        payText.setVisibility(View.GONE);
        btn.setVisibility(View.GONE);
        if(rideId != null)
        {
            historyRideRef.child("customerPaid").setValue(true);

        }
        else
        {
            historyRideRef = FirebaseDatabase.getInstance().getReference().child("History");

            historyRideRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists() && snapshot.getChildrenCount()>0)
                    {
                        for(DataSnapshot ds : snapshot.getChildren())
                        {
                            String key = ds.getKey();
                            if( historyRideRef.child(key).child("customerPaid") != null)
                            {
                                historyRideRef.child(key).child("driverPaidOut").setValue(true);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }



//        if(getIntent().getStringExtra("user") != null && getIntent().getStringExtra("user").equals("Driver"))
//        {
//            historyRideRef.child("driverPaidOut").setValue(true);
//        }
        if(getIntent().getStringExtra("user").equals("Driver")) {

            startActivity(new Intent(PaymentActivity.this,DriverMapsActivity.class));
            finish();
        }
        else
        {
            startActivity(new Intent(PaymentActivity.this, CustomerMapActivity.class));
            finish();
        }


    }

    @Override
    public void onPaymentError(int i, String s)
    {
        payText.setVisibility(View.GONE);
        if(getIntent().getStringExtra("user").equals("Driver")) {
            Toast.makeText(PaymentActivity.this, "Error: " + s, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PaymentActivity.this, DriverMapsActivity.class));
            finish();
        }
        else
        {
            Toast.makeText(PaymentActivity.this, "Error: " + s, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PaymentActivity.this, CustomerMapActivity.class));
            finish();
        }
    }
}