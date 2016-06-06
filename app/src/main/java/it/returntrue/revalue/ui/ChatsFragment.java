package it.returntrue.revalue.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.adapters.ChatsAdapter;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.provider.MessageProvider;

public class ChatsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
    ChatsAdapter.OnItemClickListener {
    protected static final int LOADER_CHATS = 1;

    private OnItemClickListener mOnItemClickListener;
    private SessionPreferences mSessionPreferences;
    private int mId;
    private int mUserId;
    private ChatsAdapter mChatsAdapter;

    @Bind(R.id.list_messages) RecyclerView mRecyclerView;

    /** Provides listeners for click events */
    public interface OnItemClickListener {
        void onItemClick(View view, int id);
    }

    public ChatsFragment() { }

    public static Fragment newInstance() {
        return new ChatsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnItemClickListener = (OnItemClickListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnItemClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(getContext());

        // Gets extra data from intent
        mId = getActivity().getIntent().getIntExtra(ChatActivity.EXTRA_ID, 0);
        mUserId = getActivity().getIntent().getIntExtra(ChatActivity.EXTRA_USER_ID, 0);

        // Binds controls
        ButterKnife.bind(this, getView());

        // Creates adapter for chats
        mChatsAdapter = new ChatsAdapter(getContext());
        mChatsAdapter.setOnItemClickListener(this);

        // Creates layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        // Sets list properties
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mChatsAdapter);

        // Setup the available loader
        getLoaderManager().initLoader(LOADER_CHATS, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                MessageProvider.buildChatUri(),
                null,
                null,
                new String[] { String.valueOf(mId) },
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mChatsAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mChatsAdapter.setCursor(null);
    }

    @Override
    public void onItemClick(View view, int id) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, id);
        }
    }
}