package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.oateam.chat.R;
import com.oateam.chat.notifications.APIService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private CircularImageView profileImage;
    private TextView name;
    private ImageButton attach,emoji,back;
    private EditText message;
    private FloatingActionButton send;
    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    boolean isemojiShow = false;
    private LinearLayout attachmentLayout;
    private boolean isHidden = true;
    private String currentGroupName,currentUserID, currentUserName,currentDate,currentTime;
    private DatabaseReference UserRef,GroupNameRef,GroupMessageKeyRef;
    private Toolbar toolbar;
    private static final int CAMERA_REQ_CODE = 100;
    private static final int STORAGE_REQ_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri image_uri = null;
    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();


        ImageButton btnDocument = (ImageButton) findViewById(R.id.menu_attachment_document);
        ImageButton btnAudio = (ImageButton) findViewById(R.id.menu_attachment_audio);
        ImageButton btnGallery = (ImageButton) findViewById(R.id.menu_attachment_gallery);
        ImageButton btnCamera = (ImageButton) findViewById(R.id.camera_attach);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        toolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        InitializeFeilds();
        GetUserInfo();



    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if (snapshot.exists())
                {
//                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if (snapshot.exists())
                {
//                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetUserInfo()
    {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFeilds()
    {
        mtoolbar = (Toolbar) findViewById(R.id.toolbar_group);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupName);
        profileImage = (CircularImageView) findViewById(R.id.image_group);
        name = (TextView) findViewById(R.id.groupName);
        attach = (ImageButton) findViewById(R.id.attach_file_group);
        emoji = (ImageButton) findViewById(R.id.emojiBtn_Groups);
        back = (ImageButton) findViewById(R.id.back_group);
        message = (EditText) findViewById(R.id.text_content_chatEEE_group);
        send = (FloatingActionButton) findViewById(R.id.btn_send_message__group);
        recyclerView = findViewById(R.id.recyclerView_group_messages);
        attachmentLayout = (LinearLayout) findViewById(R.id.menu_attachments);
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


    private void pickGallery(){
        Intent intent  = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }


    private void pickCamera()
    {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "GroupTitle");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDESCRIPTION");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
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
                        Toast.makeText(this, "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Storage permissions are required", Toast.LENGTH_SHORT).show();
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
//                sendImageMessage();
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
//                sendImageMessage();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    private void sendImageMessage()
//    {
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Please wait");
//        progressDialog.setMessage("Sending image...");
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.show();
//
//        String fileNamePath = "Image Files/" + ""+System.currentTimeMillis();
//
//        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNamePath);
//        storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
//            {
//                Task<Uri> p_uritask = taskSnapshot.getStorage().getDownloadUrl();
//                while (!p_uritask.isSuccessful());
//                Uri p_downloadUri = p_uritask.getResult();
//                if (p_uritask.isSuccessful())
//                {
//                    String timeS = ""+System.currentTimeMillis();
//                    HashMap<String,Object> hashMap = new HashMap<>();
//                    hashMap.put("sender", firebaseAuth.getUid());
//                    hashMap.put("message", p_downloadUri);
//                    hashMap.put("timestamp", timeS);
//                    hashMap.put("type", "image");
//
//                    DatabaseReference RRR = FirebaseDatabase.getInstance().getReference("PublicGroups");
//                    RRR.child(GID).child("Messages").child(timeS).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void unused)
//                        {
//                            message.setText("");
//                            progressDialog.dismiss();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e)
//                        {
//                            progressDialog.dismiss();
//
//                            Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e)
//            {
//                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

}