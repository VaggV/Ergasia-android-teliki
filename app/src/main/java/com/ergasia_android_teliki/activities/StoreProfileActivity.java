package com.ergasia_android_teliki.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.ergasia_android_teliki.Product;
import com.ergasia_android_teliki.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StoreProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ListenerRegistration registration;
    private List<String> orders;

    private final String TAG = "StoreProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_profile);

        db = FirebaseFirestore.getInstance();

        orders = new ArrayList<>();

        CollectionReference ref = db.collection("stores")
                .document("store2").collection("incoming");


        registration = ref.addSnapshotListener((value, error) -> {
            if (error != null){
                return;
            }

            if (value != null && !value.isEmpty()){
                System.out.println("DATA:");
                for(DocumentSnapshot snap : value.getDocuments()){
                    if (snap.getData() != null) {
                        String orderid = (String) snap.getData().get("orderid");
                        DocumentReference order = db.collection("orders").document(orderid);
                        order.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null ){
                                Map<String, Object> data = task.getResult().getData();
                                List<Product> productList = (List<Product>) data.get("cartlist");
                                double price = Double.parseDouble(String.valueOf(data.get("total")));
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        registration.remove();
    }
}