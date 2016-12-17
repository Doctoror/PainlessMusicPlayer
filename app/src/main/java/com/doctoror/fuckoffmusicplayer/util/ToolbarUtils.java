package com.doctoror.fuckoffmusicplayer.util;

import com.doctoror.commons.util.Log;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * {@link Toolbar} utils
 */
public final class ToolbarUtils {

    private static final String TAG = "ToolbarUtils";

    private ToolbarUtils() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static TextView getTitleTextView(@NonNull final Toolbar toolbar) {
        try {
            final Field field = toolbar.getClass().getField("mTitleTextView");
            field.setAccessible(true);
            return (TextView) field.get(toolbar);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return null;
    }

}
