/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

import it.returntrue.revalue.api.FcmTokenModel;

/**
 * Represents a data bus event for UpdateFcmToken API call
 * */
@SuppressWarnings("ALL")
public class UpdateFcmTokenEvent {
    public static class OnStart {
        private final FcmTokenModel mTokenModel;

        public OnStart(FcmTokenModel tokenModel) {
            mTokenModel = tokenModel;
        }

        public FcmTokenModel getTokenModel() {
            return mTokenModel;
        }
    }

    public static class OnSuccess { }

    public static class OnFailure { }
}