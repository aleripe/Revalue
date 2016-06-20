/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Implements a listener for FCM token refreshing
 * */
public class FavoritesRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FavoritesRemoteViewsFactory(getApplicationContext());
    }
}