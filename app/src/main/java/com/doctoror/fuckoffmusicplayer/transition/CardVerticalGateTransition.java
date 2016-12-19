package com.doctoror.fuckoffmusicplayer.transition;

import com.doctoror.fuckoffmusicplayer.R;

/**
 * {@link VerticalGateTransition} for app bar and card view
 */
public final class CardVerticalGateTransition extends VerticalGateTransition {

    public CardVerticalGateTransition() {
        setBottomViewId(R.id.cardView);
    }
}
