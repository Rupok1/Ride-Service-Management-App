package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ride.databinding.ActivityCustomerMapBinding;
import com.example.ride.databinding.ActivitySosactivityBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class SOSActivity extends MainActivity {

    ImageView sos,sos_by_email;
    ActivitySosactivityBinding binding;
    Button sosSettingBtn;
    Dialog dialog;
    DatabaseReference ref;
    EditText number,sc_number,sendEmail;
    FirebaseAuth mAuth;
    String user;
    String message,mail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySosactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sos = findViewById(R.id.sos_img);
        sos_by_email = findViewById(R.id.sos_img_by_email);
        sosSettingBtn = findViewById(R.id.sos_setting);

        mAuth = FirebaseAuth.getInstance();

        user = getIntent().getStringExtra("user");

        sosSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadDialog();

            }
        });

        sos_by_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SOS").child(user).child(mAuth.getCurrentUser().getUid());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {


                            Map<String,Object> mp = (Map<String, Object>) snapshot.getValue();

                            ArrayList<String>number = new ArrayList<>();
                            number.add(mp.get("1").toString());
                            number.add(mp.get("2").toString());

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Current_Location").child(mAuth.getCurrentUser().getUid()).child("l");

                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists())
                                    {
                                        List<Object> map = (List<Object>) snapshot.getValue();
                                        double locationLat = 0;
                                        double locationLng = 0;

                                        if(map.get(0) != null)
                                        {
                                            locationLat = Double.parseDouble(map.get(0).toString()) ;
                                        }
                                        if(map.get(1) != null)
                                        {
                                            locationLng = Double.parseDouble(map.get(1).toString()) ;
                                        }
                                        LatLng currentLatLng = new LatLng(locationLat,locationLng);



                                        message = mp.get("message").toString();
                                        mail = mp.get("email").toString();

                                        message = message + "\nMy current location: " + currentLatLng;

                                        sendEmail(mail,message,"SOS:");

                                        String x = message + "\nMy current location: " + currentLatLng+"\n Email: "+mAuth.getCurrentUser().getEmail();

                                        sendEmailToAdmin(x,"SOS: "+FirebaseAuth.getInstance().getCurrentUser().getUid());

                                        Toast.makeText(SOSActivity.this,"Email send successfully!!",Toast.LENGTH_SHORT).show();

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });






                        }
                        else
                        {
                            Toast.makeText(SOSActivity.this,"Go to SOS setting first",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SOS").child(user).child(mAuth.getCurrentUser().getUid());

                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {


                                Map<String,Object> mp = (Map<String, Object>) snapshot.getValue();

                                ArrayList<String>number = new ArrayList<>();
                                number.add(mp.get("1").toString());
                                number.add(mp.get("2").toString());

                                message = mp.get("message").toString();
                                mail = mp.get("email").toString();

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Current_Location").child(mAuth.getCurrentUser().getUid()).child("l");

                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists())
                                        {
                                            List<Object> map = (List<Object>) snapshot.getValue();
                                            double locationLat = 0;
                                            double locationLng = 0;
                                            //  call_a_car.setText("Driver Location...");
                                            if(map.get(0) != null)
                                            {
                                                locationLat = Double.parseDouble(map.get(0).toString()) ;
                                            }
                                            if(map.get(1) != null)
                                            {
                                                locationLng = Double.parseDouble(map.get(1).toString()) ;
                                            }
                                            LatLng currentLatLng = new LatLng(locationLat,locationLng);

                                           // Toast.makeText(SOSActivity.this,"l: "+currentLatLng,Toast.LENGTH_SHORT).show();


                                            message = message + "\nMy current location: " + currentLatLng;



                                            try {
                                                for(int i=0;i<number.size();i++)
                                                {
                                                    SmsManager smsManager = SmsManager.getDefault();
                                                    smsManager.sendTextMessage(number.get(i),null,message,null,null);
                                                }
                                                Toast.makeText(SOSActivity.this,"Message send successfully",Toast.LENGTH_SHORT).show();

                                            }catch (Exception e)
                                            {
                                                Toast.makeText(SOSActivity.this,"Error: "+e,Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });






                            }
                            else
                            {
                                Toast.makeText(SOSActivity.this,"Go to SOS setting first",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });




            }
        });

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
    private void sendEmailToAdmin( String msg, String subj) {

        String email = "rideapp1807000306@gmail.com";
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
    private void loadDialog() {


        Dexter.withContext(this)
                .withPermission(Manifest.permission.SEND_SMS)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {/* ... */}
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();

        dialog = new Dialog(SOSActivity.this);
        dialog.setContentView(R.layout.sos_dialog_layout);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);


        dialog.show();

        ref = FirebaseDatabase.getInstance().getReference("SOS").child(user).child(mAuth.getCurrentUser().getUid());
        Button save = dialog.findViewById(R.id.saveSoSBtn);
        number = dialog.findViewById(R.id.contactNumber);
        sc_number = dialog.findViewById(R.id.secondContactId);
        sendEmail = dialog.findViewById(R.id.sendEmail);
        EditText messageEdit = dialog.findViewById(R.id.sos_message);

        if(ref != null)
        {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists())
                    {
                        Map<String,Object> mp = (Map<String, Object>) snapshot.getValue();

                          number.setText(mp.get("1").toString());
                          sc_number.setText(mp.get("2").toString());
                          messageEdit.setText(mp.get("message").toString());
                          sendEmail.setText(mp.get("email").toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(number.getText().toString().equals("")||sc_number.getText().toString().equals("")||messageEdit.getText().toString().equals(""))
                {
                    Toast.makeText(SOSActivity.this,"All input field must be field",Toast.LENGTH_SHORT).show();
                    return ;
                }
                else if(number.getText().toString().length()!=11||sc_number.getText().toString().length()!=11)
                {
                    Toast.makeText(SOSActivity.this,"Minimum number length 11",Toast.LENGTH_SHORT).show();
                    return ;
                }
                else if(sendEmail.getText().toString().equals(""))
                {
                    Toast.makeText(SOSActivity.this,"Required!!!",Toast.LENGTH_SHORT).show();
                    return ;
                }
                else
                {
                    String fNumber = number.getText().toString();
                    String sNumber = sc_number.getText().toString();
                    String message = messageEdit.getText().toString();
                    String email = sendEmail.getText().toString();

                    Map map = new HashMap<>();

                    map.put("1",fNumber);
                    map.put("2",sNumber);
                    map.put("message",message);
                    map.put("email",email);

                    ref.updateChildren(map);
                    dialog.dismiss();
                }


            }
        });



    }

}