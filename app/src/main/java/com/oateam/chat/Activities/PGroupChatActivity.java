package com.oateam.chat.Activities;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.oateam.chat.Adapters.AdapterPGroupChat;
import com.oateam.chat.Adapters.MessagesAdapter;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.Models.ModelPGroupChat;
import com.oateam.chat.R;
import com.oateam.chat.notifications.APIService;
import com.oateam.chat.notifications.Client;
import com.oateam.chat.notifications.Data;
import com.oateam.chat.notifications.Response;
import com.oateam.chat.notifications.Sender;
import com.oateam.chat.notifications.Token;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiPopup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;

public class PGroupChatActivity extends AppCompatActivity {

    private  String GID,GImage,GName;
    private Toolbar toolbar;
    private CircularImageView profileImage;
    private TextView name;
    private ImageButton attach,emoji,back;
    private EditText message;
    private FloatingActionButton send;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private RecyclerView recyclerView;
    private ArrayList<ModelPGroupChat> groupChatArrayList;
    private AdapterPGroupChat adapterPGroupChat;
    private  String myRole,date,time;
    boolean isemojiShow = false;
    private LinearLayout attachmentLayout;
    private boolean isHidden = true;

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
        setContentView(R.layout.activity_pgroup_chat);




        toolbar = (Toolbar) findViewById(R.id.toolbar_Pgroup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setTitle(null);
        profileImage = (CircularImageView) findViewById(R.id.image_Pgroup);
        name = (TextView) findViewById(R.id.groupName_P);
        attach = (ImageButton) findViewById(R.id.attach_file_Pgroup);
        emoji = (ImageButton) findViewById(R.id.emojiBtn_PGroups);
        back = (ImageButton) findViewById(R.id.back_Pgroup);
        message = (EditText) findViewById(R.id.text_content_chatEEE_Pgroup);
        send = (FloatingActionButton) findViewById(R.id.btn_send_message__Pgroup);
        recyclerView = findViewById(R.id.recyclerView_Pgroup_messages);
        attachmentLayout = (LinearLayout) findViewById(R.id.menu_attachments);
        ImageButton btnDocument = (ImageButton) findViewById(R.id.menu_attachment_document);
        ImageButton btnAudio = (ImageButton) findViewById(R.id.menu_attachment_audio);
        ImageButton btnGallery = (ImageButton) findViewById(R.id.menu_attachment_gallery);
        ImageButton btnCamera = (ImageButton) findViewById(R.id.camera_attach);
        rootRef = FirebaseDatabase.getInstance().getReference();
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Intent i = getIntent();
        GID = i.getStringExtra("groupId");
        GImage = i.getStringExtra("groupImage");
        GName = i.getStringExtra("groupName");
        firebaseAuth = FirebaseAuth.getInstance();
        EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.root_view_Pgroups)).build(message);
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
        Picasso.get().load(GImage).placeholder(R.drawable.ic_baseline_group_24).into(profileImage);
        name.setText(GName);

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);

        SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = dateFormat.format(calendar.getTime());

//        SimpleDateFormat timeFormat  = new SimpleDateFormat("HH:mm");
//        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        time = timeFormat.format(calendar.getTime());



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String mes = message.getText().toString().trim();

                if (TextUtils.isEmpty(mes))
                {
                    Toast.makeText(PGroupChatActivity.this, "Please write message.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    notify = true;
                    SendMessage(mes);
                }


            }
        });


        emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (isemojiShow)
                {
                    emoji.setImageResource(R.drawable.ic_baseline_keyboard_24);

                    emojiPopup.toggle();
                }
                else
                {
                    emoji.setImageResource(R.drawable.emoji);
                }
                isemojiShow = !isemojiShow;
                emojiPopup.toggle();
            }
        });


        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu();
            }
        });




        message.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideMenu();
                return false;
            }
        });



        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
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
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
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
        });

        btnDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

            }
        });

        loadGroupInfo();
        loadGroupMessages();
        loadGroupRole();

    }


    private void loadGroupRole()
    {
        DatabaseReference rrrr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rrrr.child(GID)
                .child("Participants")
                .orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    myRole = ""+ds.child("role").getValue();
                    invalidateOptionsMenu();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupMessages()
    {
       groupChatArrayList = new ArrayList<>();

        DatabaseReference rRR = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rRR.child(GID).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                groupChatArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    ModelPGroupChat modelPGroupChat = dataSnapshot.getValue(ModelPGroupChat.class);
                    groupChatArrayList.add(modelPGroupChat);
                }
                adapterPGroupChat = new AdapterPGroupChat(PGroupChatActivity.this,groupChatArrayList);
                recyclerView.setAdapter(adapterPGroupChat);
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void SendMessage(String mes)
    {
        String messageText = message.getText().toString();

        String timeS = ""+System.currentTimeMillis();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", mes);
        hashMap.put("date", date);
        hashMap.put("type", "text");

        DatabaseReference RRR = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        RRR.child(GID).child("Messages").child(timeS).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused)
            {
                message.setText("");


                String msg = messageText;
                final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());
                database.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        Contacts contacts = snapshot.getValue(Contacts.class);
                        if (notify)
                        {
//                            sendNotification(messageReceiverID, contacts.getName(), messageText);
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(PGroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadGroupInfo()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        reference.orderByChild("GID").equalTo(GID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    String Gdes = ""+ds.child("groupDescription").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    @Override
    protected void onStart() {
        hideMenu();
        super.onStart();
    }

    private void hideMenu() {
        attachmentLayout.setVisibility(View.GONE);
        isHidden = true;
    }


    private void sendNotification(String messageReceiverID, String name, String messageText)
    {
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(messageReceiverID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(firebaseAuth.getUid(),name+":"+messageText,GName, messageReceiverID,R.drawable.r);

                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response)
                                {

                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    void showMenu() {
        int cx = (attachmentLayout.getLeft() + attachmentLayout.getRight());
        int cy = attachmentLayout.getTop();
        int radius = Math.max(attachmentLayout.getWidth(), attachmentLayout.getHeight());

        if (isHidden) {
            Animator anim = android.view.ViewAnimationUtils.createCircularReveal(attachmentLayout, cx, cy, 0, radius);
            attachmentLayout.setVisibility(View.VISIBLE);
            anim.start();
            isHidden = false;
        } else {
            Animator anim = android.view.ViewAnimationUtils.createCircularReveal(attachmentLayout, cx, cy, radius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    attachmentLayout.setVisibility(View.INVISIBLE);
                    isHidden = true;
                }
            });
            anim.start();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pgroups, menu);
        loadGroupRole();
//        if (myRole.equals("creator"))
//        {
//            menu.findItem(R.id.add_parti_action).setVisible(true);
//            menu.findItem(R.id.edit_group_action).setVisible(true);
//        }
//        else if (myRole.equals("admin"))
//        {
//            menu.findItem(R.id.add_parti_action).setVisible(true);
//            menu.findItem(R.id.edit_group_action).setVisible(false);
//        }
//        else
//        {
//            menu.findItem(R.id.add_parti_action).setVisible(false);
//            menu.findItem(R.id.edit_group_action).setVisible(false);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_parti_action)
        {
            Intent i = new Intent(this, PGroupAddParticipantActivity.class);
            i.putExtra("groupId", GID);
            startActivity(i);
        }
        if (id == R.id.edit_group_action)
        {
            Intent i = new Intent(this, PGroupInfoActivity.class);
            i.putExtra("groupId", GID);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
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
               sendImageMessage();
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                sendImageMessage();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage()
    {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Sending image...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String fileNamePath = "Image Files/" + ""+System.currentTimeMillis();

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

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNamePath);
        storageReference.putBytes(datasa).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Task<Uri> p_uritask = taskSnapshot.getStorage().getDownloadUrl();
                while (!p_uritask.isSuccessful());
                Uri p_downloadUri = p_uritask.getResult();
                String finalURL = p_downloadUri.toString();
                if (p_uritask.isSuccessful())
                {

                    String timeS = ""+System.currentTimeMillis();
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("sender", firebaseAuth.getUid());
                    hashMap.put("message", finalURL);
                    hashMap.put("timestamp", timeS);
                    hashMap.put("type", "image");

                    DatabaseReference RRR = FirebaseDatabase.getInstance().getReference("PrivateGroups");
                    RRR.child(GID).child("Messages").child(timeS).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused)
                        {
                            message.setText("");
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();

                            Toast.makeText(PGroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(PGroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}