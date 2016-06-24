/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.ui;

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
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.adapters.ChatsAdapter;
import it.returntrue.revalue.data.MessageData;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.GetUsersByIdsEvent;
import it.returntrue.revalue.provider.MessageProvider;
import it.returntrue.revalue.ui.base.BaseFragment;

/**
 * Shows item chat lobby
 * */
@SuppressWarnings({"UnusedParameters", "WeakerAccess", "unused"})
public class LobbyFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_CHATS = 1;

    private int mItemId;
    private ChatsAdapter mChatsAdapter;

    @Bind(R.id.list_messages) RecyclerView mRecyclerView;

    public LobbyFragment() { }

    public static Fragment newInstance(int itemId) {
        LobbyFragment lobbyFragment = new LobbyFragment();
        lobbyFragment.setItemId(itemId);
        return lobbyFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lobby, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Binds controls
        ButterKnife.bind(this, getView());

        // Creates adapter for chats
        mChatsAdapter = new ChatsAdapter(getContext(), session());

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
                new String[] { String.valueOf(mItemId) },
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        ArrayList<Integer> userIds = new ArrayList<>();
        int currentUserId = session().getUserId();

        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            int senderId = MessageData.getSenderId(data);
            int receiverId = MessageData.getReceiverId(data);

            userIds.add((senderId != currentUserId) ? senderId : receiverId);
        }

        // Gets users
        BusProvider.bus().post(new GetUsersByIdsEvent.OnStart(userIds, data));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mChatsAdapter.setCursor(null);
    }

    @Subscribe
    public void onGetUsersByIdsSuccess(GetUsersByIdsEvent.OnSuccess onSuccess) {
        mChatsAdapter.setUsers(onSuccess.getUsers());
        mChatsAdapter.setCursor(onSuccess.getCursor());
    }

    @Subscribe
    public void onGetUsersByIdsFailure(GetUsersByIdsEvent.OnFailure onFailure) {
        Toast.makeText(getContext(), R.string.could_not_save_item, Toast.LENGTH_LONG).show();
    }

    private void setItemId(int itemId) {
        mItemId = itemId;
    }
}