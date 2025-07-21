package com.example.tradeupproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;        // <-- Import này cho @NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tradeupproject.adapters.ImagePagerAdapter;
import com.example.tradeupproject.models.Listing;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class ListingDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvTitle, tvPrice, tvCategory, tvLocation,
            tvDescription, tvBehavior, tvTags, tvViews;
    private Spinner spinnerStatus;
    private Button btnUpdateStatus, btnContact, btnEdit, btnDelete;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String listingId;
    private Listing currentListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_detail);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        listingId = getIntent().getStringExtra("listingId");
        if (listingId == null) {
            Toast.makeText(this, "No listingId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ View
        viewPager       = findViewById(R.id.viewPagerImages);
        tvTitle         = findViewById(R.id.tvDetailTitle);
        tvPrice         = findViewById(R.id.tvDetailPrice);
        tvCategory      = findViewById(R.id.tvDetailCategory);
        tvLocation      = findViewById(R.id.tvDetailLocation);
        tvDescription   = findViewById(R.id.tvDetailDescription);
        tvBehavior      = findViewById(R.id.tvDetailBehavior);
        tvTags          = findViewById(R.id.tvDetailTags);
        tvViews         = findViewById(R.id.tvDetailViews);
        spinnerStatus   = findViewById(R.id.spinnerStatus);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        btnContact      = findViewById(R.id.btnContactSeller);
        btnEdit         = findViewById(R.id.btnEditListing);
        btnDelete       = findViewById(R.id.btnDeleteListing);

        // Ẩn ban đầu
        btnEdit.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        spinnerStatus.setVisibility(View.GONE);
        btnUpdateStatus.setVisibility(View.GONE);

        // Click listeners
        btnContact.setOnClickListener(v ->
                Toast.makeText(this,
                        "Contact seller: " + currentListing.getSellerId(),
                        Toast.LENGTH_SHORT).show()
        );
        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, EditListingActivity.class);
            i.putExtra("listingId", listingId);
            startActivity(i);
        });
        btnDelete.setOnClickListener(v ->
                db.collection("listings").document(listingId)
                        .delete()
                        .addOnSuccessListener(a -> {
                            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        "Delete failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        )
        );

        loadListingDetail();
    }

    private void loadListingDetail() {
        db.collection("listings")
                .document(listingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { finish(); return; }
                    currentListing = doc.toObject(Listing.class);
                    if (currentListing == null) { finish(); return; }

                    // Fill dữ liệu
                    tvTitle.setText(currentListing.getTitle());
                    tvPrice.setText("₫" + currentListing.getPrice());
                    tvCategory.setText(
                            currentListing.getCategory() + " • " + currentListing.getCondition()
                    );
                    tvLocation.setText(currentListing.getLocation());
                    tvDescription.setText(currentListing.getDescription());
                    tvBehavior.setText(currentListing.getStatus());
                    tvTags.setText(TextUtils.join(", ", currentListing.getTags()));

                    // Views
                    int oldViews = currentListing.getViews();
                    tvViews.setText("Views: " + oldViews);
                    String uid = auth.getCurrentUser() != null
                            ? auth.getCurrentUser().getUid()
                            : "";
                    boolean isOwner = uid.equals(currentListing.getSellerId());
                    if (!isOwner) {
                        db.collection("listings")
                                .document(listingId)
                                .update("views", FieldValue.increment(1))
                                .addOnSuccessListener(a ->
                                        tvViews.setText("Views: " + (oldViews + 1))
                                );
                    }

                    // Carousel ảnh
                    List<String> imgs = currentListing.getImages();
                    if (imgs == null) imgs = Arrays.asList();
                    viewPager.setAdapter(new ImagePagerAdapter(this, imgs));

                    // Owner controls
                    if (isOwner) {
                        btnEdit.setVisibility(View.VISIBLE);
                        btnDelete.setVisibility(View.VISIBLE);
                        spinnerStatus.setVisibility(View.VISIBLE);
                        btnUpdateStatus.setVisibility(View.VISIBLE);

                        List<String> statuses = Arrays.asList("Available","Sold","Paused");
                        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_spinner_item, statuses
                        );
                        statusAdapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );
                        spinnerStatus.setAdapter(statusAdapter);
                        int idx = statuses.indexOf(currentListing.getStatus());
                        if (idx >= 0) spinnerStatus.setSelection(idx);

                        btnUpdateStatus.setOnClickListener(v -> {
                            String newStatus = spinnerStatus.getSelectedItem().toString();
                            db.collection("listings")
                                    .document(listingId)
                                    .update("status", newStatus)
                                    .addOnSuccessListener(a ->
                                            Toast.makeText(this,
                                                    "Status updated to “" + newStatus + "”",
                                                    Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this,
                                                    "Update failed: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show()
                                    );
                        });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Load failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}
