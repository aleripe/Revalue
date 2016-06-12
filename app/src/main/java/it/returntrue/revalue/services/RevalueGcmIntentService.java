package it.returntrue.revalue.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import it.returntrue.revalue.R;
import it.returntrue.revalue.api.GcmTokenModel;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.UpdateGcmTokenEvent;

public class RevalueGcmIntentService extends IntentService {
    private static final String TAG = RevalueGcmIntentService.class.getSimpleName();

    public RevalueGcmIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        registerGCM();
    }

    private void registerGCM() {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String gcmRegistrationId = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(TAG, "GCM token: " + gcmRegistrationId);

            // Creates token model
            GcmTokenModel gcmTokenModel = new GcmTokenModel();
            gcmTokenModel.Token = gcmRegistrationId;

            // Calls API to update Gcm registration id (token)
            BusProvider.bus().post(new UpdateGcmTokenEvent.OnStart(gcmTokenModel));
        }
        catch (Exception e) { }
    }
}