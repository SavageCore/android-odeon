package fr.nihilus.mymusic.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.nihilus.mymusic.MediaBrowserFragment;
import fr.nihilus.mymusic.R;
import fr.nihilus.mymusic.utils.MediaIDHelper;

public class SongListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "SongListFragment";
    private static final String KEY_SONGS = "MediaItems";
    private static final String KEY_SCROLL = "ScrollY";

    private ArrayList<MediaItem> mSongs;
    private ListView mListView;
    private View mListContainer;
    private SongAdapter mAdapter;
    private ContentLoadingProgressBar mProgressBar;

    private final SubscriptionCallback mCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> items) {
            Log.d(TAG, "onChildrenLoaded: loaded " + items.size() + " from " + parentId);
            mSongs.clear();
            mSongs.addAll(items);
            mAdapter.notifyDataSetChanged();
            mProgressBar.hide();
            mListContainer.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSongs = savedInstanceState.getParcelableArrayList(KEY_SONGS);
        } else mSongs = new ArrayList<>();
        mAdapter = new SongAdapter(getContext(), mSongs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListContainer = view.findViewById(R.id.list_container);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        ViewCompat.setNestedScrollingEnabled(mListView, true);

        // FIXME: ListView header considéré comme item à l'index 0
        //setupListHeader(LayoutInflater.from(getContext()));

        mProgressBar = (ContentLoadingProgressBar) view.findViewById(android.R.id.progress);

        if (savedInstanceState == null) {
            mListContainer.setVisibility(View.GONE);
            mProgressBar.show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MediaBrowserFragment.getInstance(getActivity().getSupportFragmentManager())
                .subscribe(MediaIDHelper.MEDIA_ID_ALL_MUSIC, mCallback);
        getActivity().setTitle(R.string.all_music);
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserFragment.getInstance(getActivity().getSupportFragmentManager())
                .unsubscribe(MediaIDHelper.MEDIA_ID_ALL_MUSIC);
    }

    private void setupListHeader(final LayoutInflater inflater) {
        View listHeader = inflater.inflate(R.layout.list_header_button, mListView, false);
        mListView.addHeaderView(listHeader);
        listHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Tout jouer en aléatoire
                Toast.makeText(getContext(), R.string.play_all_shuffled, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_SONGS, mSongs);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        mListView = null;
        mProgressBar = null;
        mListContainer = null;
        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
        MediaItem clickedItem = mAdapter.getItem(position);
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null && clickedItem.isPlayable()) {
            Log.d(TAG, "onItemClick: playing song at position " + position);
            controller.getTransportControls().playFromMediaId(clickedItem.getMediaId(), null);
        }
    }
}
