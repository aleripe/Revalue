package it.returntrue.revalue.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import it.returntrue.revalue.R;
import it.returntrue.revalue.adapters.ItemsAdapter;

/**
 * Shows the list of items
 */
public class ListFragment extends Fragment implements ItemsAdapter.OnItemClickListener {
    private RecyclerView mRecyclerView;
    private ItemsAdapter mItemsAdapter;
    private OnItemClickListener mOnItemClickListener;

    /** Provides listeners for click events */
    public interface OnItemClickListener {
        void onItemClick(View view, Uri uri);
    }

    public ListFragment() {
    }

    public static Fragment newInstance() {
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

        // Creates list objects
        mRecyclerView = (RecyclerView)view.findViewById(R.id.list_items);
        mRecyclerView.setHasFixedSize(true);
        mItemsAdapter = new ItemsAdapter(getContext());
        mItemsAdapter.setOnItemClickListener(this);

        // Sets RecyclerView's side objects
        mRecyclerView.setAdapter(mItemsAdapter);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_list, menu);
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
    public void onFavoriteClick(View view, long id) {

    }

    @Override
    public void onItemClick(View view, Uri uri) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, uri);
        }
    }
}