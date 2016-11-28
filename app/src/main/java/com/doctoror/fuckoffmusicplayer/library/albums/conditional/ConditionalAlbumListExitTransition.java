package com.doctoror.fuckoffmusicplayer.library.albums.conditional;

import com.doctoror.fuckoffmusicplayer.R;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Exit transition for {@link ConditionalAlbumListFragment} content view
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ConditionalAlbumListExitTransition extends Transition {

    private static final String TRANSLATION_Y = "translationY";
    private static final String DUMMY_PROPERTY_NAME = "d";

    public ConditionalAlbumListExitTransition() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ConditionalAlbumListExitTransition(final Context context, final AttributeSet attrs) {
        super(context, attrs);
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
        final View appBar = sceneRoot.findViewById(R.id.appBar);
        if (appBar != null) {
            animators.add(ObjectAnimator.ofFloat(appBar, TRANSLATION_Y, 0, -appBar.getHeight()));
        }

        final View recyclerView = sceneRoot.findViewById(R.id.recyclerView);
        if (recyclerView != null) {
            animators.add(ObjectAnimator.ofFloat(recyclerView, TRANSLATION_Y,
                    0, recyclerView.getHeight()));
        }

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        return animatorSet;
    }
}
