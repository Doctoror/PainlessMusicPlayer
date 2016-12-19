package com.doctoror.fuckoffmusicplayer.transition;

import com.doctoror.fuckoffmusicplayer.R;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * {@link Transition} for sliding View from bottom
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class SlideFromBottomTransition extends Transition {

    private static final String TRANSLATION_Y = "translationY";
    private static final String DUMMY_PROPERTY_NAME = "d";

    @IdRes
    private int mTargetViewId;

    public SlideFromBottomTransition() {
        this(R.id.recyclerView);
    }

    public SlideFromBottomTransition(@IdRes final int targetViewId) {
        mTargetViewId = targetViewId;
    }

    public void setTargetViewId(@IdRes final int targetViewId) {
        mTargetViewId = targetViewId;
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
        final View targetView = sceneRoot.findViewById(mTargetViewId);
        if (targetView != null) {
            return createAnimator(targetView);
        }
        return null;
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
        return ObjectAnimator.ofFloat(target, TRANSLATION_Y, getStartTranslation(target), 0);
    }
}
