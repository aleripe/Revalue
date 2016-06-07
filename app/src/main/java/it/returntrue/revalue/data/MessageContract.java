/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.data;

import android.provider.BaseColumns;

public final class MessageContract {
    public static abstract class MessageEntry implements BaseColumns {
        public static final String TABLE = "message";
        public static final String COLUMN_ITEM_ID = "item_id";
        public static final String COLUMN_SENDER_ID = "sender_id";
        public static final String COLUMN_RECEIVER_ID = "receiver_id";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_DISPATCH_DATE = "date";
    }
}