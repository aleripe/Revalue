package it.returntrue.revalue.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IntDef;

import it.returntrue.revalue.R;

/**
 * Provides session preferences management
 */
public class InterfacePreferences {
    private static final String PREFERENCES_INTERFACE = "it.returntrue.revalue.PREFERENCES_FILE_INTERFACE";
    private static final String KEY_MAIN_MODE = "main_mode";
    private static final String KEY_FILTER_TITLE = "filter_title";
    private static final String KEY_FILTER_CATEGORY = "filter_category";
    private static final String KEY_FILTER_DISTANCE = "filter_distance";
    private static final String KEY_LOCATION_LATITUDE = "location_latitude";
    private static final String KEY_LOCATION_LONGITUDE = "location_longitude";
    private static final String KEY_CURRENT_PAGE = "current_page";

    private static final int DEFAULT_DISTANCE = 20;

    // Defines allowed modes as a fake enumeration
    @IntDef({ LIST_MODE, MAP_MODE })
    public @interface Modes {}
    public static final int LIST_MODE = 1;
    public static final int MAP_MODE = 2;

    private Context mContext;
    private SharedPreferences mPreferences;

    public InterfacePreferences(Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(PREFERENCES_INTERFACE, Context.MODE_PRIVATE);
    }

    public void setMainMode(@Modes int mainMode) {
        mPreferences.edit().putInt(KEY_MAIN_MODE, mainMode).apply();
    }

    public @Modes int getMainMode() {
        // noinspection ResourceType (we're sure mMode is one of the allowed values)
        return mPreferences.getInt(KEY_MAIN_MODE, LIST_MODE);
    }

    public void setFilterTitle(String title) {
        mPreferences.edit().putString(KEY_FILTER_TITLE, title).apply();
    }

    public String getFilterTitle() {
        return mPreferences.getString(KEY_FILTER_TITLE, null);
    }

    public String getFilterTitleDescription() {
        String filterTitle = getFilterTitle();
        return (filterTitle != null) ? filterTitle :
                mContext.getString(R.string.text_filter_title_empty);
    }

    public void setFilterCategory(int category) {
        mPreferences.edit().putInt(KEY_FILTER_CATEGORY,category).apply();
    }

    public int getFilterCategory() {
        return mPreferences.getInt(KEY_FILTER_CATEGORY, 0);
    }

    public String getFilterCategoryDescription() {
        int filterCategory = getFilterCategory();
        return (filterCategory != 0) ? String.valueOf(filterCategory) :
                mContext.getString(R.string.text_filter_category_empty);
    }

    public void setFilterDistance(int filterDistance) {
        mPreferences.edit().putInt(KEY_FILTER_DISTANCE, filterDistance).apply();
    }

    public int getFilterDistance() {
        return mPreferences.getInt(KEY_FILTER_DISTANCE, DEFAULT_DISTANCE);
    }

    public String getFilterDistanceDescription() {
        int filterDistance = getFilterDistance();
        return String.valueOf(filterDistance) + " " + mContext.getString(R.string.km);
    }

    public void setLocationLatitude(float latitude) {
        mPreferences.edit().putFloat(KEY_LOCATION_LATITUDE, latitude).apply();
    }

    public float getLocationLatitude() {
        return mPreferences.getFloat(KEY_LOCATION_LATITUDE, 0);
    }

    public void setLocationLongitude(float longitude) {
        mPreferences.edit().putFloat(KEY_LOCATION_LONGITUDE, longitude).apply();
    }

    public float getLocationLongitude() {
        return mPreferences.getFloat(KEY_LOCATION_LONGITUDE, 0);
    }

    public void setCurrentPage(int page) {
        mPreferences.edit().putInt(KEY_CURRENT_PAGE, page).apply();
    }

    public int getCurrentPage() {
        return mPreferences.getInt(KEY_CURRENT_PAGE, 0);
    }

    public void clearAll() {
        mPreferences.edit().clear();
    }
}