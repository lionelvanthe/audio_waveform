package com.mp3cutter.soulappsworld.custom.mix;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mp3cutter.soulappsworld.MarkerView;
import com.mp3cutter.soulappsworld.WaveformViewAdvance;
import com.mp3cutter.soulappsworld.soundfile.CheapSoundFile;
import com.wellytech.audiotrim.R;

import java.io.File;

public class MixAudioView extends ConstraintLayout implements MarkerView.MarkerListener{

    private WaveformViewAdvance waveformViewAdvance;
    private MarkerView startMarker;
    private MarkerView endMarker;
    private MediaPlayer mediaPlayer;

    private CheapSoundFile mSoundFile;

    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private boolean mIsPlaying;
    private boolean mTouchDragging;
    private float mDensity;
    private ImageButton mPlayButton;
    private ImageButton mRewindButton;
    private ImageButton mFfwdButton;
    private Handler mHandler;
    private int mWidth;

    private int alphaWidth;

    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;

    private float mTouchStart;

    private MarkerTouchListener markerTouchListener;


    public MixAudioView(@NonNull Context context) {
        super(context);
        init();
    }

    public MixAudioView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MixAudioView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_mix_audio, this);
        waveformViewAdvance = findViewById(R.id.waveformMix);
        startMarker = findViewById(R.id.startmarkerMix);
        endMarker = findViewById(R.id.endmarkerMix);

        setupMarker(startMarker);
        setupMarker(endMarker);
    }

    private void setupMarker(MarkerView startMarker) {
        startMarker.setListener(this);
        startMarker.setFocusable(true);
        startMarker.setFocusableInTouchMode(true);
        startMarker.setElevation(10f);
    }

    long mLoadingLastUpdateTime = System.currentTimeMillis();


    public void loadFromFile(String mFilename, int width) {
        File mFile = new File(mFilename);
        String mExtension = getExtensionFromFilename(mFilename);
        mLoadingLastUpdateTime = System.currentTimeMillis();
        boolean mLoadingKeepGoing = true;

        final CheapSoundFile.ProgressListener listener = new CheapSoundFile.ProgressListener() {
            public boolean reportProgress(double fractionComplete) {
                long now = System.currentTimeMillis();
                if (now - mLoadingLastUpdateTime > 100) {
                    mLoadingLastUpdateTime = now;
                }
                return mLoadingKeepGoing;
            }
        };

        waveformViewAdvance.setWidth(width);

        startMarker.post(new Runnable() {
            @Override
            public void run() {
                startMarker.setMaxX(endMarker.getX() - endMarker.getWidth());
                endMarker.setMaxX(endMarker.getX());
                endMarker.setMinX(startMarker.getX() + startMarker.getWidth());
            }
        });

        try {
            mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(), listener);

            if (mSoundFile == null) {
                String name = mFile.getName().toLowerCase();
                String[] components = name.split("\\.");
                String err;
                if (components.length < 2) {
                    err = getResources().getString(R.string.no_extension_error);
                } else {
                    err = getResources().getString(R.string.bad_extension_error) + " " + components[components.length - 1];
                }
                Log.d("Thenv", "loadFromFile: " + err);
            }
            if (mLoadingKeepGoing) {
                finishOpeningSoundFile();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void finishOpeningSoundFile() {
        waveformViewAdvance.setSoundFile(mSoundFile);
        mMaxPos = waveformViewAdvance.maxPos();
        mTouchDragging = false;
        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        if (mEndPos > mMaxPos) mEndPos = mMaxPos;
        updateDisplay();
    }

    private synchronized void updateDisplay() {
        if (mIsPlaying) {
//            int now = mPlayer.getCurrentPosition() + mPlayStartOffset;
//            int frames = waveformViewAdvance.millisecsToPixels(now);
//            waveformViewAdvance.setPlayback(frames);
//            setOffsetGoalNoUpdate(frames - mWidth / 2);
//            if (now >= mPlayEndMsec) {
//                handlePause();
//            }
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                float saveVel = mFlingVelocity;

                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10) offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0) offsetDelta = 1;
                else if (offsetDelta < -10) offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0) offsetDelta = -1;
                else offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }

        waveformViewAdvance.setParameters(mStartPos, mEndPos, mOffset);
        waveformViewAdvance.invalidate();

    }

    private String getExtensionFromFilename(String filename) {
        return filename.substring(filename.lastIndexOf('.'), filename.length());
    }

    @Override
    public void markerTouchStart(MarkerView marker, float pos) {
        markerTouchListener.onTouchDown();
        mTouchDragging = true;
        mTouchStart = pos;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;

    }

    @Override
    public void markerTouchMove(MarkerView marker, float pos) {
        float delta = pos - mTouchStart;


//        if (marker == startMarker) {
//            Log.d("Thenv", "markerTouchMove: vo day");
//            mStartPos = trap((int) (mTouchInitialStartPos + delta));
//            mEndPos = trap((int) (mTouchInitialEndPos + delta));
//        } else {
//            mEndPos = trap((int) (mTouchInitialEndPos + delta));
//            if (mEndPos < mStartPos){
//                mEndPos = mStartPos;
//            }
//        }
        mStartPos = trap((int) (mTouchInitialStartPos + delta));

        mEndPos = trap((int) (mTouchInitialEndPos + delta));
        if (mEndPos < mStartPos){
            mEndPos = mStartPos;
        }
//        waveformViewAdvance.setParameters(mStartPos, mEndPos, mEndPos/8);
//        waveformViewAdvance.invalidate();

//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(waveformViewAdvance.getMeasuredWidth()  - (int) (width/2f + mEndPos - waveformViewAdvance.getX()), 200);
//        waveformViewAdvance.setLayoutParams(layoutParams);
//        waveformViewAdvance.setX(width/2f + mEndPos);

//        updateDisplay(waveformViewAdvance);

    }

    private int trap(int pos) {
        if (pos < 0) return 0;
        return pos;
    }

    @Override
    public void markerTouchEnd(MarkerView marker, float pos) {
        ViewGroup.LayoutParams layoutParams = waveformViewAdvance.getLayoutParams();
        layoutParams.height = getHeight();

        int widthWaveForm = (int) (endMarker.getX()  - startMarker.getX() - startMarker.getWidth());
        if (widthWaveForm <= 0 ) {
            widthWaveForm = 0;
            calculatorAlphaWidth(widthWaveForm);
            waveformViewAdvance.setVisibility(INVISIBLE);
        } else {
            calculatorAlphaWidth(widthWaveForm);
            waveformViewAdvance.setVisibility(VISIBLE);
            layoutParams.width = widthWaveForm;
            waveformViewAdvance.setLayoutParams(layoutParams);
            if (marker == startMarker) {
                waveformViewAdvance.setParameters(mStartPos, mEndPos, (int) ((marker.getX() + 4)/8));
                waveformViewAdvance.invalidate();
                waveformViewAdvance.setX(pos);
                endMarker.setMinX(startMarker.getX() + startMarker.getWidth());
            } else {
                startMarker.setMaxX(endMarker.getX() - endMarker.getWidth());
            }
        }
    }

    private void calculatorAlphaWidth(int width) {
        if (waveformViewAdvance.getWidth() - width > 0) {
            alphaWidth += waveformViewAdvance.getWidth() - width;
        } else {
            alphaWidth -= (width -waveformViewAdvance.getWidth());
        }
    }

    @Override
    public void markerFocus(MarkerView marker) {

    }

    @Override
    public void markerLeft(MarkerView marker, int velocity) {

    }

    @Override
    public void markerRight(MarkerView marker, int velocity) {

    }

    @Override
    public void markerEnter(MarkerView marker) {

    }

    @Override
    public void markerKeyUp() {

    }

    @Override
    public void markerDraw() {

    }

    public void waveformZoomIn(int width) {
        waveformViewAdvance.zoomIn();
        updateViewAfterZoom(width);
    }

    public void waveformZoomOut(int width) {
        waveformViewAdvance.zoomOut();
        updateViewAfterZoom(width);
    }

    public MarkerView getStartMarker() {
        return startMarker;
    }

    public MarkerView getEndMarker() {
        return endMarker;
    }

    public int getSampleRate() {
        return mSoundFile.getSampleRate();
    }

    public int getSamplesPerFrame() {
        return mSoundFile.getSamplesPerFrame();
    }

    public int getNumFrames() {
        return mSoundFile.getNumFrames();
    }

    public MarkerTouchListener getMarkerTouchListener() {
        return markerTouchListener;
    }

    public void setMarkerTouchListener(MarkerTouchListener markerTouchListener) {
        this.markerTouchListener = markerTouchListener;
    }

    public WaveformViewAdvance getWaveformViewAdvance() {
        return waveformViewAdvance;
    }

    private void updateViewAfterZoom(int width) {
        mStartPos = waveformViewAdvance.getStart();
        mEndPos = waveformViewAdvance.getEnd();
        mMaxPos = waveformViewAdvance.maxPos();
        mOffset = waveformViewAdvance.getOffset();
        mOffsetGoal = mOffset;

        ViewGroup.LayoutParams layoutParamsWave = waveformViewAdvance.getLayoutParams();
        layoutParamsWave.width = width - alphaWidth;
        waveformViewAdvance.setLayoutParams(layoutParamsWave);
        updateDisplay();
        startMarker.setMaxX(endMarker.getX() - endMarker.getWidth());
        endMarker.setMaxX(width + endMarker.getWidth());
        endMarker.setMinX(startMarker.getX() + startMarker.getWidth());
    }

    public interface MarkerTouchListener {
        void onTouchDown();
    }
}
