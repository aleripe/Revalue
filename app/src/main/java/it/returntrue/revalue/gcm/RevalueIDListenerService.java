package it.returntrue.revalue.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import it.returntrue.revalue.services.RevalueGcmIntentService;

public class RevalueIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RevalueGcmIntentService.class);
        startService(intent);
    }
}