package com.flier.uottahack2019.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.flier.uottahack2019.R;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.api.client.util.DateTime;
import com.joestelmach.natty.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.flier.uottahack2019.GoogleCloudService.CalendarService;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;

    Button button;
    ImageView imageView;

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button_picture);
        imageView = findViewById(R.id.image_view);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //system requirements
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                        //request permission
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);

                    } else {
                        //permission granted already
                        openCamera();
                    }
                else {
                    //os < current
                    openCamera();
                }
            }
        });

//        getTextFromImage(BitmapFactory.decodeResource(getResources(), R.drawable.test_poster_3));
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pictures");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    //handling permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Permission Denied", LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            //Set the image captured to ImageView
            imageView.setImageURI(image_uri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
                getTextFromImage(bitmap);
            } catch (java.io.IOException e) {
                Log.e(TAG, "Failed to create bitmap: ", e);
            }
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void getTextFromImage(Bitmap bitmap) {
        Log.d(TAG, "Extracting text...");
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);
        String title = "";
        String text = "";
        int maxHeight = 0;
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
            Log.d(TAG, textBlock.getValue());
            List<? extends Text> lines = textBlock.getComponents();
            int height = lines.get(0).getBoundingBox().height();
            Log.d(TAG, "Height: " + String.valueOf(height));
            if (height > maxHeight) {
                maxHeight = height;
                for (int j = 0; j < lines.size(); j++) {
                    title += " " + lines.get(j).getValue();
                }
            }
            text += " " + textBlock.getValue();
        }
        Log.d(TAG, title);
        Log.d(TAG, text);

        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(text);
        if (!groups.isEmpty()) {
            List<Date> dates = new ArrayList<>();
            for (DateGroup group : groups) {
                Log.d(TAG, "DATEGROUP");
                dates.addAll(group.getDates());
            }

            DateTime dateTime = new DateTime(dates.get(0));
            try {
                CalendarService calendarService = new CalendarService();
                calendarService.insertEvent(title, dateTime);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }
}