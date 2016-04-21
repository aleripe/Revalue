package it.returntrue.revalue.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.preferences.SessionPreferences;

public class MainActivity extends AppCompatActivity implements ItemsFragment.OnItemClickListener {
    private static final String TAG_FRAGMENT_DETAIL = DetailFragment.class.getSimpleName();

    private SessionPreferences mSessionPreferences;
    private boolean mTwoPane;

    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.fab) FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creates session preferences manager
        mSessionPreferences = new SessionPreferences(this);

        // Checks login
        checkLogin();

        // Sets layout
        setContentView(R.layout.activity_main);

        // Binds controls
        ButterKnife.bind(this);

        // Sets toolbar
        setSupportActionBar(mToolbar);

        // Reacts to FAB click
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Sets drawer layout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Shows appropriate layout
        if (findViewById(R.id.fragment_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                DetailFragment detailFragment = new DetailFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_detail_container, detailFragment, TAG_FRAGMENT_DETAIL)
                        .commit();
            }
        }
        else {
            mTwoPane = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(View view, Uri uri) {
        if (mTwoPane) {
            // Shows fragment for details
            DetailFragment detailFragment = new DetailFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail_container, detailFragment, TAG_FRAGMENT_DETAIL)
                    .commit();
        }
        else {
            // Opens details activity
            Intent intent = new Intent(this, DetailActivity.class);
            startActivity(intent);
        }
    }

    private void checkLogin() {
        if (!mSessionPreferences.getIsLoggedIn()) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }
}