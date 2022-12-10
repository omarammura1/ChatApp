package com.oateam.chat.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.oateam.chat.Activities.ChatActivity;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class ContactsFragment extends Fragment {

    private View friendsView;
    private RecyclerView myFriendsList;
    private DatabaseReference FriendsRef,UsersRef;
    private FirebaseAuth firebaseAuth;
    private String currentUSerID;


    public ContactsFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        friendsView =  inflater.inflate(R.layout.fragment_contacts, container, false);

        myFriendsList = (RecyclerView)friendsView.findViewById(R.id.friends_list);
        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseAuth = FirebaseAuth.getInstance();
        currentUSerID = firebaseAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUSerID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");



        return friendsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(FriendsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull Contacts model)
            {
                final String userIDs = getRef(position).getKey();
                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists())
                        {
                            if (snapshot.child("userState").hasChild("state"))
                            {
                                String state = snapshot.child("userState").child("state").getValue().toString();
                                String lastSeenTT = snapshot.child("userState").child("lastSeen").getValue().toString();

                                if (state.equals("online"))
                                {
                                    holder.onlineStatusDot.setVisibility(View.VISIBLE);
                                }
                                if (state.equals("offline"))
                                {

//                                    String timeAgo = calculateTime(lastSeenTT);
//                                    holder.bio.setText("last seen "+timeAgo);
                                    holder.onlineStatusDot.setVisibility(View.INVISIBLE);

                                }

                            }

                            else
                            {
                                holder.onlineStatusDot.setVisibility(View.INVISIBLE);
                            }


                            if (snapshot.hasChild("image"))
                            {
                                String profileImage_ = snapshot.child("image").getValue().toString();
                                String profileName_ = snapshot.child("name").getValue().toString();
                                String profileBio_  = snapshot.child("bio").getValue().toString();

                                Picasso.get().load(profileImage_).placeholder(R.drawable.user).into(holder.profileImage);
                                holder.username.setText(profileName_);
                                holder.bio.setText(profileBio_);


                            }

                            else
                            {
                                String profileBio_  = snapshot.child("bio").getValue().toString();
                                String profileName_ = snapshot.child("name").getValue().toString();

                                holder.username.setText(profileName_);
                                holder.bio.setText(profileBio_);
                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {

                                        String profileName_ = snapshot.child("name").getValue().toString();
                                        String profileImage_ = snapshot.child("image").getValue().toString();
                                        Intent chatInt = new Intent(getContext(), ChatActivity.class);
                                        chatInt.putExtra("visit_user_id",userIDs);
                                        chatInt.putExtra("visit_user_name", profileName_);
                                        chatInt.putExtra("visit_user_image", profileImage_);
                                        startActivity(chatInt);

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout,parent,false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };

        myFriendsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,bio;
        CircularImageView profileImage,onlineStatusDot;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username_users);
            bio = itemView.findViewById(R.id.bio_users);
            profileImage = itemView.findViewById(R.id.img_users);
            onlineStatusDot = itemView.findViewById(R.id.online_dot_users);
        }
    }


    private String calculateTime(String lastSeenTT)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            long time = sdf.parse(lastSeenTT).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago+"";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

}