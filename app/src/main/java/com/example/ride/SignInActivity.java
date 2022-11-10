package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

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
                            dialog = new Dialog(SignInActivity.this);

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
                        else
                        {
                            Toast.makeText(SignInActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });



            }
        });




    }
    private void check_user() {

       FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
       FirebaseFirestore fstore = FirebaseFirestore.getInstance();

        DocumentReference documentReference = fstore.collection("users").document(firebaseAuth.getCurrentUser().getEmail());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {


                    if (documentSnapshot.getString("type").equals("Traveller")) {
                        dialog.dismiss();
                        startActivity(new Intent(SignInActivity.this,CustomerMapActivity.class));
                        finish();

                    } else if (documentSnapshot.getString("type").equals("Driver")) {
                        dialog.dismiss();
                        startActivity(new Intent(SignInActivity.this, DriverMapsActivity.class));
                        finish();

                    }
                    else if (documentSnapshot.getString("type").equals("admin")) {
                        dialog.dismiss();
                        startActivity(new Intent(SignInActivity.this, AdminHomeActivity.class));
                        finish();
                    }
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}