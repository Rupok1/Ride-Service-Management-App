package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEdit,passwordEdit;
    private TextView forgotPass,signUp;
    Button loginBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Login");
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        emailEdit = findViewById(R.id.loginEmail);
        passwordEdit = findViewById(R.id.loginPassword);
        forgotPass = findViewById(R.id.forgotPass);
        signUp = findViewById(R.id.signUpText);
        loginBtn = findViewById(R.id.loginBtn);


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,CreateAccountActivity.class));
                finish();
            }
        });
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passResetDiag = new AlertDialog.Builder(v.getContext());
                passResetDiag.setTitle("Reset password?");
                passResetDiag.setMessage("Enter your email to get reset link");
                passResetDiag.setView(resetMail);

                passResetDiag.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String mail = resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(SignInActivity.this, "Link Send", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignInActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                passResetDiag.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                passResetDiag.create().show();
            }
        });
        if(mAuth.getCurrentUser()!=null)
        {
           startActivity(new Intent(SignInActivity.this,HomeActivity.class));
           finish();
        }


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 String email = emailEdit.getText().toString();
                String pass = passwordEdit.getText().toString();

                if(TextUtils.isEmpty(email))
                {
                    emailEdit.setError("Required!");
                    emailEdit.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(pass) && pass.length()<6)
                {
                    passwordEdit.setError("Required!");
                    passwordEdit.requestFocus();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {
                          startActivity(new Intent(SignInActivity.this,HomeActivity.class));
                          finish();
                        }
                        else
                        {
                            Toast.makeText(SignInActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });



            }
        });





    }
//    private void check_user() {
//
//        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
//        DocumentReference documentReference = fstore.collection("users").document(mAuth.getCurrentUser().getEmail());
//        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                if (documentSnapshot.exists()) {
//
//
//                    if (documentSnapshot.getString("type").equals("Traveller")) {
//                       startActivity(new Intent(SignInActivity.this,CustomerMapActivity.class));
//                        finish();
//                    } else if (documentSnapshot.getString("type").equals("Driver")) {
//                        startActivity(new Intent(SignInActivity.this, DriverMapsActivity.class));
//                        finish();
//
//                    }
//                }
//            }
//        });
//
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}