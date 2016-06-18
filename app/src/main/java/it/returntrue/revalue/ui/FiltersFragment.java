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

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.api.CategoryModel;

@SuppressWarnings({"WeakerAccess", "unused"})
public class FiltersFragment extends DialogFragment {
    private RevalueApplication mApplication;
    private DialogInterface.OnDismissListener mOnDismissListener;
    private HashMap<Integer, Integer> mRadioButtons;
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

        // Sets application context
        mApplication = (RevalueApplication)getActivity().getApplicationContext();

        // Creates adapter and inserts empty default value
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mAdapter.clear();
        mAdapter.addAll(mApplication.getCategories());
        mAdapter.insert(createEmptyCategoryModel(), 0);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Creates associations for radio buttons
        mRadioButtons = new HashMap<>();
        mRadioButtons.put(50, R.id.radio_distance_50);
        mRadioButtons.put(100, R.id.radio_distance_100);
        mRadioButtons.put(200, R.id.radio_distance_200);
        mRadioButtons.put(R.id.radio_distance_50, 50);
        mRadioButtons.put(R.id.radio_distance_100, 100);
        mRadioButtons.put(R.id.radio_distance_200, 200);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_filters, null);

        // Binds controls
        ButterKnife.bind(this, view);

        // Sets adapter
        mSpinnerCategory.setAdapter(mAdapter);

        // Fills previous values
        mTextTitle.setText(mApplication.getFilterTitle());
        mSpinnerCategory.setSelection(mApplication.getCategoryPosition(mApplication.getFilterCategory()));
        mRadioDistance.check(mRadioButtons.get(mApplication.getFilterDistance()));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.search_filters)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mApplication.setFilterTitle(mTextTitle.getText().toString());
                                mApplication.setFilterCategory(mApplication.getCategoryId(
                                        mSpinnerCategory.getSelectedItemPosition()));
                                mApplication.setFilterDistance(mRadioButtons.get(
                                        mRadioDistance.getCheckedRadioButtonId()));
                            }
                        }
                )
                .setNeutralButton(R.string.clear,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mApplication.clearFilters();
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

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    private CategoryModel createEmptyCategoryModel() {
        CategoryModel category = new CategoryModel();
        category.Id = 0;
        category.Name = getString(R.string.text_filter_category_empty);
        return category;
    }
}