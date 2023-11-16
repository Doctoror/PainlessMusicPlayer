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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;

import com.doctoror.fuckoffmusicplayer.R;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A {@link Transition} that slides upper view to top and bottom view to bottom
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class VerticalGateTransition extends Transition {

    private static final String DUMMY_PROPERTY_NAME = "d";

    @IdRes
    private int mUpperViewId = R.id.appBar;

    @IdRes
    private int mBottomViewId = R.id.recyclerView;

    protected void setUpperViewId(@IdRes final int upperViewId) {
        mUpperViewId = upperViewId;
    }

    protected void setBottomViewId(@IdRes final int bottomViewId) {
        mBottomViewId = bottomViewId;
    }

    @Override
    public void captureStartValues(final TransitionValues transitionValues) {
        // Dummy property must be changed or else createAnimator() won't be called
        transitionValues.values.put(DUMMY_PROPERTY_NAME, "start");
    }

    @Override
    public void captureEndValues(final TransitionValues transitionValues) {
        // Dummy property must be changed or else createAnimator() won't be called
        transitionValues.values.put(DUMMY_PROPERTY_NAME, "end");
    }

    @Override
    public Animator createAnimator(final ViewGroup sceneRoot, final TransitionValues startValues,
            final TransitionValues endValues) {
        final Collection<Animator> animators = new ArrayList<>(2);
        final View upperView = sceneRoot.findViewById(mUpperViewId);
        if (upperView != null) {
            animators.add(ObjectAnimator.ofFloat(upperView,
                    ViewProperties.TRANSLATION_Y, 0, -upperView.getHeight()));
        }

        final View bottomView = sceneRoot.findViewById(mBottomViewId);
        if (bottomView != null) {
            final View bottomViewParent = (View) bottomView.getParent();
            if (bottomView.getHeight() <= bottomViewParent.getHeight()) {
                animators.add(ObjectAnimator.ofFloat(bottomView, ViewProperties.TRANSLATION_Y,
                        0, bottomViewParent.getHeight() - bottomView.getTop()));
            } else {
                animators.add(ObjectAnimator.ofFloat(bottomView, ViewProperties.TRANSLATION_Y,
                        0, bottomView.getHeight()));
            }
        }

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        return animatorSet;
    }

}
