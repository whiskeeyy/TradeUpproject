package com.example.tradeupproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupproject.adapters.ListingAdapter;
import com.example.tradeupproject.models.Listing;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // UI
    private Spinner spinnerFilterCategory;
    private TextInputEditText searchBar;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;

    // Data + adapter
    private final List<Listing> allListings      = new ArrayList<>();
    private final List<Listing> filteredListings = new ArrayList<>();
    private ListingAdapter adapter;

    // Firestore
    private FirebaseFirestore db;

    // Debounce
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Current filters
    private String searchQuery    = "";
    private String filterCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1️⃣ Ánh xạ view
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        searchBar             = findViewById(R.id.searchBar);
        recyclerView          = findViewById(R.id.productRecyclerView);
        bottomNav             = findViewById(R.id.bottomNavigation);

        // 2️⃣ Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // 3️⃣ Setup Category Spinner
        List<String> cats = new ArrayList<>();
        cats.add("All");
        cats.addAll(Arrays.asList("Electronics","Furniture","Clothing","Books","Other"));
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cats
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(catAdapter);
        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterCategory = cats.get(position);
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // 4️⃣ Setup RecyclerView + Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListingAdapter(filteredListings, item -> {
            Intent i = new Intent(HomeActivity.this, ListingDetailActivity.class);
            i.putExtra("listingId", item.getId());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        // 5️⃣ Load all listings from Firestore
        loadListings();

        // 6️⃣ Debounced keyword search
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchQuery = s.toString().trim().toLowerCase();
                searchRunnable = HomeActivity.this::applyFilters;
                searchHandler.postDelayed(searchRunnable, 200);
            }
        });

        // 7️⃣ Setup BottomNavigationView (simple API)
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.nav_home) {
                            return true;
                        }
                        if (id == R.id.nav_listings) {
                            startActivity(new Intent(HomeActivity.this, MyListingsActivity.class));
                            return true;
                        }
                        if (id == R.id.nav_post) {
                            startActivity(new Intent(HomeActivity.this, AddItemActivity.class));
                            return true;
                        }
                        if (id == R.id.nav_alerts) {
                            startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
                            return true;
                        }
                        if (id == R.id.nav_profile) {
                            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                            return true;
                        }
                        return false;
                    }
                }
        );
    }

    private void loadListings() {
        db.collection("listings")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allListings.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Listing l = doc.toObject(Listing.class);
                        if (l != null) {
                            l.setId(doc.getId());
                            allListings.add(l);
                        }
                    }
                    applyFilters();
                });
    }

    private void applyFilters() {
        filteredListings.clear();
        for (Listing l : allListings) {
            // 1) keyword
            if (!searchQuery.isEmpty()) {
                String t = l.getTitle().toLowerCase();
                String d = l.getDescription().toLowerCase();
                if (!t.contains(searchQuery) && !d.contains(searchQuery)) {
                    continue;
                }
            }
            // 2) category
            if (!filterCategory.equals("All") &&
                    !l.getCategory().equals(filterCategory)) {
                continue;
            }
            filteredListings.add(l);
        }
        adapter.notifyDataSetChanged();
    }
}
