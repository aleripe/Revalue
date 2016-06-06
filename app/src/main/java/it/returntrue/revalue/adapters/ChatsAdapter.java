/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.data.MessageData;

/**
 * Adapts data returned from cursor to show in a RecyclerView
 * */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    private static final String TAG = ChatsAdapter.class.getSimpleName();

    private OnItemClickListener mOnItemClickListener;
    private final Context mContext;
    private Cursor mCursor;

    /** Provides listeners for click events */
    public interface OnItemClickListener {
        void onItemClick(View view, int id);
    }

    public ChatsAdapter(Context context) {
        mContext = context;
    }

    /** Sets a new click events listener */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chats_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        holder.textText.setText(MessageData.getText(mCursor));
    }

    /** Represents a ViewHolder for a RecyclerView item */
    public final class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text_text) public TextView textText;

        public ViewHolder(View view) {
            super(view);

            // Binds controls
            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mCursor.moveToPosition(getAdapterPosition());
                        mOnItemClickListener.onItemClick(view, MessageData.getUserId(mCursor));
                    }
                }
            });
        }
    }
}