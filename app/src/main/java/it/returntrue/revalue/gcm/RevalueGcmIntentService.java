package it.returntrue.revalue.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import it.returntrue.revalue.R;
import it.returntrue.revalue.api.GcmTokenModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import retrofit2.Call;

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
        String gcmRegistrationId = null;

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            gcmRegistrationId = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(TAG, "GCM token: " + gcmRegistrationId);

            // Creates token model
            GcmTokenModel gcmTokenModel = new GcmTokenModel();
            gcmTokenModel.Token = gcmRegistrationId;

            // Calls API to update Gcm registration id (token)
            RevalueService service = RevalueServiceGenerator.createService();
            Call<Void> call = service.UpdateGcmRegistrationId(gcmTokenModel);
            call.execute();

            //sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception e) {
            //sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }
}