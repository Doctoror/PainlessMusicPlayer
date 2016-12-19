package com.doctoror.fuckoffmusicplayer.transition;

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
