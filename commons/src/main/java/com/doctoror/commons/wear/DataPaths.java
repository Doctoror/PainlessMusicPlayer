package com.doctoror.commons.wear;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class DataPaths {

    private DataPaths() {
        throw new UnsupportedOperationException();
    }

    public static final class Assets {
        public static final String ALBUM_ART = "/assets/album/art";
    }

    public static final class Paths {

        public static final String MEDIA = "/playback/media";
        public static final String PLAYBACK_STATE = "/playback/state";
        public static final String PLAYLIST = "/playback/playlist";
    }

    public static final class Messages {
        public static final String PLAY_PAUSE = "/message/playpause";
        public static final String SEEK = "/message/seek";
        public static final String PREV = "/message/prev";
        public static final String NEXT = "/message/next";
    }

}
