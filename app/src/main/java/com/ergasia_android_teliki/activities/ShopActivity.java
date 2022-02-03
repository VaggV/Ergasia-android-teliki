package com.ergasia_android_teliki.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.ergasia_android_teliki.Product;
import com.ergasia_android_teliki.R;
import com.ergasia_android_teliki.adapters.ShopAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ShopActivity extends AppCompatActivity {
    private static final String TAG = "ShopActivity";
    FirebaseFirestore db;
    Button buttonback, buttonviewcart;
    Product[] products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Database initialization
        db = FirebaseFirestore.getInstance();

        // Initalize buttons and on click listeners
        buttonviewcart = findViewById(R.id.buttonviewcart);
        buttonviewcart.setOnClickListener(view -> {
            Intent intent = new Intent(ShopActivity.this, ShoppingCartActivity.class);
            startActivity(intent);
            finish();
        });
        buttonback = findViewById(R.id.buttonback);
        buttonback.setOnClickListener(view -> finish());


        RecyclerView shopItems = findViewById(R.id.shopItems);
        ShopAdapter adapter = new ShopAdapter(new ArrayList<>(), this);
        shopItems.setAdapter(adapter);
        shopItems.setLayoutManager(new LinearLayoutManager(this));

        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                int index = 0; // Keep loop index to use in the array

                // Initialize products array with amount of firestore product records
                products = new Product[task.getResult().size()];

                // For each product in the database create an object
                // and save it to the object array
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Save document data to a Map
                    Map<String, Object> data = document.getData();

                    // Create the new object and save it to the list
                    products[index] = new Product(String.valueOf(data.get("Title")),
                            Double.parseDouble(String.valueOf(data.get("Price"))),
                            Integer.parseInt(String.valueOf(data.get("Availability"))),
                            String.valueOf(data.get("ImageName")),
                            index + 1);

                    index += 1;
                }
                // Assign the adapter to the recycler view so we can have a list of products
                ShopAdapter adapter2 = new ShopAdapter(Arrays.asList(products), this);
                shopItems.setAdapter(adapter2);
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });


    }
}