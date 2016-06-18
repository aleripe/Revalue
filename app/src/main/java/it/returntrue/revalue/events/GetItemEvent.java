package it.returntrue.revalue.events;

import it.returntrue.revalue.api.ItemModel;

@SuppressWarnings("ALL")
public class GetItemEvent {
    public static class OnStart {
        private final int mId;
        private final double mLatitude;
        private final double mLongitude;

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
        private final ItemModel mItem;

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