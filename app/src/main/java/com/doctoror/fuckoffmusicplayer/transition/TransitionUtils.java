package com.doctoror.fuckoffmusicplayer.transition;

import com.doctoror.fuckoffmusicplayer.BaseActivity;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.List;
import java.util.Map;

/**
 * Created by Yaroslav Mytkalyk on 30.11.16.
 */

public final class TransitionUtils {

    private TransitionUtils() {
        throw new UnsupportedOperationException();
    }

    public static void clearSharedElementsOnReturn(@NonNull final BaseActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionUtilsLollipop.clearSharedElementsOnReturn(activity);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static final class TransitionUtilsLollipop {

        private TransitionUtilsLollipop() {
            throw new UnsupportedOperationException();
        }

        static void clearSharedElementsOnReturn(@NonNull final BaseActivity activity) {
            activity.setEnterSharedElementCallback(new SharedElementCallback() {

                @Override
                public void onMapSharedElements(final List<String> names,
                        final Map<String, View> sharedElements) {
                    super.onMapSharedElements(names, sharedElements);
                    if (activity.isFinishingAfterTransition()) {
                        names.clear();
                        sharedElements.clear();
                    }
                }
            });
        }
    }

}
