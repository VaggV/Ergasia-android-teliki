package com.ergasia_android_teliki.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ergasia_android_teliki.R;
import com.ergasia_android_teliki.Store;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private TextView ordertotal, distanceText;
    private Button locButton, backToCart, completeOrderBtn;
    private ConstraintLayout loadingLayout, storeInfo;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;
    private Spinner spinner, datesDropdown;
    private Store[] stores;
    private Location myLocation;
    private DateFormat dateFormat;

    private static final int REQ_LOC_CODE = 23;
    private static final String TAG = "OrderActiviy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // Initalize database
        db = FirebaseFirestore.getInstance();

        // Create a format for the dates in the dropdown
        dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");

        // Initialize shared preferences
        sp = getSharedPreferences("Cart", Context.MODE_PRIVATE);

        // Get layout components
        ordertotal = findViewById(R.id.orderTotal);
        locButton = findViewById(R.id.button2);
        loadingLayout = findViewById(R.id.loadingLayout);
        spinner = findViewById(R.id.spinner);
        storeInfo = findViewById(R.id.storeInfo);
        datesDropdown = findViewById(R.id.datesDropdown);
        distanceText = findViewById(R.id.distanceText);
        backToCart = findViewById(R.id.backToCart);
        completeOrderBtn = findViewById(R.id.completeOrderBtn);

        // Get the cart total from shared preferences
        double carttotal = Double.parseDouble(sp.getString("carttotal", "0.0"));
        ordertotal.setText(getString(R.string.cart_total, String.valueOf(carttotal)));

        // Initialize location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Get location once when the activity loads
        handleLocation();

        // Set a button to get the location again if the user wants to
        locButton.setOnClickListener(view -> handleLocation());
        // Back button
        backToCart.setOnClickListener(view -> finish());

        // Set a listener for when the user selects an item from the dropdown
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (stores !=  null) {
                    // Calculate distance from user's location to the store's location
                    // and show it to the user
                    double distance = Math.round(myLocation.distanceTo(stores[i].getLocation()));
                    double distanceKm = distance/1000;
                    distanceText.setText(getString(R.string.distance_in_kilometers, String.valueOf(distanceKm)));

                    // Get the available dates of the store selected and show them
                    // in another dropdown list
                    List<String> dates = new ArrayList<>();
                    for (Timestamp timestamp : stores[i].getDates()) {
                        Date date = new Date(timestamp.toDate().getTime());
                        String formatted = dateFormat.format(date);
                        dates.add(formatted);
                    }

                    // Set the available dates to the store dropdown
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(OrderActivity.this, R.layout.support_simple_spinner_dropdown_item, dates);
                    datesDropdown.setAdapter(adapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Empty
            }
        });

        completeOrderBtn.setOnClickListener(view -> {
            // TODO: Add complete order method
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.checkSelfPermission(OrderActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            handleLocation();
        }
    }

    // Handle location function is used to get the location of the user
    // and then show relevant info based on it for all the stores
    private void handleLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OrderActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOC_CODE);
            return;
        }
        // Show a loading screen while location is being grabbed
        loadingLayout.setVisibility(View.VISIBLE);
        storeInfo.setVisibility(View.GONE);

        // Cancellation token for getCurrentLocation method
        CancellationTokenSource src = new CancellationTokenSource();
        CancellationToken ct = src.getToken();

        // --- THIS IS A BIT SLOW ---
        fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, ct).addOnSuccessListener(myLoc -> {
            // If myLoc isn't null then it means the user's location was successfully grabbed
            if (myLoc != null){
                myLocation = myLoc;

                // Get the stores from firestore database
                db.collection("stores").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        // Initialize stores array with the amount of database entries
                        stores = new Store[task.getResult().size()];
                        int i = 0;
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            // Save data to a map and then to an array
                            Map<String, Object> data = documentSnapshot.getData();
                            stores[i] = new Store(String.valueOf(data.get("address")),
                                    documentSnapshot.getId(),
                                    (GeoPoint) data.get("loc"),
                                    (List<Timestamp>) data.get("dates"));

                            i += 1;
                        }

                        // Add the stores to the dropdown
                        ArrayAdapter<Store> adapter = new ArrayAdapter<>(OrderActivity.this, R.layout.support_simple_spinner_dropdown_item, stores);
                        spinner.setAdapter(adapter);

                        // Store info is the layout with the available dates dropdown,
                        // the distance and the complete order button
                        storeInfo.setVisibility(View.VISIBLE);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
            } else {  // Else if location is null then location services are turned off
                // Show the user an appropriate message in a dialog
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_warning)
                        .setMessage(R.string.dialog_loc_services_off)
                        .setNegativeButton(R.string.dialog_ok, null)
                        .setIcon(R.drawable.warning_icon)
                        .show();

                // Set the addresses dropdown to contain a message
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        R.layout.support_simple_spinner_dropdown_item,
                        new String[]{"Couldn't get your location"});
                spinner.setAdapter(adapter);
            }
            // Hide the loading layout when the getCurrentMethod location has finished
            loadingLayout.setVisibility(View.GONE);

        }).addOnFailureListener(e -> {
            // Hide the loading layout if a failure happens
            Log.w(TAG, e);
            loadingLayout.setVisibility(View.GONE);
        });
    }
}