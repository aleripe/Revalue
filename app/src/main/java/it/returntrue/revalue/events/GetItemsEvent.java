package it.returntrue.revalue.events;

import java.util.List;

import it.returntrue.revalue.api.ItemModel;

public class GetItemsEvent {
    public static class OnStart {
        private int mMode;
        private double mLatitude;
        private double mLongitude;
        private String mTitle;
        private Integer mCategory;
        private Integer mDistance;

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
        private List<ItemModel> mItems;

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