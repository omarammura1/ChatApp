package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.oateam.chat.MainActivity;
import com.oateam.chat.R;

public class LoginActivity extends AppCompatActivity {


    private Button phoneLoginButton;
    private MaterialRippleLayout LoginButton;
    private TextInputEditText email,password;
    private TextView forgetPassword,needNewAccount;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference reference;
    private Task<String> deviceToken;
    private FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFeilds();

        needNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToSignupActivity();
            }

        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                UserLogin();

            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowForgetPasswordDialog();
            }
        });


//        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//                Intent phoneloginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
//                startActivity(phoneloginIntent);
//
//            }
//        });
    }

    private void ShowForgetPasswordDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout = new LinearLayout(this);
        EditText editText = new EditText(this);
        editText.setHint("Email");
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setMinEms(10);
        linearLayout.addView(editText);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Revocer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = editText.getText().toString().trim();
                beingRecovery(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    private void beingRecovery(String email)
    {
        progressDialog.setMessage("Sending E-mail...");
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                progressDialog.dismiss();
                if (task.isSuccessful())
                {
                    Toast.makeText(LoginActivity.this, R.string.email_sent_password_success, Toast.LENGTH_SHORT).show();

                }
                else {
                    Toast.makeText(LoginActivity.this, R.string.email_failed_toSent, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }


    private void UserLogin()
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

            progressDialog.setTitle("Signing in");
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            firebaseAuth.signInWithEmailAndPassword(userEmail,userPass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                String currentUserID = firebaseAuth.getCurrentUser().getUid();
//                                deviceToken = FirebaseMessaging.getInstance().getToken();
//                                reference.child(currentUserID).child("device_token").setValue(deviceToken)
//                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task)
//                                                    {
//                                                        if (task.isSuccessful())
//                                                        {
//
//                                                        }
//                                                    }
//                                                });

                                sendUserToMainActivity();
                                Toast toast = new Toast(getApplicationContext());
                                toast.setDuration(Toast.LENGTH_LONG);

                                //inflate view
                                View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.login_success);
                                ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_done_24);
                                ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.green_500));

                                toast.setView(custom_view);
                                toast.show();
                                progressDialog.dismiss();


                            }

                            else
                            {
                                String error = task.getException().toString();
                                Toast toast = new Toast(getApplicationContext());
                                toast.setDuration(Toast.LENGTH_LONG);

                                //inflate view
                                View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.error+"" + error);
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
        LoginButton = (MaterialRippleLayout) findViewById(R.id.login_button);
//        phoneLoginButton = (Button) findViewById(R.id.login_via_phone);
        email = (TextInputEditText) findViewById(R.id.email_login);
        password = (TextInputEditText) findViewById(R.id.pass_login);
        forgetPassword = (TextView) findViewById(R.id.forgot_password);
        needNewAccount = (TextView) findViewById(R.id.need_new_account);

        progressDialog = new ProgressDialog(this);

    }


    private void sendUserToMainActivity()
    {

        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }


    private void SendUserToSignupActivity()
    {
        Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(signupIntent);
    }
}