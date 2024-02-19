package com.mp3cutter.soulappsworld;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mp3cutter.soulappsworld.custom.mix.MixAudioView;
import com.mp3cutter.soulappsworld.soundfile.CheapSoundFile;
import com.wellytech.audiotrim.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class MixAudioActivity extends Activity implements MixAudioView.PlayerListener {
    private boolean mIsPlaying;
    private MediaPlayer mPlayer;
    private boolean mTouchDragging;
    private float mDensity;
    private ImageButton mPlayButton;
    private ImageButton mZoomIn;
    private ImageButton mZoonOut;
    private ImageButton mRewindButton;
    private ImageButton mFfwdButton;
    private Handler mHandler;

    private ConstraintLayout root;
    private ViewTime viewTime;
    private int width = 1080;
    private HashMap<String, Long> hashMapDuration = new HashMap<>();
    private long maxDuration = 0;

    private ArrayList<MixAudioView> mixAudioViews = new ArrayList<>();
    private ArrayList<String> fileNames = new ArrayList<>();

    private HorizontalScrollView horizontalScroll;
    private float mTouchStart;
    private int mMarkerTopOffset;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlayer = null;
        mIsPlaying = false;

        mHandler = new Handler();

        Intent intent = getIntent();

        if (intent.getBooleanExtra("privacy", false)) {
            return;
        }

        loadGui();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;


        fileNames = intent.getStringArrayListExtra("file_to_mix");
        for (int i = 0 ;i < fileNames.size(); i++) {
            SongMetadataReader metadataReader = new SongMetadataReader(this, fileNames.get(i));
            if (metadataReader.duration > maxDuration) {
                maxDuration = metadataReader.duration;
            }
            hashMapDuration.put(fileNames.get(i), metadataReader.duration);
        }

        for (int i = 0 ;i < fileNames.size(); i++) {
            loadFromFile(fileNames.get(i), i);
        }

        horizontalScroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        mHandler.removeCallbacks(runnableWaveForm);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mIsPlaying) {
//                            int millisecs = waveformViews.get(0).pixelsToMillisecs(horizontalScroll.getScrollX())*waveformViews.get(0).getZoomLevelRemain();
//                            mPlayer.seekTo(millisecs);
//                            mHandler.removeCallbacks(runnableWaveForm);
//                            mHandler.postDelayed(runnableWaveForm, 0);
                        }
                        break;
                }
                return false;
            }
        });

    }

    private void loadGui() {
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.activity_mix_audio);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;

        mMarkerTopOffset = (int) (10 * mDensity);

        horizontalScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollMix);
        mZoomIn = (ImageButton) findViewById(R.id.zoomIn);
        mZoonOut = (ImageButton) findViewById(R.id.zoomOut);
        mPlayButton = (ImageButton) findViewById(R.id.play);
        mRewindButton = (ImageButton) findViewById(R.id.rew);
        mFfwdButton = (ImageButton) findViewById(R.id.ffwd);
        viewTime = (ViewTime) findViewById(R.id.viewTime);

//        mWaveformView = (WaveformView) findViewById(R.id.waveform);
//        mWaveformView.setListener(this);
        root = (ConstraintLayout) findViewById(R.id.root);

        mZoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                viewTime.zoomIn();
                ViewGroup.LayoutParams layoutParams = viewTime.getLayoutParams();
                layoutParams.width = width*(viewTime.getZoomLevel()) + width/2;
                viewTime.setLayoutParams(layoutParams);

                int i = 0;
                for (MixAudioView mixView: mixAudioViews) {
                    float ratioDuration = hashMapDuration.get(fileNames.get(i))/(float)maxDuration;
                    int widthWave = (int) (((width*ratioDuration) - WaveformViewAdvance.spaceColum*mixView.getWaveformViewAdvance().getZoomLevelRemain())*(viewTime.getZoomLevel() - 1));
                    ViewGroup.LayoutParams layoutParamsMixView = mixView.getLayoutParams();
                    layoutParamsMixView.width = widthWave + mixView.getEndMarker().getWidth() + mixView.getStartMarker().getWidth();
//                    mixView.setLayoutParams(layoutParams);
                    mixView.waveformZoomIn(widthWave);
                    i++;
                }
            }
        });

        mZoonOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                viewTime.zoomOut();
                ViewGroup.LayoutParams layoutParams =viewTime.getLayoutParams();
                layoutParams.width = width*(viewTime.getZoomLevel()) +  width/2;
                viewTime.setLayoutParams(layoutParams);
                int i = 0;
                for (MixAudioView mixView: mixAudioViews) {

                    float ratioDuration = hashMapDuration.get(fileNames.get(i))/(float)maxDuration;
                    int widthWave = (int) (((width*ratioDuration) - WaveformViewAdvance.spaceColum*mixView.getWaveformViewAdvance().getZoomLevelRemain())*(viewTime.getZoomLevel() - 1));
                    ViewGroup.LayoutParams layoutParamsMixView = mixView.getLayoutParams();
                    layoutParamsMixView.width = widthWave + mixView.getEndMarker().getWidth() + mixView.getStartMarker().getWidth();
                    mixView.waveformZoomOut(widthWave);
                    i++;
                }
            }
        });

        mPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (MixAudioView mixView: mixAudioViews) {
                    mixView.onPlay();
                }
            }
        });

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(2*width, 50);
        viewTime.setLayoutParams(layoutParams);
        viewTime.setX( width/2f);
    }


    private void loadFromFile(String mFilename, int index) {


        MixAudioView mixAudioView = new MixAudioView(this, null);

        mixAudioView.setMarkerTouchListener(new MixAudioView.MarkerTouchListener() {
            @Override
            public void onTouchDown() {
                horizontalScroll.requestDisallowInterceptTouchEvent(true);
            }
        });

        mixAudioView.getStartMarker().post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams;
                float ratioDuration = hashMapDuration.get(mFilename)/(float)maxDuration;
                layoutParams = new ViewGroup.LayoutParams((int) (width*ratioDuration) - WaveformViewAdvance.spaceColum*4 + mixAudioView.getStartMarker().getWidth() + mixAudioView.getEndMarker().getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
                mixAudioView.setPlayerListener(MixAudioActivity.this);
                mixAudioView.loadFromFile(mFilename,(int) (width*ratioDuration) - 32);
                mixAudioView.initMediaPlayer(mFilename, MixAudioActivity.this);

                mixAudioView.setLayoutParams(layoutParams);
                mixAudioView.setY(index*300 + index*40);
                mixAudioView.setX(width/2f - mixAudioView.getStartMarker().getWidth());

                viewTime.recomputeHeights(mDensity);
                viewTime.setmSampleRate(mixAudioView.getSampleRate());
                viewTime.setmSamplesPerFrame(mixAudioView.getSamplesPerFrame());
                viewTime.setNumSample(mixAudioView.getNumFrames());
                viewTime.calNoNam();
                viewTime.invalidate();
            }
        });
        mixAudioViews.add(mixAudioView);
        root.addView(mixAudioView);
    }

    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
//        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
    }

    @Override
    public void onCurrentPosition(int position) {
        horizontalScroll.scrollTo(position, 0);
    }
}