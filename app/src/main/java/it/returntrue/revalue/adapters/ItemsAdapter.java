/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.returntrue.revalue.R;

/**
 * Adapts data returned from cursor to show in a RecyclerView
 * */
public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
    public static final String TAG = ItemsAdapter.class.getSimpleName();

    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;

    /** Provides listeners for click events */
    public interface OnItemClickListener {
        void onFavoriteClick(View view, long id);
        void onItemClick(View view, Uri uri);
    }

    public ItemsAdapter(Context context) {
        mContext = context;
    }

    /** Sets a new click events listener */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, null);

                }
            }
        });

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 20;
    }

    /** Represents a ViewHolder for a RecyclerView item */
    public final class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}