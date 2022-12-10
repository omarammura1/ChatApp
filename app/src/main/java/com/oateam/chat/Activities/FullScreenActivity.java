package com.oateam.chat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.oateam.chat.R;
import com.squareup.picasso.Picasso;

public class FullScreenActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView username;
    private ImageButton back;
    private String receiver , image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        receiver = getIntent().getExtras().getString("username_full").toString();
        image = getIntent().getExtras().getString("image_full").toString();

        imageView = (ImageView) findViewById(R.id.image_fullscreen);
        username = (TextView) findViewById(R.id.username_fullscreen);
        back = (ImageButton) findViewById(R.id.back_fullscreen);

        username.setText(receiver);
        Picasso.get().load(image).placeholder(R.drawable.user).into(imageView);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}