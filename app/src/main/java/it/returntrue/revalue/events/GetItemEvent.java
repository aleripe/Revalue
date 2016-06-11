package it.returntrue.revalue.events;

import it.returntrue.revalue.api.ItemModel;

public class GetItemEvent {
    public static class OnStart {
        private int mId;
        private double mLatitude;
        private double mLongitude;

        public OnStart(int id, double latitude, double longitude) {
            mId = id;
            mLatitude = latitude;
            mLongitude = longitude;
        }

        public int getId() {
            return mId;
        }

        public double getLatitude() {
            return mLatitude;
        }

        public double getLongitude() {
            return mLongitude;
        }
    }

    public static class OnSuccess {
        private ItemModel mItem;

        public OnSuccess(ItemModel item) {
            mItem = item;
        }

        public ItemModel getItem() {
            return mItem;
        }
    }

    public static class OnFailure {
        public OnFailure() { }
    }
}