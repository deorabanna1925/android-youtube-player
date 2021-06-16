package com.deorabanna1925.youtubeplayer.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.deorabanna1925.youtubeplayer.R;
import com.deorabanna1925.youtubeplayer.adapter.CommentsAdapter;
import com.deorabanna1925.youtubeplayer.adapter.NotesAdapter;
import com.deorabanna1925.youtubeplayer.common.Constant;
import com.deorabanna1925.youtubeplayer.listener.PlaybackStateListener;
import com.deorabanna1925.youtubeplayer.model.Comments;
import com.deorabanna1925.youtubeplayer.model.Notes;
import com.deorabanna1925.youtubeplayer.model.VideoLinks;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
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
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import com.deorabanna1925.youtubeplayer.databinding.*;

public class DeoraYoutubeActivity extends AppCompatActivity {

    private ActivityDeoraYoutubeBinding binding;

    private ProgressDialog dialog;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private String commonAudioUrl = "";
    private PlaybackStateListener playbackStateListener;
    private static final String TAG = DeoraYoutubeActivity.class.getName();
    private SparseArray<YtFile> allYtFiles;
    ArrayList<VideoLinks> formatsAvailable = new ArrayList<>();
    private TextView playbackSpeedTxt;
    private TextView playbackQualityTxt;
    private TextView playbackResizeTxt;
    private BottomSheetBehavior<View> bottomSheetPlaybackSpeed;
    private BottomSheetBehavior<View> bottomSheetPlaybackQuality;
    private BottomSheetBehavior<View> bottomSheetPlaybackResize;
    private BottomSheetBehavior<View> bottomSheetAllNotes;
    private BottomSheetBehavior<View> bottomSheetAllComments;
    private static final ArrayList<Integer> all_tags = new ArrayList<>();

    ArrayList<Notes> notesArrayList = new ArrayList<>();
    ArrayList<Comments> commentsArrayList = new ArrayList<>();
    NotesAdapter notesAdapter;
    CommentsAdapter commentsAdapter;
    RecyclerView recyclerViewNotes;
    RecyclerView recyclerViewComments;

    static {
        all_tags.add(160);
        all_tags.add(133);
        all_tags.add(134);
        all_tags.add(135);
        all_tags.add(136);
        all_tags.add(137);
        all_tags.add(264);
        all_tags.add(266);
        all_tags.add(140);
        all_tags.add(141);
        all_tags.add(256);
        all_tags.add(258);
    }

    private TextView playbackQuality_144, playbackQuality_240, playbackQuality_360, playbackQuality_480, playbackQuality_720, playbackQuality_1080, playbackQuality_1440, playbackQuality_2160;
    private String qualitySaved = "";

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
        playbackStateListener = new PlaybackStateListener(binding);
    }

    private void initializePlayer() {
        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = new SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build();
            player.addListener(playbackStateListener);
            player.prepare();
        }
        binding.playerView.setPlayer(player);
    }

    @SuppressLint("StaticFieldLeak")
    private void getYtFiles(String videoUrl) {
        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    allYtFiles = ytFiles;
                } else {
                    Toast.makeText(DeoraYoutubeActivity.this, "Try Again, Something went wrong", Toast.LENGTH_SHORT).show();
                    finish();
                }
                playVideo();
            }
        }.extract(videoUrl, true, true);
    }

    private void playVideo() {
        if (allYtFiles != null) {
            for (int i = 0; i < all_tags.size(); i++) {
                YtFile ytFile = allYtFiles.get(all_tags.get(i), null);
                if (ytFile != null) {
                    Format format = ytFile.getFormat();
                    int formatAudioBitrate = format.getAudioBitrate();
                    int formatHeight = format.getHeight();
                    if (formatHeight != -1) {
                        formatsAvailable.add(new VideoLinks(formatHeight, ytFile.getUrl()));
                    }
                    if (formatAudioBitrate != -1) {
                        commonAudioUrl = ytFile.getUrl();
                    }
                }
            }
            if (!commonAudioUrl.equals("")) {
                switch (qualitySaved) {
                    case "144":
                        playVideoWithQuality(144);
                        break;
                    case "240":
                        playVideoWithQuality(240);
                        break;
                    case "360":
                        playVideoWithQuality(360);
                        break;
                    case "480":
                        playVideoWithQuality(480);
                        break;
                    case "720":
                        playVideoWithQuality(720);
                        break;
                    case "1080":
                        playVideoWithQuality(1080);
                        break;
                    case "1440":
                        playVideoWithQuality(1440);
                        break;
                    case "2160":
                        playVideoWithQuality(2160);
                        break;
                    default:
                        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Youtube Test"), new DefaultBandwidthMeter.Builder(this).build());
                        MediaItem mediaItemAudio = MediaItem.fromUri(Uri.parse(commonAudioUrl));
                        MediaItem mediaItemVideo = MediaItem.fromUri(Uri.parse(formatsAvailable.get(0).getUrl()));
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
        for (int i = 0; i < formatsAvailable.size(); i++) {
            int quality = formatsAvailable.get(i).getQuality();
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
        setColors(playbackQuality_144, 144);
        setColors(playbackQuality_240, 240);
        setColors(playbackQuality_360, 360);
        setColors(playbackQuality_480, 480);
        setColors(playbackQuality_720, 720);
        setColors(playbackQuality_1080, 1080);
        setColors(playbackQuality_1440, 1440);
        setColors(playbackQuality_2160, 2160);
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
        recyclerViewNotes = findViewById(R.id.recycler_view_notes);
        recyclerViewNotes.setHasFixedSize(true);
        recyclerViewNotes.setNestedScrollingEnabled(false);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewComments = findViewById(R.id.recycler_view_comments);
        recyclerViewComments.setHasFixedSize(true);
        recyclerViewComments.setNestedScrollingEnabled(false);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));

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
        TextView player_notes_current_time;
        EditText player_notes_current_text;
        ImageButton player_notes_current_button;

        player_notes_current_time = findViewById(R.id.player_notes_current_time);
        player_notes_current_text = findViewById(R.id.player_notes_current_text);
        player_notes_current_button = findViewById(R.id.player_notes_current_button);

        exoAddBookmark.setOnClickListener(view -> {
            player.pause();
            bottomSheetAllNotes.setState(BottomSheetBehavior.STATE_EXPANDED);
            player_notes_current_time.setText(getVideoSeconds(player));
            player_notes_current_text.setHint("Enter note at " + getVideoSeconds(player));
        });

        exoViewComments.setOnClickListener(view -> {
            player.pause();
            bottomSheetAllComments.setState(BottomSheetBehavior.STATE_EXPANDED);
            loadComments();
        });

        player_notes_current_button.setOnClickListener(view -> {
            String noteText = player_notes_current_text.getText().toString().trim();
            if (noteText.length() == 0) {
                player_notes_current_text.setError("Required");
                return;
            }
            notesArrayList.add(new Notes(String.valueOf(player.getCurrentPosition()), getVideoSeconds(player), noteText));
            Collections.reverse(notesArrayList);
            notesAdapter = new NotesAdapter(DeoraYoutubeActivity.this, player, bottomSheetAllNotes, notesArrayList);
            recyclerViewNotes.setAdapter(notesAdapter);
            player_notes_current_text.setText(null);
            player.play();
            bottomSheetAllNotes.setState(BottomSheetBehavior.STATE_COLLAPSED);
            hideKeyboard(DeoraYoutubeActivity.this);
        });

        ImageView closeBottomSheet = findViewById(R.id.close_bottom_sheet);
        ImageView closeBottomSheet2 = findViewById(R.id.close_bottom_sheet2);
        ImageView closeBottomSheet3 = findViewById(R.id.close_bottom_sheet3);
        ImageView closeBottomSheet4 = findViewById(R.id.close_bottom_sheet4);
        ImageView closeBottomSheet5 = findViewById(R.id.close_bottom_sheet5);

        TextView minSpeed3 = findViewById(R.id.min_speed_3);
        TextView minSpeed2 = findViewById(R.id.min_speed_2);
        TextView minSpeed1 = findViewById(R.id.min_speed_1);
        TextView normalSpeed = findViewById(R.id.normal_speed);
        TextView maxSpeed1 = findViewById(R.id.max_speed_1);
        TextView maxSpeed2 = findViewById(R.id.max_speed_2);
        TextView maxSpeed3 = findViewById(R.id.max_speed_3);
        TextView maxSpeed4 = findViewById(R.id.max_speed_4);

        playbackQuality_144 = findViewById(R.id.playback_quality_144);
        playbackQuality_240 = findViewById(R.id.playback_quality_240);
        playbackQuality_360 = findViewById(R.id.playback_quality_360);
        playbackQuality_480 = findViewById(R.id.playback_quality_480);
        playbackQuality_720 = findViewById(R.id.playback_quality_720);
        playbackQuality_1080 = findViewById(R.id.playback_quality_1080);
        playbackQuality_1440 = findViewById(R.id.playback_quality_1440);
        playbackQuality_2160 = findViewById(R.id.playback_quality_2160);

        TextView resizeFill = findViewById(R.id.playback_resize_fill);
        TextView resizeFit = findViewById(R.id.playback_resize_fit);
        TextView resizeZoom = findViewById(R.id.playback_resize_zoom);
        TextView resizeFixHeight = findViewById(R.id.playback_resize_fix_height);
        TextView resizeFixWidth = findViewById(R.id.playback_resize_fix_width);

        minSpeed3.setOnClickListener(v -> changeSpeed(0.25f));
        minSpeed2.setOnClickListener(v -> changeSpeed(0.5f));
        minSpeed1.setOnClickListener(v -> changeSpeed(0.75f));
        normalSpeed.setOnClickListener(v -> changeSpeed(1f));
        maxSpeed1.setOnClickListener(v -> changeSpeed(1.25f));
        maxSpeed2.setOnClickListener(v -> changeSpeed(1.5f));
        maxSpeed3.setOnClickListener(v -> changeSpeed(1.75f));
        maxSpeed4.setOnClickListener(v -> changeSpeed(2f));

        playbackQuality_144.setOnClickListener(v -> {
            getCurrentSeekBar();
            playVideoWithQuality(144);
        });
        playbackQuality_240.setOnClickListener(v -> {
            getCurrentSeekBar();
            playVideoWithQuality(240);
        });
        playbackQuality_360.setOnClickListener(v -> {
            getCurrentSeekBar();
            playVideoWithQuality(360);
        });
        playbackQuality_480.setOnClickListener(v -> {
            getCurrentSeekBar();
            playVideoWithQuality(480);
        });
        playbackQuality_720.setOnClickListener(v -> {
            getCurrentSeekBar();
            playVideoWithQuality(720);
        });
        playbackQuality_1080.setOnClickListener(v -> {
            getCurrentSeekBar();
            playVideoWithQuality(1080);
        });
        playbackQuality_1440.setOnClickListener(v -> {
            getCurrentSeekBar();
            playVideoWithQuality(1440);
        });
        playbackQuality_2160.setOnClickListener(v -> {
            getCurrentSeekBar();
            playVideoWithQuality(2160);
        });

        resizeFill.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText("Fill");
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        resizeFit.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText("Fit");
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        resizeZoom.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText("Zoom");
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        resizeFixHeight.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText("Fixed\nHeight");
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        resizeFixWidth.setOnClickListener(view -> {
            player.play();
            playbackResizeTxt.setText("Fixed\nWidth");
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        closeBottomSheet.setOnClickListener(v -> {
            player.play();
            bottomSheetPlaybackSpeed.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        closeBottomSheet2.setOnClickListener(v -> {
            player.play();
            bottomSheetPlaybackQuality.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        closeBottomSheet3.setOnClickListener(v -> {
            player.play();
            bottomSheetPlaybackResize.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        closeBottomSheet4.setOnClickListener(v -> {
            player.play();
            bottomSheetAllNotes.setState(BottomSheetBehavior.STATE_COLLAPSED);
            player_notes_current_text.setText(null);
        });
        closeBottomSheet5.setOnClickListener(v -> {
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
                commentsAdapter = new CommentsAdapter(DeoraYoutubeActivity.this, commentsArrayList);
                recyclerViewComments.setAdapter(commentsAdapter);
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
                dialog.show();
            } else {
                dialog.show();
            }
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

    private String getVideoDurationSeconds(SimpleExoPlayer player) {
        int timeMs = (int) player.getDuration();
        return stringForTime(timeMs);
    }

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

    private void playVideoWithQuality(int quality) {
        qualitySaved = String.valueOf(quality);
        for (int i = 0; i < formatsAvailable.size(); i++) {
            int qlty = formatsAvailable.get(i).getQuality();
            String url = formatsAvailable.get(i).getUrl();
            if (qlty == quality) {
                if (!commonAudioUrl.equals("")) {
                    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Youtube Test"), new DefaultBandwidthMeter.Builder(this).build());
                    MediaItem mediaItemAudio = MediaItem.fromUri(Uri.parse(commonAudioUrl));
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
        playbackQualityTxt.setText(quality + "p");
        bottomSheetPlaybackQuality.setState(BottomSheetBehavior.STATE_COLLAPSED);
        player.play();
    }

    private void changeSpeed(float speed) {
        PlaybackParameters param = new PlaybackParameters(speed);
        player.setPlaybackParameters(param);
        player.play();
        playbackSpeedTxt.setText(speed + "x");
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
            player.removeListener(playbackStateListener);
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

}