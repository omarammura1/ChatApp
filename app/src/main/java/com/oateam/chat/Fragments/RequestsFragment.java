package com.oateam.chat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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


public class RequestsFragment extends Fragment {


    private View requestFragmentView;
    private RecyclerView recyclerView;
    private DatabaseReference reference,Ureference,FriendsRef;
    private FirebaseAuth auth;
    private RelativeLayout EmptyLayout;
    private String currrentUserId;
    public RequestsFragment() {
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
        requestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);
        Ureference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        auth = FirebaseAuth.getInstance();
        currrentUserId = auth.getCurrentUser().getUid();

        recyclerView = (RecyclerView) requestFragmentView.findViewById(R.id.message_req_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        EmptyLayout = (RelativeLayout) requestFragmentView.findViewById(R.id.noRequestsLayout);





        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(reference.child(currrentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestsFragment.RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull Contacts model)
            {
                holder.itemView.findViewById(R.id.accept_req_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.decline_req_btn).setVisibility(View.VISIBLE);
                Button btn_accept = holder.itemView.findViewById(R.id.accept_req_btn);
                Button btn_dec = holder.itemView.findViewById(R.id.decline_req_btn);


                final String listuserID = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if (snapshot.exists())
                        {
                            String type = snapshot.getValue().toString();
                            if (type.equals("received"))
                            {
                                Ureference.child(listuserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {

                                        if (snapshot.hasChild("image"))
                                        {
                                            final String profileImage__ = snapshot.child("image").getValue().toString();
                                            Picasso.get().load(profileImage__).placeholder(R.drawable.user).into(holder.profileImage);

                                        }

                                        final String profileBio__  = snapshot.child("bio").getValue().toString();
                                        final String profileName__= snapshot.child("name").getValue().toString();

                                        holder.username.setText(profileName__);
                                        holder.bio.setText("Want to be your friend.");




                                        btn_accept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                FriendsRef.child(currrentUserId).child(listuserID).child("Friends").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            FriendsRef.child(listuserID).child(currrentUserId).child("Friends").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        reference.child(currrentUserId).child(listuserID).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if (task.isSuccessful())
                                                                                        {
                                                                                            reference.child(currrentUserId).child(listuserID).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {
                                                                                                            reference.child(currrentUserId).child(listuserID).removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                                        {
                                                                                                                            if (task.isSuccessful())
                                                                                                                            {
                                                                                                                                reference.child(listuserID).child(currrentUserId).removeValue()
                                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                            @Override
                                                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                                                            {
                                                                                                                                                if (task.isSuccessful())
                                                                                                                                                {
                                                                                                                                                    Toast toast = new Toast(getActivity().getApplicationContext());
                                                                                                                                                    toast.setDuration(Toast.LENGTH_LONG);

                                                                                                                                                    //inflate view
                                                                                                                                                    View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                                                                                                                                    ((TextView) custom_view.findViewById(R.id.message)).setText(profileName__ + ""+R.string.added_to_list);
                                                                                                                                                    ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_error_outline_24);
                                                                                                                                                    ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.blue_500));

                                                                                                                                                    toast.setView(custom_view);
                                                                                                                                                    toast.show();
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        });
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
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
                                        });

                                        btn_dec.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                reference.child(currrentUserId).child(listuserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    reference.child(listuserID).child(currrentUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        Toast toast = new Toast(getActivity().getApplicationContext());
                                                                                        toast.setDuration(Toast.LENGTH_LONG);

                                                                                        //inflate view
                                                                                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                                                                        ((TextView) custom_view.findViewById(R.id.message)).setText(profileName__ + ""+R.string.deleted_from_list);
                                                                                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_error_outline_24);
                                                                                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.blue_500));

                                                                                        toast.setView(custom_view);
                                                                                        toast.show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        });


                                        /**
                                        * THIS IS ALERTDIALOG YOU CAN ENABLE IT*/



//                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
//                                                CharSequence options[] = new CharSequence[]
//                                                        {
//                                                                "Accept","Cancel"
//                                                        };
//                                                AlertDialog.Builder builder =  new AlertDialog.Builder(getContext());
//                                                builder.setTitle(profileName__ + "Message Request");
//
//                                                builder.setItems(options, new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialogInterface, int i)
//                                                    {
//                                                        if (i == 0)
//                                                        {
//
//                                                        }
//
//                                                        if (i == 1)
//                                                        {
//
//                                                        }
//                                                    }
//                                                });
//
//                                                builder.show();
//                                            }
//                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }


                            else if (type.equals("sent"))
                            {
                                Button request_send_btn = holder.itemView.findViewById(R.id.accept_req_btn);
                                request_send_btn.setBackgroundColor(getResources().getColor(R.color.red_600));
                                request_send_btn.setText("Cancel Request");

                                holder.itemView.findViewById(R.id.decline_req_btn).setVisibility(View.INVISIBLE);


                                Ureference.child(listuserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {
                                        if (snapshot.hasChild("image"))
                                        {
                                            final String profileImage__ = snapshot.child("image").getValue().toString();
                                            Picasso.get().load(profileImage__).placeholder(R.drawable.user).into(holder.profileImage);

                                        }

                                        final String profileBio__  = snapshot.child("bio").getValue().toString();
                                        final String profileName__= snapshot.child("name").getValue().toString();

                                        holder.username.setText(profileName__);
                                        holder.bio.setText("You have sent request to " + profileName__);

                                        btn_accept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                reference.child(currrentUserId).child(listuserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    reference.child(listuserID).child(currrentUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        Toast toast = new Toast(getActivity().getApplicationContext());
                                                                                        toast.setDuration(Toast.LENGTH_LONG);

                                                                                        //inflate view
                                                                                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                                                                        ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.msg_req_canceled);
                                                                                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_error_outline_24);
                                                                                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.blue_500));

                                                                                        toast.setView(custom_view);
                                                                                        toast.show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout,parent,false);
                RequestsViewHolder holder = new RequestsViewHolder(view);
                return holder;
            }
        };


        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,bio;
        CircularImageView profileImage;
        Button Accept,Decline;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username_users);
            bio = itemView.findViewById(R.id.bio_users);
            profileImage = itemView.findViewById(R.id.img_users);
            Accept = itemView.findViewById(R.id.accept_req_btn);
            Decline = itemView.findViewById(R.id.decline_req_btn);
        }
    }
}