/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.api.CategoryModel;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.GetCategoriesEvent;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.utilities.CategoryUtilities;
import it.returntrue.revalue.utilities.NetworkUtilities;

/**
 * Shows the items filters
 * */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FiltersFragment extends DialogFragment {
    private DialogInterface.OnDismissListener mOnDismissListener;
    private HashMap<Integer, Integer> mRadioButtons;
    private List<CategoryModel> mCategories;
    private ArrayAdapter<CategoryModel> mAdapter;

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

        loadCategories();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stops listening to bus events
        BusProvider.bus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Listens to bus events
        BusProvider.bus().register(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_filters, null);

        // Binds controls
        ButterKnife.bind(this, view);

        // Creates associations for radio buttons
        mRadioButtons = new HashMap<>();
        mRadioButtons.put(50, R.id.radio_distance_50);
        mRadioButtons.put(100, R.id.radio_distance_100);
        mRadioButtons.put(200, R.id.radio_distance_200);
        mRadioButtons.put(R.id.radio_distance_50, 50);
        mRadioButtons.put(R.id.radio_distance_100, 100);
        mRadioButtons.put(R.id.radio_distance_200, 200);

        // Fills previous values
        mTextTitle.setText(application().getFilterTitle());
        mRadioDistance.check(mRadioButtons.get(application().getFilterDistance()));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.search_filters)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                application().setFilterTitle(mTextTitle.getText().toString());
                                application().setFilterCategory(CategoryUtilities.getCategoryId(
                                        mCategories,
                                        mSpinnerCategory.getSelectedItemPosition()));
                                application().setFilterDistance(mRadioButtons.get(
                                        mRadioDistance.getCheckedRadioButtonId()));
                            }
                        }
                )
                .setNeutralButton(R.string.clear,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                application().clearFilters();
                            }
                        })
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

        // Fills previous values
        mSpinnerCategory.setSelection(CategoryUtilities.getCategoryPosition(
                mCategories, application().getFilterCategory()));
    }

    @Subscribe
    public void onGetCategoriesFailure(GetCategoriesEvent.OnFailure onFailure) {
        Toast.makeText(getContext(), R.string.could_not_get_categories, Toast.LENGTH_LONG).show();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    private void loadCategories() {
        if (NetworkUtilities.checkInternetConnection(getContext())) {
            BusProvider.bus().post(new GetCategoriesEvent.OnStart());
        }
        else {
            Toast.makeText(getContext(), R.string.check_connection, Toast.LENGTH_LONG).show();
        }
    }

    private CategoryModel createEmptyCategoryModel() {
        CategoryModel category = new CategoryModel();
        category.Id = 0;
        category.Name = getString(R.string.text_filter_category_empty);
        return category;
    }

    protected RevalueApplication application() {
        return RevalueApplication.get(getActivity().getApplicationContext());
    }

    protected SessionPreferences session() {
        return SessionPreferences.get(getActivity());
    }
}