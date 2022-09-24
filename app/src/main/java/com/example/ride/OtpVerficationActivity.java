package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OtpVerficationActivity extends AppCompatActivity {

    private String verificationId;
    private FirebaseAuth fAuth;
    ProgressBar progressBar;
    String email,pass;
    FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("OTP Verification");
        setContentView(R.layout.activity_otp_verfication);

        String phoneNumber = getIntent().getStringExtra("number");
        email = getIntent().getStringExtra("email");
        pass = getIntent().getStringExtra("pass");


        EditText code = findViewById(R.id.code);
        Button confirmBtn = findViewById(R.id.confirmBtn);
        progressBar = findViewById(R.id.progressBar);

        sendVerificationCode(phoneNumber);


        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String c = code.getText().toString().trim();
                if(c.isEmpty()||c.length()<6)
                {
                    code.setError("Enter code!");
                    code.requestFocus();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(c);


            }
        });
        

    }

    private void verifyCode(String code)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signInWithCredential(credential);

    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {

                            fAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful())
                                    {
                                        String userId = fAuth.getCurrentUser().getEmail();

                                        DocumentReference documentReference = firestore.collection("users").document(userId);
                                        Map<String,Object> user = new HashMap<>();

                                        user.put("name",getIntent().getStringExtra("name"));
                                        user.put("email",getIntent().getStringExtra("email"));
                                        user.put("phone",getIntent().getStringExtra("number"));
                                        user.put("nid",getIntent().getStringExtra("nid"));
                                        user.put("type",getIntent().getStringExtra("type"));
                                        user.put("dob",getIntent().getStringExtra("dob"));
                                        user.put("addr",getIntent().getStringExtra("addr"));



                                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                fAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                                        Intent intent = new Intent(OtpVerficationActivity.this,MainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();

                                                    }
                                                });

                                                Toast.makeText(OtpVerficationActivity.this, "data saved & user created", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }

                                }
                            });





                        }
                        else
                        {
                            Toast.makeText(OtpVerficationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void sendVerificationCode(String number)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallBack
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationId = s;

        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
            if(code!=null)
            {
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);

            }

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            Toast.makeText(OtpVerficationActivity.this, ""+e, Toast.LENGTH_SHORT).show();
        }
    };

}