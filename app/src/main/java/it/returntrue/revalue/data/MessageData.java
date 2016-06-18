/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.data;

import android.database.Cursor;
import it.returntrue.revalue.data.MessageContract.MessageEntry;
import it.returntrue.revalue.utilities.CursorUtilities;
import it.returntrue.revalue.utilities.DateUtilities;

@SuppressWarnings({"ConstantConditions", "unused"})
public final class MessageData {
	public static Integer getItemId(Cursor cursor) {
		return CursorUtilities.getInt(cursor, MessageEntry.COLUMN_ITEM_ID);
	}

	public static Integer getSenderId(Cursor cursor) {
		return CursorUtilities.getInt(cursor, MessageEntry.COLUMN_SENDER_ID);
	}

	public static Integer getReceiverId(Cursor cursor) {
		return CursorUtilities.getInt(cursor, MessageEntry.COLUMN_RECEIVER_ID);
	}

	public static String getText(Cursor cursor) {
		return CursorUtilities.getString(cursor, MessageEntry.COLUMN_TEXT);
	}

	public static String getDate(Cursor cursor) {
		return DateUtilities.format(CursorUtilities.getLong(cursor, MessageEntry.COLUMN_DATE));
	}
}