package com.example.sstep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class Register extends AppCompatActivity {
    Button submitBtn;
    EditText username, password, repeatPass, age;
    Switch maleOrFemale;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        submitBtn = (Button) findViewById(R.id.button3);
        username = (EditText)findViewById(R.id.editTextTextPersonName3);
        password = (EditText) findViewById(R.id.editTextTextPassword);
        repeatPass = (EditText) findViewById(R.id.editTextTextEmailAddress);
        age = (EditText) findViewById(R.id.editTextNumber);
        maleOrFemale = (Switch) findViewById(R.id.switch1);

        db = Database.getDbObject();
        submitBtn.setOnClickListener(this::submit);
    }

    void submit(View v){
        Log.d("mesaj","submit user in database");
        HashMap<String, Object> newUser = new HashMap<>();

        if(!password.getText().toString().equals(repeatPass.getText().toString())){
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        newUser.put("username",username.getText().toString());
        newUser.put("password",password.getText().toString());
        newUser.put("age",age.getText().toString());

        if(maleOrFemale.isChecked())
            newUser.put("gen", "F");
        else
            newUser.put("gen", "M");
        newUser.put("photo","");

        db.collection("users")
                .add(newUser)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Register.this);
                        SharedPreferences.Editor edit = preferences.edit();
                        edit.putString("username", newUser.get("username").toString());
                        edit.apply();
                        Intent i = new Intent(Register.this, GenerateProfile.class);
                        startActivity(i);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                    }
                });
    }
}