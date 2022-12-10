package com.oateam.chat;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.oateam.chat.Activities.EditProfileActivity;
import com.oateam.chat.Activities.FindFriendsActivity;
import com.oateam.chat.Activities.GroupCreateActivity;
import com.oateam.chat.Activities.HomeActivity;
import com.oateam.chat.Activities.LoginActivity;
import com.oateam.chat.Activities.SettingsActivity;
import com.oateam.chat.Adapters.TabsAccessorAdapter;
import com.oateam.chat.Fragments.ChatsFragment;
import com.oateam.chat.Fragments.ContactsFragment;
import com.oateam.chat.Fragments.GroupsFragment;
import com.oateam.chat.Fragments.PrivateGroupsFragment;
import com.oateam.chat.Fragments.RequestsFragment;
import com.oateam.chat.Models.Contacts;
import com.oateam.chat.Utilities.Tools;
import com.oateam.chat.notifications.Token;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    private BottomNavigationView navigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.keepSynced(true);

        final ChatsFragment chatsFragment = new ChatsFragment();
        final GroupsFragment groupsFragment = new GroupsFragment();
        final ContactsFragment contactsFragment = new ContactsFragment();
        final RequestsFragment requestsFragment = new RequestsFragment();
        final PrivateGroupsFragment privateGroupsFragment  = new PrivateGroupsFragment();


        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);




        navigation = (BottomNavigationView) findViewById(R.id.navigationaaa);


        navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment fragment = new Fragment();
                switch (item.getItemId())
                {
                    case R.id.action_chats:
                        fragment = chatsFragment;
                        item.isChecked();
                        break;
                    case R.id.action_friends:
                        fragment = contactsFragment;
                        item.isChecked();
                        break;
                    case R.id.action_requests:
                        fragment = requestsFragment;
                        item.isChecked();
                        break;
                    case R.id.action_groups:
                        fragment = privateGroupsFragment;
                        item.isChecked();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.rrcontainersss, fragment).commit();
                return false;
            }
        });




        navigation.setSelectedItemId(R.id.action_chats);


        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                String token2232 = task.getResult();
                updateToken(token2232);
            }
        });



    }

    



    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
        if (firebaseUser != null)
        {
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", currentUserID);
            editor.apply();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseUser == null)
        {
            SendUserToHomeActivity();
        }
        else
        {
            updateUserStatus("online");
            VerifyUser();

        }

    }



    @Override
    protected void onRestart() {
        super.onRestart();
        updateUserStatus("online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (firebaseUser != null)
        {
            updateUserStatus("offline");
        }
    }




    private void updateUserStatus(String state)
    {
        String lastSeenTime;
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);

        SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        lastSeenTime = dateFormat.format(calendar.getTime());
        HashMap<String,Object> onlineStatemap = new HashMap<>();
        onlineStatemap.put("lastSeen", lastSeenTime);
        onlineStatemap.put("state", state);
        if (firebaseUser != null )
        {
            currentUserID = firebaseAuth.getCurrentUser().getUid();
            RootRef.child("Users").child(currentUserID).child("userState").updateChildren(onlineStatemap);

        }

    }



    private void VerifyUser()
    {
        String userID = firebaseAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.child("name").exists())
                {

                }
                else
                {
                    SendUserToEditProfileActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void SendUserToHomeActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, HomeActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        Contacts contacts = new Contacts();

        startActivity(settingsIntent);

    }

    private void SendUserToEditProfileActivity()
    {
        Intent editProfileIntent = new Intent(MainActivity.this, EditProfileActivity.class);
        editProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(editProfileIntent);
        finish();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options, menu);
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);


        if (item.getItemId() == R.id.main_find_freinds_option)
        {
            SendUserToFindFriendsActivity();
        }

        if (item.getItemId() == R.id.main_settings_option)
        {
            SendUserToSettingsActivity();
        }

        if (item.getItemId() == R.id.main_create_group_option)
        {
           startActivity(new Intent(MainActivity.this, GroupCreateActivity.class));
        }
//
//        if (item.getItemId() == R.id.main_logout_option)
//        {
//
//            firebaseAuth.signOut();
//            updateUserStatus("offline");
//            SendUserToLoginActivity();
//        }
        if (item.getItemId() == android.R.id.home) {
            //finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
//        return true;
    }


    public void onMenuClick(View view) {
//        hideMenu(content_view);
    }

    private void SendUserToFindFriendsActivity()
    {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

//    private void CreateNewGroup()
//    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
//        builder.setTitle("Enter group name:");
//
//        final EditText groupNameEditText = new EditText(MainActivity.this);
//        groupNameEditText.setHint("e.g John's Family");
//        builder.setView(groupNameEditText);
//
//        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                String groupName = groupNameEditText.getText().toString();
//                if (TextUtils.isEmpty(groupName))
//                {
//
//                    Toast toast = new Toast(getApplicationContext());
//                    toast.setDuration(Toast.LENGTH_LONG);
//
//                    //inflate view
//                    View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
//                    ((TextView) custom_view.findViewById(R.id.message)).setText("Please enter group name.");
//                    ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
//                    ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));
//
//                    toast.setView(custom_view);
//                    toast.show();
//                }
//                else
//                {
//                    NewGroup(groupName);
//                }
//            }
//        });
//
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i)
//            {
//                dialogInterface.cancel();
//            }
//        });
//
//        builder.show();
//    }

    private void NewGroup(String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast toast = new Toast(getApplicationContext());
                            toast.setDuration(Toast.LENGTH_LONG);

                            //inflate view
                            View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                            ((TextView) custom_view.findViewById(R.id.message)).setText(groupName+ ""+ R.string.created_success);
                            ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_done_24);
                            ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.green_500));

                            toast.setView(custom_view);
                            toast.show();
                        }
                    }
                });
    }




    public void updateToken(String token)
    {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        if (firebaseUser != null ) {
            currentUserID = firebaseAuth.getCurrentUser().getUid();
            ref.child(currentUserID).setValue(token1);
        }
    }



}