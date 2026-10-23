package com.example.condosched.ui;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.example.condosched.R;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {
    Button buttonPicture; Button buttonSave;
    ImageView imageView;
    TextView textView;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        buttonPicture = findViewById(R.id.camera_id);
        buttonSave = findViewById(R.id.save);
        imageView = findViewById(R.id.image);

        activityResultLauncher = registerForActivityResult( // Activity result para apanhar resultado da camara
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getExtras() != null) {
                            imageBitmap = (Bitmap) data.getExtras().get("data"); // BitMap da Camara
                            imageView.setImageBitmap(imageBitmap);
                            Log.d("CameraActivity", "Image received and displayed");
                        }
                    } else {
                        Log.d("CameraActivity", "Camera capture cancelled or failed");
                    }
                }
        );

        buttonPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    activityResultLauncher.launch(takePictureIntent);
                    Log.d("CameraActivity", "Requested image capture");
                } catch (ActivityNotFoundException e) {
                    Log.d("CameraActivity", "Unable to request image capture");
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create string with date to systematize the saved files
                DateFormat dateFormat = new SimpleDateFormat("dd_MM_yy-HH_mm_ss");
                Date today = Calendar.getInstance().getTime();
                String date= dateFormat .format(today);

                // create File to be saved
                File imageFile = new File(getFilesDir(), "capture_" + date + ".png");

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

                // Save to Pictures/CondoSched
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CondoSched");

                ContentResolver resolver = getContentResolver();
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (imageUri != null) {
                    try (OutputStream out = resolver.openOutputStream(imageUri)) {
                        if (out != null) {
                            // convert Bitmap to PNG
                            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            String imageLocation = "Pictures/CondoSched/" + imageFile.getName();

                            // Success Toast + Log
                            String locationMessage = "Saved to: " + imageLocation;
                            Toast.makeText(CameraActivity.this, locationMessage, Toast.LENGTH_LONG).show();
                            Log.d("CameraActivity", "Capture saved to: " + imageLocation);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}