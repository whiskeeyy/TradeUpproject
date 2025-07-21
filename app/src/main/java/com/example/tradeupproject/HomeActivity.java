package com.example.tradeupproject;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.tradeupproject.adapters.ListingAdapter;
import com.example.tradeupproject.models.Listing;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ListingAdapter adapter;
    private final List<Listing> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_home);

        rv = findViewById(R.id.productRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListingAdapter(list, item -> {
            // Khi click: mở chi tiết
            Intent i = new Intent(this, ListingDetailActivity.class);
            i.putExtra("id", item.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        loadFromFirestore();
    }

    private void loadFromFirestore() {
        FirebaseFirestore.getInstance()
                .collection("listings")
                .whereEqualTo("status", "Available")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    list.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Listing L = d.toObject(Listing.class);
                        if (L!=null) {
                            L.setId(d.getId());
                            list.add(L);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // handle error
                });
    }
}
