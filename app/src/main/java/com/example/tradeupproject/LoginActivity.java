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

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        createAccountButton = findViewById(R.id.createAccountButton);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                loginButton.setEnabled(!email.isEmpty() && !password.isEmpty());
            }
        };

        emailEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);

        loginButton.setOnClickListener(v -> loginUser());
        forgotPasswordText.setOnClickListener(v -> sendPasswordReset());

        // Register button action
        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String message = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
                        String message = task.getException() != null ? task.getException().getMessage() : "Failed to send reset link.";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
