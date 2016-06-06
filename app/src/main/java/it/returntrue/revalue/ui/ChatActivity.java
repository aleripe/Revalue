package it.returntrue.revalue.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;

public class ChatActivity extends AppCompatActivity implements ChatsFragment.OnItemClickListener {
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_ALIAS = "user_alias";
    public static final String EXTRA_IS_OWNED = "is_owned";

    private static final String FRAGMENT_CHATS = "chats";
    private static final String FRAGMENT_CHAT = "chat";

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
        mUserAlias = getIntent().getStringExtra(ChatActivity.EXTRA_USER_ALIAS);
        mIsOwned = getIntent().getBooleanExtra(ChatActivity.EXTRA_IS_OWNED, false);

        // Sets toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Adds chat fragment
        if (mIsOwned) {
            getSupportActionBar().setTitle(R.string.messages);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new ChatsFragment())
                    .commit();
        }
        else {
            getSupportActionBar().setTitle(mUserAlias);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new ChatFragment())
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

    @Override
    public void onItemClick(View view, int id) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("ChatsFragment")
                .replace(R.id.fragment_container, new ChatFragment())
                .commit();
    }
}