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
package com.doctoror.fuckoffmusicplayer.presentation.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewParent;

/**
 * Helpers for animating sliding View from bottom
 */
public final class SlideFromBottomHelper {

    private SlideFromBottomHelper() {
        throw new UnsupportedOperationException();
    }

    public static float getStartTranslation(@NonNull final View target) {
        final ViewParent parent = target.getParent();
        if (parent instanceof View) {
            if (target.getHeight() <= ((View) parent).getHeight()) {
                return ((View) parent).getHeight() - target.getTop();
            }
        }
        return target.getHeight();
    }

    @NonNull
    public static Animator createAnimator(@NonNull final View target) {
        return ObjectAnimator
                .ofFloat(target, ViewProperties.TRANSLATION_Y, getStartTranslation(target), 0);
    }
}
