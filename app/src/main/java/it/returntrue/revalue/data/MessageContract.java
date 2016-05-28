/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.data;

import android.provider.BaseColumns;

public final class MessageContract {
    public static abstract class MessageEntry implements BaseColumns {
        public static final String TABLE = "message";
        public static final String COLUMN_ITEM_ID = "item_id";
        public static final String COLUMN_ISSENT = "is_sent";
        public static final String COLUMN_ISRECEIVED = "is_received";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_DATE = "date";
    }
}