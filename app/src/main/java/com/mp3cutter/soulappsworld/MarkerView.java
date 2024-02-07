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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Represents a draggable start or end marker.
 *
 * Most events are passed back to the client class using a
 * listener interface.
 *
 * This class directly keeps track of its own velocity, though,
 * accelerating as the user holds down the left or right arrows
 * while this control is focused.
 */
public class MarkerView extends androidx.appcompat.widget.AppCompatImageView {

    private float prevX = 0f;

    private float maxX = 10000;
    private float minX;

    private GestureDetector mGestureDetector;


    public interface MarkerListener {
        public void markerTouchStart(MarkerView marker, float pos);
        public void markerTouchMove(MarkerView marker, float pos);
        public void markerTouchEnd(MarkerView marker, float pos);
        public void markerFocus(MarkerView marker);
        public void markerLeft(MarkerView marker, int velocity);
        public void markerRight(MarkerView marker, int velocity);
        public void markerEnter(MarkerView marker);
        public void markerKeyUp();
        public void markerDraw();
    };

    private int mVelocity;
    private MarkerListener mListener;
    private float mTouchStart;
    private int mTouchLast;

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Make sure we get keys
        setFocusable(true);

        mVelocity = 0;
        mListener = null;

        mGestureDetector = new GestureDetector(
                context,
                new GestureDetector.SimpleOnGestureListener() {
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        return true;
                    }
                });
    }

    public void setListener(MarkerListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
//        if (getX() > maxX) {
//            setX(maxX);
//            return true;
//        }
//        if (getX() < minX) {
//            setX(minX);
//            return false;
//        }
        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            prevX = event.getRawX();
            mListener.markerTouchStart(this, event.getRawX());
            break;
        case MotionEvent.ACTION_MOVE:
            mListener.markerTouchMove(this, event.getRawX());
            float moveX = event.getRawX() - prevX;
            setX(getX() + moveX);
            prevX = event.getRawX();
            float width = getContext().getResources().getDisplayMetrics().widthPixels;
//            if (getX() + getWidth() >= width || getX() <= 0
//            ) {
//                Log.d("Thenv", "onTouchEvent: vod ay");
//                setX(getX() - moveX);
//            }
            break;
        case MotionEvent.ACTION_UP:
            mListener.markerTouchEnd(this, getX() + getWidth());
            break;
        }
        return true;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
                                  Rect previouslyFocusedRect) {
        if (gainFocus && mListener != null)
            mListener.markerFocus(this);
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mListener != null)
            mListener.markerDraw();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mVelocity++;
        int v = (int)Math.sqrt(1 + mVelocity / 2);
        if (mListener != null) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                mListener.markerLeft(this, v);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mListener.markerRight(this, v);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                mListener.markerEnter(this);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mVelocity = 0;
        if (mListener != null)
            mListener.markerKeyUp();
        return super.onKeyDown(keyCode, event);
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }
}
