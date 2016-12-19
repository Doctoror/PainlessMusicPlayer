package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.transition.VerticalGateTransition;

/**
 * {@link VerticalGateTransition} for PlaylistActivity with CardView
 */
final class CardVerticalGateTransition extends VerticalGateTransition {

    CardVerticalGateTransition() {
        setBottomViewId(R.id.cardView);
    }
}
