package com.deorabanna1925.youtubeplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.deorabanna1925.youtubeplayer.databinding.ActivityMainBinding;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final String YOUTUBE_THUMBNAIL_PREFIX = "https://img.youtube.com/vi/";
    private static final String YOUTUBE_THUMBNAIL_LOW_RES_SUFFIX = "/default.jpg";
    private static final String YOUTUBE_THUMBNAIL_HIGH_RES_SUFFIX = "/maxresdefault.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.playVideo.setOnClickListener(v -> sendData());
        binding.playVideo.setVisibility(View.GONE);

        binding.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() != 0) {
                    binding.playVideo.setVisibility(View.VISIBLE);
                    if (checkIfUrlIsYoutubeUrl(s.toString())) {
                        String videoId = getVideoIdFromVideoUrl(s.toString());
                        String lowResImage = YOUTUBE_THUMBNAIL_PREFIX + videoId + YOUTUBE_THUMBNAIL_LOW_RES_SUFFIX;
                        String highResImage = YOUTUBE_THUMBNAIL_PREFIX + videoId + YOUTUBE_THUMBNAIL_HIGH_RES_SUFFIX;
                        loadVideoThumbnail(lowResImage, highResImage);
                    }
                } else {
                    binding.playVideo.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String url = extras.getString(Intent.EXTRA_TEXT);
            if (url != null) {
                binding.editText.setText(url);
                String videoId = getVideoIdFromVideoUrl(url);
                String lowResImage = YOUTUBE_THUMBNAIL_PREFIX + videoId + YOUTUBE_THUMBNAIL_LOW_RES_SUFFIX;
                String highResImage = YOUTUBE_THUMBNAIL_PREFIX + videoId + YOUTUBE_THUMBNAIL_HIGH_RES_SUFFIX;
                loadVideoThumbnail(lowResImage, highResImage);
            }
        }

    }

    private void loadVideoThumbnail(String lowResImage, String highResImage) {
//        RequestBuilder<Drawable> thumbnail = Glide.with(this).load(lowResImage);
        Glide.with(this).load(highResImage).into(binding.youtubeThumbnailImage);
    }

    private String getVideoIdFromVideoUrl(String url) {
        String videoId = null;
        Pattern pattern = Pattern.compile(
                "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            videoId = matcher.group(1);
        }
        return videoId;
    }

    private void sendData() {
        if (binding.editText.getText().toString().trim().length() == 0) {
            binding.editText.setError("Required");
            return;
        }
        String urlStr = binding.editText.getText().toString();
        if (checkIfUrlIsYoutubeUrl(urlStr)) {
            startNewActivity();
        } else {
            Toast.makeText(this, "This is not Youtube Url", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkIfUrlIsYoutubeUrl(String url) {
        Pattern pattern = Pattern.compile(
                "^(http(s)?://)?((w){3}.)?youtu(be|.be)?(\\.com)?/.+",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    private void startNewActivity() {
        Toast.makeText(this, "Launch", Toast.LENGTH_SHORT).show();
/*
        new AlertDialog.Builder(this)
                .setTitle("Choose Activity")
                .setMessage("Choose the Activity to Play YouTube Video :")
                .setPositiveButton("Deora Youtube Activity", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, DeoraYoutubeActivity.class);
                    intent.putExtra("video_url", binding.editText.getText().toString());
                    startActivity(intent);
                })
                .setNegativeButton("YouTube API Activity ",(dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, YoutubeApiActivity.class);
                    intent.putExtra("video_id", getVideoIdFromVideoUrl(binding.editText.getText().toString()));
                    startActivity(intent);
                })
                .setNeutralButton("Cancel", null)
                .show();*/
    }
}