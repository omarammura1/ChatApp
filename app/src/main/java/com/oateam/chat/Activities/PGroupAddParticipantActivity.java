package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oateam.chat.Adapters.AdapterAddParticipant;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.R;

import java.util.ArrayList;

public class PGroupAddParticipantActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private ActionBar actionBar;
    private String groupId, myRole;
    private ArrayList<Contacts> userList;
    private AdapterAddParticipant adapterAddParticipant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pgroup_add_participant);
//        actionBar = getSupportActionBar();
//        actionBar.setTitle("Add Participants");
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.parti_list);
        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();

    }

    private void getAllFriends()
    {
        userList = new ArrayList<>();
        DatabaseReference reeee = FirebaseDatabase.getInstance().getReference("Friends").child(firebaseAuth.getUid());
        reeee.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    userList.clear();
                    Contacts contacts =  ds.getValue(Contacts.class);
                    if (!firebaseAuth.getUid().equals(contacts.getUid()))
                    {
                        userList.add(contacts);
                    }
                }
                adapterAddParticipant = new AdapterAddParticipant(PGroupAddParticipantActivity.this,userList,groupId,myRole);
                recyclerView.setAdapter(adapterAddParticipant);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo()
    {
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("PrivateGroups");
        ref.orderByChild("GID").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    String groupId = ds.child("GID").getValue().toString();
                    final String groupname = ds.child("GID").getValue().toString();
                    String groupName = ds.child("groupName").getValue().toString();
                    String groupDescription = ds.child("groupDescription").getValue().toString();
                    String groupImage = ds.child("groupImage").getValue().toString();
                    String timestamp = ds.child("timestamp").getValue().toString();
//                    actionBar.setTitle("Add Participants");

                    ref1.child(groupId).child("Participants").child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if (snapshot.exists())
                            {
                                myRole = snapshot.child("role").getValue().toString();
//                                actionBar.setTitle(groupname + "("+myRole+")");

                                getAllFriends();
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}