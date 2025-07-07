package com.example.tradeupproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView displayNameTextView, bioTextView, contactTextView;
    private RatingBar userRatingBar;
    private Button editProfileButton, deleteAccountButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // UI elements
        profileImageView    = findViewById(R.id.profileImageView);
        displayNameTextView = findViewById(R.id.displayNameTextView);
        bioTextView         = findViewById(R.id.bioTextView);
        contactTextView     = findViewById(R.id.contactTextView);
        userRatingBar       = findViewById(R.id.userRatingBar);
        editProfileButton   = findViewById(R.id.editProfileButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        // Load profile (đã bỏ finish() nếu không tồn tại)
        loadUserProfile();

        // Luôn cho phép vào EditProfileActivity
        editProfileButton.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class))
        );

        deleteAccountButton.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void loadUserProfile() {
        String uid = currentUser.getUid();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name    = doc.getString("displayName");
                        String bio     = doc.getString("bio");
                        String contact = doc.getString("contactInfo");
                        String imgUrl  = doc.getString("profileImageUrl");
                        Double rating  = doc.getDouble("rating");

                        displayNameTextView.setText(name != null ? name : "");
                        bioTextView.setText(bio != null ? bio : "");
                        contactTextView.setText(contact != null ? contact : "");
                        userRatingBar.setRating(rating != null ? rating.floatValue() : 0f);

                        if (imgUrl != null && !imgUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(Uri.parse(imgUrl))
                                    .into(profileImageView);
                        }
                    }
                    // Nếu không tồn tại doc, để trống UI cho user nhập mới
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void confirmDeleteAccount() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (d, w) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        String uid = currentUser.getUid();
        db.collection("users")
                .document(uid)
                .delete()
                .addOnSuccessListener(unused ->
                        currentUser.delete()
                                .addOnSuccessListener(u -> {
                                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Delete user failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete profile failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
