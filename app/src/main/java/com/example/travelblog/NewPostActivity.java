package com.example.travelblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private EditText newPostDescription;
    private Button newPostButton;
    private ProgressBar progressBar;

    private Uri postImageUri;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private Bitmap compressedImageFile;

    SharedPref pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = new SharedPref(this);
        if(pref.loadNightMode() == true){
            setTheme(R.style.DarkTheme);
        }else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add Destination");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage = findViewById(R.id.post_image);
        newPostDescription = findViewById(R.id.post_description);
        newPostButton = findViewById(R.id.post_button);
        progressBar = findViewById(R.id.post_progressBar);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });

        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc = newPostDescription.getText().toString();

                if (!TextUtils.isEmpty(desc) && postImageUri != null) {

                    progressBar.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    final StorageReference filepath = storageReference.child("post_images").child(randomName + ".jpg");
                    filepath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

//                            final String downloadUri = task.getResult().getStorage().getDownloadUrl().toString();

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();

                                    if (task.isSuccessful()) {

                                        File newImageFile = new File(postImageUri.getPath());

                                        try {
                                            compressedImageFile = new Compressor(NewPostActivity.this)
                                                    .setMaxHeight(100)
                                                    .setMaxWidth(100)
                                                    .setQuality(2)
                                                    .compressToBitmap(newImageFile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                        byte[] thumbData = baos.toByteArray();

                                        final StorageReference thumb_image = storageReference
                                                .child("post_images/thumbs").child(randomName + ".jpg");

                                        UploadTask uploadTask = thumb_image.putBytes(thumbData);

                                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                thumb_image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String downloadThumbUri = uri.toString();

                                                        final Map<String, Object> postMap = new HashMap<>();
                                                        postMap.put("image_url",downloadUri);
                                                        postMap.put("image_thumb",downloadThumbUri);
                                                        postMap.put("desc",desc);
                                                        postMap.put("user_id",current_user_id);
                                                        postMap.put("timestamp", FieldValue.serverTimestamp());

                                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                                if(task.isSuccessful()){
                                                                    String postID = task.getResult().getId();
                                                                    firebaseFirestore.collection("Posts").document(postID).update("post_id",postID);
                                                                    Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_SHORT).show();
                                                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                                    startActivity(mainIntent);
                                                                    finish();
                                                                } else {

                                                                    String error = task.getException().getMessage();

                                                                }

                                                                progressBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });


                                    } else {

                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });


                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
