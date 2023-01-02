package com.example.ride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TotalEarningAmountActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextView customerPaid,driverPaid,totalAmount,dateText;
    private DatabaseReference database;
    int cPaid = 0,dPaid = 0,totalPaid = 0;
    private Button date,search,reset,unPaidDriverList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_earning_amount);


        customerPaid = findViewById(R.id.customerPaidAmountId);
        driverPaid = findViewById(R.id.driverPaidAmountId);
        totalAmount = findViewById(R.id.totalEarnedAmountId);

        date = findViewById(R.id.pickAdateId);
        search = findViewById(R.id.searchId);
        reset = findViewById(R.id.resetId);
        dateText = findViewById(R.id.dateText);
        unPaidDriverList = findViewById(R.id.unPaidDriverListId);


        getAmountInfo();


        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new com.example.ride.DatePicker();
                datePicker.show(getSupportFragmentManager(),"date picker");
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(x!=null && y!=null)
                {
                    searchBetweenRange(x,y);
                }
                else
                {
                    Toast.makeText(TotalEarningAmountActivity.this,"Please select a date",Toast.LENGTH_SHORT).show();
                }

            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAmountInfo();
            }
        });

        unPaidDriverList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(TotalEarningAmountActivity.this,CustomerManageActivity.class);
                intent.putExtra("unpaid","unpaid");
                intent.putExtra("user","Driver");
                startActivity(intent);

            }
        });

    }

    private void getAmountInfo() {

        cPaid = 0;
        dPaid = 0;

        String cDate = DateFormat.getDateInstance(DateFormat.FULL).format(new Date());
        dateText.setText(""+cDate);

        database = FirebaseDatabase.getInstance().getReference().child("History");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot ds: snapshot.getChildren())
                    {

                        if (ds.child("customerPaid").getValue() != null)
                        {
                            if (ds.child("rideDistance").getValue() !=null) {
                                String  distance = ds.child("rideDistance").getValue().toString();
                                int  ridePrice = (int)((Double.valueOf(distance)/1000)*22);
                                cPaid += ridePrice;
                            }
                        }
                        if (ds.child("customerPaid").getValue() != null && ds.child("driverPaidOut").getValue() != null)
                        {
                            if (ds.child("rideDistance").getValue() !=null) {
                                String  distance = ds.child("rideDistance").getValue().toString();
                                int  ridePrice = (int)((Double.valueOf(distance)/1000)*22);
                                dPaid += ridePrice;
                            }
                        }

                    }

                    customerPaid.setText(String.valueOf((int)cPaid));
                    driverPaid.setText(String.valueOf((int)(dPaid * 0.1)));
                    totalAmount.setText(String.valueOf((int)(cPaid-dPaid) * 0.1));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void searchBetweenRange(Date p, Date q) {


        cPaid = 0;
        dPaid = 0;

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("History");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot ds: snapshot.getChildren())
                    {


                        if(Integer.parseInt(ds.child("timestamp").getValue().toString()) >= new Timestamp(p).getSeconds() && Integer.parseInt(ds.child("timestamp").getValue().toString()) <= new Timestamp(q).getSeconds())
                        {
                         //   Toast.makeText(TotalEarningAmountActivity.this,"Hi: "+Integer.parseInt(ds.child("timestamp").getValue().toString()),Toast.LENGTH_SHORT).show();


                            if (ds.child("customerPaid").getValue() != null)
                            {
                                if (ds.child("rideDistance").getValue() !=null) {
                                    String  distance = ds.child("rideDistance").getValue().toString();
                                    int  ridePrice = (int)((Double.valueOf(distance)/1000)*22);
                                    cPaid += ridePrice;
                                }
                            }
                            if (ds.child("customerPaid").getValue() != null && ds.child("driverPaidOut").getValue() != null)
                            {
                                if (ds.child("rideDistance").getValue() !=null) {
                                    String  distance = ds.child("rideDistance").getValue().toString();
                                    int  ridePrice = (int)((Double.valueOf(distance)/1000)*22);
                                    dPaid += ridePrice;
                                }
                            }
                        }


                    }

                    customerPaid.setText(""+(int)cPaid);
                    driverPaid.setText(""+(int)(dPaid * 0.1));
                    totalAmount.setText(""+(int)((cPaid*.1)-(dPaid * 0.1)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private Date x,y;

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,i);
        c.set(Calendar.MONTH,i1);
        c.set(Calendar.DAY_OF_MONTH,i2);

        String cDate = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
        dateText.setText(""+cDate);

        x = atStartOfDay(c.getTime());
        y = atEndOfDay(c.getTime());

    }
    public Date atEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public Date atStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}