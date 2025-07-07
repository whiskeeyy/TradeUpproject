package com.example.tradeupproject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText displayNameEditText, contactEditText, bioEditText;
    private Button saveProfileButton, deleteAccountButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayNameEditText = findViewById(R.id.displayNameEditText);
        contactEditText     = findViewById(R.id.contactEditText);
        bioEditText         = findViewById(R.id.bioEditText);
        saveProfileButton   = findViewById(R.id.saveProfileButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        // Load profile (không finish() nếu chưa có)
        loadProfile();

        saveProfileButton.setOnClickListener(v -> saveProfile());
        deleteAccountButton.setOnClickListener(v -> confirmDelete());
    }

    private void loadProfile() {
        String uid = currentUser.getUid();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        displayNameEditText.setText(doc.getString("displayName"));
                        contactEditText.setText(doc.getString("contactInfo"));
                        bioEditText.setText(doc.getString("bio"));
                    }
                    // Nếu doc không tồn tại, để trống cho user nhập mới
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void saveProfile() {
        String name    = displayNameEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();
        String bio     = bioEditText.getText().toString().trim();

        Map<String, Object> data = new HashMap<>();
        data.put("displayName", name);
        data.put("contactInfo", contact);
        data.put("bio", bio);

        db.collection("users")
                .document(currentUser.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
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
