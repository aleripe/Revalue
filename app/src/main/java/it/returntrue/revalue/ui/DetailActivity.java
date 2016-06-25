/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.events.LoginRequestedEvent;
import it.returntrue.revalue.ui.base.BaseActivity;

/**
 * Shows single item details
 * */
@SuppressWarnings({"ConstantConditions", "WeakerAccess", "unused"})
public class DetailActivity extends BaseActivity {
    public static final String EXTRA_ID = "id";

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.fab_chat) FloatingActionButton mFabChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets layout
        setContentView(R.layout.activity_detail);

        // Binds controls
        ButterKnife.bind(this);

        // Sets toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setups FAB to go to chat
        mFabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailFragment detailFragment =
                        (DetailFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_detail);
                detailFragment.goToChatActivity();
            }
        });
    }

    @Subscribe
    public void onLoginRequestedStart(LoginRequestedEvent.OnStart onStart) {
        logout();
    }
}