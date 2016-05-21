package it.returntrue.revalue.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
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

    public MapFragment() { }

    public static MapFragment newInstance(@MainFragment.ItemMode int itemMode) {
        MapFragment fragment = new MapFragment();
        fragment.ItemMode = itemMode;
        return fragment;
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
        final GoogleMap map = mMapFragment.getMap();

        if (map != null) {
            map.clear();
            map.setOnInfoWindowClickListener(this);

            for (final ItemModel itemModel : data) {
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
                                    final Marker marker = map.addMarker(new MarkerOptions()
                                            .position(new LatLng(itemModel.Latitude, itemModel.Longitude))
                                            .title(itemModel.Title)
                                            .snippet(itemModel.City + " / " +
                                                    (int)(itemModel.Distance / 1000) + " km")
                                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                                    mMarkerIDs.put(marker, itemModel.Id);
                                }
                            });
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
        if (OnItemClickListener != null) {
            OnItemClickListener.onItemClick(null, mMarkerIDs.get(marker));
        }
    }

    public class CropCircleTransformation implements Transformation<Bitmap> {

        private BitmapPool mBitmapPool;

        public CropCircleTransformation(Context context) {
            this(Glide.get(context).getBitmapPool());
        }

        public CropCircleTransformation(BitmapPool pool) {
            this.mBitmapPool = pool;
        }

        @Override
        public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
            Bitmap source = resource.get();
            int size = Math.min(source.getWidth(), source.getHeight());

            int width = (source.getWidth() - size) / 2;
            int height = (source.getHeight() - size) / 2;

            Bitmap bitmap = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(source,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);

            if (width != 0 || height != 0) {
                Matrix matrix = new Matrix();
                matrix.setTranslate(-width, -height);
                shader.setLocalMatrix(matrix);
            }
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            return BitmapResource.obtain(bitmap, mBitmapPool);
        }

        @Override public String getId() {
            return "CropCircleTransformation()";
        }
    }
}