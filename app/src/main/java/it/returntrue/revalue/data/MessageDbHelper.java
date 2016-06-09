/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import it.returntrue.revalue.data.MessageContract.MessageEntry;

public class MessageDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "revalue.db";

    private static final String SQL_CREATE_MESSAGE =
        "CREATE TABLE " + MessageEntry.TABLE + " (" +
            MessageEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
            MessageEntry.COLUMN_ITEM_ID + " INTEGER NOT NULL," +
            MessageEntry.COLUMN_SENDER_ID + " INTEGER NOT NULL," +
            MessageEntry.COLUMN_RECEIVER_ID + " INTEGER NOT NULL," +
            MessageEntry.COLUMN_TEXT + " TEXT NOT NULL," +
            MessageEntry.COLUMN_DATE + " TEXT NOT NULL" +
        ")";

    public MessageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}