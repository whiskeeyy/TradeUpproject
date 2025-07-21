package com.example.tradeupproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupproject.adapters.ListingAdapter;
import com.example.tradeupproject.adapters.ListingAdapter.OnItemClick;
import com.example.tradeupproject.models.Listing;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyListingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ListingAdapter adapter;
    private List<Listing> myListings = new ArrayList<>();
    private BottomNavigationView bottomNav;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        // 1️⃣ Init Firestore & Auth
        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 2️⃣ RecyclerView + Adapter
        recyclerView = findViewById(R.id.recyclerMyListings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ListingAdapter(myListings, new OnItemClick() {
            @Override
            public void onClick(Listing item) {
                Intent i = new Intent(MyListingsActivity.this, ListingDetailActivity.class);
                i.putExtra("listingId", item.getId());
                startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);

        // 3️⃣ Bottom Navigation
        setupBottomNav(R.id.nav_listings);

        // 4️⃣ Load current user’s listings
        loadMyListings();
    }

    private void setupBottomNav(int selectedId) {
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(selectedId);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(MyListingsActivity.this, HomeActivity.class));
                    return true;
                }
                if (id == R.id.nav_listings) return true;
                if (id == R.id.nav_post) {
                    startActivity(new Intent(MyListingsActivity.this, AddItemActivity.class));
                    return true;
                }
                if (id == R.id.nav_alerts) {
                    startActivity(new Intent(MyListingsActivity.this, NotificationActivity.class));
                    return true;
                }
                if (id == R.id.nav_profile) {
                    startActivity(new Intent(MyListingsActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void loadMyListings() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("listings")
                .whereEqualTo("sellerId", uid)
                .get()
                .addOnSuccessListener(qs -> {
                    myListings.clear();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Listing l = doc.toObject(Listing.class);
                        if (l != null) {
                            l.setId(doc.getId());
                            myListings.add(l);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
