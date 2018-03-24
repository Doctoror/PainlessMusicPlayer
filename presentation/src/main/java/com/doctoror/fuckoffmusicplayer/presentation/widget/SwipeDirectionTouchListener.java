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
package com.doctoror.fuckoffmusicplayer.presentation.widget;

import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;

public abstract class SwipeDirectionTouchListener implements View.OnTouchListener {

    private static final int SWIPE_THRESHOLD = (int)
            (24 * Resources.getSystem().getDisplayMetrics().density);

    private boolean mSwiping;
    private boolean mNotified;
    private int mSwipeStartPosition;

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mSwiping = true;
                mSwipeStartPosition = (int) event.getY();
                mNotified = false;
                break;

            case MotionEvent.ACTION_UP:
                mSwiping = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mSwiping && !mNotified) {
                    if (mSwipeStartPosition - event.getY() > SWIPE_THRESHOLD) {
                        mNotified = true;
                        onSwipedDown();
                    }
                }
                break;
        }
        return false;
    }

    protected abstract void onSwipedDown();
}
