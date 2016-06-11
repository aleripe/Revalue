package it.returntrue.revalue.utilities;

import android.support.annotation.IntDef;

public final class Constants {
    @IntDef({ NEAREST_ITEMS_MODE, FAVORITE_ITEMS_MODE, PERSONAL_MOVIES_MODE })
    public @interface ItemMode {}
    public static final int NEAREST_ITEMS_MODE = 1;
    public static final int FAVORITE_ITEMS_MODE = 2;
    public static final int PERSONAL_MOVIES_MODE = 3;
}