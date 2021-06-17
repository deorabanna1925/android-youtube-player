package com.deorabanna1925.youtubeplayer.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PictureInPictureParams;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.deorabanna1925.youtubeplayer.R;
import com.deorabanna1925.youtubeplayer.adapter.CommentsAdapter;
import com.deorabanna1925.youtubeplayer.adapter.NotesAdapter;
import com.deorabanna1925.youtubeplayer.common.Constant;
import com.deorabanna1925.youtubeplayer.databinding.ActivityDeoraYoutubeBinding;
import com.deorabanna1925.youtubeplayer.listener.PlaybackStateListener;
import com.deorabanna1925.youtubeplayer.model.Comments;
import com.deorabanna1925.youtubeplayer.model.Notes;
import com.deorabanna1925.youtubeplayer.model.VideoLinks;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.huber.youtubeExtractor.Format;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class DeoraYoutubeActivity extends AppCompatActivity {

    private ActivityDeoraYoutubeBinding binding;

    private ProgressDialog dialog;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private String audioUrl = "";
    private PlaybackStateListener listener;
    private SparseArray<YtFile> ytFiles;
    ArrayList<VideoLinks> available = new ArrayList<>();
    private TextView playbackSpeedTxt;
    private TextView playbackQualityTxt;
    private TextView playbackResizeTxt;
    private BottomSheetBehavior<View> bottomSheetPlaybackSpeed;
    private BottomSheetBehavior<View> bottomSheetPlaybackQuality;
    private BottomSheetBehavior<View> bottomSheetPlaybackResize;
    private BottomSheetBehavior<View> bottomSheetAllNotes;
    private BottomSheetBehavior<View> bottomSheetAllComments;

    private static final ArrayList<Integer> TAGS = new ArrayList<>();

    static {
        TAGS.add(160);
        TAGS.add(133);
        TAGS.add(134);
        TAGS.add(135);
        TAGS.add(136);
        TAGS.add(137);
        TAGS.add(264);
        TAGS.add(266);
        TAGS.add(140);
        TAGS.add(141);
        TAGS.add(256);
        TAGS.add(258);
    }

    private String savedQuality = "";

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityDeoraYoutubeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        bottomSheetPlaybackSpeed = BottomSheetBehavior.from(binding.bottomSheetPlaybackSpeed);
        bottomSheetPlaybackQuality = BottomSheetBehavior.from(binding.bottomSheetPlaybackQuality);
        bottomSheetPlaybackResize = BottomSheetBehavior.from(binding.bottomSheetPlaybackResize);
        bottomSheetAllNotes = BottomSheetBehavior.from(binding.bottomSheetAllNotes);
        bottomSheetAllComments = BottomSheetBehavior.from(binding.bottomSheetAllComments);

        listener = new PlaybackStateListener(binding);
    }

    private void initializePlayer() {
        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = new SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build();
            player.addListener(listener);
            player.prepare();
        }
        binding.playerView.setPlayer(player);
    }

    @SuppressLint("StaticFieldLeak")
    private void getYtFiles(String videoUrl) {
        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFile, VideoMeta vMeta) {
                if (ytFile != null) {
                    ytFiles = ytFile;
                } else {
                    Toast.makeText(DeoraYoutubeActivity.this, "Try Again, Something went wrong", Toast.LENGTH_SHORT).show();
                    finish();
                }
                playVideo();
            }
        }.extract(videoUrl, true, true);
    }

    private void playVideo() {
        if (ytFiles != null) {
            for (int i = 0; i < TAGS.size(); i++) {
                YtFile ytFile = ytFiles.get(TAGS.get(i), null);
                if (ytFile != null) {
                    Format format = ytFile.getFormat();
                    int audioBitrate = format.getAudioBitrate();
                    int formatHeight = format.getHeight();
                    if (formatHeight != -1) {
                        available.add(new VideoLinks(formatHeight, ytFile.getUrl()));
                    }
                    if (audioBitrate != -1) {
                        audioUrl = ytFile.getUrl();
                    }
                }
            }
            if (!audioUrl.equals("")) {
                switch (savedQuality) {
                    case "144":
                        playWithQuality(144);
                        break;
                    case "240":
                        playWithQuality(240);
                        break;
                    case "360":
                        playWithQuality(360);
                        break;
                    case "480":
                        playWithQuality(480);
                        break;
                    case "720":
                        playWithQuality(720);
                        break;
                    case "1080":
                        playWithQuality(1080);
                        break;
                    case "1440":
                        playWithQuality(1440);
                        break;
                    case "2160":
                        playWithQuality(2160);
                        break;
                    default:
                        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Youtube Test"), new DefaultBandwidthMeter.Builder(this).build());
                        MediaItem mediaItemAudio = MediaItem.fromUri(Uri.parse(audioUrl));
                        MediaItem mediaItemVideo = MediaItem.fromUri(Uri.parse(available.get(0).getUrl()));
                        MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItemAudio);
                        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItemVideo);
                        MergingMediaSource mediaSource = new MergingMediaSource(videoSource, audioSource);
                        player.setMediaSource(mediaSource);
                        player.setPlayWhenReady(playWhenReady);
                        player.seekTo(currentWindow, playbackPosition);
                        player.prepare();
                        break;
                }
            } else {
                Toast.makeText(this, "Audio Not Available", Toast.LENGTH_SHORT).show();
            }
            setAvailableColors();
        } else {
            Toast.makeText(this, "Try Again, Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setColors(TextView playbackQuality, int qualm) {
        for (int i = 0; i < available.size(); i++) {
            int quality = available.get(i).getQuality();
            if (quality == qualm) {
                playbackQuality.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_circle_24, 0, 0, 0);
                playbackQuality.setEnabled(true);
                break;
            } else {
                playbackQuality.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_remove_circle_24, 0, 0, 0);
                playbackQuality.setEnabled(false);
            }
        }

    }

    private void setAvailableColors() {
        setColors(binding.playbackQualityLayout.quality144, 144);
        setColors(binding.playbackQualityLayout.quality240, 240);
        setColors(binding.playbackQualityLayout.quality360, 360);
        setColors(binding.playbackQualityLayout.quality480, 480);
        setColors(binding.playbackQualityLayout.quality720, 720);
        setColors(binding.playbackQualityLayout.quality1080, 1080);
        setColors(binding.playbackQualityLayout.quality1440, 1440);
        setColors(binding.playbackQualityLayout.quality2160, 2160);
    }

    private void initCustomController() {

        ImageButton playbackSpeed = binding.playerView.findViewById(R.id.exo_playback_speed);
        ImageButton playbackQuality = binding.playerView.findViewById(R.id.exo_playback_quality);
        ImageButton playbackResize = binding.playerView.findViewById(R.id.exo_playback_resize);
        ImageButton exoAddBookmark = binding.playerView.findViewById(R.id.exo_add_bookmark);
        ImageButton exoViewComments = binding.playerView.findViewById(R.id.exo_view_comments);
        playbackSpeedTxt = binding.playerView.findViewById(R.id.exo_playback_speed_txt);
        playbackQualityTxt = binding.playerView.findViewById(R.id.exo_playback_quality_txt);
        playbackResizeTxt = binding.playerView.findViewById(R.id.exo_playback_resize_txt);

        binding.allNotesLayout.recyclerView.setHasFixedSize(true);
        binding.allNotesLayout.recyclerView.setNestedScrollingEnabled(false);
        binding.allNotesLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.allCommentsLayout.recyclerView.setHasFixedSize(true);
        binding.allCommentsLayout.recyclerView.setNestedScrollingEnabled(false);
        binding.allCommentsLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        playbackSpeed.setOnClickListener(view -> {
            player.pause();
            bottomSheetPlaybackSpeed.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        playbackQuality.setOnClickListener(view -> {
            player.pause();
            bottomSheetPlaybackQuality.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        playbackResize.setOnClickListener(view -> {
            player.pause();
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        exoAddBookmark.setOnClickListener(view -> {
            player.pause();
            bottomSheetAllNotes.setState(BottomSheetBehavior.STATE_EXPANDED);
            binding.allNotesLayout.time.setText(getVideoSeconds(player));
            binding.allNotesLayout.text.setHint("Enter note at " + getVideoSeconds(player));
        });

        exoViewComments.setOnClickListener(view -> {
            player.pause();
            bottomSheetAllComments.setState(BottomSheetBehavior.STATE_EXPANDED);
            loadComments();
        });

        binding.allNotesLayout.add.setOnClickListener(view -> {
            String noteText = binding.allNotesLayout.text.getText().toString().trim();
            if (noteText.length() == 0) {
                binding.allNotesLayout.text.setError("Required");
                return;
            }

            ArrayList<Notes> notesArrayList = new ArrayList<>();

            notesArrayList.add(new Notes(String.valueOf(player.getCurrentPosition()), getVideoSeconds(player), noteText));
            Collections.reverse(notesArrayList);
            binding.allNotesLayout.recyclerView.setAdapter(new NotesAdapter(DeoraYoutubeActivity.this, player, bottomSheetAllNotes, notesArrayList));
            binding.allNotesLayout.text.setText(null);
            player.play();
            bottomSheetAllNotes.setState(BottomSheetBehavior.STATE_COLLAPSED);
            hideKeyboard(DeoraYoutubeActivity.this);
        });

        binding.playbackSpeedLayout.minSpeed3.setOnClickListener(v -> changeSpeed(0.25f));
        binding.playbackSpeedLayout.minSpeed2.setOnClickListener(v -> changeSpeed(0.5f));
        binding.playbackSpeedLayout.minSpeed1.setOnClickListener(v -> changeSpeed(0.75f));
        binding.playbackSpeedLayout.normalSpeed.setOnClickListener(v -> changeSpeed(1f));
        binding.playbackSpeedLayout.maxSpeed1.setOnClickListener(v -> changeSpeed(1.25f));
        binding.playbackSpeedLayout.maxSpeed2.setOnClickListener(v -> changeSpeed(1.5f));
        binding.playbackSpeedLayout.maxSpeed3.setOnClickListener(v -> changeSpeed(1.75f));
        binding.playbackSpeedLayout.maxSpeed4.setOnClickListener(v -> changeSpeed(2f));

        binding.playbackQualityLayout.quality144.setOnClickListener(v -> {
            getCurrentSeekBar();
            playWithQuality(144);
        });
        binding.playbackQualityLayout.quality240.setOnClickListener(v -> {
            getCurrentSeekBar();
            playWithQuality(240);
        });
        binding.playbackQualityLayout.quality360.setOnClickListener(v -> {
            getCurrentSeekBar();
            playWithQuality(360);
        });
        binding.playbackQualityLayout.quality480.setOnClickListener(v -> {
            getCurrentSeekBar();
            playWithQuality(480);
        });
        binding.playbackQualityLayout.quality720.setOnClickListener(v -> {
            getCurrentSeekBar();
            playWithQuality(720);
        });
        binding.playbackQualityLayout.quality1080.setOnClickListener(v -> {
            getCurrentSeekBar();
            playWithQuality(1080);
        });
        binding.playbackQualityLayout.quality1440.setOnClickListener(v -> {
            getCurrentSeekBar();
            playWithQuality(1440);
        });
        binding.playbackQualityLayout.quality2160.setOnClickListener(v -> {
            getCurrentSeekBar();
            playWithQuality(2160);
        });

        binding.playbackResizeLayout.fill.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText(R.string.fill);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.playbackResizeLayout.fit.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText(R.string.fit);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.playbackResizeLayout.zoom.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText(R.string.zoom);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.playbackResizeLayout.fixHeight.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText(R.string.fixHeight);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.playbackResizeLayout.fixWidth.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText(R.string.fixWidth);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        binding.playbackSpeedLayout.close.setOnClickListener(v -> {
            player.play();
            bottomSheetPlaybackSpeed.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.playbackQualityLayout.close.setOnClickListener(v -> {
            player.play();
            bottomSheetPlaybackQuality.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.playbackResizeLayout.close.setOnClickListener(v -> {
            player.play();
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.allNotesLayout.close.setOnClickListener(v -> {
            player.play();
            bottomSheetAllNotes.setState(BottomSheetBehavior.STATE_COLLAPSED);
            binding.allNotesLayout.text.setText(null);
        });
        binding.allCommentsLayout.close.setOnClickListener(v -> {
            player.play();
            bottomSheetAllComments.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
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

    private void loadComments() {
        ArrayList<Comments> commentsArrayList = new ArrayList<>();
        showDlg(this);
        String url = "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet,replies&key=" + Constant.API_KEY + "&videoId=";
        url += getVideoIdFromVideoUrl(getIntent().getStringExtra("video_url"));
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.getCache().clear();
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            hideDlg();
            try {
                JSONObject jsonObject = new JSONObject(response);
                commentsArrayList.clear();
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    JSONObject jsonObject2 = jsonObject1.getJSONObject("snippet");
                    JSONObject jsonObject3 = jsonObject2.getJSONObject("topLevelComment");
                    JSONObject jsonObject4 = jsonObject3.getJSONObject("snippet");

                    String authorImage = jsonObject4.getString("authorProfileImageUrl");
                    String authorName = jsonObject4.getString("authorDisplayName");
                    String textOriginal = jsonObject4.getString("textOriginal");
                    int likeCount = jsonObject4.getInt("likeCount");
                    String publishedAt = jsonObject4.getString("publishedAt");

                    commentsArrayList.add(new Comments(authorImage, authorName, textOriginal, String.valueOf(likeCount), publishedAt));
                }
                binding.allCommentsLayout.recyclerView.setAdapter(new CommentsAdapter(DeoraYoutubeActivity.this, commentsArrayList));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> hideDlg());
        queue.add(request);
    }

    private void showDlg(Context c) {

        try {
            if (dialog == null) {
                dialog = new ProgressDialog(c);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideDlg() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getVideoSeconds(SimpleExoPlayer player) {
        int timeMs = (int) player.getCurrentPosition();
        return stringForTime(timeMs);
    }

//    private String getVideoDurationSeconds(SimpleExoPlayer player) {
//        int timeMs = (int) player.getDuration();
//        return stringForTime(timeMs);
//    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;
        //  videoDurationInSeconds = totalSeconds % 60;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void getCurrentSeekBar() {
        playWhenReady = player.getPlayWhenReady();
        playbackPosition = player.getCurrentPosition();
        currentWindow = player.getCurrentWindowIndex();
    }

    private void playWithQuality(int playQuality) {
        savedQuality = String.valueOf(playQuality);
        for (int i = 0; i < available.size(); i++) {
            int quality = available.get(i).getQuality();
            String url = available.get(i).getUrl();
            if (quality == playQuality) {
                if (!audioUrl.equals("")) {
                    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Youtube Test"), new DefaultBandwidthMeter.Builder(this).build());
                    MediaItem mediaItemAudio = MediaItem.fromUri(Uri.parse(audioUrl));
                    MediaItem mediaItemVideo = MediaItem.fromUri(Uri.parse(url));
                    MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItemAudio);
                    MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItemVideo);
                    MergingMediaSource mediaSource = new MergingMediaSource(videoSource, audioSource);
                    player.setMediaSource(mediaSource);
                    player.seekTo(currentWindow, playbackPosition);
                    player.setPlayWhenReady(playWhenReady);
                    player.prepare();
                } else {
                    Toast.makeText(this, "Audio Not Available", Toast.LENGTH_SHORT).show();
                }
            }
        }
        String qualityText = playQuality + "p";
        playbackQualityTxt.setText(qualityText);
        bottomSheetPlaybackQuality.setState(BottomSheetBehavior.STATE_COLLAPSED);
        player.play();
    }

    private void changeSpeed(float speed) {
        PlaybackParameters param = new PlaybackParameters(speed);
        player.setPlaybackParameters(param);
        player.play();
        String speedText = speed + "x";
        playbackSpeedTxt.setText(speedText);
        bottomSheetPlaybackSpeed.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
            initCustomController();
        }
        String videoUrl = getIntent().getStringExtra("video_url");
        getYtFiles(videoUrl);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
            initCustomController();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.removeListener(listener);
            player.release();
            player = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        binding.playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            enterPIPMode();
        } else {
            super.onBackPressed();
        }
    }

    private void enterPIPMode() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            playbackPosition = player.getCurrentPosition();
            binding.playerView.setUseController(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PictureInPictureParams.Builder params = new PictureInPictureParams.Builder();
                this.enterPictureInPictureMode(params.build());
            } else {
                this.enterPictureInPictureMode();
            }

        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPIPMode();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        if (!isInPictureInPictureMode) {
            playbackPosition = player.getCurrentPosition();
            binding.playerView.setUseController(true);
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }
}