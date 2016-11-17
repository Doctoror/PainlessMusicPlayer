package com.doctoror.fuckoffmusicplayer.base;

import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;

/**
 * {@link WearableActivity} that knows when it's Fragments are started
 */
public abstract class FragmentAwareActivity extends WearableActivity {


    public void onFragmentStart(@NonNull final LifecycleNotifierFragment fragment) {

    }

}
