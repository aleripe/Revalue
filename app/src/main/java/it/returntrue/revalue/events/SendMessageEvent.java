package it.returntrue.revalue.events;

import it.returntrue.revalue.api.MessageModel;

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