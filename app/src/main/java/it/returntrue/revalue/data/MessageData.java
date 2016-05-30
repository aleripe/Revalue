/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.data;

import android.database.Cursor;
import it.returntrue.revalue.data.MessageContract.MessageEntry;
import it.returntrue.revalue.utilities.CursorUtilities;

public final class MessageData {
	public static Integer getItemId(Cursor cursor) {
		return CursorUtilities.getInt(cursor, MessageEntry.COLUMN_ITEM_ID);
	}

	public static Integer getUserId(Cursor cursor) {
		return CursorUtilities.getInt(cursor, MessageEntry.COLUMN_USER_ID);
	}

	public static String getText(Cursor cursor) {
		return CursorUtilities.getString(cursor, MessageEntry.COLUMN_TEXT);
	}

	public static Boolean getIsSent(Cursor cursor) {
		return CursorUtilities.getBoolean(cursor, MessageEntry.COLUMN_IS_SENT);
	}

	public static Boolean getIsReceived(Cursor cursor) {
		return CursorUtilities.getBoolean(cursor, MessageEntry.COLUMN_IS_RECEIVED);
	}

	public static String getDispatchDate(Cursor cursor) {
		return CursorUtilities.getString(cursor, MessageEntry.COLUMN_DISPATCH_DATE);
	}
}