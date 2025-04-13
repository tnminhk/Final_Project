package com.example.project1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private ImageView passwordToggle;
    private Drawable visibilityIcon, visibilityOffIcon;
    private boolean isPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // Ánh xạ các thành phần giao diện
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        Button btnLogin = findViewById(R.id.btnlogin);
        Button btnSignUp = findViewById(R.id.btnsingup);
        TextView registerTextView = findViewById(R.id.signUp);

        // Bắt sự kiện click vào nút đăng nhập
        btnLogin.setOnClickListener(v -> login());

        // Bắt sự kiện click vào nút đăng ký
        btnSignUp.setOnClickListener(v -> register());

        // Bắt sự kiện click vào TextView "Đăng ký"
        registerTextView.setOnClickListener(v -> navigateToRegister());

        // Kiểm tra và tự động đăng nhập
        String savedEmail = sharedPreferences.getString("email", null);
        String savedPassword = sharedPreferences.getString("password", null);
        if (savedEmail != null && savedPassword != null) {
            mAuth.signInWithEmailAndPassword(savedEmail, savedPassword)
                    .addOnSuccessListener(authResult -> {
                        // Đăng nhập thành công, chuyển sang màn hình chính
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Đăng nhập thất bại, xử lý lỗi
                    });

        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });

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

    private void register() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void login() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Kiểm tra xem email và mật khẩu có rỗng không
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu!!!", Toast.LENGTH_LONG).show();
            return;
        }

        // Thực hiện đăng nhập
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đăng nhập thành công!!!", Toast.LENGTH_LONG).show();

                        // Lưu email và mật khẩu vào SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("email", email);
                        editor.putString("password", password);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Đóng Activity hiện tại sau khi đăng nhập thành công
                    } else {
                        Toast.makeText(this, "Đăng nhập không thành công!!!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát ứng dụng")
                .setMessage("Bạn có chắc chắn muốn thoát ứng dụng?")
                .setPositiveButton("Thoát", (dialog, which) -> finishAffinity())
                .setNegativeButton("Hủy", null)
                .show();
    }
}
