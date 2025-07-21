package com.example.tradeupproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.tradeupproject.models.Listing;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity để sửa một Listing đã tồn tại.
 * Sử dụng layout activity_add_item.xml để hiển thị form.
 */
public class EditListingActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etBehavior, etTags;
    private Spinner spinnerCategory, spinnerCondition;
    private Button btnSubmit;

    private FirebaseFirestore db;
    private String listingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);  // reuse layout for add and edit

        // Ánh xạ view
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        etLocation = findViewById(R.id.etLocation);
        etBehavior = findViewById(R.id.etBehavior);
        etTags = findViewById(R.id.etTags);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // Lấy listingId từ Intent
        listingId = getIntent().getStringExtra("listingId");
        if (listingId == null) {
            Toast.makeText(this, "Missing listingId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load dữ liệu listing và hiển thị lên form
        loadListing();

        // Cập nhật khi click
        btnSubmit.setOnClickListener(v -> updateListing());
    }

    private void loadListing() {
        db.collection("listings")
                .document(listingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    Listing listing = doc.toObject(Listing.class);
                    if (listing == null) {
                        Toast.makeText(this, "Error parsing listing", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Điền dữ liệu
                    etTitle.setText(listing.getTitle());
                    etDescription.setText(listing.getDescription());
                    etPrice.setText(String.valueOf(listing.getPrice()));
                    setSpinnerSelection(spinnerCategory, listing.getCategory());
                    setSpinnerSelection(spinnerCondition, listing.getCondition());
                    etLocation.setText(listing.getLocation());
                    etBehavior.setText(listing.getStatus());
                    List<String> tagsList = listing.getTags();
                    if (tagsList != null) {
                        etTags.setText(TextUtils.join(", ", tagsList));
                    }

                    // Cho phép submit
                    btnSubmit.setEnabled(true);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this, "Error loading: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void updateListing() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("Required");
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("title", title);
        updateData.put("description", etDescription.getText().toString().trim());
        try {
            updateData.put("price", Double.parseDouble(
                    etPrice.getText().toString().trim()));
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid number");
            return;
        }
        updateData.put("category", spinnerCategory.getSelectedItem().toString());
        updateData.put("condition", spinnerCondition.getSelectedItem().toString());
        updateData.put("location", etLocation.getText().toString().trim());
        updateData.put("status", etBehavior.getText().toString().trim());
        String tagsStr = etTags.getText().toString().trim();
        updateData.put("tags", Arrays.asList(tagsStr.split("\\s*,\\s*")));

        db.collection("listings")
                .document(listingId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Listing updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
