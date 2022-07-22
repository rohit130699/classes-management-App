package com.example.online_class;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

public class viewvideo extends AppCompatActivity {
    TextView t1;
    boolean flag=false;
    SimpleExoPlayer exoPlayer;
    private PlayerView exp;
    String filetitle,fileurl;
    ProgressBar pbar;
    ImageView btnfullscr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewvideo);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        t1=(TextView)findViewById(R.id.titletv);
        pbar=(ProgressBar)findViewById(R.id.progress_bar);
        btnfullscr=(ImageView)findViewById(R.id.fullscreen);
        exp=findViewById(R.id.exp_view);
        //mtextView.setText(title);
        //ctx = getIntent().getStringExtra("App_context");
        filetitle = getIntent().getStringExtra("filename");
        fileurl = getIntent().getStringExtra("fileurl");
        t1.setText(filetitle);
        t1.setSelected(true);
        LoadControl loadcontrol = new LoadControl() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onTracksSelected(Renderer[] renderers, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onStopped() {

            }

            @Override
            public void onReleased() {

            }

            @Override
            public Allocator getAllocator() {
                return null;
            }

            @Override
            public long getBackBufferDurationUs() {
                return 0;
            }

            @Override
            public boolean retainBackBufferFromKeyframe() {
                return false;
            }

            @Override
            public boolean shouldContinueLoading(long bufferedDurationUs, float playbackSpeed) {
                return false;
            }

            @Override
            public boolean shouldStartPlayback(long bufferedDurationUs, float playbackSpeed, boolean rebuffering) {
                return false;
            }
        };
        try{
            BandwidthMeter bandwidthMeter=new DefaultBandwidthMeter.Builder(getApplicationContext()).build();
            TrackSelector trackSelector=new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer=(SimpleExoPlayer) ExoPlayerFactory.newSimpleInstance(getApplicationContext());
            Uri video=Uri.parse(fileurl);
            DefaultHttpDataSourceFactory dsfact=new DefaultHttpDataSourceFactory("video");
            ExtractorsFactory extractorsFactory=new DefaultExtractorsFactory();
            MediaSource mediaSource=new ExtractorMediaSource(video,dsfact,extractorsFactory,null,null);
            exp.setPlayer(exoPlayer);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                }

                @Override
                public void onLoadingChanged(boolean isLoading) {

                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if(playbackState == exoPlayer.STATE_BUFFERING){
                        pbar.setVisibility(View.VISIBLE);
                    }else if(playbackState == exoPlayer.STATE_READY){
                        pbar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {

                }

                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {

                }

                @Override
                public void onPositionDiscontinuity(int reason) {

                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                }

                @Override
                public void onSeekProcessed() {

                }
            });

            btnfullscr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(flag){
                        btnfullscr.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen));
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        flag=false;
                    }else{
                        btnfullscr.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen_exit));
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        flag=true;
                    }
                }
            });
        }
        catch(Exception e){
            Log.e("ViewHolder","expolayer error" + e.toString());
            Toast.makeText(getApplicationContext(),"Some Error has Occurred in loading Video !!Please try again later..",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.getPlaybackState();
    }
}