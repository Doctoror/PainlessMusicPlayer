package com.doctoror.fuckoffmusicplayer.domain.playback;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        RepeatMode.NONE,
        RepeatMode.QUEUE,
        RepeatMode.TRACK
})
@Retention(RetentionPolicy.SOURCE)
public @interface RepeatMode {

    int NONE = 0;
    int QUEUE = 1;
    int TRACK = 2;
}
