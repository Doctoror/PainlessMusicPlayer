package com.doctoror.fuckoffmusicplayer.search;

import com.doctoror.commons.wear.nano.WearSearchData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.remote.RemoteControl;
import com.doctoror.fuckoffmusicplayer.remote.SearchResultsObservable;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
import java.util.Observable;
import java.util.Observer;

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
    private Scene mSceneSearching;

    private ViewGroup mSceneRoot;

    private View mBtnInput;
    private TextView mTextViewMessage;
    private RecyclerView mListView;
    private SearchResultsAdapter mAdapter;

    private boolean mSearching;
    private String mSearchQuery;

    private Toast mToastSpeechNotSupported;

    public SearchFragment() {
        mDefaultTransition.addListener(new TransitionListenerImpl());
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchResultsObservable.getInstance().addObserver(mSearchResultsObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SearchResultsObservable.getInstance().deleteObserver(mSearchResultsObserver);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        mSceneRoot = (ViewGroup) view.findViewById(R.id.sceneRoot);
        mSceneMessage = Scene.getSceneForLayout(mSceneRoot, R.layout.fragment_search_message,
                getActivity());
        mSceneResults = Scene.getSceneForLayout(mSceneRoot, R.layout.fragment_search_results,
                getActivity());
        mSceneSearching = Scene.getSceneForLayout(mSceneRoot, R.layout.fragment_search_searching,
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

    private void findViews() {
        mBtnInput = mSceneRoot.findViewById(R.id.btnInput);
        mListView = (RecyclerView) mSceneRoot.findViewById(android.R.id.list);
        mTextViewMessage = (TextView) mSceneRoot.findViewById(android.R.id.message);
    }

    private void bindViews() {
        mBtnInput.setOnClickListener(mOnInputClickListener);
        if (mListView != null) {
            mListView.clearOnScrollListeners();
            mListView.addOnScrollListener(mOnScrollListener);
        }
    }

    private void bindScene() {
        if (mSearchResults == null) {
            if (mSearching) {
                goToScene(mSceneSearching);
            } else {
                if (mSceneCurrent != mSceneMessage) {
                    goToScene(mSceneMessage);
                } else {
                    mTextViewMessage.setText(R.string.Search_for_artists_albums_and_songs);
                }
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
                }
            } else {
                if (mSceneCurrent != mSceneResults) {
                    goToScene(mSceneResults);
                } else {
                    if (mAdapter == null) {
                        mAdapter = new SearchResultsAdapter(getActivity(), mSearchResults);
                        mListView.setAdapter(mAdapter);
                    } else {
                        mAdapter.setResults(mSearchResults);
                    }
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

    private final View.OnClickListener mOnInputClickListener = v -> {
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        final List<ResolveInfo> resolveInfos = getActivity().getPackageManager()
                .queryIntentActivities(intent, 0);
        if (resolveInfos != null && !resolveInfos.isEmpty()) {
            mSearchQuery = null;
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
            RemoteControl.getInstance().search(mSearchQuery);
            mSearchResults = null;
            mSearching = true;
            bindScene();
        } else if (requestCode == REQUEST_CODE_SPEECH) {
            // TODO REMOVE THIS IF ABOVE
            mSearchQuery = "Death";
            mSearching = true;
            RemoteControl.getInstance().search(mSearchQuery);
            bindScene();
        }
    }

    private final Observer mSearchResultsObserver = new Observer() {

        @Override
        public void update(final Observable observable, final Object o) {
            getActivity().runOnUiThread(() -> {
                mSearchResults = (WearSearchData.Results) o;
                mSearching = false;
                bindScene();
            });
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
                //noinspection WrongConstant
                if (targetVisibility != -1 && mBtnInput.getVisibility() != targetVisibility) {
                    //noinspection WrongConstant
                    mBtnInput.setVisibility(targetVisibility);
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
