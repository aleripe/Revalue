/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

/**
 * Represents a data bus event for chat messages request
 * */
@SuppressWarnings("ALL")
public class ViewChatEvent {
    public static class OnStart {
        private int mUserId;

        public OnStart(int id) {
            mUserId = id;
        }

        public int getUserId() {
            return mUserId;
        }
    }

    public static class OnSuccess {
        public OnSuccess() { }
    }

    public static class OnFailure {
        public OnFailure() { }
    }
}