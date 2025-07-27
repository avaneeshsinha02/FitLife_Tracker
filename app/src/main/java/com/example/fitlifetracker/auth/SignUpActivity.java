package com.example.fitlifetracker.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fitlifetracker.R;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = findViewById(R.id.editText_email_signup);
        passwordEditText = findViewById(R.id.editText_password_signup);
        Button signUpButton = findViewById(R.id.button_signUp);
        ImageView backButton = findViewById(R.id.button_back);

        signUpButton.setOnClickListener(v -> handleSignUp());
        backButton.setOnClickListener(v -> finish());
    }

    private void handleSignUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }

        Toast.makeText(this, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show();
        finish();
    }
}