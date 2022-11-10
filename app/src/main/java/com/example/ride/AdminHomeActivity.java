package com.example.ride;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class AdminHomeActivity extends AppCompatActivity {

    private Button customerList,customerBtn,driverList,pDriverList,signOutBtn;
    public final int REQUEST_CODE = 100;
    boolean canW,canR;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        customerList = findViewById(R.id.customerId);
        customerBtn = findViewById(R.id.customerListId);
        driverList = findViewById(R.id.allDriverList);
        pDriverList = findViewById(R.id.driverAvailableId);
        signOutBtn = findViewById(R.id.sign_outBtnId);

        fAuth = FirebaseAuth.getInstance();

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fAuth.signOut();
                startActivity(new Intent(AdminHomeActivity.this,SignInActivity.class));
                finish();
            }
        });



        customerList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {

                    createTravellerPDF();


                }
                else {
                    requestAllPermission();
                }
            }
        });
        pDriverList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {

                    createDriverPDF();

                }
                else {
                    requestAllPermission();
                }
            }
        });
        driverList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this,CustomerManageActivity.class);
                intent.putExtra("user","Driver");
                startActivity(intent);
            }
        });

        customerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this,CustomerManageActivity.class);
                intent.putExtra("user","Customer");
                startActivity(intent);
            }
        });
//        availableDriver.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(AdminHomeActivity.this,CustomerManageActivity.class);
//                intent.putExtra("user","availableDriver");
//                startActivity(intent);
//            }
//        });
//        driverInService.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(AdminHomeActivity.this,CustomerManageActivity.class);
//                intent.putExtra("user","driverInService");
//                startActivity(intent);
//            }
//        });



    }

    private void createDriverPDF() {

       ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
       File pdfPath = contextWrapper.getExternalFilesDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());

         File file = new File(pdfPath,"Driver.pdf");

        try {
            OutputStream outputStream = new FileOutputStream(file);

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);


//            Bitmap bitmap = ((BitmapDrawable)getDrawable(R.drawable.header_image)).getBitmap();
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
//            byte[] bitmapData = stream.toByteArray();
//
//            ImageData imageData = ImageDataFactory.create(bitmapData);
//            Image image = new Image(imageData);
//            image.setAutoScaleWidth(true);
//            document.add(image);


            String t = "Driver List";
            Paragraph title = new Paragraph(t);
            title.setTextAlignment(TextAlignment.CENTER);
            title.setFontSize(20);
            title.setBold();
            document.add(title);

            float columnWidth[] = {200f,200f};


            FirebaseFirestore fstore = FirebaseFirestore.getInstance();

            fstore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    Table table = new Table(columnWidth);
                    table.setWidth(500);

                    table.addCell("Name");
                    table.addCell("Mobile");

                    if(value != null)
                    {
                        for(DocumentChange dc : value.getDocumentChanges())
                        {

                            User user = dc.getDocument().toObject(User.class);

                            if(user.type.equals("Driver"))
                            {

                                table.addCell(user.getName());
                                table.addCell(user.getPhone());

                            }

                        }
                        document.add(table);
                        document.close();

                    }

                }
            });

            Toast.makeText(AdminHomeActivity.this,"Pdf created",Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void requestAllPermission() {

        ActivityCompat.requestPermissions(AdminHomeActivity.this, new String[]{READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    private void createTravellerPDF() {


        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File pdfPath = contextWrapper.getExternalFilesDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
        File file = new File(pdfPath,"Customer.pdf");

        try {

            OutputStream outputStream = new FileOutputStream(file);
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);


//            Bitmap bitmap = ((BitmapDrawable)getDrawable(R.drawable.header_image)).getBitmap();
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
//            byte[] bitmapData = stream.toByteArray();
//
//            ImageData imageData = ImageDataFactory.create(bitmapData);
//            Image image = new Image(imageData);
//            image.setAutoScaleWidth(true);
//            document.add(image);


            String t = "Customer List";
            Paragraph title = new Paragraph(t);
            title.setTextAlignment(TextAlignment.CENTER);
            title.setFontSize(20);
            title.setBold();
            document.add(title);

            float columnWidth[] = {200f,200f};



            FirebaseFirestore fstore = FirebaseFirestore.getInstance();

            fstore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    Table table = new Table(columnWidth);
                    table.setWidth(500);

                    table.addCell("Name");
                    table.addCell("Mobile");
                    for(DocumentChange dc : value.getDocumentChanges())
                    {

                        User user = dc.getDocument().toObject(User.class);

                        if(user.type.equals("Traveller"))
                        {

                            table.addCell(user.getName());
                            table.addCell(user.getPhone());
                        }

                    }
                    document.add(table);
                    document.close();
                }
            });


            Toast.makeText(AdminHomeActivity.this,"Pdf created",Toast.LENGTH_SHORT).show();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }





    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AdminHomeActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}