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

import android.content.Context
import com.doctoror.fuckoffmusicplayer.di.scopes.FragmentScope
import com.doctoror.fuckoffmusicplayer.presentation.rxpermissions.RxPermissionsProvider
import dagger.Module
import dagger.Provides
import io.reactivex.Observable

@Module
class LibraryListModule {

    @Provides
    @FragmentScope
    fun provideRxPermissionsProvider(fragment: LibraryListFragment2) =
            RxPermissionsProvider(fragment.activity)

    @Provides
    @FragmentScope
    fun providePermissionsProvider(
            context: Context,
            rxPermissionsProvider: RxPermissionsProvider) =
            LibraryPermissionsProvider(context, rxPermissionsProvider)

    @Provides
    @FragmentScope
    fun provideOptionsMenuInvalidator(fragment: LibraryListFragment2): OptionsMenuInvalidator = {
        fragment.activity?.invalidateOptionsMenu()
    }

    @Provides
    @FragmentScope
    fun provideSearchQuerySource(fragment: LibraryListFragment2) = fragment.searchQuerySource

    @Provides
    @FragmentScope
    fun provideLibraryListModel() = LibraryListModel()

    @Provides
    @FragmentScope
    fun provideLibraryListPresenter(
            libraryPermissionProvider: LibraryPermissionsProvider,
            optionsMenuInvalidator: OptionsMenuInvalidator,
            searchQuerySource: Observable<String>,
            viewModel: LibraryListModel
    ) = LibraryListPresenter(
            libraryPermissionProvider,
            optionsMenuInvalidator,
            searchQuerySource,
            viewModel)
}
