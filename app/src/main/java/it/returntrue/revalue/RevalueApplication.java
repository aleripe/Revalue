/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.stetho.Stetho;

import java.util.List;

import it.returntrue.revalue.api.CategoryModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.utilities.CategoryUtilities;
import it.returntrue.revalue.utilities.Constants;

/**
 * Provides concrete application implementation
 * */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class RevalueApplication extends Application {
    private static RevalueApplication sApplication;

    private static final int DEFAULT_DISTANCE = 50;

    @Constants.MainMode private int mMainMode = Constants.NEAREST_ITEMS_MODE;
    @Constants.ItemsMode private int mItemsMode = Constants.LIST_MODE;
    private String mFilterTitle;
    private Integer mFilterCategory;
    private Integer mFilterDistance = DEFAULT_DISTANCE;
    private Double mLocationLatitude;
    private Double mLocationLongitude;
    private Double mMapLatitude;
    private Double mMapLongitude;
    private Float mMapZoom;
    private RevalueService mRevalueService;

    public static RevalueApplication get(Context context) {
        if (sApplication == null) {
            sApplication = (RevalueApplication)context;
        }
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    public @Constants.MainMode int getMainMode() {
        return mMainMode;
    }

    public void setMainMode(@Constants.MainMode int mode) {
        mMainMode = mode;
        mMapLatitude = null;
        mMapLongitude = null;
        mMapZoom = null;
    }

    public @Constants.ItemsMode int getItemsMode() {
        return mItemsMode;
    }

    public void setItemsMode(@Constants.ItemsMode int mode) {
        mItemsMode = mode;
    }

    public void setFilterTitle(String title) {
        if (TextUtils.isEmpty(title)) title = null;
        mFilterTitle = title;
    }

    public String getFilterTitle() {
        return mFilterTitle;
    }

    public String getFilterTitleDescription() {
        String filterTitle = getFilterTitle();
        return (filterTitle != null) ? filterTitle : getString(R.string.text_filter_title_empty);
    }

    public void setFilterCategory(Integer category) {
        mFilterCategory = category;
    }

    public Integer getFilterCategory() {
        return mFilterCategory;
    }

    public String getFilterCategoryDescription(List<CategoryModel> categories) {
        String categoryName = CategoryUtilities.getCategoryName(categories, getFilterCategory());
        return (categoryName != null) ? categoryName : getString(R.string.text_filter_category_empty);
    }

    public void setFilterDistance(Integer distance) {
        mFilterDistance = distance;
    }

    public Integer getFilterDistance() {
        return mFilterDistance;
    }

    public String getFilterDistanceDescription() {
        Integer filterDistance = getFilterDistance();
        return String.valueOf(filterDistance) + " " + getString(R.string.km);
    }

    public void clearFilters() {
        setFilterTitle(null);
        setFilterCategory(null);
        setFilterDistance(DEFAULT_DISTANCE);
    }

    public void setLocationLatitude(Double latitude) {
        mLocationLatitude = latitude;
    }

    public Double getLocationLatitude() {
        return mLocationLatitude;
    }

    public void setLocationLongitude(Double longitude) {
        mLocationLongitude = longitude;
    }

    public Double getLocationLongitude() {
        return mLocationLongitude;
    }

    public void setMapLatitude(Double latitude) {
        mMapLatitude = latitude;
    }

    public Double getMapLatitude() {
        return mMapLatitude;
    }

    public void setMapLongitude(Double longitude) {
        mMapLongitude = longitude;
    }

    public Double getMapLongitude() {
        return mMapLongitude;
    }

    public void setMapZoom(Float zoom) {
        mMapZoom = zoom;
    }

    public Float getMapZoom() {
        return mMapZoom;
    }

    public boolean isLocationAvailable() {
        return (getLocationLatitude() != null) && (getLocationLongitude() != null);
    }

    public void setupRevalueService(String token) {
        if (mRevalueService == null) {
            mRevalueService = new RevalueService(this, BusProvider.bus(), token);
            BusProvider.bus().register(mRevalueService);
        }
    }

    public void updateRevalueService(String token) {
        if (mRevalueService != null) {
            BusProvider.bus().unregister(mRevalueService);
            mRevalueService = new RevalueService(this, BusProvider.bus(), token);
            BusProvider.bus().register(mRevalueService);
        }
    }

    private void initialize() {
        // Initializes Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Initializes Stetho to provide Chrome inspect integration
        Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(this);
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this));
        initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(getApplicationContext()));
        Stetho.Initializer initializer = initializerBuilder.build();
        Stetho.initialize(initializer);
    }
}