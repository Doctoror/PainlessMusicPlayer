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
package com.doctoror.fuckoffmusicplayer.effects;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;

/**
 * Created by Yaroslav Mytkalyk on 23.10.16.
 */

public final class EffectsFragmentModel {

    private final ObservableBoolean mBassBoostEnabled = new ObservableBoolean();
    private final ObservableInt mBassBoostStrength = new ObservableInt();

    private final ObservableBoolean mEqualizerEnabled = new ObservableBoolean();

    public ObservableBoolean isBassBoostEnabled() {
        return mBassBoostEnabled;
    }

    public void setBassBoostEnabled(final boolean enabled) {
        mBassBoostEnabled.set(enabled);
    }

    public ObservableInt getBassBoostStrength() {
        return mBassBoostStrength;
    }

    public void setBassBoostStrength(final int strength) {
        mBassBoostStrength.set(strength);
    }

    public ObservableBoolean isEqualizerEnabled() {
        return mEqualizerEnabled;
    }

    public void setEqualizerEnabled(final boolean equalizerEnabled) {
        mEqualizerEnabled.set(equalizerEnabled);
    }
}
