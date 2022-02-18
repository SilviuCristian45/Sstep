package com.example.sstep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    Button loginBtn,gotoRegisterbtn;
    EditText username,password;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        loginBtn = (Button) findViewById(R.id.button);
        gotoRegisterbtn = (Button) findViewById(R.id.button2);
        loginBtn.setOnClickListener(this::loginUser);
        gotoRegisterbtn.setOnClickListener(this::switchRegisterPage);
        db = Database.getDbObject();
    }

    /**
     * validate the input given by user and switch to another activity if needed
     *
     * @param  user     the user data we get async from the firestore database
     * @param  password the password input given by the user
     * @return          nothing --- just set the app behaviour and validating input
     */
    void accesUserDatacb(User user, EditText password) {
        if (user.getPassword().equals(password.getText().toString())) {//and check if the passwword matches
            //salvare a username-ului user-ului curent
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("username", user.getUsername());
            edit.putString("userid", user.getId().toString());
            edit.apply();
            //redirect la pagina principala
            Intent goToHome = new Intent(Login.this, MainActivity.class);
            startActivity(goToHome);
        } else {
            Toast.makeText(Login.this, "username password not correct", Toast.LENGTH_SHORT).show();
        }
    }


    void loginUser(View v){
        username = (EditText) findViewById(R.id.editTextTextPersonName);
        password = (EditText) findViewById(R.id.editTextTextPersonName2);
        User.getByUsername(db, username.getText().toString(), new Callback() {
            @Override
            public void onCallback(User user) {
                accesUserDatacb(user, password);
            }
        });
    }

    void switchRegisterPage(View v){
        Intent gotoRegister = new Intent(Login.this, Register.class);
        startActivity(gotoRegister);
    }

}