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
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oateam.chat.MainActivity;
import com.oateam.chat.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class EditGroupActivity extends AppCompatActivity {

    private static final int CAMERA_REQ_CODE = 100;
    private static final int STORAGE_REQ_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;


    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri image_uri = null;
    private ImageView groupImageWW;
    private EditText Gname,Gdescription;
    private FloatingActionButton updateG;
    private FirebaseAuth firebaseAuth;
    private ActionBar actionBar;
    private String currentUSerID;
    private ProgressDialog progressDialog;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        groupId = getIntent().getStringExtra("groupId");



        groupImageWW = (ImageView) findViewById(R.id.groupImage);
        Gname = (EditText) findViewById(R.id.groupNamePublic);
        Gdescription = (EditText) findViewById(R.id.groupDesPublic);
        updateG = (FloatingActionButton) findViewById(R.id.groupEditBTN);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Updating group info");
        progressDialog.setCanceledOnTouchOutside(false);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        groupImageWW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShowImagePickDialog();
            }
        });

        loadGroupInfo();

        updateG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                StartUpdatingGroup();
            }
        });
    }

    private void StartUpdatingGroup()
    {
        progressDialog.dismiss();
        String GroupName = Gname.getText().toString().trim();
        String GroupDescrip  = Gdescription.getText().toString().trim();

        if (TextUtils.isEmpty(GroupName))
        {
            Toast.makeText(this, R.string.groupName_req, Toast.LENGTH_SHORT).show();
        }
        progressDialog.show();

        if (image_uri == null)
        {
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("groupName", GroupName);
            hashMap.put("groupDescription", GroupDescrip);

            DatabaseReference rr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
            rr.child(groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused)
                {
                    Intent intent = new Intent(EditGroupActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    progressDialog.dismiss();
                    Toast.makeText(EditGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            String gTimestamp = ""+System.currentTimeMillis();
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
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("groupName", GroupName);
                        hashMap.put("groupDescription", GroupDescrip);
                        hashMap.put("groupImage", ""+p_downloadUri);

                        DatabaseReference rr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
                        rr.child(groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused)
                            {
                                progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                progressDialog.dismiss();
                                Toast.makeText(EditGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    progressDialog.dismiss();
                    Toast.makeText(EditGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadGroupInfo()
    {
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("PrivateGroups");
        ref.orderByChild("GID").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String groupId = ds.child("GID").getValue().toString();
                    final String groupname = ds.child("GID").getValue().toString();
                    String groupName = ds.child("groupName").getValue().toString();
                    String groupDescription = ds.child("groupDescription").getValue().toString();
                    String groupImage = ds.child("groupImage").getValue().toString();
                    String timestamp = ds.child("timestamp").getValue().toString();
                    String createee = ds.child("createdBy").getValue().toString();
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


                    Gname.setText(groupName);
                    Gdescription.setText(groupDescription);
                    try {
                        Picasso.get().load(groupImage).placeholder(R.drawable.ic_baseline_group_24).into(groupImageWW);
                    } catch (Exception e) {
                        groupImageWW.setImageResource(R.drawable.ic_baseline_group_24);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

            private void pickCamera() {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon Title");
                cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon DESCRIPTION");
                image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
                startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
            }

            private boolean checkStoragePermissions() {
                boolean result = ContextCompat.checkSelfPermission(EditGroupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
                return result;
            }

            private boolean checkCameraPermissions() {
                boolean result = ContextCompat.checkSelfPermission(EditGroupActivity.this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
                boolean result1 = ContextCompat.checkSelfPermission(EditGroupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

                return result && result1;
            }

            private void requestStoragePermissions() {
                ActivityCompat.requestPermissions(EditGroupActivity.this, storagePermissions, STORAGE_REQ_CODE);
            }

            private void requestCameraPermissions() {
                ActivityCompat.requestPermissions(EditGroupActivity.this, cameraPermissions, CAMERA_REQ_CODE);
            }

            private void ShowImagePickDialog() {
                String[] options = {"Camera", "Gallery"};

                AlertDialog.Builder builder = new AlertDialog.Builder(EditGroupActivity.this);
                builder.setTitle("Pick Image:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    if (!checkCameraPermissions()) {
                                        requestCameraPermissions();
                                    } else {
                                        pickCamera();
                                    }
                                } else {
                                    if (!checkStoragePermissions()) {
                                        requestStoragePermissions();
                                    } else {
                                        pickGallery();
                                    }
                                }
                            }
                        }).show();
            }

            private void pickGallery() {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
            }

            @Override
            public boolean onSupportNavigateUp() {
                onBackPressed();
                return EditGroupActivity.super.onSupportNavigateUp();
            }
}

