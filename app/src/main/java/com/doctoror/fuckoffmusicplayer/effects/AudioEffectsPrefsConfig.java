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

import android.media.audiofx.Equalizer;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ds.gendalf.Converter;
import ds.gendalf.CustomPref;
import ds.gendalf.PrefsConfig;

/**
 * Created by Yaroslav Mytkalyk on 23.10.16.
 */

@PrefsConfig("AudioEffectsPrefs")
final class AudioEffectsPrefsConfig {

    boolean bassBoostEnabled;
    int bassBoostStrength;

    boolean equalizerEnabled;
    @CustomPref(EqualizerSettingsConverter.class)
    EqualizerSettingsWrapper equalizerSettings;

    static final class EqualizerSettingsConverter
            implements Converter<EqualizerSettingsWrapper, String> {

        @Override
        public String serialize(@Nullable final EqualizerSettingsWrapper settings) {
            if (settings == null) {
                return null;
            }
            final StringBuilder value = new StringBuilder(32);
            value.append(settings.settings.curPreset).append(',');
            value.append(settings.settings.numBands);
            if (settings.settings.bandLevels != null) {
                for (int i = 0; i < settings.settings.bandLevels.length; i++) {
                    value.append(',').append(settings.settings.bandLevels[i]);
                }
            }

            return value.toString();
        }

        @Override
        public EqualizerSettingsWrapper deserialize(@Nullable final String s) {
            if (TextUtils.isEmpty(s)) {
                return null;
            }
            final String[] values = s.split(",");
            final Equalizer.Settings settings = new Equalizer.Settings();
            if (values.length > 0) {
                settings.curPreset = Short.parseShort(values[0]);
            }
            if (values.length > 1) {
                settings.numBands = Short.parseShort(values[1]);
            }
            if (values.length > 2) {
                final short[] levels = new short[values.length - 2];
                for (int i = 2, level = 0; i < values.length; i++, level++) {
                    levels[level] = Short.parseShort(values[i]);
                }
                settings.bandLevels = levels;
            }
            return new EqualizerSettingsWrapper(settings);
        }
    }
}
