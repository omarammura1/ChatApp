package com.oateam.chat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.oateam.chat.R;

public class HomeActivity extends AppCompatActivity {

    private Button login,signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        login = findViewById(R.id.login_home);
        signup = findViewById(R.id.signup_home);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signupIntent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(signupIntent);
                finish();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent signupIntent = new Intent(HomeActivity.this, SignupActivity.class);
                    startActivity(signupIntent);
                    finish();

            }
        });
    }
}