package it.returntrue.revalue.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONObject;

import it.returntrue.revalue.R;
import retrofit2.Response;

public class NetworkUtilities {
    private static final String KEY_MESSAGE = "Message";

    public static String parseError(Context context, Response response) {
        try {
            JSONObject error = new JSONObject(response.errorBody().string());
            return error.getString(KEY_MESSAGE);
        }
        catch (Exception e) {
            return context.getString(R.string.generic_error);
        }
    }

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}