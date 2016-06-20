/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

/**
 * Represents a data bus event for SetItemAsRemoved API call
 * */
@SuppressWarnings("ALL")
public class SetItemAsRemovedEvent {
    public static class OnStart {
        private final int mId;

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