package com.ergasia_android_teliki.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
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
    private TextView emptyCartText, totalamount;
    private Button backtoshop, continueOrderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // Initialize sharedpreferences
        sp = getSharedPreferences("Cart", Context.MODE_PRIVATE);

        // The text that says "Cart is empty"
        emptyCartText = findViewById(R.id.emptyCartText);

        // The total price text
        totalamount = findViewById(R.id.totalamount);

        continueOrderBtn = findViewById(R.id.continueOrderBtn);
        continueOrderBtn.setOnClickListener(view -> {
            final Intent intent = new Intent(ShoppingCartActivity.this, OrderActivity.class);
            startActivity(intent);
        });
        backtoshop = findViewById(R.id.backtoshop);
        backtoshop.setOnClickListener(view -> {
            final Intent intent = new Intent(ShoppingCartActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();

        // Get product list from shared preferences
        products = gson.fromJson(sp.getString("productlist", null), type);
        emptyCartText.setVisibility(View.GONE);

        // If product list is empty then show the "Cart is empty" text
        if (products == null || products.isEmpty()){
            emptyCartText.setVisibility(View.VISIBLE);
            products = new ArrayList<>();
        }

        // Calculate the price sum of cart products
        double sum = 0;
        for (Product product : products){
            sum += product.getPrice();
        }

        if (sum == 0){
            continueOrderBtn.setEnabled(false);
        }

        totalamount.setText(getString(R.string.cart_total, String.valueOf(sum)));
        sp.edit().putString("carttotal", String.valueOf(sum)).apply();

        // Add shop_item.xml layout to RecyclerView so shop items can be shown
        RecyclerView cartItems = findViewById(R.id.cartItems);
        ShoppingCartAdapter adapter = new ShoppingCartAdapter(products, this, totalamount, continueOrderBtn, emptyCartText);
        cartItems.setAdapter(adapter);
        cartItems.setLayoutManager(new LinearLayoutManager(this));
    }
}