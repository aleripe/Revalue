package it.returntrue.revalue.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.ui.LoginActivity;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    protected RevalueApplication mApplication;
    protected SessionPreferences mSessionPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets application context
        mApplication = (RevalueApplication)getApplicationContext();

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(this);

        // Setup API service
        mApplication.setupRevalueService(mSessionPreferences.getToken());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stops listening to bus events
        BusProvider.bus().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listens to bus events
        BusProvider.bus().register(this);
    }

    protected void checkLogin() {
        if (!mSessionPreferences.getIsLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        String token = mSessionPreferences.getToken();
        if (!TextUtils.isEmpty(token)) Log.v(TAG, "Token: " + token);
    }

    protected boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}