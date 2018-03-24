/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.doctoror.fuckoffmusicplayer.util.ThemeUtils;

/**
 * {@link PaddingInsetsFrameLayout} that will draw window background on padded area.
 */
public class WindowBackgroundPaddingInsetsFrameLayout extends PaddingInsetsFrameLayout {

    private final Rect mTempRect = new Rect();

    private Drawable mWindowBackground;

    public WindowBackgroundPaddingInsetsFrameLayout(
            @NonNull final Context context) {
        super(context);
        init(context);
    }

    public WindowBackgroundPaddingInsetsFrameLayout(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WindowBackgroundPaddingInsetsFrameLayout(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull final Context context) {
        mWindowBackground = ThemeUtils
                .getDrawable(context.getTheme(), android.R.attr.windowBackground);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        super.onDraw(canvas);
        final int width = getWidth();
        final int height = getHeight();
        if (mWindowBackground != null) {
            int sc = canvas.save();
            canvas.translate(getScrollX(), getScrollY());

            // Top
            mTempRect.set(0, 0, width, getPaddingTop());
            mWindowBackground.setBounds(mTempRect);
            mWindowBackground.draw(canvas);

            // Bottom
            mTempRect.set(0, height - getPaddingBottom(), width, height);
            mWindowBackground.setBounds(mTempRect);
            mWindowBackground.draw(canvas);

            // Left
            mTempRect.set(0, getPaddingTop(), getPaddingLeft(), height - getPaddingBottom());
            mWindowBackground.setBounds(mTempRect);
            mWindowBackground.draw(canvas);

            // Right
            mTempRect.set(
                    width - getPaddingRight(),
                    getPaddingTop(),
                    width,
                    height - getPaddingBottom());

            mWindowBackground.setBounds(mTempRect);
            mWindowBackground.draw(canvas);

            canvas.restoreToCount(sc);
        }
    }
}
