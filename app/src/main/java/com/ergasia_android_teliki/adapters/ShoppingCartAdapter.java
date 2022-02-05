package com.ergasia_android_teliki.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia_android_teliki.Product;
import com.ergasia_android_teliki.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>{
    private static final String TAG = "ShoppingCartAdapter";

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView price;
        public ImageButton delete;
        public ImageView img;

        public ViewHolder(View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            delete = itemView.findViewById(R.id.delProductBtn);
            img = itemView.findViewById(R.id.product_img);
        }
    }

    private List<Product> products;
    private Context context;
    private TextView total_text;
    private TextView cartempty_text;
    private Button continueOrderBtn;
    public ShoppingCartAdapter(List<Product> products, Context context, TextView total_text, Button continueOrderBtn, TextView cartempty_text){
        this.products = products;
        this.context = context;
        this.total_text = total_text;
        this.continueOrderBtn = continueOrderBtn;
        this.cartempty_text = cartempty_text;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View cartView = inflater.inflate(R.layout.cart_row, parent, false);

        return new ViewHolder(cartView);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Product product = products.get(position);
        TextView title = holder.title;
        TextView price = holder.price;
        ImageButton delbutton = holder.delete;
        ImageView img = holder.img;

        // Try/catch to assign image to product
        // because File.createTempFile might throw exception
        try {
            File file = File.createTempFile("temp", "jpg");

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("product_images/" + product.getImageName());

            imageRef.getFile(file).addOnSuccessListener(taskSnapshot -> {
                img.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            }).addOnFailureListener(e -> {
                img.setBackgroundResource(R.drawable.error_image);
                Log.e(TAG, e.getMessage());
            });

        } catch (Exception e){
            img.setBackgroundResource(R.drawable.error_image);
            Log.e(TAG, e.getMessage());
        }

        title.setText(product.getTitle());
        price.setText(context.getString(R.string.product_price, String.valueOf(product.getPrice())));

        // Set delete button on click method to remove item from cart list
        SharedPreferences sp = context.getSharedPreferences("Cart", Context.MODE_PRIVATE);

        delbutton.setOnClickListener(view -> {
            String productid = "Product" + product.getId();
            // Gson and type are used to obtain and save the object to shared preferences
            // in a stringified form
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Product>>() {}.getType();

            // This gets the whole cart row so we can set its visibility to Gone
            View v = (View) view.getParent().getParent();
            v.setVisibility(View.GONE);

            // Decrease the product amount by 1 in the shared preferences
            sp.edit().putInt(productid, sp.getInt(productid, 0)-1).apply();

            // Get cart list from shared preferences
            ArrayList<Product> cartlist = gson.fromJson(sp.getString("productlist", null), type);

            // Get the index of 1 instance of the object in the array list
            // and then remove it from the array list
            int index = cartlist.indexOf(product);
            if (index != -1) cartlist.remove(index);

            // Then update the shared preferences with the updated array list
            sp.edit().putString("productlist", gson.toJson(cartlist)).apply();

            // Get total from shared preferences, update it to reflect current total
            double total = Double.parseDouble(sp.getString("carttotal", "0,0"));
            total -= product.getPrice();
            // then set the text in the layout to the new price
            total_text.setText(context.getString(R.string.cart_total, String.valueOf(total)));
            // and finally update shared preferences with the new cart total
            sp.edit().putString("carttotal", String.valueOf(total)).apply();

            // When the total goes to 0 it means the cart is empty
            if (total == 0){
                // Disable the continue button so the user cant continue
                // with empty cart, and show the cart empty text
                cartempty_text.setVisibility(View.VISIBLE);
                continueOrderBtn.setEnabled(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


}
