package com.ergasia_android_teliki.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.ergasia_android_teliki.R;
import com.ergasia_android_teliki.adapters.StoreProfileAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoreProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ListenerRegistration registration;
    private RecyclerView orderRecyclerView;
    private List<List<String>> orderItems;
    private List<String> users;
    private List<Double> prices;

    private final String TAG = "StoreProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_profile);

        // Initializations
        db = FirebaseFirestore.getInstance();
        orderItems = new ArrayList<>();
        users = new ArrayList<>();
        prices = new ArrayList<>();
        orderRecyclerView = findViewById(R.id.orderRecyclerView);

        // Initialize adapter for recycler view with an empty adapter
        // so we can update it after the data is retrieved from firestore
        StoreProfileAdapter adapter = new StoreProfileAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        orderRecyclerView.setAdapter(adapter);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Save "incoming" collection path reference to a variable
        CollectionReference ref = db.collection("stores")
                .document("store2").collection("incoming");

        // Add a snapshot listener to call when data has changed in the incoming collection
        // so we can update the order list dynamically when a user is incoming to the store
        registration = ref.addSnapshotListener((value, error) -> {
            if (error != null){
                return;
            }

            if (value != null && !value.isEmpty()){

                // Clear the arrays every time there's a new incoming user added to the database
                orderItems.clear();
                users.clear();
                prices.clear();

                // For every user in "incoming" collection
                for(DocumentSnapshot snap : value.getDocuments()){
                    if (snap.getData() != null) {

                        // Get the order id of the user
                        String orderid = (String) snap.getData().get("orderid");

                        // Document reference for the order in the orders collection
                        // so that we can grab the data from the order, for example the cartlist
                        DocumentReference order = db.collection("orders").document(orderid);

                        // Get the order from firestore
                        order.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().getData() != null) {
                                // Save the order data to a map
                                Map<String, Object> data = task.getResult().getData();

                                // Get the product list from the order
                                ArrayList<Object> productList = (ArrayList<Object>) data.get("cartlist");

                                // Get the order total cost
                                double price = Double.parseDouble(String.valueOf(data.get("total")));
                                prices.add(price);

                                // For each object in the product list
                                // save its id (the objects in the array list are of type HashMap)
                                List<String> orderItems1 = new ArrayList<>();
                                for (Object x : productList) {
                                    Map<String, Object> y = (Map<String, Object>) x;
                                    orderItems1.add("product" + y.get("id"));
                                }

                                // Add to the users array list the user id
                                users.add(snap.getId());

                                // Add to the order items list the items of the user's order
                                orderItems.add(orderItems1);

                                // Set the recycler view adapter to contain the arrays
                                // so we can show the data to the screen
                                StoreProfileAdapter adapter2 = new StoreProfileAdapter(users, orderItems, prices);
                                orderRecyclerView.setAdapter(adapter2);
                            }
                        });

                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // When the activity is closed, remove the database listener
        registration.remove();
    }
}