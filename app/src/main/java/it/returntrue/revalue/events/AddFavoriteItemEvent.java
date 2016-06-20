/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

/**
 * Represents a data bus event for AddFavoriteItem API call
 * */
@SuppressWarnings("ALL")
public class AddFavoriteItemEvent {
    public static class OnStart {
        private int mId;

        public OnStart(int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }
    }

    public static class OnSuccess {
        public OnSuccess() { }
    }

    public static class OnFailure {
        public OnFailure() { }
    }
}