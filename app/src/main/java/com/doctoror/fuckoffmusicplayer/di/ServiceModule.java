package com.doctoror.fuckoffmusicplayer.di;

import android.app.Service;
import android.support.annotation.NonNull;

import com.doctoror.fuckoffmusicplayer.di.scopes.ServiceScope;

import dagger.Module;
import dagger.Provides;

@Module
public final class ServiceModule {

    private final Service service;

    public ServiceModule(@NonNull final Service service) {
        this.service = service;
    }

    @Provides
    @ServiceScope
    Service provideService() {
        return service;
    }
}
