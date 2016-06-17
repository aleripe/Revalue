package it.returntrue.revalue.services;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import it.returntrue.revalue.api.GcmTokenModel;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.UpdateGcmTokenEvent;
import it.returntrue.revalue.preferences.SessionPreferences;

public class RevalueGcmIntentService extends IntentService {
    private static final String TAG = RevalueGcmIntentService.class.getSimpleName();

    public RevalueGcmIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Creates preferences managers
        SessionPreferences sessionPreferences = new SessionPreferences(this);
        String token = sessionPreferences.getFirebirdToken();
        boolean isLoggedIn = sessionPreferences.getIsLoggedIn();

        // If token exists and user is logged in
        if (!TextUtils.isEmpty(token) && isLoggedIn) {
            Log.i(TAG, "Firebird token: " + token);

            // Creates token model
            GcmTokenModel gcmTokenModel = new GcmTokenModel();
            gcmTokenModel.Token = token;

            // Calls API to update Gcm registration id (token)
            BusProvider.bus().post(new UpdateGcmTokenEvent.OnStart(gcmTokenModel));
        }
    }
}