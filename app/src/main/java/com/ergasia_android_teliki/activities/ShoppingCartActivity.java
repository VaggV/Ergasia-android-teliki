package com.ergasia_android_teliki.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ergasia_android_teliki.Product;
import com.ergasia_android_teliki.R;
import com.ergasia_android_teliki.adapters.ShoppingCartAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ShoppingCartActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private ArrayList<Product> products;
    private TextView emptyCartText;
    private Button backtoshop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // Initialize sharedpreferences
        sp = getSharedPreferences("Cart", Context.MODE_PRIVATE);

        // The text that says "Cart is empty"
        emptyCartText = findViewById(R.id.emptyCartText);

        backtoshop = findViewById(R.id.backtoshop);
        backtoshop.setOnClickListener(view -> finish());

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();

        // Get product list from shared preferences
        products = gson.fromJson(sp.getString("productlist", ""), type);
        emptyCartText.setVisibility(View.GONE);

        // If product list is empty then show the "Cart is empty" text
        if (products == null){
            emptyCartText.setVisibility(View.VISIBLE);
            products = new ArrayList<>();
        }

        // Add shop_item.xml layout to RecyclerView so shop items can be shown
        RecyclerView cartItems = findViewById(R.id.cartItems);
        ShoppingCartAdapter adapter = new ShoppingCartAdapter(products, this);
        cartItems.setAdapter(adapter);
        cartItems.setLayoutManager(new LinearLayoutManager(this));
    }
}