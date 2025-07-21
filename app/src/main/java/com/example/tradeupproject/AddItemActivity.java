package com.example.tradeupproject;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddItemActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private static final int PICK_IMAGE_REQUEST = 101;

    private TextInputEditText etTitle, etDescription, etPrice, etBehavior, etTags;
    private EditText etLocation;
    private Spinner spinnerCategory, spinnerCondition;
    private ImageButton btnGps, btnAddPhoto;
    private LinearLayout photosContainer;
    private Button btnPreview;

    private final List<String> selectedImages = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Ánh xạ View
        etTitle       = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice       = findViewById(R.id.etPrice);
        etLocation    = findViewById(R.id.etLocation);
        etBehavior    = findViewById(R.id.etBehavior);
        etTags        = findViewById(R.id.etTags);

        spinnerCategory  = findViewById(R.id.spinnerCategory);
        spinnerCondition = findViewById(R.id.spinnerCondition);

        btnGps         = findViewById(R.id.btnGps);
        btnAddPhoto    = findViewById(R.id.btnAddPhoto);
        photosContainer = findViewById(R.id.photosContainer);

        btnPreview = findViewById(R.id.btnPreview);

        // Khởi tạo FusedLocationProvider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Thiết lập Spinner
        setupSpinners();

        // TextWatcher để validate
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { validateFields(); }
        };
        etTitle.addTextChangedListener(watcher);
        etDescription.addTextChangedListener(watcher);
        etPrice.addTextChangedListener(watcher);
        etLocation.addTextChangedListener(watcher);

        // GPS autofill
        btnGps.setOnClickListener(v -> requestLocation());

        // Chọn ảnh
        btnAddPhoto.setOnClickListener(v -> pickImages());

        // Preview
        btnPreview.setEnabled(false);
        btnPreview.setOnClickListener(v -> goToPreview());
    }

    private void setupSpinners() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                Arrays.asList("Electronics","Furniture","Clothing","Books","Cars","Other")
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        ArrayAdapter<String> condAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                Arrays.asList("New","Used","Refurbished")
        );
        condAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(condAdapter);

        AdapterView.OnItemSelectedListener sel = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                validateFields();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        spinnerCategory.setOnItemSelectedListener(sel);
        spinnerCondition.setOnItemSelectedListener(sel);
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(loc -> {
                        if (loc != null) {
                            etLocation.setText(loc.getLatitude() + "," + loc.getLongitude());
                        } else {
                            Toast.makeText(this, "Không lấy được vị trí", Toast.LENGTH_SHORT).show();
                        }
                        validateFields();
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(requestCode, perms, res);
        if (requestCode == REQUEST_LOCATION_PERMISSION
                && res.length > 0
                && res[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        } else {
            Toast.makeText(this, "Quyền vị trí bị từ chối", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null) {
            if (data.getClipData() != null) {
                ClipData clip = data.getClipData();
                for (int i = 0;
                     i < clip.getItemCount() && selectedImages.size() < 10;
                     i++) {
                    Uri uri = clip.getItemAt(i).getUri();
                    addImage(uri);
                }
            } else if (data.getData() != null
                    && selectedImages.size() < 10) {
                addImage(data.getData());
            }
        }
    }

    private void addImage(Uri uri) {
        selectedImages.add(uri.toString());
        ImageView thumb = new ImageView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 200);
        lp.setMargins(8, 8, 8, 8);
        thumb.setLayoutParams(lp);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setImageURI(uri);
        photosContainer.addView(thumb);
        validateFields();
    }

    private void validateFields() {
        boolean valid =
                !TextUtils.isEmpty(etTitle.getText()) &&
                        !TextUtils.isEmpty(etDescription.getText()) &&
                        !TextUtils.isEmpty(etPrice.getText()) &&
                        !TextUtils.isEmpty(etLocation.getText()) &&
                        selectedImages.size() >= 1;
        btnPreview.setEnabled(valid);
    }

    private void goToPreview() {
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("title", etTitle.getText().toString().trim());
        intent.putExtra("price", Long.parseLong(etPrice.getText().toString().trim()));
        intent.putExtra("category", spinnerCategory.getSelectedItem().toString());
        intent.putExtra("condition", spinnerCondition.getSelectedItem().toString());
        intent.putExtra("description", etDescription.getText().toString().trim());
        intent.putExtra("location", etLocation.getText().toString().trim());
        intent.putExtra("status", etBehavior.getText().toString().trim());
        intent.putStringArrayListExtra(
                "tags",
                new ArrayList<>(Arrays.asList(etTags.getText().toString().split("\\s*,\\s*")))
        );
        intent.putStringArrayListExtra("images", new ArrayList<>(selectedImages));
        startActivity(intent);
    }
}
