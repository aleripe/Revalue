package it.returntrue.revalue.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class FavoritesRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FavoritesRemoteViewsFactory(getApplicationContext());
    }
}