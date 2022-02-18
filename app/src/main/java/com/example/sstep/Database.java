package com.example.sstep;

import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class Database {
    static FirebaseFirestore db = null;

    public static FirebaseFirestore getDbObject(){
        if (db == null) //daca e null si nu a mai fost instantiat
            db = FirebaseFirestore.getInstance();//;atunci il initializam
        return db; //altfel doar il returnam
    }

}
