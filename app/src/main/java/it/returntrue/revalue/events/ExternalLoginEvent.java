package it.returntrue.revalue.events;

import it.returntrue.revalue.api.ExternalTokenModel;
import it.returntrue.revalue.api.TokenModel;

public class ExternalLoginEvent {
    public static class OnStart {
        private ExternalTokenModel mExternalTokenModel;

        public OnStart(ExternalTokenModel externalTokenModel) {
            mExternalTokenModel = externalTokenModel;
        }

        public ExternalTokenModel getExternalTokenModel() {
            return mExternalTokenModel;
        }
    }

    public static class OnSuccess {
        private TokenModel mTokenModel;

        public OnSuccess(TokenModel tokenModel) {
            mTokenModel = tokenModel;
        }

        public TokenModel getTokenModel() {
            return mTokenModel;
        }
    }

    public static class OnFailure {
        private String mMessage;

        public OnFailure(String message) {
            mMessage = message;
        }

        public String getMessage() {
            return mMessage;
        }
    }
}