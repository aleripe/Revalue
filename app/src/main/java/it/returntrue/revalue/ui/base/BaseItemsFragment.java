package it.returntrue.revalue.ui.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.GetItemsEvent;
import it.returntrue.revalue.utilities.Constants;
import it.returntrue.revalue.utilities.NetworkUtilities;

public abstract class BaseItemsFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final int FASTEST_INTERVAL = 1000;
    private static final int INTERVAL = FASTEST_INTERVAL * 2;

    protected @Constants.ItemMode int mItemMode;
    protected OnItemClickListener OnItemClickListener;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    /** Provides listeners for click events */
    public interface OnItemClickListener {
        void onAddFavoriteClick(int id);

        void onRemoveFavoriteClick(int id);

        void onItemClick(int id);
    }

    public abstract void setStatus(String text);

    public BaseItemsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets a location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Create an bus of GoogleAPIClient.
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
            OnItemClickListener = (OnItemClickListener) context;
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

        // Stops listening to location updates
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Starts listening to location updates
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
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
                } else {
                    setStatus(getString(R.string.no_access_location_permission));
                }
            }
        }
    }

    public void updateItems(@Constants.ItemMode Integer itemMode) {
        if (itemMode != null) {
            mItemMode = itemMode;
        }

        loadItems();
    }

    private void loadItems() {
        if (isAdded()) {
            if (mLastLocation == null) {
                setStatus(getString(R.string.waiting_gps_fix));
            } else if (!NetworkUtilities.checkInternetConnection(getContext())) {
                setStatus(getString(R.string.check_connection));
            } else {
                BusProvider.bus().post(new GetItemsEvent.OnStart(mItemMode,
                        application().getLocationLatitude(),
                        application().getLocationLongitude(),
                        application().getFilterTitle(),
                        application().getFilterCategory(),
                        application().getFilterDistance()));
            }
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            setLastLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void setLastLocation(Location location) {
        if (location != null) {
            // Stores location and stops future updates
            mLastLocation = location;
            application().setLocationLatitude(location.getLatitude());
            application().setLocationLongitude(location.getLongitude());
            stopLocationUpdates();

            // Updates item list
            updateItems(null);
        }
    }
}