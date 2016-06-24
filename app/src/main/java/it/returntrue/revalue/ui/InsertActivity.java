/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.ui.base.BaseActivity;

/**
 * Shows new item form
 * */
@SuppressWarnings({"ConstantConditions", "WeakerAccess", "unused"})
public class InsertActivity extends BaseActivity {
    @Bind(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets view
        setContentView(R.layout.activity_insert);

        // Binds controls
        ButterKnife.bind(this);

        // Sets toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}