/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.parcelable.converter;

import android.databinding.ObservableField;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.presentation.navigation.NavigationItem;

import org.parceler.ParcelConverter;

public final class NavigationItemObservableFieldConverter
        implements ParcelConverter<ObservableField<NavigationItem>> {

    @Override
    public void toParcel(
            @Nullable final ObservableField<NavigationItem> input,
            @NonNull final Parcel parcel) {
        parcel.writeInt(input != null ? input.get().ordinal() : -1);
    }

    @Override
    public ObservableField<NavigationItem> fromParcel(@NonNull final Parcel parcel) {
        final int value = parcel.readInt();
        return new ObservableField<>(value != -1 ? NavigationItem.values()[value] : null);
    }
}
