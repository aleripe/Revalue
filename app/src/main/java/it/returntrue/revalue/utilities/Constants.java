/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.utilities;

import android.support.annotation.IntDef;

/**
 * Contains constants for general use
 * */
public final class Constants {
    @IntDef({ NEAREST_ITEMS_MODE, FAVORITE_ITEMS_MODE, PERSONAL_MOVIES_MODE })
    public @interface MainMode {}
    public static final int NEAREST_ITEMS_MODE = 1;
    public static final int FAVORITE_ITEMS_MODE = 2;
    public static final int PERSONAL_MOVIES_MODE = 3;

    @IntDef({ LIST_MODE, MAP_MODE })
    public @interface ItemsMode {}
    public static final int LIST_MODE = 1;
    public static final int MAP_MODE = 2;
}