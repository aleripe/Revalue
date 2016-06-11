package it.returntrue.revalue.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.GregorianCalendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.adapters.MessagesAdapter;
import it.returntrue.revalue.api.MessageModel;
import it.returntrue.revalue.api.RevalueServiceContract;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.data.MessageContract.MessageEntry;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.provider.MessageProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    protected static final int LOADER_MESSAGES = 1;

    private SessionPreferences mSessionPreferences;
    private int mItemId;
    private int mReceiverId;
    private int mSenderId;
    private MessagesAdapter mMessagesAdapter;

    @Bind(R.id.list_messages) RecyclerView mRecyclerView;
    @Bind(R.id.text_message) EditText mTextMessage;
    @Bind(R.id.button_send) FloatingActionButton mButtonSend;

    public ChatFragment() { }

    public static Fragment newInstance(int itemId, int receiverId) {
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setItemId(itemId);
        chatFragment.setReceiverId(receiverId);
        return chatFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(getContext());

        // Gets extra data from intent or preferences
        mSenderId = mSessionPreferences.getUserId();

        // Binds controls
        ButterKnife.bind(this, getView());

        // Creates adapter for messages
        mMessagesAdapter = new MessagesAdapter(getContext(), mSessionPreferences);

        // Creates layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);

        // Sets list properties
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mMessagesAdapter);

        // Sets listeners
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mTextMessage.getText())) return;

                GregorianCalendar calendar = new GregorianCalendar();

                //Creates message
                MessageModel messageModel = new MessageModel();
                messageModel.UserId = mReceiverId;
                messageModel.ItemId = mItemId;
                messageModel.Text = mTextMessage.getText().toString();
                messageModel.Date = calendar.getTimeInMillis();

                ContentValues values = new ContentValues(2);
                values.put(MessageEntry.COLUMN_ITEM_ID, messageModel.ItemId);
                values.put(MessageEntry.COLUMN_SENDER_ID, mSenderId);
                values.put(MessageEntry.COLUMN_RECEIVER_ID, messageModel.UserId);
                values.put(MessageEntry.COLUMN_TEXT, messageModel.Text);
                values.put(MessageEntry.COLUMN_DATE, messageModel.Date);
                getActivity().getContentResolver().insert(MessageProvider.buildMessageUri(), values);

                mTextMessage.setText("");
                mButtonSend.setEnabled(false);

                // Calls API to send message
                RevalueServiceContract service = RevalueServiceGenerator.createService(mSessionPreferences.getToken());
                Call<Void> call = service.SendMessage(messageModel);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        mButtonSend.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        mButtonSend.setEnabled(true);
                    }
                });
            }
        });

        // Setup the available loader
        getLoaderManager().initLoader(LOADER_MESSAGES, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                MessageProvider.buildMessageUri(),
                null,
                MessageEntry.COLUMN_ITEM_ID + " = ? " + "AND (" +
                        MessageEntry.COLUMN_RECEIVER_ID + " = ? OR " +
                        MessageEntry.COLUMN_SENDER_ID + " = ?) AND (" +
                        MessageEntry.COLUMN_RECEIVER_ID + " = ? OR " +
                        MessageEntry.COLUMN_SENDER_ID + " = ?)",
                new String[] {
                        String.valueOf(mItemId),
                        String.valueOf(mReceiverId),
                        String.valueOf(mReceiverId),
                        String.valueOf(mSenderId),
                        String.valueOf(mSenderId)
                },
                MessageEntry.COLUMN_DATE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMessagesAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMessagesAdapter.setCursor(null);
    }

    public void setItemId(int itemId) {
        mItemId = itemId;
    }

    public void setReceiverId(int receiverId) {
        mReceiverId = receiverId;
    }
}