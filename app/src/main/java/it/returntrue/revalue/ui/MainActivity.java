/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.CategoryModel;
import it.returntrue.revalue.events.AddFavoriteItemEvent;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.GetCategoriesEvent;
import it.returntrue.revalue.events.RemoveFavoriteItemEvent;
import it.returntrue.revalue.events.ViewItemEvent;
import it.returntrue.revalue.ui.base.BaseActivity;
import it.returntrue.revalue.ui.base.BaseItemsFragment;
import it.returntrue.revalue.utilities.Constants;
import it.returntrue.revalue.utilities.NetworkUtilities;

/**
 * Shows item list or map
 * */
@SuppressWarnings({"UnusedParameters", "WeakerAccess", "ResourceType", "unused"})
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String FRAGMENT_FILTERS = "filters";
    private static final String EXTRA_ITEMS_MODE = "items_mode";

    private BaseItemsFragment mMainFragment;
    private List<CategoryModel> mCategories;

    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view) NavigationView mNavigationView;
    @Bind(R.id.appbar) AppBarLayout mAppBar;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.box_search) RelativeLayout mBoxSearch;
    @Bind(R.id.text_filter_title) TextView mTextFilterTitle;
    @Bind(R.id.text_filter_category) TextView mTextFilterCategory;
    @Bind(R.id.text_filter_distance) TextView mTextFilterDistance;
    @Bind(R.id.fragment_container) @Nullable FrameLayout mFragmentContainer;
    @Bind(R.id.fab_chat) FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checks login
        checkLogin();

        // Sets layout
        setContentView(R.layout.activity_main);

        // Binds controls
        ButterKnife.bind(this);

        // Sets toolbar
        setSupportActionBar(mToolbar);

        // Setup the available loader
        loadCategories();

        // Sets information on NavigationView header
        setNavigationViewHeader();

        // Setups FAB to insert a new item
        setupFloatingActionButton();

        // Setups ABD to respond to toggle button
        setupActionBarDrawer();

        // Setups search box to update list or map
        setupSearchBox();

        // Sets default mode
        setItemMode(application().getItemsMode());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_search:
                showNearestItems();
                break;
            case R.id.nav_favorites:
                showFavoriteItems();
                break;
            case R.id.nav_personal:
                showPersonalItems();
                break;
            case R.id.nav_logout:
                logout();
                break;
            default:
                return false;
        }

        mDrawerLayout.closeDrawers();
        return true;
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

    @Subscribe
    public void onViewItem(ViewItemEvent.OnStart onStart) {
        // Opens details activity
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_ID, onStart.getId());
        startActivity(intent);
    }

    @Subscribe
    public void onGetCategoriesSuccess(GetCategoriesEvent.OnSuccess onSuccess) {
        mCategories = onSuccess.getCategories();
        setFilters();
    }

    @Subscribe
    public void onGetCategoriesFailure(GetCategoriesEvent.OnFailure onFailure) {
        setStatus(getString(R.string.could_not_get_categories));
    }

    @Subscribe
    public void onAddFavoriteItemSuccess(AddFavoriteItemEvent.OnSuccess onSuccess) {
        toast(R.string.favorite_item_added);
        updateListAndMap();
    }

    @Subscribe
    public void onAddFavoriteItemFailure(AddFavoriteItemEvent.OnFailure onFailure) {
        Toast.makeText(this, R.string.could_not_add_favorite_item, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onRemoveFavoriteItemSuccess(RemoveFavoriteItemEvent.OnSuccess onSuccess) {
        Toast.makeText(this, R.string.favorite_item_removed, Toast.LENGTH_LONG).show();
        updateListAndMap();
    }

    @Subscribe
    public void onRemoveFavoriteItemFailure(RemoveFavoriteItemEvent.OnFailure onFailure) {
        Toast.makeText(this, R.string.could_not_remove_favorite_item, Toast.LENGTH_LONG).show();
    }

    private void loadCategories() {
        if (NetworkUtilities.checkInternetConnection(this)) {
            BusProvider.bus().post(new GetCategoriesEvent.OnStart());
        }
        else {
            setStatus(getString(R.string.check_connection));
        }
    }

    private void showNearestItems() {
        application().setMainMode(Constants.NEAREST_ITEMS_MODE);
        updateListAndMap();
    }

    private void showFavoriteItems() {
        application().setMainMode(Constants.FAVORITE_ITEMS_MODE);
        updateListAndMap();
    }

    private void showPersonalItems() {
        application().setMainMode(Constants.PERSONAL_MOVIES_MODE);
        updateListAndMap();
    }

    private void logout() {
        session().logout();
        checkLogin();
    }

    private void setFilters() {
        mBoxSearch.setVisibility(View.VISIBLE);
        mTextFilterTitle.setText(application().getFilterTitleDescription());
        mTextFilterCategory.setText(application().getFilterCategoryDescription(mCategories));
        mTextFilterDistance.setText(application().getFilterDistanceDescription());
    }

    private void setNavigationViewHeader() {
        View header = mNavigationView.getHeaderView(0);

        ImageView mImagePicture = (ImageView)header.findViewById(R.id.nav_header_image_picture);
        TextView mLabelUsername = (TextView)header.findViewById(R.id.nav_header_label_username);
        TextView mLabelEmail = (TextView)header.findViewById(R.id.nav_header_label_email);

        mLabelUsername.setText(session().getAlias());
        mLabelEmail.setText(session().getUsername());
        Glide.with(this).load(session().getAvatar()).into(mImagePicture);

        // Sets navigation view
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void setupFloatingActionButton() {
        // Reacts to FAB click
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!application().isLocationAvailable()) {
                    toast(R.string.waiting_gps_fix);
                    return;
                }

                // Opens insert activity
                Intent intent = new Intent(MainActivity.this, InsertActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupActionBarDrawer() {
        // Sets drawer layout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupSearchBox() {
        mBoxSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FiltersFragment filtersDialog = new FiltersFragment();
                filtersDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        setFilters();
                        updateListAndMap();
                    }
                });
                filtersDialog.show(fragmentManager, FRAGMENT_FILTERS);
            }
        });
    }

    private void setItemMode(@Constants.ItemsMode int mode) {
        switch (mode) {
            case Constants.LIST_MODE:
                showList();
                break;
            case Constants.MAP_MODE:
                showMap();
                break;
        }
    }

    private void showList() {
        if (mFragmentContainer != null) {
            mMainFragment = ListFragment.newInstance(application().getMainMode());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mMainFragment)
                    .commit();

            application().setItemsMode(Constants.LIST_MODE);
        }
    }

    private void showMap() {
        if (mFragmentContainer != null) {
            mAppBar.setExpanded(true);
            mMainFragment = MapFragment.newInstance(application().getMainMode());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mMainFragment)
                    .commit();

            application().setItemsMode(Constants.MAP_MODE);
        }
    }

    private void updateListAndMap() {
        if (mMainFragment != null) {
            mMainFragment.updateItems(application().getMainMode());
        }
        else {
            Fragment listFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_list);
            Fragment mapFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_map);

            if (listFragment != null) {
                ((ListFragment) listFragment).updateItems(application().getMainMode());
            }

            if (mapFragment != null) {
                ((MapFragment) mapFragment).updateItems(application().getMainMode());
            }
        }
    }

    private void setStatus(String status) {
        if (mMainFragment != null) {
            mMainFragment.setStatus(status);
        }
    }
}