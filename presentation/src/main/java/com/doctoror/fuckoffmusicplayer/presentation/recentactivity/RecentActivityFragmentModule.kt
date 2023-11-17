package com.doctoror.fuckoffmusicplayer.presentation.recentactivity

import android.content.res.Resources
import com.doctoror.commons.reactivex.SchedulersProvider
import com.doctoror.fuckoffmusicplayer.RuntimePermissions
import com.doctoror.fuckoffmusicplayer.di.scopes.FragmentScope
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryPermissionsChecker
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryPermissionsRequester
import com.doctoror.fuckoffmusicplayer.presentation.library.albums.AlbumClickHandler
import com.doctoror.fuckoffmusicplayer.presentation.rxpermissions.RxPermissionsProvider
import dagger.Module
import dagger.Provides

@Module
class RecentActivityFragmentModule {

    @Provides
    @FragmentScope
    fun provideAlbumItemsFactory() = AlbumItemsFactory()

    @Provides
    @FragmentScope
    fun provideAlbumClickHandler(
        fragment: RecentActivityFragment,
        queueProvider: QueueProviderAlbums
    ) = AlbumClickHandler(fragment, queueProvider)

    @Provides
    @FragmentScope
    fun provideRecentActivityPresenter(
        albumClickHandler: AlbumClickHandler,
        albumItemsFactory: AlbumItemsFactory,
        albumsProvider: AlbumsProvider,
        libraryPermissionsChecker: LibraryPermissionsChecker,
        libraryPermissionRequester: LibraryPermissionsRequester,
        resources: Resources,
        runtimePermissions: RuntimePermissions,
        schedulersProvider: SchedulersProvider,
        viewModel: RecentActivityViewModel
    ) = RecentActivityPresenter(
        albumClickHandler,
        albumItemsFactory,
        albumsProvider,
        libraryPermissionsChecker,
        libraryPermissionRequester,
        resources,
        runtimePermissions,
        schedulersProvider,
        viewModel
    )

    @Provides
    @FragmentScope
    fun provideRecentActivityViewModel() = RecentActivityViewModel()

    @Provides
    @FragmentScope
    fun provideRxPermissionsProvider(fragment: RecentActivityFragment) =
        RxPermissionsProvider(fragment.requireActivity())

    @Provides
    @FragmentScope
    fun provideLibraryPermissionsProvider(
        runtimePermissions: RuntimePermissions,
        rxPermissionsProvider: RxPermissionsProvider
    ) = LibraryPermissionsRequester(
        runtimePermissions,
        rxPermissionsProvider
    )
}
