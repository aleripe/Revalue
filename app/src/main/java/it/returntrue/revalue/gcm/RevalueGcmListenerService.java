package it.returntrue.revalue.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import it.returntrue.revalue.R;
import it.returntrue.revalue.data.MessageContract.MessageEntry;
import it.returntrue.revalue.preferences.SessionPreferences;
import it.returntrue.revalue.provider.MessageProvider;
import it.returntrue.revalue.ui.MainActivity;

public class RevalueGcmListenerService extends GcmListenerService {
    private static final String TAG = RevalueGcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        // Creates preferences managers
        SessionPreferences sessionPreferences = new SessionPreferences(this);

        int id = Integer.parseInt(data.getString("id"));
        int userId = Integer.parseInt(data.getString("userId"));
        String text = data.getString("text");
        long date = Long.parseLong(data.getString("date"));

        ContentValues values = new ContentValues(2);
        values.put(MessageEntry.COLUMN_ITEM_ID, id);
        values.put(MessageEntry.COLUMN_SENDER_ID, userId);
        values.put(MessageEntry.COLUMN_RECEIVER_ID, sessionPreferences.getUserId());
        values.put(MessageEntry.COLUMN_TEXT, text);
        values.put(MessageEntry.COLUMN_DATE, date);
        getContentResolver().insert(MessageProvider.buildMessageUri(), values);

        sendNotification("New message received", text, date);
    }

    private void sendNotification(String title, String text, long date) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(text);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

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