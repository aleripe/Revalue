package it.returntrue.revalue.ui.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.preferences.SessionPreferences;
import retrofit2.Call;

public abstract class MainFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<List<ItemModel>>,
        LocationListener {
    protected static final int LOADER_ITEMS = 1;
    protected static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    protected OnItemClickListener mOnItemClickListener;
    protected Location mLastLocation;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    /** Provides listeners for click events */
    public interface OnItemClickListener {
        void onItemClick(View view, long id);
    }

    public MainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets a location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

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
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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
            getLastLocation();
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
        mLastLocation = location;

        // Loads items
        loadItems();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                }
                else {
                    Toast.makeText(getContext(), "Can't get last location", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public Loader<List<ItemModel>> onCreateLoader(int id, Bundle args) {
        return new ListAsyncTaskLoader(getContext(), mLastLocation);
    }

    @Override
    public abstract void onLoadFinished(Loader<List<ItemModel>> loader, List<ItemModel> data);

    @Override
    public abstract void onLoaderReset(Loader<List<ItemModel>> loader);

    private void loadItems() {
        if (mLocationRequest != null) {
            // Initializes loader
            getActivity().getSupportLoaderManager().initLoader(LOADER_ITEMS, null, this).forceLoad();
        }
    }

    private void getLastLocation() {
        if (mLastLocation == null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            loadItems();
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private static class ListAsyncTaskLoader extends AsyncTaskLoader<List<ItemModel>> {
        private final SessionPreferences mSessionPreferences = new SessionPreferences(getContext());
        private final Location mLastLocation;

        public ListAsyncTaskLoader(Context context, Location lastLocation) {
            super(context);
            mLastLocation = lastLocation;
        }

        @Override
        public List<ItemModel> loadInBackground() {
            RevalueService service = RevalueServiceGenerator.createService(
                    mSessionPreferences.getToken());
            Call<List<ItemModel>> call = service.GetNearestItems(
                    mLastLocation.getLatitude(), mLastLocation.getLongitude(), 0);

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                return new ArrayList<>();
            }
        }
    }
}
