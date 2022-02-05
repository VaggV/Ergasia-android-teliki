package com.ergasia_android_teliki.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ergasia_android_teliki.R;
import com.ergasia_android_teliki.Store;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private Button logoutBtn, shopBtn, storeProfileBtn;
    private FirebaseAuth mAuth;
    private LocationManager locationManager;
    private List<Store> storeList;
    private FirebaseFirestore db;
    private SharedPreferences sp;

    private static final String TAG = "MainActivity";
    static final int REQ_LOC_CODE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializations
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storeList = new ArrayList<>();
        sp = getSharedPreferences("Role", MODE_PRIVATE);

        db.collection("users")
                .document(mAuth.getCurrentUser().getEmail())
                .collection("orders")
                .get().addOnCompleteListener(task -> {

            if (task.isSuccessful() && task.getResult() != null){
                for (QueryDocumentSnapshot doc : task.getResult()){

                    db.collection("stores")
                            .document(doc.get("store").toString())
                            .get().addOnCompleteListener(task1 -> {
                                Map<String, Object> data = task1.getResult().getData();
                                storeList.add(new Store(String.valueOf(data.get("address")),
                                        doc.get("store").toString(),
                                        (GeoPoint) data.get("loc"),
                                        (List<Timestamp>) data.get("dates"),
                                        doc.getId()));
                            });
                }
            }
        });

        // Get buttons from layout and set on click listeners
        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(view -> {
            mAuth.signOut();
            final Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        shopBtn = findViewById(R.id.shopBtn);
        shopBtn.setOnClickListener(view -> {
            final Intent intent = new Intent(MainActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        storeProfileBtn = findViewById(R.id.storeProfileBtn);
        storeProfileBtn.setOnClickListener(view -> {
            final Intent intent = new Intent(MainActivity.this, StoreProfileActivity.class);
            startActivity(intent);
        });

        // Get user role from firestore
        // If it's a store, then show the "Store profile" button
        // and save the role to shared preferences to use as an id
        db.collection("users").document(mAuth.getCurrentUser().getEmail()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        String role = (String) task.getResult().get("role");
                        if (role != null && role.startsWith("store")) {
                            storeProfileBtn.setVisibility(View.VISIBLE);
                            shopBtn.setVisibility(View.GONE);
                        }
                        sp.edit().putString("role", role).apply();
                    }
                });

        // Check if the required permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, MainActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, MainActivity.this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Check if the stores are retrieved from the database and saved to the list
        if (storeList.size() > 0 && mAuth != null) {
            // Each store in the list is a store which the user has choosed to pick up his order,
            // thus it represents one order of the user
            for (Store store : storeList) {
                // For each store in the list calculate the distance from the user to its location
                double distance = location.distanceTo(store.getLocation());
                // If the distance is less that 300 meters then add an "incoming"
                // entry to the store's collection in the database
                if (distance < 300) {
                    Map<String, Object> userIncoming = new HashMap<>();
                    userIncoming.put("orderid", store.getOrderId());
                    db.collection("stores")
                            .document(store.getId())
                            .collection("incoming")
                            .document(mAuth.getCurrentUser().getEmail())
                            .set(userIncoming) // Set the id of the document to the user's email
                            .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Added incoming user to database");
                    }).addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to add incoming", e);
                    });
                    locationManager.removeUpdates(MainActivity.this);
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}