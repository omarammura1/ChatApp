package com.oateam.chat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oateam.chat.Adapters.AdapterPGroupChatList;
import com.oateam.chat.Models.ModelPGroupChatList;
import com.oateam.chat.R;

import java.util.ArrayList;


public class PrivateGroupsFragment extends Fragment {


    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelPGroupChatList> groupChatArrayList;
    private AdapterPGroupChatList adapterPGroupChatList;



    public PrivateGroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View view =  inflater.inflate(R.layout.fragment_private_groups, container, false);


       recyclerView = view.findViewById(R.id.groupsRecycler);
       firebaseAuth = FirebaseAuth.getInstance();
       loadGroupChatLists();
       return view;
    }

    private void loadGroupChatLists()
    {
        groupChatArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                groupChatArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists())
                    {
                        ModelPGroupChatList modelPGroupChatList = ds.getValue(ModelPGroupChatList.class);
                        groupChatArrayList.add(modelPGroupChatList);

                    }
                }
                adapterPGroupChatList = new AdapterPGroupChatList(getActivity(), groupChatArrayList);
                recyclerView.setAdapter(adapterPGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void searchGroupChatLists(String query)
    {
        groupChatArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("PrivateGroups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                groupChatArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists())
                    {
                        if (ds.child("groupName").toString().toLowerCase().contains(query.toLowerCase()))
                        {
                            ModelPGroupChatList modelPGroupChatList = ds.getValue(ModelPGroupChatList.class);
                            groupChatArrayList.add(modelPGroupChatList);
                        }
                    }
                }
                adapterPGroupChatList = new AdapterPGroupChatList(getActivity(), groupChatArrayList);
                recyclerView.setAdapter(adapterPGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}