/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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
import android.util.AttributeSet;

import com.alex.siriwaveview.SiriWaveView;

/**
 * Auto start {@link SiriWaveView}
 */
public class AutoStartSiriWaveView extends SiriWaveView {

    public AutoStartSiriWaveView(final Context context) {
        super(context);
    }

    public AutoStartSiriWaveView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoStartSiriWaveView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setWaveHeight(h / 2);
    }
}
