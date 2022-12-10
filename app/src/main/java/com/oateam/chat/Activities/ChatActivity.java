package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;

import java.lang.Math;

import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.Models.Messages;
import com.oateam.chat.Adapters.MessagesAdapter;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID , messageReceiverName,messageReceiverImage,messageSenderID;
    private TextView username,lastSeenTV;
    private CircularImageView profileImage;
    private Toolbar toolbar;
    private ImageButton backChat,attachFile,emojiBtn;
    private FloatingActionButton btn_send;
    private RecyclerView recyclerView;
    private EditText messageET;
    private FirebaseAuth auth;
    private CardView blockedC;
    private DatabaseReference reference;
    private String currentUserID;
    private String time,date;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private LinearLayout attachmentLayout;
    private boolean isHidden = true;
    private LinearLayout CHAAAAT;
    boolean isemojiShow = false;
    private String checker = "", myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog progressDialog;
    private boolean isBlocked = false;

    ValueEventListener seenListner;
    DatabaseReference userRefForSeen;


    APIService apiService;
    boolean notify = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);




        auth = FirebaseAuth.getInstance();
        messageSenderID = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        messageReceiverID = getIntent().getExtras().getString("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().getString("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().getString("visit_user_image").toString();
        currentUserID = auth.getCurrentUser().getUid();
//        getMessages();


        initController();
        EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.root_view)).build(messageET);

        backChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();

            }
        });


        username.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.user).into(profileImage);



        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                DatabaseReference refff = reference.child("Users").child(messageReceiverID).child("userState").child("state");

               if (!refff.equals("online"))
               {
                   notify = true;
               }
                SendMessage();
                messageET.setText("");
            }
        });





        messageET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideMenu();

                return false;
            }
        });

        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2)
            {
                if (s.toString().trim().length() == 0)
                {
                    TypingStatus("noONe");
                }
                else
                {
                    TypingStatus(messageReceiverID);
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        DisplayLastSeen();
        checkIsBlocked();
        seenMessage();
        GGTMessages();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(messageReceiverID).child("Blocked Users").orderByChild("uid").equalTo(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot)
                    {
                        for(DataSnapshot ds : snapshot.getChildren())
                        {
                            if (ds.exists())
                            {
                                CHAAAAT.setVisibility(View.GONE);
                                blockedC.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                    }
                });



        emojiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (isemojiShow)
                {
                    emojiBtn.setImageResource(R.drawable.ic_baseline_keyboard_24);

                    emojiPopup.toggle();
                }
                else
                {
                    emojiBtn.setImageResource(R.drawable.emoji);
                }
                isemojiShow = !isemojiShow;
                emojiPopup.toggle();
            }
        });

    }//Oncreate

    private void GGTMessages()
    {
        reference.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       messagesList.clear();
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            Messages messages = ds.getValue(Messages.class);
                            assert messages != null;
                            if (messages.getTo().equals(messageReceiverID)&&messages.getFrom().equals(messageSenderID) || messages.getTo().equals(messageSenderID)&& messages.getFrom().equals(messageReceiverID))
                            {
                                messagesList.add(messages);
                            }
                            messagesAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getMessages()
    {
        reference.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                                           @Override
                                           public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
                                           {
                                               Messages messages = snapshot.getValue(Messages.class);

                                               messagesList.add(messages);
                                               messagesAdapter.notifyDataSetChanged();
                                               recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                                           }

                                           @Override
                                           public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                               messagesAdapter.notifyDataSetChanged();

                                           }

                                           @Override
                                           public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                               messagesAdapter.notifyDataSetChanged();

                                           }

                                           @Override
                                           public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                               messagesAdapter.notifyDataSetChanged();

                                           }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError error) {
                                               messagesAdapter.notifyDataSetChanged();

                                           }
                                       }
                );
    }
     private void seenMessage()
     {
         DatabaseReference db = FirebaseDatabase.getInstance().getReference();
         DatabaseReference messagesRef = db.child("Messages");
         DatabaseReference messageReceiverIdRef = messagesRef.child(messageReceiverID).child(messageSenderID);
         messageReceiverIdRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
             @Override
             public void onComplete(@NonNull Task<DataSnapshot> task) {
                 if (task.isSuccessful()) {
                     for (DataSnapshot ds : task.getResult().getChildren()) {
                         String messageId = ds.child("messageID").getValue(String.class);

                         //isSeen = true
                         userRefForSeen = FirebaseDatabase.getInstance().getReference("Messages")
                                 .child(messageReceiverID).child(messageSenderID);
                         userRefForSeen.addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot)
                             {
                                 Messages messages = snapshot.getValue(Messages.class);

//                                 if (messages.getFrom().equals(messageSenderID) && messages.getTo().equals(messageReceiverID))
//                                 {
                                 if (messageId !=null)
                                 {
                                     HashMap hasSeenHash = new HashMap<>();
                                     hasSeenHash.put("isSeen",true);

                                     snapshot.getRef().child(messageId).updateChildren(hasSeenHash);
                                 }

//                                 }
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {

                             }
                         });
                     }
                 } else {
                     Log.d("TAG", task.getException().getMessage()); //Never ignore potential errors!
                 }
             }
         });
     }




    private void initController()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setTitle(null);


        profileImage = (CircularImageView) findViewById(R.id.image_chat);
        username = (TextView) findViewById(R.id.username_chat);
        lastSeenTV = (TextView) findViewById(R.id.online_status_chat);
        backChat =  (ImageButton) findViewById(R.id.back_chat);
        btn_send = (FloatingActionButton)findViewById(R.id.btn_send_message_chat);
        messageET = (EditText)findViewById(R.id.text_content_chatEEE);
        attachFile =  (ImageButton)findViewById(R.id.attach_file);
        emojiBtn = (ImageButton)findViewById(R.id.emojiBtn);
        attachmentLayout = (LinearLayout) findViewById(R.id.menu_attachments);
        CHAAAAT = (LinearLayout) findViewById(R.id.chat_E_lay);
        blockedC = (CardView) findViewById(R.id.card_blocked);
        progressDialog = new ProgressDialog(this);
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);



        ImageButton btnDocument = (ImageButton) findViewById(R.id.menu_attachment_document);
        ImageButton btnAudio = (ImageButton) findViewById(R.id.menu_attachment_audio);
        ImageButton btnGallery = (ImageButton) findViewById(R.id.menu_attachment_gallery);


        btnDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
               checker = "document";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/*");
                startActivityForResult(intent.createChooser(intent,"Select Document"), 123);
            }
        });
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "audio";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent.createChooser(intent,"Select Audio"), 123);
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                checker = "image";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent,"Select Image"), 123);
            }
        });

        messagesAdapter = new MessagesAdapter(messagesList);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView_chat_messages) ;
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messagesAdapter);


        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);

        SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = dateFormat.format(calendar.getTime());


        attachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu();
            }
        });

    }



    private void updateUserStatus(String state)
    {
        String lastSeenTime;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        lastSeenTime = dateFormat.format(calendar.getTime());
        DatabaseReference simpleRef2 = reference.child("Users").child(currentUserID).child("userState");

        HashMap<String,Object> onlineStatemap = new HashMap<>();
        onlineStatemap.put("lastSeen", lastSeenTime);
        onlineStatemap.put("state", state);
        simpleRef2.updateChildren(onlineStatemap);
    }





    private void CheckOnlineStatus(String state)
    {

        DatabaseReference simpleRef = reference.child("Users").child(currentUserID).child("userState");

        HashMap<String,Object> onlinemap = new HashMap<>();
        onlinemap.put("state", state);
        simpleRef.updateChildren(onlinemap);
    }






    @Override
    protected void onStart() {

//        updateUserStatus("online");
        CheckOnlineStatus("online");
        hideMenu();

        super.onStart();
    }

    @Override
    protected void onPause() {
        updateUserStatus("offline");
        TypingStatus("noOne");
        messagesList.clear();
        super.onPause();
    }



    @Override
    protected void onDestroy() {
       updateUserStatus("offline");
        messagesList.clear();
        super.onDestroy();
    }






    private void DisplayLastSeen()
    {
        reference.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.child("userState").hasChild("state"))
                {
                    String state = snapshot.child("userState").child("state").getValue().toString();
                    String lastSeenTT = snapshot.child("userState").child("lastSeen").getValue().toString();


                        String typing = snapshot.child("userState").child("typingTo").getValue().toString();

                        if (typing.equals(currentUserID))
                        {
                            lastSeenTV.setText("typing...");
                        }
                        else
                        {
                            if (state.equals("online"))
                            {
                                lastSeenTV.setText("online");
                            }
                            if (state.equals("offline"))
                            {
                                String timeAgo = calculateTime(lastSeenTT);
                                lastSeenTV.setText("last seen "+timeAgo);
                            }
                        }
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode==RESULT_OK && data != null && data.getData() != null)
        {

            progressDialog.setTitle("Sending File");
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            fileUri = data.getData();


            if (checker.equals("document"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Documents Files");


                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessagesKeyRef = reference.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                String messagePushID = userMessagesKeyRef.getKey();

                Cursor cursor = getContentResolver().query(fileUri,null, null, null, null);

                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                cursor.moveToFirst();
                String filename =  cursor.getString(nameIndex).toString() ;
                float SSS =cursor.getFloat(sizeIndex) / (1024*1024);
                String fileSize = new DecimalFormat("##.##").format(SSS).toString();

                String extension  = filename.substring(filename.lastIndexOf(".")+1);
                StorageReference filepath = storageReference.child(messagePushID + "." + extension);

                filepath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadURl = uri.toString();

                                    Map messageDocBody = new HashMap();
                                    messageDocBody.put("message", downloadURl);
                                    messageDocBody.put("type",checker);
                                    messageDocBody.put("name",filename);
                                    messageDocBody.put("extension",extension);
                                    messageDocBody.put("to",messageReceiverID);
                                    messageDocBody.put("from",messageSenderID);
                                    messageDocBody.put("date",date);
                                    messageDocBody.put("messageID",messagePushID);
                                    messageDocBody.put("size",fileSize+" MB");


                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID , messageDocBody);
                                    messageBodyDetails.put(messageReceiverRef + "/" + messagePushID , messageDocBody);

                                    reference.updateChildren(messageBodyDetails);
                                    progressDialog.dismiss();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot)
                    {
                        double pd= (100.0*snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setMessage((int) pd + ""+R.string.uploading);
                    }
                });



      }

            else if (checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessagesKeyRef = reference.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                String messagePushID = userMessagesKeyRef.getKey();
                StorageReference filepath = storageReference.child(messagePushID + "." + "jpg");

                Bitmap bmp = null;
                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(0);
                Bitmap imageAfterRotation = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageAfterRotation.compress(Bitmap.CompressFormat.JPEG, 35, baos);
                byte[] datasa = baos.toByteArray();



                uploadTask = filepath.putBytes(datasa);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            String path = fileUri.getLastPathSegment();
                            String filename = path.substring(path.lastIndexOf("/")+1);
                            String extension  = path.substring(path.lastIndexOf(".")+1);

                            Map messagePicBody = new HashMap();
                            messagePicBody.put("message",myUrl);
                            messagePicBody.put("name",filename);
                            messagePicBody.put("type",checker);
                            messagePicBody.put("extension",extension);
                            messagePicBody.put("to",messageReceiverID);
                            messagePicBody.put("from",messageSenderID);
                            messagePicBody.put("date",date);
                            messagePicBody.put("messageID",messagePushID);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID , messagePicBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID , messagePicBody);

                            reference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        messagesAdapter.notifyDataSetChanged();
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Toast toast = new Toast(getApplicationContext());
                                        toast.setDuration(Toast.LENGTH_LONG);

                                        //inflate view
                                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                        ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.failed_message);
                                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                                        toast.setView(custom_view);
                                        toast.show();
                                    }
                                    messageET.setText("");
                                }
                            });

                        }
                    }
                });
            }

            else if (checker.equals("audio"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Audio Files");


                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessagesKeyRef = reference.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                String messagePushID = userMessagesKeyRef.getKey();

                StorageReference filepath = storageReference.child(messagePushID + "." + "jpg");

            }
            else
            {
                progressDialog.dismiss();
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);

                //inflate view
                View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.nothing_selected);
                ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                toast.setView(custom_view);
                toast.show();
            }
        }
    }

    private String calculateTime(String lastSeenTT)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            long time = sdf.parse(lastSeenTT).getTime();
            long now = System.currentTimeMillis();
            sdf.setTimeZone(TimeZone.getDefault());
            CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago+"";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


    private void SendMessage()
    {

        String messageText = messageET.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);

            //inflate view
            View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
            ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.please_write_message);
            ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
            ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

            toast.setView(custom_view);
            toast.show();
        }

        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessagesKeyRef = reference.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessagesKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("date",date);
            messageTextBody.put("isSeen",false);
            messageTextBody.put("messageID",messagePushID);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID,messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,messageTextBody);

            reference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {

                    }
                    else
                    {
                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);

                        //inflate view
                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                        ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.failed_message);
                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                        toast.setView(custom_view);
                        toast.show();
                    }


                    String msg = messageText;
                    final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            Contacts contacts = snapshot.getValue(Contacts.class);
                            if (notify)
                            {
                                sendNotification(messageReceiverID, contacts.getName(), messageText);
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                    .child(messageSenderID)
                    .child(messageReceiverID);

            chatRef1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {

                    if (!snapshot.exists())
                    {
                        chatRef1.child("id").setValue(messageReceiverID);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                    .child(messageReceiverID)
                    .child(messageSenderID);


            chatRef2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if (!snapshot.exists())
                    {
                        chatRef2.child("id").setValue(messageSenderID);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
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
                    Data data = new Data(currentUserID,name+":"+messageText,"New Message",messageReceiverID,R.drawable.r);

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

    @Override
    public void onResume(){
//        getMessages();
        super.onResume();
    }





    private void TypingStatus(String TypingState)
    {

        HashMap<String,Object> typingStatemap = new HashMap<>();
        typingStatemap.put("typingTo", TypingState);
        currentUserID = auth.getCurrentUser().getUid();
        reference.child("Users").child(currentUserID).child("userState").updateChildren(typingStatemap);
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

    private void hideMenu() {
        attachmentLayout.setVisibility(View.GONE);
        isHidden = true;
    }


    private String saveToInternalStorage(Bitmap bitmapImage){

        String path = fileUri.getLastPathSegment();
        String filename = path.substring(path.lastIndexOf("/")+1);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("Images/Sent", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_whatsapp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.block_action)
        {
            checkIsBlocked();

            if (isBlocked)
            {
                item.setTitle("Unblock");
                UnBlockUser();
            }
            else
            {
//                item.setTitle("Unblock");

                BlockUser();
            }
        }


        return super.onOptionsItemSelected(item);
    }




    private void checkIsBlocked()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(messageSenderID).child("Blocked Users").orderByChild("uid").equalTo(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot)
                    {
                        for(DataSnapshot ds : snapshot.getChildren())
                        {
                            if (ds.exists())
                            {
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error)
                    {

                    }
                });
    }

    private void BlockUser()
    {
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid",messageReceiverID);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(messageSenderID).child("Blocked Users").child(messageReceiverID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);

                        //inflate view
                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                        ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.blocked_success);
                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_done_24);
                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.green_500));

                        toast.setView(custom_view);
                        toast.show();
                        invalidateOptionsMenu();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e)
                    {
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UnBlockUser()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(messageSenderID).child("Blocked Users").orderByChild("uid").equalTo(messageReceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    if (ds.exists())
                    {
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused)
                            {

                                Toast toast = new Toast(getApplicationContext());
                                toast.setDuration(Toast.LENGTH_LONG);

                                //inflate view
                                View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.unblocked_success);
                                ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_done_24);
                                ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.green_500));

                                toast.setView(custom_view);
                                toast.show();
                                invalidateOptionsMenu();
                                finish();
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }
}
