package com.doctoror.fuckoffmusicplayer.transition;

import com.doctoror.fuckoffmusicplayer.R;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.IdRes;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A {@link Transition} that slides upper view to top and bottom view to bottom
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class VerticalGateTransition extends Transition {

    private static final String TRANSLATION_Y = "translationY";
    private static final String DUMMY_PROPERTY_NAME = "d";

    @IdRes
    private int mUpperViewId = R.id.appBar;

    @IdRes
    private int mBottomViewId = R.id.recyclerView;

    public VerticalGateTransition() {
    }

    public void setUpperViewId(@IdRes final int upperViewId) {
        mUpperViewId = upperViewId;
    }

    public void setBottomViewId(@IdRes final int bottomViewId) {
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
        final View appBar = sceneRoot.findViewById(mUpperViewId);
        if (appBar != null) {
            animators.add(ObjectAnimator.ofFloat(appBar, TRANSLATION_Y, 0, -appBar.getHeight()));
        }

        final View recyclerView = sceneRoot.findViewById(mBottomViewId);
        if (recyclerView != null) {
            animators.add(ObjectAnimator.ofFloat(recyclerView, TRANSLATION_Y,
                    0, recyclerView.getHeight()));
        }

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        return animatorSet;
    }

}
