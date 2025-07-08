package com.example.tradeupproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {
    private static final int REQ_CODE_LOCATION = 1001;

    private EditText etTitle, etDescription, etPrice, etLocation, etBehavior, etTags;
    private Spinner spinnerCategory, spinnerCondition;
    private LinearLayout photosContainer;
    private ImageButton btnAddPhoto, btnGps;
    private Button btnPreview, btnSubmit;

    private FusedLocationProviderClient fusedLocationClient;
    private List<Uri> photoUris = new ArrayList<>();
    private ActivityResultLauncher<Intent> pickPhotosLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // 1️⃣ Ánh xạ view
        etTitle         = findViewById(R.id.etTitle);
        etDescription   = findViewById(R.id.etDescription);
        etPrice         = findViewById(R.id.etPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerCondition= findViewById(R.id.spinnerCondition);
        etLocation      = findViewById(R.id.etLocation);
        btnGps          = findViewById(R.id.btnGps);
        etBehavior      = findViewById(R.id.etBehavior);
        etTags          = findViewById(R.id.etTags);
        photosContainer = findViewById(R.id.photosContainer);
        btnAddPhoto     = findViewById(R.id.btnAddPhoto);
        btnPreview      = findViewById(R.id.btnPreview);
        btnSubmit       = findViewById(R.id.btnSubmit);

        // 2️⃣ Thiết lập Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        btnGps.setOnClickListener(v -> {
            // Nếu chưa có quyền thì request, còn có thì fetch ngay
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        REQ_CODE_LOCATION
                );
            } else {
                fetchAndFillLocation();
            }
        });

        // 3️⃣ Spinner mẫu
        spinnerCategory.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{ "Cars", "Electronics", "Furniture" }
        ));
        spinnerCondition.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{ "New", "Used - Like New", "Used - Fair" }
        ));

        // 4️⃣ Chọn ảnh từ Gallery
        pickPhotosLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null && photoUris.size() < 10) {
                            photoUris.add(imageUri);
                            ImageView thumb = new ImageView(this);
                            thumb.setImageURI(imageUri);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(150, 150);
                            lp.setMargins(8, 8, 8, 8);
                            thumb.setLayoutParams(lp);
                            photosContainer.addView(thumb);
                            updateSubmitState();
                        }
                    }
                }
        );
        btnAddPhoto.setOnClickListener(v -> {
            Intent pick = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            pick.setType("image/*");
            pickPhotosLauncher.launch(pick);
        });

        // 5️⃣ Preview & Submit
        btnPreview.setOnClickListener(v -> previewListing());
        btnSubmit.setOnClickListener(v -> submitListing());
        updateSubmitState();
    }

    // Callback sau khi người dùng phản hồi permission dialog
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_LOCATION) {
            boolean granted = grantResults.length >= 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                fetchAndFillLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchAndFillLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Lấy lat/lng
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        // Chuyển thành địa chỉ với Geocoder
                        String address = geocode(lat, lng);
                        etLocation.setText(address != null
                                ? address
                                : lat + ", " + lng);
                    } else {
                        Toast.makeText(this,
                                "Unable to get location",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error fetching location: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    // Chuyển lat/lng thành địa chỉ (nếu có) hoặc trả về null
    private String geocode(double lat, double lng) {
        try {
            Geocoder geo = new Geocoder(this, Locale.getDefault());
            List<Address> list = geo.getFromLocation(lat, lng, 1);
            if (list != null && !list.isEmpty()) {
                Address a = list.get(0);
                return a.getThoroughfare() + ", " + a.getLocality();
            }
        } catch (IOException ignored) {}
        return null;
    }

    private void updateSubmitState() {
        boolean ok = !etTitle.getText().toString().trim().isEmpty()
                && !etPrice.getText().toString().trim().isEmpty()
                && photoUris.size() >= 1;
        btnSubmit.setEnabled(ok);
    }

    private void previewListing() {
        Intent i = new Intent(this, PreviewActivity.class);
        i.putExtra("title", etTitle.getText().toString());
        i.putExtra("desc",  etDescription.getText().toString());
        i.putExtra("price", etPrice.getText().toString());
        i.putExtra("category", spinnerCategory.getSelectedItem().toString());
        i.putExtra("condition", spinnerCondition.getSelectedItem().toString());
        i.putExtra("location", etLocation.getText().toString());
        i.putExtra("behavior", etBehavior.getText().toString());
        i.putExtra("tags", etTags.getText().toString());
        i.putParcelableArrayListExtra("photos", new ArrayList<>(photoUris));
        startActivity(i);
    }

    private void submitListing() {
        // TODO: upload ảnh lên Firebase Storage và lưu metadata lên Firestore
        Toast.makeText(this,
                "Implement submit logic here",
                Toast.LENGTH_SHORT).show();
    }
}
