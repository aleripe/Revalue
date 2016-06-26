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
import it.returntrue.revalue.ui.ChatActivity;

/**
 * Implements a listener for FCM incoming messages
 * */
@SuppressWarnings("SameParameterValue")
public class RevalueFcmListenerService extends FirebaseMessagingService {
    private static final int NOTIFICATION_MESSAGE = 100;

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map data = message.getData();

        // Creates preferences managers
        SessionPreferences sessionPreferences = new SessionPreferences(this);

        int id = Integer.parseInt(String.valueOf(data.get("id")));
        int userId = Integer.parseInt(String.valueOf(data.get("userId")));
        String userAlias = String.valueOf(data.get("userAlias"));
        String title = String.valueOf(data.get("title"));
        String text = String.valueOf(data.get("text"));
        long date = Long.parseLong(String.valueOf(data.get("date")));
        boolean isOwned = Boolean.parseBoolean(String.valueOf(data.get("isOwned")));

        ContentValues values = new ContentValues(2);
        values.put(MessageEntry.COLUMN_ITEM_ID, id);
        values.put(MessageEntry.COLUMN_SENDER_ID, userId);
        values.put(MessageEntry.COLUMN_RECEIVER_ID, sessionPreferences.getUserId());
        values.put(MessageEntry.COLUMN_TEXT, text);
        values.put(MessageEntry.COLUMN_DATE, date);
        getContentResolver().insert(MessageProvider.buildMessageUri(), values);

        sendNotification(title, userAlias + ": " + text, date, id, userId, userAlias, isOwned);
    }

    private void sendNotification(String title, String text, long date, int itemId,
                                  int userId, String userAlias, boolean isOwned) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(text);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_ITEM_ID, itemId);
        intent.putExtra(ChatActivity.EXTRA_USER_ID, userId);
        intent.putExtra(ChatActivity.EXTRA_USER_ALIAS, userAlias);
        intent.putExtra(ChatActivity.EXTRA_IS_OWNED, isOwned);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        Notification notification = builder
                .setTicker(title)
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
        notificationManager.notify(NOTIFICATION_MESSAGE, notification);
    }
}