package com.doctoror.fuckoffmusicplayer.domain.queue.provider;

import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import io.reactivex.Observable;

public interface QueueFromSearchProvider {

    @NonNull
    Observable<List<Media>> queueSourceFromSearch(
            @NonNull String query,
            @Nullable Bundle extras);
}
