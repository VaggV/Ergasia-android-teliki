package com.ergasia_android_teliki.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia_android_teliki.Product;
import com.ergasia_android_teliki.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder>{
    private static final String TAG = "ShopAdapter";

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView quantity;
        public TextView price;
        public ImageView img;
        public Button btn;

        public ViewHolder(View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.itemTitle);
            quantity = itemView.findViewById(R.id.itemAvailability);
            price = itemView.findViewById(R.id.itemPrice);
            img = itemView.findViewById(R.id.itemImage);
            btn = itemView.findViewById(R.id.addToCartBtn);
        }
    }

    private List<Product> products;
    private Context context;
    private ArrayList<Product> cartlist;

    public ShopAdapter(List<Product> products_, Context context_){
        products = products_;
        context = context_;
        // We pass the context in this adapter class because
        // we want to use the sharedPreferences below
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View shopRows = inflater.inflate(R.layout.shop_item, parent, false);
        return new ViewHolder(shopRows);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Product product = products.get(position);

        TextView title = holder.title;
        TextView quantity = holder.quantity;
        TextView price = holder.price;
        Button button = holder.btn;
        ImageView img = holder.img;


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
        quantity.setText(context.getString(R.string.availability_text, String.valueOf(product.getAvailability())));
        price.setText(context.getString(R.string.product_price, String.valueOf(product.getPrice())));

        SharedPreferences sp = context.getSharedPreferences("Cart", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        cartlist = new ArrayList<>();

        // Set add to cart on click method
        button.setOnClickListener(view -> {
            cartlist.add(product);
            Gson gson = new Gson();
            editor.putString("productlist", gson.toJson(cartlist));
            editor.apply();
            Toast.makeText(context.getApplicationContext(), "Added to cart", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

}