package com.example.tradeupproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupproject.adapters.NotificationAdapter;
import com.example.tradeupproject.models.Notification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notifications = new ArrayList<>();
    private BottomNavigationView bottomNav;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Init Firestore & Auth
        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);

        // Setup Bottom Navigation
        setupBottomNav(R.id.nav_alerts);

        // Load notifications for current user
        loadNotifications();
    }

    private void setupBottomNav(int selectedItemId) {
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(NotificationActivity.this, HomeActivity.class));
                    return true;
                }
                if (id == R.id.nav_listings) {
                    startActivity(new Intent(NotificationActivity.this, MyListingsActivity.class));
                    return true;
                }
                if (id == R.id.nav_post) {
                    startActivity(new Intent(NotificationActivity.this, AddItemActivity.class));
                    return true;
                }
                if (id == R.id.nav_alerts) {
                    // Already here
                    return true;
                }
                if (id == R.id.nav_profile) {
                    startActivity(new Intent(NotificationActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void loadNotifications() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    notifications.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) {
                            n.setId(doc.getId());
                            notifications.add(n);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // TODO: show error message if needed
                });
    }
}
