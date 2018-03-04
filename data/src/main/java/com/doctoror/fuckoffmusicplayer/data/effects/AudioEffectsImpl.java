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
package com.doctoror.fuckoffmusicplayer.data.effects;

import com.doctoror.fuckoffmusicplayer.data.concurrent.Handlers;
import com.doctoror.fuckoffmusicplayer.data.effects.nano.EffectsProto;
import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.data.util.ProtoUtils;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;

import android.content.Context;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Observable;

public final class AudioEffectsImpl extends Observable implements AudioEffects {

    private static final String TAG = "AudioEffects";

    private final Object SETTINGS_LOCK = new Object();

    private static final String FILE_NAME_BASS_BOOST = "bass_boost_settings";
    private static final String FILE_NAME_EQUALIZER = "equalizer_settings";

    @NonNull
    private final Context mContext;

    @NonNull
    private final EffectsProto.BassBoostSettings mBassBoostSettings;

    @NonNull
    private final EffectsProto.EqualizerSettings mEqualizerSettings;

    private BassBoost mBassBoost;
    private Equalizer mEqualizer;

    private int mSessionId;

    public AudioEffectsImpl(@NonNull final Context context) {
        mContext = context;
        synchronized (SETTINGS_LOCK) {
            mBassBoostSettings = ProtoUtils.readFromFileNonNull(context, FILE_NAME_BASS_BOOST,
                    new EffectsProto.BassBoostSettings());
            mEqualizerSettings = ProtoUtils.readFromFileNonNull(context, FILE_NAME_EQUALIZER,
                    new EffectsProto.EqualizerSettings());
        }
    }

    @Override
    public boolean isBassBoostEnabled() {
        synchronized (SETTINGS_LOCK) {
            return mBassBoostSettings.enabled;
        }
    }

    @Override
    public boolean isEqualizerEnabled() {
        synchronized (SETTINGS_LOCK) {
            return mEqualizerSettings.enabled;
        }
    }

    @Override
    public int getBassBoostStrength() {
        synchronized (SETTINGS_LOCK) {
            return mBassBoostSettings.strength;
        }
    }

    @Override
    public int getSessionId() {
        return mSessionId;
    }

    @Nullable
    @Override
    public Equalizer getEqualizer() {
        return mEqualizer;
    }

    @Override
    public void create(final int sessionId) {
        if (mSessionId != sessionId) {
            relese();
            mSessionId = sessionId;
            final boolean bassBoostEnabled;
            final boolean equalizerEnabled;
            synchronized (SETTINGS_LOCK) {
                bassBoostEnabled = mBassBoostSettings.enabled;
                equalizerEnabled = mEqualizerSettings.enabled;
            }
            if (bassBoostEnabled) {
                restoreBassBoost();
                setChanged();
            }
            if (equalizerEnabled) {
                restoreEqualizer();
                setChanged();
            }
            notifyObservers();
        }
    }

    @Override
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

    @Override
    public void setBassBoostEnabled(final boolean enabled) {
        synchronized (SETTINGS_LOCK) {
            if (mBassBoostSettings.enabled != enabled) {
                mBassBoostSettings.enabled = enabled;
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
                persistBassBoostSettingsAsync();
            }
        }
        notifyObservers();
    }

    @Override
    public void setEqualizerEnabled(final boolean enabled) {
        synchronized (SETTINGS_LOCK) {
            if (mEqualizerSettings.enabled != enabled) {
                mEqualizerSettings.enabled = enabled;
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
                persistEqualizerSettingsAsync();
            }
        }
        notifyObservers();
    }

    private void restoreBassBoost() {
        mBassBoost = new BassBoost(Integer.MAX_VALUE, mSessionId);
        synchronized (SETTINGS_LOCK) {
            mBassBoost.setStrength((short) mBassBoostSettings.strength);
        }
        mBassBoost.setEnabled(true);
    }

    private void restoreEqualizer() {
        mEqualizer = new Equalizer(Integer.MAX_VALUE, mSessionId);

        synchronized (SETTINGS_LOCK) {
            final EffectsProto.EqualizerSettings proto = mEqualizerSettings;
            if (proto.curPreset != 0
                    || proto.numBands != 0
                    || proto.bandValues.length != 0) {
                final Equalizer.Settings settings = new Equalizer.Settings();
                settings.curPreset = (short) proto.curPreset;
                settings.numBands = (short) proto.numBands;
                settings.bandLevels = new short[proto.bandValues.length];
                for (int i = 0; i < settings.bandLevels.length; i++) {
                    settings.bandLevels[i] = (short) proto.bandValues[i];
                }

                try {
                    mEqualizer.setProperties(settings);
                } catch (IllegalArgumentException e) {
                    Log.wtf(TAG, "Failed restoring equalizer settings", e);
                }
            }
        }

        mEqualizer.setEnabled(true);
    }

    @Override
    public void setBassBoostStrength(final int strength) {
        synchronized (SETTINGS_LOCK) {
            if (mBassBoostSettings.strength != strength) {
                mBassBoostSettings.strength = strength;
                if (mBassBoost != null) {
                    mBassBoost.setStrength((short) strength);
                }
                persistBassBoostSettingsAsync();
            }
        }
    }

    @Override
    public void saveEqualizerSettings(@NonNull final Equalizer.Settings settings) {
        synchronized (SETTINGS_LOCK) {
            final EffectsProto.EqualizerSettings proto = mEqualizerSettings;
            proto.curPreset = settings.curPreset;
            proto.numBands = settings.numBands;

            final short[] bandLevels = settings.bandLevels;
            proto.bandValues = new int[bandLevels != null ? bandLevels.length : 0];
            if (bandLevels != null) {
                for (int i = 0; i < bandLevels.length; i++) {
                    proto.bandValues[i] = bandLevels[i];
                }
            }
        }

        persistEqualizerSettingsAsync();
    }

    private void persistBassBoostSettingsAsync() {
        Handlers.runOnIoThread(this::persistBassBoostSettingsBlocking);
    }

    private void persistBassBoostSettingsBlocking() {
        synchronized (SETTINGS_LOCK) {
            ProtoUtils.writeToFile(mContext, FILE_NAME_BASS_BOOST, mBassBoostSettings);
        }
    }

    private void persistEqualizerSettingsAsync() {
        Handlers.runOnIoThread(this::persistEqualizerSettingsBlocking);
    }

    private void persistEqualizerSettingsBlocking() {
        synchronized (SETTINGS_LOCK) {
            ProtoUtils.writeToFile(mContext, FILE_NAME_EQUALIZER, mEqualizerSettings);
        }
    }
}
