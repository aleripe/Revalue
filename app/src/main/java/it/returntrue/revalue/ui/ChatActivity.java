/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.squareup.otto.Subscribe;
import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.events.LoginRequestedEvent;
import it.returntrue.revalue.events.ViewChatEvent;
import it.returntrue.revalue.ui.base.BaseActivity;

/**
 * Shows chat interface (lobby and messages)
 * */
@SuppressWarnings({"FieldCanBeLocal", "ConstantConditions", "WeakerAccess", "unused"})
public class ChatActivity extends BaseActivity {
    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_ALIAS = "user_alias";
    public static final String EXTRA_IS_OWNED = "is_owned";

    private static final String FRAGMENT_LOBBY = "lobby";

    private int mItemId;
    private int mUserId;
    private String mUserAlias;
    private Boolean mIsOwned;

    @Bind(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets layout view
        setContentView(R.layout.activity_chat);

        // Binds controls
        ButterKnife.bind(this);

        // Gets extra data from intent
        mItemId = getIntent().getIntExtra(ChatActivity.EXTRA_ITEM_ID, 0);
        mUserId = getIntent().getIntExtra(ChatActivity.EXTRA_USER_ID, 0);
        mUserAlias = getIntent().getStringExtra(ChatActivity.EXTRA_USER_ALIAS);
        mIsOwned = getIntent().getBooleanExtra(ChatActivity.EXTRA_IS_OWNED, false);

        // Sets toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Adds chat fragment
        if (mIsOwned) {
            getSupportActionBar().setTitle(R.string.messages);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, LobbyFragment.newInstance(mItemId))
                    .commit();
        }
        else {
            getSupportActionBar().setTitle(mUserAlias);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, ChatFragment.newInstance(mItemId, mUserId))
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onLoginRequestedStart(LoginRequestedEvent.OnStart onStart) {
        logout();
    }

    @Subscribe
    public void onViewChatStart(ViewChatEvent.OnStart onStart) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(FRAGMENT_LOBBY)
                .replace(R.id.fragment_container, ChatFragment.newInstance(mItemId, onStart.getUserId()))
                .commitAllowingStateLoss();
    }
}