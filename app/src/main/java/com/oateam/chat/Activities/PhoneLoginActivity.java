package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.oateam.chat.MainActivity;
import com.oateam.chat.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private TextView countDownTV;
    private CountDownTimer countDownTimer;
    private AppCompatButton resendCodebtn, sendCodeBtn,verifyBtn;
    private TextInputEditText countryCode,phonenumber, code1,code2,code3,code4,code5,code6;
    private LinearLayout linearLayout;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        initToolbar();
        countDownTV = (TextView) findViewById(R.id.tv_coundown);

        InitializeFeilds();


        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {


                String phoneNumber = phonenumber.getText().toString() + countryCode.getText().toString();
                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);

                    //inflate view
                    View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                    ((TextView) custom_view.findViewById(R.id.message)).setText("Please enter your phone number");
                    ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                    ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                    toast.setView(custom_view);
                    toast.show();
                }
                else
                {
                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("We are authenticating your phone number...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            120,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            callbacks);

                }
            }

        });


        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                countDownTimer();
                sendCodeBtn.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);

                String verificationCode = code1.getText().toString() + code2.getText().toString() + code3.getText().toString() + code4.getText().toString()+ code5.getText().toString()+ code6.getText().toString();
                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);

                    //inflate view
                    View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                    ((TextView) custom_view.findViewById(R.id.message)).setText("Please write verification code first.");
                    ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                    ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                    toast.setView(custom_view);
                    toast.show();
                }

                else
                {
                    progressDialog.setTitle("Verification Code");
                    progressDialog.setMessage("We are verifying your verification code...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                progressDialog.dismiss();
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);

                //inflate view
                View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                ((TextView) custom_view.findViewById(R.id.message)).setText("Invalid phone number, please enter valid phone number with country code.");
                ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                toast.setView(custom_view);
                toast.show();
                sendCodeBtn.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {



                mVerificationId = verificationId;
                mResendToken = token;
                progressDialog.dismiss();
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);

                //inflate view
                View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                ((TextView) custom_view.findViewById(R.id.message)).setText("Code has been sent");
                ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_error_outline_24);
                ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.blue_500));

                toast.setView(custom_view);
                toast.show();
                sendCodeBtn.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);
            }

        };

    }

    private void InitializeFeilds()
    {
        resendCodebtn = (AppCompatButton) findViewById(R.id.resend_code_btn);
        sendCodeBtn = (AppCompatButton) findViewById(R.id.send_code_btn);
        verifyBtn = (AppCompatButton) findViewById(R.id.verify_btn);
        countryCode = (TextInputEditText) findViewById(R.id.country_code_tv);
        phonenumber = (TextInputEditText) findViewById(R.id.phone_number_tv);
        linearLayout = (LinearLayout) findViewById(R.id.linear_layout_phone);
        code1 = (TextInputEditText) findViewById(R.id.code_1);
        code2 = (TextInputEditText) findViewById(R.id.code_2);
        code3 = (TextInputEditText) findViewById(R.id.code_3);
        code4 = (TextInputEditText) findViewById(R.id.code_4);
        code5 = (TextInputEditText) findViewById(R.id.code_5);
        code6 = (TextInputEditText) findViewById(R.id.code_6);


    }

    private void countDownTimer()
    {
        countDownTimer = new CountDownTimer(1000 * 60 * 2, 1000) {
            @Override
            public void onTick(long l) {
                String text = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(l) % 60);
                countDownTV.setText(text);
            }

            @Override
            public void onFinish() {
                countDownTV.setText("00:00");
                resendCodebtn.setVisibility(View.VISIBLE);

            }
        };
        countDownTimer.start();
    }

    private void initToolbar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "You are logged in successfully.", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}