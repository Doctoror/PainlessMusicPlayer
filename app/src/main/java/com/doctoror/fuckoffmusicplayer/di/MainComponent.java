package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.appwidget.SingleRowAppWidgetProvider;
import com.doctoror.fuckoffmusicplayer.filemanager.FileManagerService;
import com.doctoror.fuckoffmusicplayer.library.LibraryActivity;
import com.doctoror.fuckoffmusicplayer.library.albums.AlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;
import com.doctoror.fuckoffmusicplayer.library.artistalbums.ArtistAlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.artists.ArtistsFragment;
import com.doctoror.fuckoffmusicplayer.library.genrealbums.GenreAlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.genres.GenresFragment;
import com.doctoror.fuckoffmusicplayer.library.livelists.LivePlaylistsFragment;
import com.doctoror.fuckoffmusicplayer.library.recentalbums.RecentAlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.tracks.TracksFragment;
import com.doctoror.fuckoffmusicplayer.media.browser.MediaBrowserImpl;
import com.doctoror.fuckoffmusicplayer.media.browser.SearchUtils;
import com.doctoror.fuckoffmusicplayer.media.session.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivityIntentHandler;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.queue.QueueActivity;

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

    void inject(LibraryActivity target);

    void inject(QueueActivity target);

    void inject(NowPlayingActivity target);

    void inject(NowPlayingActivityIntentHandler target);

    void inject(ArtistsFragment target);

    void inject(AlbumsFragment target);

    void inject(GenresFragment target);

    void inject(TracksFragment target);

    void inject(ConditionalAlbumListFragment target);

    void inject(ArtistAlbumsFragment target);

    void inject(GenreAlbumsFragment target);

    void inject(RecentAlbumsFragment target);

    void inject(LivePlaylistsFragment target);

    void inject(MediaBrowserImpl target);

    void inject(SearchUtils target);

    void inject(PlaybackService target);

    void inject(FileManagerService target);

    void inject(SingleRowAppWidgetProvider target);

    void inject(MediaSessionHolder target);
}
