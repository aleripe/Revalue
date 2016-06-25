/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import it.returntrue.revalue.R;
import it.returntrue.revalue.data.MessageContract.MessageEntry;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.provider.MessageProvider;
import it.returntrue.revalue.ui.DetailActivity;

/**
 * Implements a listener for FCM incoming messages
 * */
@SuppressWarnings("SameParameterValue")
public class RevalueFcmListenerService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map data = message.getData();

        // Creates preferences managers
        SessionPreferences sessionPreferences = new SessionPreferences(this);

        int id = Integer.parseInt(String.valueOf(data.get("id")));
        int userId = Integer.parseInt(String.valueOf(data.get("userId")));
        String text = String.valueOf(data.get("text"));
        long date = Long.parseLong(String.valueOf(data.get("date")));

        ContentValues values = new ContentValues(2);
        values.put(MessageEntry.COLUMN_ITEM_ID, id);
        values.put(MessageEntry.COLUMN_SENDER_ID, userId);
        values.put(MessageEntry.COLUMN_RECEIVER_ID, sessionPreferences.getUserId());
        values.put(MessageEntry.COLUMN_TEXT, text);
        values.put(MessageEntry.COLUMN_DATE, date);
        getContentResolver().insert(MessageProvider.buildMessageUri(), values);

        sendNotification(getString(R.string.new_message_received), text, date, id);
    }

    private void sendNotification(String title, String text, long date, int itemId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(text);

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_ID, itemId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification;
        notification = builder.setSmallIcon(R.mipmap.ic_launcher).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setStyle(inboxStyle)
                .setWhen(date)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setContentText(text)
                .build();

        NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(100, notification);
    }
}