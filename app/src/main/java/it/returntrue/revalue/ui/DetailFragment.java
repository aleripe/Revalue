package it.returntrue.revalue.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<ItemModel> {
    private final static int LOADER_ITEM = 1;
    private long mId;

    private RevalueApplication mApplication;
    private SupportMapFragment mMapFragment;
    private ImageView mImageCover;

    @Bind(R.id.text_title) public TextView mTextTitle;
    @Bind(R.id.text_location) public TextView mTextLocation;
    @Bind(R.id.text_description) public TextView mTextDescription;

    public DetailFragment() {
    }

    public static Fragment newInstance() {
        return new DetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets application context
        mApplication = (RevalueApplication)getActivity().getApplicationContext();

        // Gets extra data from intent
        mId = getActivity().getIntent().getLongExtra(DetailActivity.EXTRA_ID, 0);

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
    }

    @Override
    public Loader<ItemModel> onCreateLoader(int id, Bundle args) {
        return new DetailAsyncTaskLoader(mApplication, mId);
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
        Glide.with(getContext())
                .load(itemModel.PictureUrl)
                .into(mImageCover);

        // Sets toolbar title
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setTitle(itemModel.Title);

        mTextTitle.setText(itemModel.Title);
        mTextLocation.setText(itemModel.City + " / " +
                (int)(itemModel.Distance / 1000) + " km");
        mTextDescription.setText(itemModel.Description);

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
    }

    private void clearDetails() {

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