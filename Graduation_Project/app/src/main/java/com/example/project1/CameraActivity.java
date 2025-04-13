package com.example.project1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.media.VideoView;

public class CameraActivity extends AppCompatActivity {
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private VideoView videoView;
    private CustomView customView;
    private Button btnClear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.camera), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> backlogin());

        videoView = findViewById(R.id.videoView);
        customView = findViewById(R.id.customView);
        btnClear = findViewById(R.id.btnClear);

        btnClear.setOnClickListener(v -> customView.clearPoints());

        // Khởi tạo LibVLC
        try {
            libVLC = new LibVLC(this);
        } catch (Exception e) {
            Log.e("CameraActivity", "Lỗi khởi tạo LibVLC: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Lấy địa chỉ RTSP của camera từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("cameraURL", MODE_PRIVATE);
        String url = prefs.getString("URL", "");

        if (!url.isEmpty()) {
            try {
                Media media = new Media(libVLC, Uri.parse(url));
                media.addOption("--aout=opensles");
                media.addOption("--audio-time-stretch");
                media.addOption("-vvv"); // độ chi tiết nhật ký
                mediaPlayer = new MediaPlayer(libVLC);
                mediaPlayer.setMedia(media);
                mediaPlayer.getVLCVout().setVideoSurface(videoView.getHolder().getSurface(), videoView.getHolder());
                mediaPlayer.getVLCVout().attachViews();
                mediaPlayer.play();
            } catch (Exception e) {
                Log.e("CameraActivity", "Lỗi phát video: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(this, "Không thể phát video", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Không thể lấy được URL camera", Toast.LENGTH_SHORT).show();
            finish();
        }

        customView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        customView.addPoint(x, y);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.getVLCVout().detachViews();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (libVLC != null) {
            libVLC.release();
            libVLC = null;
        }
    }

    private void backlogin() {
        // Xóa thông tin camera khỏi SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("cameraURL", MODE_PRIVATE).edit();
        editor.remove("URL");
        editor.remove("camIp");
        editor.remove("camPass");
        editor.apply();

        // Chuyển về màn hình MainActivity
        Intent intent = new Intent(CameraActivity.this, MainActivity.class);
        startActivity(intent);
    }
}