package com.oateam.chat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.oateam.chat.Activities.ChatActivity;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.Models.Messages;
import com.oateam.chat.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.myHolder>
{
    Context context;
    List<Contacts> userList;
    private HashMap<String,String> lastMessageMap;
    private List<Messages> userMessagesList;


    public ChatListAdapter(Context context, List<Contacts> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();

    }


    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chatlist,parent,false);
        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position)
    {
        String messageReceiverID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userNNNAME = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(messageReceiverID);
        holder.username.setText(userNNNAME);

        if (lastMessage==null || lastMessage.equals("default"))
        {
            holder.lastMessageTV.setVisibility(View.GONE);
        }
        else
        {

            holder.lastMessageTV.setVisibility(View.VISIBLE);
            holder.lastMessageTV.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.user).into(holder.profileImage);

        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.user).into(holder.profileImage);
        }



        DatabaseReference Ureference = FirebaseDatabase.getInstance().getReference("Users");
        Ureference.child(messageReceiverID).addValueEventListener(new ValueEventListener() {
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
                                    holder.onlineStatus.setVisibility(View.VISIBLE);
                                }
                                if (state.equals("offline"))
                                {

                                    holder.onlineStatus.setVisibility(View.GONE);
                                }

                            }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FirebaseAuth auth = FirebaseAuth.getInstance();

//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");
//          reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot)
//            {
//                int unread = 0;
//                for (DataSnapshot ds : snapshot.getChildren())
//                {
//                    Messages messages = ds.getValue(Messages.class);
//                    assert messages != null;
//                    if (messages.getFrom().equals(messageReceiverID) && !messages.isSeen())
//                    {
//                        unread++;
//                    }
//                }
//
//                if (unread == 0)
//                {
//                    holder.undreCard.setVisibility(View.GONE);
//                    holder.unreadText.setVisibility(View.GONE);
//                }
//                else
//                {
//                    holder.undreCard.setVisibility(View.VISIBLE);
//                    holder.unreadText.setVisibility(View.VISIBLE);
//                    holder.unreadText.setText(unread);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


//        if (userList.get(position).getState().equals("online"))
//        {
//            holder.onlineStatus.setVisibility(View.VISIBLE);
//        }
//        else
//        {
//            holder.onlineStatus.setVisibility(View.GONE);
//        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Intent chatInt = new Intent(context, ChatActivity.class);
                chatInt.putExtra("visit_user_id",messageReceiverID);
                chatInt.putExtra("visit_user_name",userNNNAME);
                chatInt.putExtra("visit_user_image", userImage);
                context.startActivity(chatInt);

            }
        });
    }


    public void setLastMessageMap(String userID,String lastMESSAGE)
    {
        lastMessageMap.put(userID,lastMESSAGE);
    }



    @Override
    public int getItemCount() {
        return userList.size();
    }

    class myHolder extends RecyclerView.ViewHolder
    {

        CircularImageView profileImage, onlineStatus;
        TextView username,lastMessageTV,unreadText;
        CardView undreCard;

        public myHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage = itemView.findViewById(R.id.img_chatlist);
            onlineStatus = itemView.findViewById(R.id.online_dot_chatlist);
            username = itemView.findViewById(R.id.username_chatlist);
            lastMessageTV = itemView.findViewById(R.id.last_message_chatlist);
//            undreCard = itemView.findViewById(R.id.unread_card);
//            unreadText = itemView.findViewById(R.id.unread_msgs_chatList);


        }
    }
}
