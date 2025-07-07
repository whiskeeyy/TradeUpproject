package com.example.tradeupproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText displayNameEditText, contactEditText, emailEditText, passwordEditText;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1️⃣ Init Firebase Auth & Firestore
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // 2️⃣ Find UI elements
        displayNameEditText = findViewById(R.id.displayNameEditText);
        contactEditText     = findViewById(R.id.contactEditText);
        emailEditText       = findViewById(R.id.emailEditText);
        passwordEditText    = findViewById(R.id.passwordEditText);
        registerButton      = findViewById(R.id.registerButton);

        // 3️⃣ Enable register only when all fields valid
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String name    = displayNameEditText.getText().toString().trim();
                String contact = contactEditText.getText().toString().trim();
                String email   = emailEditText.getText().toString().trim();
                String pwd     = passwordEditText.getText().toString();

                boolean validName    = !name.isEmpty();
                boolean validContact = Patterns.PHONE.matcher(contact).matches();
                boolean validEmail   = Patterns.EMAIL_ADDRESS.matcher(email).matches();
                boolean validPwd     = pwd.length() >= 6;

                registerButton.setEnabled(
                        validName && validContact && validEmail && validPwd
                );
            }
        };

        displayNameEditText.addTextChangedListener(watcher);
        contactEditText.addTextChangedListener(watcher);
        emailEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);

        // 4️⃣ On click → register
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        final String name    = displayNameEditText.getText().toString().trim();
        final String contact = contactEditText.getText().toString().trim();
        final String email   = emailEditText.getText().toString().trim();
        final String pwd     = passwordEditText.getText().toString();

        // 5️⃣ Create Firebase Auth user
        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) return;

                        // 6️⃣ Save extra profile info in Firestore
                        Map<String,Object> profile = new HashMap<>();
                        profile.put("displayName", name);
                        profile.put("contactInfo", contact);
                        profile.put("email", email);
                        profile.put("createdAt", FieldValue.serverTimestamp());

                        db.collection("users")
                                .document(user.getUid())
                                .set(profile)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(
                                                this,
                                                "Profile saved. Verification email sent.",
                                                Toast.LENGTH_LONG
                                        ).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                this,
                                                "Profile save failed: " + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );

                        // 7️⃣ Send email verification
                        user.sendEmailVerification()
                                .addOnCompleteListener(v -> {
                                    if (v.isSuccessful()) {
                                        // Done: prompt user and finish
                                        Toast.makeText(
                                                this,
                                                "Please check your email to verify your account.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                        finish();  // back to LoginActivity
                                    } else {
                                        Toast.makeText(
                                                this,
                                                "Could not send verification email.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });

                    } else {
                        // Registration failed
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
