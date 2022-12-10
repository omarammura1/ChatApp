package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oateam.chat.Adapters.AdapterAddParticipant;
import com.oateam.chat.MainActivity;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PGroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private String myRole = "";
    private FirebaseAuth firebaseAuth;
    private ActionBar actionBar;
    private TextView createdBy,groupdesc,editGroup,addParti,participants,leaveGroup;
    private RecyclerView partiList;
    private ImageView groupImageInfo;
    private ArrayList<Contacts> userList;
    private AdapterAddParticipant adapterAddParticipant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pgroup_info);

        groupId = getIntent().getStringExtra("groupId");



        createdBy = findViewById(R.id.createdBy);
        groupdesc = findViewById(R.id.descriptionTv);
        editGroup = findViewById(R.id.editGroup);
        addParti = findViewById(R.id.addPartiGroup);
        participants = findViewById(R.id.participantsNUM);
        groupImageInfo = findViewById(R.id.groupImageInfo);
        leaveGroup = findViewById(R.id.leaveGroup);
        partiList = findViewById(R.id.partiss);


        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupRole();

        addParti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PGroupInfoActivity.this, PGroupAddParticipantActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "";
                String description = "";
                String positiveBTN = "";
                if (myRole.equals("creator"))
                {
                    title = String.valueOf(R.string.delete_group);
                    description = String.valueOf(R.string.sure_delete_group);
                    positiveBTN = String.valueOf(R.string.delete);
                }

                else
                {
                    title = String.valueOf(R.string.leave_group);
                    description = String.valueOf(R.string.leave_group_text);
                    positiveBTN = String.valueOf(R.string.leave);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(PGroupInfoActivity.this);
                builder.setTitle(title)
                        .setMessage(description)
                        .setPositiveButton(positiveBTN, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {

                                if (myRole.equals("creator"))
                                {
                                   DeleteGroup();
                                }

                                else
                                {
                                    LeaveGroup();
                                }
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PGroupInfoActivity.this, EditGroupActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
    }

    private void LeaveGroup()
    {
        DatabaseReference rrr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rrr.child(groupId).child("Participants").child(firebaseAuth.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        Toast.makeText(PGroupInfoActivity.this, R.string.group_left_success, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PGroupInfoActivity.this, MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PGroupInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void DeleteGroup()
    {
        DatabaseReference rrr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rrr.child(groupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused)
            {
                Toast.makeText(PGroupInfoActivity.this, R.string.group_delete_success, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PGroupInfoActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(PGroupInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupRole()
    {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        reference.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    myRole = ds.child("role").getValue().toString();
//                    actionBar.setSubtitle(myRole);
                    if (myRole.equals("participants"))
                    {
                        editGroup.setVisibility(View.GONE);
                        addParti.setVisibility(View.GONE);
                        leaveGroup.setText(R.string.leave_group);

                    }
                    else if (myRole.equals("admin"))
                    {
                        editGroup.setVisibility(View.GONE);
                        addParti.setVisibility(View.VISIBLE);
                        leaveGroup.setText(R.string.leave_group);
                    }
                    else if (myRole.equals("creator"))
                    {
                        editGroup.setVisibility(View.VISIBLE);
                        addParti.setVisibility(View.VISIBLE);
                        leaveGroup.setText("Delete Group");
                    }
                }
                
                loadParticipants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    private void loadParticipants()
    {
        userList = new ArrayList<>();
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        ref1.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot dds : snapshot.getChildren())
                {
                    String uid = dds.child("uid").getValue().toString();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                    databaseReference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            for (DataSnapshot ds : snapshot.getChildren())
                            {
                                Contacts contacts = ds.getValue(Contacts.class);
                                userList.add(contacts);
                            }
                            adapterAddParticipant = new AdapterAddParticipant(PGroupInfoActivity.this,userList,groupId,myRole);
                            partiList.setAdapter(adapterAddParticipant);
                            participants.setText("Participants ("+userList.size()+")");
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
                    String createee = ds.child("createdBy").getValue().toString();
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                    localCreatorInfo(dateTime,createee);

                    groupdesc.setText(groupDescription);
                    try {
                        Picasso.get().load(groupImage).placeholder(R.drawable.ic_baseline_group_24).into(groupImageInfo);
                    }catch (Exception e)
                    {
                        groupImageInfo.setImageResource(R.drawable.ic_baseline_group_24);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void localCreatorInfo(String dateTime, String createee)
    {
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
        ref1.orderByChild("uid").equalTo(createee).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    String name = ds.child("name").getValue().toString();
                    createdBy.setText("Created by "+name+" on "+dateTime);
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