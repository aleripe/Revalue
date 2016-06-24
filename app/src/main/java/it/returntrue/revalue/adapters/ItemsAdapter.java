/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.events.AddFavoriteItemEvent;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.RemoveFavoriteItemEvent;
import it.returntrue.revalue.events.ViewItemEvent;

/**
 * Adapts items returned from cursor to show in a RecyclerView
 * */
public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
    private final Context mContext;
    private List<ItemModel> mItems;

    public ItemsAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.items_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ItemModel itemModel = mItems.get(position);

        Glide.with(mContext)
                .load(itemModel.PictureUrl)
                .into(holder.imageCover);

        holder.imageAddFavorite.setVisibility(!itemModel.IsOwned && !itemModel.IsFavorite ?
                View.VISIBLE : View.GONE);
        holder.imageRemoveFavorite.setVisibility(!itemModel.IsOwned && itemModel.IsFavorite ?
                View.VISIBLE : View.GONE);
        holder.textTitle.setText(itemModel.Title);
        holder.textLocation.setText(mContext.getString(R.string.item_location,
                itemModel.City, (int) (itemModel.Distance / 1000)));
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
        if (items != null) {
            mItems = items;
            notifyDataSetChanged();
        }
    }

    public void clearItems() {
        mItems.clear();
        notifyDataSetChanged();
    }

    /** Represents a ViewHolder for a RecyclerView item */
    @SuppressWarnings("unused")
    public final class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.image_cover) public ImageView imageCover;
        @Bind(R.id.image_add_favorite) public ImageView imageAddFavorite;
        @Bind(R.id.image_remove_favorite) public ImageView imageRemoveFavorite;
        @Bind(R.id.text_title) public TextView textTitle;
        @Bind(R.id.text_location) public TextView textLocation;

        public ViewHolder(View view) {
            super(view);

            // Binds controls
            ButterKnife.bind(this, view);

            imageAddFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemId = mItems.get(getAdapterPosition()).Id;
                    BusProvider.bus().post(new AddFavoriteItemEvent.OnStart(itemId));
                }
            });

            imageRemoveFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemId = mItems.get(getAdapterPosition()).Id;
                    BusProvider.bus().post(new RemoveFavoriteItemEvent.OnStart(itemId));
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemId = mItems.get(getAdapterPosition()).Id;
                    BusProvider.bus().post(new ViewItemEvent.OnStart(itemId));
                }
            });
        }
    }
}