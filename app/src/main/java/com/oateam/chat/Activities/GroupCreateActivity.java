package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oateam.chat.MainActivity;
import com.oateam.chat.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class GroupCreateActivity extends AppCompatActivity {



    private static final int CAMERA_REQ_CODE = 100;
    private static final int STORAGE_REQ_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;


    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri image_uri = null;
    private ImageView groupImage;
    private EditText Gname,Gdescription;
    private FloatingActionButton createG;
    private FirebaseAuth firebaseAuth;
    private ActionBar actionBar;
    private String currentUSerID;
    private ProgressDialog progressDialog;
    private AdView adView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);


        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_pubG);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Group");


        groupImage = (ImageView) findViewById(R.id.groupImage);
        Gname = (EditText) findViewById(R.id.groupNamePublic);
        Gdescription = (EditText) findViewById(R.id.groupDesPublic);
        createG = (FloatingActionButton) findViewById(R.id.groupCreateBTN);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUSerID = firebaseAuth.getCurrentUser().getUid();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = findViewById(R.id.adView_profile);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShowImagePickDialog();
            }
        });


        createG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CreateGroupFunction();
            }
        });
    }

    private void CreateGroupFunction()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Group");

        String groupName =Gname.getText().toString().trim();
        String groupDES =Gdescription.getText().toString().trim();

        if (TextUtils.isEmpty(groupName))
        {
            Toast.makeText(this, R.string.enter_groupName, Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String gTimestamp = ""+ System.currentTimeMillis();
        if (image_uri == null)
        {
            StorageReference storage = FirebaseStorage.getInstance().getReference();
            storage.child("group.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String ImageUrl = uri.toString();
                    createGroupNoImage(gTimestamp,groupName,groupDES, ImageUrl);
                }
            });
        }
        else
        {
            String fileNameAndPath = "PUBGroup_Imgs/" + "images" + gTimestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), image_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap imageAfterRotation = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageAfterRotation.compress(Bitmap.CompressFormat.JPEG, 35, baos);
            byte[] datasa = baos.toByteArray();

            storageReference.putBytes(datasa).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Task<Uri> p_uritask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!p_uritask.isSuccessful());

                        Uri p_downloadUri = p_uritask.getResult();
                        if (p_uritask.isSuccessful())
                        {
                            createGroupNoImage(gTimestamp,groupName,groupDES, ""+p_downloadUri);
                        }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    progressDialog.dismiss();
                    Toast.makeText(GroupCreateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createGroupNoImage(final String gTimestamp, String groupN , String groupD,String gIcon)
    {

        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("GID", gTimestamp);
        hashMap.put("groupName", groupN);
        hashMap.put("groupDescription", groupD);
        hashMap.put("groupImage",gIcon);
        hashMap.put("timestamp", gTimestamp);
        hashMap.put("createdBy", currentUSerID);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        ref.child(gTimestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused)
            {
                HashMap<String, String> hashMap1 = new HashMap<>();
                hashMap1.put("uid", firebaseAuth.getUid());
                hashMap1.put("role", "creator");
                hashMap1.put("timestamp", gTimestamp);


               DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("PrivateGroups").child(gTimestamp);
                ref1.child("Participants").child(currentUSerID).setValue(hashMap1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        progressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, R.string.group_created_success, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GroupCreateActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(GroupCreateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                progressDialog.dismiss();
                Toast.makeText(GroupCreateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ShowImagePickDialog()
    {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image:")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (i == 0)
                        {
                            if (!checkCameraPermissions())
                            {
                                requestCameraPermissions();
                            }
                            else
                            {
                                pickCamera();
                            }
                        }
                        else
                        {
                            if (!checkStoragePermissions())
                            {
                                requestStoragePermissions();
                            }
                            else
                            {
                                pickGallery();
                            }
                        }
                    }
                }).show();
    }


    private void pickCamera()
    {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon DESCRIPTION");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }


    private boolean checkCameraPermissions(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestStoragePermissions()
    {
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQ_CODE);
    }

    private void requestCameraPermissions()
    {
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQ_CODE);
    }


    private void pickGallery()
    {
        Intent intent  = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case CAMERA_REQ_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted)
                    {
                        pickCamera();
                    }

                    else
                    {
                        Toast.makeText(this, R.string.camera_storage_permission_req, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQ_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted)
                    {
                        pickGallery();
                    }

                    else
                    {
                        Toast.makeText(this, R.string.storage_permission_req, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                image_uri = data.getData();
                groupImage.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                groupImage.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
