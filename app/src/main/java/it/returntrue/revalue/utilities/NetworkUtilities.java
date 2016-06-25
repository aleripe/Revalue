/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import it.returntrue.revalue.R;
import it.returntrue.revalue.api.APIError;
import retrofit2.Response;

/**
 * Provides utilities to manipulate network
 * */
public class NetworkUtilities {
    private static final String KEY_MESSAGE = "Message";

    public static APIError parseError(Context context, Response response) {
        APIError apiError = new APIError();
        try {
            apiError.Code = response.code();
            apiError.Message = response.message();
        }
        catch (Exception e) {
            apiError.Code = 500;
            apiError.Message = context.getString(R.string.generic_error);
        }
        return apiError;
    }

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}