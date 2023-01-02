package com.example.ride;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class NidLicenseActivity extends AppCompatActivity {

    private ImageView img;
    private TextView textView,details;
    private Button capture,save;
    static  final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap thumbnail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nid_license);
        img = findViewById(R.id.image_view);
        textView = findViewById(R.id.textId);
        details = findViewById(R.id.details);
        capture = findViewById(R.id.capture);
        save = findViewById(R.id.save);



        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {/* ... */}
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("Please take a photo of your NID");
                captureImage();

            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                detectNIdLicenseText();
            }
        });


    }
    int cnt=0,cnt2=0;
    private void detectNIdLicenseText() {

        InputImage image = InputImage.fromBitmap(thumbnail,0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {


                String blockText = "";
                for(Text.TextBlock block : text.getTextBlocks())
                {
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for(Text.Line  line : block.getLines())
                    {
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for(Text.Element element: line.getElements())
                        {
                            String elementTExt = element.getText();
                            blockText += elementTExt;
                            blockText += " ";

                        }

                        blockText += "\n";

                    }
                }


                List<String>nid = new ArrayList<>();
                List<String>license = new ArrayList<>();

                nid.add("Government of the People's Republic of Bangladesh");
                nid.add("Government of the People's");
                nid.add("Republic");
                nid.add("National ID Card");
                nid.add("National ID");
                nid.add("National");

                license.add("Bangladesh Road Transport Authority");
                license.add("Bangladesh Road");
                license.add("Road Transport");
                license.add("Non-professional");


                for(String x : nid)
                {
                    int i = blockText.indexOf(x);

                    if(i>0)
                    {
                        cnt++;
                    }
                }

                for(String x : license)
                {
                    int i = blockText.indexOf(x);

                    if(i>0)
                    {
                        cnt2++;
                    }
                }


               if(cnt>0)
               {
                   textView.setText("Nid Card Detected : Pass first verification");
               }
                if(cnt2>0)
                {
                    textView.setText("License Card Detected : Pass first verification");

                }

                details.setText("Details: \n"+blockText);
                details.setMovementMethod(new ScrollingMovementMethod());

                if(cnt!=0 | cnt2!=0)
                {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent = new Intent(NidLicenseActivity.this,OtpVerficationActivity.class);
                            intent.putExtra("number", getIntent().getStringExtra("number"));
                            intent.putExtra("email", getIntent().getStringExtra("email"));
                            intent.putExtra("name", getIntent().getStringExtra("name"));
                            intent.putExtra("nid", getIntent().getStringExtra("nid"));
                            intent.putExtra("type", getIntent().getStringExtra("type"));
                            intent.putExtra("dob", getIntent().getStringExtra("dob"));
                            intent.putExtra("addr", getIntent().getStringExtra("addr"));
                            intent.putExtra("pass", getIntent().getStringExtra("pass"));

                            Bitmap thumbnail = (Bitmap) data_x.getExtras().get("data");
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            thumbnail.compress(Bitmap.CompressFormat.JPEG,90,bytes);
                            byte bb[] = bytes.toByteArray();

                            intent.putExtra("img", bb);
                            startActivity(intent);
                            finish();

                        }

                    }, 5*1000);

                }
                else
                {
                    if(cnt == 0 && cnt2 == 0)
                    {
                        Toast.makeText(NidLicenseActivity.this, "Please capture photo again", Toast.LENGTH_SHORT).show();
                    }
                    if(cnt==0)
                    {
                        Toast.makeText(NidLicenseActivity.this, "Please capture your NID photo", Toast.LENGTH_SHORT).show();
                    }
                    else if(cnt2 == 0)
                    {
                        Toast.makeText(NidLicenseActivity.this, "Please capture your License photo", Toast.LENGTH_SHORT).show();

                    }

                }

            }



        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NidLicenseActivity.this, "Failed to detect text from image", Toast.LENGTH_SHORT).show();
            }
        });


    }
    private void captureImage()
    {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(takePicture,REQUEST_IMAGE_CAPTURE);
        }
    }

    Intent data_x;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        data_x = data;
        onCaptureImageResult(data);

    }

    private void onCaptureImageResult(Intent data) {

        thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG,90,bytes);
        byte bb[] = bytes.toByteArray();
        img.setImageBitmap(thumbnail);

    }


}