package it.returntrue.revalue.events;

import it.returntrue.revalue.api.GcmTokenModel;

public class UpdateGcmTokenEvent {
    public static class OnStart {
        private GcmTokenModel mTokenModel;

        public OnStart(GcmTokenModel tokenModel) {
            mTokenModel = tokenModel;
        }

        public GcmTokenModel getTokenModel() {
            return mTokenModel;
        }
    }
}