package it.returntrue.revalue.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.utilities.MapUtilities;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<ItemModel> {
    private final static int LOADER_ITEM = 1;
    private long mId;

    private RevalueApplication mApplication;
    private SessionPreferences mSessionPreferences;
    private DetailAsyncTaskLoader mDetailAsyncTaskLoader;
    private Menu mMenu;
    private SupportMapFragment mMapFragment;
    private ImageView mImageCover;
    private ItemModel mItemModel;

    @Bind(R.id.text_title) public TextView mTextTitle;
    @Bind(R.id.text_location) public TextView mTextLocation;
    @Bind(R.id.text_description) public TextView mTextDescription;

    public DetailFragment() { }

    public static Fragment newInstance() {
        return new DetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets application context
        mApplication = (RevalueApplication)getActivity().getApplicationContext();

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(getContext());

        // Gets extra data from intent
        mId = getActivity().getIntent().getIntExtra(DetailActivity.EXTRA_ID, 0);

        // Sets option menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Binds controls
        mImageCover = (ImageView)getActivity().findViewById(R.id.image_cover);

        // Binds controls
        ButterKnife.bind(this, getView());
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);

        // Initializes loader
        getLoaderManager().initLoader(LOADER_ITEM, null, this).forceLoad();

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail, menu);
        mMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_favorite:
                addFavorite();
                return true;
            case R.id.action_remove_favorite:
                removeFavorite();
                return true;
            case R.id.action_share:
                share();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<ItemModel> onCreateLoader(int id, Bundle args) {
        mDetailAsyncTaskLoader = new DetailAsyncTaskLoader(mApplication, mId);
        return mDetailAsyncTaskLoader;
    }

    @Override
    public void onLoadFinished(Loader<ItemModel> loader, ItemModel data) {
        setDetails(data);
    }

    @Override
    public void onLoaderReset(Loader<ItemModel> loader) {
        clearDetails();
    }

    private void setDetails(ItemModel itemModel) {
        if (itemModel != null) {
            mItemModel = itemModel;

            Glide.with(getContext())
                    .load(itemModel.PictureUrl)
                    .into(mImageCover);

            // Sets toolbar title
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setTitle(itemModel.Title);

            // Shows only appropriate favorite button
            mMenu.findItem(R.id.action_add_favorite).setVisible(!itemModel.IsFavorite);
            mMenu.findItem(R.id.action_remove_favorite).setVisible(itemModel.IsFavorite);

            mTextTitle.setText(itemModel.Title);
            mTextLocation.setText(itemModel.City + " / " + (int) (itemModel.Distance / 1000) + " km");
            mTextDescription.setText(itemModel.Description);

            if (itemModel.ShowOnMap) {
                GoogleMap map = mMapFragment.getMap();
                if (map != null) {
                    LatLng coordinates = new LatLng(itemModel.Latitude, itemModel.Longitude);
                    map.addMarker(new MarkerOptions().position(coordinates));

                    Circle circle = MapUtilities.getCenteredCircle(map, coordinates,
                            mApplication.getFilterDistance());
                    int zoom = MapUtilities.getCircleZoomLevel(circle);

                    if (zoom > 0) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoom));
                    }
                }
            } else {
                mMapFragment.getView().setVisibility(View.GONE);
            }
        }
    }

    private void clearDetails() {

    }

    public void addFavorite() {
        if (mItemModel != null) {
            RevalueService service = RevalueServiceGenerator.createService(
                    mSessionPreferences.getToken());
            Call<Void> call = service.AddFavorite(mItemModel.Id);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Toast.makeText(DetailFragment.this.getContext(), "Favorite item added.", Toast.LENGTH_LONG).show();

                    if (mDetailAsyncTaskLoader != null) {
                        mDetailAsyncTaskLoader.onContentChanged();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(DetailFragment.this.getContext(), "Could not add favorite item.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void removeFavorite() {
        if (mItemModel != null) {
            RevalueService service = RevalueServiceGenerator.createService(
                    mSessionPreferences.getToken());
            Call<Void> call = service.RemoveFavorite(mItemModel.Id);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Toast.makeText(DetailFragment.this.getContext(), "Favorite item removed.", Toast.LENGTH_LONG).show();

                    if (mDetailAsyncTaskLoader != null) {
                        mDetailAsyncTaskLoader.onContentChanged();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(DetailFragment.this.getContext(), "Could not remove favorite item.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void share() {
        if (mItemModel != null) {
            Glide.with(getContext())
                    .load(mItemModel.PictureUrl)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            View view = new View(getContext());
                            view.draw(new Canvas(resource));
                            String path = MediaStore.Images.Media.insertImage(
                                    getActivity().getContentResolver(), resource, "Nur", null);
                            Uri uri = Uri.parse(path);

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_SUBJECT, mItemModel.Title);
                            intent.putExtra(Intent.EXTRA_TEXT, mItemModel.Description);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent,
                                    getResources().getText(R.string.share_with)));
                        }
                    });
        }
    }

    private static class DetailAsyncTaskLoader extends AsyncTaskLoader<ItemModel> {
        private final RevalueApplication mApplication;
        private final SessionPreferences mSessionPreferences;
        private final long mId;

        public DetailAsyncTaskLoader(RevalueApplication application, long id) {
            super(application);
            mApplication = application;
            mSessionPreferences = new SessionPreferences(application);
            mId = id;
        }

        @Override
        public ItemModel loadInBackground() {
            RevalueService service = RevalueServiceGenerator.createService(mSessionPreferences.getToken());
            Call<ItemModel> call = service.GetItem(mApplication.getLocationLatitude(),
                    mApplication.getLocationLongitude(), mId);

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                return null;
            }
        }
    }
}