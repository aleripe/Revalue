package it.returntrue.revalue;

import android.app.Application;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.stetho.Stetho;

import java.util.List;

import it.returntrue.revalue.api.CategoryModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.events.BusProvider;

public class RevalueApplication extends Application {
    // Defines allowed modes as a fake enumeration
    @IntDef({ LIST_MODE, MAP_MODE })
    public @interface Modes {}
    public static final int LIST_MODE = 1;
    public static final int MAP_MODE = 2;

    private static final int DEFAULT_DISTANCE = 50;

    @Modes private int mMainMode = LIST_MODE;
    private String mFilterTitle = null;
    private Integer mFilterCategory = null;
    private Integer mFilterDistance = DEFAULT_DISTANCE;
    private Double mLocationLatitude = null;
    private Double mLocationLongitude = null;
    private List<CategoryModel> mCategories = null;
    //private Tracker mTracker;
    private RevalueService mRevalueService;

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    public void setMainMode(@Modes int mode) {
        mMainMode = mode;
    }

    public @Modes int getMainMode() {
        return mMainMode;
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

    public String getFilterCategoryDescription() {
        Integer filterCategory = getFilterCategory();
        return (filterCategory != null) ? getCategoryName(filterCategory) : getString(R.string.text_filter_category_empty);
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

    public void setCategories(List<CategoryModel> categories) {
        mCategories = categories;
    }

    public List<CategoryModel> getCategories() {
        return mCategories;
    }

    private String getCategoryName(Integer id) {
        if (id != null) {
            for (CategoryModel categoryModel : mCategories) {
                if (categoryModel.Id == getFilterCategory()) {
                    return categoryModel.Name;
                }
            }
        }

        return getString(R.string.text_filter_category_empty);
    }

    public int getCategoryPosition(Integer id) {
        if (id != null) {
            for (int i = 0; i < mCategories.size(); i++) {
                CategoryModel categoryModel = mCategories.get(i);
                if (categoryModel.Id == id) return i + 1;
            }
        }

        return 0;
    }

    public Integer getCategoryId(int position) {
        if (position > 0 && position <= mCategories.size()) {
            return mCategories.get(position - 1).Id;
        }

        return null;
    }

    /*
    public Tracker getTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
    */

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