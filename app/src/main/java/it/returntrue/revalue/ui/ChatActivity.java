package it.returntrue.revalue.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;

public class ChatActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_ALIAS = "user_alias";

    private String mUserAlias;

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

        // Sets toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mUserAlias);
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
}