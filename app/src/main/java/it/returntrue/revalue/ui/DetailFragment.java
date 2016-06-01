package it.returntrue.revalue.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.analytics.Tracker;
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
import it.returntrue.revalue.utilities.NetworkUtilities;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<ItemModel> {
    private static final int LOADER_ITEM = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private int mId;
    private OnSetFabVisibilityListener mSetFabVisibilityListener;
    private RevalueApplication mApplication;
    private Tracker mTracker;
    private SessionPreferences mSessionPreferences;
    private DetailAsyncTaskLoader mDetailLoader;
    private Menu mMenu;
    private SupportMapFragment mMapFragment;
    private ImageView mImageCover;
    private ItemModel mItemModel;

    @Bind(R.id.text_title) public TextView mTextTitle;
    @Bind(R.id.text_location) public TextView mTextLocation;
    @Bind(R.id.text_description) public TextView mTextDescription;

    public DetailFragment() { }

    public static Fragment newInstance() {
        return new DetailFragment();
    }

    public interface OnSetFabVisibilityListener {
        public void onSetChatFab(boolean isOwner);
        public void onSetRevalueFab(boolean isOwner);
        public void onSetRemoveFab(boolean isOwner);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets application context
        mApplication = (RevalueApplication)getActivity().getApplicationContext();

        // Gets analytics tracker
        mTracker = mApplication.getTracker();

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(getContext());

        // Gets extra data from intent
        mId = getActivity().getIntent().getIntExtra(DetailActivity.EXTRA_ID, 0);

        // Sets option menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Binds controls
        mImageCover = (ImageView)getActivity().findViewById(R.id.image_cover);

        // Binds controls
        ButterKnife.bind(this, getView());

        // Gets map fragment reference
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);

        if (NetworkUtilities.checkInternetConnection(getContext())) {
            // Initializes loader and forces it to start
            getLoaderManager().initLoader(LOADER_ITEM, null, this).forceLoad();
        }
        else {
            clearDetails();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mSetFabVisibilityListener = (OnSetFabVisibilityListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement OnSetFabVisibilityListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail, menu);
        mMenu = menu;
        updateMenuItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_favorite:
                addFavorite();
                return true;
            case R.id.action_remove_favorite:
                removeFavorite();
                return true;
            case R.id.action_share:
                requestShare();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<ItemModel> onCreateLoader(int id, Bundle args) {
        mDetailLoader = new DetailAsyncTaskLoader(mApplication, mId);
        return mDetailLoader;
    }

    @Override
    public void onLoadFinished(Loader<ItemModel> loader, ItemModel data) {
        setDetails(data);
    }

    @Override
    public void onLoaderReset(Loader<ItemModel> loader) {
        clearDetails();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    share();
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.could_not_share_item),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void setDetails(ItemModel itemModel) {
        if (itemModel != null) {
            mItemModel = itemModel;

            Glide.with(getContext())
                    .load(itemModel.PictureUrl)
                    .into(mImageCover);

            // Sets toolbar title
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setTitle(itemModel.Title);

            // Shows only appropriate favorite button
            updateMenuItems();

            // Sets main data
            mTextTitle.setText(itemModel.Title);
            mTextLocation.setText(itemModel.City + " / " + (int) (itemModel.Distance / 1000) + " km");
            mTextDescription.setText(itemModel.Description);

            // Shows position on map if available
            if (itemModel.ShowOnMap) {
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
            } else {
                mMapFragment.getView().setVisibility(View.GONE);
            }

            // Shows appropriate FABs
            mSetFabVisibilityListener.onSetChatFab(mItemModel.IsOwned);
            mSetFabVisibilityListener.onSetRevalueFab(mItemModel.IsOwned);
            mSetFabVisibilityListener.onSetRemoveFab(mItemModel.IsOwned);
        }
    }

    private void clearDetails() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    public void addFavorite() {
        if (!NetworkUtilities.checkInternetConnection(getContext())) {
            Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
            return;
        }

        if (mItemModel != null) {
            RevalueService service = RevalueServiceGenerator.createService(
                    mSessionPreferences.getToken());
            Call<Void> call = service.AddFavorite(mItemModel.Id);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Response is OK, reload data
                    if (response.isSuccessful()) {
                        Toast.makeText(DetailFragment.this.getContext(),
                                getString(R.string.favorite_item_added), Toast.LENGTH_LONG).show();

                        if (mDetailLoader != null) {
                            mDetailLoader.onContentChanged();
                        }
                    }
                    else {
                        // Parse and display error
                        String error = NetworkUtilities.parseError(DetailFragment.this.getContext(), response);
                        Toast.makeText(DetailFragment.this.getContext(), error, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(DetailFragment.this.getContext(),
                            getString(R.string.could_not_add_favorite_item), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void removeFavorite() {
        if (!NetworkUtilities.checkInternetConnection(getContext())) {
            Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
            return;
        }

        if (mItemModel != null) {
            RevalueService service = RevalueServiceGenerator.createService(
                    mSessionPreferences.getToken());
            Call<Void> call = service.RemoveFavorite(mItemModel.Id);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Response is OK, reload data
                    if (response.isSuccessful()) {
                        Toast.makeText(DetailFragment.this.getContext(),
                                getString(R.string.favorite_item_removed), Toast.LENGTH_LONG).show();

                        if (mDetailLoader != null) {
                            mDetailLoader.onContentChanged();
                        }
                    }
                    else {
                        // Parse and display error
                        String error = NetworkUtilities.parseError(DetailFragment.this.getContext(), response);
                        Toast.makeText(DetailFragment.this.getContext(), error, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(DetailFragment.this.getContext(),
                            getString(R.string.could_not_remove_favorite_item), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void goToChatActivity() {
        if (mItemModel != null) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_ID, mItemModel.Id);
            intent.putExtra(ChatActivity.EXTRA_USER_ID, mItemModel.UserId);
            intent.putExtra(ChatActivity.EXTRA_USER_ALIAS, mItemModel.UserAlias);
            startActivity(intent);
        }
    }

    public void setItemAsRevalued() {
        if (!NetworkUtilities.checkInternetConnection(getContext())) {
            Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
            return;
        }

        if (mItemModel != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.revalue_item))
                    .setMessage(getString(R.string.confirm_revalue_item))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            RevalueService service = RevalueServiceGenerator.createService(mSessionPreferences.getToken());
                            Call<Void> call = service.SetItemAsRevalued(mId);
                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(DetailFragment.this.getContext(),
                                            getString(R.string.could_not_revalue_item),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }})
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    public void setItemAsRemoved() {
        if (!NetworkUtilities.checkInternetConnection(getContext())) {
            Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
            return;
        }

        if (mItemModel != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.remove_item))
                    .setMessage(getString(R.string.confirm_remove_item))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            RevalueService service = RevalueServiceGenerator.createService(mSessionPreferences.getToken());
                            Call<Void> call = service.SetItemAsRemoved(mId);
                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(DetailFragment.this.getContext(),
                                            getString(R.string.could_not_remove_item),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }})
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    private void updateMenuItems() {
        if (mMenu != null && mItemModel != null) {
            mMenu.findItem(R.id.action_add_favorite).setVisible(!mItemModel.IsOwned && !mItemModel.IsFavorite);
            mMenu.findItem(R.id.action_remove_favorite).setVisible(!mItemModel.IsOwned && mItemModel.IsFavorite);
        }
    }

    private void requestShare() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else {
            share();
        }
    }

    private void share() {
        if (mItemModel != null) {
            Glide.with(getContext())
                    .load(mItemModel.PictureUrl)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            View view = new View(getContext());
                            view.draw(new Canvas(resource));
                            String path = MediaStore.Images.Media.insertImage(
                                    getActivity().getContentResolver(), resource, "Nur", null);
                            Uri uri = Uri.parse(path);

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_SUBJECT, mItemModel.Title);
                            intent.putExtra(Intent.EXTRA_TEXT, mItemModel.Description);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent,
                                    getResources().getText(R.string.share_with)));
                        }
                    });
        }
    }

    private static class DetailAsyncTaskLoader extends AsyncTaskLoader<ItemModel> {
        private final RevalueApplication mApplication;
        private final SessionPreferences mSessionPreferences;
        private final int mId;

        public DetailAsyncTaskLoader(RevalueApplication application, int id) {
            super(application);
            mApplication = application;
            mSessionPreferences = new SessionPreferences(application);
            mId = id;
        }

        @Override
        public ItemModel loadInBackground() {
            RevalueService service = RevalueServiceGenerator.createService(mSessionPreferences.getToken());
            Call<ItemModel> call = service.GetItem(mId,
                    mApplication.getLocationLatitude(),
                    mApplication.getLocationLongitude());

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                Log.e(DetailFragment.class.getSimpleName(), e.toString());
                return null;
            }
        }
    }
}