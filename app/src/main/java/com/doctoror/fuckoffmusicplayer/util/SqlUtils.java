package com.doctoror.fuckoffmusicplayer.util;

import android.database.DatabaseUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * SQL utils
 */
public final class SqlUtils {

    private SqlUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Escapes string and wraps it for LIKE argument.
     * Given the input "John's", we get '%John''s%'
     *
     * @param string the string to escape and wrap
     * @return string as LIKE argument
     */
    @NonNull
    public static String escapeAndWrapForLikeArgument(@Nullable final String string) {
        return wrapForLikeArgument(DatabaseUtils.sqlEscapeString(string));
    }

    /**
     * Wraps escaped string for LIKE argument.
     * Given the input 'thing', we get '%thing%'
     *
     * @param escapedString the escaped String for SQL query
     * @return string as LIKE argument
     */
    @NonNull
    public static String wrapForLikeArgument(@NonNull final String escapedString) {
        if (escapedString.isEmpty()) {
            throw new IllegalArgumentException("The input must not be empty");
        }
        if (escapedString.length() < 2
                || escapedString.charAt(0) != '\''
                || escapedString.charAt(escapedString.length() - 1) != '\'') {
            throw new IllegalArgumentException("The input must be wrapped in single quotes");
        }

        final StringBuilder sb = new StringBuilder(escapedString.length() + 2);
        sb.append(escapedString);
        sb.insert(1, '%');
        sb.insert(sb.length() - 1, '%');
        return sb.toString();
    }
}
