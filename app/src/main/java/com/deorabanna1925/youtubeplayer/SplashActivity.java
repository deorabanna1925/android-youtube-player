package com.deorabanna1925.youtubeplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.deorabanna1925.youtubeplayer.activity.MainActivity;
import com.deorabanna1925.youtubeplayer.databinding.*;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int SPLASH_DISPLAY_LENGTH = 1000;
        new Handler(getMainLooper()).postDelayed(() -> {
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            this.finish();
        }, SPLASH_DISPLAY_LENGTH);

    }
}