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

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentEffectsBinding;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.jakewharton.rxbinding2.widget.RxSeekBar;

import java.util.Observer;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public final class EffectsFragment extends Fragment {

    private final EffectsFragmentModel mModel = new EffectsFragmentModel();

    private FragmentEffectsBinding mBinding;

    @Inject
    AudioEffects mAudioEffects;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        mAudioEffects.addObserver(mAudioEffectsObserver);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final FragmentEffectsBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_effects, container, false);
        binding.setModel(mModel);
        binding.switchBassBoost.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setBassBoostEnabled(isChecked));
        RxSeekBar.userChanges(binding.seekBarBassBoost).subscribe(new Consumer<Integer>() {

            private boolean mFirst = true;

            @Override
            public void accept(@NonNull final Integer progress) throws Exception {
                if (mFirst) {
                    mFirst = false;
                } else {
                    setBassBoostStrength(progress);
                }
            }
        });

        binding.switchEqualizer.setEnabled(mAudioEffects.getSessionId() != 0);
        binding.switchEqualizer.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setEqualizerEnabled(isChecked));

        mBinding = binding;
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        mModel.setBassBoostStrength(mAudioEffects.getBassBoostStrength());
        mModel.setBassBoostEnabled(mAudioEffects.isBassBoostEnabled());
        mModel.setEqualizerEnabled(mAudioEffects.isEqualizerEnabled());
        mBinding.equalizerView.setEqualizer(mAudioEffects.getEqualizer());
    }

    @Override
    public void onStop() {
        super.onStop();
        mBinding.equalizerView.setEqualizer(null);
    }

    private void setBassBoostEnabled(final boolean enabled) {
        mModel.setBassBoostEnabled(enabled);
        mAudioEffects.setBassBoostEnabled(enabled);
    }

    private void setBassBoostStrength(final int strength) {
        mModel.setBassBoostStrength(strength);
        mAudioEffects.setBassBoostStrength(strength);
    }

    private void setEqualizerEnabled(final boolean enabled) {
        mModel.setEqualizerEnabled(enabled);
        mAudioEffects.setEqualizerEnabled(enabled);
        mBinding.equalizerView.setEqualizer(mAudioEffects.getEqualizer());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioEffects.deleteObserver(mAudioEffectsObserver);
    }

    private final Observer mAudioEffectsObserver = (observable, data) -> {
        mBinding.equalizerView.setEqualizer(mAudioEffects.getEqualizer());
        mBinding.switchEqualizer.setEnabled(mAudioEffects.getSessionId() != 0);
    };
}
