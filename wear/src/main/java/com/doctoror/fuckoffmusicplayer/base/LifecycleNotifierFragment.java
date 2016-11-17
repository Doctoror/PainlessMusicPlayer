package com.doctoror.fuckoffmusicplayer.base;

import android.app.Fragment;
import android.content.Context;

/**
 * Fragment that notifies it's Activity when starts
 */
public abstract class LifecycleNotifierFragment extends Fragment {

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (!(context instanceof FragmentAwareActivity)) {
            throw new RuntimeException("Expected to be attached to FragmentAwareActivity");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((FragmentAwareActivity) getActivity()).onFragmentStart(this);
    }
}
