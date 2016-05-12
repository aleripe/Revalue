package it.returntrue.revalue.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.CategoryModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.preferences.InterfacePreferences;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.ui.base.MainFragment;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements MainFragment.OnItemClickListener,
        NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<List<CategoryModel>> {
    protected static final int LOADER_CATEGORIES = 1;

    private SessionPreferences mSessionPreferences;
    private InterfacePreferences mInterfacePreferences;
    private List<CategoryModel> mCategories;

    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view) NavigationView mNavigationView;
    @Bind(R.id.appbar) AppBarLayout mAppBar;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.box_search) RelativeLayout mBoxSearch;
    @Bind(R.id.text_filter_title) TextView mTextFilterTitle;
    @Bind(R.id.text_filter_categories) TextView mTextFilterCategories;
    @Bind(R.id.text_filter_distance) TextView mTextFilterDistance;
    @Bind(R.id.fragment_container) @Nullable FrameLayout mFragmentContainer;
    @Bind(R.id.fab) FloatingActionButton mFloatingActionButton;

    private String mFilterTitle;
    private ArrayList<Integer> mFilterCategories;
    private Integer mFilterDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(this);
        mInterfacePreferences = new InterfacePreferences(this);

        // Checks login
        checkLogin();

        // Sets layout
        setContentView(R.layout.activity_main);

        // Binds controls
        ButterKnife.bind(this);

        // Sets toolbar
        setSupportActionBar(mToolbar);

        // Setup the available loader
        getSupportLoaderManager().initLoader(LOADER_CATEGORIES, null, this).forceLoad();

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
                editNameDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        setFilters();
                    }
                });
                editNameDialog.show(fm, "fragment_edit_name");
            }
        });

        setMode(mInterfacePreferences.getMainMode());
    }

    @Override
    protected void onDestroy() {
        mInterfacePreferences.clearAll();
        super.onDestroy();
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
    public void onItemClick(View view, long id) {
        // Opens details activity
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_ID, id);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<List<CategoryModel>> onCreateLoader(int id, Bundle args) {
        return new CategoryAsyncTaskLoader(this, mSessionPreferences);
    }

    @Override
    public void onLoadFinished(Loader<List<CategoryModel>> loader, List<CategoryModel> data) {
        mCategories = data;
        setFilters();
    }

    @Override
    public void onLoaderReset(Loader<List<CategoryModel>> loader) {

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
            finish();
        }

        String token = mSessionPreferences.getToken();
        if (!TextUtils.isEmpty(token)) Log.v(MainActivity.class.getSimpleName(), token);
    }

    private void setFilters() {
        mTextFilterTitle.setText(mInterfacePreferences.getFilterTitleDescription());
        mTextFilterDistance.setText(mInterfacePreferences.getFilterDistanceDescription());

        for (CategoryModel categoryModel : mCategories) {
            // Sets previous selection
            if (categoryModel.Id == mInterfacePreferences.getFilterCategory()) {
                mTextFilterCategories.setText(categoryModel.Name);
            }
        }
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

    private void setMode(@InterfacePreferences.Modes int mode) {
        switch (mode) {
            case InterfacePreferences.LIST_MODE:
                showList();
                break;
            case InterfacePreferences.MAP_MODE:
                showMap();
                break;
        }
    }

    private void showList() {
        if (mFragmentContainer != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ListFragment.newInstance())
                    .commit();

            mInterfacePreferences.setMainMode(InterfacePreferences.LIST_MODE);
        }
    }

    private void showMap() {
        if (mFragmentContainer != null) {
            mAppBar.setExpanded(true);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, MapFragment.newInstance())
                    .commit();

            mInterfacePreferences.setMainMode(InterfacePreferences.MAP_MODE);
        }
    }

    private static class CategoryAsyncTaskLoader extends AsyncTaskLoader<List<CategoryModel>> {
        private SessionPreferences mSessionPreferences;

        public CategoryAsyncTaskLoader(Context context, SessionPreferences sessionPreferences) {
            super(context);
            mSessionPreferences = sessionPreferences;
        }

        @Override
        public List<CategoryModel> loadInBackground() {
            RevalueService service = RevalueServiceGenerator.createService(
                    mSessionPreferences.getToken());
            Call<List<CategoryModel>> call = service.GetAllCategories();

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                return new ArrayList<>();
            }
        }
    }
}