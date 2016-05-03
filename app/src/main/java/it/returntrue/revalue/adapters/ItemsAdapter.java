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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.ItemModel;

/**
 * Adapts data returned from cursor to show in a RecyclerView
 * */
public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
    public static final String TAG = ItemsAdapter.class.getSimpleName();

    private OnItemClickListener mOnItemClickListener;
    private final Context mContext;
    private List<ItemModel> mItems;

    /** Provides listeners for click events */
    public interface OnItemClickListener {
        void onFavoriteClick(View view, long id);
        void onItemClick(View view, Uri uri);
    }

    public ItemsAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
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
        ItemModel itemModel = mItems.get(position);
        holder.textTitle.setText(itemModel.Title);
        holder.textLocation.setText(itemModel.City + " / " +
                (int)(itemModel.Distance / 1000) + " km");
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size();
        }
        else {
            return 0;
        }
    }

    public void setItems(List<ItemModel> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    /** Represents a ViewHolder for a RecyclerView item */
    public final class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text_title) public TextView textTitle;
        @Bind(R.id.text_location) public TextView textLocation;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}