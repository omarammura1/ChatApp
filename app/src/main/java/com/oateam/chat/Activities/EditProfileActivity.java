package com.oateam.chat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.oateam.chat.MainActivity;
import com.oateam.chat.R;
import com.squareup.picasso.Picasso;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private Button updateButton;
    private TextInputEditText username,bio;
    private CircularImageView profileImage;
    private AdView adView;
    private String UserID;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private static final int GalleryPick = 1;
    private StorageReference UserProfilePics;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        InitializeFeilds();


        firebaseAuth = FirebaseAuth.getInstance();
        UserID = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        UserProfilePics = FirebaseStorage.getInstance().getReference().child("Profile Images");
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();



        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = findViewById(R.id.adView_profile);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateProfile();
            }
        });

        RetrieveUserInfo();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent,"Select Image"), 123);
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
//                    StorageReference storageReference = UserProfilePics.child(UserID + ".jpg");

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
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (!firebaseUser.isEmailVerified())
                    {

                        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                Toast.makeText(EditProfileActivity.this, R.string.email_not_verified+"" + firebaseUser.getEmail()+""+R.string.check_spam, Toast.LENGTH_LONG).show();
                                sendUserToLoginActivity();
                            }
                        });
                    }

                    else
                    {
                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);

                        //inflate view
                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                        ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.set_profile);
                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_error_outline_24);
                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.blue_500));

                        toast.setView(custom_view);
                        toast.show();
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode== RESULT_OK && data!=null) {

            progressDialog.setTitle("Setting profile Image");
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


            Uri imageUri = data.getData();
            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            Bitmap imageAfterRotation = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageAfterRotation.compress(Bitmap.CompressFormat.JPEG, 35, baos);
            byte[] datasa = baos.toByteArray();


//           Bitmap bitmap = BitmapFactory.decodeByteArray(datasa, 0, datasa.length);
//            Bitmap b = RotateBitmap(bitmap,-90);
//            ByteArrayOutputStream boo = new ByteArrayOutputStream();
//            b.compress(Bitmap.CompressFormat.JPEG, 100, boo);
//            byte[] ddd = boo.toByteArray();




            StorageReference filePath = UserProfilePics.child(UserID + ".jpg");

            filePath.putBytes(datasa).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);

                        //inflate view
                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                        ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.profile_image_updated);
                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_done_24);
                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.green_500));

                        toast.setView(custom_view);
                        toast.show();

                     filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                         @Override
                         public void onSuccess(Uri uri)
                         {
                             rootRef.child("Users").child(UserID).child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()) {

                                         progressDialog.dismiss();
                                     } else {
                                         progressDialog.dismiss();
                                     }
                                 }
                             });
                         }
                     });
                    }

                    else
                    {
                        progressDialog.dismiss();
                        String message = task.getException().toString();

                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);

                        //inflate view
                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                        ((TextView) custom_view.findViewById(R.id.message)).setText(message);
                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                        toast.setView(custom_view);
                        toast.show();
                    }
                }
            });
        }
    }







    private void UpdateProfile()
    {
        String setUsername = username.getText().toString();
        String setBio = bio.getText().toString();

        if (TextUtils.isEmpty(setUsername))
        {
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);

            //inflate view
            View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
            ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.write_name);
            ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
            ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

            toast.setView(custom_view);
            toast.show();
        }
        if (TextUtils.isEmpty(setBio))
        {
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);

            //inflate view
            View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
            ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.write_bio);
            ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
            ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

            toast.setView(custom_view);
            toast.show();
        }

        else
        {
            StorageReference storage = FirebaseStorage.getInstance().getReference();
            storage.child("user.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String imageUrl = uri.toString();
                    HashMap<String,String> profileMap = new HashMap<>();
                    profileMap.put("uid",UserID);
                    profileMap.put("name",setUsername);
                    profileMap.put("bio",setBio);
                    profileMap.put("image",imageUrl);
                    rootRef.child("Users").child(UserID).setValue(profileMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        rootRef.child("Users").child(UserID).child("userState").child("typingTo").setValue("noOne");

                                        Toast toast = new Toast(getApplicationContext());
                                        toast.setDuration(Toast.LENGTH_LONG);

                                        //inflate view
                                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                        ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.profile_updated);
                                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_baseline_done_24);
                                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.green_500));

                                        toast.setView(custom_view);
                                        toast.show();
                                        sendUserToMainActivity();

                                    }
                                    else
                                    {
                                        String error = task.getException().toString();
                                        Toast toast = new Toast(getApplicationContext());
                                        toast.setDuration(Toast.LENGTH_LONG);

                                        //inflate view
                                        View custom_view = getLayoutInflater().inflate(R.layout.toast_icon_text, null);
                                        ((TextView) custom_view.findViewById(R.id.message)).setText(R.string.error+ "" + error);
                                        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
                                        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(getResources().getColor(R.color.red_600));

                                        toast.setView(custom_view);
                                        toast.show();
                                    }

                                }
                            });
                }
            });



        }
    }
    private void sendUserToMainActivity()
    {

        Intent mainIntent = new Intent(EditProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }

    private void sendUserToLoginActivity()
    {
        firebaseAuth.signOut();
        Intent mainIntent = new Intent(EditProfileActivity.this, LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }



    private void InitializeFeilds()
    {
        updateButton = (Button) findViewById(R.id.update_profile_btn);
        username = (TextInputEditText) findViewById(R.id.username_edittext);
        bio  = (TextInputEditText) findViewById(R.id.bio_edittext);
        profileImage = (CircularImageView) findViewById(R.id.circle_image_edit_profile);
        progressDialog = new ProgressDialog(this);
    }


    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}