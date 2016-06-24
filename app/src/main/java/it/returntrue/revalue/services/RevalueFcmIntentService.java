/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.services;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import it.returntrue.revalue.api.FcmTokenModel;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.UpdateFcmTokenEvent;
import it.returntrue.revalue.preferences.SessionPreferences;

/**
 * Implements a service to refresh FCM token on server
 * */
public class RevalueFcmIntentService extends IntentService {
    private static final String TAG = RevalueFcmIntentService.class.getSimpleName();

    public RevalueFcmIntentService() {
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
            Log.i(TAG, "FCM token: " + token);

            // Creates token model
            FcmTokenModel fcmTokenModel = new FcmTokenModel();
            fcmTokenModel.Token = token;

            // Calls API to update Gcm registration id (token)
            BusProvider.bus().post(new UpdateFcmTokenEvent.OnStart(fcmTokenModel));
        }
    }
}