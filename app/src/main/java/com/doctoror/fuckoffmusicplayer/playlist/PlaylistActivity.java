/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.filemanager.DeleteFileDialogFragment;
import com.doctoror.fuckoffmusicplayer.filemanager.FileManagerService;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.parceler.Parcel;
import org.parceler.Parcels;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 20.10.16.
 */

public final class PlaylistActivity extends BaseActivity implements
        DeleteFileDialogFragment.Callback {

    private static final String EXTRA_STATE = "EXTRA_STATE";
    private static final String TAG_DIALOG_DELETE = "TAG_DIALOG_DELETE";

    @InjectExtra
    List<Media> playlist;

    @InjectExtra
    Boolean isNowPlayingPlaylist;

    private boolean mFinishWhenDialogDismissed;
    private DeleteSession mDeleteSession;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFinishWhenDialogDismissed = false;
        Dart.inject(this);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(android.R.id.content,
                    PlaylistFragment.instantiate(this, playlist, isNowPlayingPlaylist)).commit();
        } else {
            final State state = Parcels.unwrap(savedInstanceState.getParcelable(EXTRA_STATE));
            mDeleteSession = state.deleteSession;
            mFinishWhenDialogDismissed = state.finishWhenDialogDismissed;

            if (mDeleteSession.permissionRequested) {
                onDeleteClick(mDeleteSession.media);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final State state = new State();
        state.deleteSession = mDeleteSession;
        state.finishWhenDialogDismissed = mFinishWhenDialogDismissed;
        outState.putParcelable(EXTRA_STATE, Parcels.wrap(state));
    }

    @Override
    public void setSupportActionBar(@Nullable final Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE
                    | ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_HOME_AS_UP);
        }
    }

    @Override
    public void onDeleteClick(@NonNull final Media media) {
        if (mDeleteSession == null) {
            mDeleteSession = new DeleteSession(media);
        }
        mDeleteSession.permissionRequested = true;
        RxPermissions.getInstance(this).request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe((result) -> {
                    if (result) {
                        if (!isFinishing()) {
                            FileManagerService.delete(getApplicationContext(), media);
                        }
                    }
                    mDeleteSession = null;
                    finishIfNeeded();
                });
    }

    @Override
    public void onDeleteCancel() {
        mDeleteSession = null;
    }

    @Override
    public void onDeleteDialogDismiss() {
        finishIfNeeded();
    }

    void onDeleteClickFromList(@NonNull final Media media) {
        mDeleteSession = new DeleteSession(media);
        DeleteFileDialogFragment.show(this, media, getFragmentManager(), TAG_DIALOG_DELETE);
    }

    void onPlaylistEmpty() {
        mFinishWhenDialogDismissed = true;
        finishIfNeeded();
    }

    private void finishIfNeeded() {
        if (mFinishWhenDialogDismissed && mDeleteSession == null) {
            mFinishWhenDialogDismissed = false;
            finish();
        }
    }

    @Parcel
    static final class State {

        boolean finishWhenDialogDismissed;
        DeleteSession deleteSession;
    }

    @Parcel
    static final class DeleteSession {

        Media media;
        boolean permissionRequested;

        DeleteSession() {
        }

        DeleteSession(final Media media) {
            this.media = media;
        }
    }
}
