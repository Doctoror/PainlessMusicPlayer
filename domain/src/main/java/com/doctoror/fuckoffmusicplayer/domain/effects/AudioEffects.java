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
package com.doctoror.fuckoffmusicplayer.domain.effects;

import android.media.audiofx.Equalizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Observer;

public interface AudioEffects {

    void addObserver(Observer o);

    void deleteObserver(Observer o);

    boolean isBassBoostEnabled();

    boolean isEqualizerEnabled();

    int getBassBoostStrength();

    int getSessionId();

    @Nullable
    Equalizer getEqualizer();

    void create(int sessionId);

    void relese();

    void setBassBoostEnabled(boolean enabled);

    void setEqualizerEnabled(boolean enabled);
    
    void setBassBoostStrength(int strength);

    void saveEqualizerSettings(@NonNull Equalizer.Settings settings);
}
