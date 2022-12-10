package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.oateam.chat.MainActivity;
import com.oateam.chat.R;

public class SignupActivity extends AppCompatActivity {

    private MaterialRippleLayout SignupButton;
    private TextInputEditText email,password;
    private TextView alreadyHaveAnAccount;
    private DatabaseReference rootRef;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFeilds();

        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToSignInActivity();
            }
        });


        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount()
    {
        String userEmail = email.getText().toString();
        String userPass = password.getText().toString();

        if (TextUtils.isEmpty(userEmail))
        {
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);

            //inflate view
            View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
            ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.please_enter_email);
            ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
            ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

            toast.setView(custom_view);
            toast.show();
        }
        if (TextUtils.isEmpty(userPass))
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
        }
        else
        {
            progressDialog.setTitle(R.string.creating_account);
            progressDialog.setMessage(""+R.string.please_wait);
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();


            firebaseAuth.createUserWithEmailAndPassword(userEmail,userPass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference("user.png");
                                String userID = firebaseAuth.getCurrentUser().getUid();
                                rootRef.child("Users").child(userID).child("image").setValue(storageReference.getDownloadUrl().toString());

                                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        Toast toast = new Toast(getApplicationContext());
                                        toast.setDuration(Toast.LENGTH_LONG);

                                        //inflate view
                                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                        ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.email_Sent+""+firebaseUser.getEmail()+""+R.string.please_verify_email);
                                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_done_24);
                                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.green_500));
                                        toast.setView(custom_view);
                                        toast.show();
                                        progressDialog.dismiss();
                                        Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                                        startActivity(i);
                                    }
                                });
                            }
                            else
                            {
                                String error = task.getException().toString();
                                sendUserToMAinActivity();
                                Toast toast = new Toast(getApplicationContext());
                                toast.setDuration(Toast.LENGTH_LONG);

                                //inflate view
                                View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.error+ error);
                                ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                                ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                                toast.setView(custom_view);
                                toast.show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
    }




    private void InitializeFeilds()
    {
        SignupButton = (MaterialRippleLayout) findViewById(R.id.signup_button);
        email = (TextInputEditText) findViewById(R.id.email_signup);
        password = (TextInputEditText) findViewById(R.id.pass_signup);
        alreadyHaveAnAccount = (TextView) findViewById(R.id.already_have_account);
        progressDialog = new ProgressDialog(this);
    }


    private void SendUserToSignInActivity()
    {
        Intent signInIntent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(signInIntent);
    }

    private void sendUserToMAinActivity()
    {

        Intent mainIntent = new Intent(SignupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
}