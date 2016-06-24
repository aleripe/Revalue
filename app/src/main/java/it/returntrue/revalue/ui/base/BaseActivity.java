/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import it.returntrue.revalue.R;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.ui.LoginActivity;

/**
 * Provides a base implementation for all application activities
 * */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup API service
        application().setupRevalueService(session().getToken());

        // Listens to bus events
        BusProvider.bus().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stops listening to bus events
        BusProvider.bus().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Checks Play Services availability
        checkPlayServices();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLAY_SERVICES_RESOLUTION_REQUEST:
                if (resultCode == RESULT_CANCELED) {
                    toast(R.string.play_service_unavailable);
                    finish();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void checkLogin() {
        if (!session().getIsLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
        }
    }

    protected RevalueApplication application() {
        return RevalueApplication.get(getApplicationContext());
    }

    protected SessionPreferences session() {
        return SessionPreferences.get(this);
    }

    protected void toast(@StringRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}