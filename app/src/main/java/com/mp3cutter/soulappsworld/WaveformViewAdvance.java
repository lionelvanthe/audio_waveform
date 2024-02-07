/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mp3cutter.soulappsworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.mp3cutter.soulappsworld.soundfile.CheapSoundFile;
import com.wellytech.audiotrim.R;

/**
 * WaveformView is an Android view that displays a visual representation
 * of an audio waveform.  It retrieves the frame gains from a CheapSoundFile
 * object and recomputes the shape contour at several zoom levels.
 *
 * This class doesn't handle selection or any of the touch interactions
 * directly, so it exposes a listener interface.  The class that embeds
 * this view should add itself as a listener and make the view scroll
 * and respond to other events appropriately.
 *
 * WaveformView doesn't actually handle selection, but it will just display
 * the selected part of the waveform in a different color.
 */
public class WaveformViewAdvance extends View {
    public interface WaveformListener {
        public void waveformTouchStart(float x);
        public void waveformTouchMove(float x);
        public void waveformTouchEnd();
        public void waveformFling(float x);
        public void waveformDraw();
        public void waveformZoomIn();
        public void waveformZoomOut();
    };

    // Colors
    private Paint mGridPaint;
    private Paint mSelectedLinePaint;
    private Paint mUnselectedLinePaint;
    private Paint mUnselectedBkgndLinePaint;
    private Paint mBorderLinePaint;
    private Paint mPlaybackLinePaint;
    private Paint mTimecodePaint;
    private Paint mTestPaint;

    private CheapSoundFile mSoundFile;
    private int[] mLenByZoomLevel;
    private double[][] mValuesByZoomLevel;
    private double[] mZoomFactorByZoomLevel;
    private int[] mHeightsAtThisZoomLevel;
    private int mZoomLevel;
    private int mNumZoomLevels;
    private int mSampleRate;
    private int mSamplesPerFrame;
    private int mOffset;
    private int mSelectionStart;
    private int mSelectionEnd;
    private int mPlaybackPos;
    private float mDensity;
    private float mInitialScaleSpan;
    private WaveformListener mListener;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private boolean mInitialized;

    public static int spaceColum = 8;
    private int widthColum = 5;
    private int testWidth = 1080;

    public WaveformViewAdvance(Context context, AttributeSet attrs) {
        super(context, attrs);

        // We don't want keys, the markers get these
        setFocusable(false);

        mGridPaint = new Paint();
        mGridPaint.setAntiAlias(false);
        mGridPaint.setColor(
            getResources().getColor(R.drawable.grid_line));
        mSelectedLinePaint = new Paint();
        mSelectedLinePaint.setAntiAlias(false);
        mSelectedLinePaint.setColor(
            getResources().getColor(R.drawable.waveform_selected));
        mSelectedLinePaint.setStrokeWidth(widthColum);

        mUnselectedLinePaint = new Paint();
        mUnselectedLinePaint.setAntiAlias(false);
        mUnselectedLinePaint.setColor(
            getResources().getColor(R.drawable.waveform_unselected));
        mUnselectedLinePaint.setStrokeWidth(widthColum);


        mUnselectedBkgndLinePaint = new Paint();
        mUnselectedBkgndLinePaint.setAntiAlias(false);
        mUnselectedBkgndLinePaint.setColor(
            getResources().getColor(
                R.drawable.waveform_unselected_bkgnd_overlay));
        mBorderLinePaint = new Paint();
        mBorderLinePaint.setAntiAlias(true);
        mBorderLinePaint.setStrokeWidth(1.5f);
        mBorderLinePaint.setPathEffect(
            new DashPathEffect(new float[] { 3.0f, 2.0f }, 0.0f));
        mBorderLinePaint.setColor(
            getResources().getColor(R.drawable.selection_border));
        mPlaybackLinePaint = new Paint();
        mPlaybackLinePaint.setAntiAlias(false);
        mPlaybackLinePaint.setColor(
            getResources().getColor(R.drawable.playback_indicator));
        mTimecodePaint = new Paint();
        mTimecodePaint.setTextSize(12);
        mTimecodePaint.setAntiAlias(true);
        mTimecodePaint.setColor(
            getResources().getColor(R.drawable.timecode));
        mTimecodePaint.setShadowLayer(
            2, 1, 1,
            getResources().getColor(R.drawable.timecode_shadow));

        mTestPaint = new Paint();
        mTestPaint.setAntiAlias(false);
        mTestPaint.setColor(
                getResources().getColor(R.drawable.waveform_selected));
        mTestPaint.setStrokeWidth(widthColum+spaceColum);

	mGestureDetector = new GestureDetector(
	        context,
		new GestureDetector.SimpleOnGestureListener() {
		    public boolean onFling(
			        MotionEvent e1, MotionEvent e2, float vx, float vy) {
//			mListener.waveformFling(vx);
			return true;
		    }
		});

	mScaleGestureDetector = new ScaleGestureDetector(
	        context,
		new ScaleGestureDetector.SimpleOnScaleGestureListener() {
		    public boolean onScaleBegin(ScaleGestureDetector d) {
                        Log.i("Ringdroid", "ScaleBegin " + d.getCurrentSpanX());
                        mInitialScaleSpan = Math.abs(d.getCurrentSpanX());
			return true;
		    }
		    public boolean onScale(ScaleGestureDetector d) {
                        float scale = Math.abs(d.getCurrentSpanX());
                        Log.i("Ringdroid", "Scale " + (scale - mInitialScaleSpan));
                        if (scale - mInitialScaleSpan > 40) {
                            mListener.waveformZoomIn();
                            mInitialScaleSpan = scale;
                        }
                        if (scale - mInitialScaleSpan < -40) {
                            mListener.waveformZoomOut();
                            mInitialScaleSpan = scale;
                        }
			return true;
		    }
		    public void onScaleEnd(ScaleGestureDetector d) {
                        Log.i("Ringdroid", "ScaleEnd " + d.getCurrentSpanX());
		    }
		});

        mSoundFile = null;
        mLenByZoomLevel = null;
        mValuesByZoomLevel = null;
        mHeightsAtThisZoomLevel = null;
        mOffset = 0;
        mPlaybackPos = -1;
        mSelectionStart = 0;
        mSelectionEnd = 0;
        mDensity = 1.0f;
        mInitialized = false;

        rounedCorner();
    }

    private void rounedCornerWithStroke() {

        // Tạo drawable cho viền
        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setShape(GradientDrawable.RECTANGLE);
        borderDrawable.setCornerRadius(dpToPx(15));
        borderDrawable.setStroke(dpToPx(2), 0xFF195a72); // Kích thước và màu sắc của viền

        // Tạo drawable cho nền (có thể thay đổi màu sắc nếu cần)
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setCornerRadius(dpToPx(15));
        backgroundDrawable.setColor(0xFF00000); // Màu nền

        // Gộp cả hai drawable để tạo viền cho view
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{backgroundDrawable, borderDrawable});

        // Áp dụng drawable cho view
        setBackground(layerDrawable);
//        setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16)); // Thêm padding nếu cần
        // Nếu bạn đang sử dụng ViewGroup như LinearLayout, thêm nội dung của bạn vào đây
        // addView(childView);
    }


    private void rounedCorner() {


        // Tạo drawable cho nền (có thể thay đổi màu sắc nếu cần)
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setCornerRadius(dpToPx(15));
        backgroundDrawable.setColor(0xFF00000); // Màu nền

        // Áp dụng drawable cho view
        setBackground(backgroundDrawable);
//        setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16)); // Thêm padding nếu cần
        // Nếu bạn đang sử dụng ViewGroup như LinearLayout, thêm nội dung của bạn vào đây
        // addView(childView);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//	mScaleGestureDetector.onTouchEvent(event);
//	if (mGestureDetector.onTouchEvent(event)) {
//	    return true;
//	}
        Log.d("Thenv", "onTouchEvent: " + event.getAction());
        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
//            mListener.waveformTouchStart(event.getX());
            rounedCorner();
            break;
        case MotionEvent.ACTION_MOVE:
//            mListener.waveformTouchMove(event.getX());
            break;
        case MotionEvent.ACTION_UP:
            Log.d("Thenv", "onTouchEvent: vo day");
            rounedCornerWithStroke();
//            mListener.waveformTouchEnd();
            break;
        }
        return true;
    }

    public boolean hasSoundFile() {
        return mSoundFile != null;
    }

    public void setSoundFile(CheapSoundFile soundFile) {
        mSoundFile = soundFile;
        mSampleRate = mSoundFile.getSampleRate();
        mSamplesPerFrame = mSoundFile.getSamplesPerFrame();
        computeDoublesForAllZoomLevels();
        mHeightsAtThisZoomLevel = null;
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
//            mSelectionStart *= 2;
//            mSelectionEnd *= 2;
//            int offsetCenter = mOffset + getMeasuredWidth() / 2;
//            offsetCenter *= 2;
//            mOffset = offsetCenter - getMeasuredWidth() / 2;
//            if (mOffset < 0) {
//                mOffset = 0;
//            }
            mHeightsAtThisZoomLevel = null;
            invalidate();
        }
    }

    public boolean canZoomOut() {
        return (mZoomLevel < mNumZoomLevels - 1);
    }

    public void zoomOut() {
        if (canZoomOut()) {
            mZoomLevel++;
//            mSelectionStart /= 2;
//            mSelectionEnd /= 2;
//            int offsetCenter = mOffset + getMeasuredWidth() / 2;
//            offsetCenter /= 2;
//            mOffset = offsetCenter - getMeasuredWidth() / 2;
//            if (mOffset < 0)
//                mOffset = 0;
            mHeightsAtThisZoomLevel = null;
            invalidate();
        }
    }

    public int maxPos() {
        return testWidth*(mZoomLevel - 2)/spaceColum;
    }

    public int secondsToFrames(double seconds) {
        return (int)(1.0 * seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }

    public int secondsToPixels(double seconds) {
        double z = mZoomFactorByZoomLevel[1];
        return (int)(z* seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }

    public double pixelsToSeconds(int pixels) {
        double z = mZoomFactorByZoomLevel[1];
        return (pixels * (double)mSamplesPerFrame / (mSampleRate * z));
    }

    public int millisecsToPixels(int msecs) {
        double z = mZoomFactorByZoomLevel[1];
        return (int)((msecs * 1.0 * mSampleRate * z) /
                     (1000.0 * mSamplesPerFrame) + 0.5);
    }

    public int pixelsToMillisecs(int pixels) {
        double z = mZoomFactorByZoomLevel[1];
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

    public void setPlayback(int pos) {
        mPlaybackPos = pos;
    }

    public void setListener(WaveformListener listener) {
        mListener = listener;
    }

    public void recomputeHeights(float density) {
        mHeightsAtThisZoomLevel = null;
        mDensity = density;
        mTimecodePaint.setTextSize((int)(12 * density));

        invalidate();
    }

    protected void drawWaveformLine(Canvas canvas,
                                    int x, int y0, int y1,
                                    Paint paint) {
        canvas.drawLine(x, y0, x, y1, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSoundFile == null)
            return;

        if (mHeightsAtThisZoomLevel == null)
            computeIntsForThisZoomLevel();

        // Draw waveform
        int measuredWidth = testWidth;
        int measuredHeight = getMeasuredHeight();
        int start = mOffset;
        int width = mHeightsAtThisZoomLevel.length - start;
        int ctr = measuredHeight / 2;

        if (width > measuredWidth)
            width = measuredWidth;

        int i = 0;
        // Draw waveform
        for (i = 0; i < width; i++) {
            Paint paint = mUnselectedLinePaint;
//            if (i*spaceColum + start*spaceColum >= mSelectionStart &&
//                    i*spaceColum + start*spaceColum < mSelectionEnd) {
//                paint = mSelectedLinePaint;
////                drawWaveformLine(canvas, i*spaceColum, 0, measuredHeight,
////                        mTestPaint);
//            } else {
//                drawWaveformLine(canvas, i*spaceColum, 0, measuredHeight,
//                        mUnselectedBkgndLinePaint);
//                paint = mUnselectedLinePaint;
//            }
            if (start + i < mHeightsAtThisZoomLevel.length) {
                drawWaveformLine(
                        canvas, i*spaceColum,
                        ctr - mHeightsAtThisZoomLevel[start + i],
                        ctr + 1 + mHeightsAtThisZoomLevel[start + i],
                        paint);
            }
        }

        // If we can see the right edge of the waveform, draw the
        // non-waveform area to the right as unselected
//        for (i = width; i < measuredWidth; i++) {
//            drawWaveformLine(canvas, i*spaceColum, 0, measuredHeight,
//                             mUnselectedBkgndLinePaint);
//        }

        if (mListener != null) {
            mListener.waveformDraw();
        }
    }

    /**
     * Called once when a new sound file is added
     */
    private void computeDoublesForAllZoomLevels() {
        int numFrames = mSoundFile.getNumFrames();
        int[] frameGains = mSoundFile.getFrameGains();
        double[] smoothedGains = new double[numFrames];
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0];
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0];
            smoothedGains[1] = frameGains[1];
        } else if (numFrames > 2) {
            smoothedGains[0] = (double)(
                (frameGains[0] / 2.0) +
                (frameGains[1] / 2.0));
            for (int i = 1; i < numFrames - 1; i++) {
                smoothedGains[i] = (double)(
                    (frameGains[i - 1] / 3.0) +
                    (frameGains[i    ] / 3.0) +
                    (frameGains[i + 1] / 3.0));
            }
            smoothedGains[numFrames - 1] = (double)(
                (frameGains[numFrames - 2] / 2.0) +
                (frameGains[numFrames - 1] / 2.0));
        }

        // Make sure the range is no more than 0 - 255
        double maxGain = 1.0;
        for (int i = 0; i < numFrames; i++) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i];
            }
        }
        double scaleFactor = 1.0;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0;
        int gainHist[] = new int[256];
        for (int i = 0; i < numFrames; i++) {
            int smoothedGain = (int)(smoothedGains[i] * scaleFactor);
            if (smoothedGain < 0)
                smoothedGain = 0;
            if (smoothedGain > 255)
                smoothedGain = 255;

            if (smoothedGain > maxGain)
                maxGain = smoothedGain;

            gainHist[smoothedGain]++;
        }

        // Re-calibrate the min to be 5%
        double minGain = 0;
        int sum = 0;
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[(int)minGain];
            minGain++;
        }

        // Re-calibrate the max to be 99%
        sum = 0;
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[(int)maxGain];
            maxGain--;
        }

        // Compute the heights
        double[] heights = new double[numFrames];
        double range = maxGain - minGain;
        for (int i = 0; i < numFrames; i++) {
            double value = (smoothedGains[i] * scaleFactor - minGain) / range;
            if (value < 0.0)
                value = 0.0;
            if (value > 1.0)
                value = 1.0;
            heights[i] = value * value;
        }

        mNumZoomLevels = 6;
        mLenByZoomLevel = new int[6];
        mZoomFactorByZoomLevel = new double[6];
        mValuesByZoomLevel = new double[6][];

        // Level 0 is doubled, with interpolated values
        mLenByZoomLevel[0] = numFrames * 2;
        mZoomFactorByZoomLevel[0] = 2.0;
        mValuesByZoomLevel[0] = new double[mLenByZoomLevel[0]];
        if (numFrames > 0) {
            mValuesByZoomLevel[0][0] = 0.5 * heights[0];
            mValuesByZoomLevel[0][1] = heights[0];
        }
        for (int i = 1; i < numFrames; i++) {
            mValuesByZoomLevel[0][2 * i] = 0.5 * (heights[i - 1] + heights[i]);
            mValuesByZoomLevel[0][2 * i + 1] = heights[i];
        }

        // Level 1 is normal
        mLenByZoomLevel[1] = numFrames;
        mValuesByZoomLevel[1] = new double[mLenByZoomLevel[1]];
        for (int i = 0; i < mLenByZoomLevel[1]; i++) {
            mValuesByZoomLevel[1][i] = heights[i];
        }

        // 3 more levels are each halved
//        for (int j = 2; j < 6; j++) {
//            mLenByZoomLevel[j] = mLenByZoomLevel[j - 1] / 2;
//            mValuesByZoomLevel[j] = new double[mLenByZoomLevel[j]];
//            mZoomFactorByZoomLevel[j] = mZoomFactorByZoomLevel[j - 1] / 2.0;
//            for (int i = 0; i < mLenByZoomLevel[j]; i++) {
//                mValuesByZoomLevel[j][i] =
//                    0.5 * (mValuesByZoomLevel[j - 1][2 * i] +
//                           mValuesByZoomLevel[j - 1][2 * i + 1]);
//            }
//        }

//        if (numFrames > 5000) {
//            mZoomLevel = 3;
//        } else if (numFrames > 1000) {
//            mZoomLevel = 2;
//        } else if (numFrames > 300) {
//            mZoomLevel = 1;
//        } else {
//            mZoomLevel = 0;
//        }

        mZoomLevel = 2;
        int test = (mLenByZoomLevel[1]/testWidth +1)*spaceColum/(mZoomLevel - 1);
        mZoomFactorByZoomLevel[1] = (mNumZoomLevels - mZoomLevel) *spaceColum/(float)test;
        mInitialized = true;
    }

    public int getZoomLevelRemain() {
        return mNumZoomLevels - mZoomLevel;
    }

    public void setWidth(int width) {
        testWidth = width;
    }

    /**
     * Called the first time we need to draw when the zoom level has changed
     * or the screen is resized
     */
    private void computeIntsForThisZoomLevel() {
        int halfHeight = (getMeasuredHeight() / 4) - 1;
        int test = (mLenByZoomLevel[1]/testWidth +1)*spaceColum/(mZoomLevel - 1);
        mZoomFactorByZoomLevel[1] = ((mNumZoomLevels - mZoomLevel) == 0? 1: (mNumZoomLevels - mZoomLevel))*spaceColum/(float)test;
        mHeightsAtThisZoomLevel = new int[testWidth*(mZoomLevel - 1)];
        int stepValue = (int) (test/2);
        for (int i = 0; i <mLenByZoomLevel[1] - stepValue; i+=test) {
//            float b = (float) (0.5 * (mValuesByZoomLevel[1][i] + mValuesByZoomLevel[1][i + test]));
            mHeightsAtThisZoomLevel[(int) (i/test)] = (int) (mValuesByZoomLevel[1][i+ stepValue] * halfHeight);
//            if (i%test == 0) {
//                mHeightsAtThisZoomLevel[i/test] = (int)((a/test) *  halfHeight);
//                a = 0;
//            } else {
//                a += (mValuesByZoomLevel[mZoomLevel][i]);
//            }
        }
    }

    public int getTestWidth() {
        return testWidth;
    }

    public void setmOffset(int mOffset) {
        this.mOffset = mOffset;
    }
}
