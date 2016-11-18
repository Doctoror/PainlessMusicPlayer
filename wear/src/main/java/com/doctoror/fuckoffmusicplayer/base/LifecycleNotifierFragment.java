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
package com.doctoror.fuckoffmusicplayer.base;

import android.app.Fragment;
import android.content.Context;

/**
 * Fragment that notifies it's Activity when starts
 */
public abstract class LifecycleNotifierFragment extends Fragment {

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (!(context instanceof FragmentAwareActivity)) {
            throw new RuntimeException("Expected to be attached to FragmentAwareActivity");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((FragmentAwareActivity) getActivity()).onFragmentStart(this);
    }
}
