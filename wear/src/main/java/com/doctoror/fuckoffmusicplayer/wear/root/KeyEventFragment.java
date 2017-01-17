package com.doctoror.fuckoffmusicplayer.wear.root;

import android.app.Activity;
import android.app.Fragment;
import android.view.KeyEvent;

/**
 * A {@link Fragment} that receives {@link #onKeyDown(int, KeyEvent)} from it's {@link Activity}
 */
public abstract class KeyEventFragment extends Fragment {

    protected boolean onKeyDown(final int keyCode, final KeyEvent event) {
        return false;
    }

}
