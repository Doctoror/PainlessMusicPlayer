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
package com.doctoror.fuckoffmusicplayer.presentation.home;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.view.View;

import com.doctoror.fuckoffmusicplayer.di.scopes.ActivityScope;
import com.doctoror.fuckoffmusicplayer.parcelable.converter.ObservableIntConverter;
import com.doctoror.fuckoffmusicplayer.presentation.navigation.NavigationItem;
import com.doctoror.fuckoffmusicplayer.presentation.navigation.NavigationViewModel;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.parceler.ParcelPropertyConverter;

import javax.inject.Inject;

@Parcel
@ActivityScope
public final class HomeViewModel {

    @ParcelPropertyConverter(ObservableIntConverter.class)
    public final ObservableInt playbackStatusCardVisibility = new ObservableInt(View.GONE);

    @ParcelPropertyConverter(ObservableIntConverter.class)
    public final ObservableInt title = new ObservableInt();

    public final ObservableField<NavigationItem> navigationItem = new ObservableField<>();

    public final NavigationViewModel navigationModel;

    @Inject
    @ParcelConstructor
    public HomeViewModel(@NonNull final NavigationViewModel navigationModel) {
        this.navigationModel = navigationModel;
    }

    void applyFrom(@NonNull final HomeViewModel other) {
        navigationModel.navigationItem.set(other.navigationModel.navigationItem.get());
        playbackStatusCardVisibility.set(other.playbackStatusCardVisibility.get());
        title.set(other.title.get());
    }
}
