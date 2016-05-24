/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.data;

import android.provider.BaseColumns;

public final class MessageContract {
    public static abstract class MessageEntry implements BaseColumns {
        public static final String TABLE = "message";
        public static final String COLUMN_FROM = "from";
        public static final String COLUMN_TO = "to";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_DATE = "date";
    }
}