/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.CategoryModel;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.GetCategoriesEvent;
import it.returntrue.revalue.events.InsertItemEvent;
import it.returntrue.revalue.ui.base.BaseFragment;
import it.returntrue.revalue.utilities.CategoryUtilities;
import it.returntrue.revalue.utilities.MapUtilities;
import it.returntrue.revalue.utilities.NetworkUtilities;

/**
 * Shows new item form
 * */
@SuppressWarnings({"SameParameterValue", "UnusedParameters", "WeakerAccess", "unused", "BooleanMethodIsAlwaysInverted"})
public class InsertFragment extends BaseFragment {
    private static final int ACTION_CAMERA = 1;
    private static final int ACTION_GALLERY = 2;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final String EXTRA_PICTURE = "picture";
    private static final int PICTURE_SIZE = 1000;

    private ProgressDialog mProgressDialog;
    private List<CategoryModel> mCategories;
    private ArrayAdapter<CategoryModel> mAdapter;
    private SupportMapFragment mMapFragment;
    private Bitmap mPicture;
    private String mPictureData;
    private String mPicturePath;

    @Bind(R.id.label_title) TextView mLabelTitle;
    @Bind(R.id.text_title) EditText mTextTitle;
    @Bind(R.id.required_title) TextView mRequiredTitle;
    @Bind(R.id.text_description) EditText mTextDescription;
    @Bind(R.id.required_description) TextView mRequiredDescription;
    @Bind(R.id.spinner_category) Spinner mSpinnerCategory;
    @Bind(R.id.required_category) TextView mRequiredCategory;
    @Bind(R.id.button_choose_picture) ImageButton mButtonChoosePicture;
    @Bind(R.id.required_picture) TextView mRequiredPicture;
    @Bind(R.id.switch_location) Switch mSwitchLocation;

    public InsertFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert, container, false);

        // Binds controls
        ButterKnife.bind(this, view);

        // Loads categories
        loadCategories();

        // Gets map reference
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);

        // Sets change picture button event handler to select picture
        mButtonChoosePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });

        // Sets location change listener to display position on map
        mSwitchLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                mMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.clear();

                        if (isChecked) {
                            LatLng coordinates = new LatLng(application().getLocationLatitude(),
                                    application().getLocationLongitude());
                            googleMap.addMarker(new MarkerOptions().position(coordinates));

                            Circle circle = MapUtilities.getCenteredCircle(googleMap, coordinates, 5);
                            int zoom = MapUtilities.getCircleZoomLevel(circle);

                            if (zoom > 0) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoom));
                            }
                        }
                    }
                });
            }
        });

        // Restores previously saved picture
        if (savedInstanceState != null) {
            mPicture = savedInstanceState.getParcelable(EXTRA_PICTURE);
            previewPicture();
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_insert, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                sendItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ACTION_CAMERA) {
                actionCameraResult(data);
            }
            if (requestCode == ACTION_GALLERY) {
                actionGalleryResult(data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchGalleryIntent();
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.could_not_read_pictures),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_PICTURE, mPicture);
    }

    @Subscribe
    public void onGetCategoriesSuccess(GetCategoriesEvent.OnSuccess onSuccess) {
        mCategories = onSuccess.getCategories();

        // Creates adapter and inserts empty default value
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mAdapter.clear();
        mAdapter.addAll(mCategories);
        mAdapter.insert(createEmptyCategoryModel(), 0);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Sets adapter
        mSpinnerCategory.setAdapter(mAdapter);
    }

    @Subscribe
    public void onGetCategoriesFailure(GetCategoriesEvent.OnFailure onFailure) {
        Toast.makeText(getContext(), R.string.could_not_get_categories, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onInsertItemSuccess(InsertItemEvent.OnSuccess onSuccess) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();

        hideProgress();
        Toast.makeText(getContext(), R.string.item_saved, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onInsertItemFailure(InsertItemEvent.OnFailure onFailure) {
        hideProgress();
        Toast.makeText(getContext(), R.string.could_not_save_item, Toast.LENGTH_LONG).show();
    }

    private void loadCategories() {
        if (NetworkUtilities.checkInternetConnection(getContext())) {
            BusProvider.bus().post(new GetCategoriesEvent.OnStart());
        }
        else {
            Toast.makeText(getContext(), R.string.check_connection, Toast.LENGTH_LONG).show();
        }
    }

    private void actionCameraResult(Intent data) {
        mPicture = resizeImage(BitmapFactory.decodeFile(mPicturePath), PICTURE_SIZE);
        previewPicture();
    }

    private void actionGalleryResult(Intent data) {
        if (data != null) {
            try {
                mPicture = resizeImage(MediaStore.Images.Media.getBitmap(
                        getContext().getContentResolver(), data.getData()), PICTURE_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        previewPicture();
    }

    private void previewPicture() {
        if (mPicture != null) {
            new BitmapWorkerTask(mButtonChoosePicture).execute(mPicture);
        }
    }

    private Bitmap resizeImage(Bitmap bitmap, int newWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = ((float)newWidth) / width;
        int newHeight = (int)(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

    private void selectPicture() {
        final CharSequence[] items = {
                getString(R.string.take_picture),
                getString(R.string.choose_from_library),
                getString(R.string.cancel)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.add_picture));

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position) {
                    case 0:
                        dispatchCameraIntent();
                        break;
                    case 1:
                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                        else {
                            dispatchGalleryIntent();
                        }
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }

    private void dispatchCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            File file = null;

            try {
                file = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        getString(R.string.file_provider_authority), file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, ACTION_CAMERA);
            }
        }
    }

    private void dispatchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ACTION_GALLERY);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.UK).format(new Date());
        String imageFileName = "JPEG" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mPicturePath = image.getAbsolutePath();
        return image;
    }

    private void sendItem() {
        showProgress();

        if (!NetworkUtilities.checkInternetConnection(getContext())) {
            Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
            hideProgress();
            return;
        }

        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // Creates item model
                ItemModel itemModel = new ItemModel();
                itemModel.Title = mTextTitle.getText().toString();
                itemModel.Description = mTextDescription.getText().toString();
                itemModel.ShowOnMap = mSwitchLocation.isChecked();
                itemModel.CategoryId = CategoryUtilities.getCategoryId(mCategories, mSpinnerCategory.getSelectedItemPosition());
                itemModel.PictureData = mPictureData;
                setLocationOnItem(itemModel, googleMap);

                if (!validateItem(itemModel)) {
                    Toast.makeText(getContext(), R.string.item_validation_failed, Toast.LENGTH_LONG).show();
                    hideProgress();
                    return;
                }

                // Saves item
                BusProvider.bus().post(new InsertItemEvent.OnStart(itemModel));
            }
        });
    }

    private void setLocationOnItem(final ItemModel itemModel, final GoogleMap googleMap) {
        if (mSwitchLocation.isChecked()) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

            itemModel.Latitude = googleMap.getCameraPosition().target.latitude;
            itemModel.Longitude = googleMap.getCameraPosition().target.longitude;

            try {
                List<Address> addresses = geocoder.getFromLocation(
                        itemModel.Latitude, itemModel.Longitude, 1);

                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    itemModel.City = address.getLocality();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showProgress() {
        mProgressDialog = ProgressDialog.show(getContext(),
                getString(R.string.loading), getString(R.string.uploading_data));
    }

    private void hideProgress() {
        if (mProgressDialog != null) mProgressDialog.dismiss();
    }

    private boolean validateItem(ItemModel itemModel) {
        boolean isValid = validate(mRequiredTitle, TextUtils.isEmpty(itemModel.Title));
        isValid &= validate(mRequiredDescription, TextUtils.isEmpty(itemModel.Description));
        isValid &= validate(mRequiredCategory, itemModel.CategoryId < 1);
        isValid &= validate(mRequiredPicture, itemModel.PictureData == null);
        return isValid;
    }

    private boolean validate(TextView textView, boolean condition) {
        if (condition) {
            textView.setVisibility(View.VISIBLE);
            return false;
        }
        else {
            textView.setVisibility(View.GONE);
            return true;
        }
    }

    private CategoryModel createEmptyCategoryModel() {
        CategoryModel category = new CategoryModel();
        category.Id = 0;
        category.Name = getString(R.string.hint_category);
        return category;
    }

    class BitmapWorkerTask extends AsyncTask<Bitmap, Void, Bitmap> {
        private final WeakReference<ImageView> mImageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            mImageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mPicture.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Stores resulting conversion data
            InsertFragment.this.mPictureData = Base64.encodeToString(byteArray, Base64.DEFAULT);

            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                final ImageView imageView = mImageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}