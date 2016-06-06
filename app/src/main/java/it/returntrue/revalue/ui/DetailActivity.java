package it.returntrue.revalue.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "id";

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.layout_multipane) @Nullable LinearLayout mLayoutMultipane;
    @Bind(R.id.fab_chat) FloatingActionButton mFabChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final DetailFragment detailFragment =
                (DetailFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);

        // Binds controls
        ButterKnife.bind(this);

        // Sets toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Sets chat floating action button
        mFabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailFragment.goToChatActivity();
            }
        });
    }
}