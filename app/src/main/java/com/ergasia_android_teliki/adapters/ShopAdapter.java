package com.ergasia_android_teliki.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder>{
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView quantity;
        public TextView price;
        public ImageView img;
        public Button btn;

        public ViewHolder(View itemView){
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.itemTitle);
            quantity = (TextView) itemView.findViewById(R.id.itemAvailability);
            price = (TextView) itemView.findViewById(R.id.itemPrice);
            img = (ImageView) itemView.findViewById(R.id.itemImage);
            btn = (Button) itemView.findViewById(R.id.addToCartBtn);
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
        img.setBackgroundResource(R.drawable.product);

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