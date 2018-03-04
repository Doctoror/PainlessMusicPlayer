package com.doctoror.fuckoffmusicplayer.domain.settings;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        Theme.NIGHT,
        Theme.DAY,
        Theme.DAYNIGHT
})
@Retention(RetentionPolicy.SOURCE)
public @interface Theme {
    int NIGHT = 0;
    int DAY = 1;
    int DAYNIGHT = 2;
}
