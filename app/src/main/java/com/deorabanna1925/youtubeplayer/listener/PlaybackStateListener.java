package com.deorabanna1925.youtubeplayer.listener;

import com.deorabanna1925.youtubeplayer.databinding.ActivityDeoraYoutubeBinding;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;

public class PlaybackStateListener implements Player.Listener {

    private final ActivityDeoraYoutubeBinding binding;

    public PlaybackStateListener(ActivityDeoraYoutubeBinding binding) {
        this.binding = binding;
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        String stateString;
        switch (playbackState) {
            case ExoPlayer.STATE_IDLE:
                stateString = "ExoPlayer.STATE_IDLE      -";
                break;
            case ExoPlayer.STATE_BUFFERING:
                stateString = "ExoPlayer.STATE_BUFFERING -";
                break;
            case ExoPlayer.STATE_READY:
                stateString = "ExoPlayer.STATE_READY     -";
                break;
            case ExoPlayer.STATE_ENDED:
                stateString = "ExoPlayer.STATE_ENDED     -";
                break;
            default:
                stateString = "UNKNOWN_STATE             -";
                break;
        }
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        binding.playerView.setKeepScreenOn(reason != Player.STATE_IDLE && reason != Player.STATE_ENDED && playWhenReady);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }


}