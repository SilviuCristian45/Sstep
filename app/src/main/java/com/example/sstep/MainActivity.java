package com.example.sstep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.location.LocationRequest;
import com.google.firestore.v1.WriteResult;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "tag";
    // Create a new user with a first and last name
    HashMap<String, Object> user = new HashMap<>();
    TextView title,description,city;
    FirebaseFirestore db;
    String pref_userName;
    TabLayout menu;
    ImageView profile;
    LocationRequest locationRequest;
    double latitude, longitude;
    String userid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = Database.getDbObject();
        title = (TextView) findViewById(R.id.welcomeview);
        menu = (TabLayout) findViewById(R.id.menu);
        description = (TextView) findViewById(R.id.textView9);
        profile = (ImageView) findViewById(R.id.imageView2);
        city = (TextView) findViewById(R.id.textView11);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        pref_userName = preferences.getString("username", "n/a");
        userid = preferences.getString("userid","n/a");
        title.setText(title.getText().toString() + pref_userName);

        //location request
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        menu.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = menu.getSelectedTabPosition();
                switch (position){
                    case 0:
                        Intent i = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(i);
                        break;
                    case 1:
                        Toast.makeText(MainActivity.this, "tab2", Toast.LENGTH_SHORT).show();
                        Intent ii = new Intent(MainActivity.this, GenerateCV.class);
                        startActivity(ii);
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
        //set the description for this user from the database
        //pref_username
        db.collection("users").whereEqualTo("username", pref_userName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    //Toast.makeText(MainActivity.this, documentSnapshot.getString("description"), Toast.LENGTH_SHORT).show();
                    description.setText(documentSnapshot.getString("description"));
                    //set the image into image view
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    StorageReference photoReference= storageReference.child("images/"+documentSnapshot.getString("photo"));
                    final long FIVE_MEGABYTES = 5*1024*1024;
                    photoReference.getBytes(FIVE_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            profile.setImageBitmap(bmp);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getApplicationContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        //set the latitude and longitude
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(isGPSenabled()){
                    //Toast.makeText(MainActivity.this, "gps enabled ? " + isGPSenabled(), Toast.LENGTH_SHORT).show();
                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);
                                    if(locationResult != null && locationResult.getLocations().size() > 0){
                                        //Toast.makeText(MainActivity.this, "location result + "+locationResult.getLocations().size(), Toast.LENGTH_SHORT).show();
                                        int index = locationResult.getLocations().size()-1;
                                        latitude = locationResult.getLocations().get(index).getLatitude();
                                        longitude = locationResult.getLocations().get(index).getLongitude();
                                        Toast.makeText(MainActivity.this, "latitude " + latitude + " longitude " + longitude, Toast.LENGTH_SHORT).show();
                                        //setam coordonatele obtinute
                                        DocumentReference documentReference = db.collection("users").document(userid);
                                        Map<String, Object> h = new HashMap<String,Object>();
                                        h.put("latitude",latitude);
                                        h.put("longitude", longitude);
                                        documentReference.update(h).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(MainActivity.this, "salut", Toast.LENGTH_SHORT).show();
                                                SharedPreferences.Editor edit = preferences.edit();
                                                edit.putString("city", getLocationName(latitude,longitude));
                                                edit.apply();
                                                city.setText("Your city " + getLocationName(latitude, longitude));
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                System.out.println(e.toString());
                                                Toast.makeText(MainActivity.this, "dead location", Toast.LENGTH_SHORT).show();
                                            }
                                        });


                                    }
                                }
                            }, Looper.getMainLooper());
                }
                else {
                    turnOnGPS();
                }
            }
            else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }


    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    private boolean isGPSenabled(){
        LocationManager locationManager = null;
        boolean isEnabled = false;
        if(locationManager == null){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }

    public String getLocationName(double lattitude, double longitude) {

        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
        try {

            List<Address> addresses = gcd.getFromLocation(lattitude, longitude,
                    10);
            for (Address adrs : addresses) {
                if (adrs != null) {

                    String city = adrs.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                        System.out.println("city ::  " + cityName);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;

    }

}