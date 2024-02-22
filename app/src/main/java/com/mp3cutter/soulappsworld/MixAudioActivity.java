package com.mp3cutter.soulappsworld;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.mp3cutter.soulappsworld.custom.mix.MixAudioView;
import com.wellytech.audiotrim.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MixAudioActivity extends Activity implements MixAudioView.PlayerListener {
    private boolean mTouchDragging;
    private float mDensity;
    private ImageButton mPlayButton;
    private ImageButton mZoomIn;
    private ImageButton mZoonOut;
    private ImageButton mSave;
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
    int now = -10;

    private MixAudioView mixAudioViewMax;

    private ArrayList<Integer> startPositions = new ArrayList<>();

    private Runnable runnableWaveForm = new Runnable() {
        @Override
        public void run() {
            if (!startPositions.isEmpty()) {
                if (now >= startPositions.get(0)) {
                    for (MixAudioView mixView: mixAudioViews) {
                        if (mixView.getmPlayStartMsec() + mixView.getAlphaMills() == startPositions.get(0)) {
                            mixView.onPlay();
                            setMixAudioViewMax(mixView);
                        }
                    }
                    startPositions.remove(0);
                }
            }
            if (mixAudioViewMax == null) {
                now += 10;
            } else {
                int millsPlayed = mixAudioViewMax.getMediaPlayer().getCurrentPosition() + mixAudioViewMax.getAlphaMills();
                if (millsPlayed > now){
                    now = millsPlayed;
                }
            }
            int frames = viewTime.millisecsToPixels(now);
            horizontalScroll.scrollTo(frames/ viewTime.getZoomLevelRemain(), 0);
            mHandler.removeCallbacks(runnableWaveForm);
            mHandler.postDelayed(runnableWaveForm, 10);
        }
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                        mHandler.removeCallbacks(runnableWaveForm);
                        break;
                    case MotionEvent.ACTION_UP:
                        int millisecs = viewTime.pixelsToMillisecs(horizontalScroll.getScrollX())*viewTime.getZoomLevelRemain();
                        now = millisecs;
                        if (mixAudioViewMax != null) {
                            if (mixAudioViewMax.ismIsPlaying()) {
                                for (MixAudioView mixAudioView: mixAudioViews) {
                                    mixAudioView.getMediaPlayer().seekTo(millisecs);
                                }
                                mHandler.removeCallbacks(runnableWaveForm);
                                mHandler.postDelayed(runnableWaveForm, 10);
                            }
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
        mSave = (ImageButton) findViewById(R.id.ivSave);
        mRewindButton = (ImageButton) findViewById(R.id.rew);
        mFfwdButton = (ImageButton) findViewById(R.id.ffwd);
        viewTime = (ViewTime) findViewById(R.id.viewTime);

//        mWaveformView = (WaveformView) findViewById(R.id.waveform);
//        mWaveformView.setListener(this);
        root = (ConstraintLayout) findViewById(R.id.root);

        mZoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleZoom(true);
            }
        });

        mZoonOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleZoom(false);
            }
        });

        mSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile();
            }
        });

        mPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (MixAudioView mixView: mixAudioViews) {
                    if (mixView.getStartMarker().getX() + mixView.getAlphaPosition() <= horizontalScroll.getScrollX()) {
                        mixView.onPlay();
                        startPositions.remove(0);
                        setMixAudioViewMax(mixView);
                    }
                    if (now > 0) {
                        mixView.getMediaPlayer().seekTo(now);
                    }
                }
                mHandler.removeCallbacks(runnableWaveForm);
                mHandler.postDelayed(runnableWaveForm, 10);
            }
        });
    }
    private void saveFile() {
//        /storage/emulated/0/VideoSlideShowPro/Templates/defaultmusic/0x0700000000000070/test.mp3
        // create silent audio
//        String commandLine = "-f " +
//                "lavfi " +
//                "-i " +
//                "anullsrc=r=44100:cl=mono " +
//                "-t 00:05:00 " +
//                "-q:a " +
//                "9 " +
//                "-acodec " +
//                "libmp3lame " +
//                "/storage/emulated/0/VideoSlideShowPro/Templates/defaultmusic/0x0700000000000070/test.mp3";

        //add silent audio at first file
//        String commandLine = "-y " +
//                "-i " +
//                "'/storage/emulated/0/VideoSlideShowPro/Templates/defaultmusic/0x0700000000000070/Love The Sky.m4a' " +
//                "-filter_complex " +
//                "\"aevalsrc=0:d=200[s1];[s1][0:a]concat=n=2:v=0:a=1[aout]\" " +
//                "-c:v copy " +
//                "-map 0:v? " +
//                "-map [aout] " +
//                "/storage/emulated/0/VideoSlideShowPro/Templates/defaultmusic/0x0700000000000070/test2.mp3";
        //add silent audio between 2 file
//        String commandLine = "-y " +
//                "-i " +
//                "'/storage/emulated/0/Android/data/com.miui.player/files/Music/built/Funk Down.mp3' " +
//                "-i " +
//                "'/storage/emulated/0/VideoSlideShowPro/Templates/defaultmusic/0x0700000000000070/Love The Sky.m4a' " +
//                "-filter_complex " +
//                "\"aevalsrc=0:d=200[s1];[0:a][s1]concat=n=2:v=0:a=1[ac1];[ac1][1:a]concat=n=2:v=0:a=1[aout]\" " +
//                "-map 0:v? " +
//                "-map [aout] " +
//                "/storage/emulated/0/VideoSlideShowPro/Templates/defaultmusic/0x0700000000000070/test2.mp3";

        //mix audio
        String commandLine = "-y " +
                "-i " +
                "audio-1 " +
                "-i auido-2 " +
                "-filter_complex " +
                "\"amix=inputs=2:duration=longest:dropout_transition=0, volume=2\" " +
                "<output audio file>";
        FFmpeg.executeAsync(commandLine, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == 0) {
                    Log.d("Thenv", "apply: vo day");
                }
            }
        });
    }

    private void handleZoom(boolean isZoomIn) {
        if (isZoomIn) {
            viewTime.zoomIn();
        } else {
            viewTime.zoomOut();
        }
        ViewGroup.LayoutParams layoutParams = viewTime.getLayoutParams();
        layoutParams.width = viewTime.getOriginWidth()*(viewTime.getZoomLevel()-1);
        viewTime.setLayoutParams(layoutParams);

        int i = 0;
        for (MixAudioView mixView: mixAudioViews) {
            float ratioDuration = hashMapDuration.get(fileNames.get(i))/(float)maxDuration;
            int widthWave = (int) ((width*ratioDuration)*(viewTime.getZoomLevel() - 1));
            ViewGroup.LayoutParams layoutParamsMixView = mixView.getLayoutParams();
            layoutParamsMixView.width = widthWave + mixView.getEndMarker().getWidth() + mixView.getStartMarker().getWidth();
            mixView.setMaxX(layoutParams.width - layoutParamsMixView.width - mixView.getMinX());
            if (isZoomIn) {
                mixView.waveformZoomIn(widthWave);
            } else {
                mixView.waveformZoomOut(widthWave);
            }
            i++;
        }
    }

    private void setMixAudioViewMax(MixAudioView mixView) {
        if (mixAudioViewMax == null) {
            mixAudioViewMax = mixView;
        } else {
            if (mixView.getmPlayEndMsec() + mixView.getAlphaPosition() > mixAudioViewMax.getmPlayEndMsec() + mixAudioViewMax.getAlphaPosition()) {
                mixAudioViewMax = mixView;
            }
        }
    }


    private void loadFromFile(String mFilename, int index) {


        MixAudioView mixAudioView = new MixAudioView(this, null);

        mixAudioView.setMarkerTouchListener(new MixAudioView.MarkerTouchListener() {
            @Override
            public void onTouchDown() {
                horizontalScroll.requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onTouchUp(int startPos, MixAudioView mixAudioView1) {
                int index = mixAudioViews.indexOf(mixAudioView1);
                startPositions.remove(index);
                startPositions.add(index,startPos);
                Collections.sort(startPositions);
            }
        });

        mixAudioView.getStartMarker().post(new Runnable() {
            @Override
            public void run() {
                setupMixAudioView(mFilename, mixAudioView, index);
                if (hashMapDuration.get(mFilename) == maxDuration) {
                    setupViewTime(mixAudioView);
                }
            }
        });
        mixAudioViews.add(mixAudioView);
        root.addView(mixAudioView);
    }

    private void setupViewTime(MixAudioView mixAudioView) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width/2 + 2 *(width + mixAudioView.getStartMarker().getWidth() + mixAudioView.getEndMarker().getWidth()), 50);
        viewTime.setLayoutParams(layoutParams);
        viewTime.setOriginWidth(layoutParams.width);
        viewTime.setX( width/2f);
        viewTime.recomputeHeights(mDensity);
        viewTime.setmSampleRate(mixAudioView.getSampleRate());
        viewTime.setmSamplesPerFrame(mixAudioView.getSamplesPerFrame());
        viewTime.setNumSample(mixAudioView.getNumFrames());
        viewTime.calNoNam();
        viewTime.invalidate();
    }

    private void setupMixAudioView(String mFilename, MixAudioView mixAudioView, int index) {
        ViewGroup.LayoutParams layoutParams;
        float ratioDuration = hashMapDuration.get(mFilename)/(float)maxDuration;
        layoutParams = new ViewGroup.LayoutParams((int) (width*ratioDuration) + mixAudioView.getStartMarker().getWidth() + mixAudioView.getEndMarker().getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
        mixAudioView.setPlayerListener(MixAudioActivity.this);
        mixAudioView.loadFromFile(mFilename,(int) (width*ratioDuration));
        mixAudioView.initMediaPlayer(mFilename, MixAudioActivity.this);

        mixAudioView.setLayoutParams(layoutParams);
        mixAudioView.setY(index *300 + index *40);
        mixAudioView.setX(width/2f - mixAudioView.getStartMarker().getWidth());
        mixAudioView.setMinX((int) mixAudioView.getX());
        //a la width cua view time
        int a = 2*(width + mixAudioView.getStartMarker().getWidth() + mixAudioView.getEndMarker().getWidth()) + width/2;
        int b = a - layoutParams.width - mixAudioView.getMinX();
        mixAudioView.setMaxX(b);
        startPositions.add(0);
    }

    @Override
    public void onCurrentPosition(int position) {
    }

    @Override
    public void pause() {
//        mHandler.removeCallbacks(runnableWaveForm);
    }

    @Override
    public void completed(MediaPlayer mediaPlayer) {

    }
}