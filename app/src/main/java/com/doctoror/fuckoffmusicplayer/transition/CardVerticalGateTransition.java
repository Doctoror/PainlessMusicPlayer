package com.doctoror.fuckoffmusicplayer.transition;

import com.doctoror.fuckoffmusicplayer.R;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * {@link VerticalGateTransition} for app bar and card view
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public final class CardVerticalGateTransition extends VerticalGateTransition {

    public CardVerticalGateTransition() {
        setBottomViewId(R.id.cardView);
    }
}
