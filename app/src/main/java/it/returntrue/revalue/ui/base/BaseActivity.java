package it.returntrue.revalue.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.ui.LoginActivity;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected RevalueApplication mApplication;
    protected SessionPreferences mSessionPreferences;
    protected BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets application context
        mApplication = (RevalueApplication)getApplicationContext();

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(this);

        // Setup API service
        mApplication.setupRevalueService(mSessionPreferences.getToken());

        // Creates GCM broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: decidere cosa fare alla ricezione dei messaggi
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stops listening to bus events
        BusProvider.bus().unregister(this);

        // Stops listening to broadcast messages
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listens to bus events
        BusProvider.bus().register(this);

        // TODO: verificare
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter("registrationComplete"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter("pushNotification"));
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
}