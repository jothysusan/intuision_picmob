package com.picmob.android.activity;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.picmob.android.R;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.ExceptionHandler;
import com.picmob.android.utils.UtilsFunctions;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoPlayerActivity extends BaseActivity implements Player.Listener {

    @BindView(R.id.video_view)
    PlayerView videoView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private SimpleExoPlayer mPlayer;
    DefaultDataSource.Factory dataSourceFactory;
    private String videoURL;
    private ImageView fullscreenButton;
    ImageButton exoPlayButton;
    boolean fullscreen = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        mPlayer = new SimpleExoPlayer.Builder(this).build();

        fullscreenButton = videoView.findViewById(R.id.exo_fullscreen_icon);
        exoPlayButton = videoView.findViewById(R.id.exo_play);
        fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this,
                            R.drawable.exo_ic_fullscreen_exit));
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        videoView.setPlayer(mPlayer);
        mPlayer.addListener(this);
        mPlayer.setPlayWhenReady(true);
        dataSourceFactory = new DefaultDataSourceFactory(this);


        if (getIntent().getExtras() != null) {
            videoURL = getIntent().getStringExtra(AppConstants.VIDEO_URL);
            if (UtilsFunctions.isNetworkAvail(VideoPlayerActivity.this)) {
                setValues(videoURL);
            }
        }
    }


    private void setValues(String mediaUrl) {

        if (mediaUrl != null && !mediaUrl.isEmpty()) {
            videoView.setVisibility(View.VISIBLE);
            ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(
                    Uri.parse(mediaUrl)));
            mPlayer.setMediaSource(mediaSource);
            mPlayer.prepare();
        } else {
            videoView.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        if (playbackState == Player.STATE_BUFFERING)
            progressBar.setVisibility(View.VISIBLE);
        else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED)
            progressBar.setVisibility(View.INVISIBLE);
        if (playbackState == Player.STATE_ENDED) {
            exoPlayButton.setImageResource(R.drawable.ic_replay);
        } else {
            exoPlayButton.setImageResource(R.drawable.exo_icon_play);
        }
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        UtilsFunctions.showToast(getApplicationContext(), AppConstants.shortToast, error.getMessage());
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }
}
