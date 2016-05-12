package it.returntrue.revalue.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.CategoryModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.preferences.InterfacePreferences;
import it.returntrue.revalue.preferences.SessionPreferences;
import retrofit2.Call;

public class FiltersFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<List<CategoryModel>> {
    protected static final int LOADER_CATEGORIES = 1;

    private SessionPreferences mSessionPreferences;
    private InterfacePreferences mInterfacePreferences;
    private DialogInterface.OnDismissListener mOnDismissListener;
    private List<CategoryModel> mCategories;

    @Bind(R.id.text_title) EditText mTextTitle;
    @Bind(R.id.spinner_category) Spinner mSpinnerCategory;
    @Bind(R.id.radio_distance) RadioGroup mRadioDistance;

    public FiltersFragment() {
    }

    public static Fragment newInstance() {
        return new FiltersFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(getContext());
        mInterfacePreferences = new InterfacePreferences(getContext());

        // Setup the available loader
        getLoaderManager().initLoader(LOADER_CATEGORIES, null, this).forceLoad();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_filters, null);

        // Binds controls
        ButterKnife.bind(this, view);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.search_filters)
                .setView(view)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                int checkedRadioDistance = mRadioDistance.getCheckedRadioButtonId();
                                View selectedRadioDistance = mRadioDistance.findViewById(checkedRadioDistance);
                                int index = mRadioDistance.indexOfChild(selectedRadioDistance);

                                int distance = 20;
                                if (index == 0) distance = 50;
                                if (index == 1) distance = 100;
                                if (index == 2) distance = 200;

                                mInterfacePreferences.setFilterTitle(mTextTitle.getText().toString());
                                mInterfacePreferences.setFilterCategory(
                                        mCategories.get(mSpinnerCategory.getSelectedItemPosition()).Id);
                                mInterfacePreferences.setFilterDistance(distance);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }
                )
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(dialog);
        }
    }

    @Override
    public Loader<List<CategoryModel>> onCreateLoader(int id, Bundle args) {
        return new CategoryAsyncTaskLoader(getContext(), mSessionPreferences);
    }

    @Override
    public void onLoadFinished(Loader<List<CategoryModel>> loader, List<CategoryModel> data) {
        mCategories = data;

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getContext(), android.R.layout.simple_list_item_1);

        int categoryIndex = 0;
        int selectedCategoryIndex = 0;
        for (CategoryModel categoryModel : data) {
            adapter.add(categoryModel.Name);

            // Sets previous selection
            if (categoryModel.Id == mInterfacePreferences.getFilterCategory()) {
                selectedCategoryIndex = categoryIndex;
            }

            categoryIndex++;
        }

        // Sets adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCategory.setAdapter(adapter);

        // Fills previous values
        mTextTitle.setText(mInterfacePreferences.getFilterTitle());
        mSpinnerCategory.setSelection(selectedCategoryIndex);

        int distance = mInterfacePreferences.getFilterDistance();
        if (distance == 50) mRadioDistance.check(R.id.radio_distance_50);
        if (distance == 100) mRadioDistance.check(R.id.radio_distance_100);
        if (distance == 200) mRadioDistance.check(R.id.radio_distance_200);
    }

    @Override
    public void onLoaderReset(Loader<List<CategoryModel>> loader) {
        mSpinnerCategory.setAdapter(null);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    private static class CategoryAsyncTaskLoader extends AsyncTaskLoader<List<CategoryModel>> {
        private SessionPreferences mSessionPreferences;

        public CategoryAsyncTaskLoader(Context context, SessionPreferences sessionPreferences) {
            super(context);
            mSessionPreferences = sessionPreferences;
        }

        @Override
        public List<CategoryModel> loadInBackground() {
            RevalueService service = RevalueServiceGenerator.createService(
                    mSessionPreferences.getToken());
            Call<List<CategoryModel>> call = service.GetAllCategories();

            try {
                return call.execute().body();
            }
            catch (IOException e) {
                return new ArrayList<>();
            }
        }
    }
}