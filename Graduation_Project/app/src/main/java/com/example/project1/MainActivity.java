package com.example.project1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "my_channel";
    private static final int NOTIFICATION_ID = 123;
    private static final String TAG = "MainActivity";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_FCM_TOKEN = "fcm_token";
    private static final String PREF_CAMERA_URL = "cameraURL";
    private static final String PREF_CAMERA_IP = "camIp";
    private static final String PREF_CAMERA_PASS = "camPass";

    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private EditText camIp;
    private EditText camPass;
    private MaterialButton btnCameraSet;
    private ImageView passwordToggle;
    private Drawable visibilityIcon, visibilityOffIcon;
    private boolean isPasswordVisible = false;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        passwordToggle = findViewById(R.id.password_toggle);

        visibilityIcon = ContextCompat.getDrawable(this, R.drawable.password_icon);
        visibilityOffIcon = ContextCompat.getDrawable(this, R.drawable.password_icon);

        passwordToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                camPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageDrawable(visibilityIcon);
            } else {
                camPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageDrawable(visibilityOffIcon);
            }
            camPass.setSelection(camPass.length()); // Di chuyển con trỏ về cuối
        });

        // Khởi tạo database, FirebaseAuth và SharedPreferences
        database = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // Lấy token FCM và lưu vào SharedPreferences
        getFCMToken();


        // Lắng nghe sự thay đổi của tín hiệu
        database.child("Divaovung").child("Warning").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String signalValue = dataSnapshot.getValue(String.class);
                if ("1".equals(signalValue)) {
                    // Gửi thông báo khi ứng dụng đang chạy
                    sendNotification("Thông báo đi vào vùng nguy hiểm", "Nội dung thông báo");
                }
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Lỗi khi đọc dữ liệu từ Firebase Database: " + databaseError.getMessage());
            }
        });
        database.child("Te").child("Te").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String signalValue = dataSnapshot.getValue(String.class);
                if ("1".equals(signalValue)) {
                    // Gửi thông báo khi ứng dụng đang chạy
                    sendNotificationfall("Thông báo ngã ", "Nội dung thông báo");
                }
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Lỗi khi đọc dữ liệu từ Firebase Database: " + databaseError.getMessage());
            }
        });

        // Thêm nút đăng xuất
        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> logout());

        // Ánh xạ các view liên quan đến camera
        camIp = findViewById(R.id.camIp);
        camPass = findViewById(R.id.camPassword);
        btnCameraSet = findViewById(R.id.btnCamSet);

        btnCameraSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = camIp.getText().toString();
                String pass = camPass.getText().toString();
                String address = "rtsp://admin:" + pass + "@" + ip + ":554/onvif1";
                SharedPreferences.Editor editor = getSharedPreferences("cameraURL", MODE_PRIVATE).edit();
                editor.putString("URL", address); // Đã sửa lại khóa
                editor.putString("camIp", ip);
                editor.putString("camPass", pass);
                editor.apply();
                Toast.makeText(getApplicationContext(), "Camera URL: " + address, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });


        // Xử lý sự kiện back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });

        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (mAuth.getCurrentUser() == null) {
            // Chưa đăng nhập, chuyển sang màn hình đăng nhập
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        SharedPreferences cameraPrefs = getSharedPreferences("cameraURL", MODE_PRIVATE);
        String savedIp = cameraPrefs.getString("camIp", null);
        String savedPass = cameraPrefs.getString("camPass", null);
        if (savedIp != null && savedPass != null) {
            camIp.setText(savedIp);
            camPass.setText(savedPass);
        }
    }

    private void logout() {
        // Lưu email, mật khẩu và token FCM của người dùng vào SharedPreferences
        String email = mAuth.getCurrentUser().getEmail();
        String password = sharedPreferences.getString(PREF_PASSWORD, null);
        String fcmToken = sharedPreferences.getString(PREF_FCM_TOKEN, null);
        if (email != null && password != null && fcmToken != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREF_EMAIL, email);
            editor.putString(PREF_PASSWORD, password);
            editor.putString(PREF_FCM_TOKEN, fcmToken);
            editor.apply();
        }

        // Xóa email, mật khẩu và token FCM khỏi SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_EMAIL);
        editor.remove(PREF_PASSWORD);
        editor.remove(PREF_FCM_TOKEN);
        editor.apply();

        // Đăng xuất người dùng
        mAuth.signOut();

        // Chuyển sang màn hình đăng nhập
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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

    @SuppressLint({"NewApi", "Thông Báo Nguy Hiểm"})
    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo kênh thông báo cho Android 8.0 và cao hơn
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo PendingIntent để chuyển sang activity khác khi người dùng nhấn vào thông báo
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Cảnh báo!!!")
                .setContentText("Cảnh báo người dùng!!!Người dùng đi vào khu vực nguy hiểm!!!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Thiết lập PendingIntent vào thông báo
                .setVibrate(new long[]{0, 1000, 500, 1000}) // Thêm chức năng rung
                .setColor(ContextCompat.getColor(this, R.color.red)); // Đổi màu icon thành màu đỏ


        // Sử dụng ID thông báo ngẫu nhiên
        int notificationId = generateUniqueNotificationId();
        notificationManager.notify(notificationId, builder.build());
    }
    
    @SuppressLint({"NewApi", "Thông Báo Nguy Hiểm"})
    private void sendNotificationfall(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo kênh thông báo cho Android 8.0 và cao hơn
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo PendingIntent để chuyển sang activity khác khi người dùng nhấn vào thông báo
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Cảnh báo ngã!!!")
                .setContentText("Cảnh báo có người té ngã!!!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Thiết lập PendingIntent vào thông báo
                .setVibrate(new long[]{0, 1000, 500, 1000}) // Thêm chức năng rung
                .setColor(ContextCompat.getColor(this, R.color.red)); // Đổi màu icon thành màu đỏ


        // Sử dụng ID thông báo ngẫu nhiên
        int notificationId = generateUniqueNotificationId();
        notificationManager.notify(notificationId, builder.build());
    }

    private int generateUniqueNotificationId() {
        return (int) System.currentTimeMillis();
    }


    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                // Lưu token FCM vào SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_FCM_TOKEN, token);
                editor.apply();
            }
        });
    }

}
