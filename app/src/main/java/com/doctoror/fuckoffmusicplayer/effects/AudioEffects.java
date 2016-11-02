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

import android.content.Context;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Observable;

/**
 * Created by Yaroslav Mytkalyk on 22.10.16.
 */

public final class AudioEffects extends Observable {

    private static AudioEffects sInstance;

    @NonNull
    public static AudioEffects getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (AudioEffects.class) {
                if (sInstance == null) {
                    sInstance = new AudioEffects(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private final AudioEffectsPrefs mPrefs;

    private BassBoost mBassBoost;
    private Equalizer mEqualizer;

    private int mSessionId;

    private AudioEffects(@NonNull final Context context) {
        mPrefs = AudioEffectsPrefs.with(context);
    }

    @NonNull
    AudioEffectsPrefs getPrefs() {
        return mPrefs;
    }

    int getSessionId() {
        return mSessionId;
    }

    @Nullable
    Equalizer getEqualizer() {
        return mEqualizer;
    }

    public void create(final int sessionId) {
        if (mSessionId != sessionId) {
            relese();
            mSessionId = sessionId;
            if (mPrefs.isBassBoostEnabled()) {
                restoreBassBoost();
                setChanged();
            }
            if (mPrefs.isEqualizerEnabled()) {
                restoreEqualizer();
                setChanged();
            }
            notifyObservers();
        }
    }

    public void relese() {
        mSessionId = 0;
        if (mBassBoost != null) {
            mBassBoost.setEnabled(false);
            mBassBoost.release();
            mBassBoost = null;
            setChanged();
        }
        if (mEqualizer != null) {
            mEqualizer.setEnabled(false);
            mEqualizer.release();
            mEqualizer = null;
            setChanged();
        }
        notifyObservers();
    }

    void setBassBoostEnabled(final boolean enabled) {
        mPrefs.setBassBoostEnabled(enabled);
        if (enabled) {
            if (mBassBoost == null && mSessionId != 0) {
                restoreBassBoost();
                setChanged();
            }
        } else {
            if (mBassBoost != null) {
                mBassBoost.setEnabled(false);
                mBassBoost.release();
                mBassBoost = null;
                setChanged();
            }
        }
        notifyObservers();
    }

    void setEqualizerEnabled(final boolean enabled) {
        mPrefs.setEqualizerEnabled(enabled);
        if (enabled) {
            if (mEqualizer == null && mSessionId != 0) {
                restoreEqualizer();
                setChanged();
            }
        } else {
            if (mEqualizer != null) {
                mEqualizer.setEnabled(false);
                mEqualizer.release();
                mEqualizer = null;
                setChanged();
            }
        }
        notifyObservers();
    }

    private void restoreBassBoost() {
        mBassBoost = new BassBoost(Integer.MAX_VALUE, mSessionId);
        mBassBoost.setStrength((short) mPrefs.getBassBoostStrength());
        mBassBoost.setEnabled(true);
    }

    private void restoreEqualizer() {
        mEqualizer = new Equalizer(Integer.MAX_VALUE, mSessionId);
        final EqualizerSettingsWrapper wrapper = mPrefs.fetchEqualizerSettings();
        if (wrapper != null) {
            mEqualizer.setProperties(wrapper.settings);
        }
        mEqualizer.setEnabled(true);
    }

    void setBassBoostStrength(final int strength) {
        mPrefs.setBassBoostStrength(strength);
        if (mBassBoost != null) {
            mBassBoost.setStrength((short) strength);
        }
    }

    void saveEqualizerSettings(@NonNull final Equalizer.Settings settings) {
        mPrefs.setEqualizerSettings(new EqualizerSettingsWrapper(settings));
    }
}
