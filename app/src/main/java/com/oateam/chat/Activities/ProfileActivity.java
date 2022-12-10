package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.R;
import com.oateam.chat.notifications.APIService;
import com.oateam.chat.notifications.Client;
import com.oateam.chat.notifications.Data;
import com.oateam.chat.notifications.Response;
import com.oateam.chat.notifications.Sender;
import com.oateam.chat.notifications.Token;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;

public class ProfileActivity extends AppCompatActivity {


    private String receiverUserID,current_state, senderUserID;

    private CircularImageView userProfileImage;
    private TextView usernameTV, userbioTV;
    private Button sendMessageRequestBtn, DeclinemessageRequestBtn;
    private DatabaseReference userRef,ChatrequestRef,FriendsRef,NotificationRef;
    private FirebaseAuth firebaseAuth;
    private AdView adView;
    APIService apiService;
    boolean notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatrequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        receiverUserID = getIntent().getExtras().get("visitUserID").toString();
        senderUserID = firebaseAuth.getCurrentUser().getUid();
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = findViewById(R.id.adView_profile);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        userProfileImage = (CircularImageView) findViewById(R.id.image_profile_cr);
        usernameTV = (TextView) findViewById(R.id.usrname_profile_tv);
        userbioTV = (TextView) findViewById(R.id.bio_profile_tv);
        sendMessageRequestBtn = (Button) findViewById(R.id.btn_send_message);
        DeclinemessageRequestBtn = (Button) findViewById(R.id.btn_decline_message);
        current_state = "new";

        RetrieveUserInfo();




    }

    private void RetrieveUserInfo()
    {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if ((snapshot.exists()) && (snapshot.hasChild("image")))
                {
                    String userImage = snapshot.child("image").getValue().toString();
                    String username = snapshot.child("name").getValue().toString();
                    String userBio = snapshot.child("bio").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.user).into(userProfileImage);
                    usernameTV.setText(username);
                    userbioTV.setText(userBio);


                    ManageChatRequests();
                }

                else
                {
                    String username = snapshot.child("name").getValue().toString();
                    String userBio = snapshot.child("bio").getValue().toString();
                    usernameTV.setText(username);
                    userbioTV.setText(userBio);
                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void ManageChatRequests()
    {

        ChatrequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.hasChild(receiverUserID))
                {
                    String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if (request_type.equals("sent"))
                    {
                        current_state = "request_sent";
                        sendMessageRequestBtn.setText("Cancel Message Request");
                    }
                    else if (request_type.equals("received"))
                    {
                        current_state = "request_received";
                        sendMessageRequestBtn.setText("Accept Message Request");
                        sendMessageRequestBtn.setBackgroundColor(getResources().getColor(R.color.green_400));
                        DeclinemessageRequestBtn.setVisibility(View.VISIBLE);
                        DeclinemessageRequestBtn.setEnabled(true);
                        DeclinemessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelMessageReq();
                            }
                        });
                    }
                }

                else
                {
                    FriendsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if (snapshot.hasChild(receiverUserID))
                            {
                                current_state = "friends";
                                sendMessageRequestBtn.setText("Remove friend");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!senderUserID.equals(receiverUserID))
        {
            sendMessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    sendMessageRequestBtn.setEnabled(false);
                    if (current_state.equals("new"))
                    {
                        notify = true;
                        SendRequest();
                    }

                    if (current_state.equals("request_sent"))
                    {
                        CancelMessageReq();
                    }

                    if (current_state.equals("request_received"))
                    {
                        AcceptMessageReq();
                    }
                    if (current_state.equals("friends"))
                    {
                        RemoveFriends();
                    }

                }
            });
        }

        else
        {
            sendMessageRequestBtn.setVisibility(View.INVISIBLE);
        }
    }


    private void RemoveFriends()
    {
        FriendsRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    FriendsRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                sendMessageRequestBtn.setEnabled(true);
                                current_state = "new";
                                sendMessageRequestBtn.setText("Send Message");

                                DeclinemessageRequestBtn.setVisibility(View.INVISIBLE);
                                DeclinemessageRequestBtn.setEnabled(false);
                            }
                        }

                    });
                }
            }
        });
    }

    private void AcceptMessageReq()
    {
        FriendsRef.child(senderUserID).child(receiverUserID).child("Friends").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    FriendsRef.child(receiverUserID).child(senderUserID).child("Friends").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                ChatrequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            FriendsRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    sendMessageRequestBtn.setEnabled(true);
                                                    current_state = "friends";
                                                    sendMessageRequestBtn.setText("Remove friend");
                                                    DeclinemessageRequestBtn.setVisibility(View.INVISIBLE);
                                                    DeclinemessageRequestBtn.setEnabled(false);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelMessageReq()
    {
        ChatrequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    ChatrequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                sendMessageRequestBtn.setEnabled(true);
                                current_state = "new";
                                sendMessageRequestBtn.setText("Send Message");

                                DeclinemessageRequestBtn.setVisibility(View.INVISIBLE);
                                DeclinemessageRequestBtn.setEnabled(false);
                            }
                        }

                });
                }
            }
        });
    }

    private void SendRequest()
    {
//        ChatrequestRef.child(senderUserID).child(receiverUserID)
//                .child("request_type").setValue("sent")
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task)
//                    {
//                        if (task.isSuccessful())
//                        {
//                            ChatrequestRef.child(receiverUserID).child(senderUserID)
//                                    .child("request_type").setValue("received")
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task)
//                                        {
//                                            if (task.isSuccessful())
//                                            {
//                                                HashMap<String,Object> chatNotificationMap = new HashMap<>();
//                                                chatNotificationMap.put("from",senderUserID);
//                                                chatNotificationMap.put("type","request");
//                                                NotificationRef.child(receiverUserID).push().setValue(chatNotificationMap)
//                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                    @Override
//                                                                    public void onComplete(@NonNull Task<Void> task)
//                                                                    {
//                                                                        if (task.isSuccessful())
//                                                                        {
//                                                                            sendMessageRequestBtn.setEnabled(true);
//                                                                            current_state = "request_sent";
//                                                                            sendMessageRequestBtn.setText("Cancel Message Request");
//                                                                        }
//                                                                    }
//                                                                });
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//                });




        ChatrequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ChatrequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestBtn.setEnabled(true);
                                                current_state = "request_sent";
                                                sendMessageRequestBtn.setText("Cancel Message Request");

                                                final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());
                                                database.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                                    {
                                                        Contacts contacts = snapshot.getValue(Contacts.class);
                                                        if (notify)
                                                        {
                                                            sendNotification(receiverUserID, contacts.getName());
                                                        }
                                                        notify = false;
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    private void sendNotification(String ReceiverID, String name)
    {
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(ReceiverID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(firebaseAuth.getUid(),name+ ""+R.string.want_tobe_friend,""+R.string.new_friend_req,ReceiverID,R.drawable.r);

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
}