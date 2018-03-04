package com.doctoror.fuckoffmusicplayer.data.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class StringUtils {

    private StringUtils() {

    }

    @NonNull
    public static String notNullString(@Nullable final String string) {
        return string != null ? string : "";
    }

    @NonNull
    public static String capWords(@NonNull final String input) {
        boolean prevWasWhiteSp = true;
        final char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                if (prevWasWhiteSp) {
                    chars[i] = Character.toUpperCase(chars[i]);
                }
                prevWasWhiteSp = false;
            } else {
                prevWasWhiteSp = Character.isWhitespace(chars[i]);
            }
        }
        return new String(chars);
    }
}
