package it.returntrue.revalue.ui;

import android.Manifest;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.events.AddFavoriteItemEvent;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.GetItemEvent;
import it.returntrue.revalue.events.RemoveFavoriteItemEvent;
import it.returntrue.revalue.events.SetItemAsRemovedEvent;
import it.returntrue.revalue.events.SetItemAsRevaluedEvent;
import it.returntrue.revalue.ui.base.BaseFragment;
import it.returntrue.revalue.utilities.MapUtilities;
import it.returntrue.revalue.utilities.NetworkUtilities;

public class DetailFragment extends BaseFragment {
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private int mId;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gets extra data from intent
        mId = getActivity().getIntent().getIntExtra(DetailActivity.EXTRA_ID, 0);
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

        // Gets item details
        getDetails();
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
            case R.id.action_set_revalued:
                setItemAsRevalued();
                return true;
            case R.id.action_set_removed:
                setItemAsRemoved();
                return true;
            case R.id.action_share:
                requestShare();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Subscribe
    public void onGetItemSuccess(GetItemEvent.OnSuccess onSuccess) {
        setDetails(onSuccess.getItem());
    }

    @Subscribe
    public void onGetItemFailure(GetItemEvent.OnFailure onFailure) {
        Toast.makeText(getContext(), R.string.could_not_get_item, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onAddFavoriteItemSuccess(AddFavoriteItemEvent.OnSuccess onSuccess) {
        Toast.makeText(getContext(), R.string.favorite_item_added, Toast.LENGTH_LONG).show();
        getDetails();
    }

    @Subscribe
    public void onAddFavoriteItemFailure(AddFavoriteItemEvent.OnFailure onFailure) {
        Toast.makeText(getContext(), R.string.could_not_add_favorite_item, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onRemoveFavoriteItemSuccess(RemoveFavoriteItemEvent.OnSuccess onSuccess) {
        Toast.makeText(getContext(), R.string.favorite_item_removed, Toast.LENGTH_LONG).show();
        getDetails();
    }

    @Subscribe
    public void onRemoveFavoriteItemFailure(RemoveFavoriteItemEvent.OnFailure onFailure) {
        Toast.makeText(getContext(), R.string.could_not_remove_favorite_item, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onSetItemAsRevaluedSuccess(SetItemAsRevaluedEvent.OnSuccess onSuccess) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Subscribe
    public void onSetItemAsRevaluedFailure(SetItemAsRemovedEvent.OnFailure onFailure) {
        Toast.makeText(getContext(), R.string.could_not_revalue_item, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onSetItemAsRemovedSuccess(SetItemAsRemovedEvent.OnSuccess onSuccess) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Subscribe
    public void onSetItemAsRemovedFailure(SetItemAsRemovedEvent.OnFailure onFailure) {
        Toast.makeText(getContext(), R.string.could_not_remove_item, Toast.LENGTH_LONG).show();
    }

    private void getDetails() {
        if (NetworkUtilities.checkInternetConnection(getContext())) {
            // Gets item details
            BusProvider.bus().post(new GetItemEvent.OnStart(mId,
                    mApplication.getLocationLatitude(),
                    mApplication.getLocationLongitude()));
        }
        else {
            clearDetails();
        }
    }

    private void setDetails(ItemModel itemModel) {
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
    }

    private void clearDetails() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    public void addFavorite() {
        if (NetworkUtilities.checkInternetConnection(getContext())) {
            // Adds favorite
            BusProvider.bus().post(new AddFavoriteItemEvent.OnStart(mId));
        } else {
            Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
        }
    }

    public void removeFavorite() {
        if (NetworkUtilities.checkInternetConnection(getContext())) {
            // Removes favorite
            BusProvider.bus().post(new RemoveFavoriteItemEvent.OnStart(mId));
        } else {
            Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
        }
    }

    public void goToChatActivity() {
        if (mItemModel != null) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_ITEM_ID, mItemModel.Id);
            intent.putExtra(ChatActivity.EXTRA_USER_ID, mItemModel.UserId);
            intent.putExtra(ChatActivity.EXTRA_USER_ALIAS, mItemModel.UserAlias);
            intent.putExtra(ChatActivity.EXTRA_IS_OWNED, mItemModel.IsOwned);
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
                            BusProvider.bus().post(new SetItemAsRevaluedEvent.OnStart(mId));
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
                            BusProvider.bus().post(new SetItemAsRemovedEvent.OnStart(mId));
                        }})
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    private void updateMenuItems() {
        if (mMenu != null && mItemModel != null) {
            mMenu.findItem(R.id.action_add_favorite).setVisible(!mItemModel.IsOwned && !mItemModel.IsFavorite);
            mMenu.findItem(R.id.action_remove_favorite).setVisible(!mItemModel.IsOwned && mItemModel.IsFavorite);
            mMenu.findItem(R.id.action_set_revalued).setVisible(mItemModel.IsOwned);
            mMenu.findItem(R.id.action_set_removed).setVisible(mItemModel.IsOwned);
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
}