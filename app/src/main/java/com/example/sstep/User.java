package com.example.sstep;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class User {
    String username,password;
    Object id;

    public User(String username, String password, Object id){
        this.username = username;
        this.password = password;
        this.id = id;
    }

    public User(){

    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    //input : db : Firestore : connection to the database
    //        givenUsername : String : the username of the searched user
    //output : returns one user with this username
    public static void getByUsername(FirebaseFirestore db, String givenUsername, Callback callback){

        db.collection("users")
                .whereEqualTo("username",givenUsername).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("info", document.getId() + " => " + document.getData());
                                callback.onCallback(new User(document.getString("username"), document.getString("password"), document.getId()));
                            }
                        } else {
                            Log.w("info", "Error getting documents.", task.getException());
                        }
                    }

                });
    }


}
