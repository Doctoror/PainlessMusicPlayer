/*
 * Copyright (C) 2016 Alex Gomes
 * Copyright (C) 2017 Yaroslav Mytkalyk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE X CONSORTIUM BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.alex.siriwaveview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Alex on 6/25/2016.
 * Modified by Yaroslav Mytkalyk 1/14/2017
 */
public class SiriWaveView extends View {

    private Path mPath;
    private Paint mPaint;

    private float frequency = 1.5f;
    private float IdleAmplitude = 0.00f;
    private int waveNumber = 2;
    private float phaseShift = 0.15f;
    private float initialPhaseOffset = 0.0f;
    private float waveHeight;
    private float waveVerticalPosition = 2;
    private int waveColor;
    private float phase;
    private float amplitude;
    private float level = 1.0f;

    ObjectAnimator mAmplitudeAnimator;

    public SiriWaveView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context, null);
        }
    }

    public SiriWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    public SiriWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context
                .obtainStyledAttributes(attrs, com.alex.siriwaveview.R.styleable.SiriWaveView);
        frequency = a
                .getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_waveFrequency, frequency);
        IdleAmplitude = a.getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_waveIdleAmplitude,
                IdleAmplitude);
        phaseShift = a.getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_wavePhaseShift,
                phaseShift);
        initialPhaseOffset = a
                .getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_waveInitialPhaseOffset,
                        initialPhaseOffset);
        waveHeight = a.getDimension(com.alex.siriwaveview.R.styleable.SiriWaveView_waveHeight,
                waveHeight);
        waveColor = a.getColor(com.alex.siriwaveview.R.styleable.SiriWaveView_waveColor, waveColor);
        waveVerticalPosition = a
                .getFloat(com.alex.siriwaveview.R.styleable.SiriWaveView_waveVerticalPosition,
                        waveVerticalPosition);
        waveNumber = a.getInteger(R.styleable.SiriWaveView_waveAmount, waveNumber);

        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(waveColor);

        a.recycle();
        initAnimation();
    }

    private void initAnimation() {
        if (mAmplitudeAnimator == null) {
            mAmplitudeAnimator = ObjectAnimator.ofFloat(this, "amplitude", 1f);
            mAmplitudeAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        }
        if (!mAmplitudeAnimator.isRunning()) {
            mAmplitudeAnimator.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
        updatePath();
    }

    private void updatePath() {
        mPath.reset();

        phase += phaseShift;
        amplitude = Math.max(level, IdleAmplitude);

        for (int i = 0; i < waveNumber; i++) {
            float halfHeight = getHeight() / waveVerticalPosition;
            float width = getWidth();
            float mid = width / 2.0f;

            float maxAmplitude = halfHeight - (halfHeight - waveHeight);

            // Progress is a value between 1.0 and -0.5, determined by the current wave idx, which is used to alter the wave's amplitude.
            float progress = 1.0f - (float) i / waveNumber;
            float normedAmplitude = (1.5f * progress - 0.5f) * amplitude;

            float multiplier = (float) Math.min(1.0, (progress / 3.0f * 2.0f) + (1.0f / 3.0f));

            for (int x = 0; x < width; x++) {
                float scaling = (float) (-Math.pow(1 / mid * (x - mid), 2) + 1);

                float y = (float) (scaling * maxAmplitude * normedAmplitude * Math
                        .sin(2 * Math.PI * (x / width) * frequency + phase + initialPhaseOffset)
                        + halfHeight);

                if (x == 0) {
                    mPath.moveTo(x, y);
                } else {
                    mPath.lineTo(x, y);
                }
            }
        }

        //mPath.close();
    }

    @Keep
    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
        invalidate();
    }

    @Keep
    public float getAmplitude() {
        return this.amplitude;
    }

    public void stopAnimation() {
        if (mAmplitudeAnimator != null) {
            mAmplitudeAnimator.removeAllListeners();
            mAmplitudeAnimator.end();
            mAmplitudeAnimator.cancel();
        }
    }

    public void startAnimation() {
        if (mAmplitudeAnimator != null) {
            mAmplitudeAnimator.start();
        }
    }

    public void setWaveColor(int waveColor) {
        mPaint.setColor(waveColor);
        invalidate();
    }

    public void setStrokeWidth(float strokeWidth) {
        mPaint.setStrokeWidth(strokeWidth);
        invalidate();
    }

    public void setWaveHeight(final float waveHeight) {
        this.waveHeight = waveHeight;
    }
}