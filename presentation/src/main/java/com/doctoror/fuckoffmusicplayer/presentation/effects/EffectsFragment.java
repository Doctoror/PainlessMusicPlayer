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
package com.doctoror.fuckoffmusicplayer.presentation.effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentEffectsBinding;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.jakewharton.rxbinding3.widget.RxSeekBar;

import java.util.Observer;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public final class EffectsFragment extends Fragment {

    private final EffectsFragmentModel model = new EffectsFragmentModel();

    private FragmentEffectsBinding binding;

    @Inject
    AudioEffects audioEffects;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
        audioEffects.addObserver(mAudioEffectsObserver);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final FragmentEffectsBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_effects, container, false);
        binding.setModel(model);
        binding.switchBassBoost.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setBassBoostEnabled(isChecked));
        RxSeekBar.userChanges(binding.seekBarBassBoost).subscribe(new Consumer<Integer>() {

            private boolean mFirst = true;

            @Override
            public void accept(@NonNull final Integer progress) {
                if (mFirst) {
                    mFirst = false;
                } else {
                    setBassBoostStrength(progress);
                }
            }
        });

        binding.switchEqualizer.setEnabled(audioEffects.getSessionId() != 0);
        binding.switchEqualizer.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setEqualizerEnabled(isChecked));

        this.binding = binding;
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        model.setBassBoostStrength(audioEffects.getBassBoostStrength());
        model.setBassBoostEnabled(audioEffects.isBassBoostEnabled());
        model.setEqualizerEnabled(audioEffects.isEqualizerEnabled());
        binding.equalizerView.setEqualizer(audioEffects.getEqualizer());
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.equalizerView.setEqualizer(null);
    }

    private void setBassBoostEnabled(final boolean enabled) {
        model.setBassBoostEnabled(enabled);
        audioEffects.setBassBoostEnabled(enabled);
    }

    private void setBassBoostStrength(final int strength) {
        model.setBassBoostStrength(strength);
        audioEffects.setBassBoostStrength(strength);
    }

    private void setEqualizerEnabled(final boolean enabled) {
        model.setEqualizerEnabled(enabled);
        audioEffects.setEqualizerEnabled(enabled);
        binding.equalizerView.setEqualizer(audioEffects.getEqualizer());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioEffects.deleteObserver(mAudioEffectsObserver);
    }

    private final Observer mAudioEffectsObserver = (observable, data) -> {
        binding.equalizerView.setEqualizer(audioEffects.getEqualizer());
        binding.switchEqualizer.setEnabled(audioEffects.getSessionId() != 0);
    };
}
