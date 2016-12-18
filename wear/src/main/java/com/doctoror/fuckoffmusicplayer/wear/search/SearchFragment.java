package com.doctoror.fuckoffmusicplayer.wear.search;

import com.doctoror.commons.wear.nano.WearSearchData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventSearchResults;
import com.doctoror.fuckoffmusicplayer.wear.remote.RemoteControl;
import com.doctoror.fuckoffmusicplayer.wear.root.RootActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.transition.AutoTransition;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 22.11.16.
 */

public final class SearchFragment extends Fragment {

    private static final int REQUEST_CODE_SPEECH = 1;

    private final Transition mDefaultTransition = new AutoTransition();
    private WearSearchData.Results mSearchResults;

    private Scene mSceneCurrent;
    private Scene mSceneMessage;
    private Scene mSceneResults;

    private ViewGroup mSceneRoot;

    private View mBtnInput;
    private View mProgress;
    private TextView mTextViewMessage;
    private RecyclerView mListView;
    private SearchResultsAdapter mAdapter;

    private boolean mBtnSearchVisible = true;
    private boolean mSearching;
    private String mSearchQuery;

    private Toast mToastSpeechNotSupported;

    public SearchFragment() {
        mDefaultTransition.addListener(new TransitionListenerImpl());
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        mSceneRoot = (ViewGroup) view.findViewById(R.id.sceneRoot);
        mSceneMessage = new Scene(mSceneRoot, mSceneRoot.findViewById(R.id.container));
        mSceneResults = Scene.getSceneForLayout(mSceneRoot, R.layout.fragment_search_results,
                getActivity());
        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            mSceneCurrent = mSceneMessage;
            TransitionManager.go(mSceneMessage, mDefaultTransition);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBtnInput != null) {
            mBtnInput.setClickable(true);
        }
    }

    private void findViews() {
        mBtnInput = mSceneRoot.findViewById(R.id.btnInput);
        mProgress = mSceneRoot.findViewById(android.R.id.progress);

        mListView = (RecyclerView) mSceneRoot.findViewById(android.R.id.list);
        mTextViewMessage = (TextView) mSceneRoot.findViewById(android.R.id.message);
    }

    private void bindViews() {
        mBtnInput.setClickable(true);
        mBtnInput.setOnClickListener(mOnInputClickListener);
        if (mListView != null) {
            mListView.clearOnScrollListeners();
            mListView.addOnScrollListener(mOnScrollListener);
            if (mAdapter != null) {
                mListView.setAdapter(mAdapter);
            }
        }
        bindProgress();
    }

    private void bindProgress() {
        if (mProgress != null) {
            mProgress.setVisibility(mSearching ? View.VISIBLE : View.GONE);
        }
    }

    private void bindScene() {
        if (mSearchResults == null) {
            if (mSceneCurrent != mSceneMessage) {
                goToScene(mSceneMessage);
            } else {
                mTextViewMessage.setText(R.string.Search_for_artists_albums_and_songs);
                showBtnSearch();
            }
        } else {
            if (areSearchResultsEmpty()) {
                if (mSceneCurrent != mSceneMessage) {
                    goToScene(mSceneMessage);
                } else {
                    if (mSearchQuery == null) {
                        mSearchQuery = "";
                    }
                    mTextViewMessage
                            .setText(getString(R.string.No_media_found_for_s, mSearchQuery));
                    showBtnSearch();
                }
            } else {
                if (mSceneCurrent != mSceneResults) {
                    goToScene(mSceneResults);
                } else {
                    if (mAdapter == null) {
                        mAdapter = new SearchResultsAdapter(getActivity(), mSearchResults);
                        mAdapter.setOnItemClickListener(mOnItemClickListener);
                        mListView.setAdapter(mAdapter);
                    } else {
                        mAdapter.setResults(mSearchResults);
                    }
                    showBtnSearch();
                }
            }
        }
    }

    private boolean areSearchResultsEmpty() {
        //noinspection SimplifiableIfStatement
        if (mSearchResults == null) {
            return true;
        }
        return (mSearchResults.albums == null || mSearchResults.albums.length == 0)
                && (mSearchResults.artists == null || mSearchResults.artists.length == 0)
                && (mSearchResults.tracks == null || mSearchResults.tracks.length == 0);
    }

    private void goToScene(@NonNull final Scene scene) {
        if (mSceneCurrent != scene) {
            mSceneCurrent = scene;
            if (mListView != null) {
                mListView.clearOnScrollListeners();
            }
            TransitionManager.go(scene, mDefaultTransition);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSearchResults(@NonNull final EventSearchResults results) {
        mSearchResults = results.results;
        mSearching = false;
        bindProgress();
        bindScene();
    }

    private final View.OnClickListener mOnInputClickListener = v -> {
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        final List<ResolveInfo> resolveInfos = getActivity().getPackageManager()
                .queryIntentActivities(intent, 0);
        if (resolveInfos != null && !resolveInfos.isEmpty()) {
            mSearchQuery = null;
            mBtnInput.setClickable(false);
            startActivityForResult(intent, REQUEST_CODE_SPEECH);
        } else {
            if (mToastSpeechNotSupported == null) {
                mToastSpeechNotSupported = Toast.makeText(getActivity(),
                        R.string.Speech_recognition_not_supported, Toast.LENGTH_LONG);
            }
            if (mToastSpeechNotSupported.getView() == null
                    || mToastSpeechNotSupported.getView().getWindowToken() == null) {
                mToastSpeechNotSupported.show();
            }
        }
    };

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH && resultCode == Activity.RESULT_OK) {
            final List<String> results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                mSearchQuery = results.get(0);
            }
            mSearchResults = null;
            mSearching = true;
            RemoteControl.getInstance().search(mSearchQuery);
            bindProgress();
        }
    }

    private void showBtnSearch() {
        if (!mBtnSearchVisible) {
            mBtnSearchVisible = true;
            mBtnInput.animate().scaleX(1f).scaleY(1f)
                    .setListener(mAnimatorListenerBtnInputReveal)
                    .start();

        }
    }

    private void hideBtnSearch() {
        if (mBtnSearchVisible) {
            mBtnSearchVisible = false;
            mBtnInput.animate().scaleX(0f).scaleY(0f)
                    .setListener(mAnimatorListenerBtnInputHide)
                    .start();
        }
    }

    private void goToNowPlaying() {
        final Activity activity = getActivity();
        if (activity instanceof RootActivity) {
            ((RootActivity) activity).goToNowPlaying();
        }
    }

    private final Animator.AnimatorListener mAnimatorListenerBtnInputReveal
            = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(final Animator animation) {
            super.onAnimationStart(animation);
            if (mBtnInput != null) {
                mBtnInput.setVisibility(View.VISIBLE);
            }
        }
    };

    private final Animator.AnimatorListener mAnimatorListenerBtnInputHide
            = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(final Animator animation) {
            super.onAnimationEnd(animation);
            if (mBtnInput != null) {
                mBtnInput.setVisibility(View.GONE);
            }
        }
    };

    private final SearchResultsAdapter.OnItemClickListener mOnItemClickListener
            = new SearchResultsAdapter.OnItemClickListener() {

        @Override
        public void onAlbumClick(@NonNull final WearSearchData.Album album) {
            RemoteControl.getInstance().playAlbum(album.id);
            goToNowPlaying();
        }

        @Override
        public void onArtistClick(@NonNull final WearSearchData.Artist artist) {
            RemoteControl.getInstance().playArtist(artist.id);
            goToNowPlaying();
        }

        @Override
        public void onTrackClick(@NonNull final WearSearchData.Track[] tracks,
                final long trackId) {
            RemoteControl.getInstance().playTrack(tracks, trackId);
            goToNowPlaying();
        }
    };

    private final RecyclerView.OnScrollListener mOnScrollListener
            = new RecyclerView.OnScrollListener() {

        private final int mThreshold = (int) (1f * Resources.getSystem()
                .getDisplayMetrics().density);

        private int mScrollState;

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            mScrollState = newState;
        }

        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mBtnInput != null && mScrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                int targetVisibility = -1; // -1 for unchanged
                if (dy > mThreshold) {
                    targetVisibility = View.GONE;
                } else if (-dy > mThreshold) {
                    targetVisibility = View.VISIBLE;
                }
                if (targetVisibility != -1) {
                    if (targetVisibility == View.VISIBLE) {
                        showBtnSearch();
                    } else {
                        hideBtnSearch();
                    }
                }
            }
        }
    };

    private final class TransitionListenerImpl implements Transition.TransitionListener {

        @Override
        public void onTransitionStart(final Transition transition) {

        }

        @Override
        public void onTransitionEnd(final Transition transition) {
            findViews();
            bindViews();
            bindScene();
        }

        @Override
        public void onTransitionCancel(final Transition transition) {

        }

        @Override
        public void onTransitionPause(final Transition transition) {

        }

        @Override
        public void onTransitionResume(final Transition transition) {

        }
    }
}
