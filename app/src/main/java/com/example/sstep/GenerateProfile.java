package com.example.sstep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.annotation.Nullable;

public class GenerateProfile extends AppCompatActivity {
    Button selectImage,generateProfile;
    Uri imageURI;
    ImageView imageView;
    StorageReference storageReference;
    FirebaseFirestore db;
    EditText description;
    String usernameLogged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_profile);
        selectImage = (Button) findViewById(R.id.button4);
        imageView = (ImageView) findViewById(R.id.imageView);
        description = (EditText) findViewById(R.id.editTextTextMultiLine);
        generateProfile = (Button) findViewById(R.id.button5);
        generateProfile.setOnClickListener(this::generateProfileClick);
        selectImage.setOnClickListener(this::selectImageClick);
        db = Database.getDbObject();
    }

    void selectImageClick(View v){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && data != null && data.getData() != null){
            imageURI = data.getData();
            //.makeText(this, imageURI.toString());
            Toast.makeText(GenerateProfile.this, imageURI.toString(), Toast.LENGTH_SHORT).show();
            imageView.setImageURI(imageURI);
        }
    }

    void generateProfileClick(View v){

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String filename = formatter.format(now);

        storageReference = FirebaseStorage.getInstance().getReference("images/"+filename);
        storageReference.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GenerateProfile.this);
                usernameLogged = preferences.getString("username", "n/a");
                Toast.makeText(GenerateProfile.this, usernameLogged, Toast.LENGTH_SHORT).show();
                db.collection("users").whereEqualTo("username", usernameLogged).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                            HashMap <String, Object> update = new HashMap<>();
                            update.put("photo", filename);
                            update.put("description", description.getText().toString());
                            Toast.makeText(GenerateProfile.this, document.getId() + " " + filename, Toast.LENGTH_SHORT).show();
                            db.collection("users").document(document.getId()).set(update, SetOptions.merge());
                        }
                        imageView.setImageURI(null); //se face update-ul
                        Toast.makeText(GenerateProfile.this, "update succesfully", Toast.LENGTH_SHORT).show();
                        Intent gotoHome = new Intent(GenerateProfile.this, MainActivity.class);
                        startActivity(gotoHome);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GenerateProfile.this, "update not succes on photo uri", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GenerateProfile.this, "update NOT succesfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
}