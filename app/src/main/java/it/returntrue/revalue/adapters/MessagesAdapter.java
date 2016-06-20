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
import it.returntrue.revalue.preferences.SessionPreferences;

/**
 * Adapts messages returned from cursor to show in a RecyclerView
 * */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private static final int ITEM_RECEIVED = 1;
    private static final int ITEM_SENT = 2;

    private final Context mContext;
    private final SessionPreferences mSessionPreferences;
    private Cursor mCursor;

    public MessagesAdapter(Context context, SessionPreferences sessionPreferences) {
        mContext = context;
        mSessionPreferences = sessionPreferences;
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
    public int getItemViewType(int position) {
        mCursor.moveToPosition(position);

        int userId = mSessionPreferences.getUserId();
        int senderId = MessageData.getSenderId(mCursor);
        int receiverId = MessageData.getReceiverId(mCursor);

        if (userId == senderId) return ITEM_SENT;
        if (userId == receiverId) return ITEM_RECEIVED;
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case ITEM_RECEIVED:
                view = LayoutInflater.from(mContext)
                        .inflate(R.layout.messages_list_item_left, parent, false);
                break;
            case ITEM_SENT:
                view = LayoutInflater.from(mContext)
                        .inflate(R.layout.messages_list_item_right, parent, false);
                break;
            default:
                throw new IllegalArgumentException("viewType");
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        holder.textText.setText(MessageData.getText(mCursor));
    }

    /** Represents a ViewHolder for a RecyclerView item */
    @SuppressWarnings("unused")
    public final class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text_text) public TextView textText;

        public ViewHolder(View view) {
            super(view);

            // Binds controls
            ButterKnife.bind(this, view);
        }
    }
}