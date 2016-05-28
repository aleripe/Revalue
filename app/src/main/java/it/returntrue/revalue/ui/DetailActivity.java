package it.returntrue.revalue.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;

public class DetailActivity extends AppCompatActivity implements DetailFragment.OnSetFabVisibilityListener {
    public static final String EXTRA_ID = "id";

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.layout_multipane) @Nullable LinearLayout mLayoutMultipane;
    @Bind(R.id.fab_chat) FloatingActionButton mFabChat;
    @Bind(R.id.fab_revalue) FloatingActionButton mFabRevalue;
    @Bind(R.id.fab_remove) FloatingActionButton mFabRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final DetailFragment detailFragment =
                (DetailFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);

        // Binds controls
        ButterKnife.bind(this);

        // Sets toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Sets chat floating action button
        mFabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailFragment.goToChatActivity();
            }
        });

        // Sets revalue floating action button
        mFabRevalue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailFragment.setItemAsRevalued();
            }
        });

        // Sets remove floating action button
        mFabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailFragment.setItemAsRemoved();
            }
        });
    }

    @Override
    public void onSetChatFab(boolean isOwner) {
        // Sets floating action buttons visibility
        mFabChat.setVisibility((mLayoutMultipane != null || isOwner) ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onSetRevalueFab(boolean isOwner) {
        // Sets floating action buttons visibility
        mFabRevalue.setVisibility(isOwner ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onSetRemoveFab(boolean isOwner) {
        // Sets floating action buttons visibility
        mFabRemove.setVisibility(isOwner ? View.VISIBLE : View.INVISIBLE);
    }
}