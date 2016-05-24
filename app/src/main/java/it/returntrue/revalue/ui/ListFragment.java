package it.returntrue.revalue.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.adapters.ItemsAdapter;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.ui.base.MainFragment;

/**
 * Shows the list of items
 */
public class ListFragment extends MainFragment implements ItemsAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {
    private ItemsAdapter mItemsAdapter;

    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list_items) RecyclerView mRecyclerView;
    @Bind(R.id.text_empty) TextView mTextEmpty;

    public ListFragment() { }

    public static ListFragment newInstance(@MainFragment.ItemMode int itemMode) {
        ListFragment fragment = new ListFragment();
        fragment.ItemMode = itemMode;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets option menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Binds controls
        ButterKnife.bind(this, view);

        // Sets refresh listener
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Creates list objects
        mRecyclerView.setHasFixedSize(true);
        mItemsAdapter = new ItemsAdapter(getContext());
        mItemsAdapter.setOnItemClickListener(this);

        // Sets RecyclerView's side objects
        mRecyclerView.setAdapter(mItemsAdapter);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));

        // Sets waiting text
        setEmptyText(getString(R.string.waiting_gps_fix));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAddFavoriteClick(View view, int itemId) {
        if (OnItemClickListener != null) {
            OnItemClickListener.onAddFavoriteClick(view, itemId);
        }
    }

    @Override
    public void onRemoveFavoriteClick(View view, int id) {
        if (OnItemClickListener != null) {
            OnItemClickListener.onRemoveFavoriteClick(view, id);
        }
    }

    @Override
    public void onItemClick(View view, int id) {
        if (OnItemClickListener != null) {
            OnItemClickListener.onItemClick(view, id);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<ItemModel>> loader, List<ItemModel> data) {
        if (mItemsAdapter != null) {
            if (data.size() > 0) {
                clearEmptyText();
            }
            else {
                setEmptyText("No results found.");
            }

            mItemsAdapter.setItems(data);
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<List<ItemModel>> loader) {
        if (mItemsAdapter != null) {
            mItemsAdapter.setItems(new ArrayList<ItemModel>());
        }
    }

    @Override
    public void onRefresh() {
        updateItems(null);
    }

    private void setEmptyText(String text) {
        mTextEmpty.setVisibility(View.VISIBLE);
        mTextEmpty.setText(text);
    }

    private void clearEmptyText() {
        mTextEmpty.setVisibility(View.INVISIBLE);
        mTextEmpty.setText("");
    }
}