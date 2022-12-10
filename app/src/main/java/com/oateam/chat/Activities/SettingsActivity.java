package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oateam.chat.BuildConfig;
import com.oateam.chat.Fragments.BlockedUsersFragment;
import com.oateam.chat.Fragments.PrivacyPolicyFragment;
import com.oateam.chat.R;
import com.oateam.chat.Utilities.Tools;
import com.squareup.picasso.Picasso;

public class SettingsActivity extends AppCompatActivity {

    private TextView username,bio,appVersion;
    private ImageView profileImage;
    private String UserID;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private Toolbar mToolBar;
    private FloatingActionButton float_edit_profile;
    private LinearLayout Privacy,About,ChangePass,BlockedList,AskQuestion;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseAuth = FirebaseAuth.getInstance();
        UserID = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();




        InitializeFeilds();

        appVersion.setText(BuildConfig.VERSION_NAME);

        RetrieveUserInfo();

        Privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(SettingsActivity.this, PrivacyPolicyFrag.class);
                startActivity(i);

            }
        });

        About.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogAbout();
            }
        });
        AskQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogAskQues();
            }
        });


        ChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowChangePasswordDialog();
            }
        });

        BlockedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, BlockedFrag.class);
                startActivity(i);
            }
        });


        float_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToEditProfileActivity();
            }
        });



    }

    private void showDialogAskQues() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_ask_question);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });



        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void ShowChangePasswordDialog()
    {
        View view = getLayoutInflater().inflate(R.layout.dialog_update_password,null);
        final EditText current_passwordET = view.findViewById(R.id.current_passwordET_update);
              EditText new_passwordET = view.findViewById(R.id.new_passwordET_update);
              EditText re_new_passwordET = view.findViewById(R.id.new_passwordET_update_repeat);
              Button updatePassBtn = view.findViewById(R.id.updatePasswordBtn);

        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
        updatePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String currentPass = current_passwordET.getText().toString().trim();
                String newPass = new_passwordET.getText().toString().trim();
                String reNewentPass = re_new_passwordET.getText().toString().trim();
                if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass)  || TextUtils.isEmpty(reNewentPass))
                {
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);

                    //inflate view
                    View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                    ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.please_enter_pass);
                    ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                    ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                    toast.setView(custom_view);
                    toast.show();

                    if (newPass.length() < 6 || reNewentPass.length() < 6)
                    {
                        Toast toast1 = new Toast(getApplicationContext());
                        toast1.setDuration(Toast.LENGTH_LONG);

                        //inflate view
                        View custom_view1 = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                        ((TextView) custom_view1.findViewById(R.id.message)).setText(R.string.password_length_limit);
                        ((ImageView) custom_view1.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                        ((CardView) custom_view1.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                        toast1.setView(custom_view1);
                        toast1.show();

                    }


                }
                else
                {
                    if (newPass.equals(reNewentPass))
                    {
                        dialog.dismiss();
                        updatePassword(currentPass, newPass);
                    }
                    else
                    {
                        Toast toast11 = new Toast(getApplicationContext());
                        toast11.setDuration(Toast.LENGTH_LONG);

                        //inflate view
                        View custom_view11 = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                        ((TextView) custom_view11.findViewById(R.id.message)).setText(R.string.pass_doesnot_match);
                        ((ImageView) custom_view11.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                        ((CardView) custom_view11.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                        toast11.setView(custom_view11);
                        toast11.show();
                    }
                }
            }
        });

    }

    private void updatePassword(String currentPass, String newPass)
    {

        progressDialog.setTitle("Updating Your Password");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused)
            {
                user.updatePassword(newPass).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        progressDialog.dismiss();
                        Toast toast1 = new Toast(getApplicationContext());
                        toast1.setDuration(Toast.LENGTH_LONG);

                        //inflate view
                        View custom_view1 = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                        ((TextView) custom_view1.findViewById(R.id.message)).setText(R.string.password_update_success);
                        ((ImageView) custom_view1.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_error_outline_24);
                        ((CardView) custom_view1.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.blue_600));

                        toast1.setView(custom_view1);
                        toast1.show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void RetrieveUserInfo()
    {
        rootRef.child("Users").child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if ((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image"))))
                {
                    String retreiveUsername = snapshot.child("name").getValue().toString();
                    String retreiveBio = snapshot.child("bio").getValue().toString();
                    String retreiveProfileImage = snapshot.child("image").getValue().toString();

                    username.setText(retreiveUsername);
                    bio.setText(retreiveBio);
                    Picasso.get().load(retreiveProfileImage).placeholder(R.drawable.user).into(profileImage);

                }
                else if ((snapshot.exists()) && (snapshot.hasChild("name")))
                {
                    String retreiveUsername = snapshot.child("name").getValue().toString();
                    String retreiveBio = snapshot.child("bio").getValue().toString();
                    username.setText(retreiveUsername);
                    bio.setText(retreiveBio);

                }
                else
                {

                    Toast.makeText(SettingsActivity.this,R.string.set_profile,Toast.LENGTH_SHORT);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFeilds()
    {
        username = (TextView) findViewById(R.id.profile_username);
        bio  = (TextView) findViewById(R.id.profile_bio);
        profileImage = (ImageView) findViewById(R.id.image_set_profile);
        appVersion  = (TextView) findViewById(R.id.app_version);
        About = (LinearLayout)findViewById(R.id.about_lay) ;
        AskQuestion = (LinearLayout)findViewById(R.id.ask_question) ;
        BlockedList = (LinearLayout)findViewById(R.id.blocked_friends_lay) ;
        Privacy = (LinearLayout)findViewById(R.id.privacy_policy_lay) ;
        ChangePass = (LinearLayout)findViewById(R.id.change_pass_lay) ;
        float_edit_profile = findViewById(R.id.float_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settt);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ProgressDialog progressDialog = new ProgressDialog(this);
    }




    private void SendUserToEditProfileActivity()
    {
        Intent editIntent = new Intent(SettingsActivity.this, EditProfileActivity.class);
        startActivity(editIntent);

    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(SettingsActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }



    private void showDialogAbout() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_about);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ((TextView) dialog.findViewById(R.id.tv_version)).setText("Version " + BuildConfig.VERSION_NAME);

        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        ((Button) dialog.findViewById(R.id.bt_portfolio)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.openInAppBrowser(SettingsActivity.this, "https://omarammura.me", false);
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    public static class BlockedFrag extends FragmentActivity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .add(android.R.id.content, new BlockedUsersFragment()).commit();}
        }
    }


    public static class PrivacyPolicyFrag extends FragmentActivity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .add(android.R.id.content, new PrivacyPolicyFragment()).commit();}
        }
    }
}