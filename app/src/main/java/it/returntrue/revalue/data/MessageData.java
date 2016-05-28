/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.data;

import android.database.Cursor;
import it.returntrue.revalue.data.MessageContract.MessageEntry;
import it.returntrue.revalue.utilities.CursorUtilities;

public final class MessageData {
	public static Boolean getIsSent(Cursor cursor) {
		return CursorUtilities.getBoolean(cursor, MessageEntry.COLUMN_ISSENT);
	}

	public static Boolean getIsReceived(Cursor cursor) {
		return CursorUtilities.getBoolean(cursor, MessageEntry.COLUMN_ISRECEIVED);
	}

	public static String getText(Cursor cursor) {
		return CursorUtilities.getString(cursor, MessageEntry.COLUMN_TEXT);
	}

	public static String getDate(Cursor cursor) {
		return CursorUtilities.getString(cursor, MessageEntry.COLUMN_DATE);
	}
}