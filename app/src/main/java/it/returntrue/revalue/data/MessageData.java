/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.data;

import android.database.Cursor;
import it.returntrue.revalue.data.MessageContract.MessageEntry;
import it.returntrue.revalue.utilities.CursorUtilities;

public final class MessageData {
	public static String getFrom(Cursor cursor) {
		return CursorUtilities.getString(cursor, MessageEntry.COLUMN_FROM);
	}

	public static String getTo(Cursor cursor) {
		return CursorUtilities.getString(cursor, MessageEntry.COLUMN_TO);
	}

	public static String getMessage(Cursor cursor) {
		return CursorUtilities.getString(cursor, MessageEntry.COLUMN_MESSAGE);
	}

	public static String getDate(Cursor cursor) {
		return CursorUtilities.getString(cursor, MessageEntry.COLUMN_DATE);
	}
}