package it.returntrue.revalue.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class RevalueIDListenerService extends InstanceIDListenerService {

    private static final String TAG = RevalueIDListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RevalueGcmIntentService.class);
        startService(intent);
    }
}