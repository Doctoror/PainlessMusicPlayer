package com.doctoror.commons.util;

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class ProtoPersister {

    private static final String TAG = "ProtoPersister";

    private ProtoPersister() {
        throw new UnsupportedOperationException();
    }

    public static void writeToFile(@NonNull final Context context,
            @NonNull final String fileName,
            @NonNull final MessageNano messageNano) {
        OutputStream os = null;
        try {
            os = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            final byte[] output = new byte[messageNano.getCachedSize()];
            messageNano.writeTo(CodedOutputByteBufferNano.newInstance(output));
            os.write(output);
            os.close();
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    @Nullable
    public static <T extends MessageNano> T readFromFile(@NonNull final Context context,
            @NonNull final T obj,
            @NonNull final String fileName) {
        InputStream is = null;
        T message = null;
        try {
            is = context.openFileInput(fileName);
            final byte[] bytes = ByteStreams.toByteArray(is);
            message = MessageNano.mergeFrom(obj, bytes);
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }

        return message;
    }
}
