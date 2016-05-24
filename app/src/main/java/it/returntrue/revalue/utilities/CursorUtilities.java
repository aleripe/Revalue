/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.utilities;

import android.database.Cursor;

public final class CursorUtilities {
    /** Gets the string from the specified cursor's column */
    public static String getString(Cursor cursor, String columName) {
        return cursor.getString(cursor.getColumnIndex(columName));
    }

    /** Gets the integer from the specified cursor's column */
    public static int getInt(Cursor cursor, String columName) {
        return cursor.getInt(cursor.getColumnIndex(columName));
    }

    /** Gets the long from the specified cursor's column */
    public static long getLong(Cursor cursor, String columName) {
        return cursor.getLong(cursor.getColumnIndex(columName));
    }

    /** Gets the boolean from the specified cursor's column */
    public static boolean getBoolean(Cursor cursor, String columName) {
        return cursor.getInt(cursor.getColumnIndex(columName)) != 0;
    }

    /** Gets the float from the specified cursor's column */
    public static float getFloat(Cursor cursor, String columnName) {
        return cursor.getFloat(cursor.getColumnIndex(columnName));
    }
}