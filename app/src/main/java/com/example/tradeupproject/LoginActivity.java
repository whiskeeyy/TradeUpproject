package com.example.tradeupproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, createAccountButton;
    private TextView forgotPasswordText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1️⃣ Init FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // 2️⃣ Find Views
        emailEditText       = findViewById(R.id.emailEditText);
        passwordEditText    = findViewById(R.id.passwordEditText);
        loginButton         = findViewById(R.id.loginButton);
        forgotPasswordText  = findViewById(R.id.forgotPasswordText);
        createAccountButton = findViewById(R.id.createAccountButton);

        // 3️⃣ Enable login button only when both fields are non-empty
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String email = emailEditText.getText().toString().trim();
                String pwd   = passwordEditText.getText().toString().trim();
                loginButton.setEnabled(!email.isEmpty() && !pwd.isEmpty());
            }
        };
        emailEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);

        // 4️⃣ Click listeners
        loginButton.setOnClickListener(v -> loginUser());
        forgotPasswordText.setOnClickListener(v -> sendPasswordReset());
        createAccountButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String pwd   = passwordEditText.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Bỏ hẳn phần kiểm tra emailVerified
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        String err = task.getException() != null
                                ? task.getException().getMessage()
                                : "Authentication failed";
                        Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendPasswordReset() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email to reset password.", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                    } else {
                        String err = task.getException() != null
                                ? task.getException().getMessage()
                                : "Reset failed";
                        Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
