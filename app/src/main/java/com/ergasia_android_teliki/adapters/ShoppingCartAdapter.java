package com.ergasia_android_teliki.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia_android_teliki.Product;
import com.ergasia_android_teliki.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>{
    private static final String TAG = "ShoppingCartAdapter";

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView quantity;
        public TextView price;
        public ImageButton delete;
        public ImageView img;

        public ViewHolder(View itemView){
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.product_name);
            quantity = (TextView) itemView.findViewById(R.id.product_quantity);
            price = (TextView) itemView.findViewById(R.id.product_price);
            delete = (ImageButton) itemView.findViewById(R.id.delProductBtn);
            img = (ImageView) itemView.findViewById(R.id.product_img);
        }
    }

    private List<Product> products;
    private Context context;

    public ShoppingCartAdapter(List<Product> products_, Context context_){
        products = products_;
        context = context_;
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
        TextView quantity = holder.quantity;
        TextView price = holder.price;
        ImageButton button = holder.delete;
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
        price.setText(String.valueOf(product.getPrice()));

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

}
