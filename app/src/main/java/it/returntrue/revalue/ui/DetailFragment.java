package it.returntrue.revalue.ui;

import android.content.Context;
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
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.utilities.MapUtilities;
import retrofit2.Call;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<ItemModel> {
    private final static int LOADER_ITEM = 1;
    private long mId;

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

        // Gets extra data from intent
        mId = getActivity().getIntent().getLongExtra(DetailActivity.EXTRA_ID, 0);

        // Sets option menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Binds controls
        mImageCover = (ImageView)getActivity().findViewById(R.id.image_cover);
        ButterKnife.bind(this, getView());

        // Binds controls
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
        return new DetailAsyncTaskLoader(getContext(), mId);
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

            Circle circle = MapUtilities.getCenteredCircle(map, coordinates, 20000);
            int zoom = MapUtilities.getCircleZoomLevel(circle);

            if (zoom > 0) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoom));
            }
        }
    }

    private void clearDetails() {

    }

    private static class DetailAsyncTaskLoader extends AsyncTaskLoader<ItemModel> {
        private final SessionPreferences mSessionPreferences = new SessionPreferences(getContext());
        private final long mId;

        public DetailAsyncTaskLoader(Context context, long id) {
            super(context);
            mId = id;
        }

        @Override
        public ItemModel loadInBackground() {
            RevalueService service = RevalueServiceGenerator.createService(mSessionPreferences.getToken());
            Call<ItemModel> call = service.GetItem(45.553629, 9.197735, mId);

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                return null;
            }
        }
    }
}