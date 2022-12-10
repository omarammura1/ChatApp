package com.oateam.chat.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
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

import java.util.Objects;


public class BlockedUsersFragment extends Fragment {



    private View blockedView;
    private RecyclerView myBlockedList;
    private DatabaseReference blockedRef,usersRef;
    private FirebaseAuth firebaseAuth;
    private String currentUSerID;
    private AdView adView;


    public BlockedUsersFragment() {
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
        blockedView = inflater.inflate(R.layout.fragment_blocked_users, container, false);

        Toolbar toolbar = blockedView.findViewById(R.id.blocked_list_toolbar);

        toolbar.setTitle("Blocked friends");


        myBlockedList = (RecyclerView)blockedView.findViewById(R.id.blocked_list);
        myBlockedList.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseAuth = FirebaseAuth.getInstance();
        currentUSerID = firebaseAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        blockedRef = usersRef.child(currentUSerID).child("Blocked Users");

        MobileAds.initialize(blockedView.getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = blockedView.findViewById(R.id.adView_profile);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return blockedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(blockedRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,BlockedViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, BlockedViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BlockedViewHolder holder, int position, @NonNull Contacts model) {
                holder.blockIV.setVisibility(View.GONE);

                final String userIDs = getRef(position).getKey();

                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
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
            public BlockedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout,parent,false);
                BlockedUsersFragment.BlockedViewHolder viewHolder = new BlockedViewHolder(view);
                return viewHolder;
            }
        };


        myBlockedList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class BlockedViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,bio;
        CircularImageView profileImage,onlineStatusDot;
        ImageView blockIV;

        public BlockedViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username_users);
            bio = itemView.findViewById(R.id.bio_users);
            profileImage = itemView.findViewById(R.id.img_users);
            onlineStatusDot = itemView.findViewById(R.id.online_dot_users);
            blockIV = itemView.findViewById(R.id.blockIV);


        }
    }


}