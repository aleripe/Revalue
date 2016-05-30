package it.returntrue.revalue.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.io.IOException;
import java.util.GregorianCalendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.adapters.MessagesAdapter;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.api.MessageModel;
import it.returntrue.revalue.api.RevalueService;
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
    private int mId;
    private int mUserId;
    private MessagesAdapter mMessagesAdapter;

    @Bind(R.id.list_messages) RecyclerView mRecyclerView;
    @Bind(R.id.text_message) EditText mTextMessage;
    @Bind(R.id.button_send) FloatingActionButton mButtonSend;

    public ChatFragment() { }

    public static Fragment newInstance() {
        return new ChatFragment();
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

        // Gets extra data from intent
        mId = getActivity().getIntent().getIntExtra(ChatActivity.EXTRA_ID, 0);
        mUserId = getActivity().getIntent().getIntExtra(ChatActivity.EXTRA_USER_ID, 0);

        // Binds controls
        ButterKnife.bind(this, getView());

        // Creates adapter for messages
        mMessagesAdapter = new MessagesAdapter(getContext());

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
                GregorianCalendar calendar = new GregorianCalendar();

                //Creates message
                MessageModel messageModel = new MessageModel();
                messageModel.UserId = mSessionPreferences.getUserId();
                messageModel.ItemId = mId;
                messageModel.Text = mTextMessage.getText().toString();
                messageModel.Date = calendar.getTimeInMillis();

                ContentValues values = new ContentValues(2);
                values.put(MessageEntry.COLUMN_ITEM_ID, messageModel.ItemId);
                values.put(MessageEntry.COLUMN_USER_ID, messageModel.UserId);
                values.put(MessageEntry.COLUMN_TEXT, messageModel.Text);
                values.put(MessageEntry.COLUMN_IS_SENT, 1);
                values.put(MessageEntry.COLUMN_IS_RECEIVED, 0);
                values.put(MessageEntry.COLUMN_DISPATCH_DATE, messageModel.Date);
                getActivity().getContentResolver().insert(MessageProvider.buildMessageUri(), values);

                mTextMessage.setText("");
                mButtonSend.setEnabled(false);

                // Calls API to send message
                RevalueService service = RevalueServiceGenerator.createService(mSessionPreferences.getToken());
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
                MessageEntry.COLUMN_ITEM_ID + " = ?",
                new String[] { String.valueOf(mId) },
                MessageEntry.COLUMN_DISPATCH_DATE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMessagesAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMessagesAdapter.setCursor(null);
    }

    private static class DetailAsyncTaskLoader extends AsyncTaskLoader<ItemModel> {
        private final RevalueApplication mApplication;
        private final SessionPreferences mSessionPreferences;
        private final long mId;

        public DetailAsyncTaskLoader(RevalueApplication application, long id) {
            super(application);
            mApplication = application;
            mSessionPreferences = new SessionPreferences(application);
            mId = id;
        }

        @Override
        public ItemModel loadInBackground() {
            RevalueService service = RevalueServiceGenerator.createService(mSessionPreferences.getToken());
            Call<ItemModel> call = service.GetItem(mApplication.getLocationLatitude(),
                    mApplication.getLocationLongitude(), mId);

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                return null;
            }
        }
    }
}