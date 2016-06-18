package it.returntrue.revalue.events;

import java.util.List;

import it.returntrue.revalue.api.ItemModel;

@SuppressWarnings("ALL")
public class GetItemsEvent {
    public static class OnStart {
        private final int mMode;
        private final double mLatitude;
        private final double mLongitude;
        private final String mTitle;
        private final Integer mCategory;
        private final Integer mDistance;

        public OnStart(int mode, double latitude, double longitude,
                       String title, Integer category, Integer distance) {
            mMode = mode;
            mLatitude = latitude;
            mLongitude = longitude;
            mTitle = title;
            mCategory = category;
            mDistance = distance;
        }

        public int getMode() {
            return mMode;
        }

        public double getLatitude() {
            return mLatitude;
        }

        public double getLongitude() {
            return mLongitude;
        }

        public String getTitle() {
            return mTitle;
        }

        public Integer getCategory() {
            return mCategory;
        }

        public Integer getDistance() {
            return mDistance;
        }
    }

    public static class OnSuccess {
        private final List<ItemModel> mItems;

        public OnSuccess(List<ItemModel> items) {
            mItems = items;
        }

        public List<ItemModel> getItems() {
            return mItems;
        }
    }

    public static class OnFailure {
        public OnFailure() { }
    }
}