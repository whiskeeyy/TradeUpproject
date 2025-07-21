package com.example.tradeupproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tradeupproject.adapters.ImagePagerAdapter;
import com.example.tradeupproject.models.Listing;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreviewActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvTitle, tvPrice, tvCategory, tvLocation,
            tvDescription, tvStatus, tvTags;
    private Button btnEdit, btnSubmit;

    private Listing draft;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_listing);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Ánh xạ view
        viewPager    = findViewById(R.id.viewPagerPreviewImages);
        tvTitle      = findViewById(R.id.tvPreviewTitle);
        tvPrice      = findViewById(R.id.tvPreviewPrice);
        tvCategory   = findViewById(R.id.tvPreviewCategory);
        tvLocation   = findViewById(R.id.tvPreviewLocation);
        tvDescription= findViewById(R.id.tvPreviewDescription);
        tvStatus     = findViewById(R.id.tvPreviewStatus);
        tvTags       = findViewById(R.id.tvPreviewTags);

        btnEdit   = findViewById(R.id.btnEditPreview);
        btnSubmit = findViewById(R.id.btnSubmitListing);

        // Nhận dữ liệu draft từ Intent
        draft = new Listing();
        Intent i = getIntent();
        draft.setTitle(i.getStringExtra("title"));
        draft.setPrice(i.getLongExtra("price", 0L));
        draft.setCategory(i.getStringExtra("category"));
        draft.setCondition(i.getStringExtra("condition"));
        draft.setDescription(i.getStringExtra("description"));
        draft.setLocation(i.getStringExtra("location"));
        draft.setStatus(i.getStringExtra("status"));
        draft.setSellerId(auth.getCurrentUser().getUid());
        // Tags
        ArrayList<String> tags = i.getStringArrayListExtra("tags");
        draft.setTags(tags != null ? tags : new ArrayList<>());
        // Images
        ArrayList<String> imgs = i.getStringArrayListExtra("images");
        draft.setImages(imgs != null ? imgs : new ArrayList<>());

        // Hiển thị lên UI
        tvTitle.setText(draft.getTitle());
        tvPrice.setText("₫" + draft.getPrice());
        tvCategory.setText(draft.getCategory() + " • " + draft.getCondition());
        tvLocation.setText(draft.getLocation());
        tvDescription.setText(draft.getDescription());
        tvStatus.setText(draft.getStatus());
        tvTags.setText(TextUtils.join(", ", draft.getTags()));

        // Carousel ảnh
        ImagePagerAdapter adapter =
                new ImagePagerAdapter(this, draft.getImages());
        viewPager.setAdapter(adapter);

        // Edit → quay về AddItemActivity cùng data cũ
        btnEdit.setOnClickListener(v -> {
            Intent edit = new Intent(this, AddItemActivity.class);
            // copy nguyên extras về cho AddItemActivity
            edit.putExtras(i.getExtras());
            startActivity(edit);
            finish();
        });

        // Submit → lưu lên Firestore
        btnSubmit.setOnClickListener(v -> {
            Map<String, Object> data = new HashMap<>();
            data.put("title",       draft.getTitle());
            data.put("price",       draft.getPrice());
            data.put("category",    draft.getCategory());
            data.put("condition",   draft.getCondition());
            data.put("description", draft.getDescription());
            data.put("location",    draft.getLocation());
            data.put("status",      draft.getStatus());
            data.put("tags",        draft.getTags());
            data.put("images",      draft.getImages());
            data.put("sellerId",    draft.getSellerId());
            data.put("views",       0);
            data.put("createdAt",   Timestamp.now());

            db.collection("listings")
                    .add(data)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this,
                                "Listing submitted!", Toast.LENGTH_SHORT).show();
                        // về Home
                        Intent home = new Intent(this, HomeActivity.class);
                        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(home);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Submit failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });
    }
}
