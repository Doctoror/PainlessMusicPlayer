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

import android.annotation.TargetApi;
import android.os.Build;
import android.transition.Transition;

/**
 * Created by Yaroslav Mytkalyk on 10.11.16.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class TransitionListenerAdapter implements Transition.TransitionListener {

    @Override
    public void onTransitionStart(final Transition transition) {

    }

    @Override
    public void onTransitionEnd(final Transition transition) {

    }

    @Override
    public void onTransitionCancel(final Transition transition) {

    }

    @Override
    public void onTransitionPause(final Transition transition) {

    }

    @Override
    public void onTransitionResume(final Transition transition) {

    }
}
