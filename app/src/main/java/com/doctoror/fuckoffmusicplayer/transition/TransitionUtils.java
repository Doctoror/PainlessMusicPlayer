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
 * Transition utils
 */
public final class TransitionUtils {

    private TransitionUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean supportsActivityTransitions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
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
