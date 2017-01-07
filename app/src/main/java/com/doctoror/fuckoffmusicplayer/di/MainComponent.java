package com.doctoror.fuckoffmusicplayer.di;

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
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playlist.CurrentPlaylist;
import com.doctoror.fuckoffmusicplayer.wear.WearableListenerServiceImpl;
import com.doctoror.fuckoffmusicplayer.wear.WearableSearchProviderService;

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

    void inject(NowPlayingActivity target);

    void inject(ArtistsFragment target);

    void inject(AlbumsFragment target);

    void inject(GenresFragment target);

    void inject(TracksFragment target);

    void inject(ConditionalAlbumListFragment target);

    void inject(ArtistAlbumsFragment target);

    void inject(GenreAlbumsFragment target);

    void inject(RecentAlbumsFragment target);

    void inject(LivePlaylistsFragment target);

    void inject(CurrentPlaylist target);

    void inject(MediaBrowserImpl target);

    void inject(WearableListenerServiceImpl target);

    void inject(SearchUtils target);

    void inject(PlaybackService target);

    void inject(WearableSearchProviderService target);

}
