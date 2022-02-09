package com.ergasia_android_teliki.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ergasia_android_teliki.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoreProfileAdapter extends RecyclerView.Adapter<StoreProfileAdapter.ViewHolder>{
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView orderUserId;
        private final Spinner orderItemsDropdown;
        private final TextView orderTotalPrice;
        private final Button completeIncomingOrder;

        public ViewHolder(View itemView){
            super(itemView);
            orderUserId = itemView.findViewById(R.id.orderUserId);
            orderItemsDropdown = itemView.findViewById(R.id.orderItemsDropdown);
            orderTotalPrice = itemView.findViewById(R.id.orderTotalPrice);
            completeIncomingOrder = itemView.findViewById(R.id.completeIncomingOrder);
        }
    }

    private List<String> users;
    private List<List<String>> orderItems;
    private List<Double> prices;
    private List<DocumentReference> orderIds;
    private List<DocumentReference> incomingOrders;

    public StoreProfileAdapter(List<String> users, List<List<String>> orderItems, List<Double> prices, List<DocumentReference> orderIds, List<DocumentReference> incomingOrders) {
        this.orderItems = orderItems;
        this.users = users;
        this.prices = prices;
        this.orderIds = orderIds;
        this.incomingOrders = incomingOrders;
    }

    public StoreProfileAdapter(){
        this.users = new ArrayList<>();
        this.orderItems = new ArrayList<>();
        this.prices = new ArrayList<>();
        this.orderIds = new ArrayList<>();
        this.incomingOrders = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View orderRow = inflater.inflate(R.layout.order_incoming, parent, false);
        return new ViewHolder(orderRow);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        String user = users.get(position);
        List<String> items = orderItems.get(position);
        Double price = prices.get(position);
        DocumentReference order = orderIds.get(position);
        DocumentReference incomingOrder = incomingOrders.get(position);

        TextView userid = holder.orderUserId;
        Spinner itemsDropdown = holder.orderItemsDropdown;
        TextView totalPrice = holder.orderTotalPrice;
        Button completeIncomingOrder = holder.completeIncomingOrder;

        // Set the user's order info to each component
        userid.setText(context.getString(R.string.user_id, user));

        // Copy the items list to a new one so the original stays the same
        List<String> items2 = new ArrayList<>(items);
        // Create an empty list to add the new items with a counter added next to them
        // (instead of having: product1, product1, we will have: product1 x2)
        List<String> newItems = new ArrayList<>();

        // Get the amount of DIFFERENT items in the list of items
        long n = items2.stream().distinct().count();

        for (int i=0; i < n; i++){
            // For each unique string in the list, save its occurrences
            int occurrences = Collections.frequency(items2, items2.get(0));
            // then add the modified string in the new list
            newItems.add(items2.get(0) + " x" + occurrences);
            // then remove the occurrences of the object from the old list
            items2.removeAll(Collections.singleton(items2.get(0)));
            if (items2.isEmpty()) break;
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(),
                R.layout.support_simple_spinner_dropdown_item,
                newItems);
        itemsDropdown.setAdapter(adapter);

        totalPrice.setText(context.getString(R.string.cart_total, String.valueOf(price)));

        completeIncomingOrder.setOnClickListener(view -> {
            View v = (View) view.getParent().getParent();
            v.setVisibility(View.GONE);

            // Delete order from orders collection
            order.delete().addOnSuccessListener(unused -> {
                Log.d("StoreProfileAdapter", "Order successfuly deleted from orders collection");
            }).addOnFailureListener(e -> {
                Log.w("StoreProfileAdapter", "Error delete order from orders collection", e);
            });

            // Delete order from the store's incoming collection
            incomingOrder.delete().addOnSuccessListener(unused -> {
                Log.d("StoreProfileAdapter", "Order successfuly deleted from store's incoming collection");
            }).addOnFailureListener(e -> {
                Log.w("StoreProfileAdapter", "Error delete order from store's incoming collection", e);
            });
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

}
