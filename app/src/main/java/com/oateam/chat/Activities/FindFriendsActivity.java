package com.oateam.chat.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import io.reactivex.rxjava3.annotations.NonNull;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView friendsList;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        friendsList = (RecyclerView) findViewById(R.id.find_friends_rec_list);
        friendsList.setLayoutManager(new LinearLayoutManager(this));
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        toolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> adapter  = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@androidx.annotation.NonNull FindFriendsViewHolder holder, @SuppressLint("RecyclerView") int position, @androidx.annotation.NonNull Contacts model)
            {
                String hisUID = getRef(position).getKey();

                if (!hisUID.equals(currentUserID))
                {
                    holder.username.setText(model.getName());
                    holder.bio.setText(model.getBio());
                    Picasso.get().load(model.getImage()).placeholder(R.drawable.user).into(holder.profileImage);

                    holder.blockIv.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    checkIsBlocked(hisUID, holder, position,model);
                }

                else
                {
                    holder.itemView.setVisibility(View.GONE);
                }


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        String visitUSerID = getRef(position).getKey();
                        checkIsBlockedOrNot(visitUSerID);

                    }
                });


                holder.blockIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {

                        if (model.isBlocked())
                        {
                           UnBlockUser(hisUID);
                        }
                        else
                        {
                            BlockUser(hisUID);
                        }
                    }
                });
            }




            @androidx.annotation.NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout,  parent, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };

        friendsList.setAdapter(adapter);
        adapter.startListening();
    }

    private void checkIsBlockedOrNot(String hisUID)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(hisUID).child("Blocked Users").orderByChild("uid").equalTo(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot)
                    {
                        for(DataSnapshot ds : snapshot.getChildren())
                        {
                            if (ds.exists())
                            {
                                Toast toast = new Toast(getApplicationContext());
                                toast.setDuration(Toast.LENGTH_LONG);

                                //inflate view
                                View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.blocked_by_user);
                                ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                                ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                                toast.setView(custom_view);
                                toast.show();
                                return;
                            }
                        }

                        Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("visitUserID", hisUID);
                        startActivity(profileIntent);

                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUID, FindFriendsViewHolder holder, int position, Contacts model)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(currentUserID).child("Blocked Users").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot)
                    {
                        for(DataSnapshot ds : snapshot.getChildren())
                        {
                            if (ds.exists())
                            {
                                holder.blockIv.setImageResource(R.drawable.ic_baseline_check_circle_24);
                                model.setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error)
                    {

                    }
                });
    }

    private void BlockUser(String hisUID)
    {
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid",hisUID);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(currentUserID).child("Blocked Users").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        Toast.makeText(FindFriendsActivity.this, R.string.blocked_success, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e)
                    {
                        Toast.makeText(FindFriendsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UnBlockUser(String hisUID)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(currentUserID).child("Blocked Users").orderByChild("uid").equalTo(hisUID).addListenerForSingleValueEvent(new ValueEventListener() {
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
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                Toast.makeText(FindFriendsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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


    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {

        TextView username,bio;
        CircularImageView profileImage;
        ImageView blockIv;
        public  FindFriendsViewHolder(@NonNull View itemview)
        {
            super(itemview);

            username = itemview.findViewById(R.id.username_users);
            bio = itemview.findViewById(R.id.bio_users);
            profileImage = itemview.findViewById(R.id.img_users);
            blockIv = itemview.findViewById(R.id.blockIV);


        }
    }
}