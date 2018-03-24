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

public final class EffectsFragmentModel {

    private final ObservableBoolean bassBoostEnabled = new ObservableBoolean();
    private final ObservableInt bassBoostStrength = new ObservableInt();

    private final ObservableBoolean equalizerEnabled = new ObservableBoolean();

    public ObservableBoolean isBassBoostEnabled() {
        return bassBoostEnabled;
    }

    void setBassBoostEnabled(final boolean enabled) {
        bassBoostEnabled.set(enabled);
    }

    public ObservableInt getBassBoostStrength() {
        return bassBoostStrength;
    }

    void setBassBoostStrength(final int strength) {
        bassBoostStrength.set(strength);
    }

    public ObservableBoolean isEqualizerEnabled() {
        return equalizerEnabled;
    }

    void setEqualizerEnabled(final boolean equalizerEnabled) {
        this.equalizerEnabled.set(equalizerEnabled);
    }
}
