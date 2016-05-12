package it.returntrue.revalue.ui.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.preferences.InterfacePreferences;
import it.returntrue.revalue.preferences.SessionPreferences;
import retrofit2.Call;

public abstract class MainFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<List<ItemModel>>,
        LocationListener {
    protected static final int LOADER_ITEMS = 1;
    protected static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final int FASTEST_INTERVAL = 1000;
    private static final int INTERVAL = FASTEST_INTERVAL * 2;

    protected OnItemClickListener mOnItemClickListener;
    protected Location mLastLocation;

    private SessionPreferences mSessionPreferences;
    private InterfacePreferences mInterfacePreferences;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private ListAsyncTaskLoader mListLoader;

    /** Provides listeners for click events */
    public interface OnItemClickListener {
        void onItemClick(View view, long id);
    }

    public MainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(getContext());
        mInterfacePreferences = new InterfacePreferences(getContext());

        // Sets a location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Create an instance of GoogleAPIClient.
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnItemClickListener = (OnItemClickListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnItemClickListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Binds controls
        ButterKnife.bind(this, getView());

        // Setup the available loader
        getLoaderManager().initLoader(LOADER_ITEMS, null, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        else {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // Stores last location
        setLastLocation(location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                }
                else {
                    Snackbar.make(getView(), "Can't get last location", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public Loader<List<ItemModel>> onCreateLoader(int id, Bundle args) {
        mListLoader = new ListAsyncTaskLoader(getContext(), mSessionPreferences, mInterfacePreferences);
        return mListLoader;
    }

    @Override
    public abstract void onLoadFinished(Loader<List<ItemModel>> loader, List<ItemModel> data);

    @Override
    public abstract void onLoaderReset(Loader<List<ItemModel>> loader);

    private void startLocationUpdates() {
        // Stores last location
        setLastLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void setLastLocation(Location location) {
        mLastLocation = location;

        if (mLastLocation != null) {
            mInterfacePreferences.setLocationLatitude((float) location.getLatitude());
            mInterfacePreferences.setLocationLongitude((float) location.getLongitude());
            mListLoader.onContentChanged();
        }
    }

    private static class ListAsyncTaskLoader extends AsyncTaskLoader<List<ItemModel>> {
        private SessionPreferences mSessionPreferences;
        private InterfacePreferences mInterfacePreferences;

        public ListAsyncTaskLoader(Context context, SessionPreferences sessionPreferences,
                                   InterfacePreferences interfacePreferences) {
            super(context);
            mSessionPreferences = sessionPreferences;
            mInterfacePreferences = interfacePreferences;
        }

        @Override
        public List<ItemModel> loadInBackground() {
            RevalueService service = RevalueServiceGenerator.createService(
                    mSessionPreferences.getToken());
            Call<List<ItemModel>> call = service.GetNearestItems(
                    mInterfacePreferences.getLocationLatitude(),
                    mInterfacePreferences.getLocationLongitude(),
                    mInterfacePreferences.getCurrentPage());

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                return new ArrayList<>();
            }
        }
    }
}