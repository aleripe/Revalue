package it.returntrue.revalue.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "id";

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.layout_multipane) @Nullable LinearLayout mLayoutMultipane;
    @Bind(R.id.fab) FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Binds controls
        ButterKnife.bind(this);

        // Sets toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Sets floating action button
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        // Sets floating action button visibility
        mFab.setVisibility((mLayoutMultipane != null) ? View.INVISIBLE : View.VISIBLE);
    }
}