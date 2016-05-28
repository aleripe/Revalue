/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import it.returntrue.revalue.data.MessageContract.MessageEntry;
import it.returntrue.revalue.data.MessageDbHelper;

public class MessageProvider extends ContentProvider {
    public static final String CONTENT_AUTHORITY = "it.returntrue.revalue.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MESSAGE = "message";
    public static final String MESSAGE_DIR_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "vnd." + CONTENT_AUTHORITY + "." + PATH_MESSAGE;

    private static final int MESSAGE = 100;
    private static final UriMatcher mUriMatcher = buildUriMatcher();

    private MessageDbHelper mMessageDbHelper;

    @Override
    public boolean onCreate() {
        mMessageDbHelper = new MessageDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                return MESSAGE_DIR_CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                cursor = mMessageDbHelper.getReadableDatabase().query(MessageEntry.TABLE,
                        null, null, null, null, null, null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mMessageDbHelper.getWritableDatabase();
        Uri returnUri;

        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                long id = db.insert(MessageEntry.TABLE, null, values);
                if (id > 0) {
                    returnUri = buildMessageUri(id);
                }
                else {
                    throw new SQLException("Failed to insert row into Uri: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mMessageDbHelper.getWritableDatabase();
        int rowsUpdated;

        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                rowsUpdated = db.update(MessageEntry.TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mMessageDbHelper.getWritableDatabase();
        int rowsDeleted;

        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                rowsDeleted = db.delete(MessageEntry.TABLE, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
        SQLiteDatabase db = mMessageDbHelper.getWritableDatabase();
        int totalRowCount = 0;

        String tableName;

        switch (mUriMatcher.match(uri)) {
            case MESSAGE:
                tableName = MessageEntry.TABLE;
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        db.beginTransaction();

        try {
            for (ContentValues values : valuesArray) {
                long id = db.insert(tableName, null, values);

                if (id != -1) {
                    totalRowCount += 1;
                }
            }

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        if (totalRowCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return totalRowCount;
    }

    public static Uri buildMessageUri() {
        return CONTENT_URI.buildUpon()
                .appendPath(PATH_MESSAGE)
                .build();
    }

    public static Uri buildMessageUri(long id) {
        return CONTENT_URI.buildUpon()
                .appendPath(PATH_MESSAGE)
                .appendPath(String.valueOf(id))
                .build();
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_MESSAGE, MESSAGE);
        return uriMatcher;
    }
}