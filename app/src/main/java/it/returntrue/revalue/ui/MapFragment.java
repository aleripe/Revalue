package it.returntrue.revalue.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

import it.returntrue.revalue.R;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.ui.base.MainFragment;
import it.returntrue.revalue.utilities.MapUtilities;

public class MapFragment extends MainFragment implements GoogleMap.OnInfoWindowClickListener {
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ZOOM = "zoom";

    private RevalueApplication mApplication;
    private SupportMapFragment mMapFragment;
    private HashMap<Marker, Integer> mMarkerIDs = new HashMap<>();
    private double mLatitude;
    private double mLongitude;
    private float mZoom;

    public MapFragment() {
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets application context
        mApplication = (RevalueApplication)getActivity().getApplicationContext();

        // Sets option menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_map, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Restores saved state
        if (savedInstanceState != null) {
            mLatitude = savedInstanceState.getDouble(KEY_LATITUDE);
            mLongitude = savedInstanceState.getDouble(KEY_LONGITUDE);
            mZoom = savedInstanceState.getFloat(KEY_ZOOM);
        }

        // Binds controls
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
    }

    @Override
    public void onLoadFinished(Loader<List<ItemModel>> loader, List<ItemModel> data) {
        GoogleMap map = mMapFragment.getMap();

        if (map != null) {
            map.setOnInfoWindowClickListener(this);

            for (ItemModel itemModel : data) {
                    final Marker marker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(itemModel.Latitude, itemModel.Longitude))
                            .title(itemModel.Title)
                            .snippet(itemModel.City + " / " +
                                    (int)(itemModel.Distance / 1000) + " km"));

                    Glide.with(getContext())
                            .load(itemModel.PictureUrl)
                            .asBitmap()
                            .override(100, 100)
                            .centerCrop()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                    BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
                                    marker.setIcon(icon);
                                }
                            });

                    mMarkerIDs.put(marker, itemModel.Id);
            }

            LatLng position = new LatLng(
                    mApplication.getLocationLatitude(), mApplication.getLocationLongitude());

            if (mLatitude != 0 && mLongitude != 0) {
                position = new LatLng(mLatitude, mLongitude);
            }

            Circle circle = MapUtilities.getCenteredCircle(map, position,
                    mApplication.getFilterDistance());

            if (mZoom == 0) {
                mZoom = MapUtilities.getCircleZoomLevel(circle);
            }

            if (mZoom > 0) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, mZoom));
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        GoogleMap map = mMapFragment.getMap();

        if (map != null) {
            CameraPosition camera = map.getCameraPosition();

            if (camera != null) {
                outState.putDouble(KEY_LATITUDE, camera.target.latitude);
                outState.putDouble(KEY_LONGITUDE, camera.target.longitude);
                outState.putFloat(KEY_ZOOM, camera.zoom);
            }
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, mMarkerIDs.get(marker));
        }
    }
}