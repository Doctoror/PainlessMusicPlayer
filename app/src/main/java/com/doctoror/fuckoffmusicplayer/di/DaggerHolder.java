package com.doctoror.fuckoffmusicplayer.di;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Dagger2 {@link MainComponent} holder
 */
public final class DaggerHolder {

    private static volatile DaggerHolder sInstance;

    @NonNull
    public static DaggerHolder getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (DaggerHolder.class) {
                if (sInstance == null) {
                    sInstance = new DaggerHolder(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private final MainComponent mMainComponent;

    @NonNull
    private final WearComponent mWearComponent;

    private DaggerHolder(@NonNull final Context context) {
        mMainComponent = DaggerMainComponent.builder()
                .appContextModule(new AppContextModule(context))
                .build();

        mWearComponent = DaggerWearComponent.builder().build();
    }

    @NonNull
    public MainComponent mainComponent() {
        return mMainComponent;
    }

    @NonNull
    public WearComponent wearComponent() {
        return mWearComponent;
    }
}
