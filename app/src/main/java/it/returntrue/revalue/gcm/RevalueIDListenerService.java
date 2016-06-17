package it.returntrue.revalue.gcm;

import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.services.RevalueGcmIntentService;

public class RevalueIDListenerService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        SessionPreferences sessionPreferences = new SessionPreferences(this);
        sessionPreferences.setFirebirdToken(FirebaseInstanceId.getInstance().getToken());

        Intent intent = new Intent(this, RevalueGcmIntentService.class);
        startService(intent);
    }
}