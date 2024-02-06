package com.mp3cutter.soulappsworld;

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

import com.mp3cutter.soulappsworld.soundfile.CheapSoundFile;
import com.wellytech.audiotrim.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class MixAudioActivity extends Activity implements WaveformViewAdvance.WaveformListener, MarkerView.MarkerListener{

    private long mLoadingStartTime;
    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private ProgressDialog mProgressDialog;
    private CheapSoundFile mSoundFile;
    private File mFile;
//    private String mFilename;
//    private String mDstFilename;
//    private String mArtist;
//    private String mAlbum;
//    private String mGenre;
//    private String mTitle;
//    private int mYear;
    private String mExtension;
    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private boolean mIsPlaying;
    private MediaPlayer mPlayer;
    private boolean mTouchDragging;
    private float mDensity;
    private ImageButton mPlayButton;
    private ImageButton mRewindButton;
    private ImageButton mFfwdButton;
    private Handler mHandler;

    private ConstraintLayout root;

    private ImageButton mZoomIn;
    private ImageButton mZoonOut;

    private ViewTime viewTime;
    private int width = 1080;
    private HashMap<String, Long> hashMapDuration = new HashMap<>();
    private long maxDuration = 0;
    private boolean mCanSeekAccurately;

    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;

    private ArrayList<WaveformViewAdvance> waveformViews = new ArrayList<>();
    private ArrayList<String> fileNames = new ArrayList<>();
    private WaveformViewAdvance currentWaveForm;
    private int currentPositionWaveForm = 0;

    private boolean mKeyDown;

    private HorizontalScrollView test;

    private MarkerView mStartMarker;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;

    private Runnable runnableWaveForm = new Runnable() {
        @Override
        public void run() {
            //Log.d("Thenv", "onTouch: runnableWaveForm");
            int now = mPlayer.getCurrentPosition();
            int frames = waveformViews.get(0).millisecsToPixels(now);
//            waveformViews.get(0).setX(width/2f - frames/(float) waveformViews.get(0).getZoomLevelRemain());
//            viewTime.setX(width/2f - frames/(float) waveformViews.get(0).getZoomLevelRemain());
//            root.setX(-frames/(float) (waveformViews.get(0).getZoomLevelRemain()));

            test.scrollTo(frames/(int) waveformViews.get(0).getZoomLevelRemain(), 0);
            mHandler.removeCallbacks(runnableWaveForm);
            mHandler.postDelayed(runnableWaveForm, 10);
        }
    };
    private float mTouchStart;
    private int mMarkerTopOffset;


    @Override
    public void waveformTouchStart(float x) {
//        mTouchDragging = true;
//        mTouchStart = x;
//        mTouchInitialOffset = mOffset;
//        mFlingVelocity = 0;
//        mWaveformTouchStartMsec = System.currentTimeMillis();
    }

    @Override
    public void waveformTouchMove(float x) {
//        mOffset = trap((int) (mTouchInitialOffset + (mTouchStart - x)));
//        updateDisplay();
    }

    @Override
    public void waveformTouchEnd() {
    }

    @Override
    public void waveformFling(float x) {

    }

    @Override
    public void waveformDraw() {
    }

    @Override
    public void waveformZoomIn() {
//        Log.d("Thenv", "waveformZoomIn: " + viewTime.getMeasuredWidth()/(viewTime.getZoomLevel()));
        viewTime.zoomIn();
        ViewGroup.LayoutParams  layoutParams = new ViewGroup.LayoutParams(width*(viewTime.getZoomLevel()) + 540, 50);
        viewTime.setLayoutParams(layoutParams);
        layoutParams.height = 200;
        int i = 0;
        for (WaveformViewAdvance mWaveformView: waveformViews) {
            mWaveformView.zoomIn();
            mStartPos = mWaveformView.getStart();
            mEndPos = mWaveformView.getEnd();
            mMaxPos = mWaveformView.maxPos();
            mOffset = mWaveformView.getOffset();
            mOffsetGoal = mOffset;

            float ratioDuration = hashMapDuration.get(fileNames.get(i))/(float)maxDuration;
            ViewGroup.LayoutParams layoutParamsWave = new ViewGroup.LayoutParams((int) (((width*ratioDuration) - WaveformViewAdvance.spaceColum*mWaveformView.getZoomLevelRemain())*(viewTime.getZoomLevel() - 1)), 200);
            mWaveformView.setLayoutParams(layoutParamsWave);
            updateDisplay(mWaveformView);
            i++;
        }

    }

    @Override
    public void waveformZoomOut() {

//        Log.d("Thenv", "waveformZoomOut: " + viewTime.getMeasuredWidth()*(viewTime.getZoomLevel() - 1));

        viewTime.zoomOut();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width*(viewTime.getZoomLevel()) + 540, 50);
        viewTime.setLayoutParams(layoutParams);
        int i = 0;
        for (WaveformViewAdvance mWaveformView: waveformViews) {
            mWaveformView.zoomOut();
            mStartPos = mWaveformView.getStart();
            mEndPos = mWaveformView.getEnd();
            mMaxPos = mWaveformView.maxPos();
            mOffset = mWaveformView.getOffset();
            mOffsetGoal = mOffset;

            float ratioDuration = hashMapDuration.get(fileNames.get(i))/(float)maxDuration;
            ViewGroup.LayoutParams layoutParamsWave = new ViewGroup.LayoutParams((int) (((width*ratioDuration) - WaveformViewAdvance.spaceColum*mWaveformView.getZoomLevelRemain())*(viewTime.getZoomLevel() - 1)), 200);
            mWaveformView.setLayoutParams(layoutParamsWave);
            updateDisplay(mWaveformView);
            i++;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlayer = null;
        mIsPlaying = false;

        Intent intent = getIntent();

        if (intent.getBooleanExtra("privacy", false)) {
            return;
        }

        // If the Ringtone media select activity was launched via a
        // GET_CONTENT intent, then we shouldn't display a "saved"
        // message when the user saves, we should just return whatever
        // they create.


        mSoundFile = null;

        mHandler = new Handler();


        loadGui();

//        mHandler.postDelayed(mTimerRunnable, 100);

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



        loadFromFile(fileNames.get(0), 0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFromFile(fileNames.get(1), 1);

            }
        }, 500);

        // Create the MediaPlayer in a background thread
        mCanSeekAccurately = false;
        new Thread() {
            public void run() {
                mCanSeekAccurately = SeekTest.CanSeekAccurately(getPreferences(Context.MODE_PRIVATE));

                System.out.println("Seek test done, creating media player.");
                try {
                    MediaPlayer player = new MediaPlayer();
                    player.setDataSource(mFile.getAbsolutePath());
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.prepare();
                    mPlayer = player;
                } catch (final java.io.IOException e) {
                    Runnable runnable = new Runnable() {
                        public void run() {
//                            handleFatalError("ReadError", getResources().getText(R.string.read_error), e);
                        }
                    };
                    mHandler.post(runnable);
                }
                ;
            }
        }.start();
        
        test.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mHandler.removeCallbacks(runnableWaveForm);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mIsPlaying) {
                            int millisecs = waveformViews.get(0).pixelsToMillisecs(test.getScrollX())*waveformViews.get(0).getZoomLevelRemain();
                            mPlayer.seekTo(millisecs);
                            mHandler.removeCallbacks(runnableWaveForm);
                            mHandler.postDelayed(runnableWaveForm, 0);
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

        test = (HorizontalScrollView) findViewById(R.id.asdfasdf);
        mZoomIn = (ImageButton) findViewById(R.id.zoomIn);
        mZoonOut = (ImageButton) findViewById(R.id.zoomOut);
        mPlayButton = (ImageButton) findViewById(R.id.play);
        mPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(waveformViews.get(1));
            }
        });
        mRewindButton = (ImageButton) findViewById(R.id.rew);
        mFfwdButton = (ImageButton) findViewById(R.id.ffwd);
        viewTime = (ViewTime) findViewById(R.id.viewTime);

//        mWaveformView = (WaveformView) findViewById(R.id.waveform);
//        mWaveformView.setListener(this);
        root = (ConstraintLayout) findViewById(R.id.root);
        mMaxPos = 0;
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;


        mZoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                waveformZoomIn();

            }
        });

        mZoonOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                waveformZoomOut();
            }
        });


        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(2*width, 50);
        viewTime.setLayoutParams(layoutParams);
        viewTime.setX( width/2f);
//        root.setX(540);

//        if (mSoundFile != null && !mWaveformView.hasSoundFile()) {
//            mWaveformView.setSoundFile(mSoundFile);
//            mWaveformView.recomputeHeights(mDensity);
//            mMaxPos = mWaveformView.maxPos();
//        }
//        mStartVisible = true;
//        mEndVisible = true;
//
//        updateDisplay();
    }

    private void loadFromFile(String mFilename, int index) {
        mFile = new File(mFilename);
        mExtension = getExtensionFromFilename(mFilename);

//        mTitle = metadataReader.mTitle;
//        mArtist = metadataReader.mArtist;
//        mAlbum = metadataReader.mAlbum;
//        mYear = metadataReader.mYear;
//        mGenre = metadataReader.mGenre;

        String titleLabel = "mTitle";
//        if (mArtist != null && mArtist.length() > 0) {
//            titleLabel += " - " + mArtist;
//        }
        setTitle(titleLabel);

        mLoadingStartTime = System.currentTimeMillis();
        mLoadingLastUpdateTime = System.currentTimeMillis();
        mLoadingKeepGoing = true;
        mProgressDialog = new ProgressDialog(MixAudioActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.progress_dialog_loading);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                mLoadingKeepGoing = false;
            }
        });
        mProgressDialog.show();

        final CheapSoundFile.ProgressListener listener = new CheapSoundFile.ProgressListener() {
            public boolean reportProgress(double fractionComplete) {
                long now = System.currentTimeMillis();
                if (now - mLoadingLastUpdateTime > 100) {
                    mProgressDialog.setProgress((int) (mProgressDialog.getMax() * fractionComplete));
                    mLoadingLastUpdateTime = now;
                }
                return mLoadingKeepGoing;
            }
        };

        WaveformViewAdvance mWaveformView = new WaveformViewAdvance(this, null);
        mStartMarker = new MarkerView(this, null);

        ViewGroup.LayoutParams layoutParams;
        float ratioDuration = hashMapDuration.get(mFilename)/(float)maxDuration;
        layoutParams = new ViewGroup.LayoutParams((int) (width*ratioDuration) - WaveformViewAdvance.spaceColum*4, 200);

        mStartMarker.setListener(this);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);
        mStartMarker.setImageResource(R.drawable.marker_left_normal_mix);
        mStartMarker.setElevation(10f);

        mWaveformView.setWidth((int) (width*ratioDuration));
        mWaveformView.setLayoutParams(layoutParams);
        mWaveformView.setY(index*200 + index*40);
        mWaveformView.setX(width/2f);
        waveformViews.add(mWaveformView);

        root.addView(mWaveformView);
        if (index == 0) {
            root.addView(mStartMarker);
            mStartMarker.post(new Runnable() {
                @Override
                public void run() {
                    mStartMarker.setX(width/2f - mStartMarker.getMeasuredWidth());
                    mStartMarker.setMinX(width/2f - mStartMarker.getMeasuredWidth());
                }
            });
        }

        try {
            mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(), listener);

            if (mSoundFile == null) {
                mProgressDialog.dismiss();
                String name = mFile.getName().toLowerCase();
                String[] components = name.split("\\.");
                String err;
                if (components.length < 2) {
                    err = getResources().getString(R.string.no_extension_error);
                } else {
                    err = getResources().getString(R.string.bad_extension_error) + " " + components[components.length - 1];
                }
                final String finalErr = err;
                Runnable runnable = new Runnable() {
                    public void run() {
//                                handleFatalError("UnsupportedExtension", finalErr, new Exception());
                    }
                };
                mHandler.post(runnable);
                return;
            }
        } catch (final Exception e) {
            mProgressDialog.dismiss();
            e.printStackTrace();

            Runnable runnable = new Runnable() {
                public void run() {
//                            handleFatalError("ReadError", getResources().getText(R.string.read_error), e);
                }
            };
            mHandler.post(runnable);
            return;
        }
        mProgressDialog.dismiss();
        if (mLoadingKeepGoing) {
            Runnable runnable = new Runnable() {
                public void run() {
                    finishOpeningSoundFile(mWaveformView);
                }
            };
            mHandler.post(runnable);
        } else {
            MixAudioActivity.this.finish();
        }

        // Load the sound file in a background thread
//        new Thread() {
//            public void run() {
//
//            }
//        }.start();
    }

    private void finishOpeningSoundFile(WaveformViewAdvance mWaveformView) {
        mWaveformView.setSoundFile(mSoundFile);
        viewTime.recomputeHeights(mDensity);
        viewTime.setmSampleRate(mSoundFile.getSampleRate());
        viewTime.setmSamplesPerFrame(mSoundFile.getSamplesPerFrame());
        viewTime.setNumSample(mSoundFile.getNumFrames());
        viewTime.calNoNam();
        viewTime.invalidate();
        mMaxPos = mWaveformView.maxPos();
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        if (mEndPos > mMaxPos) mEndPos = mMaxPos;
        updateDisplay(mWaveformView);
    }

    private synchronized void updateDisplay(WaveformViewAdvance mWaveformView) {
        if (mIsPlaying) {
//            int now = mPlayer.getCurrentPosition() + mPlayStartOffset;
//            int frames = mWaveformView.millisecsToPixels(now);
//            mWaveformView.setPlayback(frames);
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

        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();

    }

    private String getExtensionFromFilename(String filename) {
        return filename.substring(filename.lastIndexOf('.'), filename.length());
    }

    private String getFilenameFromUri(Uri uri) {
        Cursor c = managedQuery(uri, null, "", null, null);
        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();
        int dataIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

        return c.getString(dataIndex);
    }

    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
//        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
    }


    private synchronized void onPlay(WaveformViewAdvance mWaveformView) {
        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mPlayer == null) {
            // Not initialized yet
            return;
        }

        try {
//            mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
//            if (startPosition < mStartPos) {
//                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
//            } else if (startPosition > mEndPos) {
//                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
//            } else {
//                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
//            }

//            mPlayStartOffset = 0;

//            int startFrame = mWaveformView.secondsToFrames(0 * 0.001);
//            int endFrame = mWaveformView.secondsToFrames(maxDuration * 0.001);
//            int startByte = mSoundFile.getSeekableFrameOffset(startFrame);
//            int endByte = mSoundFile.getSeekableFrameOffset(endFrame);
//            if (mCanSeekAccurately && startByte >= 0 && endByte >= 0) {
//                try {
//                    mPlayer.reset();
//                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    FileInputStream subsetInputStream = new FileInputStream(mFile.getAbsolutePath());
//                    mPlayer.setDataSource(subsetInputStream.getFD(), startByte, endByte - startByte);
//                    mPlayer.prepare();
////                    mPlayStartOffset = mPlayStartMsec;
//                } catch (Exception e) {
//                    System.out.println("Exception trying to play file subset");
//                    mPlayer.reset();
//                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    mPlayer.setDataSource(mFile.getAbsolutePath());
//                    mPlayer.prepare();
////                    mPlayStartOffset = 0;
//                }
//            }

            mPlayer.setOnCompletionListener(new OnCompletionListener() {
                public synchronized void onCompletion(MediaPlayer arg0) {
                    handlePause();
                }
            });
            mIsPlaying = true;

            mHandler.removeCallbacks(runnableWaveForm);
            mHandler.postDelayed(runnableWaveForm, 10);
//            if (mPlayStartOffset == 0) {
//                mPlayer.seekTo(mPlayStartMsec);
//            }
            mPlayer.start();
//            enableDisableButtons();
        } catch (Exception e) {
//            showFinalAlert(e, R.string.play_error);
            return;
        }
    }

    @Override
    public void markerTouchStart(MarkerView marker, float pos) {
        test.requestDisallowInterceptTouchEvent(true);
        mTouchDragging = true;
        mTouchStart = pos;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    @Override
    public void markerTouchMove(MarkerView marker, float pos) {
        float delta = pos - mTouchStart;


        if (marker == mStartMarker) {
            Log.d("Thenv", "markerTouchMove: vo day");
            mStartPos = trap((int) (mTouchInitialStartPos + delta));
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
        } else {
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos){
                mEndPos = mStartPos;
            }
        }
//        waveformViews.get(0).setParameters(mStartPos, mEndPos, mEndPos/8);
//        waveformViews.get(0).invalidate();

//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(waveformViews.get(0).getMeasuredWidth()  - (int) (width/2f + mEndPos - waveformViews.get(0).getX()), 200);
//        waveformViews.get(0).setLayoutParams(layoutParams);
//        waveformViews.get(0).setX(width/2f + mEndPos);

//        updateDisplay(waveformViews.get(0));
    }

    private int trap(int pos) {
        if (pos < 0) return 0;
        return pos;
    }

    @Override
    public void markerTouchEnd(MarkerView marker , float pos) {
        ViewGroup.LayoutParams layoutParams;
        Log.d("Thenv", "markerTouchEnd: " + mStartMarker.getWidth());
        layoutParams = new ViewGroup.LayoutParams(792 - mEndPos - 32 - mStartMarker.getWidth(), 200);
        waveformViews.get(0).setLayoutParams(layoutParams);

        waveformViews.get(0).setParameters(mStartPos, mEndPos, mEndPos/8);
        waveformViews.get(0).invalidate();
        waveformViews.get(0).setX(pos);


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
}