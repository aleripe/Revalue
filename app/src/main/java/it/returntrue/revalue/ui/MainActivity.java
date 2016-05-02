package it.returntrue.revalue.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.preferences.SessionPreferences;

public class MainActivity extends AppCompatActivity
        implements ListFragment.OnItemClickListener,
            NavigationView.OnNavigationItemSelectedListener {
    private SessionPreferences mSessionPreferences;

    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view) NavigationView mNavigationView;
    @Bind(R.id.appbar) AppBarLayout mAppBar;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.box_search) RelativeLayout mBoxSearch;
    @Bind(R.id.fragment_container) @Nullable FrameLayout mFragmentContainer;
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

        // Sets informations on NavigationView header
        setNavigationViewHeader();

        // Reacts to FAB click
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Opens insert activity
                Intent intent = new Intent(MainActivity.this, InsertActivity.class);
                startActivity(intent);
            }
        });

        // Sets drawer layout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Sets navigation view
        mNavigationView.setNavigationItemSelectedListener(this);

        mBoxSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                FiltersFragment editNameDialog = new FiltersFragment();
                editNameDialog.show(fm, "fragment_edit_name");
            }
        });

        // Shows list as default //TODO: salvare la vista precedentemente utilizzata
        showList();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_logout:
                logout();
                return true;
            default:
                return false;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                showList();
                return true;
            case R.id.action_map:
                showMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(View view, Uri uri) {
        // Opens details activity
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }

    private void logout() {
        mSessionPreferences.logout();
        checkLogin();
    }

    private void checkLogin() {
        if (!mSessionPreferences.getIsLoggedIn()) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        Log.v(MainActivity.class.getSimpleName(), mSessionPreferences.getToken());
    }

    private void setNavigationViewHeader() {
        View header = mNavigationView.getHeaderView(0);

        ImageView mImagePicture = (ImageView)header.findViewById(R.id.nav_header_image_picture);
        TextView mLabelUsername = (TextView)header.findViewById(R.id.nav_header_label_username);
        TextView mLabelEmail = (TextView)header.findViewById(R.id.nav_header_label_email);

        mLabelUsername.setText(mSessionPreferences.getAlias());
        mLabelEmail.setText(mSessionPreferences.getUsername());
        Glide.with(this).load(mSessionPreferences.getAvatar()).into(mImagePicture);
    }

    private void showList() {
        if (mFragmentContainer != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ListFragment.newInstance())
                    .commit();
        }
    }

    private void showMap() {
        if (mFragmentContainer != null) {
            mAppBar.setExpanded(true);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, MapFragment.newInstance())
                    .commit();
        }
    }
}