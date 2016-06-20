/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

import it.returntrue.revalue.api.MessageModel;

/**
 * Represents a data bus event for SendMessage API call
 * */
@SuppressWarnings("ALL")
public class SendMessageEvent {
    public static class OnStart {
        private final MessageModel mMessageModel;

        public OnStart(MessageModel messageModel) {
            mMessageModel = messageModel;
        }

        public MessageModel getMessageModel() {
            return mMessageModel;
        }
    }

    public static class OnSuccess {
        private final MessageModel mMessageModel;

        public OnSuccess(MessageModel messageModel) {
            mMessageModel = messageModel;
        }

        public MessageModel getMessageModel() {
            return mMessageModel;
        }
    }

    public static class OnFailure {
        public OnFailure() { }
    }
}