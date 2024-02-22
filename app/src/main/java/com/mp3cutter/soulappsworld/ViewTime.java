package com.mp3cutter.soulappsworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wellytech.audiotrim.R;

public class ViewTime extends View {

    // Colors
    private Paint mTimecodePaint;
    private int mZoomLevel = 2;
    private int mNumZoomLevels = 6;
    private int mSampleRate;
    private int mSamplesPerFrame;
    private int mOffset;
    private int mSelectionStart;
    private int mSelectionEnd;
    private float mDensity;
    private WaveformViewAdvance.WaveformListener mListener;
    private boolean mInitialized;

    public static int spaceColum = 8;

    private double noName = 0;
    private int numSample = 0;

    private int originWidth = 0;

    public ViewTime(Context context, AttributeSet attrs) {
        super(context, attrs);

        // We don't want keys, the markers get these
        setFocusable(false);

        mTimecodePaint = new Paint();
        mTimecodePaint.setTextSize(16);
        mTimecodePaint.setAntiAlias(true);
        mTimecodePaint.setColor(
                getResources().getColor(R.drawable.timecode));
        mTimecodePaint.setShadowLayer(
                2, 1, 1,
                getResources().getColor(R.drawable.timecode_shadow));


        mOffset = 0;
        mSelectionStart = 0;
        mSelectionEnd = 0;
        mDensity = 1.0f;
        mInitialized = false;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//	mScaleGestureDetector.onTouchEvent(event);
//	if (mGestureDetector.onTouchEvent(event)) {
//	    return true;
//	}
//
//        switch(event.getAction()) {
//        case MotionEvent.ACTION_DOWN:
//            mListener.waveformTouchStart(event.getX());
//            break;
//        case MotionEvent.ACTION_MOVE:
//            mListener.waveformTouchMove(event.getX());
//            break;
//        case MotionEvent.ACTION_UP:
//            mListener.waveformTouchEnd();
//            break;
//        }
        return true;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public int getZoomLevel() {
        return mZoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        while (mZoomLevel > zoomLevel) {
            zoomIn();
        }
        while (mZoomLevel < zoomLevel) {
            zoomOut();
        }
    }

    public boolean canZoomIn() {
        return (mZoomLevel > 2);
    }

    public void zoomIn() {
        if (canZoomIn()) {
            mZoomLevel--;
            calNoNam();
            invalidate();
        }
    }

    public void calNoNam() {
        float test = (numSample/1080f)*spaceColum/(mZoomLevel - 1);
        noName = ((mNumZoomLevels - mZoomLevel) == 0? 1: (mNumZoomLevels - mZoomLevel))*spaceColum/test;
    }

    public boolean canZoomOut() {
        return (mZoomLevel < mNumZoomLevels - 1);
    }

    public void zoomOut() {
        if (canZoomOut()) {
            mZoomLevel++;
            calNoNam();

            invalidate();
        }
    }

    public int maxPos() {
        return getMeasuredWidth()*(mZoomLevel - 2)/spaceColum;
    }

    public int secondsToFrames(double seconds) {
        return (int)(1.0 * seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }

    public int secondsToPixels(double seconds) {
        double z = 1;
        return (int)(z* seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }

    public double pixelsToSeconds(int pixels) {
        double z = 32/(double)96;
        return (pixels * (double)mSamplesPerFrame / (mSampleRate * noName));
    }

    public int millisecsToPixels(int msecs) {
        double z = noName;
        return (int)((msecs * 1.0 * mSampleRate * z) /
                (1000.0 * mSamplesPerFrame) + 0.5);
    }

    public int pixelsToMillisecs(int pixels) {
        double z = noName;
        return (int)(pixels * (1000.0 * mSamplesPerFrame) /
                (mSampleRate * z) + 0.5);
    }

    public void setParameters(int start, int end, int offset) {
        mSelectionStart = start;
        mSelectionEnd = end;
        mOffset = offset;
    }

    public int getStart() {
        return mSelectionStart;
    }

    public int getEnd() {
        return mSelectionEnd;
    }

    public int getOffset() {
        return mOffset;
    }

    public void recomputeHeights(float density) {
        mDensity = density;
        mTimecodePaint.setTextSize((int)(12 * density));

        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw waveform
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int start = mOffset;
        int width = measuredWidth;
        int ctr = measuredHeight / 2;

        // Draw grid
        double onePixelInSecs = pixelsToSeconds(1);
        boolean onlyEveryFiveSecs = (onePixelInSecs > 1.0 / 50.0);
        double fractionalSecs = mOffset * onePixelInSecs;
        int integerSecs = (int) fractionalSecs;
        int i = 0;

        // Draw timecode
        double timecodeIntervalSecs = 1.0;

//        if (timecodeIntervalSecs / onePixelInSecs < 50) {
//            timecodeIntervalSecs = 5.0;
//        }
//        if (timecodeIntervalSecs / onePixelInSecs < 50) {
//            timecodeIntervalSecs = 15.0;
//        }

        // Draw grid
        fractionalSecs = mOffset * onePixelInSecs;
        int integerTimecode = (int) (fractionalSecs / timecodeIntervalSecs);
        while (i < width) {
            i+=spaceColum;
            fractionalSecs += onePixelInSecs;
            integerSecs = (int) fractionalSecs*spaceColum*((mNumZoomLevels - mZoomLevel) == 0? 1: (mNumZoomLevels - mZoomLevel));
            int integerTimecodeNew = (int) (fractionalSecs /
                    timecodeIntervalSecs);
            if (integerTimecodeNew != integerTimecode) {
                integerTimecode = integerTimecodeNew;

                // Turn, e.g. 67 seconds into "1:07"
                String timecodeMinutes = "" + (integerSecs / 60);
                String timecodeSeconds = "" + (integerSecs % 60);
                if ((integerSecs % 60) < 10) {
                    timecodeSeconds = "0" + timecodeSeconds;
                }
                String timecodeStr = timecodeMinutes + ":" + timecodeSeconds;
                float offset = (float) (
                        0.5 * mTimecodePaint.measureText(timecodeStr));

                canvas.drawText(timecodeStr,
                        (i - offset),
                        (int)(12 * mDensity),
                        mTimecodePaint);
            }

        }


        if (mListener != null) {
            mListener.waveformDraw();
        }
    }

    public int getZoomLevelRemain() {
        return mNumZoomLevels - mZoomLevel;
    }


    public int getmSampleRate() {
        return mSampleRate;
    }

    public void setmSampleRate(int mSampleRate) {
        this.mSampleRate = mSampleRate;
    }

    public int getmSamplesPerFrame() {
        return mSamplesPerFrame;
    }

    public void setmSamplesPerFrame(int mSamplesPerFrame) {
        this.mSamplesPerFrame = mSamplesPerFrame;
    }

    public int getNumSample() {
        return numSample;
    }

    public void setNumSample(int numSample) {
        this.numSample = numSample;
    }

    public int getOriginWidth() {
        return originWidth;
    }

    public void setOriginWidth(int originWidth) {
        this.originWidth = originWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
}