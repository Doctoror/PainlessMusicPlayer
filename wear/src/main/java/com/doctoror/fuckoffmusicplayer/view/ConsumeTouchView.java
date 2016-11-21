package com.doctoror.fuckoffmusicplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Yaroslav Mytkalyk on 21.11.16.
 */

public class ConsumeTouchView extends TextView {

    public ConsumeTouchView(final Context context) {
        super(context);
    }

    public ConsumeTouchView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ConsumeTouchView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ConsumeTouchView(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return true;
    }
}
