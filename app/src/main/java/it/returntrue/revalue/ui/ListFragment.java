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

    public ListFragment() { }

    public static ListFragment newInstance() {
        return new ListFragment();
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

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onFavoriteClick(View view, long id) {

    }

    @Override
    public void onItemClick(View view, long id) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, id);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<ItemModel>> loader, List<ItemModel> data) {
        if (mItemsAdapter != null) {
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
        updateItems();
    }
}