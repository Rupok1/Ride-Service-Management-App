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
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    Button btn;
    TextView payText;
    String price;
    String userId,userEmail,userPhone;
    String rideId,driverEmail;
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

        payText.setText(price+" TK");

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        rideId = getIntent().getExtras().getString("rideId");
        driverEmail = getIntent().getExtras().getString("driverEmail");
        if(rideId != null)
        {
            historyRideRef = FirebaseDatabase.getInstance().getReference().child("History").child(rideId);
        }

        rPrice = Integer.parseInt(price);

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

            String email = "rideapp1807000306@gmail.com";
            String subj = "Payment from Driver";
            String msg = "Driver Id: "+FirebaseAuth.getInstance().getUid()+"\nDriver Email: "+driverEmail+"\n Paid Amount: "+rPrice+" TK";

            sendEmailToAdmin(email,msg,subj);

            startActivity(new Intent(PaymentActivity.this,DriverMapsActivity.class));
            finish();
        }
        else
        {

            String subj = "Payment from Passenger";
            String msg = "Passenger Id: "+FirebaseAuth.getInstance().getUid()+"\n Paid Amount: "+rPrice+" tk";


            sendEmail(driverEmail,msg,subj);

            startActivity(new Intent(PaymentActivity.this, CustomerMapActivity.class));
            finish();
        }


    }

    private void sendEmailToAdmin(String email, String msg, String subj) {
        String username = "rupokhasan789@gmail.com";
        String password = "mqrsatvefpojrgmg";

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

    private void sendEmail(String email, String msg, String subj) {

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