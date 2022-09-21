package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    String type;
    EditText nameEdit,emailEdit,nidEdit,dopEdit,addrEdit,phone;
    Button createBtn;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Create Account");
        setContentView(R.layout.activity_create_account);
        Spinner spinner = findViewById(R.id.spinner);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        ArrayAdapter<CharSequence>adapter = ArrayAdapter.createFromResource(this,R.array.type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                type = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        nameEdit = findViewById(R.id.nameId);
        emailEdit = findViewById(R.id.emailId);
        nidEdit = findViewById(R.id.nid);
        dopEdit = findViewById(R.id.date);
        createBtn = findViewById(R.id.createBtn);
        addrEdit = findViewById(R.id.addressId);
        phone = findViewById(R.id.phone);

//        createBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String name = nameEdit.getText().toString();
//                Toast.makeText(CreateAccountActivity.this, "name: "+name, Toast.LENGTH_SHORT).show();
//            }
//        });




        Calendar calendar = Calendar.getInstance();
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int month = calendar.get(Calendar.MONTH);
        final int year = calendar.get(Calendar.YEAR);
        final String[] date = new String[1];

        dopEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateAccountActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {

                                month = month+1;
                                date[0] = day + "/"+month+"/"+year;
                                dopEdit.setText(date[0]);
                            }
                        },year,month,day);
                datePickerDialog.show();
            }
        });




       firestore = FirebaseFirestore.getInstance();



        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameEdit.getText().toString().trim();
                String email = emailEdit.getText().toString().trim();
                String nid = nidEdit.getText().toString().trim();
                String p_no = phone.getText().toString().trim();


                String dob;
                if(TextUtils.isEmpty(date[0]))
                {
                    dob = dopEdit.getText().toString();
                }
                else
                {
                    dob = date[0];
                }
                String addr = addrEdit.getText().toString().trim();


                if(name.isEmpty())
                {
                    nameEdit.setError("Required!");
                    nameEdit.requestFocus();
                    return;
                }
                if(email.isEmpty())
                {
                    emailEdit.setError("Required!");
                    emailEdit.requestFocus();
                    return;
                }
                if(nid.isEmpty())
                {
                    nidEdit.setError("Required!");
                    nidEdit.requestFocus();
                    return;
                }
                if(dob.isEmpty())
                {
                    dopEdit.setError("Required!");
                    dopEdit.requestFocus();
                    return;
                }
                if(addr.isEmpty())
                {
                    addrEdit.setError("Required!");
                    addrEdit.requestFocus();
                    return;
                }
                if(p_no.isEmpty() && p_no.length()!=11)
                {
                    addrEdit.setError("Required!");
                    addrEdit.requestFocus();
                    return;
                }



                DocumentReference documentReference = firestore.collection("Users").document(p_no);
                Map<String,Object>user = new HashMap<>();

                user.put("name",name);
                user.put("email",email);
                user.put("phone",p_no);
                user.put("nid",nid);
                user.put("type",type);
                user.put("dob",dob);
                user.put("addr",addr);

                String finalP_no = p_no;
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(CreateAccountActivity.this, "data saved", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateAccountActivity.this,OtpVerficationActivity.class);
                        intent.putExtra("number", "+88"+p_no);
                        startActivity(intent);

                    }
                });

            }
        });


    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}