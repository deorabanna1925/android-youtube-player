package com.deorabanna1925.youtubeplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.deorabanna1925.youtubeplayer.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

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
                        loadData(s.toString());
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
                loadData(url);
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

        String urlStr = binding.editText.getText().toString().trim();
        if (checkIfUrlIsYoutubeUrl(urlStr)) {
            startNewActivity(urlStr);
        } else {
            Toast.makeText(this, "This is not Youtube Url", Toast.LENGTH_SHORT).show();
        }

    }

    private void loadData(String videoUrl) {
        String url = "https://noembed.com/embed?url=" + videoUrl;
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.getCache().clear();
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject != null) {
                    String title = jsonObject.getString("title");
                    String author = jsonObject.getString("author_name");
                    binding.title.setText(title);
                    binding.author.setText(author);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
        });
        queue.add(request);
    }

    private boolean checkIfUrlIsYoutubeUrl(String url) {
        Pattern pattern = Pattern.compile(
                "^(http(s)?://)?((w){3}.)?youtu(be|.be)?(\\.com)?/.+",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    private void startNewActivity(String url) {
        Intent intent = new Intent(MainActivity.this, DeoraYoutubeActivity.class);
        intent.putExtra("video_url", url);
        startActivity(intent);
    }
}