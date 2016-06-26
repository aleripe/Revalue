/*
 * Copyright (C) 2016 Alessandro Riperi
*/

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
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.GetItemsEvent;
import it.returntrue.revalue.events.ViewItemEvent;
import it.returntrue.revalue.imaging.CropCircleTransformation;
import it.returntrue.revalue.ui.base.BaseItemsFragment;
import it.returntrue.revalue.utilities.Constants;
import it.returntrue.revalue.utilities.MapUtilities;

/**
 * Shows a map of items
 * */
@SuppressWarnings({"UnusedParameters", "unused"})
public class MapFragment extends BaseItemsFragment implements GoogleMap.OnInfoWindowClickListener,
    OnMapReadyCallback, GoogleMap.OnCameraChangeListener {
    private SupportMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private final HashMap<Marker, Integer> mMarkerIDs = new HashMap<>();

    public MapFragment() { }

    public static MapFragment newInstance(@Constants.MainMode int itemMode) {
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

        // Binds controls
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
    }

    @Subscribe
    public void onGetItemsSuccess(final GetItemsEvent.OnSuccess onSuccess) {
        if (!mMapFragment.isAdded() || (mGoogleMap == null)) return;

        mGoogleMap.clear();

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
                            if (!mMapFragment.isAdded() || (mGoogleMap == null)) return;

                            final Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(itemModel.Latitude, itemModel.Longitude))
                                    .title(itemModel.Title)
                                    .snippet(getString(R.string.item_location,
                                            itemModel.City, (int) (itemModel.Distance / 1000)))
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                            mMarkerIDs.put(marker, itemModel.Id);
                        }
                    });
        }

        // Restores saved state
        Double mapLatitude = application().getMapLatitude();
        Double mapLongitude = application().getMapLongitude();
        Float mapZoom = application().getMapZoom();

        if (mapLatitude != null && mapLongitude != null && mapZoom != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mapLatitude, mapLongitude))
                    .zoom(mapZoom)
                    .build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        else {
            LatLng position = new LatLng(
                    application().getLocationLatitude(),
                    application().getLocationLongitude());

            Circle circle = MapUtilities.getCenteredCircle(mGoogleMap, position,
                    application().getFilterDistance());

            int zoom = MapUtilities.getCircleZoomLevel(circle);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
        }
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
    public void onInfoWindowClick(Marker marker) {
        BusProvider.bus().post(new ViewItemEvent.OnStart(mMarkerIDs.get(marker)));
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        application().setMapLatitude(cameraPosition.target.latitude);
        application().setMapLongitude(cameraPosition.target.longitude);
        application().setMapZoom(cameraPosition.zoom);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnCameraChangeListener(this);
    }
}