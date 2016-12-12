package com.doctoror.fuckoffmusicplayer.library.recentalbums;

import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListQuery;
import com.doctoror.fuckoffmusicplayer.transition.TransitionUtils;
import com.doctoror.fuckoffmusicplayer.transition.VerticalGateTransition;
import com.doctoror.fuckoffmusicplayer.util.SelectionUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

/**
 * "Recently played albums" Activity
 */
public final class RecentAlbumsActivity extends BaseActivity {

    public static final String TRANSITION_NAME_ROOT = "TRANSITION_NAME_ROOT";

    @InjectExtra
    long[] albumIds;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);

        ViewCompat.setTransitionName(findViewById(android.R.id.content),
                TRANSITION_NAME_ROOT);

        supportPostponeEnterTransition();

        TransitionUtils.clearSharedElementsOnReturn(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setReturnTransition(new VerticalGateTransition());
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(android.R.id.content,
                    ConditionalAlbumListFragment.instantiate(this, newParams())).commit();
        }
    }

    @NonNull
    private RxCursorLoader.Query newParams() {
        return ConditionalAlbumListQuery.newParams(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                SelectionUtils.inSelectionLong(MediaStore.Audio.Albums._ID, albumIds),
                SelectionUtils.orderByLongField(MediaStore.Audio.Albums._ID, albumIds));
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

}
