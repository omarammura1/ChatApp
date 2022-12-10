package com.oateam.chat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.internal.TextDrawableHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oateam.chat.Activities.PGroupChatActivity;
import com.oateam.chat.Models.ModelPGroupChatList;
import com.oateam.chat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterPGroupChatList extends  RecyclerView.Adapter<AdapterPGroupChatList.HolderGroupChatList>{

    private Context context;
    private ArrayList<ModelPGroupChatList> groupChatArrayList;

    public AdapterPGroupChatList(Context context, ArrayList<ModelPGroupChatList> groupChatArrayList) {
        this.context = context;
        this.groupChatArrayList = groupChatArrayList;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_groupchat,parent,false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position)
    {
        ModelPGroupChatList modelPGroupChatList = groupChatArrayList.get(position);
        final String groupID = modelPGroupChatList.getGID();
        String groupFImage = modelPGroupChatList.getGroupImage();
        String groupFName = modelPGroupChatList.getGroupName();
        holder.messageSender.setText("");
        holder.time.setText("");
        holder.message.setText("");
        loadLastMessage(modelPGroupChatList,holder);

        holder.groupName.setText(groupFName);



        try {
            Picasso.get().load(groupFImage).placeholder(R.drawable.ic_baseline_group_24).into(holder.groupImage);
        }catch (Exception e)
        {
            holder.groupImage.setImageResource(R.drawable.ic_baseline_group_24);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(context, PGroupChatActivity.class);
                intent.putExtra("groupId", groupID);
                intent.putExtra("groupImage",groupFImage );
                intent.putExtra("groupName", groupFName);
                context.startActivity(intent);
            }
        });



    }

    private void loadLastMessage(ModelPGroupChatList modelPGroupChatList, HolderGroupChatList holder)
    {
        DatabaseReference rrr = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        rrr.child(modelPGroupChatList.getGID()).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    String messagess = ds.child("message").getValue().toString();
                    String timestamppp = ds.child("date").getValue().toString();
                    String senderee = ds.child("sender").getValue().toString();
                    String type = ds.child("type").getValue().toString();
                    //convert Time (((timestamp)))


                    if (type.equals("image"))
                    {
                        holder.message.setText("Sent Photo");
                    }
                    else if (type.equals("text"))
                    {
                        holder.message.setText(messagess);
                    }


                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.orderByChild("uid").equalTo(senderee).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            for (DataSnapshot ds : snapshot.getChildren())
                            {
                                String name = ds.child("name").getValue().toString();
                                holder.messageSender.setText(name+":");
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
    public int getItemCount() {
        return groupChatArrayList.size();
    }

    class HolderGroupChatList extends RecyclerView.ViewHolder{
        private ImageView groupImage,unread;
        private TextView groupName,messageSender,message,time;



        public HolderGroupChatList(@NonNull View itemView)
        {


            super(itemView);
            groupImage = itemView.findViewById(R.id.imageview_groups_item);
            unread = itemView.findViewById(R.id.image_unread_messages);
            groupName = itemView.findViewById(R.id.tv_groupsName_item);
            messageSender = itemView.findViewById(R.id.tv_groupsSenderName_item);
            message = itemView.findViewById(R.id.tv_groupsMessage_item);
            time = itemView.findViewById(R.id.tv_groupstimeMessage_item);

        }
    }
}
