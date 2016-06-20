package it.returntrue.revalue.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.HashMap;

import it.returntrue.revalue.R;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.events.GetItemsEvent;
import it.returntrue.revalue.imaging.CropCircleTransformation;
import it.returntrue.revalue.ui.base.BaseItemsFragment;
import it.returntrue.revalue.utilities.Constants;
import it.returntrue.revalue.utilities.MapUtilities;

@SuppressWarnings({"UnusedParameters", "unused"})
public class MapFragment extends BaseItemsFragment implements GoogleMap.OnInfoWindowClickListener {
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ZOOM = "zoom";

    private SupportMapFragment mMapFragment;
    private final HashMap<Marker, Integer> mMarkerIDs = new HashMap<>();
    private double mLatitude;
    private double mLongitude;
    private float mZoom;

    public MapFragment() { }

    public static MapFragment newInstance(@Constants.ItemMode int itemMode) {
        MapFragment fragment = new MapFragment();
        fragment.mItemMode = itemMode;
        return fragment;
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

    @Subscribe
    public void onGetItemsSuccess(final GetItemsEvent.OnSuccess onSuccess) {
        if (!mMapFragment.isAdded()) return;

        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.clear();
                googleMap.setOnInfoWindowClickListener(MapFragment.this);

                for (final ItemModel itemModel : onSuccess.getItems()) {
                    if (!itemModel.ShowOnMap) continue;

                    Glide.with(getContext())
                            .load(itemModel.MarkerUrl)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .transform(new CropCircleTransformation(getContext()))
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                    if (!mMapFragment.isAdded()) return;

                                    final Marker marker = googleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(itemModel.Latitude, itemModel.Longitude))
                                            .title(itemModel.Title)
                                            .snippet(getString(R.string.item_location,
                                                    itemModel.City, (int) (itemModel.Distance / 1000)))
                                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                                    mMarkerIDs.put(marker, itemModel.Id);
                                }
                            });
                }

                LatLng position = new LatLng(
                        application().getLocationLatitude(), application().getLocationLongitude());

                if (mLatitude != 0 && mLongitude != 0) {
                    position = new LatLng(mLatitude, mLongitude);
                }

                Circle circle = MapUtilities.getCenteredCircle(googleMap, position,
                        application().getFilterDistance());

                if (mZoom == 0) {
                    mZoom = MapUtilities.getCircleZoomLevel(circle);
                }

                if (mZoom > 0) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, mZoom));
                }
            }
        });
    }

    @Subscribe
    public void onGetItemsFailure(GetItemsEvent.OnFailure onFailure) {
        setStatus(getString(R.string.could_not_get_items));
    }

    @Override
    public void setStatus(String text) {
        // No status label on this view
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                CameraPosition camera = googleMap.getCameraPosition();

                if (camera != null) {
                    outState.putDouble(KEY_LATITUDE, camera.target.latitude);
                    outState.putDouble(KEY_LONGITUDE, camera.target.longitude);
                    outState.putFloat(KEY_ZOOM, camera.zoom);
                }
            }
        });

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (OnItemClickListener != null) {
            OnItemClickListener.onItemClick(mMarkerIDs.get(marker));
        }
    }
}