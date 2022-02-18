package com.example.sstep;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;

public class GenerateCV extends AppCompatActivity {

    TabLayout menu;
    Button generateCvBtn;
    EditText name, number,education, experience, email, fileName;
    int pageWidth = 1200;
    int pageHeight = 2010;
    int pageNumber = 1;
    FirebaseFirestore db;
    //Bitmap bmp,scaledBmp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_cv);
        menu = (TabLayout) findViewById(R.id.menu);
        generateCvBtn = findViewById(R.id.button6);
        name = findViewById(R.id.editTextTextPersonName4);
        number = findViewById(R.id.editTextPhone);
        education = findViewById(R.id.editTextTextMultiLine2);
        experience = findViewById(R.id.editTextTextMultiLine3);
        email = findViewById(R.id.editTextTextEmailAddress2);
        fileName = findViewById(R.id.editTextTextPersonName5);

        db = Database.getDbObject();

        TabLayout.Tab tab = menu.getTabAt(1);
        menu.selectTab(tab);
        //bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);
        //scaledBmp = Bitmap.createScaledBitmap(bmp, 1200, 2010, false);
        menu.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = menu.getSelectedTabPosition();
                switch (position){
                    case 0:
                        Intent i = new Intent(GenerateCV.this, MainActivity.class);
                        startActivity(i);
                        break;
                    case 1:
                        Toast.makeText(GenerateCV.this, "Esti deja pe acest tab", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (checkPermission()) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        generateCvBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateCVpdf();
            }
        });

    }


    void generateCVpdf(){

        if(name.getText().toString().length() == 0 ||
            number.getText().toString().length() == 0 ||
            email.getText().toString().length() == 0 ||
            education.getText().toString().length() == 0 ||
            experience.getText().toString().length() == 0) {

            Toast.makeText(this, "Trebuie toate campurile completate", Toast.LENGTH_SHORT).show();
            return;
        }

        //Toast.makeText(GenerateCV.this, "click", Toast.LENGTH_SHORT).show();
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint contactPaint = new Paint();
        Paint subtitlePaint = new Paint();

        subtitlePaint.setTextSize(25);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //basic document settings
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page myPage = document.startPage(myPageInfo);
        Canvas canvas = myPage.getCanvas();

        //configurare titlu
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(30);
        canvas.drawText(name.getText().toString(), pageWidth/2,100, titlePaint);

        //configurare contact telefon
        contactPaint.setTextAlign(Paint.Align.CENTER);
        contactPaint.setTextSize(20);
        canvas.drawText("Telefon : " + number.getText().toString(), pageWidth/2 , 150, contactPaint);

        //configurare contact email
        canvas.drawText("Email : " + email.getText().toString(), pageWidth/2 , 200, contactPaint);

        //configurare contact locatie
        String city = preferences.getString("city","n/a");
        canvas.drawText("Locatie : " + city, pageWidth/2 , 250, contactPaint);

        //header pt educatie
        canvas.drawText("Education ", pageWidth/4 - 100, 300, subtitlePaint);
        //contentul pt educatie
        canvas.drawText(education.getText().toString(), pageWidth/5, 350, contactPaint);

        //header pt work experience
        canvas.drawText("Work Experience", pageWidth/4 - 100, 400, subtitlePaint);
        canvas.drawText(experience.getText().toString(), pageWidth/5, 450, contactPaint);


        String pref_userName = preferences.getString("username", "n/a");

        //get the picture from the server
        db.collection("users").whereEqualTo("username", pref_userName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    //set the image into image view
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    StorageReference photoReference= storageReference.child("images/"+documentSnapshot.getString("photo"));
                    final long ONE_MEGABYTE = 5 * 1024 * 1024;
                    photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap scaledbmp = Bitmap.createScaledBitmap(bmp, 200, 200, false);
                            canvas.drawBitmap(scaledbmp, pageWidth/2 + 200, 0, paint );
                            document.finishPage(myPage);
                            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                    "/" + fileName.getText().toString() + ".pdf");

                            try{
                                document.writeTo(new FileOutputStream(file));
                                Toast.makeText(GenerateCV.this, "CV generat cu succes", Toast.LENGTH_SHORT).show();
                                document.close();
                                displayNotification("CV generat cu succes ", "Click aici pentru a deschide pdf-ul", file);
                            }
                            catch (Exception exception){
                                Toast.makeText(GenerateCV.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getApplicationContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });


    }

    //verificam daca userul permite scrierea
    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    //cerem permisiune pentru scriere fisier
    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 200);
    }

    private void displayNotification(String titlu, String mesaj, File file){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //daca api-ul folosit e mai mare de oreo
            NotificationChannel nf = new NotificationChannel("my notification","my notification name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(nf);
        }

        //pregatim intentul care se va activa la click-ul pe notificare
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //setam intentul sa poata deschide un fisier pdf (fisierul pe care il specificam noi)
        intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID  + ".provider", file) ,
                                       "application/pdf");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"my notification");
        mBuilder
                .setContentTitle(titlu)
                .setContentText(mesaj)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(PendingIntent.FLAG_UPDATE_CURRENT, mBuilder.build());
    }

}