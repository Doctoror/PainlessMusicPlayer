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
package com.doctoror.fuckoffmusicplayer.presentation.library

import android.app.Fragment
import android.content.Context
import com.doctoror.fuckoffmusicplayer.di.scopes.FragmentScope
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.Module
import dagger.Provides

@Module
class LibraryListModule {

    @Provides
    @FragmentScope
    fun provideRxPermissions(fragment: Fragment) = RxPermissions(fragment.activity)

    @Provides
    @FragmentScope
    fun providePermissionsProvider(
            context: Context,
            rxPermissions: RxPermissions) = LibraryPermissionsProvider(context, rxPermissions)

    @Provides
    @FragmentScope
    fun provideOptionsMenuInvalidator(fragment: Fragment): OptionsMenuInvalidator = {
        fragment.activity?.invalidateOptionsMenu()
    }

    @Provides
    @FragmentScope
    fun provideSearchQuerySource(fragment: LibraryListFragment) = fragment.searchQuerySource
}
