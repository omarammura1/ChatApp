package com.oateam.chat.Fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oateam.chat.Adapters.ChatListAdapter;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.Models.Messages;
import com.oateam.chat.Models.ModelChatList;
import com.oateam.chat.R;
import com.oateam.chat.Adapters.TabsAccessorAdapter;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private View view;
    private RecyclerView recyclerView;

    private DatabaseReference reference,Ureference;
    private FirebaseAuth auth;
    private String currrentUserId;
    List<ModelChatList> chatLists;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter accessorAdapter;
    List<Contacts> contacts;
    private FloatingActionButton contactsBtn;
    ChatListAdapter chatListAdapter;


    public ChatsFragment() {
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

        view =  inflater.inflate(R.layout.fragment_chats, container, false);

//        viewPager = view.findViewById(R.id.main_tab_pager);
//        tabLayout = view.findViewById(R.id.main_tab);
        auth = FirebaseAuth.getInstance();
        chatLists = new ArrayList<>();
//        currrentUserId = auth.getCurrentUser().getUid();
        recyclerView = (RecyclerView)view.findViewById(R.id.chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Ureference = FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseUser user = auth.getCurrentUser();
        if (user != null)
        {
            reference = FirebaseDatabase.getInstance().getReference().child("ChatList").child(auth.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    chatLists.clear();
                    for (DataSnapshot ds: snapshot.getChildren())
                    {
                        ModelChatList modelChatList = ds.getValue(ModelChatList.class);
                        chatLists.add(modelChatList);
                    }
                    loadChats();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        return view;
    }

    private void loadChats()
    {
        contacts = new ArrayList<>();
        Ureference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                contacts.clear();
                Messages messages = snapshot.getValue(Messages.class);
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    Contacts users = ds.getValue(Contacts.class);
                    for (ModelChatList chat : chatLists)
                    {
                        if (users.getUid() != null && users.getUid().equals(chat.getId()))
                        {
                            contacts.add(users);
                            break;
                        }
                    }
                    chatListAdapter = new ChatListAdapter(getContext(), contacts);

                    recyclerView.setAdapter(chatListAdapter);

                    for (int i = 0; i<contacts.size(); i++)
                    {
                        lastMessage(contacts.get(i).getUid());
                    }

//                    recyclerView.setVisibility(chatLists.isEmpty() ? View.GONE : View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId)
    {

        FirebaseUser user = auth.getCurrentUser();
        if (user != null)
        {
            currrentUserId = auth.getCurrentUser().getUid();
        }


        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        DatabaseReference messagesRef = db.child("Messages");
        DatabaseReference messageReceiverIdRef = messagesRef.child(currrentUserId).child(userId);
        messageReceiverIdRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        String messageId = ds.child("messageID").getValue(String.class);

                        assert messageId != null;
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Messages").child(currrentUserId).child(userId).child(messageId);
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                String theLastMessage = "default";
                                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                {
                                    Messages messages = snapshot.getValue(Messages.class);
                                    if (messages == null)
                                    {
                                        continue;
                                    }
                                    String sender = messages.getFrom();
                                    String receiver = messages.getTo();

                                    if (sender == null || receiver==null)
                                    {
                                        continue;
                                    }
                                    if (messages.getTo().equals(currrentUserId) && messages.getFrom().equals(userId) || messages.getTo().equals(userId) && messages.getFrom().equals(currrentUserId) )
                                    {

                                        if (messages.getType().equals("text"))
                                        {
                                            theLastMessage = messages.getMessage();
                                        }
                                        else
                                        {
                                            theLastMessage = "sent "+messages.getType();
                                        }


                                    }
                                }

                                chatListAdapter.setLastMessageMap(userId, theLastMessage);
                                chatListAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    Log.d("TAG", task.getException().getMessage()); //Never ignore potential errors!
                }
            }
        });



    }




}