package com.doctoror.fuckoffmusicplayer.di;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger context module
 */
@Module
final class AppContextModule {

    @NonNull
    private final Context mContext;

    AppContextModule(@NonNull final Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return mContext;
    }

    @Provides
    @Singleton
    ContentResolver provideContentResolver() {
        return mContext.getContentResolver();
    }
}
