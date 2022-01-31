package com.ergasia_android_teliki.adapters;

import android.content.Context;
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

import java.util.List;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>{
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

    public ShoppingCartAdapter(List<Product> products_){
        products = products_;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View cartView = inflater.inflate(R.layout.cart_row, parent, false);

        return new ViewHolder(cartView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Product product = products.get(position);
        TextView title = holder.title;
        TextView quantity = holder.quantity;
        TextView price = holder.price;
        ImageButton button = holder.delete;
        ImageView img = holder.img;
        img.setBackgroundResource(R.drawable.product);

        title.setText(product.getTitle());
        quantity.setText(String.valueOf(product.getAvailability()));
        price.setText(String.valueOf(product.getPrice()));

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

}
