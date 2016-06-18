/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.utilities;

import android.database.Cursor;

@SuppressWarnings({"SameParameterValue", "unused"})
public final class CursorUtilities {
    /** Gets the string from the specified cursor's column */
    public static String getString(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return !cursor.isNull(columnIndex) ? cursor.getString(columnIndex) : null;
    }

    /** Gets the integer from the specified cursor's column */
    public static Integer getInt(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return !cursor.isNull(columnIndex) ? cursor.getInt(columnIndex) : null;
    }

    /** Gets the long from the specified cursor's column */
    public static Long getLong(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return !cursor.isNull(columnIndex) ? cursor.getLong(columnIndex) : null;
    }

    /** Gets the boolean from the specified cursor's column */
    public static Boolean getBoolean(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return !cursor.isNull(columnIndex) ? (cursor.getInt(columnIndex) != 0) : null;
    }

    /** Gets the float from the specified cursor's column */
    public static Float getFloat(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return !cursor.isNull(columnIndex) ? cursor.getFloat(columnIndex) : null;
    }
}