package it.returntrue.revalue.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.api.CategoryModel;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.utilities.MapUtilities;
import it.returntrue.revalue.utilities.NetworkUtilities;

public class InsertFragment extends Fragment {
    private static final int ACTION_CAMERA = 1;
    private static final int ACTION_GALLERY = 2;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private RevalueApplication mApplication;
    private SessionPreferences mSessionPreferences;
    private ArrayAdapter<CategoryModel> mAdapter;
    private SupportMapFragment mMapFragment;
    private Bitmap mPicture;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets option menu
        setHasOptionsMenu(true);

        // Sets application context
        mApplication = (RevalueApplication)getActivity().getApplicationContext();

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(getContext());

        // Creates adapter and inserts empty default value
        mAdapter = new ArrayAdapter<CategoryModel>(
                getContext(), android.R.layout.simple_list_item_1);
        mAdapter.clear();
        mAdapter.addAll(mApplication.getCategories());
        mAdapter.insert(createEmptyCategoryModel(), 0);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert, container, false);

        // Binds controls
        ButterKnife.bind(this, view);

        // Gets map reference
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);

        // Sets adapter
        mSpinnerCategory.setAdapter(mAdapter);

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
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GoogleMap map = mMapFragment.getMap();
                if (map != null) {
                    map.clear();

                    if (isChecked) {
                        LatLng coordinates = new LatLng(mApplication.getLocationLatitude(),
                                mApplication.getLocationLongitude());
                        map.addMarker(new MarkerOptions().position(coordinates));

                        Circle circle = MapUtilities.getCenteredCircle(map, coordinates, 5);
                        int zoom = MapUtilities.getCircleZoomLevel(circle);

                        if (zoom > 0) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoom));
                        }
                    }
                }
            }
        });

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

    private void actionCameraResult(Intent data) {
        mPicture = resizeImage((Bitmap)data.getExtras().get("data"), 400);

        if (mPicture != null) {
            mButtonChoosePicture.setImageBitmap(mPicture);
        }
    }

    private Bitmap resizeImage(Bitmap bitmap, int newWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = ((float)newWidth) / width;
        int newHeight = (int)(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

    private void actionGalleryResult(Intent data) {
        if (data != null) {
            try {
                mPicture = resizeImage(MediaStore.Images.Media.getBitmap(
                        getContext().getContentResolver(), data.getData()), 400);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mPicture != null) {
            mButtonChoosePicture.setImageBitmap(mPicture);
        }
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
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(R.string.take_picture)) {
                    dispatchCameraIntent();
                } else if (items[item].equals(R.string.choose_from_library)) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                                PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                    else {
                        dispatchGalleryIntent();
                    }
                } else if (items[item].equals(R.string.cancel)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void dispatchCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, ACTION_CAMERA);
    }

    private void dispatchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ACTION_GALLERY);
    }

    private void sendItem() {
        if (!NetworkUtilities.checkInternetConnection(getContext())) {
            Toast.makeText(getContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
            return;
        }

        String text = mTextTitle.getText().toString();
        String description = mTextDescription.getText().toString();
        Double latitude = null;
        Double longitude = null;
        String city = null;
        Boolean showOnMap = mSwitchLocation.isChecked();
        Integer categoryId = mApplication.getCategoryId(mSpinnerCategory.getSelectedItemPosition());
        String token = mSessionPreferences.getToken();

        if (!validateItem(text, description, categoryId, mPicture)) {
            Toast.makeText(getContext(), R.string.item_validation_failed, Toast.LENGTH_LONG).show();
            return;
        }

        if (mSwitchLocation.isChecked() && mMapFragment.getMap() != null) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

            latitude = mMapFragment.getMap().getCameraPosition().target.latitude;
            longitude = mMapFragment.getMap().getCameraPosition().target.longitude;

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null || addresses.size() > 0) {
                    Address address = addresses.get(0);
                    city = address.getLocality();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        new SaveItemAsyncTask(text, description, latitude, longitude,
                city, showOnMap,categoryId, mPicture, token).execute();
    }

    private boolean validateItem(String title, String description,
                                 Integer categoryId, Bitmap picture) {
        boolean isValid = true;
        isValid &= validate(mRequiredTitle, TextUtils.isEmpty(title));
        isValid &= validate(mRequiredDescription, TextUtils.isEmpty(description));
        isValid &= validate(mRequiredCategory, categoryId == null);
        isValid &= validate(mRequiredPicture, mPicture == null);
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

    class SaveItemAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final String mText;
        private final String mDescription;
        private final Double mLatitude;
        private final Double mLongitude;
        private final String mCity;
        private final Boolean mShowOnMap;
        private final Integer mCategoryId;
        private final Bitmap mPicture;
        private final String mToken;

        public SaveItemAsyncTask(String text, String description, Double latitude,
                                 Double longitude, String city, Boolean showOnMap,
                                 Integer categoryId, Bitmap picture, String token) {
            mText = text;
            mDescription = description;
            mLatitude = latitude;
            mLongitude = longitude;
            mCity = city;
            mShowOnMap = showOnMap;
            mCategoryId = categoryId;
            mPicture = picture;
            mToken = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ItemModel item = new ItemModel();
            item.Title = mText;
            item.Description = mDescription;
            item.Latitude = mLatitude;
            item.Longitude = mLongitude;
            item.City = mCity;
            item.ShowOnMap = mShowOnMap;
            item.CategoryId = mCategoryId;

            if (mPicture != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                mPicture.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                item.PictureData = Base64.encodeToString(byteArray, Base64.DEFAULT);
            }

            RevalueService service = RevalueServiceGenerator.createService(mToken);

            try {
                service.InsertItem(item).execute();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Toast.makeText(InsertFragment.this.getContext(),
                    getString(R.string.item_saved), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }
}