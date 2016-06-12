package it.returntrue.revalue.events;

import it.returntrue.revalue.api.MessageModel;

public class SendMessageEvent {
    public static class OnStart {
        private MessageModel mMessageModel;

        public OnStart(MessageModel messageModel) {
            mMessageModel = messageModel;
        }

        public MessageModel getMessageModel() {
            return mMessageModel;
        }
    }

    public static class OnSuccess {
        public OnSuccess() { }
    }

    public static class OnFailure {
        public OnFailure() { }
    }
}