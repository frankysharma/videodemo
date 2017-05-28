package com.rahul.shrofiledemo;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.rahul.shrofiledemo.utils.Utils;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener
,View.OnClickListener, MediaPlayer.OnCompletionListener{
    private SurfaceView mSurfaceView;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mSurfaceHolder;
    private RelativeLayout rlMiddle;
    private ImageButton btPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        rlMiddle = (RelativeLayout)findViewById(R.id.rlMiddle);
        btPlay = (ImageButton)findViewById(R.id.btPlay);
        btPlay.setEnabled(false);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightForPreview = displayMetrics.widthPixels;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(heightForPreview,heightForPreview);
        lp.gravity = Gravity.CENTER_VERTICAL;
        rlMiddle.setLayoutParams(lp);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(PlayerActivity.this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDisplay(mSurfaceHolder);
        String path = Utils.getPrefrences(PlayerActivity.this,"VIDEO");
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setOnCompletionListener(PlayerActivity.this);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(PlayerActivity.this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        btPlay.setEnabled(true);
//        mMediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btBack:
                finish();
                break;
            case R.id.btPlay:
                if (!mMediaPlayer.isPlaying()){
                    mMediaPlayer.start();
                    btPlay.setImageResource(R.drawable.ic_pause);
                } else {
                    mMediaPlayer.pause();
                    btPlay.setImageResource(R.drawable.ic_play);
                }
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        btPlay.setImageResource(R.drawable.ic_play);
    }
}