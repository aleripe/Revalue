package it.returntrue.revalue.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.returntrue.revalue.R;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.preferences.SessionPreferences;
import retrofit2.Call;

public class MapFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<ItemModel>> {
    private final static int LOADER_ITEMS = 1;

    private SupportMapFragment mMapFragment;

    public MapFragment() {
    }

    public static Fragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets option menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_map, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Binds controls
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);

        // Initializes loader
        getActivity().getSupportLoaderManager().initLoader(LOADER_ITEMS, null, this).forceLoad();
    }

    @Override
    public Loader<List<ItemModel>> onCreateLoader(int id, Bundle args) {
        return new ListAsyncTaskLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<ItemModel>> loader, List<ItemModel> data) {
        GoogleMap map = mMapFragment.getMap();
        if (map != null) {
            for (ItemModel itemModel : data) {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(itemModel.Latitude, itemModel.Longitude))
                        .title(itemModel.Title));
            }

            Circle circle = map.addCircle(new CircleOptions().center(new LatLng(45.553629, 9.197735)).radius(20000).strokeColor(Color.TRANSPARENT));
            circle.setVisible(true);
            int zoomLevel = getZoomLevel(circle);
            if (zoomLevel > 0) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.553629, 9.197735), zoomLevel));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ItemModel>> loader) {
        GoogleMap map = mMapFragment.getMap();
        if (map != null) {
            map.clear();
        }
    }

    private int getZoomLevel(Circle circle) {
        if (circle != null){
            double radius = circle.getRadius();
            double scale = radius / 500;
            return (int) (16 - Math.log(scale) / Math.log(2));
        }
        return 0;
    }

    private static class ListAsyncTaskLoader extends AsyncTaskLoader<List<ItemModel>> {
        private final SessionPreferences mSessionPreferences = new SessionPreferences(getContext());

        public ListAsyncTaskLoader(Context context) {
            super(context);
        }

        @Override
        public List<ItemModel> loadInBackground() {

            RevalueService service = RevalueServiceGenerator.createService(mSessionPreferences.getToken());
            Call<List<ItemModel>> call = service.GetNearestItems(45.553629, 9.197735, 0);

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                return new ArrayList<>();
            }
        }
    }
}