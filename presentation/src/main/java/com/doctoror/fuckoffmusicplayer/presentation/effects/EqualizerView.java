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

import android.content.Context;
import android.content.res.Resources;
import android.media.audiofx.Equalizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.presentation.widget.BaseRecyclerAdapter;
import com.doctoror.fuckoffmusicplayer.presentation.widget.OnSeekBarChangeListenerAdapter;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EqualizerView extends RecyclerView {

    public interface OnBandChangeListener {
        void onBandChange(short band, short value);
    }

    private Equalizer equalizer;
    private AdapterImpl adapter;

    private int eqLevelRange;
    private int minEqLevel;

    private OnBandChangeListener onBandChangeListener;

    @Inject
    AudioEffects mAudioEffects;

    public EqualizerView(final Context context) {
        super(context);
        init(context);
    }

    public EqualizerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EqualizerView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull final Context context) {
        DaggerHolder.getInstance(context).mainComponent().inject(this);
        setLayoutManager(new LinearLayoutManager(context));
    }

    public void setOnBandChangeListener(@Nullable final OnBandChangeListener onBandChangeListener) {
        this.onBandChangeListener = onBandChangeListener;
    }

    public void setEqualizer(@Nullable final Equalizer equalizer) {
        if (this.equalizer != equalizer) {
            this.equalizer = equalizer;
            rebuild();
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        if (adapter != null) {
            adapter.setEnabled(enabled);
            adapter.notifyDataSetChanged();
        }
    }

    private void rebuild() {
        if (equalizer == null) {
            if (adapter != null) {
                adapter.clear();
            }
        } else {
            final List<Item> items = buildItems(equalizer);
            if (adapter == null) {
                adapter = new AdapterImpl(getContext(), items);
                adapter.setMinEqLevel(minEqLevel);
                adapter.setEqLevelRange(eqLevelRange);
                adapter.setOnBandChangeListener(mInternalBandChangeListener);
                adapter.setEnabled(isEnabled());
                setAdapter(adapter);
            } else {
                adapter.setMinEqLevel(minEqLevel);
                adapter.setEqLevelRange(eqLevelRange);
                adapter.setItems(items);
            }
        }
    }

    private List<Item> buildItems(@NonNull final Equalizer equalizer) {
        final List<Item> items = new ArrayList<>(equalizer.getNumberOfBands());

        final short[] bandLevelRange = equalizer.getBandLevelRange();
        final short minEQLevel = bandLevelRange[0];
        final short maxEQLevel = bandLevelRange[1];

        eqLevelRange = maxEQLevel - minEQLevel;
        minEqLevel = minEQLevel;
        final Resources res = getResources();
        for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
            final Item item = new Item();
            final int herz = equalizer.getCenterFreq(i) / 1000;
            final String herzText;
            if (herz > 1000) {
                final NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                formatter.setMaximumFractionDigits(1);
                formatter.setMinimumFractionDigits(0);
                formatter.setRoundingMode(RoundingMode.HALF_UP);
                herzText = res.getString(R.string.s_kHz, formatter.format((float) herz / 1000f));
            } else {
                herzText = res.getString(R.string.d_Hz, herz);
            }
            item.title = herzText;
            item.seekBarValue = equalizer.getBandLevel(i) - minEQLevel;
            items.add(item);
        }
        return items;
    }

    private final OnBandChangeListener mInternalBandChangeListener = (band, value) -> {
        equalizer.setBandLevel(band, value);
        mAudioEffects.saveEqualizerSettings(equalizer.getProperties());
        if (onBandChangeListener != null) {
            onBandChangeListener.onBandChange(band, value);
        }
    };

    private static final class Item {

        int seekBarValue;
        String title;
    }

    static final class VH extends ViewHolder {

        @BindView(R.id.bandText) TextView bandName;
        @BindView(R.id.bandSeekBar) SeekBar bandValue;

        VH(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private static final class AdapterImpl extends BaseRecyclerAdapter<Item, VH> {

        private int eqLevelRange;
        private int minEqLevel;
        private boolean enabled;

        private OnBandChangeListener onBandChangeListener;

        AdapterImpl(@NonNull final Context context,
                @Nullable final List<Item> items) {
            super(context, items);
        }

        void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        void setEqLevelRange(final int range) {
            eqLevelRange = range;
        }

        void setMinEqLevel(final int minEqLevel) {
            this.minEqLevel = minEqLevel;
        }

        void setOnBandChangeListener(@Nullable final OnBandChangeListener onBandChangeListener) {
            this.onBandChangeListener = onBandChangeListener;
        }

        @Override
        @NonNull
        public VH onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            final VH vh = new VH(getLayoutInflater().inflate(
                    R.layout.equalizer_band, parent, false));
            vh.bandValue.setMax(eqLevelRange);
            vh.bandValue.setOnSeekBarChangeListener(new OnSeekBarChangeListenerAdapter() {

                @Override
                public void onProgressChanged(final SeekBar seekBar, final int progress,
                        final boolean fromUser) {
                    if (fromUser && onBandChangeListener != null) {
                        final int position = vh.getAdapterPosition();
                        final Item item = getItem(position);
                        item.seekBarValue = progress;
                        onBandChangeListener.onBandChange((short) position,
                                (short) (progress + minEqLevel));
                    }
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull final VH holder, final int position) {
            final Item item = getItem(position);
            holder.bandName.setText(item.title);
            holder.bandValue.setProgress(item.seekBarValue);

            holder.bandName.setEnabled(enabled);
            holder.bandValue.setEnabled(enabled);
        }
    }
}
