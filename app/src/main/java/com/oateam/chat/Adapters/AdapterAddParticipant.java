package com.oateam.chat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.R;
import com.oateam.chat.notifications.Data;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.annotations.NonNull;

public class AdapterAddParticipant extends RecyclerView.Adapter<AdapterAddParticipant.HolderAddParticipant>{
    private Context context;
    private ArrayList<Contacts> userList;
    private String GID,myRole;

    public AdapterAddParticipant(Context context, ArrayList<Contacts> userList, String groupId, String myRole) {
        this.context = context;
        this.userList = userList;
        this.GID = groupId;
        this.myRole = myRole;
    }

    @androidx.annotation.NonNull
    @Override
    public HolderAddParticipant onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_participant,parent,false);
        return new HolderAddParticipant(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull HolderAddParticipant holder, int position)
    {
        Contacts model = userList.get(position);
        String name = model.getName();
        String bioM = model.getBio();
        String image = model.getImage();
        String uid = model.getUid();

        holder.username.setText(name);
        holder.bio.setText(bioM);

        try {
            Picasso.get().load(image).placeholder(R.drawable.user).into(holder.profileImage);
        }catch (Exception e)
        {
            holder.profileImage.setImageResource(R.drawable.user);
        }

        checkIfAlreadyExists(model,holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PrivateGroups");
                databaseReference.child(GID).child("Participants").child(model.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists())
                        {
                            String hispreviousRole = snapshot.child("role").getValue().toString();

                            String[] options;

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Choose option");
                            if (myRole.equals("creator"))
                            {
                                if (hispreviousRole.equals("admin"))
                                {
                                    options = new String[]{"Dismiss Admin","Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0)
                                            {
                                                removeAdmin(model);
                                            }
                                            else
                                            {
                                                removeParticipant(model);
                                            }
                                        }
                                    }).show();
                                }

                                else if (hispreviousRole.equals("participant"))
                                {
                                    options = new String[]{"Make group admin", "Remove user"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0)
                                            {
                                                makeAdmin(model);
                                            }
                                            else
                                            {
                                                removeParticipant(model);
                                            }
                                        }
                                    }).show();
                                }
                            }
                            else if (myRole.equals("admin"))
                            {
                                if (hispreviousRole.equals("creator"))
                                {
                                    Toast.makeText(context, R.string.creator_of_group, Toast.LENGTH_SHORT).show();
                                }
                                else if (hispreviousRole.equals("admin"))
                                {
                                    options = new String[]{String.valueOf(R.string.make_admin), String.valueOf(R.string.remove_user)};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0)
                                            {
                                                makeAdmin(model);
                                            }
                                            else
                                            {
                                                removeParticipant(model);
                                            }
                                        }
                                    }).show();
                                }
                                else if (hispreviousRole.equals("participant"))
                                {
                                    options = new String[]{String.valueOf(R.string.make_admin), String.valueOf(R.string.remove_user)};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0)
                                            {
                                                makeAdmin(model);
                                            }
                                            else
                                            {
                                                removeParticipant(model);
                                            }
                                        }
                                    }).show();
                                }
                            }
                        }
                        else
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(R.string.add_participants)
                                    .setMessage(R.string.add_user_to_group_quest)
                                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            addParticipant(model);
                                        }
                                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void addParticipant(Contacts model)
    {
        String timeStamp = ""+System.currentTimeMillis();
        HashMap<String ,String> hashMap = new HashMap<>();
        hashMap.put("uid", model.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timeStamp);

        DatabaseReference rrrr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rrrr.child(GID).child("Participants").child(model.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, R.string.added_success, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e)
            {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void makeAdmin(Contacts model)
    {
        HashMap<String ,Object> hashMap1 = new HashMap<>();
        hashMap1.put("role", "admin");

        DatabaseReference rrrr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rrrr.child(GID).child("Participants").child(model.getUid()).updateChildren(hashMap1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, R.string.user_is_Admin, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e)
            {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeParticipant(Contacts model)
    {
        DatabaseReference rrrr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rrrr.child(GID).child("Participants").child(model.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, R.string.user_removed, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e)
            {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(Contacts model)
    {
        HashMap<String ,Object> hashMap1 = new HashMap<>();
        hashMap1.put("role", "participant");

        DatabaseReference rrrr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rrrr.child(GID).child("Participants").child(model.getUid()).updateChildren(hashMap1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, R.string.no_longer_admin, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e)
            {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkIfAlreadyExists(Contacts model, HolderAddParticipant holder)
    {
        DatabaseReference rff = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rff.child(GID).child("Participants").child(model.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    String hisRole = snapshot.child("role").getValue().toString();
                    holder.who.setText(hisRole);
                }
                else
                {
                    holder.who.setText("");
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderAddParticipant extends RecyclerView.ViewHolder{

        private CircularImageView profileImage,onlineDot;
        private TextView username,bio,who;

        public HolderAddParticipant(@NonNull View itemView){
            super(itemView);


            profileImage = itemView.findViewById(R.id.img_parti);
            onlineDot = itemView.findViewById(R.id.online_dot_parti);
            username = itemView.findViewById(R.id.username_parti);
            bio = itemView.findViewById(R.id.bio_parti);
            who = itemView.findViewById(R.id.who_parti);
        }
    }
}
