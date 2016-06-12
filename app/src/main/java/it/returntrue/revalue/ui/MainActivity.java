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

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.events.AddFavoriteItemEvent;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.GetCategoriesEvent;
import it.returntrue.revalue.events.RemoveFavoriteItemEvent;
import it.returntrue.revalue.services.RevalueGcmIntentService;
import it.returntrue.revalue.ui.base.BaseActivity;
import it.returntrue.revalue.ui.base.BaseItemsFragment;
import it.returntrue.revalue.utilities.Constants;
import it.returntrue.revalue.utilities.NetworkUtilities;

public class MainActivity extends BaseActivity implements BaseItemsFragment.OnItemClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private static final String FRAGMENT_FILTERS = "filters";

    private BaseItemsFragment mMainFragment;

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

    private @Constants.ItemMode int mItemMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checks login
        checkLogin();

        // Sets default item mode
        mItemMode = Constants.NEAREST_ITEMS_MODE;

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

        // Sets navigation view
        mNavigationView.setNavigationItemSelectedListener(this);

        // Setups search box to update list or map
        setupSearchBox();

        // Sets default mode
        setMode(mApplication.getMainMode());

        // Registers GCM
        registerGCM();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_search:
                mDrawerLayout.closeDrawers();
                showNearestItems();
                return true;
            case R.id.nav_favorites:
                mDrawerLayout.closeDrawers();
                showFavoriteItems();
                return true;
            case R.id.nav_personal:
                mDrawerLayout.closeDrawers();
                showPersonalItems();
                return true;
            case R.id.nav_logout:
                mDrawerLayout.closeDrawers();
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
    public void onItemClick(View view, int id) {
        // Opens details activity
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_ID, id);
        startActivity(intent);
    }

    @Override
    public void onAddFavoriteClick(View view, int id) {
        BusProvider.bus().post(new AddFavoriteItemEvent.OnStart(id));
    }

    @Override
    public void onRemoveFavoriteClick(View view, int id) {
        BusProvider.bus().post(new RemoveFavoriteItemEvent.OnStart(id));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe
    public void onGetCategoriesSuccess(GetCategoriesEvent.OnSuccess onSuccess) {
        mApplication.setCategories(onSuccess.getCategories());
        setFilters();
    }

    @Subscribe
    public void onGetCategoriesFailure(GetCategoriesEvent.OnFailure onFailure) {
        setStatus(getString(R.string.could_not_get_categories));
    }

    @Subscribe
    public void onAddFavoriteItemSuccess(AddFavoriteItemEvent.OnSuccess onSuccess) {
        Toast.makeText(this, R.string.favorite_item_added, Toast.LENGTH_LONG).show();
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
        mItemMode = Constants.NEAREST_ITEMS_MODE;
        updateListAndMap();
    }

    private void showFavoriteItems() {
        mItemMode = Constants.FAVORITE_ITEMS_MODE;
        updateListAndMap();
    }

    private void showPersonalItems() {
        mItemMode = Constants.PERSONAL_MOVIES_MODE;
        updateListAndMap();
    }

    private void logout() {
        mSessionPreferences.logout();
        checkLogin();
    }

    private void setFilters() {
        mBoxSearch.setVisibility(View.VISIBLE);
        mTextFilterTitle.setText(mApplication.getFilterTitleDescription());
        mTextFilterCategory.setText(mApplication.getFilterCategoryDescription());
        mTextFilterDistance.setText(mApplication.getFilterDistanceDescription());
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

    private void setupFloatingActionButton() {
        // Reacts to FAB click
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void setMode(@RevalueApplication.Modes int mode) {
        switch (mode) {
            case RevalueApplication.LIST_MODE:
                showList();
                break;
            case RevalueApplication.MAP_MODE:
                showMap();
                break;
        }
    }

    private void showList() {
        if (mFragmentContainer != null) {
            mMainFragment = ListFragment.newInstance(mItemMode);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mMainFragment)
                    .commit();

            mApplication.setMainMode(RevalueApplication.LIST_MODE);
        }
    }

    private void showMap() {
        if (mFragmentContainer != null) {
            mAppBar.setExpanded(true);
            mMainFragment = MapFragment.newInstance(mItemMode);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mMainFragment)
                    .commit();

            mApplication.setMainMode(RevalueApplication.MAP_MODE);
        }
    }

    private void updateListAndMap() {
        if (mMainFragment != null) {
            mMainFragment.updateItems(mItemMode);
        }
        else {
            Fragment listFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_list);
            Fragment mapFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_map);

            if (listFragment != null) {
                ((ListFragment) listFragment).updateItems(mItemMode);
            }

            if (mapFragment != null) {
                ((MapFragment) mapFragment).updateItems(mItemMode);
            }
        }
    }

    private void setStatus(String status) {
        if (mMainFragment != null) {
            mMainFragment.setStatus(status);
        }
    }

    private void registerGCM() {
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RevalueGcmIntentService.class);
            startService(intent);
        }
    }
}