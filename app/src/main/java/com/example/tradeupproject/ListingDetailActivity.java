package com.example.tradeupproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tradeupproject.adapters.ImagePagerAdapter;
import com.example.tradeupproject.models.Listing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ListingDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvTitle, tvPrice, tvCategory, tvLocation,
            tvDescription, tvBehavior, tvTags;
    private Button btnContact, btnEdit, btnDelete;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String listingId;
    private Listing currentListing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_detail);

        // 1️⃣ Init Firestore + Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 2️⃣ Lấy listingId từ Intent
        listingId = getIntent().getStringExtra("listingId");
        if (listingId == null) {
            Toast.makeText(this, "Không có listingId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3️⃣ Ánh xạ view
        viewPager    = findViewById(R.id.viewPagerImages);
        tvTitle      = findViewById(R.id.tvDetailTitle);
        tvPrice      = findViewById(R.id.tvDetailPrice);
        tvCategory   = findViewById(R.id.tvDetailCategory);
        tvLocation   = findViewById(R.id.tvDetailLocation);
        tvDescription= findViewById(R.id.tvDetailDescription);
        tvBehavior   = findViewById(R.id.tvDetailBehavior);
        tvTags       = findViewById(R.id.tvDetailTags);

        btnContact = findViewById(R.id.btnContactSeller);
        btnEdit    = findViewById(R.id.btnEditListing);
        btnDelete  = findViewById(R.id.btnDeleteListing);

        // 4️⃣ Ẩn trước nút Edit/Delete, chỉ show khi xác định owner
        btnEdit.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);

        // 5️⃣ Xử lý click
        btnContact.setOnClickListener(v -> {
            // ví dụ: mở chat, gọi điện, hoặc gửi email...
            Toast.makeText(this,
                    "Contact seller: " + currentListing.getSellerId(),
                    Toast.LENGTH_SHORT).show();
        });

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditListingActivity.class);
            intent.putExtra("listingId", listingId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            db.collection("listings")
                    .document(listingId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this,
                                "Xóa thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Xóa thất bại: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });

        // 6️⃣ Load dữ liệu từ Firestore
        loadListingDetail();
    }

    private void loadListingDetail() {
        db.collection("listings")
                .document(listingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this,
                                "Listing không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    currentListing = doc.toObject(Listing.class);
                    if (currentListing == null) {
                        Toast.makeText(this,
                                "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // 7️⃣ Đổ dữ liệu lên UI
                    tvTitle.setText(currentListing.getTitle());
                    tvPrice.setText("₫" + currentListing.getPrice());
                    tvCategory.setText(
                            currentListing.getCategory()
                                    + " • "
                                    + currentListing.getCondition()
                    );
                    tvLocation.setText(currentListing.getLocation());
                    tvDescription.setText(currentListing.getDescription());
                    tvBehavior.setText(currentListing.getStatus());  // nếu bạn đổi field behavior→status

                    // nối list tags thành chuỗi
                    List<String> tags = currentListing.getTags();
                    if (tags != null && !tags.isEmpty()) {
                        tvTags.setText(TextUtils.join(", ", tags));
                    }

                    // 8️⃣ Carousel ảnh
                    List<String> imgs = currentListing.getImages();
                    if (imgs == null) imgs = new ArrayList<>();
                    ImagePagerAdapter adapter =
                            new ImagePagerAdapter(this, imgs);
                    viewPager.setAdapter(adapter);

                    // 9️⃣ Hiện Edit/Delete nếu đúng owner
                    String uid = auth.getCurrentUser() != null
                            ? auth.getCurrentUser().getUid()
                            : "";
                    if (uid.equals(currentListing.getSellerId())) {
                        btnEdit.setVisibility(View.VISIBLE);
                        btnDelete.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Load thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}
