package com.oateam.chat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oateam.chat.Models.ModelPGroupChat;
import com.oateam.chat.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AdapterPGroupChat extends RecyclerView.Adapter<AdapterPGroupChat.HolderGroupChat>
{
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelPGroupChat> modelPGroupChats;

    private FirebaseAuth firebaseAuth;

    public AdapterPGroupChat(Context context, ArrayList<ModelPGroupChat> modelPGroupChats) {
        this.context = context;
        this.modelPGroupChats = modelPGroupChats;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.groupchat_right,parent,false);
            return new HolderGroupChat(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.groupchat_left,parent,false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position)
    {
        ModelPGroupChat model = modelPGroupChats.get(position);
        String messageSS = model.getMessage();
        String TS = model.getDate();
        String UID = model.getSender();
        String messageType = model.getType();

        if (messageType.equals("text"))
        {
            holder.image.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(messageSS);
        }
        else if (messageType.equals("image"))
        {
            holder.image.setVisibility(View.VISIBLE);
            holder.message.setVisibility(View.GONE);
            try {

                Picasso.get().load(messageSS).placeholder(R.drawable.ic_baseline_image_24).into(holder.image);
            }catch (Exception e)
            {
                holder.image.setImageResource(R.drawable.ic_baseline_image_24);
            }
        }

        SimpleDateFormat inputDate  = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        SimpleDateFormat outputDate = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        inputDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = inputDate.parse(TS);
            assert date != null;
            outputDate.setTimeZone(TimeZone.getDefault());
            String finalTime = outputDate.format(date);


            holder.time.setText(finalTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        setUserName(model,holder);
    }

    @Override
    public int getItemViewType(int position) {
        if (modelPGroupChats.get(position).getSender().equals(firebaseAuth.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }
    }

    private void setUserName(ModelPGroupChat model, HolderGroupChat holder)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    String namessss = ds.child("name").getValue().toString();
                    holder.name.setText(namessss);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return modelPGroupChats.size();
    }

    class HolderGroupChat extends RecyclerView.ViewHolder{
        private TextView name,message,time;
        private ImageView image;

        public HolderGroupChat(@NonNull View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.message_name_groups);
            message = itemView.findViewById(R.id.message_content_chat_groups);
            time = itemView.findViewById(R.id.text_time_groups);
            image = itemView.findViewById(R.id.messageIv);

        }
    }

}
