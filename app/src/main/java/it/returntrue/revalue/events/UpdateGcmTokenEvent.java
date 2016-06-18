package it.returntrue.revalue.events;

import it.returntrue.revalue.api.GcmTokenModel;

@SuppressWarnings("ALL")
public class UpdateGcmTokenEvent {
    public static class OnStart {
        private final GcmTokenModel mTokenModel;

        public OnStart(GcmTokenModel tokenModel) {
            mTokenModel = tokenModel;
        }

        public GcmTokenModel getTokenModel() {
            return mTokenModel;
        }
    }

    public static class OnSuccess { }

    public static class OnFailure { }
}