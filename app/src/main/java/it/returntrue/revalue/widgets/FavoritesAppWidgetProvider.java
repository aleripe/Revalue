/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import it.returntrue.revalue.R;
import it.returntrue.revalue.ui.MainActivity;

/**
 * Implements a provider to manage application widgets
 * */
public class FavoritesAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // Intent to call service
            Intent serviceIntent = new Intent(context, FavoritesRemoteViewsService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            // Intent to call activity
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            activityIntent.setData(Uri.parse(activityIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_favorites);
            views.setRemoteAdapter(R.id.list_view, serviceIntent);
            views.setEmptyView(R.id.list_view, R.id.empty_view);
            views.setPendingIntentTemplate(R.id.list_view, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}