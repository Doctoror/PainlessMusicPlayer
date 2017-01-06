package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.library.albums.AlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.livelists.LivePlaylistsFragment;
import com.doctoror.fuckoffmusicplayer.media.browser.MediaBrowserImpl;
import com.doctoror.fuckoffmusicplayer.playlist.CurrentPlaylist;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Main Component
 */
@Singleton
@Component(modules = {
        AppContextModule.class,
        MediaStoreProvidersModule.class,
        PlaylistsModule.class
})
public interface MainComponent {

    void inject(AlbumsFragment target);

    void inject(LivePlaylistsFragment target);

    void inject(CurrentPlaylist target);

    void inject(MediaBrowserImpl target);

}
