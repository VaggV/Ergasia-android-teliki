package com.ergasia_android_teliki.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia_android_teliki.R;

import java.util.List;

public class StoreProfileAdapter extends RecyclerView.Adapter<StoreProfileAdapter.ViewHolder>{
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView orderUserId;
        private final Spinner orderItemsDropdown;
        private final TextView orderTotalPrice;

        public ViewHolder(View itemView){
            super(itemView);
            orderUserId = itemView.findViewById(R.id.orderUserId);
            orderItemsDropdown = itemView.findViewById(R.id.orderItemsDropdown);
            orderTotalPrice = itemView.findViewById(R.id.orderTotalPrice);
        }
    }

    private List<String> users;
    private List<List<String>> orderItems;
    private List<Double> prices;

    public StoreProfileAdapter(List<String> users, List<List<String>> orderItems, List<Double> prices) {
        this.orderItems = orderItems;
        this.users = users;
        this.prices = prices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View orderRow = inflater.inflate(R.layout.order_view, parent, false);
        return new ViewHolder(orderRow);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        String user = users.get(position);
        List<String> items = orderItems.get(position);
        Double price = prices.get(position);

        TextView userid = holder.orderUserId;
        Spinner itemsDropdown = holder.orderItemsDropdown;
        TextView totalPrice = holder.orderTotalPrice;

        // Set the user's order info to each component

        userid.setText(user);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(),
                R.layout.support_simple_spinner_dropdown_item,
                items);

        itemsDropdown.setAdapter(adapter);

        totalPrice.setText(context.getString(R.string.cart_total, String.valueOf(price)));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

}
