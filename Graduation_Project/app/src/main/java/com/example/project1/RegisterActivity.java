package com.example.project1;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private ImageView passwordToggle;
    private Drawable visibilityIcon, visibilityOffIcon;
    private boolean isPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Button signUpButton = findViewById(R.id.btnsingup);
        TextView loginTextView = findViewById(R.id.login);
        signUpButton.setOnClickListener(v -> registerUser(emailEditText.getText().toString(), passwordEditText.getText().toString(), mAuth));
        loginTextView.setOnClickListener(v -> navigateToLogin());
        passwordToggle = findViewById(R.id.password_toggle);

        visibilityIcon = ContextCompat.getDrawable(this, R.drawable.password_icon);
        visibilityOffIcon = ContextCompat.getDrawable(this, R.drawable.password_icon);

        passwordToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageDrawable(visibilityIcon);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageDrawable(visibilityOffIcon);
            }
            passwordEditText.setSelection(passwordEditText.length()); // Di chuyển con trỏ về cuối
        });
    }

    private void registerUser(String email, String password, FirebaseAuth mAuth) {
        if (email.isEmpty()) {
            showToast("Vui lòng nhập email!!!");
            return;
        }
        if (password.isEmpty()) {
            showToast("Vui lòng nhập password!!!");
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Tạo tài khoản thành công!!!");
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        showToast("Tạo tài khoản không thành công!!!");
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
