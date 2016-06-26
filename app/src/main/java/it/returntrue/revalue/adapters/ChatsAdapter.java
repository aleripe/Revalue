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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.UserModel;
import it.returntrue.revalue.data.MessageData;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.ViewChatEvent;
import it.returntrue.revalue.preferences.SessionPreferences;

/**
 * Adapts chats returned from cursor to show in a RecyclerView
 * */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    private final SessionPreferences mSessionPreferences;
    private final Context mContext;
    private Cursor mCursor;
    private HashMap<Integer, UserModel> mUsers;

    public ChatsAdapter(Context context, SessionPreferences sessionPreferences) {
        mContext = context;
        mSessionPreferences = sessionPreferences;
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public void setUsers(List<UserModel> users) {
        mUsers = new HashMap<>();

        for (UserModel user : users) {
            mUsers.put(user.Id, user);
        }
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

        int currentUserId = mSessionPreferences.getUserId();
        int senderId = MessageData.getSenderId(mCursor);
        int receiverId = MessageData.getReceiverId(mCursor);
        UserModel user = mUsers.get((currentUserId != senderId) ? senderId : receiverId);
        String prefix = (currentUserId == senderId) ? mContext.getString(R.string.you) + ": " : "";
        String date = MessageData.getDate(mCursor);
        String text = prefix + MessageData.getText(mCursor);

        Glide.with(mContext)
                .load(user.Avatar)
                .into(holder.imageAvatar);

        holder.textAlias.setText(user.Alias);
        holder.textDate.setText(date);
        holder.textText.setText(text);
    }

    /** Represents a ViewHolder for a RecyclerView item */
    @SuppressWarnings("unused")
    public final class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.image_avatar) public ImageView imageAvatar;
        @Bind(R.id.text_alias) public TextView textAlias;
        @Bind(R.id.text_date) public TextView textDate;
        @Bind(R.id.text_text) public TextView textText;

        public ViewHolder(View view) {
            super(view);

            // Binds controls
            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCursor.moveToPosition(getAdapterPosition());

                    int userId = mSessionPreferences.getUserId();
                    int senderId = MessageData.getSenderId(mCursor);
                    int receiverId = MessageData.getReceiverId(mCursor);
                    int otherId = (userId != senderId) ? senderId : receiverId;

                    BusProvider.bus().post(new ViewChatEvent.OnStart(otherId));
                }
            });
        }
    }
}