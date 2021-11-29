package com.uzair.picturecaptureapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    public static final int CAMERA_REQUEST_CODE = 10;
    ImageView imageView;
    StorageReference dbStorage;
    Uri imageUri;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Uploading");
        progressDialog.setCancelable(false);
        imageView = findViewById(R.id.imageView);

        dbStorage = FirebaseStorage.getInstance().getReference().child("TestImages");



        /// click on button to take image using camera
        findViewById(R.id.captureImage)
                .setOnClickListener(v -> {
                    if (isPermissionEnable()) {
                        cameraIntent();
                    } else {
                        requestPermission();
                    }
                });

        // click to upload image
        findViewById(R.id.uploadImage)
                .setOnClickListener(v -> {
                    progressDialog.show();
                    // Create a reference to "mountains.jpg"
                    StorageReference mountainsRef = dbStorage.child(imageUri.getLastPathSegment());
                    mountainsRef.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, ""+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });


                });
        



    }

    /// open camera intent
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.launch(intent);

    }

    /// get result from camera intent
    private ActivityResultLauncher<Intent> cameraIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {

                    Intent resultIntent = result.getData();
                    Bitmap imageBitmap = (Bitmap) resultIntent.getExtras().get("data");
                    Log.d("imageUriCamera", "imageUri: " + imageBitmap);
                    imageView.setImageBitmap(imageBitmap);
                    setImageUri(imageBitmap);

                }
            });

    private void setImageUri(Bitmap imageBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, "Image", null);
        imageUri = Uri.parse(path);

    }

    /// request for permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    /// check camera permission
    private boolean isPermissionEnable() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    /// convert image to byte array
    private byte[] convertImageToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the bitmap object
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == requestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

    }


}