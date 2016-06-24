/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.fcm;

import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.services.RevalueFcmIntentService;

/**
 * Implements a listener for FCM token refreshing
 * */
public class RevalueIDListenerService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        SessionPreferences sessionPreferences = new SessionPreferences(this);
        sessionPreferences.setFirebirdToken(FirebaseInstanceId.getInstance().getToken());

        Intent intent = new Intent(this, RevalueFcmIntentService.class);
        startService(intent);
    }
}