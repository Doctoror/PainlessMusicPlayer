package com.doctoror.fuckoffmusicplayer.nowplaying;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.transition.VerticalGateTransition;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Exit transition for {@link NowPlayingActivity} content view
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
final class ArtAndControlsGateTransition extends VerticalGateTransition {

    ArtAndControlsGateTransition() {
        setUpperViewId(R.id.albumArtContainer);
        setBottomViewId(R.id.controlsContainer);
    }
}
